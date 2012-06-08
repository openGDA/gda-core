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

import gda.jython.JythonServerFacade;
import gda.jython.Terminal;
import gda.scan.ScanDataPoint;
import gda.util.Sleep;
import gda.util.Version;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

/**
 * Thread for dealing with a client connected to the Jython server.
 */
public class ServerThread extends Thread implements Terminal {

	private static final Logger logger = LoggerFactory.getLogger(ServerThread.class);
	
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
	
	protected JythonServerFacade command_server = JythonServerFacade.getInstance();

	protected PrintWriter out = null;

	protected BufferedReader in = null;

	protected ServerThread() {
		super("CommandThread");
		
		// This also adds this instance to the JSF's list of terminals, so as to receive output
		command_server.addIObserver(this);
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
		in = new BufferedReader(new InputStreamReader(inputStream));
	}
	
	protected void setOutputStream(OutputStream outputStream) {
		out = new PrintWriter(outputStream, true);
	}

	@Override
	public synchronized void run() {
		out.printf(WELCOME_BANNER, Version.RELEASE_VER);
		new ServerListenThread(this.in).start();
		// Give server a chance to setup up things, so this message gets to the peer too
		Sleep.sleep(100);
	}

	@Override
	public synchronized void update(Object name, Object data) {
		if (data instanceof ScanDataPoint) {
			String point = ((ScanDataPoint) data).toString();
			out.println(point);
			out.flush();
		}
	}

	@Override
	public synchronized void write(byte[] data) {
		out.print(new String(data));
		out.flush();
	}
}