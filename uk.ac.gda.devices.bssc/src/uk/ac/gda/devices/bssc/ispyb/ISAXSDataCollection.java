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

public interface ISAXSDataCollection {
	public static final String SAMPLE_NAME = "sampleName";
	public static final String COLLECTION_STATUS = "collectionStatus";
	public static final String REDUCTION_STATUS = "reductionStatus";
	public static final String ANALYSIS_STATUS = "analysisStatus";
	public static final String COLLECTION_PROGRESS = "collectionProgress";
	public static final String REDUCTION_PROGRESS = "reductionProgress";
	public static final String ANALYSIS_PROGRESS = "analysisProgress";

	public void setSampleName(String sampleName);

	public String getSampleName();
	
	public ISpyBStatusInfo getCollectionStatus();

	public ISpyBStatusInfo getReductionStatus();

	public ISpyBStatusInfo getAnalysisStatus();

	public void setCollectionStatus(ISpyBStatusInfo collectionStatus);

	public void setReductionStatus(ISpyBStatusInfo reductionStatus);
	
	public void setAnalysisStatus(ISpyBStatusInfo analysisStatus);

	public long getBlSessionId();

	public void setBlSessionId(long blSessionId);

	public long getExperimentId();

	public void setExperimentId(long experimentId);

	public void setId(long saxsDataCollectionId);
	
	public long getId();

	public void setBufferBeforeMeasurementId(long bufferBeforeMeasurementId);

	public void setBufferAfterMeasurementId(long bufferAfterMeasurementId);
}
