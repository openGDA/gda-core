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

package uk.ac.gda.ui.views.synoptic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.MotorException;
import gda.device.Scannable;
import gda.device.ScannableMotion;
import gda.device.motor.DummyMotor;
import gda.device.scannable.ScannableMotor;
import gda.device.scannable.ScannableUtils;
import gda.exafs.xes.IXesEnergyScannable;
import gda.exafs.xes.IXesSpectrometerScannable;
import gda.exafs.xes.XesUtils;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.rcp.views.NudgePositionerComposite;
import uk.ac.gda.client.livecontrol.LiveControlBase;

public class XesSimulatedPositionsView extends LiveControlBase {
	private static final Logger logger = LoggerFactory.getLogger(XesSimulatedPositionsView.class);

	/** Name of the XESEnergyScannable object used to calculate the motor positions */
	private String xesEnergyScannableName = "";

	private String xesBraggScannableName = "";

	/** Format to use for the motor position text labels */
	private String numberFormat = "%.5g";

	private IXesEnergyScannable xesEnergyScannable;
	private IXesSpectrometerScannable xesBraggScannable;

	private double defaultInitialEnergy = 2000;
	private Map<String, Text> positionWidgets = Collections.emptyMap();

	private ScannableMotion dummyEnergy;
	private ScannableMotion dummyBragg;

	private volatile boolean updateInProgress = false;
	private int motorLabelWidth = 100;
	private int motorIncrementWidth = 30;

	@Override
	public void createControl(Composite composite) {
		xesEnergyScannable = Finder.find(xesEnergyScannableName);
		if (xesEnergyScannable == null) {
			MessageDialog.openWarning(composite.getShell(), "Cannot open XesSimulatedPositions view",
					"Cannot open simulated positions view - required XesEnergyScannable object "+xesEnergyScannableName+" was not found on server.");
			return;
		}

		xesBraggScannable = Finder.find(xesBraggScannableName);
		if (xesBraggScannable == null) {
			logger.warn("Xes bragg scannable {} could not be found - using default Bragg angle limits", xesBraggScannableName);
		}

		try {
			setupGui(composite);
		} catch(Exception e) {
			logger.warn("Problem setting up simulated position GUI for {}", xesEnergyScannableName, e);
		}
	}

	private void setPosition(Scannable motor, double newPosition) throws MotorException {
		if (motor instanceof ScannableMotor mot) {
			mot.getMotor().setPosition(newPosition);
		}
	}

	private void setupGui(Composite parent) throws Exception {

		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout());

		// Set the parent container match the background colour of these widgets
		parent.getParent().setBackground(mainComposite.getBackground());

		double currentEnergy = defaultInitialEnergy;
		try {
			currentEnergy = getDouble(xesEnergyScannable.getPosition());
		} catch(DeviceException e) {
			logger.warn("Problem getting current energy from {} - using default initial energy ({})", xesEnergyScannableName, currentEnergy);
		}

		GridDataFactory fac = GridDataFactory.fillDefaults().grab(true, false);

		createDummyMotorControls(mainComposite);

		// Set the energy to match current XES energy value, and bragg angle to match
		setPosition(dummyEnergy, currentEnergy);
		setPosition(dummyBragg, convertEnergyToAngle(currentEnergy));

		// Update angle and energy when either one changes
		dummyEnergy.addIObserver(this::updateAngleEnergy);
		dummyBragg.addIObserver(this::updateAngleEnergy);

		// Recalculate the motor positions and update the GUI when energy is changed
		dummyEnergy.addIObserver((source, value) -> updateValues());

