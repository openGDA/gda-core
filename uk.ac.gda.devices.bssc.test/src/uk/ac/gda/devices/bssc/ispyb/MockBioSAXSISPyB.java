package uk.ac.gda.devices.bssc.ispyb;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockBioSAXSISPyB implements BioSAXSISPyB {
	private static final int MODEL_SIZE = 7;
	Map<Long, ISAXSDataCollection> collectionsMap;

	public MockBioSAXSISPyB() {
		collectionsMap = new HashMap<Long, ISAXSDataCollection>();
		initialiseDataCollections();
	}

	private void initialiseDataCollections() {
		for (long i = 0; i < MODEL_SIZE; i++) {
			ISAXSDataCollection bioSaxsDataCollection = new MockSAXSDataCollection();
			bioSaxsDataCollection.setId(i);
			bioSaxsDataCollection.setExperimentId(0);
			bioSaxsDataCollection.setSampleName("Sample " + i);
			bioSaxsDataCollection.setBlSessionId(0);
			ISpyBStatusInfo collectionStatusInfo = new ISpyBStatusInfo();
			collectionStatusInfo.setStatus(ISpyBStatus.NOT_STARTED);
			collectionStatusInfo.setProgress(0);
			collectionStatusInfo.addFileName("");
			collectionStatusInfo.setMessage("");
			bioSaxsDataCollection.setCollectionStatus(collectionStatusInfo);
			ISpyBStatusInfo reductionStatusInfo = new ISpyBStatusInfo();
			reductionStatusInfo.setStatus(ISpyBStatus.NOT_STARTED);
			reductionStatusInfo.setProgress(0);
			reductionStatusInfo.addFileName("");
			reductionStatusInfo.setMessage("");
			bioSaxsDataCollection.setReductionStatus(reductionStatusInfo);
			ISpyBStatusInfo analysisStatusInfo = new ISpyBStatusInfo();
			analysisStatusInfo.setStatus(ISpyBStatus.NOT_STARTED);
			analysisStatusInfo.setProgress(0);
			analysisStatusInfo.addFileName("");
			analysisStatusInfo.setMessage("");
			bioSaxsDataCollection.setAnalysisStatus(analysisStatusInfo);
			collectionsMap.put(i, bioSaxsDataCollection);
		}
	}

	@Override
	public long getSessionForVisit(String visitname) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long createSaxsDataCollection(long experimentID, short plate,
			short row, short column, String sampleName,
			double concentrationMgMl, double molecularMassKda,
			short bufferPlate, short bufferRow, short bufferColumn,
			float exposureTemperature, int numFrames, double timePerFrame,
			double flow, double volume, double energyInkeV, String viscosity)
			throws SQLException {
		long dataCollectionId = collectionsMap.size();

		ISAXSDataCollection bioSaxsDataCollection = new MockSAXSDataCollection();
		bioSaxsDataCollection.setId(dataCollectionId);
		bioSaxsDataCollection.setExperimentId(0);
		bioSaxsDataCollection.setSampleName("Sample " + dataCollectionId);
		bioSaxsDataCollection.setBlSessionId(0);
		ISpyBStatusInfo collectionStatusInfo = new ISpyBStatusInfo();
		collectionStatusInfo.setStatus(ISpyBStatus.NOT_STARTED);
		collectionStatusInfo.setProgress(0);
		collectionStatusInfo.addFileName("");
		collectionStatusInfo.setMessage("");
		bioSaxsDataCollection.setCollectionStatus(collectionStatusInfo);
		ISpyBStatusInfo reductionStatusInfo = new ISpyBStatusInfo();
		reductionStatusInfo.setStatus(ISpyBStatus.NOT_STARTED);
		reductionStatusInfo.setProgress(0);
		reductionStatusInfo.addFileName("");
		reductionStatusInfo.setMessage("");
		bioSaxsDataCollection.setReductionStatus(reductionStatusInfo);
		ISpyBStatusInfo analysisStatusInfo = new ISpyBStatusInfo();
		analysisStatusInfo.setStatus(ISpyBStatus.NOT_STARTED);
		analysisStatusInfo.setProgress(0);
		analysisStatusInfo.addFileName("");
		analysisStatusInfo.setMessage("");
		bioSaxsDataCollection.setAnalysisStatus(analysisStatusInfo);
		collectionsMap.put(dataCollectionId, bioSaxsDataCollection);

		// Mock the controller receiving a notification update that database has
		// been updated
		sendISpyBUpdate(dataCollectionId);
		return dataCollectionId;
	}

	@Override
	public long createBufferRun(long currentDataCollectionId,
			double timePerFrame, float storageTemperature,
			float exposureTemperature, double energy, int frameCount,
			double transmission, double beamCenterX, double beamCenterY,
			double pixelSizeX, double pixelSizeY, double radiationRelative,
			double radiationAbsolute, double normalization, String filename,
			String internalPath) {

		// Mimic updating of IspyB
		ISpyBStatusInfo status = new ISpyBStatusInfo();
		status.setStatus(ISpyBStatus.RUNNING);
		status.setProgress(33);
		status.addFileName(filename);
		setDataCollectionStatus(currentDataCollectionId, status);

		// Mock the controller receiving a notification update that database has
		// been updated
		sendISpyBUpdate(currentDataCollectionId);
		return currentDataCollectionId;
	}

	@Override
	public long createSampleRun(long dataCollectionId, double timePerFrame,
			float storageTemperature, float exposureTemperature, double energy,
			int frameCount, double transmission, double beamCenterX,
			double beamCenterY, double pixelSizeX, double pixelSizeY,
			double radiationRelative, double radiationAbsolute,
			double normalization, String filename, String internalPath) {

		ISpyBStatusInfo status = new ISpyBStatusInfo();
		status.setStatus(ISpyBStatus.RUNNING);
		status.setProgress(66);
		status.addFileName(filename);
		setDataCollectionStatus(dataCollectionId, status);

		// Mock the controller receiving a notification update that database has
		// been updated
		sendISpyBUpdate(dataCollectionId);
		return dataCollectionId;
	}

	private void sendISpyBUpdate(long collectionId) {
		final String[] cmd = { "python",
				"/home/xlw00930/scripts/simple_udp.py", "ws141", "9877",
				"simpleUDPServer:" + collectionId };

		try {
			Runtime.getRuntime().exec(cmd);
			// Sleep for two seconds so that we do not retrieve from model
			// before
			// it has been notified of updates
			Thread.sleep(2000);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void setDataCollectionStatus(long saxsDataCollectionId,
			ISpyBStatusInfo status) {
		// Mock up setting database object here
		collectionsMap.get(saxsDataCollectionId).setCollectionStatus(status);
	}

	@Override
	public ISpyBStatusInfo getDataCollectionStatus(long saxsDataCollectionId)
			throws SQLException {
		// Mock up getting item from the database here
		return collectionsMap.get(saxsDataCollectionId).getCollectionStatus();
	}

	@Override
	public long createDataReduction(long dataCollectionId) throws SQLException {
		// Mock up creating a data reduction data base object here
		long dataReductionId = 0;
		ISpyBStatusInfo status = new ISpyBStatusInfo();
		status.setStatus(ISpyBStatus.COMPLETE);
		status.setProgress(100);
		setDataReductionStatus(dataCollectionId, status);

		// Mock the controller receiving a notification update that database has
		// been updated
		sendISpyBUpdate(dataReductionId);
		return dataReductionId;
	}

	@Override
	public long createDataAnalysis(long dataCollectionId) throws SQLException {
		// Mock up creating a data reduction data base object here
		long dataAnalysisId = 0;
		ISpyBStatusInfo status = new ISpyBStatusInfo();
		status.setStatus(ISpyBStatus.COMPLETE);
		status.setProgress(100);
		status.addFileName("");
		setDataAnalysisStatus(dataCollectionId, status);

		// Mock the controller receiving a notification update that database has
		// been updated
		sendISpyBUpdate(dataCollectionId);
		return dataAnalysisId;
	}

	@Override
	public void setDataReductionStatus(long saxsDataCollectionId,
			ISpyBStatusInfo status) throws SQLException {
		collectionsMap.get(saxsDataCollectionId).setReductionStatus(status);
	}

	@Override
	public ISpyBStatusInfo getDataReductionStatus(long saxsDataCollectionId)
			throws SQLException {
		return collectionsMap.get(saxsDataCollectionId).getReductionStatus();
	}

	@Override
	public void setDataAnalysisStatus(long saxsDataCollectionId,
			ISpyBStatusInfo status) throws SQLException {
		collectionsMap.get(saxsDataCollectionId).setAnalysisStatus(status);
	}

	@Override
	public ISpyBStatusInfo getDataAnalysisStatus(long saxsDataCollectionId)
			throws SQLException {
		return collectionsMap.get(saxsDataCollectionId).getAnalysisStatus();
	}

	@Override
	public void disconnect() throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<SampleInfo> getSaxsDataCollectionInfo(long saxsDataCollectionId)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SampleInfo> getExperimentInfo(long experimentId)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Long> getExperimentsForSession(long blsessionId)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Long> getDataCollectionsForExperiments(long experiment)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long createExperiment(long sessionId, String name,
			String experimentType, String comments) throws SQLException {
		long experimentId = 0;
		return experimentId;
	}

	@Override
	public List<ISAXSDataCollection> getSAXSDataCollections(long blSessionId)
			throws SQLException {
		List<ISAXSDataCollection> collectionsList = new ArrayList<ISAXSDataCollection>(collectionsMap.values());
		return collectionsList;
	}

	@Override
	public long createSaxsDataCollectionUsingPreviousBuffer(long experimentID,
			short plate, short row, short column, String sampleName,
			double concentrationMgMl, double molecularMassKda,
			short bufferPlate, short bufferRow, short bufferColumn,
			float exposureTemperature, int numFrames, double timePerFrame,
			double flow, double volume, double energyInkeV, String viscosity,
			long previousDataCollectionId) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ISAXSDataCollection getSAXSDataCollection(long dataCollectionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setExperimentFinished(long experimentId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setExperimentAborted(long experimentId) {
		// TODO Auto-generated method stub
		
	}
}