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
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
import org.eclipse.richbeans.widgets.wrappers.RadioWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.exafs.xes.IXesEnergyScannable;
import gda.exafs.xes.XesUtils;
import uk.ac.gda.beans.exafs.XesScanParameters;

public class XesScanRangeControls extends XesControlsBuilder {
	private static final Logger logger = LoggerFactory.getLogger(XesScanRangeControls.class);

	/**
	 * Simple class to contain all the widgets for the setting the scan parameters
	 */
	private class EnergyWidgets {
		ScaleBoxAndFixedExpression initialEnergy;
		ScaleBoxAndFixedExpression finalEnergy;
		ScaleBox stepSize;
		ScaleBox integrationTime;
		IXesEnergyScannable xesEnergyScannable;

		List<FieldComposite> getWidgets() {
			return Arrays.asList(initialEnergy, finalEnergy, stepSize, integrationTime);
		}

		public void setVisible(boolean show) {
			var widgets = getWidgets();
			widgets.forEach(w -> XesScanRangeControls.this.setVisible(w, show));
			XesScanRangeControls.this.setVisible(initialEnergy.getParent(), show);
		}

		/**
		 * Calculate number of steps from energy range and step size
		 * @return number of steps
		 */
		public int getNumberOfSteps() {
			double range = Math.abs(finalEnergy.getNumericValue() - initialEnergy.getNumericValue());
			return (int) Math.floor(range/stepSize.getNumericValue());
		}

		/**
		 * Calculate final energy from intial energy, step size and number of steps
		 * @param numSteps
		 * @return final energy
		 */
		public double getFinalEnergy(int numSteps) {
			return initialEnergy.getNumericValue() + stepSize.getNumericValue() * numSteps;
		}

		/**
		 * Set the expression tooltip for the initial and final energy to show Bragg angle range
		 * derived from current energy range of the xesEnergyScannable.
		 */
		public void setupBraggRangeToolTip() {
			double[] angleRange = getAngleRange();
			String label = String.format("%.0f < θ < %.0f°", angleRange[0], angleRange[1]);
			initialEnergy.setExpressionLabelTooltip(label);
			finalEnergy.setExpressionLabelTooltip(label);
		}

		/**
		 * Get the Bragg angle range from the xesEnergyScannable
		 * by converting energy range back to Bragg angle.
		 *
		 * @return
		 */
		private double[] getAngleRange() {
			double minBragg = XesUtils.MIN_THETA;
			double maxBragg = XesUtils.MAX_THETA;
			try {
				double[] energyRange = xesEnergyScannable.getEnergyRange();
				maxBragg = XesUtils.getBragg(energyRange[0], xesEnergyScannable.getMaterialType(), xesEnergyScannable.getCrystalCut());
				minBragg= XesUtils.getBragg(energyRange[1], xesEnergyScannable.getMaterialType(), xesEnergyScannable.getCrystalCut());
			} catch (DeviceException e) {
				logger.error("Problem getting allowed Bragg angle range from "+xesEnergyScannable.getName(), e);
			}

			return new double[] {minBragg, maxBragg};
		}
	}

	private EnergyWidgets widgetsRow1;
	private EnergyWidgets widgetsRow2;
	private List<IXesEnergyScannable> xesScannables;

	private String rowLabelPattern = "Energy %s";
	private String row1Suffix = "(upper)";
	private String row2Suffix = "(lower)";

	private Group mainGroup;

	private double minStepSize = 0.01;
	private double maxStepSize = 1000;
	private double minIntegrationTime = 0.01;
	private double maxIntegrationTime = 30.0;

	private BooleanWrapper energyTransferButton;
	private ScaleBox monoEnergyWidget;
	private ScaleBox monoInitialEnergyWidget;
	private ScaleBox monoFinalEnergyWidget;

	private RadioWrapper loopChoice;
	private int scanType;
	private boolean showRow2Controls = false;

	/** Set to true to compute finalEnergy in row2 from initial energy,
	 * step size and number of steps from row1 values.
	 */
	private boolean computeFinalEnergy = true;

