/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.MotorException;
import gda.device.Scannable;
import gda.device.motor.EpicsMotor;
import gda.factory.FindableBase;
import gda.factory.Finder;
import gda.util.CrystalParameters.CrystalMaterial;
import uk.ac.diamond.daq.persistence.jythonshelf.LocalParameters;

/**
 * Class used to manipulate Epics motor record offsets for XES spectrometer.
 * The current set of motor offsets for all motors in an {@link XESEnergyScannable} object along
 * with additional information can be saved and loaded from XML file
 * (using Apache Common({@link XMLConfiguration}).
 * <p>
 * <li>The current set of motor records offsets can be retrieved using {@link #getEpicsOffsets()}
 * <li>The latest offsets values can be saved to an XML file using {@link #saveOffsets(String, String)};
 * <li> A previously saved XML file can be reloaded and the the offsets applied to the motor records using {@link #loadOffsets(String, String)}.
 *
 */
public class MotorOffsetStore extends FindableBase {

	private static final Logger logger = LoggerFactory.getLogger(MotorOffsetStore.class);

	private static final String CRYSTAL_CUT_PROPERTY = "crystalCut";
	private static final String MATERIAL_PROPERTY = "crystalMaterial";
	private static final String RADIUS_PROPERTY = "radius";
	private static final String DATE_PROPERTY = "date";
	private static final String EXTRA_INFO_PROPERTY = "extraInformation";
	private static final String SPECTROMETER_NAME_PROPERTY = "spectrometerName";

	private static final List<String> EXTRA_PROP_KEYS = Arrays.asList(CRYSTAL_CUT_PROPERTY, MATERIAL_PROPERTY, RADIUS_PROPERTY, DATE_PROPERTY, EXTRA_INFO_PROPERTY, SPECTROMETER_NAME_PROPERTY);

	private XESEnergyScannable xesEnergyScannable;
	private String dateTimeFormat = "yyyy/MM/dd HH:mm:ss";
	private String extraInformation = "";

	/**
	 * Save the current Epics motor records offsets and metadata to an XML file
	 *
	 * @param directory where the file should be saved
	 * @param fileName name of the file to save to (without .xml suffix)
	 * @throws ConfigurationException
	 * @throws IOException
	 * @throws DeviceException
	 */
	public void saveOffsets(String directory, String fileName) throws ConfigurationException, IOException, DeviceException {
		XMLConfiguration config = LocalParameters.getXMLConfiguration(directory, fileName, true);
		config.clear();
		for(var ent : createExtraProperties().entrySet()) {
			config.addProperty(ent.getKey(), ent.getValue());
		}

		for(var ent : getEpicsOffsets().entrySet()) {
			config.addProperty(ent.getKey().getName(), ent.getValue());
		}
		config.save();
	}

	/**
	 * Load a previously saved XML and apply the offsets to the Epics motor records.
	 *
	 * @param directory containing the file
	 * @param fileName of the file to be loaded
	 * @throws Exception
	 */
	public void loadOffsets(String directory, String fileName) throws Exception {
		XMLConfiguration config = LocalParameters.getXMLConfiguration(directory, fileName, false, true);

		checkAllPropertiesPresent(config);

		String spectrometerName = getConfigProperty(config, SPECTROMETER_NAME_PROPERTY);
		if (!spectrometerName.equals(xesEnergyScannable.getName())) {
			throw new IllegalArgumentException("Spectrometer name in config file '"+spectrometerName+"' does not match name of the XESEnergy scannable settings would be applied to ("+xesEnergyScannable.getName()+")");
		}

		// Iterate over all the keys and make map containing offset values for each scannable motor
		var allKeys = config.getKeys();
		Map<Scannable, Double> offsetMap = new LinkedHashMap<>();
		while(allKeys.hasNext()) {
			String key = allKeys.next();
			if (EXTRA_PROP_KEYS.contains(key)) {
				continue;
			}
			Scannable scn = getScannable(key);
			offsetMap.put(scn, config.getDouble(key));
		}

		setupSpectrometer(config);

		// If we get here all scannable have been found -> apply the offset values
		applyEpicsOffsets(offsetMap);
	}

	/**
	 * Create mao containing additional information (key = information type)
	 * e.g. name of spectrometer scannable, crystal type and cut etc.
	 *
	 * @return map
	 * @throws DeviceException
	 */
	public Map<String, Object> createExtraProperties() throws DeviceException {
		Map<String, Object> extraProps = new LinkedHashMap<>();

		extraProps.put(SPECTROMETER_NAME_PROPERTY, xesEnergyScannable.getName());

		int[] crystalCut = xesEnergyScannable.getCrystalCut();
		String cutString = Arrays.stream(crystalCut).mapToObj(i -> Integer.toString(i)).collect(Collectors.joining());

		extraProps.put(CRYSTAL_CUT_PROPERTY, cutString);
		extraProps.put(RADIUS_PROPERTY, xesEnergyScannable.getRadius());
		extraProps.put(MATERIAL_PROPERTY, xesEnergyScannable.getMaterialType().getDisplayString());
		extraProps.put(DATE_PROPERTY, getCurrentDateTimeString());
		extraProps.put(EXTRA_INFO_PROPERTY, StringUtils.defaultIfEmpty(extraInformation, ""));

		return extraProps;
	}

