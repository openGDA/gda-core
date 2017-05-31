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

package gda.rcp;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.handlers.IActionCommandMappingService;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.intro.IntroDescriptor;
import org.eclipse.ui.internal.provisional.application.IActionBarConfigurer2;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.texteditor.StatusLineContributionItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.RateLimiter;

import gda.commandqueue.Processor;
import gda.commandqueue.ProcessorCurrentItem;
import gda.commandqueue.Queue;
import gda.configuration.properties.LocalProperties;
import gda.jython.IJythonServerStatusObserver;
import gda.jython.InterfaceProvider;
import gda.jython.Jython;
import gda.jython.JythonServerStatus;
import gda.jython.authenticator.UserAuthentication;
import gda.jython.batoncontrol.BatonChanged;
import gda.jython.batoncontrol.BatonLeaseRenewRequest;
import gda.jython.batoncontrol.ClientDetails;
import gda.observable.IObserver;
import gda.rcp.views.GdaImages;
import gda.scan.Scan;
import gda.scan.ScanEvent;
import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.ui.status.LinkContributionItem;
import uk.ac.gda.views.baton.BatonView;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of the actions added to a workbench window.
 * Each window will be populated with new actions.
 */
// The suppress warnings is because we are using some provisional API (IActionBarConfigurer2)
// and need access to IDEWorkbenchMessages
@SuppressWarnings("restriction")
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
	public static final String NEW_GDA_EXT = "new.gda.ext"; // Group. //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(ApplicationActionBarAdvisor.class);

	/** Flag to add a developer test item to the File menu. The Action can be modified below. */
	private static final boolean USE_TEST_ACTION = false;

	private IWorkbenchWindow window;
	private ActionSetRegistry registry;

	// predefined actions
	private IWorkbenchAction closeAction;
	private IWorkbenchAction closeAllAction;

	private IWorkbenchAction saveAction;
	private IWorkbenchAction saveAllAction;
	private IWorkbenchAction saveAsAction;

	private IWorkbenchAction printAction;

	private IWorkbenchAction newWindowAction;
	private IWorkbenchAction newEditorAction;

	private IWorkbenchAction exitAction;
	private IWorkbenchAction openPreferencesAction;

	private IWorkbenchAction newWizardAction;

	private IWorkbenchAction introAction;
	private IWorkbenchAction helpAction;
	private IWorkbenchAction aboutAction;

	private IWorkbenchAction perspectiveCustomizeAction;
	private IWorkbenchAction perspectiveSaveAsAction;
	private IWorkbenchAction perspectiveResetAction;
	private IWorkbenchAction perspectiveCloseAction;
	private IWorkbenchAction perspectiveCloseAllAction;

	// generic retarget actions
	private IWorkbenchAction undoAction;
	private IWorkbenchAction redoAction;

	/** A test action for developers and experimentation */
	private Action testAction;
	private IWorkbenchAction exportWizardAction;
	private IWorkbenchAction importWizardAction;

	private final RateLimiter scanEventRateLimiter = RateLimiter.create(20); // 20 refreshes per sec max

	/**
	 * @param configurer
	 */
	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
		window = configurer.getWindowConfigurer().getWindow();
		registry = WorkbenchPlugin.getDefault().getActionSetRegistry();
	}

	@Override
	protected void makeActions(final IWorkbenchWindow window) {
		// Creates the actions and registers them.
		// Registering is needed to ensure that key bindings work.
		// The corresponding commands keybindings are defined in the plugin.xml file.
		// Registering also provides automatic disposal of the actions when
		// the window is closed.

		makeFileActions(window);
		makeEditActions(window);
		makeWindowActions(window);
		makeHelpActions(window);
		makeTestActions();

		// Some platform menus appear by default when certain workbench plug-ins are loaded
		// Here we manually remove an action sets if a use property is set to false for it

		// RUN_ACTION_SET may be required by pydev/jython perspective
		// testActionSetProperty(LocalProperties.GDA_GUI_USE_ACTIONS_RUN,RUN_ACTION_SET,true,true);
	}

	private void removeActionSet(String actionSetId) {
		IActionSetDescriptor actionSet = registry.findActionSet(actionSetId);
		if(null!=actionSet) {
			IExtension extension = actionSet.getConfigurationElement().getDeclaringExtension();
			if(null!=extension) {
				registry.removeExtension(extension,new Object[]{actionSet});
			}
		}
	}

	@SuppressWarnings("unused")
	private boolean testActionSetProperty(String propertyId, String actionSetId, boolean bDefault, boolean doRemove) {
		boolean useActionSet = LocalProperties.check(propertyId,bDefault);
		if(doRemove && !useActionSet) {
			removeActionSet(actionSetId);
		}
		return useActionSet;
	}

	private void makeHelpActions(final IWorkbenchWindow window) {
		IntroDescriptor introDescriptor = ((Workbench) window.getWorkbench()).getIntroDescriptor();
		if (introDescriptor == null)
			logger.debug("The Intro Action is not available. There doesn't appear to be a product/intro binding using the org.eclipse.ui.intro");
		else {
			introAction = ActionFactory.INTRO.create(window);
			register(introAction);
		}

		helpAction = ActionFactory.HELP_CONTENTS.create(window);
		register(helpAction);

		aboutAction = ActionFactory.ABOUT.create(window);
		register(aboutAction);
	}

	private void makeEditActions(final IWorkbenchWindow window) {
		undoAction = ActionFactory.UNDO.create(window);
		register(undoAction);

		redoAction = ActionFactory.REDO.create(window);
		register(redoAction);

		perspectiveCustomizeAction = ActionFactory.EDIT_ACTION_SETS.create(window);
		register(perspectiveCustomizeAction);
	}

	private void makeFileActions(final IWorkbenchWindow window) {
		closeAction = ActionFactory.CLOSE.create(window);
		register(closeAction);

		closeAllAction = ActionFactory.CLOSE_ALL.create(window);
		register(closeAllAction);

		saveAction = ActionFactory.SAVE.create(window);
		register(saveAction);

		saveAsAction = ActionFactory.SAVE_AS.create(window);
		register(saveAsAction);

		saveAllAction = ActionFactory.SAVE_ALL.create(window);
		register(saveAllAction);

		printAction = ActionFactory.PRINT.create(window);
		register(printAction);

		exitAction = ActionFactory.QUIT.create(window);
		register(exitAction);

		exportWizardAction = ActionFactory.EXPORT.create(window);
		register(exportWizardAction);

		importWizardAction = ActionFactory.IMPORT.create(window);
		register(importWizardAction);

		newWizardAction = ActionFactory.NEW.create(window);
		register(newWizardAction);
	}

	private void makeTestActions() {
		// Action to use for testing. Modify it as needed.
		if (USE_TEST_ACTION) {
			final Display display = window.getShell().getDisplay();
			testAction = new Action() {
				@Override
				public void run() {
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							// Help determine where properties are coming from
							String yyyVal = System.getProperty("yyy", "Not found");
							String zzzVal = System.getProperty("zzz", "Not found");
							// Generate the message
							String msg = "GDA_dev development version";
							msg += "\nzzz=" + zzzVal;
							msg += "\nyyy=" + yyyVal;
							// Bring up a MessageDialog
							MessageDialog.openInformation(display.getActiveShell(), "Information", msg);
						}
					});
				}
			};
			testAction.setText("Test...");
		}
	}

	private void makeWindowActions(final IWorkbenchWindow window) {
		newWindowAction = ActionFactory.OPEN_NEW_WINDOW.create(getWindow());
		newWindowAction.setText("New Window");
		register(newWindowAction);

		newEditorAction = ActionFactory.NEW_EDITOR.create(window);
		register(newEditorAction);

		openPreferencesAction = ActionFactory.PREFERENCES.create(window);
		register(openPreferencesAction);

		perspectiveSaveAsAction = ActionFactory.SAVE_PERSPECTIVE.create(window);
		register(perspectiveSaveAsAction);

		perspectiveResetAction = ActionFactory.RESET_PERSPECTIVE.create(window);
		register(perspectiveResetAction);

		perspectiveCloseAction = ActionFactory.CLOSE_PERSPECTIVE.create(window);
		register(perspectiveCloseAction);

		perspectiveCloseAllAction = ActionFactory.CLOSE_ALL_PERSPECTIVES.create(window);
		register(perspectiveCloseAllAction);
	}

	/**
	 * Fills the coolbar with the workbench actions.
	 */
	@Override
	protected void fillCoolBar(ICoolBarManager coolBar) {

		IActionBarConfigurer2 actionBarConfigurer = (IActionBarConfigurer2) getActionBarConfigurer();

		{ // File Group
			IToolBarManager fileToolBar = actionBarConfigurer.createToolBarManager();
			fileToolBar.add(new Separator(IWorkbenchActionConstants.NEW_GROUP));
			/*
			 * we need to ensure a wizard is on teh menu otherwise we expose a NPE in CustomisePerpespective
			 * perspective.setNewWizardActionIds(getVisibleIDs(wizards));
			 */
			fileToolBar.add(newWizardAction);
			fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
			fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.SAVE_GROUP));
			fileToolBar.add(saveAction);
			// NOTE Intentionally added, rich bean editors can change bean name. If not required
			// add new system property and set that to turn this off.
			fileToolBar.add(saveAsAction);

			fileToolBar.add(new Separator(IWorkbenchActionConstants.EDIT_START));
			fileToolBar.add(undoAction);
			fileToolBar.add(redoAction);
			fileToolBar.add(new Separator(IWorkbenchActionConstants.EDIT_END));

			fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
			// fileToolBar.add(getPrintItem());
			fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.PRINT_EXT));

			fileToolBar.add(new Separator(IWorkbenchActionConstants.BUILD_GROUP));
			fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.BUILD_EXT));
			fileToolBar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

			// Add to the cool bar manager
			coolBar.add(actionBarConfigurer.createToolBarContributionItem(fileToolBar,
					IWorkbenchActionConstants.TOOLBAR_FILE));
		}

		// coolBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		// coolBar.add(new GroupMarker(IIDEActionConstants.GROUP_NAV));
		// { // Navigate group
		// IToolBarManager navToolBar = actionBarConfigurer.createToolBarManager();
		// navToolBar.add(new Separator(
		// IWorkbenchActionConstants.HISTORY_GROUP));
		// navToolBar
		// .add(new GroupMarker(IWorkbenchActionConstants.GROUP_APP));
		// navToolBar.add(backwardHistoryAction);
		// navToolBar.add(forwardHistoryAction);
		// navToolBar.add(new Separator(IWorkbenchActionConstants.PIN_GROUP));
		// navToolBar.add(pinEditorContributionItem);
		//
		// // Add to the cool bar manager
		// coolBar.add(actionBarConfigurer.createToolBarContributionItem(navToolBar,
		// IWorkbenchActionConstants.TOOLBAR_NAVIGATE));
		// }
		//
		// coolBar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_EDITOR));
		//
		// coolBar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_HELP));
		//
		// { // Help group
		// IToolBarManager helpToolBar = actionBarConfigurer.createToolBarManager();
		// helpToolBar.add(new Separator(IWorkbenchActionConstants.GROUP_HELP));
		// // helpToolBar.add(searchComboItem);
		// // Add the group for applications to contribute
		// helpToolBar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_APP));
		// // Add to the cool bar manager
		// coolBar.add(actionBarConfigurer.createToolBarContributionItem(helpToolBar,
		// IWorkbenchActionConstants.TOOLBAR_HELP));
		// }

	}

	@Override
	protected void fillMenuBar(IMenuManager menuBar) {
		menuBar.add(createFileMenu());
		menuBar.add(createEditMenu());
		// NOTE: This is where extension menus are added.
		menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menuBar.add(createWindowMenu());
		menuBar.add(createHelpMenu());
	}

	@Override
	protected void fillStatusLine(final IStatusLineManager manager) {

		final StatusLineContributionItem logStatus = new StatusLineContributionItem("GDA-LoggedInUser", true, 15);
		logStatus.setText(UserAuthentication.getUsername());
		logStatus.setImage(GdaImages.getImage("user_gray.png"));
		manager.add(logStatus);

		final LinkContributionItem batonStatus = new LinkContributionItem("uk.ac.gda.baton.status",18);
		batonStatus.setToolTipText("Double click status to bring up baton manager.");
		batonStatus.setText("GDA-Baton");
		manager.add(batonStatus);

		batonStatus.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				try {
					final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					page.showView(BatonView.ID);
				} catch (Exception ne) {
					logger.error("Cannot open baton manager", ne);
				}
			}
		});

		final StatusLineContributionItem scanStatus = new StatusLineContributionItem("GDA-ScanStatus", true, 55);
		manager.add(scanStatus);

		final StatusLineContributionItem scriptStatus = new StatusLineContributionItem("GDA-ScriptStatus", true, 20);
		manager.add(scriptStatus);

		/*
		 * add contribution to show the state of the queue if one exists
		 */
		Queue queue = CommandQueueViewFactory.getQueue();
		final Processor processor = CommandQueueViewFactory.getProcessor();
		if (queue != null && processor != null) {
			final LinkContributionItem queueStatus = new LinkContributionItem("uk.ac.gda.queue.status",20);
			queueStatus.setToolTipText("Double click status to bring up command queue.");
			queueStatus.setText("GDA-QueueStatus");
			manager.add(queueStatus);
			queueStatus.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDoubleClick(MouseEvent e) {
					try {
						final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getActivePage();
						page.showView(CommandQueueViewFactory.ID);
					} catch (Exception ne) {
						logger.error("Cannot open command queue view", ne);
					}
				}
			});
			IObserver queueObserver = new IObserver() {

				private ProcessorCurrentItem getProcessorCurrentItem() {
					try {
						return processor.getCurrentItem();
					} catch (Exception e) {
						logger.error("Error getting processor current item", e);
					}
					return null;
				}


				// update the GUI based on state of processor
				void updateStatus(final Processor.STATE stateIn, final ProcessorCurrentItem item) {
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							Processor.STATE state = stateIn;
							if (state == null)
								state = getProcessorState();

							queueStatus.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
							switch (state) {
							case PROCESSING_ITEMS:
								queueStatus.setText("Running " + (item != null ? item.getDescription() : ""));
								queueStatus.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
								break;
							case UNKNOWN:
								queueStatus.setText("Queue - unknown");
								queueStatus.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_MAGENTA));
								break;
							case WAITING_QUEUE:
								queueStatus.setText("Queue - waiting");
								queueStatus.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
								break;
							case WAITING_START:
								queueStatus.setText("Queue - paused");
								queueStatus.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
								break;
							}
						}

						private Processor.STATE getProcessorState() {
							try {
								return processor.getState();
							} catch (Exception e) {
								logger.error("Error getting processor state", e);
							}
							return Processor.STATE.UNKNOWN;
						}

					});

				}

				@Override
				public void update(Object source, Object arg) {
					ProcessorCurrentItem item=null;
					if ( arg instanceof Processor.STATE) {
						if( ((Processor.STATE)arg) == Processor.STATE.PROCESSING_ITEMS){
							 item = getProcessorCurrentItem();
						}
						updateStatus((Processor.STATE) arg, item);
					}

				}

			};
			CommandQueueViewFactory.getProcessor().addIObserver(queueObserver);
			try {
				queueObserver.update(null, processor.getState());
			} catch (Exception e1) {
				logger.error("Error getting state of processor",e1);
			}
		}

		final IJythonServerStatusObserver serverObserver = new IJythonServerStatusObserver() {
			@Override
			public void update(Object theObserved, final Object changeCode) {
				if (changeCode instanceof BatonChanged) {
					updateBatonStatus(batonStatus);
				} else if (changeCode instanceof BatonLeaseRenewRequest) {
					// Cause the baton to be renewed by this client - Seems like a odd place for this?
					InterfaceProvider.getBatonStateProvider().amIBatonHolder();
				} else if (changeCode instanceof JythonServerStatus || changeCode instanceof Scan.ScanStatus) {
					updateScriptStatus(scriptStatus);
				// If its a scan event limit the rate of GUI updates
				} else if (changeCode instanceof ScanEvent && scanEventRateLimiter.tryAcquire()) {
					updateScanDetails(scanStatus, (ScanEvent) changeCode);
					updateScriptStatus(scriptStatus);
				}
			}
		};

		// NOTE these methods connect to Jython server which may be down.
		// If throw exception from fillStatusLine(...) whole client fails.
		try {
			InterfaceProvider.getJSFObserver().addIObserver(serverObserver);
			InterfaceProvider.getScanDataPointProvider().addScanEventObserver(serverObserver);
			updateBatonStatus(batonStatus);
			updateScanStatus(scanStatus);
			updateScriptStatus(scriptStatus);
		} catch (Exception ne) {
			logger.error("Cannot connect to JythonServerFacade", ne);
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Cannot Connect to GDA Server",
							"The GDA Server is not responding.\n\nPlease contact your GDA support engineer.");
				}
			});
		}

		ApplicationWorkbenchAdvisor.addCleanupWork(new ApplicationWorkbenchAdvisor.CleanupWork() {
			@Override
			public void cleanup() {
				InterfaceProvider.getJSFObserver().deleteIObserver(serverObserver);
			}
		});
	}

	private void updateScanDetails(StatusLineContributionItem status, ScanEvent changeCode) {
		String message = changeCode.toProgressString();
		switch (changeCode.getLatestStatus()) {
		case COMPLETED_AFTER_FAILURE:
		case COMPLETED_AFTER_STOP:
		case COMPLETED_EARLY:
			setStatusLineText(status, message, "control_stop_blue.png");
			break;
		case PAUSED:
			setStatusLineText(status, message, "control_pause_blue.png");
			break;
		case NOTSTARTED:
		case RUNNING:
			setStatusLineText(status, message, "computer_go.png");
			break;
		case FINISHING_EARLY:
		case TIDYING_UP_AFTER_FAILURE:
		case TIDYING_UP_AFTER_STOP:
		case COMPLETED_OKAY:
		default:
			setStatusLineText(status, message);
			break;
		}
	}

	private void updateScanStatus(StatusLineContributionItem status) {
		final int scan = InterfaceProvider.getScanStatusHolder().getScanStatus();
		updateStatus(status, scan, "Scan");
	}

	private void updateScriptStatus(StatusLineContributionItem status) {
		final int script = InterfaceProvider.getScriptController().getScriptStatus();
		updateStatus(status, script, "Script");
	}

	private void updateStatus(StatusLineContributionItem status, int scan, final String postFix) {
		if (scan == Jython.IDLE) {
			setStatusLineText(status, "No " + postFix + " running");
		} else if (scan == Jython.PAUSED) {
			setStatusLineText(status, "Paused " + postFix, "control_pause_blue.png");
		} else if (scan == Jython.RUNNING) {
			setStatusLineText(status, "Running " + postFix, "computer_go.png");
		}
	}

	private void updateBatonStatus(LinkContributionItem batonStatus) {
		final String text;
		final String tooltip;
		final Image image;
		if (InterfaceProvider.getBatonStateProvider().isBatonHeld()) {
			final ClientDetails holder = InterfaceProvider.getBatonStateProvider().getBatonHolder();
			final boolean isHolder = InterfaceProvider.getBatonStateProvider().amIBatonHolder();
			if (isHolder) {
				text = "Baton held";
				image = null;
				tooltip = "You hold the baton.\nDouble click to open baton manager.";
			} else {
				text = "Baton not held!";
				image = GdaImages.getImage("delete.png");
				if (holder.getUserID().equals(UserAuthentication.getUsername())) {
					tooltip = "Another client logged on as " + holder.getUserID()
							+ " has the baton.\nDouble click to open baton manager.";
				} else {
					tooltip = "You do not hold the baton.\nDouble click to open baton manager.";
				}
			}

		} else {
			text = "Baton not held";
			image = GdaImages.getImage("delete.png");
			tooltip = "";
		}

		// Update the GUI this needs to be in the UI thread
		window.getShell().getDisplay().asyncExec(() -> {
			batonStatus.setText(text);
			batonStatus.setImage(image);
			batonStatus.setToolTipText(tooltip);
		});
	}

	private void setStatusLineText(final StatusLineContributionItem status, final String text) {
		setStatusLineText(status, text, null);
	}

	private void setStatusLineText(final StatusLineContributionItem status, final String text, final String imageName) {

		final Image image = GdaImages.getImage(imageName);
		final IStatusLineManager man = getActionBarConfigurer().getStatusLineManager();

		// Update the GUI this needs to be in the UI thread
		window.getShell().getDisplay().asyncExec(() -> {
			status.setText(text);
			status.setImage(image);
			man.update(true); // makes new widgets
		});
	}

	/**
	 * Returns the window to which this action builder is contributing.
	 */
	private IWorkbenchWindow getWindow() {
		return window;
	}

	/**
	 * Creates and returns the File menu
	 */
	private MenuManager createFileMenu() {

		MenuManager menu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
		// menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
		// {
		// // create the New submenu, using the same id for it as the New action
		// String newText = "&New";
		// String newId = ActionFactory.NEW.getId();
		// MenuManager newMenu = new MenuManager(newText, newId);
		////            newMenu.setActionDefinitionId("org.eclipse.ui.file.newQuickMenu"); //$NON-NLS-1$
		// newMenu.add(new Separator(newId));
		// // this.newWizardMenu = new NewWizardMenu(getWindow());
		// // newMenu.add(this.newWizardMenu);
		// newMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		// menu.add(newMenu);
		// }

		// We disable the standard "new" extension marker and replace with a gda one.
		// This is done in order to provide our own implementation of OpenLocalFileAction
		// (which is normally provided by org.eclipse.ui.ide) which does not allow easy
		// changes to default location. So we replace with one of our own.
		// menu.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
		menu.add(new GroupMarker(NEW_GDA_EXT));

		menu.add(new Separator());
		menu.add(closeAction);
		menu.add(closeAllAction);
		// menu.add(closeAllSavedAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.CLOSE_EXT));
		menu.add(new Separator());
		menu.add(saveAction);
		menu.add(saveAsAction);
		menu.add(saveAllAction);
		// menu.add(getRevertItem());
		// menu.add(new Separator());
		// menu.add(getMoveItem());
		// menu.add(getRenameItem());
		// menu.add(getRefreshItem());
		//
		// menu.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
		menu.add(new Separator());
		menu.add(printAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.PRINT_EXT));
		menu.add(new Separator());
		// menu.add(openWorkspaceAction);
		// menu.add(new GroupMarker(IWorkbenchActionConstants.OPEN_EXT));
		// menu.add(new Separator());
		// menu.add(importResourcesAction);
		// menu.add(exportResourcesAction);

		menu.add(new GroupMarker(IWorkbenchActionConstants.IMPORT_EXT));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		// menu.add(new Separator());
		// menu.add(getPropertiesItem());

		menu.add(ContributionItemFactory.REOPEN_EDITORS.create(getWindow()));
		menu.add(new GroupMarker(IWorkbenchActionConstants.MRU));
		menu.add(new Separator());
		if(LocalProperties.check(LocalProperties.GDA_GUI_USE_ACTIONS_EXPORT,true)) {
			menu.add(exportWizardAction);
		}
		if(LocalProperties.check(LocalProperties.GDA_GUI_USE_ACTIONS_IMPORT,true)) {
			menu.add(importWizardAction);
		}

		menu.add(new Separator());
		// Add the test action here
		if (USE_TEST_ACTION) {
			menu.add(new Separator());
			menu.add(testAction);
		}
		// Add NewWizardAction, appears as  "Other..." on menu
		if(LocalProperties.check(LocalProperties.GDA_GUI_USE_ACTIONS_NEW,true)) {
			menu.add(newWizardAction);
		}

		// If we're on OS X we shouldn't show this command in the File menu. It
		// should be invisible to the user. However, we should not remove it -
		// the carbon UI code will do a search through our menu structure
		// looking for it when Cmd-Q is invoked (or Quit is chosen from the
		// application menu.
		ActionContributionItem quitItem = new ActionContributionItem(exitAction);
		quitItem.setVisible(!"carbon".equals(SWT.getPlatform())); //$NON-NLS-1$
		menu.add(new Separator());
		menu.add(quitItem);
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));

		return menu;
	}

	/**
	 * Creates and returns the Edit menu.
	 */
	private MenuManager createEditMenu() {
		MenuManager menu = new MenuManager("&Edit", IWorkbenchActionConstants.M_EDIT);
		menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));

		menu.add(undoAction);
		menu.add(redoAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.UNDO_EXT));
		menu.add(new Separator());

		// menu.add(getCutItem());
		// menu.add(getCopyItem());
		// menu.add(getPasteItem());
		menu.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));
		menu.add(new Separator());

		// menu.add(getDeleteItem());
		// menu.add(getSelectAllItem());
		menu.add(new Separator());

        menu.add(getCutItem());
        menu.add(getCopyItem());
        menu.add(getPasteItem());
        menu.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));
        menu.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));
        menu.add(new Separator());

        menu.add(getDeleteItem());
        menu.add(getSelectAllItem());
        menu.add(new Separator());

		menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		return menu;
	}

	/**
	 * Creates and returns the Window menu.
	 */
	private MenuManager createWindowMenu() {
		MenuManager menu = new MenuManager("&Window", IWorkbenchActionConstants.M_WINDOW);

		if(LocalProperties.check(LocalProperties.GDA_GUI_USE_ACTIONS_NEW_WINDOW,true)) {
			menu.add(newWindowAction);
		}
		if(LocalProperties.check(LocalProperties.GDA_GUI_USE_ACTIONS_NEW_EDITOR,false)) {
			menu.add(newEditorAction);
		}

		menu.add(new Separator());
		addPerspectiveActions(menu);

		menu.add(new Separator());
		// addKeyboardShortcuts(menu);

		Separator sep = new Separator(IWorkbenchActionConstants.MB_ADDITIONS);
		sep.setVisible(!"carbon".equals(SWT.getPlatform())); //$NON-NLS-1$
		menu.add(sep);

		// See the comment for quit in createFileMenu
		ActionContributionItem openPreferencesItem = new ActionContributionItem(openPreferencesAction);
		openPreferencesItem.setVisible(!"carbon".equals(SWT.getPlatform())); //$NON-NLS-1$
		menu.add(openPreferencesItem);

		menu.add(ContributionItemFactory.OPEN_WINDOWS.create(getWindow()));
		return menu;
	}

	/**
	 * Adds the perspective actions to the specified menu.
	 */
	private void addPerspectiveActions(MenuManager menu) {
		{
			String openText = IDEWorkbenchMessages.Workbench_openPerspective;
			MenuManager changePerspMenuMgr = new MenuManager(openText, "openPerspective"); //$NON-NLS-1$
			IContributionItem changePerspMenuItem = ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(getWindow());
			changePerspMenuMgr.add(changePerspMenuItem);
			menu.add(changePerspMenuMgr);
		}
		{
			MenuManager showViewMenuMgr = new MenuManager(IDEWorkbenchMessages.Workbench_showView, "showView"); //$NON-NLS-1$
			IContributionItem showViewMenu = ContributionItemFactory.VIEWS_SHORTLIST.create(getWindow());
			showViewMenuMgr.add(showViewMenu);
			menu.add(showViewMenuMgr);
		}

		menu.add(new Separator());
		menu.add(perspectiveResetAction);
		if(LocalProperties.check(LocalProperties.GDA_GUI_USE_ACTIONS_PERSPECTIVE_CUSTOM,true)) {
			menu.add(perspectiveCustomizeAction);
			menu.add(perspectiveSaveAsAction);
			menu.add(perspectiveCloseAction);
			menu.add(perspectiveCloseAllAction);
		}

	}

	/**
	 * Creates and returns the Help menu.
	 */
	private MenuManager createHelpMenu() {
		MenuManager menu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
		addSeparatorOrGroupMarker(menu, "group.intro"); //$NON-NLS-1$
		// See if a welcome or intro page is specified
		if (introAction != null) {
			menu.add(introAction);
		} /*
		 * else if (quickStartAction != null) { menu.add(quickStartAction); }
		 */
		menu.add(helpAction);
		menu.add(new Separator());

		menu.add(new GroupMarker("group.intro.ext")); //$NON-NLS-1$
		addSeparatorOrGroupMarker(menu, "group.main"); //$NON-NLS-1$
		// menu.add(helpContentsAction);
		// menu.add(helpSearchAction);
		// menu.add(dynamicHelpAction);
		//		addSeparatorOrGroupMarker(menu, "group.assist"); //$NON-NLS-1$
		// // See if a tips and tricks page is specified
		// if (tipsAndTricksAction != null) {
		// menu.add(tipsAndTricksAction);
		// }
		// HELP_START should really be the first item, but it was after
		// quickStartAction and tipsAndTricksAction in 2.1.
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));
		menu.add(new GroupMarker("group.main.ext")); //$NON-NLS-1$
		addSeparatorOrGroupMarker(menu, "group.tutorials"); //$NON-NLS-1$
		addSeparatorOrGroupMarker(menu, "group.tools"); //$NON-NLS-1$
		addSeparatorOrGroupMarker(menu, "group.updates"); //$NON-NLS-1$
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
		addSeparatorOrGroupMarker(menu, IWorkbenchActionConstants.MB_ADDITIONS);
		// about should always be at the bottom
		menu.add(new Separator("group.about")); //$NON-NLS-1$

		menu.add(aboutAction);
		// ActionContributionItem aboutItem = new ActionContributionItem(aboutAction);
		//		aboutItem.setVisible(!"carbon".equals(SWT.getPlatform())); //$NON-NLS-1$
		// menu.add(aboutItem);
		menu.add(new GroupMarker("group.about.ext")); //$NON-NLS-1$
		return menu;
	}

	// private IContributionItem getPrintItem() {
	// return getItem(
	// ActionFactory.PRINT.getId(),
	//				"org.eclipse.ui.file.print", ISharedImages.IMG_ETOOL_PRINT_EDIT, //$NON-NLS-1$
	// ISharedImages.IMG_ETOOL_PRINT_EDIT_DISABLED,
	// WorkbenchMessages.Workbench_print,
	// WorkbenchMessages.Workbench_printToolTip, null);
	// }
	//
	// private IContributionItem getItem(String actionId, String commandId,
	// String image, String disabledImage, String label, String tooltip, String helpContextId) {
	// ISharedImages sharedImages = getWindow().getWorkbench()
	// .getSharedImages();
	//
	// IActionCommandMappingService acms = (IActionCommandMappingService) getWindow()
	// .getService(IActionCommandMappingService.class);
	// acms.map(actionId, commandId);
	//
	// CommandContributionItemParameter commandParm = new CommandContributionItemParameter(
	// getWindow(), actionId, commandId, null, sharedImages
	// .getImageDescriptor(image), sharedImages
	// .getImageDescriptor(disabledImage), null, label, null,
	// tooltip, CommandContributionItem.STYLE_PUSH, null, false);
	// return new CommandContributionItem(commandParm);
	// }

	/**
	 * Adds a <code>GroupMarker</code> or <code>Separator</code> to a menu. The test for whether a separator should be
	 * added is done by checking for the existence of a preference matching the string useSeparator.MENUID.GROUPID that
	 * is set to <code>true</code>.
	 *
	 * @param menu
	 *            the menu to add to
	 * @param groupId
	 *            the group id for the added separator or group marker
	 */
	private void addSeparatorOrGroupMarker(MenuManager menu, String groupId) {
		String prefId = "useSeparator." + menu.getId() + "." + groupId; //$NON-NLS-1$ //$NON-NLS-2$
		boolean addExtraSeparators = GDAClientActivator.getDefault().getPreferenceStore().getBoolean(prefId);
		if (addExtraSeparators) {
			menu.add(new Separator(groupId));
		} else {
			menu.add(new GroupMarker(groupId));
		}
	}

    private IContributionItem getCutItem() {
		return getItem(
				ActionFactory.CUT.getId(),
				ActionFactory.CUT.getCommandId(),
				ISharedImages.IMG_TOOL_CUT,
				ISharedImages.IMG_TOOL_CUT_DISABLED,
				WorkbenchMessages.Workbench_cut,
				WorkbenchMessages.Workbench_cutToolTip, null);
	}

    private IContributionItem getCopyItem() {
		return getItem(
				ActionFactory.COPY.getId(),
				ActionFactory.COPY.getCommandId(),
				ISharedImages.IMG_TOOL_COPY,
				ISharedImages.IMG_TOOL_COPY_DISABLED,
				WorkbenchMessages.Workbench_copy,
				WorkbenchMessages.Workbench_copyToolTip, null);
	}

    private IContributionItem getPasteItem() {
		return getItem(
				ActionFactory.PASTE.getId(),
				ActionFactory.PASTE.getCommandId(),
				ISharedImages.IMG_TOOL_PASTE,
				ISharedImages.IMG_TOOL_PASTE_DISABLED,
				WorkbenchMessages.Workbench_paste,
				WorkbenchMessages.Workbench_pasteToolTip, null);
	}

    private IContributionItem getDeleteItem() {
        return getItem(ActionFactory.DELETE.getId(),
        		ActionFactory.DELETE.getCommandId(),
        		ISharedImages.IMG_TOOL_DELETE,
        		ISharedImages.IMG_TOOL_DELETE_DISABLED,
        		WorkbenchMessages.Workbench_delete,
        		WorkbenchMessages.Workbench_deleteToolTip,
        		IWorkbenchHelpContextIds.DELETE_RETARGET_ACTION);
    }

    private IContributionItem getSelectAllItem() {
		return getItem(
				ActionFactory.SELECT_ALL.getId(),
				ActionFactory.SELECT_ALL.getCommandId(),
				null, null, WorkbenchMessages.Workbench_selectAll,
				WorkbenchMessages.Workbench_selectAllToolTip, null);
	}

    private IContributionItem getItem(String actionId, String commandId,
    		String image, String disabledImage, String label, String tooltip, String helpContextId) {
		ISharedImages sharedImages = getWindow().getWorkbench()
				.getSharedImages();

		IActionCommandMappingService acms = getWindow()
				.getService(IActionCommandMappingService.class);
		acms.map(actionId, commandId);

		CommandContributionItemParameter commandParm = new CommandContributionItemParameter(
				getWindow(), actionId, commandId, null, sharedImages
						.getImageDescriptor(image), sharedImages
						.getImageDescriptor(disabledImage), null, label, null,
				tooltip, CommandContributionItem.STYLE_PUSH, helpContextId, false);
		return new CommandContributionItem(commandParm);
	}

}
