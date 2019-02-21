/*-
 * Copyright © 2019 Diamond Light Source Ltd.
 *
 * Part of GDA @since GDA 9.12
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

package uk.ac.gda.client.actions;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.eclipse.ui.menus.UIElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.JythonServerFacade;
import gda.observable.IObserver;
import gda.rcp.GDAClientActivator;
import gda.scan.ScanEvent;

/**
 * Handle the GDA client SoundToggle behaviour.
 *
 * The sound toggle defaults to off, but is stored in the workspace so will be restored whenever the GDA client starts.
 *
 * Rather than showing state using a single icon this Handler switches icon for sound enabled and disabled states, using
 * the IElementUpdater interface.
 *
 * It also handles the monitoring of FINISHED ScanEvents through the IObserver interface.
 *
 * Implementation based on https://wiki.eclipse.org/Menu_Contributions/Toggle_Button_Command and
 * https://web.archive.org/web/20180311233946/http://www.robertwloch.net:80/2011/01/eclipse-tips-tricks-label-updating-command-handler/
 */
public class SoundToggleHandler extends AbstractHandler implements IElementUpdater {

	private static final Logger logger = LoggerFactory.getLogger(SoundToggleHandler.class);

	public static final String ID = "uk.ac.gda.client.sound.toggle";

	private String enabledText = null;

	private final IObserver soundToggleObserver = this::update;

	// AbstractHandler

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		HandlerUtil.toggleCommandState(event.getCommand());

		// Trigger updateElement to configure button and observer for new state
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		ICommandService commandService = window.getService(ICommandService.class);
		if (commandService != null) {
			commandService.refreshElements(ID, null);
		}

		// Must return null
		return null;
	}

	@Override
	public void dispose() {
		JythonServerFacade.getInstance().deleteScanEventObserver(soundToggleObserver);
		super.dispose();
	}

	// IElementUpdater

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		Command toggleCommand = element.getServiceLocator().getService(ICommandService.class).getCommand(ID);
		Boolean state = (Boolean) toggleCommand.getState(RegistryToggleState.STATE_ID).getValue();

		// The element is resized when the text changes, but the frame it's inside isn't, so make sure that the first
		if (enabledText == null) { // text set on the element is the largest, by adding an en-space if shorter.
			enabledText = "Beep at scan end" + (state ? "\u2002" : "");
		}
		if (state) {
			JythonServerFacade.getInstance().addScanEventObserver(soundToggleObserver);
			element.setIcon(GDAClientActivator.getImageDescriptor("icons/speaker-volume.png"));
			element.setTooltip("Push to disable the beep at the end of the scan");
			element.setText(enabledText);
			logger.debug("Beep after scan finish enabled");
		} else {
			JythonServerFacade.getInstance().deleteScanEventObserver(soundToggleObserver);
			element.setIcon(GDAClientActivator.getImageDescriptor("icons/speaker-volume-control-mute.png"));
			element.setTooltip("Push to enable the beep at the end of the scan");
			element.setText("No scan end beep");
			logger.debug("Beep after scan finish disabled");
		}
	}

	// Private functions

	private void update(Object source, Object arg) {
		if (arg instanceof ScanEvent) {
			ScanEvent scanEvent = (ScanEvent) arg;

			if (scanEvent.getType() == ScanEvent.EventType.FINISHED) {
				// BEEP to inform users that the scan has finished.
				PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
					logger.debug("======= system beep =======");
					PlatformUI.getWorkbench().getDisplay().beep();
				});
			}
		} else {
			logger.warn("Non ScanEvent update received: {} from {}", arg, source);
		}
	}
}
