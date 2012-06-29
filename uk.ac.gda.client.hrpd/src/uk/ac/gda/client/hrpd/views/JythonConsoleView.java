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

package uk.ac.gda.client.hrpd.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import javax.swing.JApplet;

import com.artenum.jyconsole.JyConsole;


public class JythonConsoleView extends ViewPart {
	public static final String ID = "uk.ac.gda.hrpd.views.JythonConsoleView";

	private Composite swtAwtBridge;
	
	public JythonConsoleView() {
		// TODO Auto-generated constructor stub
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		
		swtAwtBridge = new Composite(parent, SWT.EMBEDDED);
		java.awt.Frame frame = SWT_AWT.new_Frame(swtAwtBridge);
		JApplet applet = new JApplet();

		// set system properties for the JyConsole
		//Base dir used to set the default open directory for script loading
		System.setProperty("jyconsole.pref.file.path",System.getProperty("gda.config")+"/var/preference.data");
		System.setProperty("jyconsole.pref.script.dir", System.getProperty("gda.config")+"/scripts");
		System.setProperty("jyconsole.pref.txt.color.error","#FF0000");
		System.setProperty("jyconsole.pref.txt.color.normal","#000000");
		System.setProperty("jyconsole.pref.bg.color","#EEEEEE");
		// Select thread behavior between the two class name
		// - com.artenum.jyconsole.command.ThreadPerCommandRunner
		// - com.artenum.jyconsole.command.SingleThreadCommandRunner
		System.setProperty("jyconsole.pref.commandRunner.className","com.artenum.jyconsole.command.ThreadPerCommandRunner");
		//Choose to overide the standard output into the JyConsole or not.
		System.setProperty("jyconsole.pref.print.std.stream","false");
		//Choose to overide the standard output error into the JyConsole or not
		System.setProperty("jyconsole.pref.print.error.stream","false");
		//Loading script for initial configuration
		System.setProperty("jyconsole.pref.loading.script",System.getProperty("gda.config")+"/scripts/start_gda.py");

		JyConsole console = new JyConsole();
		
		frame.add(applet);
		applet.add(console);
		
	}

	@Override
	public void setFocus() {
		this.swtAwtBridge.setFocus();
	}

	
	
}
