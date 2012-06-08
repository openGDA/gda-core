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

package gda.device.detector.areadetector.v17.impl;

import gda.device.DeviceException;
import gda.device.detector.areadetector.IPVProvider;
import gda.device.detector.areadetector.v17.ADDriverProsilica;
import gda.epics.connection.EpicsController;

import org.springframework.beans.factory.InitializingBean;

public class ADDriverProsilicaImpl implements ADDriverProsilica, InitializingBean {

	private static final String PS_FRAMES_DROPPED_RBV = "PSFramesDropped_RBV";
	private static final String PS_FRAMES_COMPLETED_RBV = "PSFramesCompleted_RBV";
	private static final String PS_PACKETS_ERRONEOUS_RBV = "PSPacketsErroneous_RBV";
	private static final String PS_PACKETS_MISSED_RBV = "PSPacketsMissed_RBV";
	private static final String PS_PACKETS_RECEIVED_RBV = "PSPacketsReceived_RBV";
	private static final String PS_PACKETS_REQUESTED_RBV = "PSPacketsRequested_RBV";
	private static final String PS_PACKETS_RESENT_RBV = "PSPacketsResent_RBV";
	private static final String PS_BAD_FRAME_COUNTER_RBV = "PSBadFrameCounter_RBV";
	private static final String PS_FRAME_RATE_RBV = "PSFrameRate_RBV";
	private static final String PS_FILTER_VERSION_RBV = "PSFilterVersion_RBV";
	private static final String PS_DRIVER_TYPE_RBV = "PSDriverType_RBV";
	private static final String STROBE1_DURATION = "Strobe1Duration";
	private static final String STROBE1_DURATION_RBV = "Strobe1Duration_RBV";
	private static final String STROBE1_DELAY = "Strobe1Delay";
	private static final String STROBE1_DELAY_RBV = "Strobe1Delay_RBV";
	private static final String STROBE1_CTL_DURATION = "Strobe1CtlDuration";
	private static final String STROBE1_CTL_DURATION_RBV = "Strobe1CtlDuration_RBV";
	private static final String STROBE1_MODE = "Strobe1Mode";
	private static final String STROBE1_MODE_RBV = "Strobe1Mode_RBV";
	private static final String SYNC_OUT1_INVERT = "SyncOut1Invert";
	private static final String SYNC_OUT2_INVERT = "SyncOut1Invert";
	private static final String SYNC_OUT3_INVERT = "SyncOut1Invert";
	private static final String SYNC_OUT1_INVERT_RBV = "SyncOut1Invert_RBV";
	private static final String SYNC_OUT2_INVERT_RBV = "SyncOut2Invert_RBV";
	private static final String SYNC_OUT3_INVERT_RBV = "SyncOut3Invert_RBV";
	private static final String SYNC_OUT1_LEVEL = "SyncOut1Level";
	private static final String SYNC_OUT2_LEVEL = "SyncOut1Level";
	private static final String SYNC_OUT3_LEVEL = "SyncOut1Level";
	private static final String SYNC_OUT1_LEVEL_RBV = "SyncOut1Level_RBV";
	private static final String SYNC_OUT2_LEVEL_RBV = "SyncOut2Level_RBV";
	private static final String SYNC_OUT3_LEVEL_RBV = "SyncOut3Level_RBV";
	private static final String SYNC_OUT1_MODE = "SyncOut1Mode";
	private static final String SYNC_OUT2_MODE = "SyncOut2Mode";
	private static final String SYNC_OUT3_MODE = "SyncOut3Mode";
	private static final String SYNC_OUT1_MODE_RBV = "SyncOut1Mode_RBV";
	private static final String SYNC_OUT2_MODE_RBV = "SyncOut2Mode_RBV";
	private static final String SYNC_OUT3_MODE_RBV = "SyncOut3Mode_RBV";
	private static final String SYNC_IN1_LEVEL_RBV = "SyncIn1Level_RBV";
	private static final String SYNC_IN2_LEVEL_RBV = "SyncIn2Level_RBV";
	
	SimpleChannelProvider simpleChannelProvider;

	private IPVProvider pvProvider;

	public void setPvProvider(IPVProvider pvProvider) {
		this.pvProvider = pvProvider;
	}
	
	private EpicsController epicsController;

	EpicsController getEpicsController() {
		if (epicsController == null) {
			epicsController = EpicsController.getInstance();
		}
		return epicsController;
	}	

