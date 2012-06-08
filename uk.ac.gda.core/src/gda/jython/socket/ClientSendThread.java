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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Reads from standard input and passes that to a socket
 */
public class ClientSendThread extends Thread {
	PrintWriter out = null;

	/**
	 * @param out
	 */
	public ClientSendThread(PrintWriter out) {
		this.out = out;
	}

	@Override
	public void run() {
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String fromUser;

		try {
			while ((fromUser = stdIn.readLine()) != null) {
				out.println(fromUser);
			}
		} catch (IOException ex) {
		}
	}
}
