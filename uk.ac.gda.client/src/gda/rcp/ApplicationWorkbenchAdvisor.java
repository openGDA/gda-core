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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.menus.CommandContributionItem;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.factory.corba.util.EventService;
import gda.gui.RCPController;
import gda.gui.RCPControllerImpl;
import gda.gui.RCPOpenPerspectiveCommand;
import gda.gui.RCPOpenViewCommand;
import gda.gui.RCPSetPreferenceCommand;
import gda.jython.IScanDataPointObserver;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;
import gda.jython.UserMessage;
import gda.jython.batoncontrol.BatonRequested;
import gda.observable.IObserver;
import gda.rcp.preferences.GdaRootPreferencePage;
import gda.util.ObjectServer;
import uk.ac.gda.client.closeactions.UserOptionsOnCloseDialog;
import uk.ac.gda.client.liveplot.LivePlotViewManager;
import uk.ac.gda.client.scripting.JythonPerspective;
import uk.ac.gda.client.scripting.ScriptProjectCreator;
import uk.ac.gda.ui.partlistener.MenuDisplayPartListener;
import uk.ac.gda.views.baton.MessageView;
import uk.ac.gda.views.baton.dialogs.BatonRequestDialog;

@SuppressWarnings("restriction")
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationWorkbenchAdvisor.class);

	@Override
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	@Override
	public void initialize(IWorkbenchConfigurer configurer) {

		// Prevent PyDev popping up a funding appeal dialog box on first use
		// Diamond Light Source is already a Gold Sponsor of PyDev (via dawnsci)
		System.setProperty("pydev.funding.hide", "true");

		/*
		 * Called to enable alternative content on navigators.
		 */
		IDE.registerAdapters();

		/*
		 * Does nothing in eclipse 3.5x
		 */
		super.initialize(configurer);

		// Don't clear the background when resizing. This should remove the flickering.
		// This may have other effects of causing "cheese" to appear.
		System.setProperty("sun.awt.noerasebackground", "true");

		 // Option to save and restore the GUI state between sessions. Default 'true'.
		 // If 'true' the setting to force the Intro/Welcome Screen may have no effect
		boolean doSaveRestore = LocalProperties.check(LocalProperties.GDA_GUI_SAVE_RESTORE,true);
		configurer.setSaveAndRestore(doSaveRestore);

		/*
		 * Makes the images in the problems view work correctly.
		 */
		declareDefaultImages(configurer);

		removeUnusedPreferencePages();
	}

	private void removeUnusedPreferencePages(){
		PreferenceManager pm = PlatformUI.getWorkbench( ).getPreferenceManager();
		//Print available preference pages
		//IPreferenceNode[] arr = pm.getRootSubNodes();
			//for(IPreferenceNode pn:arr)
			//    System.out.println("Label:" + pn.getLabelText() + " ID:" + pn.getId());
		pm.remove("org.eclipse.jdt.ui.preferences.JavaBasePreferencePage");//Remove Java preference page
		pm.remove("org.eclipse.ant.ui.AntPreferencePage");//Remove Ant preference page
		pm.remove("org.eclipse.team.ui.TeamPreferences");//Remove Team preference page
		pm.remove("org.eclipse.wst.xml.ui.preferences.xml");//Remove xml preference page
		pm.remove("org.eclipse.debug.ui.DebugPreferencePage");//Remove debug preference page
		pm.remove("org.eclipse.help.ui.browsersPreferencePage");//Remove help preference page
		pm.remove("org.eclipse.update.internal.ui.preferences.MainPreferencePage");//Remove Install/Update preference page
		pm.remove("org.eclipse.equinox.internal.p2.ui.sdk.ProvisioningPreferencePage");//Remove another Install/Update preference page
		pm.remove("net.sf.py4j.defaultserver.preferences.DefaultServerPreferencePage");//Remove Py4J preference page
	}

	@Override
	public void postStartup() {
		super.postStartup();

		final IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (workbenchWindow != null) {
			/*
			 * Adds the part listener to add view menu text to all parts opened hereafter.
			 */
			applyViewMenuText(workbenchWindow);

			// Add a listener to make sure a live scan plot is visible at the start of each scan (unless this
			// behaviour has been switched off in the client preferences)
			final IScanDataPointObserver openXYPlotOnScanStart = new LivePlotViewManager();
			InterfaceProvider.getScanDataPointProvider().addIScanDataPointObserver(openXYPlotOnScanStart);
			addCleanupWork(new CleanupWork() {
				@Override
				public void cleanup() {
					InterfaceProvider.getScanDataPointProvider().deleteIScanDataPointObserver(openXYPlotOnScanStart);
				}
			});

			Findable obj = Finder.getInstance().findNoWarn(RCPControllerImpl.name);
			if (obj != null) {
				final RCPController obs = (RCPController) obj;
				final IObserver rcpControllerObs = new IObserver() {

					@Override
					public void update(Object source, final Object arg) {
						if (arg instanceof RCPOpenViewCommand) {
							PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
								@Override
								public void run() {
									String id = ((RCPOpenViewCommand) arg).getId();
									try {
										PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
												.showView(id);
									} catch (PartInitException e) {
										logger.error("Error opening " + id, e);
									}
								}
							});
						}
						if (arg instanceof RCPOpenPerspectiveCommand) {
							PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
								@Override
								public void run() {
									try {
										PlatformUI.getWorkbench().showPerspective(
												((RCPOpenPerspectiveCommand) arg).getId(),
												PlatformUI.getWorkbench().getActiveWorkbenchWindow());
									} catch (PartInitException e) {
										logger.error("Error opening " + ((RCPOpenPerspectiveCommand) arg).getId(), e);
									} catch (WorkbenchException e) {
										logger.error("Error opening " + ((RCPOpenPerspectiveCommand) arg).getId(), e);
									}
								}
							});
						}
						if( arg instanceof RCPSetPreferenceCommand){
							IPreferenceStore preferenceStore = GDAClientActivator.getDefault().getPreferenceStore();
							RCPSetPreferenceCommand preference = (RCPSetPreferenceCommand) arg;
							Object value = preference.getValue();
							if (value instanceof Double) {
								preferenceStore.setValue(preference.getId(), (Double)value);
							}
							else if( value instanceof Float) {
								preferenceStore.setValue(preference.getId(), (Float)value);
							}
							else if( value instanceof Integer) {
								preferenceStore.setValue(preference.getId(), (Integer)value);
							}
							else if( value instanceof Long) {
								preferenceStore.setValue(preference.getId(), (Long)value);
							}
							else if( value instanceof String) {
								preferenceStore.setValue(preference.getId(), (String)value);
							}
							else if( value instanceof Boolean) {
								preferenceStore.setValue(preference.getId(), (Boolean)value);
							}
							else {
								throw new IllegalArgumentException("The value must be of one of the following types: Double, Float, Integer, Long, String, or Boolean");
							}
						}
					}
				};
				obs.addIObserver(rcpControllerObs);
				addCleanupWork(new CleanupWork() {

					@Override
					public void cleanup() {
						obs.deleteIObserver(rcpControllerObs);
					}
				});
			}

			listenForUserMessages();
			listenForBatonMessages();

			// should these two be done during initialize instead of now when Windows have been created?
			final WorkspaceModifyOperation wkspaceModifyOperation = new WorkspaceModifyOperation() {
				@Override
				protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
						InterruptedException {
					monitor.beginTask("Refreshing Projects", IProgressMonitor.UNKNOWN);
					prepareJythonEnv(monitor);
					DataProject.createOnStartup(monitor);
					monitor.done();
				}
			};

			final WorkspaceJob workspaceJob = new WorkspaceJob("Setting up workspace") {
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
					try {
						wkspaceModifyOperation.run(monitor);
					} catch (Exception e) {
						logger.error("Error in postStartup", e);
					}
					return Status.OK_STATUS;
				}
			};
			workspaceJob.schedule();
			try {
				refreshFromLocal();
			} finally {// Resume background jobs after we startup
				Job.getJobManager().resume();
			}
		}
	}

	private void listenForUserMessages() {

		final IObserver messageObserver = new IObserver() {
			@Override
			public void update(Object source, Object arg) {
				// If a message is received, force the Messages view to open
				if (arg instanceof UserMessage) {
					PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							try {
								PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(MessageView.ID);
							} catch (Exception e) {
								logger.warn("Could not open Messages view", e);
							}
						}
					});
				}
			}
		};

		InterfaceProvider.getJSFObserver().addIObserver(messageObserver);
	}

	private void listenForBatonMessages() {

		final IObserver batonObserver = new IObserver() {
			@Override
			public void update(Object source, Object arg) {
				if (arg instanceof BatonRequested) {
					final BatonRequested request = (BatonRequested) arg;
					if (JythonServerFacade.getInstance().amIBatonHolder()) {
						final Display display = PlatformUI.getWorkbench().getDisplay();
						display.asyncExec(new Runnable() {
							@Override
							public void run() {
								final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
								BatonRequestDialog.doPassBaton(shell, request);
							}
						});
					}
				}
			}
		};

		InterfaceProvider.getJSFObserver().addIObserver(batonObserver);
	}

	private void refreshFromLocal() {
		String[] commandLineArgs = Platform.getCommandLineArgs();
		boolean refresh = true;
		if (!refresh) {
			return;
		}

		// Do not refresh if it was already done by core on startup.
		for (int i = 0; i < commandLineArgs.length; i++) {
			if (commandLineArgs[i].equalsIgnoreCase("-refresh")) { //$NON-NLS-1$
				return;
			}
		}

		final IContainer root = ResourcesPlugin.getWorkspace().getRoot();
		Job job = new WorkspaceJob("Refreshing workspace") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				root.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				return Status.OK_STATUS;
			}
		};
		job.setRule(root);
		job.schedule();
	}

	/**
	 * This method adds a part listener which enables the text next to the icon in the views' toolbar. Also enables the
	 * text for the views that are open during workbench window creation(for the default perspective).
	 *
	 * @param workbenchWindow
	 */
	private void applyViewMenuText(final IWorkbenchWindow workbenchWindow) {
		if (GDAClientActivator.getDefault().getPreferenceStore().getBoolean(GdaRootPreferencePage.SHOW_MENU_TEXT)) {
			workbenchWindow.getPartService().addPartListener(new MenuDisplayPartListener());
			IViewReference[] viewReferences = workbenchWindow.getActivePage().getViewReferences();
			for (IViewReference vr : viewReferences) {
				IViewPart view = vr.getView(false);
				if (view != null) {
					IToolBarManager toolBarManager = view.getViewSite().getActionBars().getToolBarManager();
					IContributionItem[] items = toolBarManager.getItems();
					for (IContributionItem iContributionItem : items) {
						if (iContributionItem instanceof ActionContributionItem) {
							ActionContributionItem commandContributionItem = (ActionContributionItem) iContributionItem;
							commandContributionItem.setMode(ActionContributionItem.MODE_FORCE_TEXT);
							commandContributionItem.update();
						}
						if (iContributionItem instanceof CommandContributionItem) {
							// As per https://bugs.eclipse.org/bugs/show_bug.cgi?id=256340 the
							// CommandContributionItem
							// doesn't expose the setMode() method. Therefore, setting the mode using reflection.
							CommandContributionItem commandContributionItem = (CommandContributionItem) iContributionItem;
							commandContributionItem.getData().mode = CommandContributionItem.MODE_FORCE_TEXT;
							commandContributionItem.getId();
							Class<? extends CommandContributionItem> class1 = commandContributionItem.getClass();
							try {
								Field field = class1.getDeclaredField("mode");
								field.setAccessible(true);
								field.set(commandContributionItem, new Integer(1));
							} catch (SecurityException e) {
								logger.error("Security exception - cant access private member", e);
							} catch (IllegalArgumentException e) {
								logger.error("IllegalArgumentException - Problem setting mode", e);
							} catch (IllegalAccessException e) {
								logger.error("IllegalAccessException - Problem setting mode", e);
							} catch (NoSuchFieldException e) {
								logger.error("NoSuchFieldException - Problem setting mode", e);
							}
							commandContributionItem.update();
						}
					}
					if (toolBarManager instanceof ToolBarManager) {
						ToolBarManager tm = (ToolBarManager) toolBarManager;
						tm.getControl().pack(true);
					}
				}
			}
			workbenchWindow.getActivePage().savePerspective();
			workbenchWindow.getActivePage().resetPerspective();
		}
	}

	private void prepareJythonEnv(IProgressMonitor monitor) {
		try {
			monitor.subTask("Initialising script projects");
			ScriptProjectCreator.handleShowXMLConfig(monitor);
			ScriptProjectCreator.createProjects(monitor);
		} catch (Exception e) {
			logger.error("Error preparing Jython interpeter and projects", e);
		}
	}

	public interface CleanupWork {
		/**
		 * Called during shutdown. Throwing exceptions from this method, even runtime ones, not recommended.
		 */
		public void cleanup();
	}

	private static List<ApplicationWorkbenchAdvisor.CleanupWork> cleanupTasks;

	/**
	 * @param work
	 */
	public static void addCleanupWork(final ApplicationWorkbenchAdvisor.CleanupWork work) {
		if (cleanupTasks == null)
			cleanupTasks = new ArrayList<CleanupWork>(7);
		cleanupTasks.add(work);
	}

	@Override
	public boolean preShutdown() {
		if (LocalProperties.check("gda.gui.useCloseMenu")) {
			return CloseMenu();
		}
		return true;
	}

	private boolean CloseMenu() {
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		UserOptionsOnCloseDialog close = new UserOptionsOnCloseDialog(shell);
		if (close.open() == UserOptionsOnCloseDialog.CANCEL) {
			return false; // veto the shutdown
		}
		return true;
	}

	/*
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#postShutdown()
	 */
	@Override
	public void postShutdown() {
		try {
			// Do any other shutdown tasks
			if (cleanupTasks != null) {
				for (CleanupWork work : cleanupTasks)
					work.cleanup();
			}

		} finally {
			// if running GDA in distributed mode then unsubscribe from
			// eventservice
			if (!ObjectServer.isLocal()) {
				EventService eventService = EventService.getInstance();
				if (eventService != null) {
					eventService.unsubscribe();
				}
			}

			// tell JythonServerFacade to disconnect from JythonServer
			JythonServerFacade.disconnect();
		}
	}

	@Override
	public String getInitialWindowPerspectiveId() {
		return JythonPerspective.ID;
	}

	/**
	 * Reuse some default icons defined in the eclipse IDE plugins that are used in default browsers that we extend.
	 *
	 * @param configurer
	 */
	private void declareDefaultImages(IWorkbenchConfigurer configurer) {
		final String iconsPath = "icons/full/obj16/";
		Bundle ide = Platform.getBundle(IDEWorkbenchPlugin.IDE_WORKBENCH);
		/* images for Project Explorer */
		declareWorkbenchImage(configurer, ide, IDE.SharedImages.IMG_OBJ_PROJECT, iconsPath + "prj_obj.gif");
		declareWorkbenchImage(configurer, ide, IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED, iconsPath + "cprj_obj.gif");
		/* images for the problems browser */
		declareWorkbenchImage(configurer, ide, IDEInternalWorkbenchImages.IMG_OBJS_ERROR_PATH, iconsPath
				+ "error_tsk.gif");
		declareWorkbenchImage(configurer, ide, IDEInternalWorkbenchImages.IMG_OBJS_WARNING_PATH, iconsPath
				+ "warn_tsk.gif");
		declareWorkbenchImage(configurer, ide, IDEInternalWorkbenchImages.IMG_OBJS_INFO_PATH, iconsPath
				+ "info_tsk.gif");
	}

	private void declareWorkbenchImage(IWorkbenchConfigurer configurer, Bundle bundle, String symbolicName, String path) {
		URL url = bundle.getEntry(path);
		ImageDescriptor desc = ImageDescriptor.createFromURL(url);
		configurer.declareImage(symbolicName, desc, true);
	}

	/**
	 * Return the root of the workspace so that the project explorer will be refreshed when a change in the workspace occurs.
	 * This removes the problem seen when we added a working set causing the Project Explorer to switch to show Working Sets
	 * rather than Projects.
	 */
	@Override
	public IAdaptable getDefaultPageInput() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
}
