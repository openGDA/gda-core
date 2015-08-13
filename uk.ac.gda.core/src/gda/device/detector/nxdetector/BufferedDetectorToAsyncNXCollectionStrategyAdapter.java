package gda.device.detector.nxdetector;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.detector.BufferedDetector;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataDoubleAppender;
import gda.device.detector.nxdata.NXDetectorDataNexusTreeProviderAppender;
import gda.scan.ScanInformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Adapter to use a BufferedDetector as a collection strategy in NXDetector.
 * <p>
 * Due to the available hooks not matching precisely, with the current NXDetector code, the scan setup is all done in
 * prepareForCollection() and will happen at least twice for each scan line. The underlying BufferedDetector needs to
 * cope with having atScanStart() and atScanLineStart() called repeatedly, and more times than atScanLineEnd() and
 * atScanEnd().
 * <p>
 * If multiple lines are being scanned, prepareForCollection(), atScanStart(), atScanLineStart() and atScanLineEnd()
 * will be called for each one, but atScanEnd() and endCollection() will only be called at the end of the last line. If
 * this causes problems, a simple fix would be to move the method calls from completeCollection() into completeLine().
 * <p>
 * This class was written along with a complete set of unit tests which specify the behaviour quite precisely. If you
 * change anything in this class, make sure you change the tests to match.
 */
public class BufferedDetectorToAsyncNXCollectionStrategyAdapter implements AsyncNXCollectionStrategy {

	private BufferedDetector bufferedDetector;
	private int framesAlreadyRead = 0;

	public BufferedDetectorToAsyncNXCollectionStrategyAdapter(BufferedDetector bufferedDetector) {
		this.bufferedDetector = bufferedDetector;
	}