	private GridDataFactory gdFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);

	public Composite getMainComposite() {
		return mainGroup;
	}

	@Override
	public void createControls(Composite parent) {
		mainGroup = new Group(parent, SWT.NONE);
		mainGroup.setText("XES Scan");

		gdFactory.applyTo(mainGroup);
		mainGroup.setLayout(new GridLayout(1, false));

		energyTransferButton = new BooleanWrapper(mainGroup, SWT.CHECK);
		energyTransferButton.setText("Use 'energy transfer' for XES energies");
		energyTransferButton.setToolTipText("When selected : XES energy = Mono energy - Transfer energy");

		loopChoice = new RadioWrapper(mainGroup, SWT.NONE, XesScanParameters.LOOPOPTIONS);
		loopChoice.setValue(XesScanParameters.LOOPOPTIONS[0]);
		loopChoice.setText("Loop order");
		gdFactory.applyTo(loopChoice);

		widgetsRow1 = createEnergySettingWidgets(mainGroup, String.format(rowLabelPattern, row1Suffix));
		widgetsRow1.xesEnergyScannable = xesScannables.get(0);
		widgetsRow1.setupBraggRangeToolTip();

		widgetsRow2 = createEnergySettingWidgets(mainGroup, String.format(rowLabelPattern, row2Suffix));
		if (xesScannables.size() > 1) {
			widgetsRow2.xesEnergyScannable = xesScannables.get(1);
			widgetsRow2.setupBraggRangeToolTip();
		}
		widgetsRow1.getWidgets().forEach(gdFactory::applyTo);
		widgetsRow2.getWidgets().forEach(gdFactory::applyTo);

		setupFieldWidgets(getEnergyWidgets());
		setupFieldWidget(loopChoice);
		setupFieldWidget(energyTransferButton);

		// update the
		energyTransferButton.addValueListener(l -> updateEnergyWidgetRange());

		getEnergyWidgets().forEach(w -> w.addValueListener(event -> updateFinalEnergy()));
		parent.addDisposeListener(l -> dispose());
	}

	public void setRowScannables(List<IXesEnergyScannable> xesScannables) {
		this.xesScannables = new ArrayList<>(xesScannables);
	}

	public void showRowControls(boolean showRow1, boolean showRow2) {
		widgetsRow1.setVisible(showRow1);
		widgetsRow2.setVisible(showRow2);
	}

	public void enableRowControls(boolean enableRow1, boolean enableRow2) {
		widgetsRow1.getWidgets().forEach(w -> w.setEnabled(enableRow1));
		widgetsRow2.getWidgets().forEach(w -> w.setEnabled(enableRow2));
		updateFinalEnergy();
	}

	private EnergyWidgets createEnergySettingWidgets(Composite parent, String labelText) {

		Group container = new Group(parent, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		container.setText(labelText);
		gdFactory.applyTo(container);

		Label lblInitialEnergy = new Label(container, SWT.NONE);
		lblInitialEnergy.setText("Initial Energy");
		var initialEnergy = new ScaleBoxAndFixedExpression(container, SWT.NONE);
		initialEnergy.setPrefix("   θ");
		initialEnergy.setLabelUnit("°");
		initialEnergy.setUnit("eV");

		Label label = new Label(container, SWT.NONE);
		label.setText("Final Energy");
		var finalEnergy = new ScaleBoxAndFixedExpression(container, SWT.NONE);
		finalEnergy.setPrefix("   θ");
		finalEnergy.setLabelUnit("°");
		finalEnergy.setUnit("eV");

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
		initialEnergy.addValueListener(e -> updateEnergyWidgetRange(widgets));
		finalEnergy.addValueListener(e -> updateEnergyWidgetRange(widgets));
		return widgets;
	}

	/**
	 * Update the value in the row2 'final energy' text box (if {@link #computeFinalEnergy} == true)
	 * Calculates number of steps from row1 parameters;
	 * row2 'final energy' is given by row2 'initial energy', step size and number of row1 steps.
	 */
	private void updateFinalEnergy() {
		if (!computeFinalEnergy) {
			return;
		}
		// Set the final energy of row2 using number of steps from the row1 parameters
		int numSteps = widgetsRow1.getNumberOfSteps();
		double finalEnergy = widgetsRow2.getFinalEnergy(numSteps);
		widgetsRow2.finalEnergy.setEditable(false);
		widgetsRow2.integrationTime.setEditable(false);
		widgetsRow2.integrationTime.setValue(widgetsRow1.integrationTime.getNumericValue());
		widgetsRow2.finalEnergy.setValue(finalEnergy);
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

	private boolean isEnergyTransfer() {
		return Boolean.TRUE.equals(energyTransferButton.getValue());
	}

	public void updateEnergyWidgetRange() {
		updateEnergyWidgetRange(widgetsRow1);
		updateEnergyWidgetRange(widgetsRow2);
	}

	private void updateEnergyWidgetRange(EnergyWidgets widgets) {
		try {
			List<Double> allowedEnergyRange = Arrays.stream(widgets.xesEnergyScannable.getEnergyRange()).boxed().toList();
			if (isEnergyTransfer()) {
				if (scanType == XesScanParameters.SCAN_XES_FIXED_MONO) {
					allowedEnergyRange = XesUtils.convertToEnergyTransfer(allowedEnergyRange, monoEnergyWidget.getNumericValue());
				} else if (scanType == XesScanParameters.SCAN_XES_SCAN_MONO) {
					// Convert energy range to energy transfer :
					// (Energy transfer range has larger (+ve) value first, smaller (-ve) value 2nd.)
					List<Double> rangeInitialMono = XesUtils.convertToEnergyTransfer(allowedEnergyRange,  monoInitialEnergyWidget.getNumericValue());
					List<Double> rangeFinalMono = XesUtils.convertToEnergyTransfer(allowedEnergyRange,  monoFinalEnergyWidget.getNumericValue());

					// Work out the range of values allowed for energy transfer:

					// smallest +ve value is upper limit
					double upperLimit = Math.min(rangeInitialMono.get(0), rangeFinalMono.get(0));

					// largest (least -ve) value is lower limit
					double lowerLimit = Math.max(rangeInitialMono.get(1), rangeFinalMono.get(1));

					allowedEnergyRange = List.of(lowerLimit, upperLimit);
				}
			}
			updateEnergyWidgetRange(widgets, allowedEnergyRange);
		} catch(DeviceException e) {
			logger.warn("Problem updating angle and energy ranges in XesScanRangeControls for {}", widgets.xesEnergyScannable.getName(), e);
		}

	}

	/**
	 * Update the theta and min, max allowed energy values for the current
	 * energy values and crystal cut, material
	 * values.
	 *
	 */
	private void updateEnergyWidgetRange(EnergyWidgets widgets, List<Double> allowedEnergyRange) {
		IXesEnergyScannable xesEnergyScannable = widgets.xesEnergyScannable;
		if (xesEnergyScannable == null) {
			return;
		}
		try {
			setMinMax(widgets.initialEnergy, allowedEnergyRange.get(0), allowedEnergyRange.get(1));
			setMinMax(widgets.finalEnergy, allowedEnergyRange.get(0), allowedEnergyRange.get(1));

			double minAllowedStepSize = minStepSize;
			// set lower limit for row2 widgets to allow -ve step sizes
			if (widgets == widgetsRow2) {
				minAllowedStepSize = -maxStepSize;
			}

			setMinMax(widgets.stepSize, minAllowedStepSize, maxStepSize);
			setMinMax(widgets.integrationTime, minIntegrationTime, maxIntegrationTime);

			updateTheta(widgets.initialEnergy, xesEnergyScannable);
			updateTheta(widgets.finalEnergy, xesEnergyScannable);
		} catch(DeviceException e) {
			logger.warn("Problem updating angle and energy ranges in XesScanRangeControls for {}", xesEnergyScannable.getName(), e);
		}
	}

	/**
	 * Set the min and max allowed values for the ScaleBox
	 * @param widget
	 * @param min
	 * @param max
	 */
	private void setMinMax(ScaleBox widget, double min, double max) {
		widget.setMinimum(Math.min(min, max));
		widget.setMaximum(Math.max(min, max));
	}

	/**
	 *  Set the theta (angle) equivalent for the ScaleBox for the currently set energy
	 * @param energyBox
	 * @throws DeviceException
	 */
	private void updateTheta(ScaleBoxAndFixedExpression energyBox, IXesEnergyScannable scn) throws DeviceException {
		double energyValue = energyBox.getNumericValue();

		//null tooltip string clears the XES energy from tooltip (use null to avoid adding an empty line)
		String xesEnergyToolTip = null;

		// Generate tooltip string to show XES energy value(s) for energy transfer value
		if (isEnergyTransfer()) {
			// convert energy transfer value to XES energy
			if (scanType == XesScanParameters.SCAN_XES_FIXED_MONO) {
				energyValue = XesUtils.convertFromEnergyTransfer(energyValue, monoEnergyWidget.getNumericValue());
				xesEnergyToolTip = String.format("XES energy : %.2f eV",energyValue);
			} else if (scanType == XesScanParameters.SCAN_XES_SCAN_MONO) {
				// work out the range of XES energies from initial and final mono energies
				double energyInitialMono = XesUtils.convertFromEnergyTransfer(energyValue, monoInitialEnergyWidget.getNumericValue());
				double energyFinalMono = XesUtils.convertFromEnergyTransfer(energyValue, monoFinalEnergyWidget.getNumericValue());
				// use XES energy for initial mono energy
				energyValue = energyInitialMono;
				xesEnergyToolTip = String.format("XES energies : %.2f eV ... %.2f eV", energyInitialMono, energyFinalMono);
			}

			// Add allowed XES energy range to tooltip string
			double[] xesEnergyRange = scn.getEnergyRange();
			String xesRangeString = String.format("Allowed XES energy range : %.2f eV ... %.2f eV",xesEnergyRange[0], xesEnergyRange[1]);
			xesEnergyToolTip += "\n"+xesRangeString;
		}

		// calculate Bragg angle from energy value
		double theta = XesUtils.getBragg(energyValue, scn.getMaterialType(), scn.getCrystalCut());
		energyBox.setFixedExpressionValue(theta);
		energyBox.setTooltipOveride(xesEnergyToolTip);
	}

	private EnergyWidgets getWidgetsForRow(int row) {
		return row == 0 ? widgetsRow1 : widgetsRow2;
	}

	public ScaleBoxAndFixedExpression getInitialEnergy(int row) {
		return getWidgetsForRow(row).initialEnergy;
	}

	public double getXesInitialTheta(int row) throws DeviceException {
		EnergyWidgets w = getWidgetsForRow(row);
		return XesUtils.getBragg(w.initialEnergy.getNumericValue(), w.xesEnergyScannable.getMaterialType(), w.xesEnergyScannable.getCrystalCut());
	}

	public ScaleBoxAndFixedExpression getFinalEnergy(int row) {
		return getWidgetsForRow(row).finalEnergy;
	}

	public ScaleBox getIntegrationTime(int row) {
		return getWidgetsForRow(row).integrationTime;
	}

	public ScaleBox getStepSize(int row) {
		return getWidgetsForRow(row).stepSize;
	}

	public BooleanWrapper getUseEnergyTransfer() {
		return energyTransferButton;
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
		showRowControls(true, showRow2Controls);
	}

	public void setShowRow2Controls(boolean showRow2Controls) {
		this.showRow2Controls = showRow2Controls;
	}

	public boolean isComputeFinalEnergy() {
		return computeFinalEnergy;
	}

	/**
	 * Set to true to compute final row2 energy using number of steps from row1 settings,
	 * initial row2 energy and row2 step size.
	 *
	 * @param computeFinalEnergy
	 */
	public void setComputeFinalEnergy(boolean computeFinalEnergy) {
		this.computeFinalEnergy = computeFinalEnergy;
	}

	public void setMonoFixedEnergyWidget(ScaleBox monoEnergyWidget) {
		this.monoEnergyWidget = monoEnergyWidget;
		monoEnergyWidget.addValueListener(l -> updateEnergyWidgetRange());
	}


	public void setMonoScanEnergyWidget(ScaleBox initialEnergy, ScaleBox finalEnergy) {
		this.monoInitialEnergyWidget = initialEnergy;
		this.monoFinalEnergyWidget = finalEnergy;
		monoInitialEnergyWidget.addValueListener(l -> updateEnergyWidgetRange());
		monoFinalEnergyWidget.addValueListener(l -> updateEnergyWidgetRange());
	}
}