		Group comp = new Group(mainComposite, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		comp.setText("Simulated positions for "+xesEnergyScannableName);

		// Set up the widgets to show the motor names and calculated positions
		positionWidgets = new HashMap<>();
		Map<String, Double> scnPositions = xesEnergyScannable.getPositionsMap(currentEnergy);
		for(var pos : scnPositions.entrySet()) {
			Label label = new Label(comp, SWT.NONE);
			label.setText(pos.getKey());
			fac.align(SWT.RIGHT, SWT.CENTER).applyTo(label);

			Text textbox = new Text(comp, SWT.NONE);
			textbox.setEditable(false);
			fac.align(SWT.FILL, SWT.CENTER).applyTo(textbox);

			positionWidgets.put(pos.getKey(), textbox);
		}
		updateValues(scnPositions);

		mainComposite.addDisposeListener(d ->{
			dummyEnergy.deleteIObservers();
			dummyBragg.deleteIObservers();
		});
	}

	private void createDummyMotorControls(Composite parent) throws Exception {
		Group comp = new Group(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1 , false));
		comp.setText("Energy and angle");

		GridDataFactory fac = GridDataFactory.fillDefaults().grab(true, false);

		dummyEnergy = createDummyScannableMotor("XES Energy (eV)");
		dummyBragg = createDummyScannableMotor("XES Bragg (deg)");
		setupMotorLimits();

		NudgePositionerComposite energyPositioner = createNudgePositioner(comp);
		energyPositioner.setScannable(dummyEnergy);
		energyPositioner.setLimits(dummyEnergy.getLowerGdaLimits()[0], dummyEnergy.getUpperGdaLimits()[0]);
		fac.applyTo(energyPositioner);

		NudgePositionerComposite braggPositioner = createNudgePositioner(comp);
		braggPositioner.setScannable(dummyBragg);
		braggPositioner.setLimits(dummyBragg.getLowerGdaLimits()[0], dummyBragg.getUpperGdaLimits()[0]);
		fac.applyTo(braggPositioner);

