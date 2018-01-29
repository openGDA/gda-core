/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;

/**
 * This is a class to ensure the client has a connection to the server. During the client startup if a connection can't
 * be established a dialog is shown and the client will exit. After that point a watchdog is started so if the server
 * connection is lost a dialog can be displayed to the user indicating the problem.
 *
 * @author James Mudd
 * @since GDA 9.7
 */
public final class ServerAvailableWatchdog {

	private static final Logger logger = LoggerFactory.getLogger(ServerAvailableWatchdog.class);

	/** The location of the server status port */
	private static final String SERVER_HOST = LocalProperties.get("gda.server.host");
	/** Server status port default 19999 */
	private static final int SERVER_STATUS_PORT = LocalProperties.getAsInt("gda.server.statusPort", 19999);

	/**
	 * This checks that the server is still reachable from this client. It does it by checking whether the status port
	 * on the GDA server is reachable. If the port is not reachable a dialog will be displayed to the user saying the
	 * connection was lost and suggesting the client should be closed.
	 * <p>
	 * Before starting the watchdog it checks once that the server can be reached. If at that point it fails a dialog is
	 * displayed indicating the server could never be reached. The watchdog will not be started and the method will
	 * return false.
	 *
	 * @return true If the server can be reached, and the watchdog has been started
	 *
	 * @since GDA 9.7
	 */
	public boolean startServerAvailableWatchdog() {

		// Check whether there is a server to connect to if not display dialog and exit
		if (!isServerReachable()) {
			// Using the logging here before it's setup so it goes to stdout. That's good because if the server
			// isn't running there will be no logserver so the message would be lost.
			logger.error("Can't connect to GDA server on: {}:{}", SERVER_HOST, SERVER_STATUS_PORT);
			// Show a dialog to explain to the user what to do.
			Display.getDefault().syncExec(this::displayServerCantBeReachedDialog);
			return false; // Couldn't connect to the server
		}

		// Server can be reached so start the watchdog
		logger.debug("Starting server available watchdog...");

		// Scheduled execution with a named daemon thread
		final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
			Thread thread = new Thread(runnable);
			thread.setName("Server available watchdog");
			thread.setDaemon(true);
			return thread;
		});

		// Start scheduled execution of checking whether the server is reachable every second starting now
		executorService.scheduleAtFixedRate(this::checkServerReachable, 0, 1, TimeUnit.SECONDS);

		logger.info("Started server available watchdog");
		return true;
	}

	/**
	 * Attempts to make a connection to the server status port. If it succeeds then then server is running.
	 *
	 * @return true if the server can be reached, false otherwise
	 */
	private boolean isServerReachable() {
		try {
			final Socket statusSocket = new Socket(SERVER_HOST, SERVER_STATUS_PORT);
			statusSocket.close();
			return true;
		} catch (IOException e) {
			logger.error("Server status port ({}:{}) not reachable", SERVER_HOST, SERVER_STATUS_PORT, e);
			return false;
		}
	}

	/**
	 * This is called by the scheduled execution every second. It checks if the server is reachable. If it is do
	 * nothing. If it's not prompt the user to inform them and ask how to proceed.
	 */
	private void checkServerReachable() {
		// If the server is reachable do nothing, if it's not inform the user.
		if (!isServerReachable()) {
			logger.debug("Server connection was lost asking user how to proceed");
			// syncExec here because we throw after this and the thread dies so need to block this thread while the
			// dialog is displayed.
			PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
				final int userSelection = displayServerConnectionLostDialog();
				if (userSelection == 1) { // 1 is the button index of "Exit Client"
					logger.info("User chose to close client after server connection was lost");
					// Close down the client
					PlatformUI.getWorkbench().close();
				} else { // User chose "Ignore" or closed the dialog
					logger.warn("User ignored the loss of server connection");
				}
			});
			// Throw because this causes the scheduled execution to be cancelled. Needed in the Ignore case to stop the
			// dialog being redisplayed every second.
			throw new IllegalStateException("Connection to the GDA server has been lost");
		}
	}

	/**
	 * Displays a dialog indicating the server connection was lost and asks the user what they want to do returns the
	 * index of the button pressed.
	 * <p>
	 * <b>Must be called in UI thread</b>
	 *
	 * @return 0 if "Ignore", or 1 if "Exit Client"
	 */
	private int displayServerConnectionLostDialog() {
		// Want the dialog to be modal so get the shell from the workbench
		final Shell shell = PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
		final MessageDialog messageBox = new MessageDialog(shell, "GDA Server connection lost", // Dialog title
				null, // Icon, null = inherit application icon
				// Dialog message
				"This GDA client has lost connection to the GDA server.\n\n"
						+ "Most likely the server has been shutdown or restarted. The client will need to be restarted"
						+ " to restore the conection.\n\n"
						+ "Choosing 'Ignore' will close this dialog and allow you to return to the client, but server"
						+ " features will not work.",
				SWT.ICON_ERROR, // Dialog type
				new String[] { "Ignore", "Exit Client" }, // Buttons: Ignore=0 Exit Client=1
				0); // Default to Ignore button if you hit enter

		// Open the dialog and ask the user what to do.
		return messageBox.open();
	}

	/**
	 * Displays a dialog indicating no connection to the GDA server and the client can't start.
	 * <p>
	 * <b>Must be called in UI thread</b>
	 *
	 * @return 0 for "Exit Client" pressed
	 */
	private int displayServerCantBeReachedDialog() {
		final MessageDialog messageBox = new MessageDialog(null, // Use top level shell
				"No GDA Server connection", // Dialog title
				null, // Icon, null = inherit application icon
				// Dialog message
				"The GDA client cannot connect to the the GDA server.\n\n"
						+ "Most likely the GDA server and client will need to be restarted.\n\n"
						+ "Currently the GDA client cannot start and will exit",
				SWT.ICON_ERROR, // Dialog type
				new String[] { "OK" }, // Buttons: OK=0
				0); // Default to OK button if you hit enter

		// Open the dialog and ask the user what to do.
		return messageBox.open();
	}

}
