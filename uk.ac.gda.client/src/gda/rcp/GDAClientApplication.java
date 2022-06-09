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

import static gda.configuration.properties.LocalProperties.GDA_CHECK_USER_VISIT_VALID;
import static java.util.Arrays.stream;

import java.nio.file.Paths;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.ServiceHolder;
import gda.data.metadata.VisitEntry;
import gda.data.metadata.icat.IcatProvider;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.jython.authenticator.Authenticator;
import gda.jython.authenticator.UserAuthentication;
import gda.jython.authoriser.AuthoriserProvider;
import gda.rcp.util.UIScanDataPointEventService;
import gda.spring.context.SpringContext;
import gda.util.logging.LogbackUtils;
import uk.ac.gda.preferences.PreferenceConstants;
import uk.ac.gda.remoting.client.RmiProxyFactory;
import uk.ac.gda.richbeans.BeansFactoryInit;
import uk.ac.gda.ui.dialog.AuthenticationDialog;
import uk.ac.gda.ui.dialog.GenericDialog;
import uk.ac.gda.ui.dialog.VisitIDDialog;

/**
 * This class controls all aspects of the application's execution. We are very similar to an IDEApplication, so some of
 * this code comes from there.
 */
public class GDAClientApplication implements IApplication {

	private static final Logger logger = LoggerFactory.getLogger(GDAClientApplication.class);

	private static final String PROP_EXIT_CODE = "eclipse.exitcode";

	private final ServerAvailableWatchdog serverAvailableWatchdog = new ServerAvailableWatchdog();

	private boolean usingDefaultVisit = false;

	@Override
	public Object start(IApplicationContext context) {
		Display display = PlatformUI.createDisplay();
		try {
			// NOTE: Please keep the methods called during startup in tidy order. New tests or configurations should be
			// encapsulated in their own method.

			LogbackUtils.configureLoggingForClientProcess("rcp");
			logger.info("Starting GDA client...");
			customiseEnvironment();

			Finder.addFactory(new RmiProxyFactory());
			// Start watchdog checking whether the server can be reached.
			if(!serverAvailableWatchdog.startServerAvailableWatchdog()) {
				// Could not connect to the server - dialog has been displayed to the user.
				return EXIT_OK; // Exit the client can't start
			}
			// Once we are here the server is reachable

			authenticateUser(display);

			// Start Spring and load the client context
			createObjectFactory();

			if (identifyVisitID(display) == EXIT_OK) {
				return EXIT_OK;
			}

			Location instanceLocation = Platform.getInstanceLocation();
			if (instanceLocation != null && instanceLocation.isSet()) {
				logger.info("Workspace set to '{}'", instanceLocation.getURL());
			} else {
				throw new Exception("Workspace location not set.");
			}

			// To break the dependency of uk.ac.gda.common.BeansFactory of RCP/Eclipse, we
			// manually force initialisation here. In the object server this is handled
			// by Spring, in Eclipse we use the registry
			try {
				BeansFactoryInit.initBeansFactory();
			} catch (Exception e) {
				final String message = "Failed to initialize Beans Factory";
				logger.error(message, e);
				throw new RuntimeException(message, e);
			}

			IPreferenceStore preferenceStore = GDAClientActivator.getDefault().getPreferenceStore();
			if (preferenceStore.getBoolean(PreferenceConstants.GDA_USE_SCANDATAPOINT_SERVICE)) {
				logger.debug("Using Scan Data Point Service");
				createScanDataPointService();
			} else {
				logger.debug("Not using Scan Data Point Service");
			}

			fixVisitID();

			// Start a watchdog to close the client when the user's visit has expired
			// TODO: Change LocalProperties.check() default to true when successfully tested in live mode
			if (!usingDefaultVisit && LocalProperties.check(GDA_CHECK_USER_VISIT_VALID, false)) {
				new CurrentVisitValidWatchdog(System.getProperty("user.name"), LocalProperties.get(LocalProperties.RCP_APP_VISIT)).startWatchdog();
			}

			// This is where we block while the client is running
			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			// Here once the client is being closed
			logger.info("Closing GDA client (User exited)...");
			logger.debug("Workbench return code: {}", returnCode);

			if (LocalProperties.check("gda.gui.useCloseMenu")) {
				//overrides any other normal exit conditions
				if (System.getProperty("requestedRestart", "false").equals("true")) {
					System.setProperty("requestedRestart", "false");
					return EXIT_RELAUNCH.equals(Integer.getInteger(PROP_EXIT_CODE)) ? EXIT_RELAUNCH : EXIT_RESTART;
				} else {
					return EXIT_OK;
				}
			}

			// the workbench doesn't support relaunch yet (bug 61809) so
			// for now restart is used, and exit data properties are checked
			// here to substitute in the relaunch return code if needed

			if (returnCode != PlatformUI.RETURN_RESTART) {
				return EXIT_OK;
			}

			// if the exit code property has been set to the relaunch code, then
			// return that code now, otherwise this is a normal restart
			return EXIT_RELAUNCH.equals(Integer.getInteger(PROP_EXIT_CODE)) ? EXIT_RELAUNCH : EXIT_RESTART;

		} catch (Throwable ne) {
			logger.error("Cannot start client", ne);
			String problem = ne.getMessage();
			String resolution = "The usual remedy is to reset the GDA client workspace. If there is no option to reset"
					+ " the workspace when starting the GDA client from the Diamond Launcher, you should be able to run"
					+ " either 'gda client --reset' or 'gdaclient --reset' from a terminal window.";
			// TODO: Remove reference to 'gdaclient --reset' when all configs have been standardised.
			if (problem == null) {
				problem = "A " + ne.getClass().getName() + " was thrown.";
			}
			MessageBox messageBox = new MessageBox(new Shell(display), SWT.ICON_ERROR);
			messageBox.setText("Cannot Start GDA Client");
			messageBox.setMessage("The GDA Client cannot start."
					+ "\n\n'" + problem + "'\n\n" + resolution
					+ "\n\nIf the problem persists, please contact your GDA support representative.");
			messageBox.open();
			return EXIT_OK;

		} finally {
			if (display != null && !display.isDisposed()) {
				try {
					display.dispose();
				} catch (Throwable ignored) {
					// Exit time, if exception thrown here, user gets message.
				}
			}
		}
	}

