package uk.ac.diamond.daq.server;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import gda.jython.GDAJythonClassLoader;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;
import gda.util.ObjectServer;
import gda.util.SpringObjectServer;
import gda.util.Version;
import uk.ac.diamond.daq.server.configuration.IGDAConfigurationService;
import uk.ac.diamond.daq.server.configuration.commands.ObjectServerCommand;
import uk.ac.diamond.daq.services.PropertyService;

/**
 * This class controls all aspects of the application's execution
 */
public class GDAServerApplication implements IApplication {

	private static final Logger logger = LoggerFactory.getLogger(GDAServerApplication.class);

	private static IGDAConfigurationService configurationService;

	private final CountDownLatch shutdownLatch = new CountDownLatch(1);
	private final Map<String, ObjectServer> objectServers = new HashMap<>();

	private Process logServerProcess;

	private ServerSocket statusPort;

	/**
	 * Application start method invoked when it is launched. Loads the required configuration via  the external OSGI configuration service.
	 * Starts the 4 (or more) servers and then execution waits for the shutdown hook trigger, multiple object Servers may be started.
	 * If the application is run from the command line or eclipse it only requires the config file (-f) and profile (-p) args
	 * to be supplied; in this case it will default to the example-config values for all other things that the scripts pass in.
	 */
	public Object start(IApplicationContext context) throws Exception
	{
		// Log some info for debugging - this only goes to stdout as logging is not setup yet
		logger.info("Starting GDA server application {}", Version.getRelease());
		logger.info("Java version: {}", System.getProperty("java.version"));
		logger.info("JVM arguments: {}", ManagementFactory.getRuntimeMXBean().getInputArguments());

		ApplicationEnvironment.initialize();
		configurationService.loadConfiguration();

		try {
			try {
				logServerProcess =  configurationService.getLogServerCommand().execute();
				logger.info("Log server starting");
			} catch (Exception subEx) {
				logger.error("Unable to start log server, GDA server shutting down");
				throw subEx;
			}

			for (ObjectServerCommand command : configurationService.getObjectServerCommands()) {
				ObjectServer server = command.execute();
				objectServers.put(command.getProfile(), server);
				logger.info("{} object server started", command.getProfile());
				// Also make it obvious in the IDE Console.
				final String hline_80char = "================================================================================";
				System.out.println(hline_80char + "\n" + command.getProfile() + " object server started\n" + hline_80char);
			}
		} catch (Exception ex) {
			logger.error("GDA server startup failure", ex);
			ex.printStackTrace();
			stop(); // N.B. this method does not exit the app, it just cleans up resources.
		}

		if (!objectServers.isEmpty()) {
			// Once we are here all the servers have started
			openStatusPort();
			awaitShutdown();
			logger.info("GDA server application ended");
		}
		return IApplication.EXIT_OK;
	}

	/**
	 * This opens a port on the server. The presence of this open port can be used
	 * by the client to ensure the server is running. It would be possible to extend
	 * this to offer information such as server uptime, connected clients etc.
	 *
	 * @Since GDA 9.7
	 */
	private void openStatusPort() {
		// TODO Here use the PropertyService for now but once backed by sys properties will not be needed.
		final int serverPort = getPropertyService().getAsInt("gda.server.statusPort", 19999);
		try {
			statusPort = new ServerSocket(serverPort);
			Executors.newSingleThreadExecutor().execute(this::acceptStatusPortConnections);
			logger.debug("Opened status port on: {}", serverPort);
		} catch (IOException e) {
			logger.error("Opening status port on {} failed", serverPort, e);
		}
	}

	private void acceptStatusPortConnections() {
		while (!statusPort.isClosed()) {
			try {
				final Socket s = statusPort.accept();
				s.close();
			} catch (IOException e) {
				if (statusPort.isClosed()) {
					// Normal shutdown case. The port is closed while waiting to accept
					logger.debug("Stopping accepting status port connections");
					break;
				}
				logger.error("Exception occurred while accepting status port connection", e);
			}
		}
	}

	/**
	 * This closes the port on the server opened by the {@link #openStatusPort()}
	 * method
	 *
	 * @Since GDA 9.7
	 */
	private void closeStatusPort() {
		if (statusPort != null) { // Will be null if server fails to start fully
			try {
				statusPort.close();
				logger.debug("Closed status port");
			} catch (IOException e) {
				logger.error("Error closing status port", e);
			}
		}
	}

	/**
	 * Clears up all the resources created by start and then clears the {@link #shutdownLatch}
	 * allowing the {@link #start(IApplicationContext)} to complete.
	 */
	public void stop() {
		logger.info("GDA application stopping");
		ITerminalPrinter printer = InterfaceProvider.getTerminalPrinter();
		// If server startup failed, this may not have been created
		if (printer != null) {
			// Notify via Jython console this is useful as dead clients will display the message
			printer.print("GDA server is shutting down");
		}

		closeStatusPort();

		if (objectServers.size() > 0) {

			// Shutdown using the SpringObjectServer shutdown
			// TODO: Refactor command class so we can lose the cast
			for (Map.Entry<String, ObjectServer> entry : objectServers.entrySet()) {
				((SpringObjectServer)entry.getValue()).shutdown();
			}
			objectServers.clear();
		}

		try {
			logger.info("Log Server shutting down");
			((LoggerContext)LoggerFactory.getILoggerFactory()).stop();
			if (!logServerProcess.destroyForcibly().waitFor(1000, TimeUnit.MILLISECONDS)) {
				logger.error("Shutdown of log server timed out, check for orphaned process");
			}
		} catch (InterruptedException e) {
			logger.error("Shutdown of log server interrupted, check for orphaned process", e);
			e.printStackTrace(); // log server might be dead
			Thread.currentThread().interrupt();
		}

		GDAJythonClassLoader.closeJarClassLoaders();
		ApplicationEnvironment.release();
		shutdownLatch.countDown();
	}

	/**
	 * Make provision for graceful shutdown by adding a shutdown listener and then waiting on the
	 * {@link #shutdownLatch}. When shutdown is triggered {@link #stop()} is called which clears
	 * all created objects/processes and then clears the latch.
	 *
	 * @throws InterruptedException if the shutdwonHook thread is interrupted
	 */
	protected void awaitShutdown() throws InterruptedException {
		Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
		shutdownLatch.await();
	}

	public static void setConfigurationService(IGDAConfigurationService service) {
		configurationService = service;
	}

	// TODO Once LocalProperties is backed by System properties remove this
	private PropertyService getPropertyService() {
		return GDAServerActivator.getService(PropertyService.class)
				.orElseThrow(() -> new IllegalStateException("No PropertyService is available"));
	}
}
