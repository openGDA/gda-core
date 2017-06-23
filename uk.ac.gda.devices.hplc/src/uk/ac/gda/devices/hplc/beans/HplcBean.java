package uk.ac.gda.devices.hplc.beans;

import java.util.Map;

import gda.jython.InterfaceProvider;
import gda.jython.batoncontrol.ClientDetails;
import uk.ac.gda.devices.hatsaxs.beans.LocationBean;
import uk.ac.gda.util.beans.xml.XMLRichBean;

public class HplcBean implements XMLRichBean {

	private static final long serialVersionUID = 2999210681645575696L;
	public static final String DEFAULT_HPLC_MODE = "HPLC";

	public static Map<String, Boolean> MODES;
	public static void setModes(Map<String, Boolean> modes) {
		MODES = modes;
	}
	private LocationBean location = new LocationBean(HplcSessionBean.HPLC_PLATES);
	private String sampleName = "Sample";
	private double concentration;
	private double molecularWeight;
	private double timePerFrame;
	private String visit;
	private String username;
	private String comment = "";
	private String buffers = "";
	private String mode = DEFAULT_HPLC_MODE;
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
	public String getVisit() {
		return visit;
	}
	public void setVisit(String visit) {
		//TODO: remove permissions logic from bean
		if (!(isStaff || this.visit.equals(visit))) {
			throw new UnsupportedOperationException("User does not have permission to change username/visit");
		}
		this.visit = visit;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		//TODO: remove permissions logic from bean
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
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		if (mode == null || !MODES.containsKey(mode = mode.toUpperCase())) {
			throw new IllegalArgumentException("Mode " + mode + " is not valid");
		}
		this.mode = mode;
	}

	public void clear() {

	}

}
