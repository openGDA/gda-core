/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.ionchambers;

import java.io.Serializable;
import java.net.URL;
import java.util.List;

public class IonChamberBean implements Serializable {
	
	static public final URL mappingURL = IonChambersBean.class.getResource("ionChamberMapping.xml");
	static public final URL schemaURL = IonChambersBean.class.getResource("ionChamberMapping.xsd");
	
	
	private String ionChamberScannable;
	private String ionChamberName;
	private String amplifierScannable;
	private String sensitivity;
	private String voltageScannable;
	private String gasTypesAvailable;
	private String gasTypeSelected;
	private boolean flush;
	private double absorption;
	private double totalPressure;
	private double chamberLength;
	private double fill1Period;
	private double fill2Period;
	
	public IonChamberBean(){
		absorption=0;
		ionChamberScannable="";
		ionChamberName="";
		amplifierScannable="";
		sensitivity="";
		voltageScannable="";
		gasTypesAvailable="";
		gasTypeSelected="";
		flush=false;
		totalPressure=0;
		chamberLength=0;
		fill1Period=0;
		fill2Period=0;
	}
	
	public void clear(){
		absorption=0;
		ionChamberScannable="";
		ionChamberName="";
		amplifierScannable="";
		sensitivity="";
		voltageScannable="";
		gasTypesAvailable="";
		gasTypeSelected="";
		flush=false;
		totalPressure=0;
		chamberLength=0;
		fill1Period=0;
		fill2Period=0;
	}

	public double getAbsorption() {
		return absorption;
	}

	public void setAbsorption(double absorption) {
		this.absorption = absorption;
	}

	public String getIonChamberScannable() {
		return ionChamberScannable;
	}

	public void setIonChamberScannable(String ionChamberScannable) {
		this.ionChamberScannable = ionChamberScannable;
	}

	public String getIonChamberName() {
		return ionChamberName;
	}

	public void setIonChamberName(String ionChamberName) {
		this.ionChamberName = ionChamberName;
	}

	public String getAmplifierScannable() {
		return amplifierScannable;
	}

	public void setAmplifierScannable(String amplifierScannable) {
		this.amplifierScannable = amplifierScannable;
	}

	public String getSensitivity() {
		return sensitivity;
	}

	public void setSensitivity(String sensitivity) {
		this.sensitivity = sensitivity;
	}

	public String getVoltageScannable() {
		return voltageScannable;
	}

	public void setVoltageScannable(String voltageScannable) {
		this.voltageScannable = voltageScannable;
	}

	public String getGasTypesAvailable() {
		return gasTypesAvailable;
	}

	public void setGasTypesAvailable(String gasTypesAvailable) {
		this.gasTypesAvailable = gasTypesAvailable;
	}

	public String getGasTypeSelected() {
		return gasTypeSelected;
	}

	public void setGasTypeSelected(String gasTypeSelected) {
		this.gasTypeSelected = gasTypeSelected;
	}

	public boolean getFlush() {
		return flush;
	}

	public void setFlush(boolean flush) {
		this.flush = flush;
	}

	public double getTotalPressure() {
		return totalPressure;
	}

	public void setTotalPressure(double totalPressure) {
		this.totalPressure = totalPressure;
	}

	public double getChamberLength() {
		return chamberLength;
	}

	public void setChamberLength(double chamberLength) {
		this.chamberLength = chamberLength;
	}

	public double getFill1Period() {
		return fill1Period;
	}

	public void setFill1Period(double fill1Period) {
		this.fill1Period = fill1Period;
	}

	public double getFill2Period() {
		return fill2Period;
	}

	public void setFill2Period(double fill2Period) {
		this.fill2Period = fill2Period;
	}

	public static URL getMappingurl() {
		return mappingURL;
	}

	public static URL getSchemaurl() {
		return schemaURL;
	}

}