	private String getCurrentDateTimeString() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(dateTimeFormat);
		return dtf.format(LocalDateTime.now());
	}

	public Map<Scannable, Double> getEpicsOffsets() throws MotorException {
		return getEpicsOffsets(xesEnergyScannable.getXes().getScannables());
	}

	public Map<Scannable, Double> getEpicsOffsets(List<Scannable> scannables) throws MotorException {
		Map<Scannable, Double> offsets = new LinkedHashMap<>();
		for(var scn : scannables) {
			offsets.put(scn, getOffset(scn));
		}
		return offsets;
	}

	private Double getOffset(Scannable scn) throws MotorException {
		if (scn instanceof ScannableMotor scnMotor && scnMotor.getMotor() instanceof EpicsMotor epicsMotor) {
			return epicsMotor.getUserOffset();
		}
		return 0.0;
	}

	private void applyEpicsOffsets(Map<Scannable, Double> offsetsMap) throws MotorException {
		logger.info("Applying Epics offsets for {}", xesEnergyScannable.getName());
		for(var entry : offsetsMap.entrySet()) {
			applyOffset(entry.getKey(), entry.getValue());
		}
	}

	private void applyOffset(Scannable scn, Double offset) throws MotorException {
		if (scn instanceof ScannableMotor scnMotor && scnMotor.getMotor() instanceof EpicsMotor epicsMotor) {
			logger.debug("Setting offset for {} : {}", scn.getName(), offset);
			epicsMotor.setUserOffset(offset);
		}
	}

	/**
	 * Get a value from the XMLConfiguration.
	 *
	 * @param config XMLConfiguration
	 * @param key of the property to retrieve
	 * @return property with the given key in the configuration
	 * @throws IllegalArgumentException if property with given key name is not present
	 */
	private String getConfigProperty(XMLConfiguration config, String key) throws IllegalArgumentException {
		if (!config.containsKey(key)) {
			throw new IllegalArgumentException("Cannot find required configuration property '"+key+"'");
		}
		return config.getString(key);
	}

	private void checkAllPropertiesPresent(XMLConfiguration config) {
		EXTRA_PROP_KEYS.forEach(key -> getConfigProperty(config, key));
	}

	private Scannable getScannable(String name) throws Exception {
		return Finder.findOptionalOfType(name, Scannable.class).orElseThrow(() -> new Exception("Could not find scannable called "+name));
	}

	private void setupSpectrometer(XMLConfiguration config) throws DeviceException {
		String crystalCuts = getConfigProperty(config,  CRYSTAL_CUT_PROPERTY);
		applyCrystalCuts(crystalCuts);

		String crystalMaterial = getConfigProperty(config,  MATERIAL_PROPERTY);
		if (!crystalMaterial.equals(CrystalMaterial.SILICON.getDisplayString() )
				&& !crystalMaterial.equals(CrystalMaterial.GERMANIUM.getDisplayString()) ) {
			throw new IllegalArgumentException("Invalid crystal material type '"+crystalMaterial+"'");

		}
		xesEnergyScannable.getMaterial().moveTo(crystalMaterial);

		String radius = getConfigProperty(config, RADIUS_PROPERTY);
		xesEnergyScannable.getXes().setRadius(Double.valueOf(radius));

		extraInformation = config.getString(EXTRA_INFO_PROPERTY);
	}

	private void applyCrystalCuts(String crystalCuts) throws DeviceException {
		String[] cutStringValues = crystalCuts.split("");
		int[] cutIntValues = Arrays.stream(cutStringValues).mapToInt(Integer::valueOf).toArray();

		xesEnergyScannable.getCut1().moveTo(cutIntValues[0]);
		xesEnergyScannable.getCut2().moveTo(cutIntValues[1]);
		xesEnergyScannable.getCut3().moveTo(cutIntValues[2]);
	}

	public XESEnergyScannable getXesEnergyScannable() {
		return xesEnergyScannable;
	}

	public void setXesEnergyScannable(XESEnergyScannable xesEnergyScannable) {
		this.xesEnergyScannable = xesEnergyScannable;
	}

	public String getExtraInformation() {
		return extraInformation;
	}

	public void setExtraInformation(String extraInformation) {
		this.extraInformation = extraInformation;
	}
}
