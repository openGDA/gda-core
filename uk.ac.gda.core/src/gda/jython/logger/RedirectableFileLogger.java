/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.jython.logger;

import gda.data.ObservablePathProvider;
import gda.data.PathChanged;
import gda.observable.IObserver;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;

/**
 * A RedirectableLogger logs strings to a file maintained by the instance and to a standard slf4j logger. The log file's
 * location will be updated upon receipt of PathChanged events from a configured {@link ObservablePathProvider}. When
 * the file location changes, a note will left in the last file directing a reader to the new file, and a note will be
 * made in the new file indicating where the log came from.
 * <p>
 * This might be used for example to create a server-side logger that mirrors everything from a Jython terminal window to a log
 * file in the current visit directory. This should typically be done in Spring by wiring into a server's
 * JythonServerFacade and command-server through adaptors. For example, given that a JythonServer has been made (always named
 * 'command_server'), try this to get a JythonServerFacade reference:
 *
 * <p><code><pre>
 *
 * <bean id="jython_server_facade" class="gda.jython.JythonServerFacade">
 *       <constructor-arg ref="command_server" />
 * </bean>
 * </pre></code><p>
 * and then the following to build up a typical logger:
 * <p><code><pre>
 * <bean id="terminal_logger" class="gda.jython.logger.RedirectableFileLogger">
 * 	     <constructor-arg ref="terminallog_path_provider" />
 * </bean>
 * <bean class="gda.jython.logger.OutputTerminalAdapter">
 * 	     <constructor-arg ref="jython_server_facade" />
 * 	     <constructor-arg ref="terminal_logger"/>
 * </bean>
 * <bean class="gda.jython.logger.InputTerminalAdapter">
 *      <constructor-arg ref="command_server" />
 *      <constructor-arg ref="terminal_logger"/>
 * </bean>
 * <bean class="gda.jython.logger.ScanDataPointAdapter">
 * 	     <constructor-arg ref="jython_server_facade" />
 * 	     <constructor-arg ref="terminal_logger"/>
 * </bean>
 * <bean class="gda.jython.logger.BatonChangedAdapter">
 * 	     <constructor-arg ref="jython_server_facade" />
 *       <constructor-arg ref="terminal_logger"/>
 * 	</bean>
 *
 * </pre></code><p>
 *
 * where the terminallog_path_provider bean might be a dummy:
 *
 * <p><code><pre>
 * 	<bean id="terminallog_path_provider" class="gda.data.SimpleObservablePathProvider">
 * 		<property name="path" value="${gda.data.scan.datawriter.datadir}/gdaterminal.log" />
 * 		<property name="local" value="true" />
 * 	</bean>
* </pre></code><p>
 *
 * or a one that tracks the server's visit metadata:
 *
 * <p><code><pre>
 *  	<bean id="terminallog_path_provider" class="gda.data.ObservablePathConstructor">
 * 		<property name="template" value="${gda.data.scan.datawriter.datadir}/gdaterminal.log" />
 * 		<property name="gdaMetadata" ref="GDAMetadata" />
 * 		<property name="local" value="true" />
* </pre></code><p>
 */

public class RedirectableFileLogger implements LineLogger, IObserver {

	static private org.slf4j.Logger logger = LoggerFactory.getLogger(RedirectableFileLogger.class);

	private FileAppender<ILoggingEvent> localAppender;

	private ch.qos.logback.classic.Logger localLogger;

	public RedirectableFileLogger(ObservablePathProvider logFilePathProvider) {

		// Context
		LoggerContext localContext = new LoggerContext();

		// Layout
		PatternLayout localLayout = new PatternLayout();
		localLayout.setPattern("%d | %m%n");
		localLayout.setContext(localContext);
		localLayout.start();

		// file appender
		localAppender = new FileAppender<ILoggingEvent>();
		localAppender.setAppend(true);
		localAppender.setFile(logFilePathProvider.getPath());
		localAppender.setContext(localContext);
		localAppender.setLayout(localLayout);
		localAppender.start();

		// logger
		localLogger = localContext.getLogger("TerminalLogger");
		localLogger.setLevel(Level.ALL);
		localLogger.addAppender(localAppender);

		// register for path updates
		logFilePathProvider.addIObserver(this); //FIXME: potential race condition
	}

	private void setFile(String logfile) {
		localAppender.stop();
		localAppender.setFile(logfile);
		localAppender.start();
	}

	public String getFile() {
		return localAppender.getFile();
	}

	@Override
	public void log(String msg) {
		String lines[] = msg.split("\\r?\\n");
		for (String line : lines) {
			String stripEnd = StringUtils.stripEnd(line, null);
			if(logger.isDebugEnabled()){
				logger.debug(" | " + stripEnd);
			}
			localLogger.info(stripEnd);
		}
	}

	@Override
	public void update(Object source, Object event) {

		String newFile = ((PathChanged) event).getPath();
		if (!newFile.equals(getFile())) {
			String oldFile = getFile();
			log("<<<log moved to: " + newFile + ">>>");
			setFile(newFile);
			log("<<<log moved from: " + oldFile + ">>>");
		}
	}
}
