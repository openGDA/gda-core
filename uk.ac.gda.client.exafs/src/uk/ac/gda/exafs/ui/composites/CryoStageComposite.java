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

package uk.ac.gda.exafs.ui.composites;

import gda.jython.JythonServerFacade;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.richbeans.components.FieldBeanComposite;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.components.scalebox.RangeBox;

/**
 *
 */
public class CryoStageComposite extends FieldBeanComposite {
	
	private static final Logger logger = LoggerFactory.getLogger(XYThetaStageComposite.class);

	private RangeBox x;
	private RangeBox rot;

	@SuppressWarnings("unused")
	public CryoStageComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		Label label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("x");
		this.x = new RangeBox(this, SWT.NONE);
		x.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		x.setMaximum(0);
		x.setMinimum(-100);
		x.setDecimalPlaces(2);
		x.setUnit("mm");
		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		
		label.setText("rot");
		this.rot = new RangeBox(this, SWT.NONE);
		rot.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		rot.setUnit("deg");
		new Label(this, SWT.NONE);
		
		try {
			setMotorLimits("cryox", x);
			setMotorLimits("cryorot", rot);
			
		} catch (Exception e) {
			logger.warn("exception while fetching hardware limits: " + e.getMessage(), e);
		}
		
		Button btnSet = new Button(this, SWT.NONE);
		btnSet.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String xval = JythonServerFacade.getInstance().evaluateCommand("cryox()");
				
				if(xval.substring(xval.indexOf(".")+1).length()>2)
					x.setValue(xval.substring(0, xval.indexOf(".")+3));
				else
					x.setValue(xval);
				
				String rotval = JythonServerFacade.getInstance().evaluateCommand("cryorot()");
				if(rotval.substring(rotval.indexOf(".")+1).length()>2)
					rot.setValue(rotval.substring(0, rotval.indexOf(".")+3));
				else
					rot.setValue(rotval);
			}
		});
		
		
		
		btnSet.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnSet.setText("Get current values");

	}

	public void setMotorLimits(String motorName, RangeBox box) throws Exception{
		String lowerLimit = JythonServerFacade.getInstance().evaluateCommand(motorName+".getLowerMotorLimit()");
		String upperLimit = JythonServerFacade.getInstance().evaluateCommand(motorName+".getUpperMotorLimit()");
		box.setMinimum(Double.parseDouble(lowerLimit));
		box.setMaximum(Double.parseDouble(upperLimit));
	}
	
	public FieldComposite getX() {
		return x;
	}

	public FieldComposite getRot() {
		return rot;
	}

}
