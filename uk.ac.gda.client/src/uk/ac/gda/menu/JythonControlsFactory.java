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

import static gda.jython.JythonStatus.IDLE;
import static gda.jython.JythonStatus.PAUSED;
import static gda.jython.JythonStatus.RUNNING;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.ResourceManager;

import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;
import gda.jython.JythonServerStatus;
import gda.jython.JythonStatus;
import gda.jython.commandinfo.ICommandThreadObserver;
import gda.rcp.GDAClientActivator;
import gda.scan.Scan.ScanStatus;
import uk.ac.gda.preferences.PreferenceConstants;

/**
 * We have to implement these actions in code because they connect to the server and
 * have complex logic as to when they are enabled.
 */
public class JythonControlsFactory extends ExtensionContributionFactory {
	private static final Logger logger = LoggerFactory.getLogger(JythonControlsFactory.class);

	private static ActionContributionItem pauseScan;
	private static ActionContributionItem finishScanEarly;
	private static final ActionUpdater actionUpdater = new ActionUpdater();

	public static void enableUIControls() {
		logger.trace("Enabling scan UI controls");
		updateControls(RUNNING, RUNNING);
	}

	public static void disableUIControls() {
		logger.trace("Disabling scan UI controls");
		updateControls(IDLE, IDLE);
	}

	private static void updateControls(JythonStatus script, JythonStatus scan) {
		logger.trace("Update controls called with script: {}, scan: {}", script, scan);
		boolean pauseEnabled = (script != IDLE) || (scan != IDLE);
		boolean pauseChecked = script == PAUSED || scan == PAUSED;

		boolean fastForwardEnabled = scan != IDLE;
		boolean fastForwardChecked = false; // Never checked

		logger.trace("Updating controls, pause enabled: {}, pause checked: {}, fastForward enabled: {}",
				pauseEnabled, pauseChecked, fastForwardEnabled);
		updateControl(pauseScan, pauseEnabled, pauseChecked);
		updateControl(finishScanEarly, fastForwardEnabled, fastForwardChecked);
	}

	private static void updateControl(ActionContributionItem item, boolean enabled, boolean checked) {
		if (item != null) {
			item.getAction().setEnabled(enabled);
			item.getAction().setChecked(checked);
		}
	}

	@Override
	public void createContributionItems(final IServiceLocator serviceLocator, IContributionRoot additions) {

		additions.addContributionItem(new Separator(), Expression.TRUE);

		finishScanEarly = createFinishEarlyAction(serviceLocator, "Finish scan early", "uk.ac.gda.client.jython.FinishScanEarly", "/control_fastforward_blue.png");
		additions.addContributionItem(finishScanEarly, Expression.TRUE);

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

		var state = JythonServerFacade.getInstance().getServerStatus();
		updateControls(state.scriptStatus, state.scanStatus);
		InterfaceProvider.getJSFObserver().addIObserver(actionUpdater);
	}

	private ActionContributionItem createFinishEarlyAction(final IServiceLocator serviceLocator, final String label, final String commandId, final String iconPath) {
		final ActionContributionItem finishEarly = new ActionContributionItem(new Action(label, SWT.NONE) {
			@Override
			public void run() {
				try {
					serviceLocator.getService(IHandlerService.class).executeCommand(commandId, new Event());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		finishEarly.getAction().setImageDescriptor(ResourceManager.getImageDescriptor(JythonControlsFactory.class, iconPath));
		return finishEarly;
	}

	private ActionContributionItem createPauseAction(final IServiceLocator serviceLocator, final String label, final String commandId, final String iconPath) {
		final ActionContributionItem pause = new ActionContributionItem(new Action(label, SWT.TOGGLE) {
			@Override
			public void run() {
				try {
					setChecked(false); //force this to only be checked by scan events
					serviceLocator.getService(IHandlerService.class).executeCommand(commandId, new Event());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		pause.getAction().setImageDescriptor(ResourceManager.getImageDescriptor(JythonControlsFactory.class, iconPath));
		return pause;
	}

	private static class ActionUpdater implements ICommandThreadObserver {
		private volatile JythonStatus scan = IDLE;
		private volatile JythonStatus script = IDLE;

		@Override
		public void update(Object source, Object update) {
			logger.trace("Update {} from {}", update, source);
			if (update instanceof ScanStatus) {
				ScanStatus status = (ScanStatus) update;
				updateScanStatus(status);
			} else if (update instanceof JythonServerStatus) {
				JythonServerStatus event = (JythonServerStatus) update;
				script = event.scriptStatus;
			}
			updateControlStates();
		}

		private void updateScanStatus(ScanStatus status) {
			switch(status) {
			case RUNNING:
				scan = RUNNING;
				break;
			case PAUSED:
				scan = PAUSED;
				break;
			default:
				scan = IDLE;
				break;
			}
		}

		private void updateControlStates() {
			logger.trace("State now (script {}, scan: {})", script, scan);
			updateControls(script, scan);

		}
	}
}