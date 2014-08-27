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

package uk.ac.gda.exafs.ui.detector.xspress;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.beans.XspressParameters;
import uk.ac.gda.beans.XspressROI;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;

public class RegionType {
	private ComboWrapper regionType;
	private Label lblRegionType;
	
	public RegionType(Composite parent) {
		lblRegionType = new Label(parent, SWT.NONE);
		lblRegionType.setText("Region type");
		regionType = new ComboWrapper(parent, SWT.READ_ONLY);
		regionType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		regionType.setItems(new String[]{XspressParameters.VIRTUALSCALER, XspressROI.MCA});
		regionType.select(0);
	}

	public ComboWrapper getRegionType() {
		return regionType;
	}
	
	public void setVisible(boolean visible){
		lblRegionType.setVisible(visible);
		regionType.setVisible(visible);
	}

}