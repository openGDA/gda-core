/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package org.opengda.detector.electronanalyser.entrance.slit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.factory.ConfigurableBase;
import gda.factory.FactoryException;
import gda.observable.IObserver;
import uk.ac.diamond.daq.pes.api.EntranceSlitInformationProvider;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(EntranceSlitInformationProvider.class)
public class AnalyserEntranceSlit extends ConfigurableBase implements EntranceSlitInformationProvider, IObserver {
	private static final Logger logger = LoggerFactory.getLogger(AnalyserEntranceSlit.class);
	public static final String DEFAULT_SLIT_STRING = "Straight, S0.05, A0.5";

	protected final Set<EntranceSlit> entranceSlitsSet = new HashSet<>();
	protected EntranceSlit defaultSlit;
	protected EntranceSlit currentSlit;
	private String name;

	//Configurable in spring xml
	private EnumPositioner slitScannable;
	private String splitSeparator = ", ";
	private int slitShapePosition = Integer.MAX_VALUE;
	private int slitSizePosition = Integer.MAX_VALUE;
	private int slitDirectionPosition = Integer.MAX_VALUE;
	private double defaultSlitSize = 0.1;
	private String defaultSlitShape = "Straight";
	private String defaultSlitDirection = "Vertical";
	private List<String> slitsList;
	private String defaultSlitString;

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) return;

		setDefaultSlit(parseEntranceSlitString(DEFAULT_SLIT_STRING, 0));

		if (getSlitScannable()==null) {
			logger.error("Failed to configure analyser entrance slit - slitScannable is null");
			return;
		}
		// set monitor to synchronise current slit
		slitScannable.addIObserver(this);

		setEntranceSlitsSet();

		// Update the current slit to avoid possible NPE
		update(null, null);

		logger.info("Finished configuring analyser entrance slit");
		setConfigured(true);
	}

	protected void setEntranceSlitsSet() {
		// empty list of slits and fill list of slits from scannable
		entranceSlitsSet.clear();
		try {
			slitsList = slitScannable.getPositionsList();
			for (String slit : slitsList) {
				logger.debug("Found slit : {}", slit);
				entranceSlitsSet.add(parseEntranceSlitString(slit, slitsList.indexOf(slit)));
			}
		} catch (DeviceException e) {
			logger.error("Failed to initially set up current analyser entrance slit", e);
		}
	}

	@Override
	public void reconfigure() throws FactoryException {
		logger.debug("Reconfigure called");
		if (isConfigured()) setConfigured(false);
		dispose();
		configure();
	}

	@Override
	public void update(Object source, Object arg) {
		try {
			setCurrentSlitByRawValue(getCurentSlitScannableRawValue());
		} catch (NumberFormatException | DeviceException e) {
			logger.error("Failed to set current slit after position update", e);
		}
	}

	protected int getCurentSlitScannableRawValue() throws NumberFormatException, DeviceException {
		return slitsList.indexOf(slitScannable.getPosition());
	}

	private EntranceSlit getEntranceSlitByRawValue(int rawValue) {
		return entranceSlitsSet.stream()
		.filter(slit -> slit.getRawValue()==rawValue)
		.findFirst().orElseGet(this::getDefaultSlit);
	}

	@Override
	public void setCurrentSlitByRawValue(int rawValue) {
		currentSlit = getEntranceSlitByRawValue(rawValue);
	}

	public EntranceSlit parseEntranceSlitString(String newPositionString, int i) {
		String[] values = newPositionString.split(getSplitSeparator());
		try {
			double slitSize = (getSlitSizePosition()==Integer.MAX_VALUE || values[getSlitSizePosition()]==null)? defaultSlitSize : extractDouble(values[getSlitSizePosition()]);
			String slitShape = (getSlitShapePosition() == Integer.MAX_VALUE || values[getSlitShapePosition()]==null)? defaultSlitShape : values[getSlitShapePosition()].strip();
			String slitDirection = (getSlitDirectionPosition() == Integer.MAX_VALUE || values[getSlitDirectionPosition()]==null)? defaultSlitDirection : values[getSlitDirectionPosition()].strip();

			return new EntranceSlit(i, slitSize, slitShape, slitDirection);
		} catch (Exception e) {
			logger.error("Failed to parse entrance slit values from epics string", e);
			logger.error("Setting entrance slit to a default value");
			return defaultSlit;
		}
	}

	private double extractDouble(String input) {
		return Double.parseDouble(input.strip().replaceAll("[^0-9.]", ""));
	}

	@Override
	public Number getRawValue() {
		return currentSlit.getRawValue();
	}

	@Override
	public Double getSizeInMM() {
		return currentSlit.getSize();
	}

	@Override
	public String getShape() {
		return currentSlit.getShape();
	}

	@Override
	public String getDirection() {
		return currentSlit.direction;
	}

	public EntranceSlit getCurrentSlit() {
		return currentSlit;
	}

	public String getDefaultSlitString() {
		return defaultSlitString;
	}

	public void setDefaultSlitString(String defaultSlitString) {
		if (defaultSlitString.isEmpty()) {
			this.defaultSlitString=DEFAULT_SLIT_STRING;
		} else {
			this.defaultSlitString = defaultSlitString;
		}
	}

	public EnumPositioner getSlitScannable() {
		return slitScannable;
	}

	public void setSlitScannable(EnumPositioner slitScannable) {
		this.slitScannable = slitScannable;
	}

	public EntranceSlit getDefaultSlit() {
		return defaultSlit;
	}

	public void setDefaultSlit(EntranceSlit defaultSlit) {
		this.defaultSlit = defaultSlit;
	}

	public void dispose() {
		if (slitScannable!=null) slitScannable.deleteIObserver(this);
	}

	@Override
	public double getSizeByRawValue(int rawValue) {
		EntranceSlit s = getEntranceSlitByRawValue(rawValue);
		return s.getSize();
	}

	@Override
	public List<Integer> getSlitsRawValueList() {
		return entranceSlitsSet.stream().map(i->i.getRawValue()).sorted().toList();
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getSplitSeparator() {
		return splitSeparator;
	}

	public void setSplitSeparator(String splitSeparator) {
		this.splitSeparator = splitSeparator;
	}

	public int getSlitShapePosition() {
		return slitShapePosition;
	}

	public void setSlitShapePosition(int slitShapePosition) {
		this.slitShapePosition = slitShapePosition;
	}

	public int getSlitSizePosition() {
		return slitSizePosition;
	}

	public void setSlitSizePosition(int slitSizePosition) {
		this.slitSizePosition = slitSizePosition;
	}

	public int getSlitDirectionPosition() {
		return slitDirectionPosition;
	}

	public void setSlitDirectionPosition(int slitDirectionPosition) {
		this.slitDirectionPosition = slitDirectionPosition;
	}

	/**
	 * Small immutable class to encapsulate the information about a entrance slit
	 */
	public static class EntranceSlit {
		private final int rawValue;
		private final double size;
		private final String shape;
		private final String direction;

		public EntranceSlit(int rawValue, double size, String shape, String direction) {
			this.rawValue = rawValue; // eg. 100, 200, 300
			this.size = size; // in mm eg 0.1 mm 0.3 mm
			this.shape = shape; // e.g. curved, straight, aperture
			this.direction = direction; // vertical or horizontal
		}

		public int getRawValue() {
			return rawValue;
		}

		public double getSize() {
			return size;
		}

		public String getShape() {
			return shape;
		}

		public String getDirection() {
			return direction;
		}

		@Override
		public String toString() {
			return "EntranceSlit [rawValue=" + rawValue + ", size=" + size + ", direction="+direction+", shape=" + shape + "]";
		}
	}
}
