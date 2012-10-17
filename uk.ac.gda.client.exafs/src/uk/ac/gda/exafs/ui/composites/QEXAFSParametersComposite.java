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

import gda.configuration.properties.LocalProperties;
import gda.jscience.physics.quantities.BraggAngle;
import gda.jython.JythonServerFacade;
import gda.util.QuantityFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.NonSI;
import org.jscience.physics.units.Unit;

import uk.ac.gda.beans.exafs.QEXAFSParameters;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.LabelWrapper;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;

/**
 *
 */
public final class QEXAFSParametersComposite extends Composite {

	private ScaleBox initialEnergy;
	private ScaleBox finalEnergy;
	private ScaleBox speed;
	private ScaleBox stepSize;
	private LabelWrapper totalTime;
	private Label numberPoints;
	private Label avgTimePerPoint;

	private FieldComposite shouldValidate;

	private NumberFormat formatter = new DecimalFormat("#0.00000");
	private Length crystal = null;

	public QEXAFSParametersComposite(Composite parent, int style, final QEXAFSParameters provider) {
		super(parent, SWT.NONE);

		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = 15;
		gridLayout.marginHeight = 0;
		setLayout(gridLayout);

		GridData gd1 = new GridData(SWT.NONE, SWT.CENTER, true, true);
		gd1.widthHint = 900;
		this.setLayoutData(gd1);

		Label label = new Label(this, SWT.NONE);
		label.setText("Initial Energy");
		this.initialEnergy = new ScaleBox(this, SWT.NONE);
		((GridData) initialEnergy.getControl().getLayoutData()).widthHint = 150;
		initialEnergy.setUnit("eV");
		new Label(initialEnergy, SWT.NONE);

		label = new Label(this, SWT.NONE);
		label.setText("Final Energy");
		this.finalEnergy = new ScaleBox(this, SWT.NONE);
		((GridData) finalEnergy.getControl().getLayoutData()).widthHint = 150;
		finalEnergy.setUnit("eV");
		new Label(finalEnergy, SWT.NONE);

		if (LocalProperties.get("gda.beamline.name").equals("BL18B")) {
			String dcmCrystal = JythonServerFacade.getInstance().evaluateCommand("dcm_crystal()");
			if (dcmCrystal.equals("Si(111)")) {
				finalEnergy.setMinimum(2050.0);
				finalEnergy.setMaximum(26000.0);
				initialEnergy.setMinimum(2050.0);
				initialEnergy.setMaximum(26000.0);
				crystal = Quantity.valueOf(6.2695, NonSI.ANGSTROM);
			} else if (dcmCrystal.equals("Si(311)")) {
				finalEnergy.setMinimum(4000.0);
				finalEnergy.setMaximum(40000.0);
				initialEnergy.setMinimum(4000.0);
				initialEnergy.setMaximum(40000.0);
				crystal = Quantity.valueOf(3.275, NonSI.ANGSTROM);
			}
		} else {
			Double max = ExafsActivator.getDefault().getPreferenceStore()
					.getDouble(ExafsPreferenceConstants.XAS_MAX_ENERGY);
			if (max == 0)
				max = 40000.;
			finalEnergy.setMinimum(2050.0);
			finalEnergy.setMaximum(22000.0);
			initialEnergy.setMinimum(2050.0);
			initialEnergy.setMaximum(22000.0);
			crystal = Quantity.valueOf(6.2695, NonSI.ANGSTROM);
		}

		label = new Label(this, SWT.NONE);
		label.setText("Speed");
		String minSpeed = "Min = 0.1 mdeg/s";
		String maxSpeed = JythonServerFacade.getInstance().evaluateCommand("bragg_speed()");
		double maxSpeedDouble = Double.parseDouble(maxSpeed)*1000;

		Composite speedComp = new Composite(this, SWT.NONE);
		GridLayout gl_speedComp = new GridLayout(3, false);
		gl_speedComp.marginHeight = 0;
		gl_speedComp.marginWidth = 0;
		speedComp.setLayout(gl_speedComp);
		GridData gd2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd2.widthHint = 490;
		speedComp.setLayoutData(gd2);

		this.speed = new ScaleBox(speedComp, SWT.NONE);
		((GridData) speed.getControl().getLayoutData()).widthHint = 150;
		speed.setUnit("mdeg/s");
		speed.setDecimalPlaces(4);
		speed.setMinimum(0.1);
		speed.setMaximum(maxSpeedDouble);
		
		new Label(speed, SWT.NONE);
		Label minSpeedlabel = new Label(speedComp, SWT.NONE);
		GridData gd_minSpeedlabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_minSpeedlabel.widthHint = 119;
		minSpeedlabel.setLayoutData(gd_minSpeedlabel);
		minSpeedlabel.setText(minSpeed);
		Label maxSpeedlabel = new Label(speedComp, SWT.NONE);
		maxSpeedlabel.setText("Max = " + formatter.format(maxSpeedDouble) + " mdeg/sec");

		label = new Label(this, SWT.NONE);
		label.setText("Step Size");
		this.stepSize = new ScaleBox(this, SWT.NONE);
		((GridData) stepSize.getControl().getLayoutData()).widthHint = 150;
		stepSize.setUnit("eV");
		stepSize.setMinimum(0.0001);
		stepSize.setDecimalPlaces(5);
		
		double initialEnergyVal = provider.getInitialEnergy();
		double finalEnergyVal = provider.getFinalEnergy();
		double rangeEv = finalEnergyVal-initialEnergyVal;
		stepSize.setMaximum(rangeEv);
		
		new Label(stepSize, SWT.NONE);

		label = new Label(this, SWT.NONE);
		label.setText("Scan Time");
		this.totalTime = new LabelWrapper(this, SWT.NONE);
		GridData gd_totalTime = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_totalTime.widthHint = 159;
		totalTime.setLayoutData(gd_totalTime);
		
		label = new Label(this, SWT.NONE);
		label.setText("Number of Points");
		this.numberPoints = new Label(this, SWT.NONE);
		GridData gd_numberPoints = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_numberPoints.widthHint = 544;
		numberPoints.setLayoutData(gd_numberPoints);
		
		label = new Label(this, SWT.NONE);
		label.setText("Avg Time Per Point");
		this.avgTimePerPoint = new Label(this, SWT.NONE);
		GridData gd_avgTimePerPoint = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_avgTimePerPoint.widthHint = 159;
		avgTimePerPoint.setLayoutData(gd_avgTimePerPoint);
		
		initialEnergy.addValueListener(new ValueAdapter("initialEnergyListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				if(initialEnergy.getNumericValue()!=0 && !initialEnergy.getValue().toString().equals(""))
					calculate(provider);
			}
		});

