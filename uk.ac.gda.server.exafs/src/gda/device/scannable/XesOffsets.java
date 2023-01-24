/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.device.scannable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.IScannableMotor;
import gda.device.Scannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.exafs.xes.IXesOffsets;
import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;
import uk.ac.diamond.daq.persistence.jythonshelf.LocalParameters;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * This class is a replacement for the jython XESOffset, XESCalculate classes in xes_offset.py, xes_calculate.py.
 *
 */
@ServiceInterface(IXesOffsets.class)
public class XesOffsets extends FindableConfigurableBase implements IXesOffsets {
	private static final Logger logger = LoggerFactory.getLogger(XesOffsets.class);

	private String currentOffsetFile = "";
	private String tempOffsetFile = "xes_temp";
	private String storeDirectory = "";

	/** Scannables whose offset values are to saved/loaded from XMLConfiguration by {@link #apply(String)} and {@link #saveAs(String)}
	 *  Normally just the scannables for XesEnergyScannable, but might include more, depending on the spring configuration */
	private ScannableGroup spectrometerGroup;

	private XESEnergyScannable xesEnergyScannable;

	public XesOffsets() {
		storeDirectory = LocalProperties.getVarDir() +"xes_offsets/";
	}

	@Override
	public void configure() throws FactoryException {
		if (spectrometerGroup == null) {
			throw new FactoryException("spectrometerGroup has not been set for XesOffset object");
		}
		if (xesEnergyScannable == null) {
			throw new FactoryException("xesEnergyScannable has not been set for XesOffset object");
		}
		logger.info("Setting {} scannable group scannables using all scannables from {}", spectrometerGroup.getName(), xesEnergyScannable.getName());
		spectrometerGroup.setGroupMembersWithList(xesEnergyScannable.getXes().getScannables(), true);
		setConfigured(true);
	}

    /**
     *
     * Calibrate the spectrometer :
     * <li> Assume that the spectrometer is currently aligned to specified energy,
     * <li> Calculate the extra offsets such that actual current position + offset = position needed for energy.
     * <li> Save the offsets to temporary file.
     * (Run by 'Calibrate' button in xes calibration view).
     *
     * @throws DeviceException
    */
	@Override
	public void applyFromLive(double fluoEnergy) throws DeviceException, IOException {
		logger.info("Applying motor offsets for expected energy {} eV", fluoEnergy);
		Map<Scannable, Double> expectedValuesMap =  calcSpectrometerPositions(fluoEnergy);
		Map<IScannableMotor, Double> newOffsets = createOffsetMap(expectedValuesMap);
		applyFromMap(newOffsets);
		saveToTemp();
	}

	/**
	 * Apply offset settings saved in file to motors
	 * ('Load' button in xes calibration view)
	 */
	@Override
	public void apply(String filename) throws IOException {
		logger.info("Loading offsets from file {} in directory {} and applying to spectrometer", filename, storeDirectory);

		Map<IScannableMotor, Double> offsets = loadOffsetMap(filename);
		applyFromMap(offsets);
		currentOffsetFile = filename;
	}

	@Override
	public void reApply() throws IOException {
		apply(currentOffsetFile);
	}

	@Override
	public void applyFromTemp() throws IOException {
		apply(tempOffsetFile);
	}

	/**
	 * Set offsets on all motors to zero
	 * ('Remove offsets' button in xes calibration view)
	 */
	@Override
	public void removeAll() {
		logger.debug("Setting all motor offsets to zero");
		for (Scannable scn : spectrometerGroup.getGroupMembersAsArray()) {
			if (scn instanceof IScannableMotor scnMotor) {
				scnMotor.setOffset(0.0);
			}
		}
		spectrometerGroup.update(this, OFFSET_UPDATE_EVENT);
	}

	private Map<IScannableMotor, Double> loadOffsetMap(String filename) throws  IOException {
		logger.info("Loading offsets from file {} in directory {}", filename, storeDirectory);
		// Check filename has been given
		if (filename == null || filename.isEmpty()) {
			throw new FileNotFoundException("No offset file name given");
		}

		// Check file exists
		if (!Paths.get(storeDirectory, filename + ".xml").toFile().exists()) {
			throw new FileNotFoundException("Offset file " + filename + ".xml not found");
		}

		XMLConfiguration store;
		try {
			store = LocalParameters.getXMLConfiguration(storeDirectory, filename, false);
		} catch (ConfigurationException e) {
			throw new IOException("Problem loading offsets from file "+filename, e);
		}

		// Make map from scannable to offset
		Map<IScannableMotor, Double> offsets = new HashMap<>();
		for (var scn : getSpectrometerScannables()) {
			Object obj = store.getProperty(scn.getName());
			Double offset;
			if (obj == null) {
				offset = Double.valueOf(0);
			} else {
				offset = ScannableUtils.objectToArray(obj)[0];
			}
			offsets.put((ScannableMotor) scn, offset);
		}
		return offsets;
	}


