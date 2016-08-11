package uk.ac.gda.devices.bssc.ispyb;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.List;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import gda.factory.FactoryException;
import uk.ac.gda.devices.bssc.beans.BioSAXSProgressController;
import uk.ac.gda.devices.bssc.beans.ISAXSProgress;

public class BioSAXSProgressModelTest {
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
		bioSAXSISPyB = new MockBioSAXSISPyB();
		controller = new BioSAXSProgressController();

		realm = new DefaultRealm();
		realm.exec(new Runnable() {
			@Override
			public void run() {
				visit = "nt20-12";
				try {
					blSessionId = bioSAXSISPyB.getSessionForVisit(visit);
					experimentId = bioSAXSISPyB.createExperiment(blSessionId,
							"test", "TEMPLATE", "test");
					model = new WritableList();
					controller.setModel(model);
					controller.setISpyBAPI(bioSAXSISPyB);
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (FactoryException e) {
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
				.loadModelFromISpyB(iSpyBSAXSDataCollections);
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
			ISpyBStatusInfo modelCollectionStatus = ((ISpyBStatusInfo) modelProgressItem
					.getCollectionStatusInfo());
			assertEquals(expectedCollectionStatus, modelCollectionStatus.getStatus());

			// Check reduction status of model object is the same as the status
			// in ISpyB
			ISpyBStatus expectedReductionStatus = (ISpyBStatus) iSpyBSAXSDataCollections
					.get(i).getReductionStatus().getStatus();
			ISpyBStatusInfo modelReductionStatus = ((ISpyBStatusInfo) modelProgressItem
					.getReductionStatusInfo());
			assertEquals(expectedReductionStatus, modelReductionStatus.getStatus());

			// Check collection progress in model is same as collection progress
			// in ISpyB
			double expectedCollectionProgress = iSpyBSAXSDataCollections.get(i)
					.getCollectionStatus().getProgress();
			ISpyBStatusInfo modelCollectionProgress = modelProgressItem
					.getCollectionStatusInfo();
			assertEquals(expectedCollectionProgress, modelCollectionProgress.getProgress(),
					0.0);

			// Check reduction progress in model is same as reduction progress
			// in ISpyB
			double expectedReductionProgress = iSpyBSAXSDataCollections.get(i)
					.getReductionStatus().getProgress();
			ISpyBStatusInfo modelReductionProgress = modelProgressItem
					.getReductionStatusInfo();
			assertEquals(expectedReductionProgress, modelReductionProgress.getProgress(), 0.0);

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
		ISAXSProgress modelProgressItem = (ISAXSProgress) model.get(modelIndex);
		assertEquals(33, modelProgressItem.getCollectionStatusInfo().getProgress(), 0.0);
		assertEquals(0, modelProgressItem.getReductionStatusInfo().getProgress(), 0.0);
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

		ISAXSProgress modelProgressItem = (ISAXSProgress) model.get(modelIndex);
		assertEquals(66, modelProgressItem.getCollectionStatusInfo().getProgress(), 0.0);
		assertEquals(0, modelProgressItem.getReductionStatusInfo().getProgress(), 0.0);
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
		ISAXSProgress modelProgressItem = (ISAXSProgress) model.get(modelIndex);
		assertEquals(100, modelProgressItem.getCollectionStatusInfo().getProgress(), 0.0);
		assertEquals(0, modelProgressItem.getReductionStatusInfo().getProgress(), 0.0);
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
		ISAXSProgress modelProgressItem = (ISAXSProgress) model.get(modelIndex);
		assertEquals(66, modelProgressItem.getCollectionStatusInfo().getProgress(), 0.0);
		assertEquals(100, modelProgressItem.getReductionStatusInfo().getProgress(), 0.0);
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



class MockSAXSDataCollection implements ISAXSDataCollection {

	private String sampleName;
	private ISpyBStatusInfo collectionStatus;
	private ISpyBStatusInfo reductionStatus;
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
	public void setCollectionStatus(ISpyBStatusInfo collectionStatus) {
		this.collectionStatus = collectionStatus;
	}

	@Override
	public void setReductionStatus(ISpyBStatusInfo reductionStatus) {
		this.reductionStatus = reductionStatus;
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
	@Override
	public boolean isCurrent() {
		return true;
	}

	@Override
	protected void syncExec(Runnable runnable) {
		runnable.run();
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
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
