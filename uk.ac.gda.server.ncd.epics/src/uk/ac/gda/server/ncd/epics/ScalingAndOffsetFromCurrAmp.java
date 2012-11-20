package uk.ac.gda.server.ncd.epics;
/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.server.ncd.subdetector.ScalingAndOffset;

@CorbaAdapterClass(ScannableAdapter.class)
@CorbaImplClass(ScannableImpl.class)
public class ScalingAndOffsetFromCurrAmp extends ScannableBase implements Scannable, Findable, ScalingAndOffset, MonitorListener {
	
	private static final Logger logger = LoggerFactory.getLogger(ScalingAndOffsetFromCurrAmp.class);  		
	private String pvName;
	public static final String[] epicsnamelist = {".ZRST", ".ONST", ".TWST", ".THST", ".FRST", ".FVST", ".SXST", ".SVST", ".EIST", ".NIST", ".TEST", ".ELST", ".TVST", ".TTST", ".FFST"};
	private String[] labellist;
	private int gain;
	private EpicsController ec;
	private Channel channel; 
	private boolean busy = false;
	
	@Override
	public void configure() throws FactoryException {
		super.configure();
		
		ec = EpicsController.getInstance();
		
		try {
			channel = ec.createChannel(pvName);
			ec.setMonitor(channel, this);
			labellist = ec.cagetLabels(channel);
		} catch (Exception e) {
			throw new FactoryException("error connecting to "+getName(), e);
		}
		
	}
	@Override
	public boolean isBusy() throws DeviceException {
		return busy;
	}
	
	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		int newgain=0;
		
		if (position instanceof Number) {
			newgain = ((Number) position).intValue();
		} else {
			newgain = str2int(position.toString());
		}
		
		try {
			busy = true;
			ec.caput(channel, newgain);
		} catch (Exception e) {
			throw new DeviceException(getName() +" exception in rawAsynchronousMoveTo", e);
		}
	}
	
	@Override
	public Object rawGetPosition() throws DeviceException {
		return labellist[gain];
	}
	
	public int str2int(String setting){
		for (int i = 0; i < labellist.length; i++) {
			if (labellist[i].equalsIgnoreCase(setting))
				return i;
		}
		return -1;
	}
	
	public String getPvName() {
		return pvName;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	@Override
	public Double getScaling() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getOffset() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void monitorChanged(MonitorEvent mev) {
		DBR dbr = mev.getDBR();
		if (dbr.isENUM()) {
			gain = ((DBR_Enum) dbr).getEnumValue()[0];
		} else {
			logger.error("Gain does not return Enum type.");
		}
		busy = false;
	}
	
	public String[] getPositions() {
		return labellist;
	}
}