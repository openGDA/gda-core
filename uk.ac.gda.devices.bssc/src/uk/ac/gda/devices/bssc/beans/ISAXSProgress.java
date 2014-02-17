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

import java.beans.PropertyChangeListener;
import java.util.List;

import uk.ac.gda.devices.bssc.ispyb.ISpyBStatus;
import uk.ac.gda.devices.bssc.ispyb.ISpyBStatusInfo;

public interface ISAXSProgress {
	public static final String SAMPLE_NAME = "sampleName";
	public static final String COLLECTION_STATUS = "collectionStatus";
	public static final String REDUCTION_STATUS = "reductionStatus";
	public static final String ANALYSIS_STATUS = "analysisStatus";
	public static final String COLLECTION_PROGRESS = "collectionProgress";
	public static final String REDUCTION_PROGRESS = "reductionProgress";
	public static final String ANALYSIS_PROGRESS = "analysisProgress";

	public void setId(double id);

	public double getId();
	
	public void setSampleName(String sampleName);

	public String getSampleName();
	
	public void setCollectionProgress(ISpyBStatusInfo collectionStatusInfo);
	
	public double getCollectionProgress();

	public void setReductionProgress(ISpyBStatusInfo reductionStatusInfo);
	
	public double getReductionProgress();

	public void setAnalysisProgress(ISpyBStatusInfo analysisStatusInfo);
	
	public double getAnalysisProgress();

	public ISpyBStatus getCollectionStatus();

	public ISpyBStatus getReductionStatus();
	
	public ISpyBStatus getAnalysisStatus();
	
	public List<String> getCollectionFileNames();
}
