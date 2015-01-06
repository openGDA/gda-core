package org.opengda.lde.scannables;

import java.io.IOException;

import gda.device.DeviceException;
import gda.epics.LazyPVFactory;
import gda.device.ISpin;
import gda.device.scannable.ScannableBase;

public class Spinner extends ScannableBase implements ISpin {
	private String OnOffPV;
	private String voltagePV;
	public enum ONOFF {
		OFF,
		ON
	}
	@Override
	public Object getPosition() throws DeviceException {
		return getState();
	}
	
	@Override
	public boolean isBusy() throws DeviceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void on() throws DeviceException {
		try {
			LazyPVFactory.newEnumPV(getOnOffPV(), ONOFF.class).putWait(ONOFF.ON);
		} catch (IOException e) {
			throw new DeviceException("Failed to put 1 (ON) to PV: "+getOnOffPV());
		}
	}

	@Override
	public void off() throws DeviceException {
		try {
			LazyPVFactory.newEnumPV(getOnOffPV(), ONOFF.class).putWait(ONOFF.OFF);
		} catch (IOException e) {
			throw new DeviceException("Failed to put 0 (OFF) to PV: "+getOnOffPV());
		}
	}

	@Override
	public void setSpeed(double voltage) throws DeviceException {
		try {
			LazyPVFactory.newDoublePV(voltagePV).putWait(voltage);
		} catch (IOException e) {
			throw new DeviceException("Failed to put voltage to PV: "+voltagePV);
		}
	}

	@Override
	public double getSpeed() throws DeviceException {
		try {
			return LazyPVFactory.newDoublePV(voltagePV).get();
		} catch (IOException e) {
			throw new DeviceException("Failed to get voltage from PV: "+voltagePV);
		}
	}

	@Override
	public String getState() throws DeviceException {
		try {
			return LazyPVFactory.newEnumPV(getOnOffPV(), ONOFF.class).get().name();
		} catch (IOException e) {
			throw new DeviceException("Failed to get spin state from PV: "+getOnOffPV());
		}
	}

	public String getOnOffPV() {
		return OnOffPV;
	}

	public void setOnOffPV(String onOffPV) {
		OnOffPV = onOffPV;
	}

	public String getVoltagePV() {
		return voltagePV;
	}

	public void setVoltagePV(String voltagePV) {
		this.voltagePV = voltagePV;
	}

}
