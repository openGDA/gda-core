package uk.ac.gda.devices.bssc.beans;

import java.util.List;

import uk.ac.gda.beans.ObservableModel;
import uk.ac.gda.devices.bssc.ispyb.ISpyBStatus;
import uk.ac.gda.devices.bssc.ispyb.ISpyBStatusInfo;

public class BioSAXSProgress extends ObservableModel implements ISAXSProgress {

	private double id;
	private String sampleName;
	private ISpyBStatusInfo collectionStatusInfo;
	private ISpyBStatusInfo reductionStatusInfo;
	private ISpyBStatusInfo analysisStatusInfo;
	private double collectionProgress;
	private double reductionProgress;
	private double analysisProgress;

	public BioSAXSProgress(long id, String sampleName, ISpyBStatusInfo collectionStatusInfo,
			ISpyBStatusInfo reductionStatusInfo, ISpyBStatusInfo analysisStatusInfo) {
		this.id = id;
		this.sampleName = sampleName;
		this.collectionStatusInfo = collectionStatusInfo;
		this.reductionStatusInfo = reductionStatusInfo;
		this.analysisStatusInfo = analysisStatusInfo;
	}

	@Override
	public void setId(double id) {
		this.id = id;
	}

	@Override
	public double getId() {
		return id;
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
	public void setCollectionProgress(ISpyBStatusInfo collectionStatusInfo) {
		firePropertyChange(ISAXSProgress.COLLECTION_PROGRESS, this.collectionProgress,
				this.collectionProgress = collectionStatusInfo.getProgress());
	}

	@Override
	public void setReductionProgress(ISpyBStatusInfo reductionStatusInfo) {
		firePropertyChange(ISAXSProgress.REDUCTION_PROGRESS, this.reductionProgress,
				this.reductionProgress = reductionStatusInfo.getProgress());
	}

	@Override
	public void setAnalysisProgress(ISpyBStatusInfo analysisStatusInfo) {
		firePropertyChange(ISAXSProgress.ANALYSIS_PROGRESS, this.analysisProgress,
				this.analysisProgress = analysisStatusInfo.getProgress());
	}

	@Override
	public ISpyBStatus getCollectionStatus() {
		return collectionStatusInfo.getStatus();
	}
	
	@Override
	public ISpyBStatus getReductionStatus() {
		return reductionStatusInfo.getStatus();
	}
	
	@Override
	public ISpyBStatus getAnalysisStatus()
	{
		return analysisStatusInfo.getStatus();
	}
	
	@Override
	public double getCollectionProgress() {
		return collectionProgress;
	}

	@Override
	public double getReductionProgress() {
		return reductionProgress;
	}

	@Override
	public double getAnalysisProgress() {
		return analysisProgress;
	}

	@Override
	public List<String> getCollectionFileNames() {
		return collectionStatusInfo.getFileNames();
	}
}
