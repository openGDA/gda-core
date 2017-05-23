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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.eclipse.richbeans.widgets.FieldBeanComposite;
import org.eclipse.richbeans.widgets.FieldComposite;
import org.eclipse.richbeans.widgets.scalebox.NumberBox;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.JythonServerFacade;

public class B18FurnaceComposite extends FieldBeanComposite {
	private final Logger logger = LoggerFactory.getLogger(B18FurnaceComposite.class);
	private ScaleBox temperature;
	private ScaleBox tolerance;
	private ScaleBox time;
	private BooleanWrapper controlFlag;

	/**
	 * Create the composite.
	 *
	 * @param parent
	 * @param style
	 */
	public B18FurnaceComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		final Logger logger = LoggerFactory.getLogger(XYThetaStageComposite.class);
		Label lblTemperature = new Label(this, SWT.NONE);
		lblTemperature.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTemperature.setText("Temperature");

		temperature = new ScaleBox(this, SWT.NONE);
		temperature.setUnit("C");
		temperature.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		tolerance.setDecimalPlaces(0); //If setDecimalPlaces to 0, getValue() returns an Integer which *fails to map from ui to bean* !
		// Instead create new DecimalFormat object with 0 decimal places, and pass it in.
		// This sets the format only, not the number of decimal places, which means that :
		//  1) Gui accepts only integer input.
		//  2) The ScaleBox internally thinks it has 2 decimal places (zero for decimal part), so getValue() returns a Double which maps correctly to bean.
		temperature.setNumberFormat(getDecimalFormat(0)); //sets the format only, and *not* the number of decimal places

		try {
			setMotorLimits("eurotherm", temperature);

		} catch (Exception e) {
			logger.warn("exception while fetching hardware limits: " + e.getMessage(), e);
		}

		Label lblTolerance = new Label(this, SWT.NONE);
		lblTolerance.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTolerance.setText("Tolerance");

		tolerance = new ScaleBox(this, SWT.NONE);
		tolerance.setUnit("C");
		tolerance.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tolerance.setNumberFormat(getDecimalFormat(0));

		Label lblTime = new Label(this, SWT.NONE);
		lblTime.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTime.setText("Wait for stability time");

		time = new ScaleBox(this, SWT.NONE);
		time.setUnit("s");
		time.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		this.controlFlag = new BooleanWrapper(this, SWT.NONE);
		controlFlag.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		controlFlag.setText("Only read, no control");
	}

	/**
	 * Create new DecimalFormat for specified number of decimal places
	 * (copied from NumberBox constructor, {@link NumberBox#NumberBox(Composite, int)})
	 * @param decimalPlaces
	 * @return DecimalFormat
	 */
	public DecimalFormat getDecimalFormat(int decimalPlaces) {
		DecimalFormat numberFormat = new DecimalFormat();
		numberFormat.setMaximumFractionDigits(decimalPlaces);
		numberFormat.setMinimumFractionDigits(decimalPlaces);
		numberFormat.setGroupingUsed(false);
		DecimalFormatSymbols dfs = ((DecimalFormat) numberFormat).getDecimalFormatSymbols();
		dfs.setInfinity(String.valueOf(Double.POSITIVE_INFINITY));
		((DecimalFormat) numberFormat).setDecimalFormatSymbols(dfs);
		return numberFormat;
	}

	public void setMotorLimits(String motorName, ScaleBox box) throws Exception {
		// Check to see if furnace is initialised before trying to get limits
		// - to avoid slow gui due to timeout from missing Epics PVs. imh 24/5/2016
		String confString = JythonServerFacade.getInstance().evaluateCommand(motorName + ".isConfigured()");
		if (!confString.equals("True")) {
			logger.warn("Unable to get limits for " + motorName + " ");
			return;
		}

		String lowerLimit = JythonServerFacade.getInstance().evaluateCommand(motorName + ".getLowerLimit()");
		String upperLimit = JythonServerFacade.getInstance().evaluateCommand(motorName + ".getUpperLimit()");
		if (!lowerLimit.equals("None") && !lowerLimit.isEmpty())
			box.setMinimum(Double.parseDouble(lowerLimit));
		if (!upperLimit.equals("None") && !upperLimit.isEmpty())
			box.setMaximum(Double.parseDouble(upperLimit));
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public FieldComposite getTemperature() {
		return temperature;
	}

	public FieldComposite getTolerance() {
		return tolerance;
	}

	public FieldComposite getTime() {
		return time;
	}

	public FieldComposite getControlFlag() {
		return controlFlag;
	}

}
