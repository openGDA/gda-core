package gda.device.detector.nxdetector.andor.proc;

import java.io.IOException;
import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.device.detector.nxdetector.andor.proc.FlatAndDarkFieldPlugin.ScanType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class FlatAndDarkFieldPluginTest {

	private FlatAndDarkFieldPlugin flatAndDarkFieldPluginToTest;
	private InOrder order;
	private static final int DISABLED = 0;
	private static final int ENABLED = 1;

	@Mock
	NDProcess ndProcess;
	@Mock
	NDPluginBase pluginBase;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		this.order = Mockito.inOrder(ndProcess,pluginBase);
		// create FlatAndDarkFieldPlugin to be tested
		flatAndDarkFieldPluginToTest = new FlatAndDarkFieldPlugin(ndProcess);
	}

	@After
	public void tearDown() throws Exception {
		flatAndDarkFieldPluginToTest = null;
	}

	@Test(expected = DeviceException.class)
	public void checkValidBackgroundExceptionIsThrown() throws IOException,Exception {
		short validBackground = 0;
		when(ndProcess.getValidBackground_RBV()).thenReturn(validBackground);
		flatAndDarkFieldPluginToTest.checkValidBackground();
	}

	@Test(expected = DeviceException.class)
	public void checkValidFlatFieldExceptionIsThrown() throws IOException,Exception {
		short validFlatField = 0;
		when(ndProcess.getValidFlatField_RBV()).thenReturn(validFlatField);
		flatAndDarkFieldPluginToTest.checkValidFlatField();
	}

	@Test
	public void disableBackgroundIsSuccessfull() throws Exception{
		flatAndDarkFieldPluginToTest.enableBackground(DISABLED);
		verify(ndProcess).setEnableBackground(DISABLED);
	}

	@Test(expected = DeviceException.class)
	public void enableBackgroundIsNotSuccessfullForNotValidBackground() throws IOException,Exception {
		short validBackground = 0;

		when(ndProcess.getValidBackground_RBV()).thenReturn(validBackground);

		flatAndDarkFieldPluginToTest.enableBackground(ENABLED);
		order.verify(ndProcess).getValidBackground_RBV();
		order.verify(ndProcess).setEnableBackground(ENABLED);
	}

	@Test
	public void enableBackgroundIsSuccessfull() throws IOException,Exception {
		short validBackground = 1;

		when(ndProcess.getValidBackground_RBV()).thenReturn(validBackground);

		flatAndDarkFieldPluginToTest.enableBackground(ENABLED);
		order.verify(ndProcess).getValidBackground_RBV();
		order.verify(ndProcess).setEnableBackground(ENABLED);
	}

	@Test
	public void disableFlatFieldIsSuccessfull() throws Exception{
		flatAndDarkFieldPluginToTest.enableFlatField(DISABLED);
		verify(ndProcess).setEnableFlatField(DISABLED);
	}

	@Test(expected = DeviceException.class)
	public void enableFlatFieldIsNotSuccessfullForNotValidBackground() throws IOException,Exception {
		short validFlatField = 0;

		when(ndProcess.getValidFlatField_RBV()).thenReturn(validFlatField);

		flatAndDarkFieldPluginToTest.enableFlatField(ENABLED);
		order.verify(ndProcess).getValidFlatField_RBV();
		order.verify(ndProcess).setEnableFlatField(ENABLED);
	}

	@Test
	public void enableFlatFieldIsSuccessfull() throws IOException,Exception {
		short validFlatField = 1;

		when(ndProcess.getValidFlatField_RBV()).thenReturn(validFlatField);

		flatAndDarkFieldPluginToTest.enableFlatField(ENABLED);
		order.verify(ndProcess).getValidFlatField_RBV();
		order.verify(ndProcess).setEnableFlatField(ENABLED);
	}

	@Test
	public void enableScaleOffsetClippingIsSuccessfull() throws IOException,Exception {
		flatAndDarkFieldPluginToTest.enableScaleOffsetClipping(ENABLED);

		verify(ndProcess).setEnableOffsetScale(ENABLED);
		verify(ndProcess).setEnableHighClip(ENABLED);
		verify(ndProcess).setEnableLowClip(ENABLED);
	}

	public void prepareForCollectionForDarkOrFlatField() throws Exception{
		prepareForCollectionTestInitialization();
		order.verify(ndProcess.getPluginBase()).enableCallbacks();
		order.verify(ndProcess).setEnableFilter(DISABLED);
		order.verify(ndProcess).setEnableBackground(DISABLED);
		order.verify(ndProcess).setEnableFlatField(DISABLED);
		verify(ndProcess).setEnableOffsetScale(DISABLED);
		verify(ndProcess).setEnableHighClip(DISABLED);
		verify(ndProcess).setEnableLowClip(DISABLED);
	}

	public void prepareForCollectionTestInitialization() throws Exception{
		short validBackground = 1;
		short validFlatField = 1;

		when(ndProcess.getValidBackground_RBV()).thenReturn(validBackground);
		when(ndProcess.getValidFlatField_RBV()).thenReturn(validFlatField);
		Mockito.doReturn(pluginBase).when(ndProcess).getPluginBase();
	}

	@Test
	public void prepareForCollectionForDarkField() throws Exception{
		int numberImagesPerCollection = 1;

		prepareForCollectionTestInitialization();
		flatAndDarkFieldPluginToTest.setScanType(ScanType.DARK_FIELD);
		flatAndDarkFieldPluginToTest.prepareForCollection(numberImagesPerCollection, null);
		prepareForCollectionForDarkOrFlatField();
	}

	@Test
	public void prepareForCollectionForFlatField() throws Exception{
		int numberImagesPerCollection = 1;

		prepareForCollectionTestInitialization();
		flatAndDarkFieldPluginToTest.setScanType(ScanType.FLAT_FIELD);
		flatAndDarkFieldPluginToTest.prepareForCollection(numberImagesPerCollection, null);
		prepareForCollectionForDarkOrFlatField();
	}

	@Test
	public void prepareForCollectionForCorrectedImage() throws Exception{
		int numberImagesPerCollection = 1;

		prepareForCollectionTestInitialization();
		flatAndDarkFieldPluginToTest.setScanType(ScanType.CORRECTED_IMAGES);
		flatAndDarkFieldPluginToTest.prepareForCollection(numberImagesPerCollection, null);
		order.verify(ndProcess.getPluginBase()).enableCallbacks();
		order.verify(ndProcess).setEnableFilter(DISABLED);
		order.verify(ndProcess).getValidBackground_RBV();
		order.verify(ndProcess).setEnableBackground(ENABLED);
		order.verify(ndProcess).getValidFlatField_RBV();
		order.verify(ndProcess).setEnableFlatField(ENABLED);
		verify(ndProcess).setEnableOffsetScale(ENABLED);
		verify(ndProcess).setEnableHighClip(ENABLED);
		verify(ndProcess).setEnableLowClip(ENABLED);
		order.verify(ndProcess).setAutoOffsetScale(ENABLED);
	}

	public void prepareForCollectionForNoCorrectedImage() throws Exception{
		int numberImagesPerCollection = 1;

		prepareForCollectionTestInitialization();
		flatAndDarkFieldPluginToTest.setScanType(ScanType.NO_CORRECTED_IMAGES);
		flatAndDarkFieldPluginToTest.prepareForCollection(numberImagesPerCollection, null);
		order.verify(ndProcess.getPluginBase()).enableCallbacks();
		order.verify(ndProcess).setEnableFilter(DISABLED);
		order.verify(ndProcess).setEnableBackground(DISABLED);
		order.verify(ndProcess).setEnableFlatField(DISABLED);
		verify(ndProcess).setEnableOffsetScale(ENABLED);
		verify(ndProcess).setEnableHighClip(ENABLED);
		verify(ndProcess).setEnableLowClip(ENABLED);
		order.verify(ndProcess).setAutoOffsetScale(ENABLED);
	}

	public void defaultConfigurationAfterScanComplete() throws Exception{
		order.verify(ndProcess).setEnableBackground(DISABLED);
		order.verify(ndProcess).setEnableFlatField(DISABLED);
		verify(ndProcess).setEnableOffsetScale(ENABLED);
		verify(ndProcess).setEnableHighClip(ENABLED);
		verify(ndProcess).setEnableLowClip(ENABLED);
	}

	@Test
	public void completeCollectionForDarkField() throws Exception{
		prepareForCollectionTestInitialization();

		flatAndDarkFieldPluginToTest.setScanType(ScanType.DARK_FIELD);
		flatAndDarkFieldPluginToTest.completeCollection();

		order.verify(ndProcess).setSaveBackground(ENABLED);
		order.verify(ndProcess).getValidBackground_RBV();
		defaultConfigurationAfterScanComplete();
	}

	@Test
	public void completeCollectionForFlatField() throws Exception{
		prepareForCollectionTestInitialization();

		flatAndDarkFieldPluginToTest.setScanType(ScanType.FLAT_FIELD);
		flatAndDarkFieldPluginToTest.completeCollection();

		order.verify(ndProcess).setSaveFlatField(ENABLED);
		order.verify(ndProcess).getValidFlatField_RBV();
		defaultConfigurationAfterScanComplete();
	}

}
