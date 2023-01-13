package uk.ac.gda.devices.bssc.beans;

import java.util.List;

import uk.ac.gda.beans.ObservableModel;
import uk.ac.gda.devices.bssc.ispyb.ISpyBStatusInfo;

public class BioSAXSProgress extends ObservableModel implements ISAXSProgress {
	private long experimentId;
	private long dataCollectionId;
	private String sampleName;
	private ISpyBStatusInfo collectionStatusInfo;
	private ISpyBStatusInfo reductionStatusInfo;

	public BioSAXSProgress(long experimentId, long dataCollectionId, String sampleName, ISpyBStatusInfo collectionStatusInfo,
			ISpyBStatusInfo reductionStatusInfo) {
		this.experimentId = experimentId;
		this.dataCollectionId = dataCollectionId;
		this.sampleName = sampleName;
		this.collectionStatusInfo = collectionStatusInfo;
		this.reductionStatusInfo = reductionStatusInfo;
	}

	@Override
	public void setExperimentId(long experimentId) {
		this.experimentId = experimentId;
	}

	@Override
	public long getExperimentId() {
		return experimentId;
	}
	
	@Override
	public void setDataCollectionId(long dataCollectionId) {
		this.dataCollectionId = dataCollectionId;
	}

	@Override
	public long getDataCollectionId() {
		return dataCollectionId;
	}

	@Override
	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}

	@Override
	public String getSampleName() {
		return sampleName;
	}

	@Override
	public void setCollectionStatusInfo(ISpyBStatusInfo collectionStatusInfo) {
		firePropertyChange(ISAXSProgress.COLLECTION_STATUS_INFO, this.collectionStatusInfo,
				this.collectionStatusInfo = collectionStatusInfo);
	}

	@Override
	public void setReductionStatusInfo(ISpyBStatusInfo reductionStatusInfo) {
		firePropertyChange(ISAXSProgress.REDUCTION_STATUS_INFO, this.reductionStatusInfo,
				this.reductionStatusInfo = reductionStatusInfo);
	}

	@Override
	public ISpyBStatusInfo getCollectionStatusInfo() {
		return collectionStatusInfo;
	}
	
	@Override
	public ISpyBStatusInfo getReductionStatusInfo() {
		return reductionStatusInfo;
	}
	
	@Override
	public List<String> getCollectionFileNames() {
		return collectionStatusInfo.getFileNames();
	}
}
