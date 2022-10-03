package uk.ac.gda.sisa.feedback.ui;

import gda.factory.Findable;
import uk.ac.gda.client.livecontrol.ControlSet;

public class FeedbackControls implements Findable {

	private String name;
	private ControlSet pressureControls;
	private ControlSet temperatureControls;
	private ControlSet ringStatusControls;
	private ControlSet gateValveControls;
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	public ControlSet getPressureControls() {
		return pressureControls;
	}
	
	public void setPressureControls(ControlSet pressureControls) {
		this.pressureControls = pressureControls;
	}
	
	public ControlSet getTemperatureControls() {
		return temperatureControls;
	}
	
	public void setTemperatureControls(ControlSet temperatureControls) {
		this.temperatureControls = temperatureControls;
	}

	public ControlSet getGateValveControls() {
		return gateValveControls;
	}

	public void setGateValveControls(ControlSet gateValveControls) {
		this.gateValveControls = gateValveControls;
	}

	public ControlSet getRingStatusControls() {
		return ringStatusControls;
	}

	public void setRingStatusControls(ControlSet ringStatusControls) {
		this.ringStatusControls = ringStatusControls;
	}
	
	
	
}
