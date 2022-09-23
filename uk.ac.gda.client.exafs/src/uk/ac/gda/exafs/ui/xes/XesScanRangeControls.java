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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.richbeans.api.widget.IFieldWidget;
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

	/**
	 * Simple class to contain all the widgets for the setting the scan parameters
	 */
	private class EnergyWidgets {
		ScaleBoxAndFixedExpression initialEnergy;
		ScaleBoxAndFixedExpression finalEnergy;
		ScaleBox stepSize;
		ScaleBox integrationTime;

		List<FieldComposite> getWidgets() {
			return Arrays.asList(initialEnergy, finalEnergy, stepSize, integrationTime);
		}
	}

	private EnergyWidgets widgetsRow1;
	private EnergyWidgets widgetsRow2;

	private String rowLabelPattern = "Energy %s";
	private String row1Suffix = "(upper)";
	private String row2Suffix = "(lower)";

	private Group mainGroup;

	private CrystalMaterial crystalMaterial;
	private int[] crystalCutValues;

	private double minStepSize = 0.01;
	private double maxStepSize = 1000;
	private double minIntegrationTime = 0.01;
	private double maxIntegrationTime = 30.0;
	private RadioWrapper loopChoice;
	private int scanType;
	private boolean showRow2Controls = false;

	public Composite getMainComposite() {
		return mainGroup;
	}

	@Override
	public void createControls(Composite parent) {
		mainGroup = new Group(parent, SWT.NONE);
		mainGroup.setText("XES Scan");

		GridDataFactory gdFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).hint(50,  SWT.DEFAULT);

		gdFactory.hint(500, SWT.DEFAULT).applyTo(mainGroup);
		mainGroup.setLayout(new GridLayout(1, false));

		loopChoice = new RadioWrapper(mainGroup, SWT.NONE, XesScanParameters.LOOPOPTIONS);
		loopChoice.setValue(XesScanParameters.LOOPOPTIONS[0]);
		loopChoice.setText("Loop order");
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gridData.widthHint = 500;
		loopChoice.setLayoutData(gridData);

		widgetsRow1 = createEnergySettingWidgets(mainGroup, String.format(rowLabelPattern, row1Suffix));
		widgetsRow2 = createEnergySettingWidgets(mainGroup, String.format(rowLabelPattern, row2Suffix));

		widgetsRow1.getWidgets().forEach(gdFactory::applyTo);
		widgetsRow2.getWidgets().forEach(gdFactory::applyTo);

		setupFieldWidgets(getEnergyWidgets());
		setupFieldWidget(loopChoice);

		parent.addDisposeListener(l -> dispose());
	}

	public void showRowControls(boolean showRow1, boolean showRow2) {
		setVisible(widgetsRow1, showRow1);
		setVisible(widgetsRow2, showRow2);
	}

	public void enableRowControls(boolean enableRow1, boolean enableRow2) {
		widgetsRow1.getWidgets().forEach(w -> w.setEnabled(enableRow1));
		widgetsRow2.getWidgets().forEach(w -> w.setEnabled(enableRow2));
	}

	public void showRowControls(boolean showRows) {
		setVisible(widgetsRow1, showRows);
		setVisible(widgetsRow2, showRows);
	}

	private void setVisible(EnergyWidgets widgets, boolean show) {
		widgets.getWidgets().forEach(w -> setVisible(w, show));
		setVisible(widgets.initialEnergy.getParent(), show);
	}

	private EnergyWidgets createEnergySettingWidgets(Composite parent, String labelText) {

		Group container = new Group(parent, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		container.setText(labelText);

		Label lblInitialEnergy = new Label(container, SWT.NONE);
		lblInitialEnergy.setText("Initial Energy");
		var initialEnergy = new ScaleBoxAndFixedExpression(container, SWT.NONE);
		initialEnergy.setPrefix("   θ");
		initialEnergy.setLabelUnit("°");
		initialEnergy.setUnit("eV");
		initialEnergy.setExpressionLabelTooltip("65° < θ < 85°");

		Label label = new Label(container, SWT.NONE);
		label.setText("Final Energy");
		var finalEnergy = new ScaleBoxAndFixedExpression(container, SWT.NONE);
		finalEnergy.setPrefix("   θ");
		finalEnergy.setLabelUnit("°");
		finalEnergy.setUnit("eV");
		finalEnergy.setExpressionLabelTooltip("65° < θ < 85°");

		label = new Label(container, SWT.NONE);
		label.setText("Step Size");
		var stepSize = new ScaleBox(container, SWT.NONE);
		stepSize.setUnit("eV");

		label = new Label(container, SWT.NONE);
		label.setText("Integration Time");
		var integrationTime = new ScaleBox(container, SWT.NONE);
		integrationTime.setUnit("s");

		EnergyWidgets widgets = new EnergyWidgets();
		widgets.initialEnergy = initialEnergy;
		widgets.finalEnergy = finalEnergy;
		widgets.stepSize = stepSize;
		widgets.integrationTime = integrationTime;

		// Add listeners to update the theta values when energy changes
		initialEnergy.addValueListener(e -> updateProperties(widgets));
		finalEnergy.addValueListener(e -> updateProperties(widgets));
		return widgets;
	}

	public void dispose() {
		getEnergyWidgets().forEach(IFieldWidget::dispose);
		loopChoice.dispose();
		deleteIObservers();
	}

	private List<IFieldWidget> getEnergyWidgets() {
		List<IFieldWidget> widgets = new ArrayList<>();
		widgets.addAll(widgetsRow1.getWidgets());
		widgets.addAll(widgetsRow2.getWidgets());
		return widgets;
	}

	/**
	 * Update the theta and min, max allowed energy values for the current
	 * energy values and crystal cut, material
	 * values.
	 *
	 */
	private void updateProperties(EnergyWidgets widgets) {
		CrystalMaterial material = getCrystalMaterial();
		double minXESEnergy= XesUtils.getFluoEnergy(XesUtils.MAX_THETA, material, getCrystalCutValues());
		double maxXESEnergy= XesUtils.getFluoEnergy(XesUtils.MIN_THETA, material, getCrystalCutValues());


		// Upper limit for initial energy is lowest of max allowed Xes energy and final energy
		double maxAllowedEnergy = Math.min(widgets.finalEnergy.getNumericValue(), maxXESEnergy);
		setMinMax(widgets.initialEnergy, minXESEnergy, maxAllowedEnergy);

		// Lower limit for final energy largest of initial energy and min allowed Xes energy
		double minAllowedEnergy = Math.max(widgets.initialEnergy.getNumericValue(), minXESEnergy);
		setMinMax(widgets.finalEnergy, minAllowedEnergy, maxXESEnergy);

		setMinMax(widgets.stepSize, minStepSize, maxStepSize);
		setMinMax(widgets.integrationTime, minIntegrationTime, maxIntegrationTime);

		updateTheta(widgets.initialEnergy);
		updateTheta(widgets.finalEnergy);
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
		return widgetsRow1.initialEnergy;
	}

	public ScaleBoxAndFixedExpression getFinalEnergy() {
		return widgetsRow1.finalEnergy;
	}

	public ScaleBox getIntegrationTime() {
		return widgetsRow1.integrationTime;
	}


	public ScaleBox getStepSize() {
		return widgetsRow1.stepSize;
	}

	public RadioWrapper getLoopChoice() {
		return loopChoice;
	}

	public void setShowLoopChoice(boolean show) {
		setVisible(loopChoice, show);
	}

	public void showMain(boolean show) {
		setVisible(mainGroup, show);
	}

	public void setRow1Suffix(String row1Suffix) {
		this.row1Suffix = row1Suffix;
	}

	public void setRow2Suffix(String row2Suffix) {
		this.row2Suffix = row2Suffix;
	}

	public void setScanType(int scanTypeVal) {
		this.scanType = scanTypeVal;
	}

	public void setupWidgetsForScanType() {
		boolean isTwoD = scanType == XesScanParameters.SCAN_XES_SCAN_MONO;

		setShowLoopChoice(isTwoD);
		showRowControls(true, showRow2Controls && !isTwoD);
		enableRowControls(true, showRow2Controls);
	}

	public void setShowRow2Controls(boolean showRow2Controls) {
		this.showRow2Controls = showRow2Controls;
	}
}
