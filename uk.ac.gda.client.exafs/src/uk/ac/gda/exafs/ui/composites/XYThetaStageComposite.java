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

package uk.ac.gda.exafs.ui.composites;

import org.dawnsci.common.richbeans.components.FieldBeanComposite;
import org.dawnsci.common.richbeans.components.FieldComposite;
import org.dawnsci.common.richbeans.components.scalebox.ScaleBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.JythonServerFacade;

public class XYThetaStageComposite extends FieldBeanComposite {

	private static final Logger logger = LoggerFactory.getLogger(XYThetaStageComposite.class);

	private ScaleBox theta;
	private ScaleBox y;
	private ScaleBox x;
	private String xName;
	private String yName;
	private String thetaName;

	public XYThetaStageComposite(Composite parent, int style, String xName, String yName, String thetaName) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		this.xName = xName;
		this.yName = yName;
		this.thetaName = thetaName;

		Label lblX = new Label(this, SWT.NONE);
		lblX.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblX.setText("X");

		x = new ScaleBox(this, SWT.NONE);
		x.setUnit("mm");
		x.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		x.setDecimalPlaces(3);
		Label lblY = new Label(this, SWT.NONE);
		lblY.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblY.setText("Y");

		y = new ScaleBox(this, SWT.NONE);
		y.setUnit("mm");
		y.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		y.setDecimalPlaces(3);
		Label lblZ = new Label(this, SWT.NONE);
		lblZ.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblZ.setText("Theta");

		theta = new ScaleBox(this, SWT.NONE);
		theta.setUnit("deg");
		theta.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		theta.setDecimalPlaces(3);
		Label label_3 = new Label(this, SWT.NONE);
		label_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		Button btnGetCurrentValues = new Button(this, SWT.NONE);
		btnGetCurrentValues.setToolTipText("Fill the text boxes with the current motor values");
		btnGetCurrentValues.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnGetCurrentValues.setText("Get current values");

		try {
			setMotorLimits("sam2x", x);
			setMotorLimits("sam2y", y);
			setMotorLimits("sam2rot", theta);

		} catch (Exception e) {
			logger.warn("exception while fetching hardware limits: " + e.getMessage(), e);
		}

		btnGetCurrentValues.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					String xPosition = JythonServerFacade.getInstance().evaluateCommand(XYThetaStageComposite.this.xName + "()");
					x.setValue(String.format("%.4f", Double.parseDouble(xPosition)));
				} catch (Exception e) {
					logger.error("Exception retrieving current x motor position", e);
				}
				try {
					String yPosition = JythonServerFacade.getInstance().evaluateCommand(XYThetaStageComposite.this.yName + "()");
					y.setValue(String.format("%.4f", Double.parseDouble(yPosition)));
				} catch (Exception e) {
					logger.error("Exception retrieving current y motor position", e);
				}
				try {
					String thetaPosition = JythonServerFacade.getInstance().evaluateCommand(XYThetaStageComposite.this.thetaName + "()");
					theta.setValue(String.format("%.4f", Double.parseDouble(thetaPosition)));
				} catch (Exception e) {
					logger.error("Exception retrieving current theta motor position", e);
				}
			}
		});
	}

	public void setMotorLimits(String motorName, ScaleBox box) throws Exception {
		String lowerLimit = JythonServerFacade.getInstance().evaluateCommand(motorName + ".getLowerMotorLimit()");
		String upperLimit = JythonServerFacade.getInstance().evaluateCommand(motorName + ".getUpperMotorLimit()");
		if (!lowerLimit.equals("None") && !lowerLimit.isEmpty())
			box.setMinimum(Double.parseDouble(lowerLimit));
		if (!upperLimit.equals("None") && !upperLimit.isEmpty())
			box.setMaximum(Double.parseDouble(upperLimit));
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public FieldComposite getX() {
		return x;
	}

	public FieldComposite getY() {
		return y;
	}

	public FieldComposite getTheta() {
		return theta;
	}

}
