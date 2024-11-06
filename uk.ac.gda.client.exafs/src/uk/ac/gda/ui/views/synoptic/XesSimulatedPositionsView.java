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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.device.scannable.ScannableUtils;
import gda.exafs.xes.IXesEnergyScannable;
import gda.exafs.xes.IXesSpectrometerScannable;
import gda.exafs.xes.XesUtils;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.observable.IObserver;
import gda.rcp.views.NudgePositionerComposite;
import uk.ac.gda.client.livecontrol.LiveControlBase;

public class XesSimulatedPositionsView extends LiveControlBase implements IObserver {
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

	private volatile boolean updateAngleEnergyInProgress = false;
	private volatile boolean updateLimitsInProgress = false;

	private int motorLabelWidth = 150;
	private int motorIncrementWidth = 30;

	private NudgePositionerComposite energyPositioner;
	private NudgePositionerComposite braggPositioner;

	/** Names of the scannables that control the 'parameters' of the spectrometer (crystal cut, material and radiu) */
	private List<String> parameterScannableNames = Collections.emptyList();

	/** The spectrometer crystal cut, radius and material scannables being observed */
	private List<Scannable> parameterScannables = Collections.emptyList();

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

		// Register this class as observer of the parameter scannables,
		// so that changes in crystal cut, material and radius can be detected
		parameterScannables = new ArrayList<>();
		for(var name : parameterScannableNames) {
			Scannable scn = Finder.find(name);
			if (scn != null) {
				scn.addIObserver(this);
				parameterScannables.add(scn);
			}
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
		updatePositionWidgetValues(scnPositions);

		mainComposite.addDisposeListener(d ->{
			dummyEnergy.deleteIObservers();
			dummyBragg.deleteIObservers();
			parameterScannables.forEach(scn -> scn.deleteIObserver(this));
		});
	}

	private void createDummyMotorControls(Composite parent) throws Exception {
		Group comp = new Group(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1 , false));
		comp.setText("Energy and angle");

		GridDataFactory fac = GridDataFactory.fillDefaults().grab(true, false);

		dummyEnergy = createDummyScannableMotor("XES Energy (eV)");
		dummyBragg = createDummyScannableMotor("XES Bragg (deg)");

		energyPositioner = createNudgePositioner(comp);
		energyPositioner.setScannable(dummyEnergy);
		fac.applyTo(energyPositioner);

		braggPositioner = createNudgePositioner(comp);
		braggPositioner.setScannable(dummyBragg);
		fac.applyTo(braggPositioner);

		setupMotorLimits();

		// Make sure the widgets have default background colour behaviour
		// (i.e. white background in Text widget rather than grey)
		comp.setBackgroundMode(SWT.INHERIT_DEFAULT);
	}

	/**
	 * Update the Bragg and Energy motor limits. This should be called if the crystal cut
	 * or material type is changed :
	 *
	 * <li> <b>lower</b> energy limit is calculated from the <b>maximum</b> allowed Bragg angle,
	 * <li> <b>upper</b> energy limit is calculated from the <b>minimum</b> allowed Bragg angle.
	 * <li> Energy motor position is set using the current value of Bragg angle,
	 * so that the two values are consistent
	 * <br>
	 * (Bragg angle to energy conversion depends on crystal cut and material as well as Bragg angle.)
	 * @throws Exception
	 */
	private void setupMotorLimits() throws Exception {
		updateLimitsInProgress = true;
		try {
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

			// Set limits on the nudge positioners (setEnabled does this already!)
			energyPositioner.setLimits(lowerEnergy, upperEnergy);
			braggPositioner.setLimits(minBragg, maxBragg);

			// Enable the nudge positioners, so that the limits are updated from the
			// underlying scannables and the tooltip is also updated.
			Display.getDefault().asyncExec(() -> {
				energyPositioner.setEnabled(true);
				braggPositioner.setEnabled(true);
			});

			double currentBragg = getDouble(dummyBragg.getPosition());
			dummyEnergy.moveTo(convertAngleToEnergy(currentBragg));
		} finally {
			updateLimitsInProgress = false;
		}
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
	 * Update the dummy energy or Bragg angle value, depending on the 'source' of the update :
	 *
	 * <li>source = dummyEnergy -> update dummyBragg from the energy
	 * <li>source = dummyBragg -> update the energy from the Bragg angle
	 *
	 * @param source
	 * @param value
	 */
	private void updateAngleEnergy(Object source, Object value) {
		// To prevent race conditions, return immediately if this function in executing
		// a different thread or the limits are currently being updated
		if (updateAngleEnergyInProgress || updateLimitsInProgress) {
			return;
		}
		updateAngleEnergyInProgress = true;
		try {
			// If energy has changed then calculate the Bragg angle from energy.
			// Otherwise calculate the energy from the Bragg angle
			boolean energyChanged = source == dummyEnergy;
			Scannable srcScannable = (Scannable) source;
			double currentValue = getDouble(srcScannable.getPosition());
			if (Double.isNaN(currentValue)) {
				return;
			}

			// If current energy value is outside of the limits,
			// replace it with the value calculated from max or minimum Bragg angle
			if (energyChanged && dummyEnergy.checkPositionValid(currentValue) != null) {
				if (currentValue < dummyEnergy.getLowerGdaLimits()[0]) {
					currentValue = convertAngleToEnergy(dummyBragg.getUpperGdaLimits()[0]);
				} else {
					currentValue = convertAngleToEnergy(dummyBragg.getLowerGdaLimits()[0]);
				}
			}

			// Generate the new energy or angle
			double newValue = energyChanged ? convertEnergyToAngle(currentValue) : convertAngleToEnergy(currentValue);

			// Scannable to be moved
			Scannable delegateScannable = energyChanged ? dummyBragg : dummyEnergy;

			logger.debug("Converting {} = {} -> {} = {}", srcScannable.getName(), currentValue, delegateScannable.getName(), newValue);
			if (Double.isFinite(newValue)) {
				delegateScannable.moveTo(newValue);
			}

			// Update the motor positions in the GUI
			updateValues();
		} catch (Exception e) {
			logger.error("Problem update energy/angle from dummy scannable {}", source, e);
		} finally {
			updateAngleEnergyInProgress = false;
		}
	}

	/**
	 * Update the calculated values in the GUI using current energy position of the dummyEnergy motor
	 */
	private void updateValues() {
		try {
			double currentEnergy = getDouble(dummyEnergy.getPosition());
			Map<String, Double> positions = xesEnergyScannable.getPositionsMap(currentEnergy);
			Display.getDefault().asyncExec(() -> updatePositionWidgetValues(positions));
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
	private void updatePositionWidgetValues(Map<String, Double> positions) {
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

	public void setParameterScannableNames(List<String> spectrometerParamScannableNames) {
		this.parameterScannableNames = spectrometerParamScannableNames;
	}

	/**
	 * Update the limits when crystal cut, radius or material scannables are changed.
	 */
	@Override
	public void update(Object source, Object arg) {
		if (source instanceof Scannable scn && arg instanceof ScannablePositionChangeEvent) {
			try {
				logger.debug("{} changed, updating energy motor limits and recalculating energy", scn.getName());
				setupMotorLimits();
				updateValues();
			} catch (Exception e) {
				logger.error("Encountered problem", e);
			}
		}
	}
}
