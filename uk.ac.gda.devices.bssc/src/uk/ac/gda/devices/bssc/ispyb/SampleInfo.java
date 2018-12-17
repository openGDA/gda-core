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

package uk.ac.gda.devices.bssc.ispyb;

import uk.ac.gda.devices.hatsaxs.beans.LocationBean;

public class SampleInfo {
	private LocationBean location;
	private String sampleId;
	private String experimentId;
	private String name;
	private String sampleFileName;
	private String bufferBeforeFileName; 
	private String bufferAfterFileName;
	private String collectionStatus;
	private String reductionStatus;
	private String analysisStatus;
	
	public LocationBean getLocation() {
		return location;
	}
	public void setLocation(LocationBean location) {
		this.location = location;
	}
	public String getSampleId() {
		return sampleId;
	}
	public void setSampleId(String sampleId) {
		this.sampleId = sampleId;
	}
	public String getExperimentId() {
		return experimentId;
	}
	public void setExperimentId(String experimentId) {
		this.experimentId = experimentId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSampleFileName() {
		return sampleFileName;
	}
	public void setSampleFileName(String sampleFileName) {
		this.sampleFileName = sampleFileName;
	}
	public String getBufferBeforeFileName() {
		return bufferBeforeFileName;
	}
	public void setBufferBeforeFileName(String bufferBeforeFileName) {
		this.bufferBeforeFileName = bufferBeforeFileName;
	}
	public String getBufferAfterFileName() {
		return bufferAfterFileName;
	}
	public void setBufferAfterFileName(String bufferAfterFileName) {
		this.bufferAfterFileName = bufferAfterFileName;
	}
	public String getCollectionStatus() {
		return collectionStatus;
	}
	public void setCollectionStatus(String collectionStatus) {
		this.collectionStatus = collectionStatus;
	}
	public String getReductionStatus() {
		return reductionStatus;
	}
	public void setReductionStatus(String reductionStatus) {
		this.reductionStatus = reductionStatus;
	}
	public String getAnalysisStatus() {
		return analysisStatus;
	}
	public void setAnalysisStatus(String analysisStatus) {
		this.analysisStatus = analysisStatus;
	}
}
