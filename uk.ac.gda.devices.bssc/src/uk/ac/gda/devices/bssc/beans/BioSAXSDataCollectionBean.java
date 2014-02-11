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

package uk.ac.gda.devices.bssc.beans;

import uk.ac.gda.devices.bssc.ispyb.ISAXSDataCollection;
import uk.ac.gda.devices.bssc.ispyb.ISpyBStatus;
import uk.ac.gda.devices.bssc.ispyb.ISpyBStatusInfo;

public class BioSAXSDataCollectionBean implements ISAXSDataCollection {
	private String sampleName;
	private ISpyBStatusInfo collectionStatusInfo;
	private ISpyBStatusInfo reductionStatusInfo;
	private ISpyBStatusInfo analysisStatusInfo;
	private String visit;
	private long experimentId;
	private long blSessionId;
	private long id;
	@SuppressWarnings("unused")
	private long bufferBeforeMeasurementId;
	@SuppressWarnings("unused")
	private long bufferAfterMeasurementId;

	public BioSAXSDataCollectionBean() {
		ISpyBStatusInfo notStartedStatus = new ISpyBStatusInfo();
		notStartedStatus.setStatus(ISpyBStatus.NOT_STARTED);
		collectionStatusInfo = notStartedStatus;
		reductionStatusInfo = notStartedStatus;
		analysisStatusInfo = notStartedStatus;
	}

	@Override
	public long getExperimentId() {
		return experimentId;
	}

	@Override
	public void setExperimentId(long experimentId) {
		this.experimentId = experimentId;
	}

	@Override
	public String getSampleName() {
		return sampleName;
	}

	@Override
	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}

	@Override
	public ISpyBStatusInfo getCollectionStatus() {
		return collectionStatusInfo;
	}

	@Override
	public void setCollectionStatus(ISpyBStatusInfo collectionStatus) {
		this.collectionStatusInfo = collectionStatus;
	}

	@Override
	public ISpyBStatusInfo getReductionStatus() {
		return reductionStatusInfo;
	}

	@Override
	public void setReductionStatus(ISpyBStatusInfo reductionStatus) {
		this.reductionStatusInfo = reductionStatus;
	}

	@Override
	public ISpyBStatusInfo getAnalysisStatus() {
		return analysisStatusInfo;
	}

	@Override
	public void setAnalysisStatus(ISpyBStatusInfo analysisStatus) {
		this.analysisStatusInfo = analysisStatus;
	}

	public String getVisit() {
		return visit;
	}

	public void setVisit(String visit) {
		this.visit = visit;
	}

	@Override
	public long getBlSessionId() {
		return blSessionId;
	}

	@Override
	public void setBlSessionId(long blSessionId) {
		this.blSessionId = blSessionId;
	}

	@Override
	public void setId(long saxsDataCollectionId) {
		this.id = saxsDataCollectionId;
	}

	@Override
	public long getId() {
		return id;
	}
	
	@Override
	public void setBufferBeforeMeasurementId(long bufferBeforeMeasurementId) {
		this.bufferBeforeMeasurementId = bufferBeforeMeasurementId;
	}

	@Override
	public void setBufferAfterMeasurementId(long bufferAfterMeasurementId) {
		this.bufferAfterMeasurementId = bufferAfterMeasurementId;
	}
}
