/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.jython;

import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;
import gda.util.Sleep;

public class Gda5479Test {

	public static void main(String[] args) throws Exception {
		
		final JythonServer jythonServer = new JythonServer();
		jythonServer.configure();
		
		final JythonServerFacade facade = new JythonServerFacade(jythonServer);
		InterfaceProvider.setTerminalPrinterForTesting(facade);
		
		System.out.println("Starting threads");
		
		Thread[] threads = new Thread[2];
		
		for (int i=0; i<2; i++) {
			threads[i] = new Thread() {
				@Override
				public void run() {
					while (true) {
						// put a breakpoint on the following line, conditional on 'stop' being true
						jythonServer.runCommand("print 'hello'", "123");
					}
				}
			};
			threads[i].start();
		}
		
		Sleep.sleep(3000);
		
		System.out.println("Interrupting");
		stop = true;
		threads[0].interrupt();
	}
	
	public static boolean stop = false;

}
