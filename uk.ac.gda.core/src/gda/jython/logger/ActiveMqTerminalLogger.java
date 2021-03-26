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

import java.util.Optional;

import gda.jython.JythonServer;
import gda.jython.TerminalInput;
import gda.jython.TerminalOutput;
import gda.observable.IObserver;
import gda.scan.ScanDataPoint;
import uk.ac.diamond.daq.api.messaging.Destination;
import uk.ac.diamond.daq.api.messaging.Message;
import uk.ac.diamond.daq.api.messaging.MessagingService;
import uk.ac.gda.core.GDACoreActivator;

/**
 * Class to send text shown in Jython terminal window to ActiveMQ. Destination topic is {@link #DEFAULT_QUEUE_TOPIC}.
 * This is added as a listener to {@link JythonServer} - the update method sends a text representation of all {@link TerminalInput},
 * {@link TerminalOutput} and {@link ScanDataPoint} messages to ActiveMq in same format as displayed in Jython console.
 *
 * This can also be used in place of a terminal logger {@link RedirectableFileLogger} to send data to ActiveMq rather than a
 * file, by creating the bean with the no-arg constructor and passing it to the logger adapter objects.
 */
public class ActiveMqTerminalLogger implements LineLogger, IObserver {
	public static final String DEFAULT_QUEUE_TOPIC = "gda.jython.terminal.output";

	public ActiveMqTerminalLogger() {
	}

	/**
	 *
	 * @param jythonServer The GDA's {@link JythonServer}.
	 */
	public ActiveMqTerminalLogger(JythonServer jythonServer) {
		jythonServer.addIObserver(this);
	}

	@Destination(ActiveMqTerminalLogger.DEFAULT_QUEUE_TOPIC)
	private static class TerminalInputOutput implements Message {
		private final String message;

		public TerminalInputOutput(String message) {
			this.message = message;
		}

		/** This is needed so that the 'message' field is included during JSon serializion */
		@SuppressWarnings("unused")
		public String getMessage() {
			return message;
		}
	}

	@Override
	public void update(Object source, Object event) {
		// Watch for terminal input, terminal output, scan datapoint events send the input/output to activeMq
		StringBuilder str = new StringBuilder();
		if (event instanceof TerminalInput) {
			TerminalInput input = (TerminalInput) event;
			str.append(">>> " + String.join("\n... ", input.lines()));
		}
		else if (event instanceof TerminalOutput) {
			TerminalOutput output = (TerminalOutput) event;
			str.append(output.getOutput().stripTrailing());
		}
		else if (event instanceof ScanDataPoint) {
			ScanDataPoint sdp = (ScanDataPoint) event;
			if (sdp.getCurrentPointNumber()==0) {
				str.append(sdp.getHeaderString()+"\n"); // add the column headers
			}
			str.append(sdp.toFormattedString());
		}

		if (str.length() > 0) {
			Optional<MessagingService> optionalJms = GDACoreActivator.getService(MessagingService.class);
			optionalJms.ifPresent(jms ->
					jms.sendMessage(new TerminalInputOutput(str.toString()))
			);
		}
		sendToActiveMq(str.toString());
	}

	@Override
	public void log(String msg) {
		if (msg == null || msg.isEmpty()) {
			return;
		}
		String[] splitStr = msg.split("[\\r?\\n]");
		for(String str : splitStr) {
			sendToActiveMq(str.stripTrailing());
		}
	}

	private void sendToActiveMq(String str) {
		if (str != null && !str.isEmpty()) {
			Optional<MessagingService> optionalJms = GDACoreActivator.getService(MessagingService.class);
			optionalJms.ifPresent(jms ->
			jms.sendMessage(new TerminalInputOutput(str)));
		}
	}
}