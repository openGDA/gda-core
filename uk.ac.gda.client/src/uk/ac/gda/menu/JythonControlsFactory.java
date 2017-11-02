/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.menu;

import org.eclipse.core.expressions.Expression;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

import com.swtdesigner.ResourceManager;

import gda.rcp.GDAClientActivator;
import uk.ac.gda.preferences.PreferenceConstants;

/**
 * We have to implement these actions in code because they connect to the server and
 * have complex logic as to when they are enabled.
 */
public class JythonControlsFactory extends ExtensionContributionFactory {

	private static ActionContributionItem pauseScan;
	private static ActionContributionItem haltScan;
	private static Boolean controlsEnabled = true;

	public static void enableUIControls(){
		controlsEnabled = true;
		enableControls();
	}

	private static void enableControls(){
		enableControl(pauseScan);
		enableControl(haltScan);
	}

	private static void enableControl(ActionContributionItem item) {
		if (item != null) {
			item.getAction().setEnabled(controlsEnabled);
		}
	}

	public static void disableUIControls(){
		controlsEnabled = false;
		enableControls();
	}

	@Override
	public void createContributionItems(final IServiceLocator serviceLocator, IContributionRoot additions) {

		additions.addContributionItem(new Separator(), Expression.TRUE);

		haltScan = createHaltAction(serviceLocator, "Fast forward to end of scan", "uk.ac.gda.client.jython.HaltScan", "/control_fastforward_blue.png");
		additions.addContributionItem(haltScan, Expression.TRUE);

		additions.addContributionItem(new Separator(), Expression.TRUE);

		pauseScan = createPauseAction(serviceLocator, "Pause Current Scan/Script", "uk.ac.gda.client.jython.PauseScan", "/control_pause_blue.png");
		additions.addContributionItem(pauseScan, Expression.TRUE);

		additions.addContributionItem(new Separator(), Expression.TRUE);

		CommandContributionItemParameter abortCommandsAction = new CommandContributionItemParameter(serviceLocator, null, "uk.ac.gda.client.AbortCommands", null, ResourceManager.getImageDescriptor(JythonControlsFactory.class, "/control_stop_blue.png"), null, null, "Abort all running commands, scripts and scans", null, null, SWT.PUSH, null, false);
		final CommandContributionItem    abortCommandsItem = new CommandContributionItem(abortCommandsAction);
		additions.addContributionItem(abortCommandsItem, Expression.TRUE);

		// TODO remove this temporary solution which gives beamline time to switch to the new StoAll on the main toolbar.
		final CommandContributionItemParameter beamlineHaltAction = new CommandContributionItemParameter(serviceLocator, null, "uk.ac.gda.client.StopAllCommand", null, ResourceManager.getImageDescriptor(JythonControlsFactory.class, "/stop.png"), null, null, "Call stop on all beamline commands and hardware", null, null, SWT.PUSH, null, false);
		final CommandContributionItem beamlineHaltItem = new CommandContributionItem(beamlineHaltAction);
		if (GDAClientActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.GDA_SHOW_PANIC_STOP_ON_JYTHON_CONSOLE)) {
			additions.addContributionItem(beamlineHaltItem, Expression.TRUE);
		}

		additions.addContributionItem(new Separator(), Expression.TRUE);
	}

	private ActionContributionItem createHaltAction(final IServiceLocator serviceLocator, final String label, final String commandId, final String iconPath) {
		final ActionContributionItem halt = new ActionContributionItem(new Action(label, SWT.NONE) {
			@Override
			public void run() {
				try {
					((IHandlerService)serviceLocator.getService(IHandlerService.class)).executeCommand(commandId, new Event());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		halt.getAction().setImageDescriptor(ResourceManager.getImageDescriptor(JythonControlsFactory.class, iconPath));
		return halt;
	}

	private ActionContributionItem createPauseAction(final IServiceLocator serviceLocator, final String label, final String commandId, final String iconPath) {
		final ActionContributionItem pause = new ActionContributionItem(new Action(label, SWT.TOGGLE) {
			@Override
			public void run() {
				try {
					final Boolean isPaused = (Boolean)((IHandlerService)serviceLocator.getService(IHandlerService.class)).executeCommand(commandId, new Event());
					setChecked(isPaused);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		pause.getAction().setImageDescriptor(ResourceManager.getImageDescriptor(JythonControlsFactory.class, iconPath));
		return pause;
	}
}