	@Override
	public double getAcquireTime() throws Exception {
		throw new UnsupportedOperationException("This adapter can only be used in hardware-triggered scans");
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		throw new UnsupportedOperationException("This adapter can only be used in hardware-triggered scans");
	}

	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		throw new UnsupportedOperationException("This adapter can only be used in hardware-triggered scans");
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		throw new UnsupportedOperationException("Must be operated via prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo)");
	}

	/*
	 * This method is confusing! It is called from NXDetector.prepareCollectionStrategyAtScanStart() using
	 * numberImagesPerCollection to mean the number of images taken by the detector at each scan point. In a hardware-
	 * triggered scan, it is then called later from HardwareTriggeredNXDetector.collectData() using
	 * numberImagesPerCollection to mean the total number of points in the current scan line, i.e. the number of
	 * hardware triggers the detector should expect.
	 *
	 * In this adapter, it is probably safe to just pass the numberImagesPerCollection on to the detector as the
	 * number of points, but if the underlying BufferedDetector's setContinuousParameters methods has side effects
	 * then this could cause strange behaviour.
	 */
	@Override
	public void prepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo)
			throws Exception {
		bufferedDetector.setCollectionTime(collectionTime);
		bufferedDetector.prepareForCollection();
		bufferedDetector.atScanStart();
		bufferedDetector.atScanLineStart();
		bufferedDetector.clearMemory();
		framesAlreadyRead = 0;

		// Number of data points is the only field of ContinuousParameters used by most BufferedDetectors
		// Could also perhaps set total time with a rough estimate by multiplying collection time and number of points?
		// Not sure this is necessary for now though
		ContinuousParameters continuousParameters = new ContinuousParameters();
		continuousParameters.setNumberDataPoints(numberImagesPerCollection);

		bufferedDetector.setContinuousParameters(continuousParameters);
	}

	/*
	 * Do nothing in this method! In a hardware-triggered scan, HardwareTriggeredNXDetector will call
	 * prepareForCollection() for a second time, after prepareForLine() and immediately before collectData(), so all
	 * necessary scan and scan line setup must be done in prepareForCollection()
	 */
	@Override
	public void prepareForLine() throws Exception {
		// Do nothing
	}

	@Override
	public void completeLine() throws Exception {
	}

	@Override
	public void completeCollection() throws Exception {
		bufferedDetector.setContinuousMode(false);
		bufferedDetector.atScanLineEnd();
		bufferedDetector.atScanEnd();
		bufferedDetector.endCollection();
	}

	@Override
	public void collectData() throws Exception {
		bufferedDetector.setContinuousMode(true);
	}

	@Override
	public int getStatus() throws Exception {
		return bufferedDetector.getStatus();
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, Exception {
		bufferedDetector.waitWhileBusy();
	}

	/**
	 * This adapter does not support generation of callbacks. Currently it throws an exception if callbacks are
	 * requested, but it might need to be changed to simply ignore the request if this adapter is required to work
	 * alongside other NXPlugins which require callbacks and the callbacks can be generated in some other way.
	 *
	 * @param generateCallbacks must be <code>false</code>
	 */
	@Override
	public void setGenerateCallbacks(boolean generateCallbacks) {
		if (generateCallbacks) {
			throw new IllegalArgumentException("Not an AreaDetector, cannot generate callbacks");
		}
	}

	@Override
	public boolean isGenerateCallbacks() {
		return false;
	}

	@Override
	public int getNumberImagesPerCollection(double collectionTime) throws Exception {
		return 1;
	}

	@Override
	public boolean requiresAsynchronousPlugins() {
		return true;
	}

	@Override
	public String getName() {
		return bufferedDetector.getName();
	}

	@Override
	public boolean willRequireCallbacks() {
		return false;
	}

	@Override
	public void atCommandFailure() throws Exception {
		bufferedDetector.atCommandFailure();
	}

	@Override
	public void stop() throws Exception {
		bufferedDetector.stop();
	}

	@Override
	public List<String> getInputStreamNames() {
		return Arrays.asList(bufferedDetector.getExtraNames());
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList(bufferedDetector.getOutputFormat());
	}

	@Override
	public synchronized List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException,
			InterruptedException, DeviceException {
		if (maxToRead < 1) {
			throw new IllegalArgumentException("Number of elements to read must be at least one");
		}
		if (bufferedDetector.getContinuousParameters() != null) {
			if (framesAlreadyRead >= bufferedDetector.getContinuousParameters().getNumberDataPoints()) {
				throw new NoSuchElementException("Cannot read more points - all data points already read");
			}
		}

		// Wait until new frames are available
		int newFramesAvailable;
		while ((newFramesAvailable = getNewFramesAvailable()) <= 0) {
			Thread.sleep(100);
		}

		// Read the new frames from the detector
		int framesToRead = Math.min(newFramesAvailable, maxToRead);
		int lastFrameToRead = framesAlreadyRead + framesToRead;
		// Subtract one from last frame index when calling readFrames because BufferedDetector uses an inclusive
		// final index. (We could instead subtract it when assigning the value to lastFrameToRead, but would then need
		// to add it back when updating the value of framesAlreadyRead below. This way is simpler, but still confusing!)
		Object[] newFrames = bufferedDetector.readFrames(framesAlreadyRead, lastFrameToRead - 1);

		if (newFrames == null || newFrames.length == 0) {
			throw new NoSuchElementException(bufferedDetector.getName() + " has returned no frames");
		}
		framesAlreadyRead = lastFrameToRead;

		// Create NXDetectorDataAppenders from the new frames and return them in a list
		List<NXDetectorDataAppender> appenders = new ArrayList<NXDetectorDataAppender>(newFrames.length);
		for (Object frame : newFrames) {
			appenders.add(createDataAppenderFromObject(frame));
		}
		return appenders;
	}

	private int getNewFramesAvailable() throws DeviceException {
		int totalFramesAvailable = bufferedDetector.getNumberFrames();
		return totalFramesAvailable - framesAlreadyRead;
	}

	/**
	 * Create an NXDetectorDataAppender according to the type of object given.
	 * <p>
	 * This method has package-level visibility to allow access by JUnit only.
	 *
	 * @param dataObject a member of the array returned from a BufferedDetector's readFrames() method
	 * @return an appropriate NXDetectorDataAppender
	 */
	NXDetectorDataAppender createDataAppenderFromObject(Object dataObject) {
		NXDetectorDataAppender appender = null;

		// Xspress3BufferedDetector returns NXDetectorData objects. Xspress2BufferedDetector, XmapBufferedDetector and
		// DummyXmapBufferedDetector return NexusTreeProvider objects (which might really be NXDetectorData objects but
		// are not declared as such). Both types will be handled correctly by an NXDetectorDataNexusTreeProviderAppender
		if (dataObject instanceof NexusTreeProvider) {
			appender = new NXDetectorDataNexusTreeProviderAppender((NexusTreeProvider)dataObject);
		}
		// Xspress3FFoverI0BufferedDetector, VortexQexafsFFIO, QexafsGMSDOverI0 and QexafsFFoverIO return Doubles
		// Creation of NXDetectorDataDoubleAppender will fail if getInputStreamNames().size() != 1
		else if (dataObject instanceof Double) {
			appender = new NXDetectorDataDoubleAppender(getInputStreamNames(), Arrays.asList((Double)dataObject));
		}
		// BufferedScaler returns double[] objects
		// Creation of NXDetectorDataDoubleAppender will fail if getInputStreamNames().size() != doubleArray.length
		else if (dataObject instanceof double[]) {
			double[] doubleArray = (double[]) dataObject;
			List<Double> valuesList = new ArrayList<Double>(doubleArray.length);
			for (double value : doubleArray) {
				valuesList.add(Double.valueOf(value));
			}
			appender = new NXDetectorDataDoubleAppender(getInputStreamNames(), valuesList);
		}
		// The only other known BufferedDetector is EpicsScanData, which returns int[] but is apparently not used and
		// so this case is ignored.
		else {
			throw new IllegalArgumentException("Unknown readout object type: " + dataObject.getClass().getSimpleName());
		}
		return appender;
	}

	/**
	 * @return The number of data points/frames already read from the detector
	 */
	public int getNumberOfDataPointsAlreadyRead() {
		return framesAlreadyRead;
	}
}