	SimpleChannelProvider getSimpleChannelProvider(){
		if(simpleChannelProvider == null){
			simpleChannelProvider = new SimpleChannelProvider(getEpicsController(),pvProvider);
		}
		return simpleChannelProvider;
	}
	static private SYNCIN_LEVEL[] syncInLevels = new SYNCIN_LEVEL[]{SYNCIN_LEVEL.LOW, SYNCIN_LEVEL.HIGH};
	SYNCIN_LEVEL getSyncInLevel(String pv) throws DeviceException {
		try {
			short val = getEpicsController().cagetShort((getSimpleChannelProvider().createChannel(pv)));
			return syncInLevels[val];
		} catch (Exception e) {
			throw new DeviceException("Error getting value for " + pv, e);
		}
	}
	
	@Override
	public SYNCIN_LEVEL getSyncIn1Level() throws DeviceException {
		return getSyncInLevel(SYNC_IN1_LEVEL_RBV);
	}

	@Override
	public SYNCIN_LEVEL getSyncIn2Level() throws DeviceException {
		return getSyncInLevel(SYNC_IN2_LEVEL_RBV);
	}

	static private SYNCOUT_MODE[] syncOutModes = new SYNCOUT_MODE[]{SYNCOUT_MODE.GPO, SYNCOUT_MODE.ACQ_TRIG_READY,
		SYNCOUT_MODE.FRAME_TRIG_READY, SYNCOUT_MODE.FRAME_TRIGGER, SYNCOUT_MODE.IMAGING, SYNCOUT_MODE.ACQUIRING, SYNCOUT_MODE.SYNCIN1,
		SYNCOUT_MODE.SYNCIN2, SYNCOUT_MODE.SYNCIN3, SYNCOUT_MODE.SYNCIN4, SYNCOUT_MODE.STROBE1, SYNCOUT_MODE.STROBE2,
		SYNCOUT_MODE.STROBE3, SYNCOUT_MODE.STROBE4};


	SYNCOUT_MODE getSyncOutMode(String pv) throws DeviceException {
		try {
			short val = getEpicsController().cagetShort((getSimpleChannelProvider().createChannel(pv)));
			return syncOutModes[val];
		} catch (Exception e) {
			throw new DeviceException("Error getting value for " + pv, e);
		}
	}
	
	@Override
	public SYNCOUT_MODE getSyncOut1Mode() throws DeviceException {
		return getSyncOutMode(SYNC_OUT1_MODE_RBV);
	}

	@Override
	public SYNCOUT_MODE getSyncOut2Mode() throws DeviceException {
		return getSyncOutMode(SYNC_OUT2_MODE_RBV);
	}

	@Override
	public SYNCOUT_MODE getSyncOut3Mode() throws DeviceException {
		return getSyncOutMode(SYNC_OUT3_MODE_RBV);
	}

	void setSyncOutMode(String pv, SYNCOUT_MODE mode) throws DeviceException {
		try {
			for( int i=0; i< syncOutModes.length;i++){
				if( mode == syncOutModes[i]){
					getEpicsController().caput(getSimpleChannelProvider().createChannel(pv),i);
					return;
				}
			}
		} catch (Exception e) {
			throw new DeviceException("Error setting value for " + pv, e);
		}
	}
	@Override
	public void setSyncOut1Mode(SYNCOUT_MODE mode) throws DeviceException {
		setSyncOutMode(SYNC_OUT1_MODE, mode );
	}
	
	@Override
	public void setSyncOut2Mode(SYNCOUT_MODE mode) throws DeviceException {
		setSyncOutMode(SYNC_OUT2_MODE, mode );
	}
	
	
	@Override
	public void setSyncOut3Mode(SYNCOUT_MODE mode) throws DeviceException {
		setSyncOutMode(SYNC_OUT3_MODE, mode );
	}

	boolean getSyncOutLevel(String pv) throws DeviceException {
		try {
			short val = getEpicsController().cagetByte(getSimpleChannelProvider().createChannel(pv));
			return val == 0;
		} catch (Exception e) {
			throw new DeviceException("Error getting value for " + pv, e);
		}
	}
	
	@Override
	public boolean getSyncOut1Level() throws DeviceException {
		return getSyncOutLevel(SYNC_OUT1_LEVEL_RBV);
	}

	@Override
	public boolean getSyncOut2Level() throws DeviceException {
		return getSyncOutLevel(SYNC_OUT2_LEVEL_RBV);
	}

	@Override
	public boolean getSyncOut3Level() throws DeviceException {
		return getSyncOutLevel(SYNC_OUT3_LEVEL_RBV);
	}

	void setSyncOutLevel(String pv, boolean level) throws DeviceException {
		try {
			getEpicsController().caput(getSimpleChannelProvider().createChannel(pv),level ? 1:0);
		} catch (Exception e) {
			throw new DeviceException("Error setting value for " + pv, e);
		}
	}

