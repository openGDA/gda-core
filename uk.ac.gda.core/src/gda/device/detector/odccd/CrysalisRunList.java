/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.detector.odccd;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the parameters for a Crysalis data collection. It includes an array of CrysalisRuns.
 */
public class CrysalisRunList {

	String name = "";
	int dwtotalnumofframes;
	int wreferenceframefrequency;
	int wversioninfo;
	int inumofruns;
	int wisreferenceframes;
	int inumofreferenceruns;
	String cexperimentname = "";
	String cexperimentdir = "";
	String runFolder = ""; // folder on Crysalis machine containing the run file
	String runFile = ""; // name of run file used to hold the run data

	ArrayList<CrysalisRun> runlist;

	/**
	 * 
	 */
	static public URL mappingURL = CrysalisRunList.class.getResource("CrysalisRunListMapping.xml");
	/**
	 * 
	 */
	static public URL schemaURL = null; // CrysalisRunList.class.getResource("CrysalisRunListSchema.xsd");

	/**
	 * constructor
	 */
	public CrysalisRunList() {
		runlist = new ArrayList<CrysalisRun>();
	}

	/**
	 * @param run
	 */
	public void addRun(CrysalisRun run) {
		runlist.add(run);
	}

	/**
	 * @return value
	 */
	public List<CrysalisRun> getRunList() {
		return runlist;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the dwtotalnumofframes
	 */
	public int getDwtotalnumofframes() {
		return dwtotalnumofframes;
	}

	/**
	 * @param dwtotalnumofframes the dwtotalnumofframes to set
	 */
	public void setDwtotalnumofframes(int dwtotalnumofframes) {
		this.dwtotalnumofframes = dwtotalnumofframes;
	}

	/**
	 * @return the wreferenceframefrequency
	 */
	public int getWreferenceframefrequency() {
		return wreferenceframefrequency;
	}

	/**
	 * @param wreferenceframefrequency the wreferenceframefrequency to set
	 */
	public void setWreferenceframefrequency(int wreferenceframefrequency) {
		this.wreferenceframefrequency = wreferenceframefrequency;
	}

	/**
	 * @return the wversioninfo
	 */
	public int getWversioninfo() {
		return wversioninfo;
	}

	/**
	 * @param wversioninfo the wversioninfo to set
	 */
	public void setWversioninfo(int wversioninfo) {
		this.wversioninfo = wversioninfo;
	}

	/**
	 * @return the inumofruns
	 */
	public int getInumofruns() {
		return inumofruns;
	}

	/**
	 * @param inumofruns the inumofruns to set
	 */
	public void setInumofruns(int inumofruns) {
		this.inumofruns = inumofruns;
	}

	/**
	 * @return the wisreferenceframes
	 */
	public int getWisreferenceframes() {
		return wisreferenceframes;
	}

	/**
	 * @param wisreferenceframes the wisreferenceframes to set
	 */
	public void setWisreferenceframes(int wisreferenceframes) {
		this.wisreferenceframes = wisreferenceframes;
	}

	/**
	 * @return the inumofreferenceruns
	 */
	public int getInumofreferenceruns() {
		return inumofreferenceruns;
	}

	/**
	 * @param inumofreferenceruns the inumofreferenceruns to set
	 */
	public void setInumofreferenceruns(int inumofreferenceruns) {
		this.inumofreferenceruns = inumofreferenceruns;
	}

	/**
	 * @return the cexperimentname
	 */
	public String getCexperimentname() {
		return cexperimentname;
	}

	/**
	 * @param cexperimentname the cexperimentname to set
	 */
	public void setCexperimentname(String cexperimentname) {
		this.cexperimentname = cexperimentname;
	}

	/**
	 * @return the cexperimentdir
	 */
	public String getCexperimentdir() {
		return cexperimentdir;
	}

	/**
	 * @param cexperimentdir the cexperimentdir to set
	 */
	public void setCexperimentdir(String cexperimentdir) {
		this.cexperimentdir = cexperimentdir;
	}

	/**
	 * @return the runFolder
	 */
	public String getRunFolder() {
		return runFolder;
	}

	/**
	 * @param runFolder the runFolder to set
	 */
	public void setRunFolder(String runFolder) {
		this.runFolder = runFolder;
	}

	/**
	 * @return the runFile
	 */
	public String getRunFile() {
		return runFile;
	}

	/**
	 * @param runFile the runFile to set
	 */
	public void setRunFile(String runFile) {
		this.runFile = runFile;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof CrysalisRunList)) {
			return false;
		}

		CrysalisRunList other = (CrysalisRunList) o;
		return this.name.equals(other.name) && this.runFolder.equals(other.runFolder)
				&& this.runFile.equals(other.runFile) && this.cexperimentdir.equals(other.cexperimentdir)
				&& this.cexperimentname.equals(other.cexperimentname)
				&& this.dwtotalnumofframes == other.dwtotalnumofframes
				&& this.wreferenceframefrequency == other.wreferenceframefrequency
				&& this.wversioninfo == other.wversioninfo && this.wisreferenceframes == other.wisreferenceframes
				&& this.inumofreferenceruns == other.inumofreferenceruns && this.inumofruns == other.inumofruns
				&& this.runlist.equals(other.runlist);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String text = String.format("RunList - " + name + "\n" + "runFolder = " + runFolder + "\n" + "runFile = "
				+ runFile + "\n" + "cexperimentdir = " + cexperimentdir + "\n" + "cexperimentname = " + cexperimentname
				+ "\n" + "dwtotalnumofframes = %d\n" + "wreferenceframefrequency = %d\n" + "wversioninfo = %d\n"
				+ "wisreferenceframes = %d\n" + "inumofreferenceruns = %d\n" + "inumofruns = %d\n", dwtotalnumofframes,
				wreferenceframefrequency, wversioninfo, wisreferenceframes, inumofreferenceruns, inumofruns);
		for (CrysalisRun run : runlist) {
			text += "\n" + run.toString();
		}
		return text;
	}

	void checkValidity(String validatorConfigPath) throws CrysalisValidityException {
		CrysalisRunListValidator validator = new CrysalisRunListValidator(validatorConfigPath, false);
		validator.checkValidity(this);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
