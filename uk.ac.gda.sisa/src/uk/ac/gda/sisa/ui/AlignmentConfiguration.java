package uk.ac.gda.sisa.ui;

import java.util.ArrayList;
import java.util.List;

import gda.factory.Findable;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.livecontrol.ControlSet;
import uk.ac.gda.devices.vgscienta.IVGScientaAnalyserRMI;

public class AlignmentConfiguration implements Findable {
	
	private String name;
	private ControlSet analyserControls;
	private ControlSet sampleControls;
	private CameraControl analyserEavControl;
	private List<AlignmentStat> alignmentStats = new ArrayList<AlignmentStat>();
	
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
	
	public boolean hasAnalyserControls() {
		return getAnalyserControls() != null;
	}
	
	public boolean hasSampleControls() {
		return getSampleControls() != null;
	}

	public CameraControl getAnalyserEavControl() {
		return analyserEavControl;
	}

	public void setAnalyserEavControl(CameraControl analyserEavControl) {
		this.analyserEavControl = analyserEavControl;
	}

	public List<AlignmentStat> getAlignmentStats() {
		return alignmentStats;
	}

	public void setAlignmentStats(List<AlignmentStat> alignmentStats) {
		this.alignmentStats = alignmentStats;
	}
}
