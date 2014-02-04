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

public class BioSAXSMockISpyBTest {
	private static IProgressModel model;
	private static BioSAXSProgressController controller;
	private static BioSAXSISPyB bioSAXSISPyB;
	private static final int MODEL_SIZE = 7;
	private static DefaultRealm realm;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		bioSAXSISPyB = new MyBioSAXSISPy();
		realm = new DefaultRealm();
		realm.exec(new Runnable() {
			@Override
			public void run() {
				model = new BioSAXSProgressModel();
				OSGIServiceRegister modelReg = new OSGIServiceRegister();
				modelReg.setClass(IProgressModel.class);
				modelReg.setService(model);

				controller = new BioSAXSProgressController(model);
				bioSAXSISPyB = new MyBioSAXSISPy();
				controller.setISpyBAPI(bioSAXSISPyB);

				// modelReg.afterPropertiesSet();

			}
		});
	}

	@Test
	public void testInitSAXSDataCollections() {
		String visit = "nt20-12";
		long blsessionId;
		long experimentId = 0;

		try {
			blsessionId = bioSAXSISPyB.getSessionForVisit(visit);
			experimentId = bioSAXSISPyB.createExperiment(blsessionId, "test",
					"TEMPLATE", "test");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		controller.loadModelFromISPyB();

		assertEquals(MODEL_SIZE, model.getItems().size());
	}

	@Test
	public void testRunSAXSDataCollections() throws Exception {
		ObservableList items = (ObservableList) model.getItems();
		for (int i = 0; i < model.size(); i++) {
			items.add(model.get(i));
		}

		for (long dataCollectionId = 0; dataCollectionId < items.size(); dataCollectionId++) {
			ISAXSDataCollection dataCollection = (ISAXSDataCollection) items
					.get(((Long) dataCollectionId).intValue());

			ISpyBStatus status = bioSAXSISPyB
					.getDataCollectionStatus(dataCollectionId);
			assertEquals(ISpyBStatus.NOT_STARTED, status);

			String bufferBeforeFile = "/dls/b21/data/2013/sm999-9/b21-9990.nxs";
			String bufferBeforePath = "/entry1/detector/data";
			long bufferBeforeRun = bioSAXSISPyB.createBufferRun(-1,
					dataCollectionId, 1.0, 20.0f, 20.0f, 10.0, 10, 1.0, 1.0,
					1.0, 1.0, 1.0, 1.0, 1.0, 1.0, bufferBeforeFile,
					bufferBeforePath);
			status = ISpyBStatus.RUNNING;
			status.setProgress(33);
			status.setFileName(bufferBeforeFile);
			bioSAXSISPyB.setDataCollectionStatus(dataCollectionId, status);
			status = bioSAXSISPyB.getDataCollectionStatus(dataCollectionId);
			assertEquals(ISpyBStatus.RUNNING, status);
			assertEquals(33, status.getProgress());
			assertEquals(bufferBeforeFile, status.getFileName());

			String sampleFile = "/dls/b21/data/2013/sm999-9/b21-9991.nxs";
			String samplePath = "/entry1/detector/data";
			long sampleRun = bioSAXSISPyB.createSampleRun(dataCollectionId,
					1.0, 20.0f, 20.0f, 10.0, 10, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
					1.0, 1.0, sampleFile, samplePath);
			status = ISpyBStatus.RUNNING;
			status.setProgress(66);
			status.setFileName(sampleFile);
			bioSAXSISPyB.setDataCollectionStatus(dataCollectionId, status);
			status = bioSAXSISPyB.getDataCollectionStatus(dataCollectionId);
			assertEquals(ISpyBStatus.RUNNING, status);
			assertEquals(66, status.getProgress());
			assertEquals(sampleFile, status.getFileName());

			String bufferAfterFile = "/dls/b21/data/2013/sm999-9/b21-9992.nxs";
			String bufferAfterPath = "/entry1/detector/data";
			long bufferAfterRun = bioSAXSISPyB.createBufferRun(-1,
					dataCollectionId, 1.0, 20.0f, 20.0f, 10.0, 10, 1.0, 1.0,
					1.0, 1.0, 1.0, 1.0, 1.0, 1.0, bufferAfterFile,
					bufferAfterPath);
			status = ISpyBStatus.COMPLETE;
			status.setProgress(100);
			status.setFileName(bufferAfterFile);
			bioSAXSISPyB.setDataCollectionStatus(dataCollectionId, status);
			status = bioSAXSISPyB.getDataCollectionStatus(dataCollectionId);
			assertEquals(ISpyBStatus.COMPLETE, status);
			assertEquals(100, dataCollection.getCollectionProgress());
			assertEquals(bufferAfterFile, status.getFileName());

			status = ISpyBStatus.NOT_STARTED;
			String reductionFileName = "/dls/b21/data/2013/sm999-9/b21-9993.nxs";
			bioSAXSISPyB.setDataReductionStatus(dataCollectionId, status,
					reductionFileName);
			bioSAXSISPyB.createDataReduction(dataCollectionId);
			status = ISpyBStatus.RUNNING;
			bioSAXSISPyB.setDataReductionStatus(dataCollectionId, status,
					reductionFileName);
			assertEquals(status,
					bioSAXSISPyB.getDataReductionStatus(dataCollectionId));
			status = ISpyBStatus.COMPLETE;
			bioSAXSISPyB.setDataReductionStatus(dataCollectionId, status,
					reductionFileName);
			assertEquals(status,
					bioSAXSISPyB.getDataReductionStatus(dataCollectionId));

			status = ISpyBStatus.NOT_STARTED;
			String analysisFileName = "/dls/b21/data/2013/sm999-9/b21-9994.nxs";
			bioSAXSISPyB.setDataAnalysisStatus(dataCollectionId, status,
					analysisFileName);
			bioSAXSISPyB.createDataAnalysis(dataCollectionId);
			status = ISpyBStatus.RUNNING;
			bioSAXSISPyB.setDataAnalysisStatus(dataCollectionId, status,
					analysisFileName);
			assertEquals(status,
					bioSAXSISPyB.getDataAnalysisStatus(dataCollectionId));
			status = ISpyBStatus.COMPLETE;
			bioSAXSISPyB.setDataReductionStatus(dataCollectionId, status,
					analysisFileName);
			assertEquals(status,
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

	private List<ISAXSDataCollection> dataCollections;

	public MyBioSAXSISPy() {
		dataCollections = new ArrayList<ISAXSDataCollection>();
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

		long beforeBufferId = createBufferMeasurement(dataCollectionId,
				bufferPlate, bufferRow, bufferColumn, exposureTemperature,
				numFrames, timePerFrame, flow, volume, energyInkeV, viscosity);
		createMeasurementToDataCollection(dataCollectionId, beforeBufferId);

		long sampleId = createSampleMeasurement(dataCollectionId, plate, row,
				column, exposureTemperature, numFrames, energyInkeV,
				energyInkeV, energyInkeV, energyInkeV, viscosity);
		createMeasurementToDataCollection(dataCollectionId, sampleId);

		long bufferAfterId = createBufferMeasurement(dataCollectionId,
				bufferPlate, bufferRow, bufferColumn, exposureTemperature,
				numFrames, timePerFrame, flow, volume, energyInkeV, viscosity);
		createMeasurementToDataCollection(dataCollectionId, bufferAfterId);

		return dataCollectionId;
	}

	@Override
	public long createBufferMeasurement(long dataCollectionId, short plate,
			short row, short column, float exposureTemperature, int numFrames,
			double timePerFrame, double flow, double volume,
			double energyInkeV, String viscosity) throws SQLException {
		long bufferBeforeMeasurementId = 0;

		return bufferBeforeMeasurementId;
	}

	@Override
	public long createSampleMeasurement(long dataCollectionId, short plate,
			short row, short column, float exposureTemperature, int numFrames,
			double timePerFrame, double flow, double volume,
			double energyInkeV, String viscosity) throws SQLException {
		long sampleMeasurementId = 0;

		return sampleMeasurementId;
	}

	@Override
	public long createMeasurementToDataCollection(long saxsDataCollectionId,
			long measurementId) throws SQLException {
		long bufferAfterMeasurementId = 0;

		return bufferAfterMeasurementId;
	}

	@Override
	public long createBufferRun(long previousDataCollectionId,
			long currentDataCollectionId, double timePerFrame,
			float storageTemperature, float exposureTemperature, double energy,
			int frameCount, double transmission, double beamCenterX,
			double beamCenterY, double pixelSizeX, double pixelSizeY,
			double radiationRelative, double radiationAbsolute,
			double normalization, String filename, String internalPath) {
		return currentDataCollectionId;
	}

	@Override
	public long createSampleRun(long dataCollectionId, double timePerFrame,
			float storageTemperature, float exposureTemperature, double energy,
			int frameCount, double transmission, double beamCenterX,
			double beamCenterY, double pixelSizeX, double pixelSizeY,
			double radiationRelative, double radiationAbsolute,
			double normalization, String filename, String internalPath) {
		return dataCollectionId;
	}

	@Override
	public void setMeasurementStatus(long dataCollectionId, long measurementId,
			ISpyBStatus status) {
		// TODO Auto-generated method stub

	}

	@Override
	public ISpyBStatus getMeasurementStatus(long measurementId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDataCollectionStatus(long saxsDataCollectionId,
			ISpyBStatus status) {
		// Mock up setting database object here
		int dataCollectionId = ((Long) saxsDataCollectionId).intValue();
		dataCollections.get(dataCollectionId).setCollectionStatus(status);
	}

	@Override
	public ISpyBStatus getDataCollectionStatus(long saxsDataCollectionId)
			throws SQLException {
		// Mock up getting item from the database here
		int dataCollectionId = ((Long) saxsDataCollectionId).intValue();
		return dataCollections.get(dataCollectionId).getCollectionStatus();
	}

	@Override
	public long createDataReduction(long dataCollectionId) throws SQLException {
		// Mock up creating a data reduction data base object here
		long dataReductionId = 0;
		return dataReductionId;
	}

	@Override
	public long createDataAnalysis(long dataCollectionId) throws SQLException {
		// Mock up creating a data reduction data base object here
		long dataAnalysisId = 0;
		return dataAnalysisId;
	}

	@Override
	public void setDataReductionStatus(long saxsDataCollectionId,
			ISpyBStatus status, String resultsFilename) throws SQLException {
		int dataCollectionId = ((Long) saxsDataCollectionId).intValue();
		dataCollections.get(dataCollectionId).setReductionStatus(status);
	}

	@Override
	public ISpyBStatus getDataReductionStatus(long saxsDataCollectionId)
			throws SQLException {
		int dataCollectionId = ((Long) saxsDataCollectionId).intValue();
		return dataCollections.get(dataCollectionId).getReductionStatus();
	}

	@Override
	public void setDataAnalysisStatus(long saxsDataCollectionId,
			ISpyBStatus status, String resultsFilename) throws SQLException {
		int dataCollectionId = ((Long) saxsDataCollectionId).intValue();
		dataCollections.get(dataCollectionId).setAnalysisStatus(status);
	}

	@Override
	public ISpyBStatus getDataAnalysisStatus(long saxsDataCollectionId)
			throws SQLException {
		int dataCollectionId = ((Long) saxsDataCollectionId).intValue();
		return dataCollections.get(dataCollectionId).getAnalysisStatus();
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<ISAXSDataCollection> getBioSAXSMeasurements(long blSessionId)
			throws SQLException {
		for (int i = 0; i < 7; i++) {
			BioSAXSDataCollection bioSaxsDataCollection = new BioSAXSDataCollection();
			bioSaxsDataCollection.setExperimentId(String.valueOf(i));
			bioSaxsDataCollection.setSampleName("Sample " + i);
			bioSaxsDataCollection.setBlSessionId(blSessionId);

			if (!containsId(bioSaxsDataCollection.getSampleName())) {
				dataCollections.add(bioSaxsDataCollection);
			}
		}
		return dataCollections;
	}

	private boolean containsId(String string) {
		for (ISAXSDataCollection dataCollection : dataCollections) {
			if (dataCollection.getSampleName() == string) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ISAXSDataCollection getDataCollection(long blSessionId,
			long dataCollectionId) throws SQLException {
		// TODO Auto-generated method stub
		return null;
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
