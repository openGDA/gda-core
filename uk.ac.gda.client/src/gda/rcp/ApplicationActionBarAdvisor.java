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

import static gda.configuration.properties.LocalProperties.GDA_GUI_FORCE_LEFT_STOP_ALL;
import static gda.configuration.properties.LocalProperties.GDA_GUI_STATUS_HIDE_STOP_ALL;
import static gda.configuration.properties.LocalProperties.GDA_GUI_STOP_ALL_COMMAND_ID;

import java.net.URI;

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
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.status.StatusBean;
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
import org.eclipse.ui.handlers.IHandlerService;
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
import gda.jython.JythonServerFacade;
import gda.jython.JythonServerStatus;
import gda.jython.JythonStatus;
import gda.jython.authenticator.UserAuthentication;
import gda.jython.batoncontrol.BatonChanged;
import gda.jython.batoncontrol.ClientDetails;
import gda.observable.IObserver;
import gda.rcp.views.GdaImages;
import gda.scan.ScanEvent;
import uk.ac.diamond.osgi.services.ServiceProvider;
import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.client.blueapi.BlueApiStatusItem;
import uk.ac.gda.ui.status.LinkContributionButton;
import uk.ac.gda.ui.status.LinkContributionItem;
import uk.ac.gda.ui.status.LinkContributionLabel;
import uk.ac.gda.views.baton.BatonView;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of the actions added to a workbench window.
 * Each window will be populated with new actions.
 */
