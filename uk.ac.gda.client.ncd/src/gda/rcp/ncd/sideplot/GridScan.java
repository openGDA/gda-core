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

package gda.rcp.ncd.sideplot;

import gda.jython.JythonServerFacade;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot.GridProfile;

public class GridScan extends GridProfile {
	@Override
	protected void addControlWidgets(Composite parent) {
		final Group scanButtonArea = new Group(parent, SWT.NONE);
		scanButtonArea.setLayout(new GridLayout(1, false));
		scanButtonArea.setText("Control");
		final Button updateImage = new Button(scanButtonArea, SWT.NONE);
		updateImage.setText("Refresh Image");
		updateImage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JythonServerFacade.getInstance().runCommand("ncdgridscan.snap()");
			}
		});
		final Button startScan = new Button(scanButtonArea, SWT.NONE);
		startScan.setText("Start Mapping Scan");
		startScan.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JythonServerFacade.getInstance().runCommand("ncdgridscan.scan()");
			}
		});
	}
}