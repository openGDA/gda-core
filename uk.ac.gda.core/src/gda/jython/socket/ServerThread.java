/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.jython.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import gda.jython.JythonServerFacade;
import gda.jython.Terminal;
import gda.scan.ScanDataPoint;
import gda.util.Version;

/**
 * Thread for dealing with a client connected to the Jython server.
 */
public abstract class ServerThread extends Thread implements Terminal, SessionClosedCallback {

	private static final Logger logger = LoggerFactory.getLogger(ServerThread.class);

	private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

	protected final JythonServerFacade command_server = JythonServerFacade.getInstance();

	private OutputStream out = null;
	private InputStream in = null;

	private static final String WELCOME_BANNER_FILENAME = "welcome_banner.txt";
	private static final String WELCOME_BANNER = readBanner();
	private static String readBanner() {
		try {
			InputStream is = ServerThread.class.getResourceAsStream(WELCOME_BANNER_FILENAME);
			Reader r = new InputStreamReader(is);
			return FileCopyUtils.copyToString(r);
		} catch (IOException e) {
			logger.warn("Couldn't read welcome banner", e);
			return "";
		}
	}

	protected ServerThread() {
		super("CommandThread");

		// This also adds this instance to the JSF's list of terminals, so as to receive output
		command_server.addIObserver(this); //FIXME: potential race condition
	}

	/**
	 * Creates a server thread.
	 *
	 * @param inputStream client input stream
	 * @param outputStream client output stream
	 */
	public ServerThread(InputStream inputStream, OutputStream outputStream) {
		this();
		setOutputStream(outputStream);
		setInputStream(inputStream);
	}

	protected void setInputStream(InputStream inputStream) {
		in = inputStream;
	}

	protected void setOutputStream(OutputStream outputStream) {
		this.out = outputStream;
	}

	@Override
	public synchronized void run() {
		write(String.format(WELCOME_BANNER, Version.getRelease()));

		try {
			new JlineServerListenThread(this.in, this.out, this).start();
		} catch (IOException e) {
			logger.error("Unable to create thread to listen to client", e);
		}
		// Give server a chance to setup up things, so this message gets to the peer too
		try {
			Thread.sleep(100);
		} catch (InterruptedException ex) {
			logger.error("InterruptedException while waiting 100ms to give the server a chance to set things up.", ex);
		}
	}

	@Override
	public synchronized void update(Object name, Object data) {
		if (data instanceof ScanDataPoint) {
			String string = ((ScanDataPoint) data).toString();
			write(string);
		}
	}

	@Override
	public synchronized void write(byte[] data) {
		try {
			out.write(data);
			out.flush();
		} catch (IOException e) {
			// Re-encode the data as UTF8 to make it readable
			logger.error("Error writting '{}' to console", new String(data, UTF8_CHARSET), e);
		}
	}

	@Override
	public void write(String output) {
		// Call through to the byte[] method, after encoding with UTF8
		write(output.getBytes(UTF8_CHARSET));
	}

}