	/**
	 * Sets the visit ID of this Client process in the local JSF instance.
	 * <p>
	 * This must be done after the Workbench has started as the JSF makes connections to the server which can cause
	 * issues if this is done before the workbench is created.
	 */
	private void fixVisitID() {
		logger.info("User '{}' running GDA client using visit '{}'", UserAuthentication.getUsername(), LocalProperties.get(LocalProperties.RCP_APP_VISIT));
		InterfaceProvider.getBatonStateProvider().changeVisitID(LocalProperties.get(LocalProperties.RCP_APP_VISIT));
	}

	private void createScanDataPointService() {

		try {
			UIScanDataPointEventService.getInstance();
		} catch (Exception ne) {
			logger.error("Cannot start scan data point service", ne);
		}

	}

	/**
	 * Sets the property {@link LocalProperties#RCP_APP_VISIT} to the default visit given by property
	 * {@link LocalProperties#GDA_DEF_VISIT} or to {@link LocalProperties#DEFAULT_VISIT} if the property is not set. It
	 * logs the result.
	 */
	private void setToDefaultVisit() {
		// TODO show popup explaining that ICAT may be down and that it will use 0-0 unless local contact sets defVisit
		// to correct value and add them to the $BEAMLINE-config/xml/beamlinestaff.xml

		// Get the default visit defaulting to LocalProperties.DEFAULT_VISIT if not defined
		final String defaultVisit = LocalProperties.get(LocalProperties.GDA_DEF_VISIT, LocalProperties.DEFAULT_VISIT);

		// Check if the default visit property is set for better logging
		if (LocalProperties.contains(LocalProperties.GDA_DEF_VISIT)) {
			logger.info("Defaulting to visit '{}' defined by property '{}'", defaultVisit, LocalProperties.GDA_DEF_VISIT);
		} else { // The default visit property is NOT set
			logger.info("Defaulting to visit '{}' the property '{}' is NOT set", defaultVisit, LocalProperties.GDA_DEF_VISIT);
		}

		// Set the RCP visit property
		checkAndSetVisit(defaultVisit);

		usingDefaultVisit = true;
	}

