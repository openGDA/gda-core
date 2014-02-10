package uk.ac.gda.devices.bssc.ispyb;

import static org.junit.Assert.assertEquals;
import gda.factory.FactoryException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.gda.devices.bssc.beans.BioSAXSProgressController;
import uk.ac.gda.devices.bssc.beans.ISAXSProgress;

public class BioSAXSISpyBAPITest {
	public static IObservableList model;
	public static BioSAXSProgressController controller;
	private static BioSAXSISPyB bioSAXSISPyB;
	public static final int MODEL_SIZE = 7;
	private static DefaultRealm realm;
	private static long blSessionId;
	private static long experimentId;
	private static String visit;
	private List<ISAXSDataCollection> iSpyBSAXSDataCollections;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		controller = new BioSAXSProgressController();
		bioSAXSISPyB = new MyBioSAXSISPyB();
		try {
			controller.setISpyBAPI(bioSAXSISPyB);
		} catch (FactoryException e1) {
			e1.printStackTrace();
		}

		realm = new DefaultRealm();
		realm.exec(new Runnable() {
			@Override
			public void run() {
				model = controller.getModel();

				visit = "nt20-12";
				try {
					blSessionId = bioSAXSISPyB.getSessionForVisit(visit);
					experimentId = bioSAXSISPyB.createExperiment(blSessionId,
							"test", "TEMPLATE", "test");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Before
	public void loadMockISpyBItems() {
		model.clear();
		iSpyBSAXSDataCollections = controller.getDataCollectionsFromISPyB();
		List<ISAXSProgress> progressList = controller
				.loadModel(iSpyBSAXSDataCollections);
		model.addAll(progressList);
	}

	@Test
	public void testAddItemsToModelFromISpyB() {
		assertEquals(7, model.size());

		for (int i = 0; i < iSpyBSAXSDataCollections.size(); i++) {
			ISAXSProgress modelProgressItem = (ISAXSProgress) model.get(i);

			// Check model sample name is same as sample name in IsPyB
			String expectedSampleName = iSpyBSAXSDataCollections.get(i)
					.getSampleName();
			String modelSampleName = modelProgressItem.getSampleName();
			assertEquals(expectedSampleName, modelSampleName);

			// Check collection status of model object is the same as the status
			// in ISpyB
			ISpyBStatus expectedCollectionStatus = (ISpyBStatus) iSpyBSAXSDataCollections
					.get(i).getCollectionStatus().getStatus();
			ISpyBStatus modelCollectionStatus = ((ISpyBStatus) modelProgressItem
					.getCollectionStatus());
			assertEquals(expectedCollectionStatus, modelCollectionStatus);

			// Check reduction status of model object is the same as the status
			// in ISpyB
			ISpyBStatus expectedReductionStatus = (ISpyBStatus) iSpyBSAXSDataCollections
					.get(i).getReductionStatus().getStatus();
			ISpyBStatus modelReductionStatus = ((ISpyBStatus) modelProgressItem
					.getReductionStatus());
			assertEquals(expectedReductionStatus, modelReductionStatus);

			// Check analysis status of model object is the same as the status
			// in ISpyB
			ISpyBStatus expectedAnalysisStatus = (ISpyBStatus) iSpyBSAXSDataCollections
					.get(i).getAnalysisStatus().getStatus();
			ISpyBStatus modelAnalysisStatus = ((ISpyBStatus) modelProgressItem
					.getAnalysisStatus());
			assertEquals(expectedAnalysisStatus, modelAnalysisStatus);

			// Check collection progress in model is same as collection progress
			// in ISpyB
			double expectedCollectionProgress = iSpyBSAXSDataCollections.get(i)
					.getCollectionStatus().getProgress();
			double modelCollectionProgress = modelProgressItem
					.getCollectionProgress();
			assertEquals(expectedCollectionProgress, modelCollectionProgress,
					0.0);

			// Check reduction progress in model is same as reduction progress
			// in ISpyB
			double expectedReductionProgress = iSpyBSAXSDataCollections.get(i)
					.getReductionStatus().getProgress();
			double modelReductionProgress = modelProgressItem
					.getReductionProgress();
			assertEquals(expectedReductionProgress, modelReductionProgress, 0.0);

			// Check analysis progress in model is same as analysis progress in
			// ISpyB
			double expectedAnalysisProgress = iSpyBSAXSDataCollections.get(i)
					.getAnalysisStatus().getProgress();
			double modelAnalysisProgress = modelProgressItem
					.getAnalysisProgress();
			assertEquals(expectedAnalysisProgress, modelAnalysisProgress, 0.0);
		}
	}

	@Test
	public void testUpdateBufferBeforeProgress() {
		// launch python script to send a datagram
		long dataCollectionId = 0;
		int modelIndex = ((Long) dataCollectionId).intValue();

		String bufferBeforeFile = "/dls/b21/data/2013/sm999-9/b21-9990.nxs";
		String bufferBeforePath = "/entry1/detector/data";
		long bufferBeforeRun = bioSAXSISPyB.createBufferRun(dataCollectionId,
				1.0, 20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				1.0, bufferBeforeFile, bufferBeforePath);

		// assert model is updated with new value from ISpyB
		ISAXSProgress modelProgressItem = (ISAXSProgress) model.get(
				modelIndex);
		assertEquals(33, modelProgressItem.getCollectionProgress(), 0.0);
		assertEquals(0, modelProgressItem.getReductionProgress(), 0.0);
		assertEquals(0, modelProgressItem.getAnalysisProgress(), 0.0);
	}

	@Test
	public void testUpdateSampleProgress() {
		// launch python script to send a datagram
		long dataCollectionId = 0;
		int modelIndex = ((Long) dataCollectionId).intValue();

		String sampleFile = "/dls/b21/data/2013/sm999-9/b21-9990.nxs";
		String samplePath = "/entry1/detector/data";
		long sampleRun = bioSAXSISPyB.createSampleRun(dataCollectionId, 1.0,
				20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				sampleFile, samplePath);

		ISAXSProgress modelProgressItem = (ISAXSProgress) model.get(
				modelIndex);
		assertEquals(66, modelProgressItem.getCollectionProgress(), 0.0);
		assertEquals(0, modelProgressItem.getReductionProgress(), 0.0);
		assertEquals(0, modelProgressItem.getAnalysisProgress(), 0.0);
	}

	@Test
	public void testUpdateBufferAfterProgress() {
		// launch python script to send a datagram
		long dataCollectionId = 0;
		int modelIndex = ((Long) dataCollectionId).intValue();

		String bufferAfterFile = "/dls/b21/data/2013/sm999-9/b21-9990.nxs";
		String bufferAfterPath = "/entry1/detector/data";
		long sampleRun = bioSAXSISPyB.createSampleRun(dataCollectionId, 1.0,
				20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				bufferAfterFile, bufferAfterPath);

		// assert model is updated with new value from ISpyB
		ISAXSProgress modelProgressItem = (ISAXSProgress) model.get(
				modelIndex);
		assertEquals(100, modelProgressItem.getCollectionProgress(), 0.0);
		assertEquals(0, modelProgressItem.getReductionProgress(), 0.0);
		assertEquals(0, modelProgressItem.getAnalysisProgress(), 0.0);
	}

	@Test
	public void testReductionProgress() {
		// launch python script to send a datagram
		long dataCollectionId = 0;
		int modelIndex = ((Long) dataCollectionId).intValue();

		try {
			long reductionId = bioSAXSISPyB
					.createDataReduction(dataCollectionId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// assert model is updated with new value from ISpyB
		ISAXSProgress modelProgressItem = (ISAXSProgress) model.get(
				modelIndex);
		assertEquals(66, modelProgressItem.getCollectionProgress(), 0.0);
		assertEquals(100, modelProgressItem.getReductionProgress(), 0.0);
		assertEquals(0, modelProgressItem.getAnalysisProgress(), 0.0);
	}

	@Test
	public void testAnalysisProgress() {
		// launch python script to send a datagram
		long dataCollectionId = 0;
		int modelIndex = ((Long) dataCollectionId).intValue();

		try {
			long analysisId = bioSAXSISPyB.createDataAnalysis(dataCollectionId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// assert model is updated with new value from ISpyB
		ISAXSProgress modelProgressItem = (ISAXSProgress) model.get(
				modelIndex);
		assertEquals(66, modelProgressItem.getCollectionProgress(), 0.0);
		assertEquals(100, modelProgressItem.getReductionProgress(), 0.0);
		assertEquals(100, modelProgressItem.getAnalysisProgress(), 0.0);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Thread.sleep(3000);
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
}

class MyBioSAXSISPyB implements BioSAXSISPyB {

	private List<ISAXSDataCollection> isPyBSAXSDataCollections;

	public MyBioSAXSISPyB() {
		isPyBSAXSDataCollections = new ArrayList<ISAXSDataCollection>();
	}

	@Override
	public long getSessionForVisit(String visitname) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long createSaxsDataCollection(long experimentID, short plate,
			short row, short column, String sampleName, short bufferPlate,
			short bufferRow, short bufferColumn, float exposureTemperature,
			int numFrames, double timePerFrame, double flow, double volume,
			double energyInkeV, String viscosity) throws SQLException {
		long dataCollectionId = 0;

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
		status.setFileName(filename);
		setDataCollectionStatus(currentDataCollectionId, status);
		isPyBSAXSDataCollections.get(
				((Long) currentDataCollectionId).intValue())
				.setCollectionStatus(status);

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
		status.setFileName(filename);
		setDataCollectionStatus(dataCollectionId, status);

		// Mock the controller receiving a notification update that database has
		// been updated
		sendISpyBUpdate(dataCollectionId);
		return dataCollectionId;
	}

	private void sendISpyBUpdate(long collectionId) {
		int dataCollectionId = ((Long) collectionId).intValue();

		ISAXSDataCollection dataCollectionUpdated = isPyBSAXSDataCollections
				.get(dataCollectionId);

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
		int dataCollectionId = ((Long) saxsDataCollectionId).intValue();

		isPyBSAXSDataCollections.get(dataCollectionId).setCollectionStatus(
				status);
	}

	@Override
	public ISpyBStatusInfo getDataCollectionStatus(long saxsDataCollectionId)
			throws SQLException {
		// Mock up getting item from the database here
		int dataCollectionId = ((Long) saxsDataCollectionId).intValue();

		return isPyBSAXSDataCollections.get(dataCollectionId)
				.getCollectionStatus();
	}

	@Override
	public long createDataReduction(long dataCollectionId) throws SQLException {
		// Mock up creating a data reduction data base object here
		long dataReductionId = 0;
		ISpyBStatusInfo status = new ISpyBStatusInfo();
		status.setStatus(ISpyBStatus.COMPLETE);
		status.setProgress(100);
		status.setFileName("");
		setDataReductionStatus(dataCollectionId, status, "");

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
		status.setFileName("");
		setDataAnalysisStatus(dataCollectionId, status, "");

		// Mock the controller receiving a notification update that database has
		// been updated
		sendISpyBUpdate(dataCollectionId);
		return dataAnalysisId;
	}

	@Override
	public void setDataReductionStatus(long saxsDataCollectionId,
			ISpyBStatusInfo status, String resultsFilename) throws SQLException {
		int dataCollectionId = ((Long) saxsDataCollectionId).intValue();

		isPyBSAXSDataCollections.get(dataCollectionId).setReductionStatus(
				status);
	}

	@Override
	public ISpyBStatusInfo getDataReductionStatus(long saxsDataCollectionId)
			throws SQLException {
		int dataCollectionId = ((Long) saxsDataCollectionId).intValue();

		return isPyBSAXSDataCollections.get(dataCollectionId)
				.getReductionStatus();
	}

	@Override
	public void setDataAnalysisStatus(long saxsDataCollectionId,
			ISpyBStatusInfo status, String resultsFilename) throws SQLException {
		int dataCollectionId = ((Long) saxsDataCollectionId).intValue();

		isPyBSAXSDataCollections.get(dataCollectionId)
				.setAnalysisStatus(status);
	}

	@Override
	public ISpyBStatusInfo getDataAnalysisStatus(long saxsDataCollectionId)
			throws SQLException {
		int dataCollectionId = ((Long) saxsDataCollectionId).intValue();

		return isPyBSAXSDataCollections.get(dataCollectionId)
				.getAnalysisStatus();
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
		for (int i = 0; i < BioSAXSISpyBAPITest.MODEL_SIZE; i++) {
			ISAXSDataCollection bioSaxsDataCollection = new MockSAXSDataCollection();
			bioSaxsDataCollection.setExperimentId(0);
			bioSaxsDataCollection.setSampleName("Sample " + i);
			bioSaxsDataCollection.setBlSessionId(blSessionId);
			ISpyBStatusInfo collectionStatusInfo = new ISpyBStatusInfo();
			collectionStatusInfo.setStatus(ISpyBStatus.NOT_STARTED);
			collectionStatusInfo.setProgress(0);
			collectionStatusInfo.setFileName("");
			collectionStatusInfo.setMessage("");
			bioSaxsDataCollection.setCollectionStatus(collectionStatusInfo);
			ISpyBStatusInfo reductionStatusInfo = new ISpyBStatusInfo();
			reductionStatusInfo.setStatus(ISpyBStatus.NOT_STARTED);
			reductionStatusInfo.setProgress(0);
			reductionStatusInfo.setFileName("");
			reductionStatusInfo.setMessage("");
			bioSaxsDataCollection.setReductionStatus(reductionStatusInfo);
			ISpyBStatusInfo analysisStatusInfo = new ISpyBStatusInfo();
			analysisStatusInfo.setStatus(ISpyBStatus.NOT_STARTED);
			analysisStatusInfo.setProgress(0);
			analysisStatusInfo.setFileName("");
			analysisStatusInfo.setMessage("");
			bioSaxsDataCollection.setAnalysisStatus(analysisStatusInfo);
			isPyBSAXSDataCollections.add(bioSaxsDataCollection);
		}
		return isPyBSAXSDataCollections;
	}

	@Override
	public long createSaxsDataCollectionUsingPreviousBuffer(long experimentID,
			short plate, short row, short column, String sampleName,
			short bufferPlate, short bufferRow, short bufferColumn,
			float exposureTemperature, int numFrames, double timePerFrame,
			double flow, double volume, double energyInkeV, String viscosity,
			long previousDataCollectionId) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getPreviousCollectionId(long dataCollectionId) {
		// TODO Auto-generated method stub
		return 0;
	}
}

class MockSAXSDataCollection implements ISAXSDataCollection {

	private String sampleName;
	private ISpyBStatusInfo collectionStatus;
	private ISpyBStatusInfo reductionStatus;
	private ISpyBStatusInfo analysisStatus;
	private long blSessionId;
	private long experimentId;
	private long saxsCollectionId;

	@Override
	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}

	@Override
	public String getSampleName() {
		return sampleName;
	}

	@Override
	public ISpyBStatusInfo getCollectionStatus() {
		return collectionStatus;
	}

	@Override
	public ISpyBStatusInfo getReductionStatus() {
		return reductionStatus;
	}

	@Override
	public ISpyBStatusInfo getAnalysisStatus() {
		return analysisStatus;
	}

	@Override
	public void setCollectionStatus(ISpyBStatusInfo collectionStatus) {
		this.collectionStatus = collectionStatus;
	}

	@Override
	public void setReductionStatus(ISpyBStatusInfo reductionStatus) {
		this.reductionStatus = reductionStatus;
	}

	@Override
	public void setAnalysisStatus(ISpyBStatusInfo analysisStatus) {
		this.analysisStatus = analysisStatus;
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
	public long getExperimentId() {
		return experimentId;
	}

	@Override
	public void setExperimentId(long experimentId) {
		this.experimentId = experimentId;
	}

	@Override
	public void setId(long saxsCollectionId) {
		this.saxsCollectionId = saxsCollectionId;
	}

	@Override
	public long getId() {
		return saxsCollectionId;
	}

	@Override
	public void setBufferBeforeMeasurementId(long bufferBeforeMeasurementId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBufferAfterMeasurementId(long bufferAfterMeasurementId) {
		// TODO Auto-generated method stub

	}

}

class DefaultRealm extends Realm {
	private Realm previousRealm;

	public DefaultRealm() {
		previousRealm = super.setDefault(this);
	}

	/**
	 * @return always returns true
	 */
	public boolean isCurrent() {
		return true;
	}

	protected void syncExec(Runnable runnable) {
		runnable.run();
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	public void asyncExec(Runnable runnable) {
		// throw new UnsupportedOperationException("asyncExec is unsupported");
		runnable.run();
	}

	/**
	 * Removes the realm from being the current and sets the previous realm to
	 * the default.
	 */
	public void dispose() {
		if (getDefault() == this) {
			setDefault(previousRealm);
		}
	}
}
