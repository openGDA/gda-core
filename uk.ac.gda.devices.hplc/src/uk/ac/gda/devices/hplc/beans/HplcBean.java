package uk.ac.gda.devices.hplc.beans;

import gda.jython.InterfaceProvider;
import gda.jython.batoncontrol.ClientDetails;
import uk.ac.gda.beans.IRichBean;
import uk.ac.gda.devices.bssc.beans.LocationBean;

public class HplcBean implements IRichBean {

	private static final long serialVersionUID = 2999210681645575696L;
	LocationBean location = new LocationBean(HplcSessionBean.HPLC_PLATES);
	String sampleName = "Sample";
	double concentration;
	double molecularWeight;
	double timePerFrame;
	int frames;
	String visit;
	String username;
	String comment = "";
	String buffers = "";
	private boolean isStaff;

	public HplcBean() {
		ClientDetails myDetails = InterfaceProvider.getBatonStateProvider().getMyDetails();
		this.visit = myDetails.getVisitID();
		this.username = myDetails.getUserID();
		this.isStaff = myDetails.getAuthorisationLevel() >= 3;
	}
	public LocationBean getLocation() {
		return location;
	}
	public void setLocation(LocationBean location) {
		location.setConfig(HplcSessionBean.HPLC_PLATES);
		if (!location.isValid()) {
			throw new IllegalArgumentException("Location is not valid");
		}
		this.location = location;
	}
	public String getSampleName() {
		return sampleName;
	}
	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}
	public double getConcentration() {
		return concentration;
	}
	public void setConcentration(double concentration) {
		this.concentration = concentration;
	}
	public double getMolecularWeight() {
		return molecularWeight;
	}
	public void setMolecularWeight(double molecularWeight) {
		this.molecularWeight = molecularWeight;
	}
	public double getTimePerFrame() {
		return timePerFrame;
	}
	public void setTimePerFrame(double timePerFrame) {
		this.timePerFrame = timePerFrame;
	}
	public int getFrames() {
		return frames;
	}
	public void setFrames(int frames) {
		this.frames = frames;
	}
	public String getVisit() {
		return visit;
	}
	public void setVisit(String visit) {
		if (!(isStaff || this.visit.equals(visit))) {
			throw new UnsupportedOperationException("User does not have permission to change username/visit");
		}
		this.visit = visit;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		if (!(isStaff || this.username.equals(username))) {
			throw new UnsupportedOperationException("User does not have permission to change username/visit");
		}
		this.username = username;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getBuffers() {
		return buffers;
	}
	public void setBuffers(String buffers) {
		this.buffers = buffers;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

}
