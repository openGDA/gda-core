package gda.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.util.StringUtils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.net.SimpleSocketServer;
import ch.qos.logback.classic.net.SocketNode;
import ch.qos.logback.core.joran.spi.JoranException;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.util.logging.LogbackUtils;

/**
 * A simple {@link SocketNode} based server for Logging.
 *
 * <pre>
 *     &lt;b&gt;Usage:&lt;/b&gt; java gda.util.LogServer port configFile
 *
 *     where
 * <em>
 * port
 * </em>
 *     is a port number where the server listens and
 * <em>
 * configFile
 * </em>
 *     is an xml configuration file fed to {@link JoranConfigurator}.
 * </pre>
 *
 * or
 *
 * <pre>
 * you need to set the following java properties
 * <em>
 * gda.server.logging.port
 * </em>
 * 	is the port number where this Log server listens and
 * <em>
 * gda.logserver.xml
 * </em>
 * 	is an xml configuration file fed to {@link JoranConfigurator}
 * .
 */
public class LogServer implements Configurable, BeanNameAware {

	static Logger logger = LoggerFactory.getLogger(LogServer.class);

	private static final String CONFIG_FILE_PROPERTY = "gda.logserver.xml";

	private static final String PORT_NUMBER_PROPERTY = "gda.server.logging.port";

	private static final String LISTENING_PORT_PROPERTY = "gda.logserver.port";

	private static final int DEFAULT_PORT = 6000;

	/**
	 * Entry point for starting up the log server as a standalone application.
	 *
	 * @param argv
	 *            command-line arguments
	 * @throws JoranException
	 *             if the logging system cannot be configured
	 */
	public static void main(String argv[]) throws JoranException {
		LogServer logServer = null;
		String configFile;
		if (argv.length == 2) {
			final String portStr = argv[0];
			try {
				int port = Integer.parseInt(portStr);
				logServer = new LogServer(port, argv[1]);
			} catch (java.lang.NumberFormatException e) {
				e.printStackTrace();
				usage("Could not interpret port number [" + portStr + "].");
			}
		} else if ((configFile = System.getProperty(CONFIG_FILE_PROPERTY)) != null) {
			String portStr = null;
			int port;
			if ((portStr = System.getProperty(LISTENING_PORT_PROPERTY, null)) != null) {
				port = Integer.parseInt(portStr);
			} else {
				port = Integer.getInteger(PORT_NUMBER_PROPERTY, DEFAULT_PORT);
			}
			logServer = new LogServer(port, configFile);
		} else {
			usage("Wrong number of arguments or " + CONFIG_FILE_PROPERTY + " property not set!");
		}

		if (logServer != null) {
			logServer.configureAndStartLogServer();
		}
	}

	static void usage(String msg) {
		System.err.println(msg);
		System.err.println("Usage: java " + LogServer.class.getName() + " [port configFile]");
		System.exit(1);
	}

	int port;
	String configFile;
	SimpleSocketServer socketServer;
	LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

	/**
	 * Creates a new log server.
	 *
	 * @param port
	 *            the port on which the log server will listen
	 * @param configFile
	 *            the logging configuration file
	 */
	public LogServer(int port, String configFile) {
		this.port = port;
		this.configFile = configFile;
		socketServer = new SimpleSocketServer(lc, port);
	}

	/**
	 * Creates a new unconfigured log server.
	 */
	public LogServer() {
		// do nothing
	}

	/**
	 * Sets the port on which the log server listens.
	 *
	 * @param port
	 *            the port on which the log server listens
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Sets the configuration file used by the log server.
	 *
	 * @param configFile
	 *            the log server's configuration file
	 */
	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	void configureAndStartLogServer() throws JoranException {
		//Use stdout as use of logger is no good if the logging configuration is wrong
		System.out.println("Configure logserver using file " + StringUtils.quote(configFile));
		SimpleSocketServer.configureLC(lc, configFile);
		LogbackUtils.addSourcePropertyAndListener(lc, "logserver");
		LogbackUtils.setEventDelayToZeroInAllSocketAppenders(lc);
		socketServer.start();
	}

	private String name;

	@Override
	public void setBeanName(String name) {
		this.name = name;
	}

	/**
	 * Configures this log server, and starts a thread to run it.
	 */
	@Override
	public void configure() throws FactoryException {
		try {
			configureAndStartLogServer();
		} catch (JoranException e) {
			String msg = "Unable to configure LogServer " + name;
			logger.error(msg, e);
			throw new FactoryException(msg, e);
		}
	}

}
