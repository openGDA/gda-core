/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.xes;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.richbeans.widgets.FieldComposite;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.scalebox.ScaleBoxAndFixedExpression;
import org.eclipse.richbeans.widgets.wrappers.RadioWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import gda.device.DeviceException;
import gda.exafs.xes.XesUtils;
import gda.util.CrystalParameters.CrystalMaterial;
import uk.ac.gda.beans.exafs.XesScanParameters;

public class XesScanRangeControls extends XesControlsBuilder {


	private ScaleBoxAndFixedExpression initialEnergy;
	private ScaleBoxAndFixedExpression finalEnergy;
	private ScaleBox stepSize;
	private ScaleBox integrationTime;

	private Group mainGroup;

	private CrystalMaterial crystalMaterial;
	private int[] crystalCutValues;

	private double minStepSize = 0.01;
	private double maxStepSize = 1000;
	private double minIntegrationTime = 0.01;
	private double maxIntegrationTime = 30.0;
	private RadioWrapper loopChoice;

	public Composite getMainComposite() {
		return mainGroup;
	}

	@Override
	public void createControls(Composite parent) {
		mainGroup = new Group(parent, SWT.NONE);
		mainGroup.setText("XES Scan");

		GridDataFactory gdFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).hint(50,  SWT.DEFAULT);

		gdFactory.hint(500, SWT.DEFAULT).applyTo(mainGroup);

		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginRight = 5;
		gridLayout.marginBottom = 5;
		gridLayout.marginLeft = 5;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		mainGroup.setLayout(gridLayout);

		loopChoice = new RadioWrapper(mainGroup, SWT.NONE, XesScanParameters.LOOPOPTIONS);
		loopChoice.setValue(XesScanParameters.LOOPOPTIONS[0]);
		loopChoice.setText("Loop order");
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gridData.widthHint = 500;
		loopChoice.setLayoutData(gridData);

		Label lblInitialEnergy = new Label(mainGroup, SWT.NONE);
		lblInitialEnergy.setText("Initial Energy");
		initialEnergy = new ScaleBoxAndFixedExpression(mainGroup, SWT.NONE);
		initialEnergy.setPrefix("   θ");
		initialEnergy.setLabelUnit("°");
		initialEnergy.setUnit("eV");
		initialEnergy.setExpressionLabelTooltip("65° < θ < 85°");
		gdFactory.applyTo(initialEnergy);

		Label label = new Label(mainGroup, SWT.NONE);
		label.setText("Final Energy");
		finalEnergy = new ScaleBoxAndFixedExpression(mainGroup, SWT.NONE);
		finalEnergy.setPrefix("   θ");
		finalEnergy.setLabelUnit("°");
		finalEnergy.setUnit("eV");
		finalEnergy.setExpressionLabelTooltip("65° < θ < 85°");
		gdFactory.applyTo(finalEnergy);

		label = new Label(mainGroup, SWT.NONE);
		label.setText("Step Size");
		stepSize = new ScaleBox(mainGroup, SWT.NONE);
		stepSize.setUnit("eV");
		gdFactory.applyTo(stepSize);

		label = new Label(mainGroup, SWT.NONE);
		label.setText("Integration Time");
		integrationTime = new ScaleBox(mainGroup, SWT.NONE);
		integrationTime.setUnit("s");
		gdFactory.applyTo(integrationTime);

		// Add listeners to update the theta values when energy changes
		initialEnergy.addValueListener(e -> updateProperties());
		finalEnergy.addValueListener(e -> updateProperties());

		parent.addDisposeListener(l -> dispose());
	}

	public void dispose() {
		getWidgets().forEach(Composite::dispose);
		deleteIObservers();
	}

	private List<Composite> getWidgets() {
		return List.of(mainGroup, initialEnergy, finalEnergy, stepSize, integrationTime);
	}

	public void fireValueListeners() {
		getWidgets().stream()
			.filter(FieldComposite.class::isInstance)
			.map(FieldComposite.class::cast)
			.forEach(FieldComposite::fireValueListeners);
	}

	/**
	 * Update the theta and min, max allowed energy values for the current
	 * energy values and crystal cut, material
	 * values.
	 *
	 */
	private void updateProperties() {
		CrystalMaterial material = getCrystalMaterial();
		double minXESEnergy= XesUtils.getFluoEnergy(XesUtils.MAX_THETA, material, getCrystalCutValues());
		double maxXESEnergy= XesUtils.getFluoEnergy(XesUtils.MIN_THETA, material, getCrystalCutValues());


		// Upper limit for initial energy is lowest of max allowed Xes energy and final energy
		double maxAllowedEnergy = Math.min(finalEnergy.getNumericValue(), maxXESEnergy);
		setMinMax(initialEnergy, minXESEnergy, maxAllowedEnergy);

		// Lower limit for final energy largest of initial energy and min allowed Xes energy
		double minAllowedEnergy = Math.max(initialEnergy.getNumericValue(), minXESEnergy);
		setMinMax(finalEnergy, minAllowedEnergy, maxXESEnergy);

		setMinMax(stepSize, minStepSize, maxStepSize);
		setMinMax(integrationTime, minIntegrationTime, maxIntegrationTime);

		updateTheta(initialEnergy);
		updateTheta(finalEnergy);
	}

	/**
	 * Set the min and max allowed values for the ScaleBox
	 * @param widget
	 * @param min
	 * @param max
	 */
	private void setMinMax(ScaleBox widget, double min, double max) {
		widget.setMinimum(min);
		widget.setMaximum(max);
	}

	/**
	 *  Set the theta (angle) equivalent for the ScaleBox for the currently set energy
	 * @param energyBox
	 * @throws DeviceException
	 */
	private void updateTheta(ScaleBoxAndFixedExpression energyBox) {
		double energyValue = energyBox.getNumericValue();
		double theta = XesUtils.getBragg(energyValue, getCrystalMaterial(), getCrystalCutValues());
		energyBox.setFixedExpressionValue(theta);
	}

	public CrystalMaterial getCrystalMaterial() {
		return crystalMaterial;
	}

	public void setCrystalMaterial(CrystalMaterial crystalMaterial) {
		this.crystalMaterial = crystalMaterial;
	}

	public int[] getCrystalCutValues() {
		return crystalCutValues;
	}

	public void setCrystalCutValues(int[] crystalCutValues) {
		this.crystalCutValues = crystalCutValues;
	}

	public ScaleBoxAndFixedExpression getInitialEnergy() {
		return initialEnergy;
	}

	public ScaleBoxAndFixedExpression getFinalEnergy() {
		return finalEnergy;
	}

	public ScaleBox getIntegrationTime() {
		return integrationTime;
	}


	public ScaleBox getStepSize() {
		return stepSize;
	}

	public RadioWrapper getLoopChoice() {
		return loopChoice;
	}
}