	/**
	 * Apply offset to each scannable motor in offsets map, using {@link ScannableMotor#setOffset(Double...)}.
	 * @param offsets map with : key = scannable motor, value = offset value to be applied.
	 */
	private void applyFromMap(Map<IScannableMotor, Double> offsets ) {
		logger.debug("Applying motor offsets");
		offsets.entrySet().forEach( entry -> entry.getKey().setOffset( entry.getValue() ) );
		spectrometerGroup.update(this, OFFSET_UPDATE_EVENT);
	}

	private List<IScannableMotor> getSpectrometerScannables() {
		return xesEnergyScannable.getXes().getScannables()
				.stream()
				.filter(IScannableMotor.class::isInstance)
				.map(IScannableMotor.class::cast)
				.toList();
	}

	/**
	 * Save current motor offset values to file.
	 * ('Save' button from xes calibration view)
	 */
	@Override
	public void saveAs(String filename) throws IOException {
		logger.info("Saving offsets to file {} in directory {}", filename, storeDirectory);
		try {
			XMLConfiguration store = LocalParameters.getXMLConfiguration(storeDirectory, filename, true);

			// Lookup the current motor offsets for all the scannables used by spectrometer
			// and place in store :
			for(var scn : getSpectrometerScannables()) {
				Double offset = getOffsetForMotor(scn);
				store.setProperty(scn.getName(), offset);
			}
			store.save();
		} catch (ConfigurationException e) {
			throw new IOException("Problem saving offsets to file "+filename, e);
		}
	}

	@Override
	public void saveToTemp() throws IOException {
		saveAs(tempOffsetFile);
	}

	/**
	 * Return offsets values so that motor positions match those in requiredPositionMap.
	 * @param requiredPositionMap key = scannable motor, value = required position
	 * @throws DeviceException
	 */
	private Map<IScannableMotor, Double> createOffsetMap(Map<Scannable, Double> requiredPositionMap) throws DeviceException {
		logger.debug("Calculating motor offsets from map of required positions : {}", requiredPositionMap);

		// Calculate offset for each motor from :  current position, current offset and required position
		Map<IScannableMotor, Double> newOffsets = new HashMap<>();
		for(Entry<Scannable, Double> entry : requiredPositionMap.entrySet()) {
			if (!(entry.getKey() instanceof IScannableMotor)) {
				continue;
			}
			IScannableMotor scnMotor = (IScannableMotor) entry.getKey();
			Double currentMotorPosition = ScannableUtils.getCurrentPositionArray(scnMotor)[0];
			Double currentMotorOffset = getOffsetForMotor(scnMotor);
			Double requiredPosition = entry.getValue();

			// Calc. new offset from : requiredPosition - newOffset = currentMotorPosition - currentMotorOffset
			Double newOffset = requiredPosition - (currentMotorPosition - currentMotorOffset);
			newOffsets.put(scnMotor, newOffset);
		}
		logger.debug("New motor offsets : {}", newOffsets);
		return newOffsets;
	}

	/**
	 *
	 * @param scnMotor
	 * @return Offset value from motor as Double (not Double[]), using zero if no offset is null.
	 */
	private Double getOffsetForMotor(IScannableMotor scnMotor) {
		if (scnMotor.getOffset() == null) {
			return Double.valueOf(0);
		} else {
			return scnMotor.getOffset()[0];
		}
	}

	/**
	 * Calculate the positions of all the motors in the XES spectrometer for it to be aligned to given energy.
	 * @param energy (eV)
	 * @return
	 * @throws DeviceException
	 */
    private Map<Scannable, Double> calcSpectrometerPositions(double energy) throws DeviceException {
    	logger.debug("Calculating spectrometer positions for energy {} eV", energy);
		Map<Scannable, Double> expectedValues = xesEnergyScannable.getScannablePositionsMap(energy);
		logger.debug("Calculated spectrometer positions : {}", expectedValues);
		return expectedValues;
    }

	/**
	 * Used for 'Loaded offset file' textbox in xes calibration view.
	 */
	@Override
	public String getCurrentFile() {
		return currentOffsetFile;
	}

	@Override
	public void setTempSaveName(String filename) {
		tempOffsetFile = filename;
	}

	@Override
	public String getTempSaveName() {
		return tempOffsetFile;
	}

	@Override
	public String getSpectrometerGroupName() {
		return spectrometerGroup.getName();
	}

	@Override
	public String getXesEnergyScannableName() {
		return xesEnergyScannable.getName();
	}

	public String getStoreDirectory() {
		return storeDirectory;
	}

	public void setStoreDirectory(String storeDirectory) {
		this.storeDirectory = storeDirectory;
	}

	public ScannableGroup getSpectrometerGroup() {
		return spectrometerGroup;
	}

	public void setSpectrometerGroup(ScannableGroup spectrometerGroup) {
		this.spectrometerGroup = spectrometerGroup;
	}

	public XESEnergyScannable getXesEnergyScannable() {
		return xesEnergyScannable;
	}

	public void setXesEnergyScannable(XESEnergyScannable xesEnergyScannable) {
		this.xesEnergyScannable = xesEnergyScannable;
	}

}
