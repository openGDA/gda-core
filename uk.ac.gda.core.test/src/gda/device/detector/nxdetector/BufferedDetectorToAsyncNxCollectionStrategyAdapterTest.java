package gda.device.detector.nxdetector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import gda.device.ContinuousParameters;
import gda.device.Detector;
import gda.device.detector.BufferedDetector;
import gda.device.detector.NXDetectorData;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataDoubleAppender;
import gda.device.detector.nxdata.NXDetectorDataNexusTreeProviderAppender;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@SuppressWarnings("unused") // to avoid warnings about un-thrown exceptions
public class BufferedDetectorToAsyncNxCollectionStrategyAdapterTest {

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Configure test environment
	//

	private static final String MOCK_BUFFERED_DETECTOR_NAME = "MockBufferedDetector";
	private static final Double[] ONE_DOUBLE_VALUE = new Double[] { Double.valueOf(0.0) };
	private static final Double[] TWO_DOUBLE_VALUES = new Double[] { Double.valueOf(1.0), Double.valueOf(2.0) };
	private static final double[] TWO_PRIMITIVE_DOUBLE_VALUES = new double[] { 1.0, 2.0 };

	@Mock
	private BufferedDetector bufferedDetector;

	private InOrder inOrder;
	private BufferedDetectorToAsyncNXCollectionStrategyAdapter adapter;

	@Before
	public void setUp() throws Exception {
		// Set up mock detector
		MockitoAnnotations.initMocks(this);
		inOrder = inOrder(bufferedDetector);

		when(bufferedDetector.getName()).thenReturn(MOCK_BUFFERED_DETECTOR_NAME);

		// Create adapter to test
		adapter = new BufferedDetectorToAsyncNXCollectionStrategyAdapter(bufferedDetector);
	}

