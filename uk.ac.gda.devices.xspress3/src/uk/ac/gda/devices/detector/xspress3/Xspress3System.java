package uk.ac.gda.devices.detector.xspress3;

import gda.device.CounterTimer;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.detector.NXDetectorData;
import gda.device.timer.Tfg;

import org.apache.commons.lang.ArrayUtils;

/**
 * Extension to the Xspress3Detector class which also drives a TFG timer to
 * create the time frames.
 * <p>
 * The TFG unit will be used to create the time frames. It then fans out logic
 * signals to the Xspress3 unit and other detectors if required to control their
 * acquisitions.
 * <p>
 * This assumes it has control of the TFG unit. Xspress3 does not have to be
 * used via this class and can be used more independently from any timing unit
 * using the Xspress3Detector class. This class simply makes it easier to use a
 * combined TFG/Xspress3 system.
 * <p>
 * In a multi-dimensional scan it is assumed that the TFG operates in the
 * innermost dimension of the scan i.e. each line is a complete run through of
 * the frame sets.
 * <p>
 * It is assumed that the TFG is correctly wired up to the Xspress3 unit!
 * 
 * @author rjw82
 * 
 */
public class Xspress3System extends Xspress3Detector implements Detector,
		CounterTimer {

	protected final Timer tfg;
	private boolean frameSetsCreated = false; // as no getters in the Timer
												// interface!!
	private Double[] scanTimes;

	public Xspress3System(Xspress3Controller controller, Timer tfg) {
		super(controller);
		this.tfg = tfg;
	}

	@Override
	public int getMaximumFrames() throws DeviceException {
		int tfgMax = tfg.getMaximumFrames();
		int x3Max = controller.getMaxNumberFrames();
		return x3Max > tfgMax ? x3Max : tfgMax;
	}

	@Override
	public int getCurrentFrame() throws DeviceException {
		return tfg.getCurrentFrame();
	}

	@Override
	public int getCurrentCycle() throws DeviceException {
		return tfg.getCurrentCycle();
	}

	@Override
	public void setCycles(int cycles) throws DeviceException {
		tfg.setCycles(cycles);
	}

	@Override
	public void start() throws DeviceException {
		collectData();
		tfg.start();
	}

	@Override
	public void restart() throws DeviceException {
		tfg.restart();
	}

	@Override
	public void addFrameSet(int frameCount, double requestedLiveTime,
			double requestedDeadTime) throws DeviceException {
		tfg.addFrameSet(frameCount, requestedLiveTime, requestedDeadTime);
		frameSetsCreated = true;
	}

	@Override
	public void addFrameSet(int frameCount, double requestedLiveTime,
			double requestedDeadTime, int deadPort, int livePort,
			int deadPause, int livePause) throws DeviceException {
		tfg.addFrameSet(frameCount, requestedDeadTime, requestedLiveTime,
				deadPort, livePort, deadPause, livePause);
		frameSetsCreated = true;
	}

	@Override
	public void clearFrameSets() throws DeviceException {
		tfg.clearFrameSets();
		frameSetsCreated = false;
	}

	@Override
	public void loadFrameSets() throws DeviceException {
		tfg.loadFrameSets();
	}

	public Double[] getScanTimes() {
		return scanTimes;
	}

	/**
	 * Instead of use frame sets, give this class an array of times of the scan
	 * in advance, so it can set up the time-frames. This will make operation of
	 * the detector much faster than a simple step scan.
	 * 
	 * @param times
	 *            - array of times of the upcoming scan in seconds.
	 */
	public void setScanTimes(Double[] times) {
		this.scanTimes = times;
	}

	@Override
	public double[] readChannel(int startFrame, int frameCount, int channel)
			throws DeviceException {
		NXDetectorData[] readoutFrames = (NXDetectorData[]) readoutFrames(
				startFrame, frameCount + startFrame);
		double[] toReturn = new double[frameCount];
		for (int i = 0; i < frameCount; i++) {
			toReturn[i] = readoutFrames[i].getDoubleVals()[channel];
		}
		return toReturn;
	}

	@Override
	public double[] readFrame(int startChannel, int channelCount, int frame)
			throws DeviceException {
		NXDetectorData readoutFrame = (NXDetectorData) readoutFrames(frame,
				frame)[0];
		Double[] values = readoutFrame.getDoubleVals();
		Double[] toReturn = (Double[]) ArrayUtils.subarray(values,
				startChannel, channelCount + startChannel);
		return ArrayUtils.toPrimitive(toReturn);
	}

	@Override
	public boolean isSlave() throws DeviceException {
		return false;
	}

	@Override
	public void setSlave(boolean slave) throws DeviceException {
		// do nothing. If you do not want this object to be a slave, use the
		// Xspress3Detector clas
	}

	// what methods to setup and run experiments? Readout already from xspress3,
	// but may wish to add a time column...

	@Override
	public void atScanLineStart() throws DeviceException {
		super.atScanLineStart();

		if (usingTimesArray()) {
			clearFrameSets();
			for (int i = 0; i < scanTimes.length; i++) {
				// convert times to milliseconds for da.server
				addFrameSet(1, 0, scanTimes[i] * 1000, 0, 0, -1, 0);
			}
			tfg.setAttribute(Tfg.SOFTWARE_START_AND_TRIG_ATTR_NAME,
					Boolean.TRUE);
		}
		
		if (frameSetsCreated|| usingTimesArray()) {
			tfg.loadFrameSets();
			startRunningXspress3FrameSet();
			tfg.start();
		}
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
		if (usingTimesArray()) {
			tfg.setAttribute(Tfg.SOFTWARE_START_AND_TRIG_ATTR_NAME,
					Boolean.FALSE);
		}
		super.atScanLineEnd();
	}

	private boolean usingTimesArray() {
		return frameSetsCreated == false && scanTimes != null && scanTimes.length > 0;
	}

	@Override
	public void collectData() throws DeviceException {
		if (frameSetsCreated || usingTimesArray()) {
			tfg.restart();
		} else {
			// create a run a single frame set
			tfg.clearFrameSets();
			tfg.addFrameSet(1, 0, getCollectionTime() * 1000);
			tfg.setAttribute(Tfg.SOFTWARE_START_AND_TRIG_ATTR_NAME,
					Boolean.FALSE);
			super.atScanLineStart();
			setNumberOfFramesToCollect(1);
			startRunningXspress3FrameSet();
			tfg.start();
		}
	}

	@Override
	public int getStatus() throws DeviceException {
		int tfgStatus = tfg.getStatus();
		int x3Status = super.getStatus();
		return x3Status > tfgStatus ? x3Status : tfgStatus;
	}

}
