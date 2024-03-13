/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.util.logging;

import static gda.util.logging.LoggerContextAdapter.persistantResetListener;
import static gda.util.logging.LoggerContextAdapter.resetListener;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchService;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.helpers.AttributesImpl;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.net.SocketAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.SaxEventRecorder;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.ConfigurationWatchList;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.util.Duration;

/**
 * Utility methods for Logback.
 */
public final class LogbackUtils {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LogbackUtils.class);

	/**
	 * Name of property that specifies the logging configuration file used for server-side processes (channel server, object servers).
	 */
	public static final String GDA_SERVER_LOGGING_XML = "gda.server.logging.xml";

	/**
	 * Name of property that specifies the logging configuration file used for client-side processes.
	 */
	public static final String GDA_CLIENT_LOGGING_XML = "gda.client.logging.xml";

	public static final String SOURCE_PROPERTY_NAME = "GDA_SOURCE";

	private LogbackUtils() {}

	/** Legacy configuration method for previous configuration system */
	// This method duplicates a lot of what is done in the other configureLoggingForProcess
	// method but is only intended to be a temporary method to aid the transition to
	// the new configuration framework
	@Deprecated(since = "9.29", forRemoval = true)
	public static void configureLoggingForProcess(String processName, String configFile) {
		if (configFile == null || configFile.isBlank()) {
			logger.error("Unable to configure without config filename");
			return;
		}
		LoggerContext context = getLoggerContext();

		// Reset logging - remove all appenders/levels and cancel previous file monitoring.
		context.reset();

		// If anything goes wrong from here onwards, we should throw an exception. It's not worth trying to log the
		// error, since there may be no appenders.

		// Capture java.util.logging calls and handle with slf4j
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();

		// Set any properties required when configuring logging
		context.addListener(persistantResetListener(
				ctx -> ctx.putProperty(SOURCE_PROPERTY_NAME, processName)
				));
		context.putProperty(SOURCE_PROPERTY_NAME, processName);

		// Configure using the specified logging configuration.
		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			configurator.doConfigure(configFile);
			setEventDelayToZeroInAllSocketAppenders(context);
			addShutdownHook();
		} catch (JoranException e) {
			throw new IllegalArgumentException("Unable to configure logging using: " + configFile, e);
		}
	}

	/**
	 * Configures Logback using specified configuration files and properties.
	 *
	 * @param processName The name of the process for which logging is being configured
	 * @param configFiles A list of files to use to customise the logging configuration
	 * @param properties A map of properties that should be made available in the logging context
	 */
	public static void configureLoggingForProcess(String processName, List<URL> configFiles, Map<String, String> properties) {
		// If no config filename is specified, log an error. Treat this as non-fatal, because Logback will still
		// be in its default state (so log messages will still be displayed on the console).
		if (configFiles == null || configFiles.isEmpty()) {
			logger.error("Unable to configure without logging filenames.");
			return;
		} else if (configFiles.stream().anyMatch(Objects::isNull)) {
			logger.error("Cannot configure logging with null or empty logging files");
			return;
		}

		refreshContext(processName, configFiles, properties);
		addShutdownHook();
	}

	private static void refreshContext(String processName, List<URL> configFiles, Map<String, String> properties) {
		LoggerContext context = getLoggerContext();
		System.out.format("Configuring logging with files: %s%n", configFiles); //NOSONAR may not be any logging at this point

		// Reset logging - remove all appenders/levels and cancel previous file monitoring.
		context.reset();

		// If anything goes wrong from here onwards, we should throw an exception. It's not worth trying to log the
		// error, since there may be no appenders.

		// Capture java.util.logging calls and handle with slf4j
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();

		// Set any properties required when configuring logging
		properties.forEach(context::putProperty);
		context.putProperty(SOURCE_PROPERTY_NAME, processName);

		// Configure using the specified logging configuration.
		try {
			var currentConfiguration = configureLogging(context, configFiles);
			setEventDelayToZeroInAllSocketAppenders(context);
			var monitor = new Monitor(currentConfiguration, () -> refreshContext(processName, configFiles, properties));
			var fileMonitoring = context
					.getScheduledExecutorService()
					.scheduleAtFixedRate(monitor, 10, 10, TimeUnit.SECONDS);
			context.addScheduledFuture(fileMonitoring);
			context.addListener(resetListener(ctx -> monitor.close()));
		} catch (JoranException e) {
			throw new IllegalArgumentException("Unable to configure logging using: " + configFiles, e);
		} catch (IOException e) {
			logger.error("Could not set up logging configuration file monitoring", e);
		}

	}

	/**
	 * Replacement for the default 'scan' and 'scanPeriod' settings in logback.
	 *
	 * As we have no top-level configuration file that imports the list of files
	 * generated at runtime, the builtin file monitoring does not work.
	 * Instead, set up a file watcher to monitor the directories containing the
	 * logging configuration files used.
	 *
	 * This monitoring runs in a background thread and will run until a file changes.
	 * It will then reconfigure the logger context and exit. The reconfiguration
	 * process should start a new monitoring thread as the list of included files
	 * may have changed.
	 */
	private static class Monitor implements Runnable {
		/** Filter to exclude the spurious events that don't match the required kind */
		private static final Predicate<WatchEvent<?>> SPURIOUS = e -> e.kind() != OVERFLOW;
		private final Runnable callback;
		private final Set<File> configuration;
		private WatchService watchService;
		public Monitor(Set<File> configuration, Runnable callback) throws IOException {
			logger.info("Monitoring {} for logging config changed", configuration);
			this.callback = callback;
			this.configuration = configuration;
			this.watchService = FileSystems.getDefault().newWatchService();
			initWatch();
		}
		private void initWatch() throws IOException {
			var configDirectories = configuration.stream()
					.map(File::getParent)
					.map(Path::of)
					.collect(toSet());
			for (var dir: configDirectories) {
				dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
			}
		}

		@Override
		public void run() {
			var key = watchService.poll();
			if (key == null) return;
			if (key.watchable() instanceof Path dir) {
				var configChanged = key.pollEvents().stream()
						.filter(SPURIOUS)
						.map(WatchEvent::context) // get the file that changed
						.filter(Path.class::isInstance)
						.map(Path.class::cast)
						.map(dir::resolve)
						.map(Path::toFile)
						.anyMatch(configuration::contains);
				if (configChanged) {
					logger.info("Logging configuration changed - reloading logging");
					callback.run();
					close();
				}
			}
			if (!key.reset()) {
				close();
			}
		}

		public void close() {
			try {
				watchService.close();
			} catch (IOException e) {
				logger.warn("Failed to close watch service", e);
			}
		}
	}

	/**
	 * For the default Logback logger context, dumps a list of all loggers, their levels, and their appenders.
	 */
	public static void dumpLoggers() {
		dumpLoggers(getLoggerContext());
	}

	/**
	 * Logback uses a {@link ScheduledThreadPoolExecutor} for {@code ServerSocket[Receiver,Appender]} connections which uses a thread
	 * to listen for clients and also a thread for each client. As documented in the Javadoc, {@link ScheduledThreadPoolExecutor}
	 * behaves as a fixed thread pool. Logback has coded this to 8 threads (see {@link CoreConstants#SCHEDULED_EXECUTOR_POOL_SIZE}).
	 * When more than 6 clients/log panels are connected a major issue arises as a backlog of tasks is created - each of which
	 * has ownership of a socket stuck in {@code CLOSE_WAIT} state. As clients time out and attempt to reconnect the queue increases
	 * and more sockets are used until the process reaches its quota for max open files.
	 * <p>
	 * This method can be scheduled to run regularly as a workaround which monitors the queue and adjusts the thread count
	 * to match demand, scaling it both up and down.
	 * <p>
	 * This technique was taken from GDA's {@code Async} class.
	 */
	public static void monitorAndAdjustLogbackExecutor() {
		LoggerContext context = getLoggerContext();
		ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) context.getScheduledExecutorService();
		// SCHEDULER stats
		int scheduleThreadCount = executor.getActiveCount();
		int schedulerPoolSize = executor.getCorePoolSize();
		int scheduleQueueSize = executor.getQueue().size();
		if (scheduleThreadCount >= schedulerPoolSize) {
			logger.warn("Logback scheduled thread pool using {}/{} threads. Queue size: {}", scheduleThreadCount, schedulerPoolSize, scheduleQueueSize);
			int newThreadPoolSize = schedulerPoolSize + 4; // Ramp up quickly to combat rising sockets
			logger.info("Increasing Logback scheduler pool size to {}", newThreadPoolSize);
			executor.setCorePoolSize(newThreadPoolSize);
		} else {
			logger.trace("Logback scheduled pool thread using {}/{} threads. Queue size: {}", scheduleThreadCount, schedulerPoolSize, scheduleQueueSize);
			if (schedulerPoolSize > scheduleThreadCount + 2 && schedulerPoolSize > CoreConstants.SCHEDULED_EXECUTOR_POOL_SIZE) {
				/*
				 * Reducing the core pool size while all threads are active will not kill the threads It only kills them when they become idle so in the case
				 * where new tasks have been added since the threads were counted, no processes will be affected
				 */
				int newSize = schedulerPoolSize - 1;
				logger.info("Reducing the Logback scheduler pool size to {}", newSize);
				executor.setCorePoolSize(newSize);
			}
		}
	}

	/**
	 * Returns the default Logback logger context.
	 *
	 * <p>This method can be used instead of calling {@link LoggerFactory#getILoggerFactory()} directly and casting the
	 * result to a Logback {@link LoggerContext}. It assumes that Logback is being used, so that the singleton SLF4J
	 * logger factory can be cast to a Logback {@link LoggerContext}. If a {@link ClassCastException} occurs, a more
	 * useful exception will be thrown instead.
	 *
	 * @return the Logback logger context
	 */
	private static LoggerContext getLoggerContext() {
		try {
			return (LoggerContext) LoggerFactory.getILoggerFactory();
		} catch (ClassCastException e) {
			final String msg = "Couldn't cast the logger factory to a Logback LoggerContext. Perhaps you aren't using Logback?";
			throw new IllegalStateException(msg, e);
		}
	}

	/**
	 * Resets the default Logback logger context. This causes the following to happen:
	 *
	 * <ul>
	 * <li>All appenders are removed from any loggers that have been created. (Loggers are created when they are
	 * configured or used from code.)</li>
	 * <li>Existing loggers are retained, but their levels are cleared.</li>
	 * </ul>
	 */
	static void resetLogging() {
		getLoggerContext().reset();
	}

	/**
	 * Configures the logger context using the specified configuration files.
	 *
	 * <p>Appenders defined in the file are <b>added</b> to loggers. Repeatedly configuring using the same configuration
	 * file will result in duplicate appenders.
	 *
	 * @param context The LoggerContext to be configured
	 * @param configFiles List of logback configuration files
	 * @throws JoranException if configuration is not valid
	 * @throws ActionException
	 * @throws IOException
	 */
	private static Set<File> configureLogging(LoggerContext context, List<URL> configFiles) throws JoranException {
		ConfigurationWatchList cwl = new ConfigurationWatchList();
		cwl.setContext(context);
		ConfigurationWatchListUtil.registerConfigurationWatchList(context, cwl);

		var configurator = new JoranConfigurator();
		configurator.setContext(context);
		configurator.doConfigure(mockMainLoggingFile(context, configFiles));

		return Set.copyOf(cwl.getCopyOfFileWatchList());
	}

	/**
	 * This is a hack to make a list of configuration files be loaded by
	 * logback as if they were included from a single file.
	 * <p>
	 * When logback parses a file, the individual SaxEvents are parsed in turn.
	 * This method generates the SaxEvents that would be generated by parsing a
	 * single file containing a series of includes so that they can be used to
	 * configure the context relying on the normal internal include handling
	 * that logback uses.
	 * @param context Logback LoggerContext
	 * @param configFiles List of URLs of files to be included in configuration
	 * @return List of SaxEvents that would be generated by parsing an xml file that imported
	 *         all the configuration files.
	 */
	private static List<SaxEvent> mockMainLoggingFile(LoggerContext context, List<URL> configFiles) {
		var recorder = new SaxEventRecorder(context);
		recorder.setDocumentLocator(new NullLocator());
		recorder.startElement("", "configuration", "configuration", attributes(Map.of("debug", "true")));
		for (var url: configFiles) {
			recorder.startElement("", "include", "include", attributes(Map.of("url", url.toExternalForm())));
			recorder.endElement("", "include", "include");
		}
		recorder.endElement("", "configuration", "configuration");
		return recorder.saxEventList;
	}

	private static Attributes attributes(Map<String, String> atts) {
		var att = new AttributesImpl();
		atts.forEach((name, value) -> att.addAttribute("", name, name, "String", value));
		return att;
	}

	/**
	 * For the specified Logback logger context, dumps a list of all loggers, their levels, and their appenders.
	 */
	private static void dumpLoggers(LoggerContext loggerContext) {
		System.out.println("Loggers:"); // NOSONAR No point using loggers to debug logging
		List<Logger> loggers = loggerContext.getLoggerList();
		for (Logger logger : loggers) {
			System.out.printf("    %s level=%s effective=%s%n", // NOSONAR
					logger, logger.getLevel(), logger.getEffectiveLevel());
			Iterable<Appender<ILoggingEvent>> appenders = logger::iteratorForAppenders;
			for (var appender: appenders) {
				System.out.println("        " + appender); // NOSONAR
			}
		}
	}

	private static void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(() ->  {
			logger.info("Shutting down logging");
			// See https://logback.qos.ch/manual/configuration.html#stopContext
			getLoggerContext().stop();
		}));
	}

	private static void setEventDelayToZeroInAllSocketAppenders(LoggerContext context) {
		// Force event delay to zero for all SocketAppenders.
		// Prevents 100 ms delay per log event when a SocketAppender's queue fills up
		// (this happens if the SocketAppender can't connect to the remote host)
		for (Logger logger : context.getLoggerList()) {
			final Iterator<Appender<ILoggingEvent>> appenderIterator = logger.iteratorForAppenders();
			while (appenderIterator.hasNext()) {
				final Appender<ILoggingEvent> appender = appenderIterator.next();
				if (appender instanceof SocketAppender socket) {
					socket.setEventDelayLimit(Duration.buildByMilliseconds(0));
				}
			}
		}
	}

	/** Implementation of Locator that serves no purpose other than to prevent NPEs */
	private static final class NullLocator implements Locator {
		@Override public String getSystemId() { return null; }
		@Override public String getPublicId() { return null; }
		@Override public int getLineNumber() { return -1; }
		@Override public int getColumnNumber() { return -1; }
	}
}
