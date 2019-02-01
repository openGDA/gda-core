/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.device.detector.areadetector.v17.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.NDOverlaySimple;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gov.aps.jca.Channel;

public class NDOverlaySimpleImpl extends NDBaseImpl implements NDOverlaySimple, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(NDOverlaySimpleImpl.class);

	private static final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	private static final String NAME = "Name";
	private static final String NAME_RBV = "Name_RBV";

	private static final String USE = "Use";
	private static final String USE_RBV = "Use_RBV";

	private static final String SHAPE = "Shape";
	private static final String SHAPE_RBV = "Shape_RBV";

	private static final String DRAW_MODE = "DrawMode";
	private static final String DRAW_MODE_RBV = "DrawMode_RBV";

	private static final String RED_VALUE = "Red";
	private static final String RED_VALUE_RBV = "Red_RBV";

	private static final String GREEN_VALUE = "Green";
	private static final String GREEN_VALUE_RBV = "Green_RBV";

	private static final String BLUE_VALUE = "Blue";
	private static final String BLUE_VALUE_RBV = "Blue_RBV";

	private static final String POSITION_X = "PositionX";
	private static final String POSITION_X_RBV = "PositionX_RBV";

	private static final String POSITION_Y = "PositionY";
	private static final String POSITION_Y_RBV = "PositionY_RBV";

	private static final String CENTRE_X = "CenterX";
	private static final String CENTRE_X_RBV = "CenterX_RBV";

	private static final String CENTRE_Y = "CenterY";
	private static final String CENTRE_Y_RBV = "CenterY_RBV";

	private static final String SIZE_X = "SizeX";
	private static final String SIZE_X_RBV = "SizeX_RBV";

	private static final String SIZE_Y = "SizeY";
	private static final String SIZE_Y_RBV = "SizeY_RBV";

	// Map of PV suffix to channel
	private final Map<String, Channel> channelMap = new ConcurrentHashMap<>();

	private Integer overlayNumber;
	private String basePVName;

	@Override
	public void afterPropertiesSet() throws FactoryException {
		if (overlayNumber == null) {
			throw new FactoryException("overlay number not set");
		}
		if (basePVName == null) {
			throw new FactoryException("base PV not set");
		}
	}

	/**
	 * Create a channel corresponding to the given PV suffix, or return it from the map if it already exists
	 *
	 * @param pvSuffix
	 *            suffix to be added to {@link #basePVName}
	 * @return channel corresponding to the suffix
	 */
	private Channel getChannel(String pvSuffix) {
		return channelMap.computeIfAbsent(pvSuffix, pvSuffix2 -> {
			try {
				return EPICS_CONTROLLER.createChannel(getFullPvName(pvSuffix2));
			} catch (Exception e) {
				logger.error("Error creating channel", e);
				return null;
			}
		});
	}

	private String getFullPvName(String pvSuffix) {
		return String.format("%s:%d:%s", basePVName, overlayNumber, pvSuffix);
	}

	private String getStringValue(String pvSuffix) throws DeviceException {
		try {
			return EPICS_CONTROLLER.caget(getChannel(pvSuffix));
		} catch (Exception ex) {
			final String message = constructGetErrorMessage(pvSuffix);
			logger.error(message, ex);
			throw new DeviceException(message, ex);
		}
	}

	private void setStringValue(String pvSuffix, String value) throws DeviceException {
		try {
			EPICS_CONTROLLER.caput(getChannel(pvSuffix), value);
		} catch (Exception ex) {
			final String message = constructSetErrorMessage(pvSuffix);
			logger.error(message, ex);
			throw new DeviceException(message, ex);
		}
	}

	private short getEnumValue(String pvSuffix) throws DeviceException {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(pvSuffix));
		} catch (Exception ex) {
			final String message = constructGetErrorMessage(pvSuffix);
			logger.error(message, ex);
			throw new DeviceException(message, ex);
		}
	}

	private void setEnumValue(String pvSuffix, short value) throws DeviceException {
		try {
			EPICS_CONTROLLER.caput(getChannel(pvSuffix), value);
		} catch (Exception ex) {
			final String message = constructSetErrorMessage(pvSuffix);
			logger.error(message, ex);
			throw new DeviceException(message, ex);
		}
	}

	private int getIntValue(String pvSuffix) throws DeviceException {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(pvSuffix));
		} catch (Exception ex) {
			final String message = constructGetErrorMessage(pvSuffix);
			logger.error(message, ex);
			throw new DeviceException(message, ex);
		}
	}

	private void setIntValue(String pvSuffix, int value) throws DeviceException {
		try {
			EPICS_CONTROLLER.caput(getChannel(pvSuffix), value);
		} catch (Exception ex) {
			final String message = constructSetErrorMessage(pvSuffix);
			logger.error(message, ex);
			throw new DeviceException(message, ex);
		}
	}

	private String constructGetErrorMessage(String pvSuffix) {
		return String.format("Error getting value of %s", getFullPvName(pvSuffix));
	}

	private String constructSetErrorMessage(String pvSuffix) {
		return String.format("Error setting value of %s", getFullPvName(pvSuffix));
	}

	@Override
	public String getName() throws DeviceException {
		return getStringValue(NAME);
	}

	@Override
	public void setName(String name) throws DeviceException {
		setStringValue(NAME, name);
	}

	@Override
	public String getNameRbv() throws DeviceException {
		return getStringValue(NAME_RBV);
	}

	@Override
	public short getUse() throws DeviceException {
		return getEnumValue(USE);
	}

	@Override
	public void setUse(short use) throws DeviceException {
		setEnumValue(USE, use);
	}

	@Override
	public short getUseRbv() throws DeviceException {
		return getEnumValue(USE_RBV);
	}

	@Override
	public short getShape() throws DeviceException {
		return getEnumValue(SHAPE);
	}

	@Override
	public void setShape(short shape) throws DeviceException {
		setEnumValue(SHAPE, shape);
	}

	@Override
	public short getShapeRbv() throws DeviceException {
		return getEnumValue(SHAPE_RBV);
	}

	@Override
	public short getDrawMode() throws DeviceException {
		return getEnumValue(DRAW_MODE);
	}

	@Override
	public void setDrawMode(short drawMode) throws DeviceException {
		setEnumValue(DRAW_MODE, drawMode);
	}

	@Override
	public short getDrawModeRbv() throws DeviceException {
		return getEnumValue(DRAW_MODE_RBV);
	}

	@Override
	public int getRed() throws DeviceException {
		return getIntValue(RED_VALUE);
	}

	@Override
	public void setRed(int value) throws DeviceException {
		setIntValue(RED_VALUE, value);
	}

	@Override
	public int getRedRbv() throws DeviceException {
		return getIntValue(RED_VALUE_RBV);
	}

	@Override
	public int getGreen() throws DeviceException {
		return getIntValue(GREEN_VALUE);
	}

	@Override
	public void setGreen(int value) throws DeviceException {
		setIntValue(GREEN_VALUE, value);
	}

	@Override
	public int getGreenRbv() throws DeviceException {
		return getIntValue(GREEN_VALUE_RBV);
	}

	@Override
	public int getBlue() throws DeviceException {
		return getIntValue(BLUE_VALUE);
	}

	@Override
	public void setBlue(int value) throws DeviceException {
		setIntValue(BLUE_VALUE, value);
	}

	@Override
	public int getBlueRbv() throws DeviceException {
		return getIntValue(BLUE_VALUE_RBV);
	}

	@Override
	public int getPositionX() throws DeviceException {
		return getIntValue(POSITION_X);
	}

	@Override
	public void setPositionX(int position) throws DeviceException {
		setIntValue(POSITION_X, position);
	}

	@Override
	public int getPositionXRbv() throws DeviceException {
		return getIntValue(POSITION_X_RBV);
	}

	@Override
	public int getPositionY() throws DeviceException {
		return getIntValue(POSITION_Y);
	}

	@Override
	public void setPositionY(int position) throws DeviceException {
		setIntValue(POSITION_Y, position);
	}

	@Override
	public int getPositionYRbv() throws DeviceException {
		return getIntValue(POSITION_Y_RBV);
	}

	@Override
	public int getCentreX() throws DeviceException {
		return getIntValue(CENTRE_X);
	}

	@Override
	public void setCentreX(int position) throws DeviceException {
		setIntValue(CENTRE_X, position);
	}

	@Override
	public int getCentreXRbv() throws DeviceException {
		return getIntValue(CENTRE_X_RBV);
	}

	@Override
	public int getCentreY() throws DeviceException {
		return getIntValue(CENTRE_Y);
	}

	@Override
	public void setCentreY(int position) throws DeviceException {
		setIntValue(CENTRE_Y, position);
	}

	@Override
	public int getCentreYRbv() throws DeviceException {
		return getIntValue(CENTRE_Y_RBV);
	}

	@Override
	public int getSizeX() throws DeviceException {
		return getIntValue(SIZE_X);
	}

	@Override
	public void setSizeX(int size) throws DeviceException {
		setIntValue(SIZE_X, size);
	}

	@Override
	public int getSizeXRbv() throws DeviceException {
		return getIntValue(SIZE_X_RBV);
	}

	@Override
	public int getSizeY() throws DeviceException {
		return getIntValue(SIZE_Y);
	}

	@Override
	public void setSizeY(int size) throws DeviceException {
		setIntValue(SIZE_Y, size);
	}

	@Override
	public int getSizeYRbv() throws DeviceException {
		return getIntValue(SIZE_Y_RBV);
	}

	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		// Strip any colon(s) from the end of the PV
		this.basePVName = basePVName.replaceAll(":$", "");
	}

	public int getOverlayNumber() {
		return overlayNumber;
	}

	public void setOverlayNumber(int overlayNumber) {
		this.overlayNumber = overlayNumber;
	}

	@Override
	public String toString() {
		return "NDOverlaySimpleImpl [basePVName=" + basePVName + ", overlayNumber=" + overlayNumber + "]";
	}

}
