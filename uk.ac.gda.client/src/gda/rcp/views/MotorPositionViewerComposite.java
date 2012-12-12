/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.rcp.views;

import gda.device.Scannable;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import uk.ac.gda.ui.viewer.MotorPositionViewer;

public class MotorPositionViewerComposite extends Composite {

	public MotorPositionViewerComposite(Composite parent, int style, Scannable scannable, Boolean layoutHoriz,
			String label, Integer decimalPlaces, String commandFormat, Boolean restoreValueWhenFocusLost) {
		this(parent, style, null, scannable, layoutHoriz, label, decimalPlaces, commandFormat, restoreValueWhenFocusLost); 
	}
	
	public MotorPositionViewerComposite(Composite parent, int style, final Display display, Scannable scannable, Boolean layoutHoriz,
				String label, Integer decimalPlaces, String commandFormat, Boolean restoreValueWhenFocusLost) {
		super(parent, style);
		
		GridLayoutFactory.fillDefaults().numColumns(layoutHoriz ? 2: 1).applyTo(this);
		GridDataFactory.fillDefaults().applyTo(this);
		MotorPositionViewer mpv = new MotorPositionViewer(this, scannable, label);		
		mpv.setCommandFormat(commandFormat);
		if (decimalPlaces != null) 
			mpv.setDecimalPlaces(decimalPlaces.intValue());
		else
			mpv.setDecimalPlaces(2);
		if (restoreValueWhenFocusLost != null) {
			mpv.setRestoreValueWhenFocusLost(restoreValueWhenFocusLost);
		}
		else {
			mpv.setRestoreValueWhenFocusLost(false);
		}
	}
}