	@After
	public void tearDown() throws Exception {
		adapter = null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Test simple methods
	//

	@Test
	public void nameShouldReturnValueFromUnderlyingDetector() throws Exception {
		assertThat(adapter.getName(), is(equalTo(MOCK_BUFFERED_DETECTOR_NAME)));
	}

	@Test
	public void getNumberImagesPerCollectionShouldReturnOne() throws Exception {
		assertThat(adapter.getNumberImagesPerCollection(0.0), is(equalTo(1)));
		assertThat(adapter.getNumberImagesPerCollection(1000000.0), is(equalTo(1)));
	}

	@Test
	public void shouldRequireAsynchronousPlugins() throws Exception {
		assertThat(adapter.requiresAsynchronousPlugins(), is(true));
	}

	@Test(expected=UnsupportedOperationException.class)
	public void prepareForCollectionWithTwoArgumentsShouldThrowUnsupportedOperationException() throws Exception {
		adapter.prepareForCollection(1, null);
	}

	@Test(expected=UnsupportedOperationException.class)
	public void getAcquireTimeShouldThrowUnsupportedOperationException() throws Exception {
		adapter.getAcquireTime();
	}

	@Test(expected=UnsupportedOperationException.class)
	public void getAcquirePeriodShouldThrowUnsupportedOperationException() throws Exception {
		adapter.getAcquirePeriod();
	}

	@Test(expected=UnsupportedOperationException.class)
	public void configureAcquireAndPeriodTimesShouldThrowUnsupportedOperationException() throws Exception {
		adapter.configureAcquireAndPeriodTimes(0);
	}

	@Test
	public void shouldNotGenerateCallbacks() throws Exception {
		assertThat(adapter.isGenerateCallbacks(), is(false));
	}

	@Test
	public void shouldNotRequireCallbacks() throws Exception {
		assertThat(adapter.willRequireCallbacks(), is(false));
	}

	@Test
	public void settingGenerateCallbacksToFalseShouldBeAcceptable() throws Exception {
		adapter.setGenerateCallbacks(false); // should not throw an exception
	}

	@Test(expected=IllegalArgumentException.class)
	public void settingGenerateCallbacksToTrueShouldThrowException() throws Exception {
		adapter.setGenerateCallbacks(true);
	}

	@Test
	public void getStatusShouldReturnValueFromUnderlyingDetector() throws Exception {
		when(bufferedDetector.getStatus()).thenReturn(Detector.IDLE);
		assertThat(adapter.getStatus(), is(equalTo(Detector.IDLE)));
		verify(bufferedDetector).getStatus();
	}

	@Test
	public void waitWhileBusyShouldCallUnderlyingDetector() throws Exception {
		adapter.waitWhileBusy();
		verify(bufferedDetector).waitWhileBusy();
	}

	@Test
	public void atCommandFailureShouldCallUnderlyingDetector() throws Exception {
		adapter.atCommandFailure();
		verify(bufferedDetector).atCommandFailure();
	}

	@Test
	public void stopShouldCallUnderlyingDetector() throws Exception {
		adapter.stop();
		verify(bufferedDetector).stop();
	}

	@Test
	public void getInputStreamNamesShouldReturnExtraNamesFromUnderlyingDetector() throws Exception {
		String[] extraNames = new String[] {  };
		List<String> namesList = Arrays.asList(extraNames);
		when(bufferedDetector.getExtraNames()).thenReturn(extraNames);
		assertThat(adapter.getInputStreamNames(), is(equalTo(namesList)));
	}

	@Test
	public void getInputStreamFormatsShouldReturnOutputFormatsFromUnderlyingDetector() throws Exception {
		String[] outputFormats = new String[] { "%5.5g", "%.8f" };
		List<String> formatsList = Arrays.asList(outputFormats);
		when(bufferedDetector.getOutputFormat()).thenReturn(outputFormats);
		assertThat(adapter.getInputStreamFormats(), is(equalTo(formatsList)));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Test scan logic
	//
	// The methods called on NXCollectionStrategy in a hardware-triggered scan should reproduce the sequence of calls
	// made on a BufferedDetector in a ContinuousScan. The tests in this section check this scan logic.
	//

	@Test
	public void prepareForCollectionShouldSetUpScanCorrectly() throws Exception {
		double collectionTime = 2;
		int numberOfDataPoints = 153;
		adapter.prepareForCollection(collectionTime, numberOfDataPoints, null);

		inOrder.verify(bufferedDetector).setCollectionTime(collectionTime);
		inOrder.verify(bufferedDetector).prepareForCollection();
		inOrder.verify(bufferedDetector).atScanStart();
		inOrder.verify(bufferedDetector).atScanLineStart();
		inOrder.verify(bufferedDetector).clearMemory();

		ArgumentCaptor<ContinuousParameters> continuousParametersArgument = ArgumentCaptor.forClass(ContinuousParameters.class);
		inOrder.verify(bufferedDetector).setContinuousParameters(continuousParametersArgument.capture());
		ContinuousParameters continuousParameters = continuousParametersArgument.getValue();
		assertThat(continuousParameters.getNumberDataPoints(), is(equalTo(numberOfDataPoints)));

		assertThat(adapter.getNumberOfDataPointsAlreadyRead(), is(equalTo(0)));
	}

	@Test
	public void prepareForLineShouldDoNothing() throws Exception {
		adapter.prepareForLine();
		verifyZeroInteractions(bufferedDetector);
	}

	@Test
	public void collectDataShouldCallSetContinuousModeTrue() throws Exception {
		adapter.collectData();
		verify(bufferedDetector).setContinuousMode(true);
	}

	@Test
	public void completeLineShouldSetContinuousModeFalseAndCallAtScanLineEnd() throws Exception {
		adapter.completeLine();
		inOrder.verify(bufferedDetector).setContinuousMode(false);
		inOrder.verify(bufferedDetector).atScanLineEnd();
	}

	@Test
	public void completeCollectionShouldCallAtScanEndAndEndCollection() throws Exception {
		adapter.completeCollection();
		inOrder.verify(bufferedDetector).atScanEnd();
		inOrder.verify(bufferedDetector).endCollection();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Test readout
	//

	@Test(expected=IllegalArgumentException.class)
	public void readZeroShouldThrowIllegalArgumentException() throws Exception {
		adapter.read(0);
	}

	@Test(expected=NoSuchElementException.class)
	public void readShouldThrowWhenBufferedDetectorReturnsNull() throws Exception {
		when(bufferedDetector.getNumberFrames()).thenReturn(1);
		when(bufferedDetector.readFrames(anyInt(), anyInt())).thenReturn(null);
		adapter.read(1);
	}

	@Test(expected=NoSuchElementException.class)
	public void readShouldThrowWhenBufferedDetectorReturnsEmptyArray() throws Exception {
		when(bufferedDetector.getNumberFrames()).thenReturn(1);
		when(bufferedDetector.readFrames(anyInt(), anyInt())).thenReturn(new Object[0]);
		adapter.read(1);
	}

	@Test
	public void readOneShouldReturnOneFrame() throws Exception {
		when(bufferedDetector.getNumberFrames()).thenReturn(1);
		when(bufferedDetector.readFrames(anyInt(), anyInt())).thenReturn(ONE_DOUBLE_VALUE);
		when(bufferedDetector.getExtraNames()).thenReturn(new String[] { "First extra name" });

		List<NXDetectorDataAppender> result = adapter.read(1);
		verify(bufferedDetector).readFrames(0, 0); // BufferedDetector uses inclusive final index
		assertThat(result.size(), is(equalTo(1)));
	}

	@Test(expected=NoSuchElementException.class)
	public void withTwoFramesToCollectReadShouldReturnTwoFramesThenThrowNoSuchElementException() throws Exception {
		int numberOfFramesToCollect = 2;
		ContinuousParameters params = new ContinuousParameters();
		params.setNumberDataPoints(numberOfFramesToCollect);
		when(bufferedDetector.getContinuousParameters()).thenReturn(params);
		when(bufferedDetector.getNumberFrames()).thenReturn(numberOfFramesToCollect);
		when(bufferedDetector.readFrames(anyInt(), anyInt())).thenReturn(ONE_DOUBLE_VALUE);
		when(bufferedDetector.getExtraNames()).thenReturn(new String[] { "First extra name" });

		for (int i = 0; i < numberOfFramesToCollect; i++) {
			adapter.read(1);
		}
		adapter.read(1); // should throw
	}

	@Test
	public void withThreeFramesAvailableReadOneShouldReturnOneFrameEachTime() throws Exception {
		when(bufferedDetector.getNumberFrames()).thenReturn(3);
		when(bufferedDetector.readFrames(anyInt(), anyInt())).thenReturn(ONE_DOUBLE_VALUE);
		when(bufferedDetector.getExtraNames()).thenReturn(new String[] { "First extra name" });

		List<NXDetectorDataAppender> result = adapter.read(1);
		assertThat(result.size(), is(equalTo(1)));
		result = adapter.read(1);
		assertThat(result.size(), is(equalTo(1)));
		result = adapter.read(1);
		assertThat(result.size(), is(equalTo(1)));
	}

	@Test
	public void withThreeFramesAvailableReadTwoShouldReturnTwoFramesThenOne() throws Exception {
		when(bufferedDetector.getNumberFrames()).thenReturn(3);
		when(bufferedDetector.readFrames(0, 1)).thenReturn(TWO_DOUBLE_VALUES);
		when(bufferedDetector.readFrames(2, 2)).thenReturn(ONE_DOUBLE_VALUE);
		when(bufferedDetector.getExtraNames()).thenReturn(new String[] { "First extra name" });

		List<NXDetectorDataAppender> result = adapter.read(2);
		assertThat(result.size(), is(equalTo(2)));
		result = adapter.read(2);
		assertThat(result.size(), is(equalTo(1)));
	}

	@Test
	public void withOneNewFrameAvailableEachTimeReadTwoShouldReturnOneFrameEachTime() throws Exception {
		when(bufferedDetector.getNumberFrames()).thenReturn(1).thenReturn(2).thenReturn(3);
		when(bufferedDetector.readFrames(anyInt(), anyInt())).thenReturn(ONE_DOUBLE_VALUE);
		when(bufferedDetector.getExtraNames()).thenReturn(new String[] { "First extra name" });

		List<NXDetectorDataAppender> result = adapter.read(2);
		assertThat(result.size(), is(equalTo(1)));
		result = adapter.read(2);
		assertThat(result.size(), is(equalTo(1)));
	}

	@Test
	public void readShouldWaitUntilAFrameIsAvailable() throws Exception {
		// This test will take longer than most, since the read() method sleeps when there are no new frames
		when(bufferedDetector.getNumberFrames()).thenReturn(0).thenReturn(0).thenReturn(0).thenReturn(1);
		when(bufferedDetector.readFrames(anyInt(), anyInt())).thenReturn(ONE_DOUBLE_VALUE);
		when(bufferedDetector.getExtraNames()).thenReturn(new String[] { "First extra name" });

		List<NXDetectorDataAppender> result = adapter.read(1);
		assertThat(result.size(), is(equalTo(1)));
	}

	@Test
	public void NXDetectorDataNexusTreeProviderAppenderShouldBeCreatedFromNXDetectorDataObject() throws Exception {
		NXDetectorDataAppender appender = adapter.createDataAppenderFromObject(new NXDetectorData());
		assertThat(appender, is(instanceOf(NXDetectorDataNexusTreeProviderAppender.class)));
	}

	@Test
	public void NXDetectorDataDoubleAppenderShouldBeCreatedFromDoubleObject() throws Exception {
		when(bufferedDetector.getExtraNames()).thenReturn(new String[] { "First extra name" });
		NXDetectorDataAppender appender = adapter.createDataAppenderFromObject(Double.valueOf(0.0));
		assertThat(appender, is(instanceOf(NXDetectorDataDoubleAppender.class)));
	}

	@Test
	public void NXDetectorDataDoubleAppenderShouldBeCreatedFromDoubleArray() throws Exception {
		when(bufferedDetector.getExtraNames()).thenReturn(new String[] { "First extra name", "Second extra name" });
		NXDetectorDataAppender appender = adapter.createDataAppenderFromObject(TWO_PRIMITIVE_DOUBLE_VALUES);
		assertThat(appender, is(instanceOf(NXDetectorDataDoubleAppender.class)));
	}

	@Test(expected=IllegalArgumentException.class)
	public void creatingDataAppenderFromOtherObjectTypeShouldThrowException() throws Exception {
		NXDetectorDataAppender appender = adapter.createDataAppenderFromObject(new Object());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Test full scan
	//

	@Test
	public void successiveScansShouldReadoutWithoutThrowingExceptions() throws Exception {
		// Set up
		int numberOfScans = 3;
		double collectionTime = 2;
		int numberOfDataPoints = 4;
		ContinuousParameters params = new ContinuousParameters();
		params.setNumberDataPoints(numberOfDataPoints);
		when(bufferedDetector.getContinuousParameters()).thenReturn(params);
		when(bufferedDetector.getNumberFrames()).thenReturn(numberOfDataPoints);
		when(bufferedDetector.readFrames(anyInt(), anyInt())).thenReturn(ONE_DOUBLE_VALUE);
		when(bufferedDetector.getExtraNames()).thenReturn(new String[] { "First extra name" });

		// Run scans
		for (int scan = 0; scan < numberOfScans; scan++) {
			adapter.prepareForCollection(collectionTime, numberOfDataPoints, null);
			adapter.collectData();
			for (int dataPoint = 0; dataPoint < numberOfDataPoints; dataPoint++) {
				adapter.read(1);
			}
			adapter.completeLine();
			adapter.completeCollection();
		}
	}
}
