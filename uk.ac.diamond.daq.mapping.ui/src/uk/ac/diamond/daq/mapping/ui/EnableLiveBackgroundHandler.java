/*-
 * Copyright © 2019 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.diamond.daq.mapping.ui;

import java.util.Map;
import java.util.Optional;

import org.dawnsci.mapping.ui.api.IMapFileController;
import org.dawnsci.mapping.ui.datamodel.LiveStreamMapObject;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.commands.State;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.eclipse.ui.menus.UIElement;

import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.api.ILiveStreamConnectionService;


/**
 * Handler for toolbar {@link Button} to enable a live stream background in the mapping view if one is configured. In
 * order to cope with the situation where the required stream has not been configured, the handler must manage the
 * toggle state of the button itself by retrieving the {@link State} from the {@link Command} object first, storing it,
 * updating it if required/possible and then broadcasting a refresh back to the {@link UIElement} in the
 * {@link #updateElement} method of the {@link IElementUpdater} interface
 *
 * @since GDA9.12
 */
public class EnableLiveBackgroundHandler implements IHandler, IElementUpdater {
	private boolean initialised = false;
	private State toggleState;
	private LiveStreamMapObject liveStreamMap;
	private IWorkbench workbench = PlatformUI.getWorkbench();
	private IMapFileController mapFileController = workbench.getService(IMapFileController.class);
	private ICommandService commandService = workbench.getService(ICommandService.class);

	/**
	 * Handles the button press. The stream connection is first initialised; if this is the first time execute has been
	 * called. Next, the current toggle state is retrieved and stored so that it can be used later to refresh the button
	 * widget regardless of whether is has been changed or not. Based on this, {@link LiveStreamMapObject},
	 * {@link IMapFileController} and the state of the toggle are then updated. Finally the button widget is refreshed
	 * according to the 'new' toggleState.
	 *
	 * @param event		An {@link ExecutionEvent} object that references the {@link Command} associated with the GUI
	 * 					{@link Button}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Command buttonCommand = event.getCommand();
		if (!initialised) {
			initialiseStream();
		}

		toggleState = buttonCommand.getState(RegistryToggleState.STATE_ID);
		updateIfInitialised();
		commandService.refreshElements(buttonCommand.getId(), null);
		return null;
	}

	/**
	 * Updates the Mapping View and the toggle button command state once the stream has been initialised
	 */
	private void updateIfInitialised() {
		if (initialised) {
			toggleState.setValue(!toggleStateAsBool());
			liveStreamMap.setPlotted(toggleStateAsBool());
			mapFileController.registerUpdates(null);
		}
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
		liveStreamMap = null;
		commandService = null;
		mapFileController = null;
		workbench = null;
		toggleState = null;
	}

	/**
	 * Broadcast the new state to the UI Button as a result of the refresh triggered by {@link #execute}
	 *
	 * @param element		An element representing the GUI toggle button
	 * @param parameters	Any parameters registered with the callback
	 */
	@Override
	public void updateElement(UIElement element, Map parameters) {
		if (toggleState != null) {
			element.setChecked(toggleStateAsBool());
		}
	}

	/**
	 *  Establishes the Live stream connection and links it to the mapping view. If no suitable default connection has
	 *  been defined a dialog is displayed informing the user what should be done to correct this.
	 *
	 *  @throws ExecutionException if the attempt to connect to the live stream fails
	 */
	private void initialiseStream() throws ExecutionException {
		ILiveStreamConnectionService connectionService = workbench.getService(ILiveStreamConnectionService.class);
		IMapFileController mapFileController = workbench.getService(IMapFileController.class);

		try {
			Optional<LiveStreamMapObject> sourceOptional = connectionService.getDefaultStreamSource();
			if (sourceOptional.isPresent()) {
				liveStreamMap = sourceOptional.get();
				mapFileController.addLiveStream(liveStreamMap);
				initialised = true;
				return;
			}
		} catch(LiveStreamException e) {
			throw new ExecutionException("Could not connect to specified live stream source", e);
		}

		MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				"Missing Camera Configuration",
				"No default Camera Configuration is set,\n"
				+ "Please add the name of a valid CameraConfiguration bean\n"
				+ "to the mapping_stage_info bean in your mapping.xml file");
	}

	/**
	 * Convenience method to convert the {@link Command} {@link State} to a {@link Boolean}. This cannot just be a
	 * member variable as the boolean value needs to be re-evaluated from the value of the {@link State} at the time of
	 * conversion
	 *
	 * @return	The current value of the {@link Command} {@link State} as a {@link Boolean}
	 */
	private Boolean toggleStateAsBool() {
		return (Boolean)toggleState.getValue();
	}
}