		// Make sure the widgets have default background colour behaviour
		// (i.e. white background in Text widget rather than grey)
		comp.setBackgroundMode(SWT.INHERIT_DEFAULT);
	}

	private void setupMotorLimits() throws Exception {
		// Determine the Bragg angle limits (from xesBragg scannable if it has been set)
		double maxBragg = xesBraggScannable == null ? XesUtils.MAX_THETA : xesBraggScannable.getMaxTheta();
		double minBragg = xesBraggScannable == null ? XesUtils.MIN_THETA : xesBraggScannable.getMinTheta();

		// Set the Bragg dummy scannable limits
		dummyBragg.setUpperGdaLimits(maxBragg);
		dummyBragg.setLowerGdaLimits(minBragg);

		// Calculate the min and max allowed energies, update the dummy energy scannable limits
		double lowerEnergy = convertAngleToEnergy(maxBragg);
		double upperEnergy = convertAngleToEnergy(minBragg);
		dummyEnergy.setLowerGdaLimits(lowerEnergy);
		dummyEnergy.setUpperGdaLimits(upperEnergy);
	}

	private ScannableMotion createDummyScannableMotor(String name) throws FactoryException, MotorException {
		DummyMotor dummyMotor = new DummyMotor();
		dummyMotor.setName(name+"DummyMotor");
		dummyMotor.setMinPosition(0);
		dummyMotor.setMaxPosition(100000);
		dummyMotor.setPosition(0);
		dummyMotor.setSpeed(1000000);
		dummyMotor.configure();

		ScannableMotor scnMotor = new ScannableMotor();
		scnMotor.setName(name);
		scnMotor.setMotor(dummyMotor);
		scnMotor.configure();
		return scnMotor;
	}

	private NudgePositionerComposite createNudgePositioner(Composite parent) {
		NudgePositionerComposite positionerComposite = new NudgePositionerComposite(parent, SWT.HORIZONTAL, false);
		positionerComposite.setIncrementTextWidth(motorIncrementWidth);
		positionerComposite.setDisplayNameWidth(motorLabelWidth);
		positionerComposite.hideStopButton();
		return positionerComposite;
	}

	private double getDouble(Object position) {
		return ScannableUtils.objectToArray(position)[0];
	}

	/**
	 * Convert Energy (eV) to Bragg angle (degrees)
	 *
	 * @param energy (ev)
	 * @return angle (degrees)
	 * @throws DeviceException
	 */
	private double convertEnergyToAngle(double energyEv) throws DeviceException {
		return XesUtils.getBragg(energyEv, xesEnergyScannable.getMaterialType(), xesEnergyScannable.getCrystalCut());
	}

	/**
	 * Convert Bragg angle (degrees) to Energy (eV)
	 *
	 * @param angle (degrees)
	 * @return energy (ev)
	 * @throws DeviceException
	 */
	private double convertAngleToEnergy(double angle) throws DeviceException {
		return XesUtils.getFluoEnergy(angle, xesEnergyScannable.getMaterialType(), xesEnergyScannable.getCrystalCut());
	}

	/**
	 * Update the dummy energy or bragg angle value, depending on the 'source' of the update :
	 *
	 * <li>source = dummyEnergy -> update dummyBragg from the energy
	 * <li>source = dummyBragg -> update the energy from the bragg angle
	 *
	 * @param source
	 * @param value
	 */
	private void updateAngleEnergy(Object source, Object value) {
		if (updateInProgress) {
			return;
		}
		updateInProgress = true;
		try {
			// If energy has changed then calc. the bragg angle from energy; otherwise calculate the energy from the bragg angle
			boolean energyChanged = source == dummyEnergy;
			Scannable srcScannable = (Scannable) source;
			double currentValue = getDouble(srcScannable.getPosition());
			if (Double.isNaN(currentValue)) {
				return;
			}
			// Convert to energy or angle
			double newValue = energyChanged ? convertEnergyToAngle(currentValue) : convertAngleToEnergy(currentValue);

			// Scannable to be moved
			Scannable delegateScannable = energyChanged ? dummyBragg : dummyEnergy;

			logger.debug("Converting {} = {} -> {} = {}", srcScannable.getName(), currentValue, delegateScannable.getName(), newValue);
			if (Double.isFinite(newValue)) {
				delegateScannable.asynchronousMoveTo(newValue);
			}
		} catch (DeviceException e) {
			logger.error("Problem update energy/angle from dummy scannable {}", source);
		} finally {
			updateInProgress = false;
		}

	}

	/**
	 * Update the calculated values in the GUI using current energy position of the dummyEnergy motor
	 */
	private void updateValues() {
		try {
			double currentEnergy = getDouble(dummyEnergy.getPosition());
			Map<String, Double> positions = xesEnergyScannable.getPositionsMap(currentEnergy);
			Display.getDefault().asyncExec(() -> updateValues(positions));
		} catch (DeviceException e) {
			logger.error("Problem updating calculated positions for {} from dummy energy", xesEnergyScannableName, e);
		}
	}

	/**
	 * Update the GUI with the given motor positions
	 * Numerical values are formatted using {@link #numberFormat}
	 *
	 * @param positions map of motor positions
	 */
	private void updateValues(Map<String, Double> positions) {
		for(var pos : positions.entrySet()) {
			Text textbox = positionWidgets.get(pos.getKey());
			if (textbox != null) {
				textbox.setText(String.format(numberFormat, pos.getValue()));
			}
		}
	}

	/**
	 * Set the format used for the numerical values of the motor positions
	 *
	 * @param numberFormat
	 */
	public void setNumberFormat(String numberFormat) {
		this.numberFormat = numberFormat;
	}

	public void setMotorLabelWidth(int motorLabelWidth) {
		this.motorLabelWidth = motorLabelWidth;
	}

	/**
	 * Set the name of the XESEnergyScannable object used to calculate the motor positions
	 *
	 * @param xesEnergyScannableName
	 */
	public void setXesEnergyScannableName(String xesEnergyScannableName) {
		this.xesEnergyScannableName = xesEnergyScannableName;
	}

	public void setXesBraggScannableName(String xesBraggScannableName) {
		this.xesBraggScannableName = xesBraggScannableName;
	}
}
