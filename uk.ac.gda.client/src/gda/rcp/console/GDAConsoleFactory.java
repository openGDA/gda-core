/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.rcp.console;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.ui.console.IConsoleFactory;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.debug.newconsole.PydevConsole;
import org.python.pydev.debug.newconsole.PydevConsoleCommunication;
import org.python.pydev.debug.newconsole.PydevConsoleInterpreter;
import org.python.pydev.debug.newconsole.env.PydevIProcessFactory;
import org.python.pydev.debug.newconsole.env.PydevIProcessFactory.PydevConsoleLaunchInfo;
import org.python.pydev.debug.newconsole.env.UserCanceledException;
import org.python.pydev.shared_interactive_console.console.IScriptConsoleCommunication;
import org.python.pydev.shared_interactive_console.console.ui.ScriptConsoleManager;


/**
 * This is the class responsible for creating the console (and setting up the communication
 * between the console server and the client).
 */
public class GDAConsoleFactory implements IConsoleFactory {

    @Override
    public void openConsole() {
        createConsole();
    }

    /**
     * @return a new PydevConsole or null if unable to create it (user cancels it)
     */
    public PydevConsole createConsole() {
        ScriptConsoleManager manager = ScriptConsoleManager.getInstance();
        try {
            PydevConsoleInterpreter interpreter = createDefaultPydevInterpreter();
            if(interpreter != null){
	            PydevConsole console = new PydevConsole(interpreter, null);
	            manager.add(console, true);
	            return console;
            }
        } catch (Exception e) {
        	//FIXME no longer on the interface
//            PydevPlugin.(e);
        }
        return null;
    }

    /**
     * @return A PydevConsoleInterpreter with its communication configured.
     *
     * @throws CoreException
     * @throws IOException
     * @throws UserCanceledException
     */
    @SuppressWarnings("unused")
	public static PydevConsoleInterpreter createDefaultPydevInterpreter() throws Exception,
            UserCanceledException {

//            import sys; sys.ps1=''; sys.ps2=''
//            import sys;print >> sys.stderr, ' '.join([sys.executable, sys.platform, sys.version])
//            print >> sys.stderr, 'PYTHONPATH:'
//            for p in sys.path:
//                print >> sys.stderr,  p
//
//            print >> sys.stderr, 'Ok, all set up... Enjoy'

    	IScriptConsoleCommunication protocol = new GDACorbaConsoleCommunication();

        PydevConsoleInterpreter consoleInterpreter = new PydevConsoleInterpreter();
		consoleInterpreter.setConsoleCommunication(protocol);
        consoleInterpreter.setNaturesUsed(new ArrayList<IPythonNature>());

        return consoleInterpreter;

    }

	@SuppressWarnings("unused")
	private static PydevConsoleCommunication createCommunication()
			throws UserCanceledException, Exception {
		PydevIProcessFactory iprocessFactory = new PydevIProcessFactory();

        PydevConsoleLaunchInfo interactiveLaunch = iprocessFactory.createInteractiveLaunch();
        if(interactiveLaunch == null){
            return null;
        }
        final ILaunch launch = interactiveLaunch.launch;
        if(launch == null){
            return null;
        }

        int port = Integer.parseInt(launch.getAttribute(PydevIProcessFactory.INTERACTIVE_LAUNCH_PORT));

        PydevConsoleCommunication protocol = new PydevConsoleCommunication(port, interactiveLaunch.process, interactiveLaunch.clientPort,
        		interactiveLaunch.cmdLine, interactiveLaunch.env, interactiveLaunch.encoding);
		return protocol;
	}
}