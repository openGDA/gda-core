package uk.ac.gda.devices.bssc.ispyb;

import static org.junit.Assert.assertEquals;
import gda.rcp.GDAClientActivator;
import gda.rcp.util.OSGIServiceRegister;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.ObservableList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.gda.devices.bssc.beans.BioSAXSDataCollection;
import uk.ac.gda.devices.bssc.beans.BioSAXSProgressController;
import uk.ac.gda.devices.bssc.beans.BioSAXSProgressModel;
import uk.ac.gda.devices.bssc.beans.IProgressModel;
import uk.ac.gda.devices.bssc.beans.ISAXSDataCollection;
import uk.ac.gda.devices.bssc.beans.ISpyBStatus;

public class BioSAXSISpyBAPITest {
	public static IProgressModel model;
	private static BioSAXSProgressController controller;
	private static BioSAXSISPyB bioSAXSISPyB;
	public static final int MODEL_SIZE = 7;
	private static DefaultRealm realm;
	private static long blSessionId;
	private static long experimentId;
	private static String visit;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		bioSAXSISPyB = new MyBioSAXSISPy();
		realm = new DefaultRealm();
		realm.exec(new Runnable() {
			@Override
			public void run() {
				controller = new BioSAXSProgressController();
				bioSAXSISPyB = new MyBioSAXSISPy();
				controller.setISpyBAPI(bioSAXSISPyB);

				model = new BioSAXSProgressModel(controller);

				// modelReg.afterPropertiesSet();
				visit = "nt20-12";

				try {
					blSessionId = bioSAXSISPyB.getSessionForVisit(visit);
					experimentId = bioSAXSISPyB.createExperiment(blSessionId,
							"test", "TEMPLATE", "test");
				} catch (SQLException e) {
					e.printStackTrace();
				}

				List<ISAXSDataCollection> iSpyBSAXSDataCollections = controller
						.loadModelFromISPyB();
				model.addItems(iSpyBSAXSDataCollections);
			}
		});
	}

	@Test
	public void testInitSAXSDataCollections() {
		assertEquals(MODEL_SIZE, model.getItems().size());
	}

	@Test
	public void testRunSAXSDataCollections() throws Exception {
		for (long dataCollectionId = 0; dataCollectionId < model.getItems()
				.size(); dataCollectionId++) {
			int dataCollectionIdIntValue = ((Long) dataCollectionId).intValue();
			ISAXSDataCollection dataCollection = (ISAXSDataCollection) model
					.getItems().get(dataCollectionIdIntValue);

			ISpyBStatus modelCollectionStatus = bioSAXSISPyB
					.getDataCollectionStatus(dataCollectionId);
			// assertEquals(ISpyBStatus.NOT_STARTED, modelCollectionStatus);

			String bufferBeforeFile = "/dls/b21/data/2013/sm999-9/b21-9990.nxs";
			String bufferBeforePath = "/entry1/detector/data";
			long bufferBeforeRun = bioSAXSISPyB.createBufferRun(
					dataCollectionId, 1.0, 20.0f, 20.0f, 10.0, 10, 1.0, 1.0,
					1.0, 1.0, 1.0, 1.0, 1.0, 1.0, bufferBeforeFile,
					bufferBeforePath);

			// assert here model is updated
			ISAXSDataCollection modelDataCollection = (ISAXSDataCollection) model
					.getItems().get(dataCollectionIdIntValue);

			modelCollectionStatus = modelDataCollection.getCollectionStatus();
			assertEquals(ISpyBStatus.RUNNING,
					modelDataCollection.getCollectionStatus());
			assertEquals(33, modelCollectionStatus.getProgress(), 0.0);
			assertEquals(bufferBeforeFile, modelCollectionStatus.getFileName());

			String sampleFile = "/dls/b21/data/2013/sm999-9/b21-9991.nxs";
			String samplePath = "/entry1/detector/data";
			long sampleRun = bioSAXSISPyB.createSampleRun(dataCollectionId,
					1.0, 20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
					1.0, 1.0, sampleFile, samplePath);

			assertEquals(ISpyBStatus.RUNNING, modelCollectionStatus);
			assertEquals(66, modelCollectionStatus.getProgress(), 0.0);
			assertEquals(sampleFile, modelCollectionStatus.getFileName());

			String bufferAfterFile = "/dls/b21/data/2013/sm999-9/b21-9992.nxs";
			String bufferAfterPath = "/entry1/detector/data";
			long bufferAfterRun = bioSAXSISPyB.createBufferRun(
					dataCollectionId, 1.0, 20.0f, 20.0f, 10.0, 10, 1.0, 1.0,
					1.0, 1.0, 1.0, 1.0, 1.0, 1.0, bufferAfterFile,
					bufferAfterPath);
			modelCollectionStatus = bioSAXSISPyB
					.getDataCollectionStatus(dataCollectionId);
			// assertEquals(ISpyBStatus.COMPLETE, status);
			// assertEquals(100, status.getProgress(), 0.0);
			// assertEquals(bufferAfterFile, status.getFileName());

			ISpyBStatus modelReductionStatus = bioSAXSISPyB
					.getDataReductionStatus(dataCollectionId);

			bioSAXSISPyB.createDataReduction(dataCollectionId);
			modelReductionStatus = bioSAXSISPyB
					.getDataReductionStatus(dataCollectionId);

			assertEquals(ISpyBStatus.COMPLETE, modelReductionStatus);
			assertEquals(100, modelReductionStatus.getProgress(), 0.0);
			assertEquals("", modelReductionStatus.getFileName());

			ISpyBStatus modelAnalysisStatus = bioSAXSISPyB
					.getDataAnalysisStatus(dataCollectionId);

			bioSAXSISPyB.createDataAnalysis(dataCollectionId);
			modelAnalysisStatus = bioSAXSISPyB
					.getDataAnalysisStatus(dataCollectionId);
			assertEquals(modelAnalysisStatus,
					bioSAXSISPyB.getDataAnalysisStatus(dataCollectionId));
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
}

class MyBioSAXSISPy implements BioSAXSISPyB {

	private List<ISAXSDataCollection> isPyBSAXSDataCollections;

	public MyBioSAXSISPy() {
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

		ISpyBStatus status = ISpyBStatus.RUNNING;
		status.setProgress(33);
		status.setFileName(filename);
		setDataCollectionStatus(currentDataCollectionId, status);

		// Mock the model receiving a notification update that database has
		// been updated
		updateModel(currentDataCollectionId);

		return currentDataCollectionId;
	}

	private void updateModel(long currentDataCollectionId) {
		int dataCollectionId = ((Long) currentDataCollectionId).intValue();
		
		
		ISAXSDataCollection dataCollectionUpdated = isPyBSAXSDataCollections
				.get(dataCollectionId);
		
		// here we would get the data collection from ISPyB using the controller and update the model
		
		ISAXSDataCollection modelCollectionToUpdate = (ISAXSDataCollection) BioSAXSISpyBAPITest.model
				.getItems().get(dataCollectionId);
		modelCollectionToUpdate = dataCollectionUpdated;
	}

	@Override
	public long createSampleRun(long dataCollectionId, double timePerFrame,
			float storageTemperature, float exposureTemperature, double energy,
			int frameCount, double transmission, double beamCenterX,
			double beamCenterY, double pixelSizeX, double pixelSizeY,
			double radiationRelative, double radiationAbsolute,
			double normalization, String filename, String internalPath) {

		ISpyBStatus status = ISpyBStatus.RUNNING;
		status.setProgress(66);
		status.setFileName(filename);
		setDataCollectionStatus(dataCollectionId, status);

		// Mock the model receiving a notification update that database has
		// been updated
		updateModel(dataCollectionId);
		return dataCollectionId;
	}

	@Override
	public void setDataCollectionStatus(long saxsDataCollectionId,
			ISpyBStatus status) {
		// Mock up setting database object here
		int dataCollectionId = ((Long) saxsDataCollectionId).intValue();

		isPyBSAXSDataCollections.get(dataCollectionId).setCollectionStatus(
				status);
	}

	@Override
	public ISpyBStatus getDataCollectionStatus(long saxsDataCollectionId)
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
		ISpyBStatus status = ISpyBStatus.COMPLETE;
		status.setProgress(100);
		status.setFileName("");
		setDataReductionStatus(dataCollectionId, status, "");

		updateModel(dataReductionId);
		return dataReductionId;
	}

	@Override
	public long createDataAnalysis(long dataCollectionId) throws SQLException {
		// Mock up creating a data reduction data base object here
		long dataAnalysisId = 0;
		ISpyBStatus status = ISpyBStatus.COMPLETE;
		status.setProgress(100);
		status.setFileName("");
		setDataAnalysisStatus(dataCollectionId, status, "");

		updateModel(dataCollectionId);
		return dataAnalysisId;
	}

	@Override
	public void setDataReductionStatus(long saxsDataCollectionId,
			ISpyBStatus status, String resultsFilename) throws SQLException {
		int dataCollectionId = ((Long) saxsDataCollectionId).intValue();

		isPyBSAXSDataCollections.get(dataCollectionId).setReductionStatus(
				status);
	}

	@Override
	public ISpyBStatus getDataReductionStatus(long saxsDataCollectionId)
			throws SQLException {
		int dataCollectionId = ((Long) saxsDataCollectionId).intValue();

		return isPyBSAXSDataCollections.get(dataCollectionId)
				.getReductionStatus();
	}

	@Override
	public void setDataAnalysisStatus(long saxsDataCollectionId,
			ISpyBStatus status, String resultsFilename) throws SQLException {
		int dataCollectionId = ((Long) saxsDataCollectionId).intValue();

		isPyBSAXSDataCollections.get(dataCollectionId)
				.setAnalysisStatus(status);
	}

	@Override
	public ISpyBStatus getDataAnalysisStatus(long saxsDataCollectionId)
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
			ISAXSDataCollection bioSaxsDataCollection = new BioSAXSDataCollection();
			bioSaxsDataCollection.setExperimentId(String.valueOf(i));
			bioSaxsDataCollection.setSampleName("Sample " + i);
			bioSaxsDataCollection.setBlSessionId(blSessionId);

			if (!containsId(bioSaxsDataCollection.getSampleName())) {
				isPyBSAXSDataCollections.add(bioSaxsDataCollection);
			}
		}
		return isPyBSAXSDataCollections;
	}

	private boolean containsId(String string) {
		for (ISAXSDataCollection dataCollection : isPyBSAXSDataCollections) {
			if (dataCollection.getSampleName() == string) {
				return true;
			}
		}
		return false;
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
	public void setAnalysisStatus(long dataCollectionId,
			ISpyBStatus analysisStatus) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPreviousCollectionId(long dataCollectionId) {
		// TODO Auto-generated method stub
		return 0;
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
