/*-
 * Copyright © 2011-2013 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.epics;

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

import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.server.ncd.subdetector.ScalingAndOffset;
import uk.ac.gda.server.ncd.subdetector.ScalingAndOffsetParameters;

@CorbaAdapterClass(ScannableAdapter.class)
@CorbaImplClass(ScannableImpl.class)
public class ScalingAndOffsetFromCurrAmp extends ScannableBase implements Scannable, Findable, ScalingAndOffset, MonitorListener {
	private static final Logger logger = LoggerFactory.getLogger(ScalingAndOffsetFromCurrAmp.class);
	
	public class OptionsExhausedException extends DeviceException {
		public OptionsExhausedException(String message) {
			super(message);
		}
	}
	public class MoveProhibitedException extends DeviceException {
		public MoveProhibitedException(String message) {
			super(message);
		}
	}
	private String pvName;
	private String[] labellist;
	private int gain;
	private EpicsController ec;
	private Channel channel; 
	private boolean busy = false;
	boolean fixed = false;
	private Map<Integer, ScalingAndOffsetParameters> gaintosando;
	
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
		if (fixed)
			throw new MoveProhibitedException("gain change prohibited by configuration (scan might be running)");
		
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
	public String rawGetPosition() {
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
		if (gaintosando == null || !gaintosando.containsKey(gain))
			return 1.0;
		
		return gaintosando.get(gain).scaling;
	}

	@Override
	public Double getOffset() {
		if (gaintosando == null || !gaintosando.containsKey(gain))
			return 0.0;
		
		return gaintosando.get(gain).offset;
	}

	@Override
	public String getDescription() {
		if (gaintosando == null || !gaintosando.containsKey(gain))
			return String.format("Unknown gain setting \"%s\": defaulting to raw data.", rawGetPosition());
		
		return String.format("Data scaled by %5.5g and offset by %5.5g for gainsetting \"%s\".", getScaling(), getOffset(), rawGetPosition());
	}
	
	@Override
	public void monitorChanged(MonitorEvent mev) {
		try { 
			DBR dbr = mev.getDBR();
			gain = ((DBR_Enum) dbr).getEnumValue()[0];
		} catch (Exception e) {
			logger.error("unexpected problems processing monitor updates on gain PV", e);
		}
		busy = false;
	}
	
	public String[] getPositions() {
		return labellist;
	}
	public Map<Integer, ScalingAndOffsetParameters> getGaintosando() {
		return gaintosando;
	}
	public void setGaintosando(Map<Integer, ScalingAndOffsetParameters> gaintosando) {
		this.gaintosando = gaintosando;
	}
	
	private boolean looksLikeSimilarSettings(int g1, int g2) {
		int count=0;
		String s1 = labellist[g1];
		String s2 = labellist[g2];
		StringTokenizer st1 = new StringTokenizer(s1);
		StringTokenizer st2 = new StringTokenizer(s2);
		while(st1.hasMoreTokens() && st2.hasMoreTokens()) {
			if (st1.nextToken().equalsIgnoreCase(st2.nextToken()))
				count++;
		}
		if (count >= 2)
			return true;
		return false;
	}
	
	private Integer findNext(int higher) throws DeviceException {

		Integer solution = null;
		if (gaintosando == null || !gaintosando.containsKey(gain))
			throw new DeviceException("not configured well enough to change gain myself");
		Double currentgain = gaintosando.get(gain).scaling;
		for(int g: new int[] {gain-1,gain+1}) {
			if (gaintosando.containsKey(g)) {
				if (currentgain.compareTo(gaintosando.get(g).scaling) * higher > 0) {
					solution = g;
					if (looksLikeSimilarSettings(gain, solution))
						return solution;
				}
			}
		}
		if (solution != null)
			return solution;
		
		throw new OptionsExhausedException("no better gain found");
	}
	
	public void decreaseAmplification() throws DeviceException {
		rawAsynchronousMoveTo(findNext(-1));
	}
	public void increaseAmplification() throws DeviceException {
		rawAsynchronousMoveTo(findNext(1));
	}

	@Override
	public boolean isFixed() {
		return fixed;
	}

	@Override
	public void setFixed(boolean auto) {
		this.fixed = auto;
	}
}