	@Override
	public void setSyncOut1Level(boolean level) throws DeviceException {
		setSyncOutLevel(SYNC_OUT1_LEVEL, level);
	}
	@Override
	public void setSyncOut2Level(boolean level) throws DeviceException {
		setSyncOutLevel(SYNC_OUT2_LEVEL, level);
	}

	@Override
	public void setSyncOut3Level(boolean level) throws DeviceException {
		setSyncOutLevel(SYNC_OUT3_LEVEL, level);
	}


	
	boolean getSyncOutInvert(String pv) throws DeviceException {
		try {
			short val = getEpicsController().cagetByte(getSimpleChannelProvider().createChannel(pv));
			return val == 0;
		} catch (Exception e) {
			throw new DeviceException("Error getting value for " + pv, e);
		}
	}
	

	@Override
	public boolean getSyncOut1Invert() throws DeviceException {
		return getSyncOutInvert(SYNC_OUT1_INVERT_RBV);
	}
	@Override
	public boolean getSyncOut2Invert() throws DeviceException {
		return getSyncOutInvert(SYNC_OUT2_INVERT_RBV);
	}
	@Override
	public boolean getSyncOut3Invert() throws DeviceException {
		return getSyncOutInvert(SYNC_OUT3_INVERT_RBV);
	}

	void setSyncOutInvert(String pv, boolean invert) throws DeviceException {
		try {
			getEpicsController().caput(getSimpleChannelProvider().createChannel(pv),invert ? 1:0);
		} catch (Exception e) {
			throw new DeviceException("Error setting value for " + pv, e);
		}
	}

	
	@Override
	public void setSyncOut1Invert(boolean invert) throws DeviceException {
		setSyncOutInvert(SYNC_OUT1_INVERT, invert);
	}

	@Override
	public void setSyncOut2Invert(boolean invert) throws DeviceException {
		setSyncOutInvert(SYNC_OUT2_INVERT, invert);
	}

	@Override
	public void setSyncOut3Invert(boolean invert) throws DeviceException {
		setSyncOutInvert(SYNC_OUT3_INVERT, invert);
	}
	
	static private STROBE_MODE[] strobeModes = new STROBE_MODE[]{STROBE_MODE.ACQ_TRIG_READY, STROBE_MODE.FRAME_TRIG_READY,
		STROBE_MODE.FRAME_TRIGGER, STROBE_MODE.EXPOSING, STROBE_MODE.FRAME_READOUT, STROBE_MODE.ACQUIRING, STROBE_MODE.SYNCIN1,
		STROBE_MODE.SYNCIN2, STROBE_MODE.SYNCIN3, STROBE_MODE.SYNCIN4};


	@Override
	public STROBE_MODE getStrobeMode() throws DeviceException {
		try {
			short val = getEpicsController().cagetShort((getSimpleChannelProvider().createChannel(STROBE1_MODE_RBV)));
			return strobeModes[val];
		} catch (Exception e) {
			throw new DeviceException("Error getting value for " + STROBE1_MODE_RBV, e);
		}
	}
	
	@Override
	public void setStrobeMode(STROBE_MODE mode) throws DeviceException {
		try {
			for( int i=0; i< strobeModes.length;i++){
				if( mode == strobeModes[i]){
					getEpicsController().caput(getSimpleChannelProvider().createChannel(STROBE1_MODE),i);
					return;
				}
			}
		} catch (Exception e) {
			throw new DeviceException("Error setting value for " + STROBE1_MODE, e);
		}

	}


	@Override
	public void setStrobeCtlDuration(boolean yes_no) throws DeviceException {
		try {
			getEpicsController().caput(getSimpleChannelProvider().createChannel(STROBE1_CTL_DURATION),yes_no ? 1:0);
		} catch (Exception e) {
			throw new DeviceException("Error setting value for " + STROBE1_CTL_DURATION, e);
		}

	}

	@Override
	public boolean getStrobeCtlDuration() throws DeviceException {
		try {
			short val = getEpicsController().cagetByte(getSimpleChannelProvider().createChannel(STROBE1_CTL_DURATION_RBV));
			return val == 0;
		} catch (Exception e) {
			throw new DeviceException("Error getting value for " + STROBE1_CTL_DURATION_RBV, e);
		}
	}

	@Override
	public void setStrobeDuration(double duration) throws DeviceException {
		try {
			getEpicsController().caput(getSimpleChannelProvider().createChannel(STROBE1_DURATION),duration);
		} catch (Exception e) {
			throw new DeviceException("Error setting value for " + STROBE1_DURATION, e);
		}

	}

	@Override
	public double getStrobeDuration() throws DeviceException {
		try {
			return getEpicsController().cagetDouble(getSimpleChannelProvider().createChannel(STROBE1_DURATION_RBV));
		} catch (Exception e) {
			throw new DeviceException("Error getting value for " + STROBE1_DURATION_RBV, e);
		}
	}

