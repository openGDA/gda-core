package uk.ac.gda.sisa.monitor.ui;

import java.io.Serializable;

import gda.factory.Findable;
import uk.ac.gda.client.livecontrol.ControlSet;

public class MonitorConfiguration implements Findable {
	
	private String name;
	private ControlSet analyserControls;
	private ControlSet sampleControls;
	private ControlSet drainCurrentControls;
	private ControlSet cameraRegionControls;
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public ControlSet getAnalyserControls() {
		return analyserControls;
	}
	
	public void setAnalyserControls(ControlSet analyserControls) {
		this.analyserControls = analyserControls;
	}
	
	public ControlSet getSampleControls() {
		return sampleControls;
	}
	
	public void setSampleControls(ControlSet sampleControls) {
		this.sampleControls = sampleControls;
	}
	
	public ControlSet getDrainCurrentControls() {
		return drainCurrentControls;
	}
	
	public void setDrainCurrentControls(ControlSet drainCurrentControls) {
		this.drainCurrentControls = drainCurrentControls;
	}
	
	public ControlSet getCameraRegionControls() {
		return cameraRegionControls;
	}
	
	public void setCameraRegionControls(ControlSet cameraRegionControls) {
		this.cameraRegionControls = cameraRegionControls;
	}
	
	public boolean hasAnalyserControls() {
		return getAnalyserControls() != null;
	}
	
	public boolean hasSampleControls() {
		return getSampleControls() != null;
	}

}
