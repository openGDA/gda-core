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

package uk.ac.gda.exafs.ui.composites;

import gda.jython.JythonServerFacade;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import uk.ac.gda.richbeans.components.FieldBeanComposite;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;

public class YThetaStageComposite extends FieldBeanComposite {
	private ScaleBox theta;
	private ScaleBox y;
	private String yName;
	private String thetaName;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public YThetaStageComposite(Composite parent, int style, final String yName, final String thetaName) {
		super(parent, style);
		setLayout(new GridLayout(2, false));
		
		this.yName = yName;
		this.thetaName = thetaName;

		Label lblY = new Label(this, SWT.NONE);
		lblY.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblY.setText("Y");
		
		y = new ScaleBox(this, SWT.NONE);
		y.setUnit("mm");
		y.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblZ = new Label(this, SWT.NONE);
		lblZ.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblZ.setText("Theta");
		
		theta = new ScaleBox(this, SWT.NONE);
		theta.setUnit("deg");
		theta.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		Label label_3 = new Label(this, SWT.NONE);
		label_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		
				Button btnGetCurrentValues = new Button(this, SWT.NONE);
				btnGetCurrentValues.setToolTipText("Fill the text boxes with the current motor values");
				btnGetCurrentValues.setText("Get current values");
				
						btnGetCurrentValues.addListener(SWT.Selection, new Listener() {
							@Override
							public void handleEvent(Event event) {
								y.setValue(JythonServerFacade.getInstance().evaluateCommand(YThetaStageComposite.this.yName + "()"));
								theta.setValue(JythonServerFacade.getInstance().evaluateCommand(
										YThetaStageComposite.this.thetaName + "()"));
							}
						});
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public FieldComposite getY(){
		return y;
	}

	public FieldComposite getTheta(){
		return theta;
	}

}
