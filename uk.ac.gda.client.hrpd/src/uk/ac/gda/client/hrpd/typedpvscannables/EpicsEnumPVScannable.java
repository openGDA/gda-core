/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.client.hrpd.typedpvscannables;

import gda.device.DeviceException;
import gda.device.detector.EpicsAreaDetectorConstants;
import gda.device.scannable.PVScannable;
import gda.epics.EpicsConstants;
import gda.epics.LazyPVFactory;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsEnumDataListener;

/**
 * A simple PV scannable that facilitates access to a single EPICS PV of DBR_Enum type. It only supports get and set value
 * from the EPICS PV specified. It is different from the {@link PVScannable} in that it does not monitor PV value
 * changes by design.
 * 
 * for monitoring a single PV of Double type, please see {@link EpicsEnumDataListener}.
 * 
 * NOTE: this class cannot be used in Spring bean to create object - cglib target invocation -> null
 */
public class EpicsEnumPVScannable extends EpicsPVScannable {
	private String type;
	private Class<?> enumType;
	private static final Logger logger=LoggerFactory.getLogger(EpicsEnumPVScannable.class);
	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		super.asynchronousMoveTo(position);
		
		try {
			isBusy=true;
			if (position instanceof EpicsConstants.YesNo) {
				EpicsConstants.YesNo target= (EpicsConstants.YesNo)position;
				LazyPVFactory.newEnumPV(getPvName(), EpicsConstants.YesNo.class).putNoWait(target, this);
			} else if (position instanceof EpicsConstants.DisableEnable) {
				EpicsConstants.DisableEnable target= (EpicsConstants.DisableEnable)position;
				LazyPVFactory.newEnumPV(getPvName(), EpicsConstants.DisableEnable.class).putNoWait(target, this);
			} else if (position instanceof EpicsAreaDetectorConstants.Acquire) {
				EpicsAreaDetectorConstants.Acquire target= (EpicsAreaDetectorConstants.Acquire)position;
				LazyPVFactory.newEnumPV(getPvName(), EpicsAreaDetectorConstants.Acquire.class).putNoWait(target, this);
			} else if (position instanceof EpicsAreaDetectorConstants.ColourMode) {
				EpicsAreaDetectorConstants.ColourMode target= (EpicsAreaDetectorConstants.ColourMode)position;
				LazyPVFactory.newEnumPV(getPvName(), EpicsAreaDetectorConstants.ColourMode.class).putNoWait(target, this);
			} else if (position instanceof EpicsAreaDetectorConstants.DataType) {
				EpicsAreaDetectorConstants.DataType target= (EpicsAreaDetectorConstants.DataType)position;
				LazyPVFactory.newEnumPV(getPvName(), EpicsAreaDetectorConstants.DataType.class).putNoWait(target, this);
			} else if (position instanceof EpicsAreaDetectorConstants.FrameType) {
				EpicsAreaDetectorConstants.FrameType target= (EpicsAreaDetectorConstants.FrameType)position;
				LazyPVFactory.newEnumPV(getPvName(), EpicsAreaDetectorConstants.FrameType.class).putNoWait(target, this);
			} else if (position instanceof EpicsAreaDetectorConstants.ImageMode) {
				EpicsAreaDetectorConstants.ImageMode target= (EpicsAreaDetectorConstants.ImageMode)position;
				LazyPVFactory.newEnumPV(getPvName(), EpicsAreaDetectorConstants.ImageMode.class).putNoWait(target, this);
			} else if (position instanceof EpicsAreaDetectorConstants.ShutterControl) {
				EpicsAreaDetectorConstants.ShutterControl target= (EpicsAreaDetectorConstants.ShutterControl)position;
				LazyPVFactory.newEnumPV(getPvName(), EpicsAreaDetectorConstants.ShutterControl.class).putNoWait(target, this);
			} else if (position instanceof EpicsAreaDetectorConstants.TriggerMode) {
				EpicsAreaDetectorConstants.TriggerMode target= (EpicsAreaDetectorConstants.TriggerMode)position;
				LazyPVFactory.newEnumPV(getPvName(), EpicsAreaDetectorConstants.TriggerMode.class).putNoWait(target, this);
			} else {
				throw new DeviceException("type "+getType()+" is not yet supported. Please extend asynchronousMoveTo(Object) here to support this type.");
			}
		} catch (IOException e) {
			isBusy = false;
			throw new DeviceException(e.getMessage(),e);
		}
		
	}
	@Override
	public Object getPosition() throws DeviceException {
			try {
				return LazyPVFactory.newEnumPV(getPvName(),enumType).get();
			} catch (IOException e) {
				logger.error("Failed to get enum value from {}", getPvName(), e);
				throw new DeviceException(getName(), "Failed to get enum value from "+ getPvName(), e);
			}
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
		if (type.equals(EpicsConstants.YesNo.class.getName())) {
			enumType=EpicsConstants.YesNo.class;
		} else if (type.equals(EpicsConstants.DisableEnable.class.getName())) {
			enumType=EpicsConstants.DisableEnable.class;
		} else if (type.equals("gda.device.detector.EpicsAreaDetectorConstants.Acquire")) {
			enumType=EpicsAreaDetectorConstants.Acquire.class;
		} else if (type.equals(EpicsAreaDetectorConstants.ColourMode.class.getName())) {
			enumType=EpicsAreaDetectorConstants.ColourMode.class;
		} else if (type.equals(EpicsAreaDetectorConstants.DataType.class.getName())) {
			enumType=EpicsAreaDetectorConstants.DataType.class;
		} else if (type.equals(EpicsAreaDetectorConstants.FrameType.class.getName())) {
			enumType=EpicsAreaDetectorConstants.FrameType.class;
		} else if (type.equals(EpicsAreaDetectorConstants.ImageMode.class.getName())) {
			enumType=EpicsAreaDetectorConstants.ImageMode.class;
		} else if (type.equals(EpicsAreaDetectorConstants.ShutterControl.class.getName())) {
			enumType=EpicsAreaDetectorConstants.ShutterControl.class;
		} else if (type.equals(EpicsAreaDetectorConstants.TriggerMode.class.getName())) {
			enumType=EpicsAreaDetectorConstants.TriggerMode.class;
		} else {
			throw new IllegalArgumentException("type '"+getType()+"' is not supported. Fully qualified Enum class name is required here.");
		}
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		if (enumType==null){
			throw new IllegalStateException("'enumType' cannot be null for object '"+getName()+"'.");
		}
		logger.info("{} initialisation completed.", getName());
	}
}