	@Override
	public void setStrobeDelay(double delay) throws DeviceException {
		try {
			getEpicsController().caput(getSimpleChannelProvider().createChannel(STROBE1_DELAY),delay);
		} catch (Exception e) {
			throw new DeviceException("Error setting value for " + STROBE1_DELAY, e);
		}
	}

	@Override
	public double getStrobeDelay() throws DeviceException {
		try {
			return getEpicsController().cagetDouble(getSimpleChannelProvider().createChannel(STROBE1_DELAY_RBV));
		} catch (Exception e) {
			throw new DeviceException("Error getting value for " + STROBE1_DELAY_RBV, e);
		}
	}

	@Override
	public String getDriverType() throws DeviceException {
		try {
			return getEpicsController().cagetString(getSimpleChannelProvider().createChannel(PS_DRIVER_TYPE_RBV));
		} catch (Exception e) {
			throw new DeviceException("Error getting value for " + PS_DRIVER_TYPE_RBV, e);
		}
	}

	@Override
	public String getFilterVersion() throws DeviceException {
		try {
			return getEpicsController().cagetString(getSimpleChannelProvider().createChannel(PS_FILTER_VERSION_RBV));
		} catch (Exception e) {
			throw new DeviceException("Error getting value for " + PS_FILTER_VERSION_RBV, e);
		}
	}

	@Override
	public double getFrameRate() throws DeviceException {
		try {
			return getEpicsController().cagetDouble(getSimpleChannelProvider().createChannel(PS_FRAME_RATE_RBV));
		} catch (Exception e) {
			throw new DeviceException("Error getting value for " + PS_FRAME_RATE_RBV, e);
		}
	}

	@Override
	public double getFramesCompleted()  throws DeviceException {
		try {
			return getEpicsController().cagetDouble(getSimpleChannelProvider().createChannel(PS_FRAMES_COMPLETED_RBV));
		} catch (Exception e) {
			throw new DeviceException("Error getting value for " + PS_FRAMES_COMPLETED_RBV, e);
		}
	}

	@Override
	public double getFramesDropped()  throws DeviceException {
		try {
			return getEpicsController().cagetDouble(getSimpleChannelProvider().createChannel(PS_FRAMES_DROPPED_RBV));
		} catch (Exception e) {
			throw new DeviceException("Error getting value for " + PS_FRAMES_DROPPED_RBV, e);
		}
	}

	@Override
	public double getPacketsErroneuos()  throws DeviceException {
		try {
			return getEpicsController().cagetDouble(getSimpleChannelProvider().createChannel(PS_PACKETS_ERRONEOUS_RBV));
		} catch (Exception e) {
			throw new DeviceException("Error getting value for " + PS_PACKETS_ERRONEOUS_RBV, e);
		}
	}

	@Override
	public double getPacketsMissed()  throws DeviceException {
		try {
			return getEpicsController().cagetDouble(getSimpleChannelProvider().createChannel(PS_PACKETS_MISSED_RBV));
		} catch (Exception e) {
			throw new DeviceException("Error getting value for " + PS_PACKETS_MISSED_RBV, e);
		}
	}

	@Override
	public double getPacketsReceived()  throws DeviceException {
		try {
			return getEpicsController().cagetDouble(getSimpleChannelProvider().createChannel(PS_PACKETS_RECEIVED_RBV));
		} catch (Exception e) {
			throw new DeviceException("Error getting value for " + PS_PACKETS_RECEIVED_RBV, e);
		}
	}

	@Override
	public double getPacketsRequested()  throws DeviceException {
		try {
			return getEpicsController().cagetDouble(getSimpleChannelProvider().createChannel(PS_PACKETS_REQUESTED_RBV));
		} catch (Exception e) {
			throw new DeviceException("Error getting value for " + PS_PACKETS_REQUESTED_RBV, e);
		}
	}

	@Override
	public double getPacketsResent()  throws DeviceException {
		try {
			return getEpicsController().cagetDouble(getSimpleChannelProvider().createChannel(PS_PACKETS_RESENT_RBV));
		} catch (Exception e) {
			throw new DeviceException("Error getting value for " + PS_PACKETS_RESENT_RBV, e);
		}
	}

	@Override
	public double getBadFrameCounter() throws DeviceException {
		try {
			return getEpicsController().cagetDouble(getSimpleChannelProvider().createChannel( PS_BAD_FRAME_COUNTER_RBV));
		} catch (Exception e) {
			throw new DeviceException("Error getting value for " + PS_BAD_FRAME_COUNTER_RBV, e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (pvProvider == null) {
			throw new IllegalArgumentException("pvProvider is null");
		}
	}

}