	private void checkAndSetVisit(String newVisit) {
		if (!GDAClientActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.CONFIRM_VISIT_ON_CLIENT_START)) {
			logger.debug("Not checking whether to change visit ({} = false)", PreferenceConstants.CONFIRM_VISIT_ON_CLIENT_START);
			LocalProperties.set(LocalProperties.RCP_APP_VISIT, newVisit);
			return;
		}

		String currentVisitPath = InterfaceProvider.getPathConstructor().getVisitDirectory();
		String currentVisit = Paths.get(currentVisitPath).getFileName().toString();
		if (currentVisit.isEmpty() || currentVisit.equals(newVisit)) {
			logger.debug("Setting visit to {}", newVisit);
			LocalProperties.set(LocalProperties.RCP_APP_VISIT, newVisit);
			return;
		}

		// Check if user is member of staff
		String user = UserAuthentication.getUsername();
		// Set the new visit if not staff
		if (!isMemberOfStaff()) {
			logger.info("User {} is not staff - setting visit to {}", user, newVisit);
			LocalProperties.set(LocalProperties.RCP_APP_VISIT, newVisit);
			return;
		}

		// Present dialog box with option to change the visit.
		logger.info("User {} is staff - checking whether to change current visit to {}", user, newVisit);
		boolean replace = MessageDialog.openQuestion(null, "Change current visit",
				"Visit is currently set to "+currentVisit+". Do you want to change it to "+newVisit);
		String visit = replace ? newVisit : currentVisit;
		logger.info("Changing visit to {}", visit);
		LocalProperties.set(LocalProperties.RCP_APP_VISIT, visit);
	}

	private boolean isMemberOfStaff() {
		boolean isStaff = false;
		String user = UserAuthentication.getUsername();
		try {
			if (AuthoriserProvider.getAuthoriser().isLocalStaff(user)) {
				isStaff = true;
			}
		} catch (ClassNotFoundException e) {
			logger.error("Problem checking if user is staff. Assuming user IS staff.", e);
			isStaff = true;
		}
		return isStaff;
	}
	/**
	 * only sets the private chosenVisit attribute
	 */
	private int identifyVisitID(Display display) throws Exception {

		if (!IcatProvider.getInstance().icatInUse()) {
			logger.info("Icat database not in use.");
			setToDefaultVisit();
			return 1;
		}

		// test if the result has multiple entries
		String user = UserAuthentication.getUsername();
		VisitEntry[] visits;
		try {
			visits = IcatProvider.getInstance().getMyValidVisits(user);
		} catch (Exception e) {
			logger.error("Error retrieving visits from database.", e);
			setToDefaultVisit();
			return 1;
		}

		// if no valid visit ID then do same as the cancel button
		if (visits == null || visits.length == 0) {
			if (!isMemberOfStaff()) {
				logger.error("No visits found for user '{}' at this time on this beamline. GUI will not start.", user);
				MessageBox messageBox = new MessageBox(new Shell(display), SWT.ICON_ERROR);
				messageBox.setText("Cannot Start GDA Client");
				messageBox.setMessage("No visits found for user: " + user + ""
						+ "\n\nAre you sure you're logged in as the right user?"
						+ "\n\nGDA will not start");
				messageBox.open();
				return EXIT_OK;
			}
			logger.info("No visits found for user '{}' at this time on this beamline. Will use default visit as ID listed as a member of staff.", user);
			setToDefaultVisit();
		} else if (visits.length == 1) {
			checkAndSetVisit(visits[0].getVisitID());
		} else {
			// send array of visits to dialog to pick one
			String[][] visitInfo = new String[visits.length][];
			int i = 0;
			for (VisitEntry visit : visits) {
				visitInfo[i] = new String[] { visit.getVisitID(), visit.getTitle() };
				i++;
			}

			final VisitIDDialog visitDialog = new VisitIDDialog(display, visitInfo);
			if (visitDialog.open() == IDialogConstants.CANCEL_ID) {
				logger.info("Cancel pressed in visit chooser dialog. GUI will not continue.");
				return EXIT_OK;
			}
			if (visitDialog.getChoosenID() == null) {
				logger.info("Visit not resolved from visit chooser dialog. GUI will not start.");
				return EXIT_OK;
			}
			checkAndSetVisit(visitDialog.getChoosenID());
		}
		return 1;
	}

	@Override
	public void stop() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;
		final Display display = workbench.getDisplay();
		if (display == null)
			return;

		if (!display.isDisposed())
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					if (!display.isDisposed()){
						System.out.println("closing the workbench");
						workbench.close();
						System.out.println("closed the workbench");
					}
				}
			});
	}

	/**
	 * Exits if no good
	 */
	private void authenticateUser(final Display display) {

		// if java property not set, then use the process' user.
		if (LocalProperties.get(Authenticator.AUTHENTICATORCLASS_PROPERTY, null) == null) {
			UserAuthentication.setToUseOSAuthentication();
			return;
		}

		String userID = System.getProperty("user.name");

		// change to AuthenticationDialog_old to use an alternative which looks like the old swing version of the GDA
		// login
		final AuthenticationDialog dialog = new AuthenticationDialog(display, SWT.OPEN,
				"Login to the Data Acquisition Client", userID, null);

		final GenericDialog.PasswordChecker checker = new GenericDialog.PasswordChecker() {
			@Override
			public boolean isValid() {
				if (!dialog.isAutomatic()) {
					UserAuthentication.setToNotUseOSAuthentication(dialog.getUsername(), dialog.getPassword());

					// test if info given and user was authenticated
					if (UserAuthentication.getUsername() == null || UserAuthentication.getPassword() == null
							|| UserAuthentication.getUsername().length() == 0
							|| UserAuthentication.getPassword().length() == 0) {
						dialog.setErrorMessage("Please enter a user name and password");
						return false;
					}

					try {
						if (!UserAuthentication.isAuthenticated()) {
							dialog.setErrorMessage("Please enter a correct user name and password");
							return false;
						}
						return true;
					} catch (Exception e) {
						logger.error("Could not authenticate user", e);
						System.exit(0);
					}
				} else {
					UserAuthentication.setToUseOSAuthentication();
					return true;
				}
				return false;
			}
		};

		dialog.setChecker(checker);
		final Object ob = dialog.open();

		if (ob == null)
			System.exit(0);

		return;
	}

	/*
	 * Launch the ObjectServer to create the client implementation (as the AcquisitionFrame would do in original gda)
	 */
	private static boolean started = false;

	/**
	 * Application wide monitoring or special logging modifications
	 */
	private void customiseEnvironment() {
		var swtDisposeLogger = LoggerFactory.getLogger("GDAClientSWTDispose");
		if (!ServiceHolder.getSessionService().defaultConnectionActive()) {
			throw new IllegalStateException("ActiveMQ is not available - will not be able to connect to server");
		}
		Resource.setNonDisposeHandler(error -> {
			var trimmedTrace = stream(error.getStackTrace()).limit(10).toArray(StackTraceElement[]::new);
			error.setStackTrace(trimmedTrace);
			swtDisposeLogger.warn("SWT resource not disposed properly - trimmed creation stack:", error);
		});
	}

	private static void createObjectFactory() throws FactoryException {
		if (!started) {
			String gda_gui_beans = LocalProperties.get(LocalProperties.GDA_GUI_BEANS_XML, LocalProperties.get(LocalProperties.GDA_GUI_XML));
			if (gda_gui_beans != null) {
				SpringContext.registerFactory(gda_gui_beans);
			}
			started = true;
		}
	}
}
