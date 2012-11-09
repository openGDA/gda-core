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

package gda.rcp.ncd.views;

import gda.swing.ncd.WatchPanel;

import javax.swing.JApplet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class WatchView extends ViewPart {

	public static final String ID = "gda.rcp.ncd.views.WatchView"; //$NON-NLS-1$
	private Composite swtAwtBridge;

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {			
		this.swtAwtBridge = new Composite(parent, SWT.EMBEDDED);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(swtAwtBridge, "uk.ac.diamond.gda.rcp.ncd.configure");
		java.awt.Frame frame = SWT_AWT.new_Frame(swtAwtBridge);
		JApplet applet = new JApplet();
		WatchPanel watchPanel = new WatchPanel();
		watchPanel.setAutoHide(false);
		frame.add(applet);
		applet.add(watchPanel);
		watchPanel.configure();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		this.swtAwtBridge.setFocus();
	}
}