// The suppress warnings is because we are using some provisional API (IActionBarConfigurer2)
// and need access to IDEWorkbenchMessages
@SuppressWarnings("restriction")
public final class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationActionBarAdvisor.class);

	private static final String NEW_GDA_EXT = "new.gda.ext";
	private static final String STOP_ALL_TEXT = "Stop All";
	private static final String STOP_ALL_TOOLTIP = "Stops all GDA devices, including motors and detectors.\n"
			+ "Also aborts scans and scripts and kills threads.\n"
			+ "Warning: this might leave GDA in an inconsistent state.";
	private static final String DEFAULT_STOP_ALL_COMMAND_ID = "uk.ac.gda.client.StopAllCommand";

	/** Flag to add a developer test item to the File menu. The Action can be modified below. */
	private static final boolean USE_TEST_ACTION = false;

	private final RateLimiter scanEventRateLimiter = RateLimiter.create(20); // 20 refreshes per sec max

	private final IWorkbenchWindow window;
	private final ActionSetRegistry registry;

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

	/**
	 * @param configurer
	 */
	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
		window = configurer.getWindowConfigurer().getWindow();
		registry = WorkbenchPlugin.getDefault().getActionSetRegistry();
	}

	/**
	 * Creates the actions and registers them.<br>
	 * Registering is needed to ensure that key bindings work.<br>
	 * The corresponding commands key bindings are defined in the plugin.xml file.<br>
	 * Registering also provides automatic disposal of the actions when<br>
	 *  the window is closed.
	 */
	@Override
	protected void makeActions(IWorkbenchWindow window) {
		makeFileActions(window);
		makeEditActions(window);
		makeWindowActions(window);
		makeHelpActions(window);
		makeTestActions();
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

	private void makeHelpActions(IWorkbenchWindow window) {
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

	private void makeEditActions(IWorkbenchWindow window) {
		undoAction = ActionFactory.UNDO.create(window);
		register(undoAction);

		redoAction = ActionFactory.REDO.create(window);
		register(redoAction);

		perspectiveCustomizeAction = ActionFactory.EDIT_ACTION_SETS.create(window);
		register(perspectiveCustomizeAction);
	}

	private void makeFileActions(IWorkbenchWindow window) {
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

	private void testMessageGeneration(Display display) {
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

	private void makeTestActions() {
		// Action to use for testing. Modify it as needed.
		if (USE_TEST_ACTION) {
			Display display = window.getShell().getDisplay();
			testAction = new Action() {
				@Override
				public void run() {
					display.asyncExec(() -> ApplicationActionBarAdvisor.this.testMessageGeneration(display));
				}
			};
			testAction.setText("Test...");
		}
	}

	private void makeWindowActions(IWorkbenchWindow window) {
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

		// File Group
		IToolBarManager fileToolBar = actionBarConfigurer.createToolBarManager();
		fileToolBar.add(new Separator(IWorkbenchActionConstants.NEW_GROUP));

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
		fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.PRINT_EXT));

		fileToolBar.add(new Separator(IWorkbenchActionConstants.BUILD_GROUP));
		fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.BUILD_EXT));

		// Add to the cool bar manager
		coolBar.add(
				actionBarConfigurer.createToolBarContributionItem(fileToolBar, IWorkbenchActionConstants.TOOLBAR_FILE));
		coolBar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		coolBar.add(new GroupMarker("gda.script.actions"));
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

	private StatusLineContributionItem buildLogStatus() {
		StatusLineContributionItem logStatus = new StatusLineContributionItem("GDA-LoggedInUser", true, 15);
		logStatus.setText(UserAuthentication.getUsername());
		logStatus.setImage(GdaImages.getImage("user_gray.png"));
		return logStatus;
	}

	private LinkContributionItem buildBatonStatus() {
		LinkContributionItem batonStatus = new LinkContributionItem("uk.ac.gda.baton.status", new LinkContributionLabel(), 18);
		batonStatus.setToolTipText("Double click status to bring up baton manager.");
		batonStatus.setText("GDA-Baton");

		batonStatus.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				try {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					page.showView(BatonView.ID);
				} catch (Exception ne) {
					logger.error("Cannot open baton manager", ne);
				}
			}
		});
		return batonStatus;
	}

	private StatusLineContributionItem buildScanStatus() {
		StatusLineContributionItem scanStatus = new StatusLineContributionItem("GDA-ScanStatus", true, 55);

		IEventService service = ServiceProvider.getService(IEventService.class);

		try  {
			ISubscriber<IBeanListener<StatusBean>> statusTopicSubscriber = service.createSubscriber(new URI(LocalProperties.getBrokerURI()), EventConstants.STATUS_TOPIC);
			statusTopicSubscriber.addListener(event -> {
				if (event.getBean() instanceof ScanBean) {
					updateScanDetails(scanStatus, (ScanBean)event.getBean());
				}
			});

			ApplicationWorkbenchAdvisor.addCleanupWork(() -> {
				try {
					statusTopicSubscriber.removeAllListeners();
					statusTopicSubscriber.close();
				} catch (EventException e1) {
					logger.error("Error removing listener from STATUS_TOPIC", e1);
				}
			});
		} catch (NullPointerException e) {
			// Handling for non-StatusQueueView/Mapping beamlines, prevent stack trace being printed to console
			if (service == null) {
				logger.warn("EventService null when adding listener to STATUS_TOPIC, ScanBean status bar progress disabled. If this beamline uses the Queue, this will cause errors elsewhere");
			} else {
				throw e;
			}

		} catch (Exception e2) {
			logger.error("Error adding listener to STATUS_TOPIC", e2);
		}
		return scanStatus;
	}

	private StatusLineContributionItem buildScriptStatus() {
		return new StatusLineContributionItem("GDA-ScriptStatus", true, 20);
	}

	private Runnable updateGuiBasedOnProcessorState(Processor.STATE stateIn, ProcessorCurrentItem item, LinkContributionItem queueStatus, Processor processor) {
		return new Runnable() {

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
		};
	}

	private void addQueueStatusContribution(IStatusLineManager manager) {
		/*
		 * add contribution to show the state of the queue if one exists
		 */
		Queue queue = CommandQueueViewFactory.getQueue();
		if(queue == null) return;
		Processor processor = CommandQueueViewFactory.getProcessor();
		if(processor == null) return;

		LinkContributionItem queueStatus = new LinkContributionItem("uk.ac.gda.queue.status", new LinkContributionLabel(), 20);
		queueStatus.setToolTipText("Double click status to bring up command queue.");
		queueStatus.setText("GDA-QueueStatus");
		manager.add(queueStatus);
		queueStatus.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				try {
					IWorkbenchPage page = PlatformUI.getWorkbench()
													.getActiveWorkbenchWindow()
													.getActivePage();
					page.showView(CommandQueueViewFactory.ID);
				} catch (Exception exception) {
					logger.error("Cannot open command queue view", exception);
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
			void updateStatus(Processor.STATE stateIn, ProcessorCurrentItem item) {
				Display.getDefault().asyncExec(updateGuiBasedOnProcessorState(stateIn, item, queueStatus, processor));
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

	@Override
	protected void fillStatusLine(IStatusLineManager manager) {
		StatusLineContributionItem buildLogStatus = buildLogStatus();
		manager.add(buildLogStatus);

		LinkContributionItem batonStatus = buildBatonStatus();
		manager.add(batonStatus);

		BlueApiStatusItem.build().ifPresent(manager::add);

		StatusLineContributionItem scanStatus = buildScanStatus();
		manager.add(scanStatus);

		StatusLineContributionItem scriptStatus = buildScriptStatus();
		manager.add(scriptStatus);

		if (!LocalProperties.check(GDA_GUI_STATUS_HIDE_STOP_ALL)) {
			LinkContributionItem stopAllButton = createStopAllButton();
			if (LocalProperties.check(GDA_GUI_FORCE_LEFT_STOP_ALL)) {
				manager.insertBefore(buildLogStatus.getId(), stopAllButton);
			} else {
				manager.add(stopAllButton);
			}
			// Add a bit of space after the button so it is not too close to the end marker
			StatusLineContributionItem spacer = new StatusLineContributionItem("uk.ac.diamond.daq.stopall.spacer", true, 0);
			manager.insertAfter(stopAllButton.getId(), spacer);
		}

		addQueueStatusContribution(manager);

		IJythonServerStatusObserver serverObserver = (theObserved, changeCode) -> {
			if (changeCode instanceof BatonChanged) {
				updateBatonStatus(batonStatus);
			} else if (changeCode instanceof JythonServerStatus) {
				updateScriptStatus(scriptStatus, (JythonServerStatus)changeCode);
			} else if (changeCode instanceof ScanEvent) {
				updateScanDetails(scanStatus, (ScanEvent) changeCode);
			}
		};

		// NOTE these methods connect to Jython server which may be down.
		// If throw exception from fillStatusLine(...) whole client fails.
		try {
			InterfaceProvider.getJSFObserver().addIObserver(serverObserver);
			InterfaceProvider.getScanDataPointProvider().addScanEventObserver(serverObserver);
			updateBatonStatus(batonStatus);
			JythonServerFacade jsf = JythonServerFacade.getInstance();
			JythonServerStatus jythonServerStatus = jsf.getServerStatus();
			updateScanStatus(scanStatus, jythonServerStatus);
			updateScriptStatus(scriptStatus, jythonServerStatus);
		} catch (Exception ne) {
			logger.error("Cannot connect to JythonServerFacade", ne);
			Display.getDefault().asyncExec(() -> MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Cannot Connect to GDA Server",
					"The GDA Server is not responding.\n\nPlease contact your GDA support engineer."));
		}

		ApplicationWorkbenchAdvisor.addCleanupWork(() -> InterfaceProvider.getJSFObserver().deleteIObserver(serverObserver));
	}

	private LinkContributionItem createStopAllButton() {
		LinkContributionItem stopAll = new LinkContributionItem("uk.ac.diamond.daq.stopall", new LinkContributionButton(), 15);
		stopAll.setText(STOP_ALL_TEXT);
		stopAll.setToolTipText(STOP_ALL_TOOLTIP);
		stopAll.setImage(GdaImages.getImage("stop.png"));
		stopAll.setVisible(true);
		stopAll.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent ev) {
				try {
					String stopAllCommandId = LocalProperties.get(GDA_GUI_STOP_ALL_COMMAND_ID, DEFAULT_STOP_ALL_COMMAND_ID);
					IHandlerService handlerService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IHandlerService.class);
					handlerService.executeCommand(stopAllCommandId, null);
				} catch (Exception e) {
					logger.error("Error executing Stop All command", e);
				}
			}
		});
		return stopAll;
	}

    private void updateScanDetails(StatusLineContributionItem status, ScanBean scanBean) {
    	String message = scanBean.toProgressString();

		switch (scanBean.getStatus()) {
		case TERMINATED:
		case REQUEST_TERMINATE:
		case FAILED:
			setStatusLineText(status, message, "control_stop_blue.png");
			break;
		case REQUEST_PAUSE:
		case PAUSED:
			setStatusLineText(status, message, "control_pause_blue.png");
			break;
		case REQUEST_RESUME:
		case PREPARING:
		case RESUMED:
		case RUNNING:
		case SUBMITTED:
			if (scanEventRateLimiter.tryAcquire()) {
				setStatusLineText(status, message, "computer_go.png");
			}
			break;
		case FINISHING:
		case COMPLETE:
		case NONE:
		case UNFINISHED:
		default:
			setStatusLineText(status, message);
			break;
		}
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
			// If it's an update while scan is running, limit the rate of GUI updates
			if (scanEventRateLimiter.tryAcquire()) {
				setStatusLineText(status, message, "computer_go.png");
			}
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

	private void updateScanStatus(StatusLineContributionItem status, JythonServerStatus jythonStatus) {
		updateStatus(status, jythonStatus.scanStatus, "Scan");
	}

	private void updateScriptStatus(StatusLineContributionItem status, JythonServerStatus jythonStatus) {
		updateStatus(status, jythonStatus.scriptStatus, "Script");
	}

	private void updateStatus(StatusLineContributionItem status, JythonStatus scan, String postFix) {
		switch (scan) {
		case IDLE:
			setStatusLineText(status, "No " + postFix + " running");
			break;
		case PAUSED:
			setStatusLineText(status, "Paused " + postFix, "control_pause_blue.png");
			break;
		case RUNNING:
			setStatusLineText(status, "Running " + postFix, "computer_go.png");
			break;
		default:
			throw new IllegalStateException("Unreachable");
		}
	}

	private void updateBatonStatus(LinkContributionItem batonStatus) {
		String text;
		String tooltip;
		Image image;
		if (InterfaceProvider.getBatonStateProvider().isBatonHeld()) {
			ClientDetails holder = InterfaceProvider.getBatonStateProvider().getBatonHolder();
			boolean isHolder = InterfaceProvider.getBatonStateProvider().amIBatonHolder();
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

	private void setStatusLineText(StatusLineContributionItem status, String text) {
		setStatusLineText(status, text, null);
	}

	private void setStatusLineText(StatusLineContributionItem status, String text, String imageName) {

		Image image = GdaImages.getImage(imageName);
		IStatusLineManager man = getActionBarConfigurer().getStatusLineManager();

		// Update the GUI this needs to be in the UI thread
		window.getShell().getDisplay().asyncExec(() -> {
			status.setText(text);
			status.setImage(image);
			man.update(false); // makes new widgets
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
		// We disable the standard "new" extension marker and replace with a gda one.
		// This is done in order to provide our own implementation of OpenLocalFileAction
		// (which is normally provided by org.eclipse.ui.ide) which does not allow easy
		// changes to default location. So we replace with one of our own.
		menu.add(new GroupMarker(NEW_GDA_EXT));
		// Add NewWizardAction, override default name: "Other..."
		if (LocalProperties.check(LocalProperties.GDA_GUI_USE_ACTIONS_NEW, true)) {
			newWizardAction.setText("New (from Wizard)...");
			menu.add(newWizardAction);
		}

		menu.add(new Separator());
		menu.add(closeAction);
		menu.add(closeAllAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.CLOSE_EXT));

		menu.add(new Separator());
		menu.add(saveAction);
		menu.add(saveAsAction);
		menu.add(saveAllAction);

		menu.add(new Separator());
		menu.add(printAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.PRINT_EXT));

		menu.add(new Separator());
		menu.add(new GroupMarker(IWorkbenchActionConstants.IMPORT_EXT));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		MenuManager recent = new MenuManager(WorkbenchMessages.OpenRecentDocuments_text);
		recent.add(ContributionItemFactory.REOPEN_EDITORS.create(getWindow()));
		menu.add(recent);

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
		menu.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));
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
		String openText = IDEWorkbenchMessages.Workbench_openPerspective;
		MenuManager changePerspMenuMgr = new MenuManager(openText, "openPerspective"); //$NON-NLS-1$
		IContributionItem changePerspMenuItem = ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(getWindow());
		changePerspMenuMgr.add(changePerspMenuItem);
		menu.add(changePerspMenuMgr);

		MenuManager showViewMenuMgr = new MenuManager(IDEWorkbenchMessages.Workbench_showView, "showView"); //$NON-NLS-1$
		IContributionItem showViewMenu = ContributionItemFactory.VIEWS_SHORTLIST.create(getWindow());
		showViewMenuMgr.add(showViewMenu);
		menu.add(showViewMenuMgr);

		menu.add(new Separator());
		menu.add(perspectiveResetAction);
		if (LocalProperties.check(LocalProperties.GDA_GUI_USE_ACTIONS_PERSPECTIVE_CUSTOM, true)) {
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
		}

		menu.add(helpAction);
		menu.add(new Separator());

		menu.add(new GroupMarker("group.intro.ext")); //$NON-NLS-1$
		addSeparatorOrGroupMarker(menu, "group.main"); //$NON-NLS-1$

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
		menu.add(new GroupMarker("group.about.ext")); //$NON-NLS-1$
		return menu;
	}

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
		return getItem(ActionFactory.CUT.getId(),
				ActionFactory.CUT.getCommandId(),
				ISharedImages.IMG_TOOL_CUT,
				ISharedImages.IMG_TOOL_CUT_DISABLED,
				WorkbenchMessages.Workbench_cut,
				WorkbenchMessages.Workbench_cutToolTip,
				null);
	}

    private IContributionItem getCopyItem() {
		return getItem(ActionFactory.COPY.getId(),
				ActionFactory.COPY.getCommandId(),
				ISharedImages.IMG_TOOL_COPY,
				ISharedImages.IMG_TOOL_COPY_DISABLED,
				WorkbenchMessages.Workbench_copy,
				WorkbenchMessages.Workbench_copyToolTip,
				null);
	}

    private IContributionItem getPasteItem() {
		return getItem(ActionFactory.PASTE.getId(),
				ActionFactory.PASTE.getCommandId(),
				ISharedImages.IMG_TOOL_PASTE,
				ISharedImages.IMG_TOOL_PASTE_DISABLED,
				WorkbenchMessages.Workbench_paste,
				WorkbenchMessages.Workbench_pasteToolTip,
				null);
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
		return getItem(ActionFactory.SELECT_ALL.getId(),
				ActionFactory.SELECT_ALL.getCommandId(),
				null,
				null,
				WorkbenchMessages.Workbench_selectAll,
				WorkbenchMessages.Workbench_selectAllToolTip,
				null);
	}

    private IContributionItem getItem(String actionId,
    									String commandId,
    									String image,
    									String disabledImage,
    									String label,
    									String tooltip,
    									String helpContextId) {

    	ISharedImages sharedImages = getWindow().getWorkbench()
												.getSharedImages();

		IActionCommandMappingService acms =
				getWindow().getService(IActionCommandMappingService.class);

		acms.map(actionId, commandId);

		CommandContributionItemParameter commandParm = new CommandContributionItemParameter(
				getWindow(), actionId, commandId, null,
				sharedImages.getImageDescriptor(image),
				sharedImages.getImageDescriptor(disabledImage),
				null, label, null, tooltip,
				CommandContributionItem.STYLE_PUSH, helpContextId, false );

		return new CommandContributionItem(commandParm);
	}

}
