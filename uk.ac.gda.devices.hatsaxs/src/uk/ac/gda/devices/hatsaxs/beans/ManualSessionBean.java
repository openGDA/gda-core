package uk.ac.gda.devices.hatsaxs.beans;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import uk.ac.gda.util.beans.xml.XMLHelpers;
import uk.ac.gda.util.beans.xml.XMLRichBean;

public class ManualSessionBean implements XMLRichBean {

	private static final long serialVersionUID = -2871394446889967499L;
	public static final URL mappingURL = ManualSessionBean.class.getResource("ManualMapping.xml");
	public static final URL schemaURL  = ManualSessionBean.class.getResource("ManualMapping.xsd");
	
	List<ManualBean> measurements = new ArrayList<>();
	private transient Gson gson = new Gson();


	public static ManualSessionBean createFromXML(String filename) throws Exception {
		return XMLHelpers.createFromXML(mappingURL, ManualSessionBean.class, schemaURL, filename);
	}

	public static void writeToXML(ManualSessionBean bean, String filename) throws Exception {
		XMLHelpers.writeToXML(mappingURL, bean, filename);
	}

	public List<ManualBean> getMeasurements() {
		return measurements;
	}

	public void setMeasurements(List<ManualBean> measurements) {
		this.measurements = measurements;
	}
	public void clear() {
		measurements.clear();
	}

	public String asJson() {
		return gson.toJson(measurements);
	}
}