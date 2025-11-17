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
	public static final String COLLECTION_PROGRESS = "collectionProgress";
	public static final String REDUCTION_PROGRESS = "reductionProgress";

	void setSampleName(String sampleName);

	String getSampleName();

	ISpyBStatusInfo getCollectionStatus();

	ISpyBStatusInfo getReductionStatus();

	void setCollectionStatus(ISpyBStatusInfo collectionStatus);

	void setReductionStatus(ISpyBStatusInfo reductionStatus);

	long getBlSessionId();

	void setBlSessionId(long blSessionId);

	long getExperimentId();

	void setExperimentId(long experimentId);

	void setId(long saxsDataCollectionId);

	long getId();

	void setBufferBeforeMeasurementId(long bufferBeforeMeasurementId);

	void setBufferAfterMeasurementId(long bufferAfterMeasurementId);
}