		finalEnergy.addValueListener(new ValueAdapter("finalEnergyListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				if(finalEnergy.getNumericValue()!=0 && !finalEnergy.getValue().toString().equals(""))
					calculate(provider);
			}
		});

		speed.addValueListener(new ValueAdapter("speedListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				if(speed.getNumericValue()!=0 && !speed.getValue().toString().equals(""))
					calculate(provider);
			}
		});

		stepSize.addValueListener(new ValueAdapter("stepSizeListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				if(stepSize.getNumericValue()!=0 && !stepSize.getValue().toString().equals(""))
					calculate(provider);
			}
		});
		calculate(provider);
	}
	
	private void calculate(QEXAFSParameters provider) {
		try {
			BeanUI.uiToBean(this, provider);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		double initialEnergyVal = provider.getInitialEnergy();
		double finalEnergyVal = provider.getFinalEnergy();
		double speedVal = provider.getSpeed();
		double stepSizeVal = provider.getStepSize();
		double rangeEv = finalEnergyVal-initialEnergyVal;
		Unit<?> userUnits = QuantityFactory.createUnitFromString("eV");
		Quantity initialAngleQuantity = QuantityFactory.createFromObject(initialEnergyVal, userUnits);
		Angle initialAngle = BraggAngle.braggAngleOf((Energy) initialAngleQuantity, crystal);
		Quantity finalAngleQuantity = QuantityFactory.createFromObject(finalEnergyVal, userUnits);
		Angle finalAngle = BraggAngle.braggAngleOf((Energy) finalAngleQuantity, crystal);
		
		double range = ((initialAngle.doubleValue()*180)/Math.PI) - ((finalAngle.doubleValue()*180)/Math.PI);
		double time = (range/speedVal)*1000;
		totalTime.setUnit("s");
		if(time >0){
			totalTime.setValue(time);
			totalTime.setText(formatter.format(time)+ " s");
		}
		int numberOfPoints = (int) (rangeEv / stepSizeVal);
		numberPoints.setText(numberOfPoints + "");
		if(numberOfPoints>4000){
			numberPoints.setForeground(new Color(null, 255,0,0));
			numberPoints.setText(numberOfPoints + " (Too many points! Please increase step size or reduce energy range)");
		}
		else
			numberPoints.setForeground(new Color(null, 0,0,0));
		if(time >0&&numberOfPoints>0)
			avgTimePerPoint.setText(formatter.format((time/numberOfPoints)*1000) + " ms");
		
		stepSize.setMaximum(rangeEv);
	}

	public FieldComposite getInitialEnergy() {
		return initialEnergy;
	}

	public FieldComposite getFinalEnergy() {
		return finalEnergy;
	}

	public ScaleBox getSpeed() {
		return speed;
	}

	public ScaleBox getStepSize() {
		return stepSize;
	}

	public FieldComposite getTime() {
		return totalTime;
	}
	
	public FieldComposite getShouldValidate() {
		return shouldValidate;
	}

}
