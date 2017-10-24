/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.device.detector.countertimer;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.Timer;
import gda.device.TimerStatus;
import gda.device.timer.DummyTfg;
import gda.device.timer.FrameSet;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.observable.IObserver;
import gda.scan.Scan;
import gda.util.Gaussian;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Can be used as a substitute for a CounterTimer for testing purposes. Not all the methods work though it can be used
 * in scans and will produce either totally random data or a set of Gaussians depending on the setting of the
 * useGaussian flag. The widths and heights of the Gaussians and a level of random noise applied to them are all
 * controlled by xml parameters.
 */
public class DummyCounterTimer extends TFGCounterTimer implements Runnable, IObserver, Scannable {

	private static final Logger logger = LoggerFactory.getLogger(DummyCounterTimer.class);

	private int totalChans = 8;

	private int state = Detector.IDLE;

	private Thread runner;

	private boolean waiting = false;

	private long sleepTime;

	private boolean simulatedCountRequired = false;

	private double data[];

	private ArrayList<double[]> continuousScanData;

	private ArrayList<FrameSet> frameSets;

	private boolean timeChannelRequired = false;

	private int pointCounter = 0;

	private int incrementCounter = 1;

	private boolean useGaussian = false;

	private Gaussian gaussian;

	private double gaussianPosition = 10.0;

	private double gaussianWidth = 5.0;

	private double gaussianHeight = 10.0;

	private double noiseLevel = 0.1;

	private String tfgName = null;

	private int frameBeingDone;

	private boolean continuousScanInProgress = false;

	private int dataDecimalPlaces = 2;

	private double roundingFactor = Math.pow(10.0, dataDecimalPlaces);

	private boolean readoutFileName = false;

	/**
	 * Constructor.
	 */
	public DummyCounterTimer() {
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();

		runner = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName() + " " + getName());
		runner.start();

		// Wait for the runner thread to start before returning (i.e. declaring
		// DummyCounterTimer to be constructed).
		while (!waiting) {
			Thread.yield();
		}
		Thread.yield();

		// A tfgName can be specified if you want several DummyCounterTimers to
		// use the same Tfg (which can be real or dummy). However if a name is
		// not
		// specified the DummyCounterTimer should still function so in that
		// situation it creates a DummyTfg for itself. In that situation the XML
		// set value of slave is overriden.
		if (tfgName != null) {
			timer = (Timer) Finder.getInstance().find(tfgName);
		} else {
			timer = new DummyTfg();
			((Configurable) timer).configure();
			slave = false;
		}
		timer.addIObserver(this);
		logger.debug("The name of this CounterTimer is " + getName());
		data = new double[totalChans];
		frameSets = new ArrayList<FrameSet>();

	}

	@Override
	public void collectData() throws DeviceException {
		countAsync(collectionTime);
	}

	@Override
	public Object readout() throws DeviceException {
		String s = "blahdiblah";
		if (readoutFileName) {
			return s;
		}
		double[] values = data;

		if (timeChannelRequired) {
			values = new double[data.length];
			values[0] = getCollectionTime();
			// Data is always rescaled so that a collectionTime of 1000.0 gives default answers. This helps in detecting
			// problems in scans.
			for (int i = 1; i < values.length; i++) {
				values[i] = data[i - 1] * values[0] / 1000.0;
			}
		}

		// This method is called at each point in a scan and so can
		// be used to increment the point counter. In useGaussian mode
		// what is actually produced is a set of Gaussians, the first
		// centered on gaussianPosition, the next on gaussianPosition plus
		// two times gaussianWidth and so on. Each gaussian is also higher
		// than the previous.
		pointCounter++;
		if (useGaussian) {
			resetGaussian();
		}
		logger.debug("DummyCounterTimer " + getName() + " readout() returning: " + valuesToString(values));
		return values;

	}

	private void resetGaussian() {
		logger.debug("%%%%%%% pointCounter now " + pointCounter);
		if (pointCounter == (int) gaussianPosition + 2 * (int) gaussianWidth) {
			gaussianPosition += 4.0 * gaussianWidth;
			incrementCounter++;
			gaussian = new Gaussian(gaussianPosition, gaussianWidth, incrementCounter * gaussianHeight);
		}
	}

	/**
	 * Returns the data array converted to a String (mostly for debugging purposes
	 *
	 * @param values
	 *            the values to convert
	 * @return a string version of the data array
	 */
	private String valuesToString(double[] values) {
		String string = "";
		for (double element : values) {
			string = string + element + " ";
		}

		return string;

	}

//	/**
//	 * @see gda.device.CounterTimer#getTotalChans()
//	 * @param totalChans
//	 */
//	public void setTotalChans(int totalChans) {
//		this.totalChans = totalChans;
//		data = new double[totalChans];
//	}
//
//	public int getTotalChans() throws DeviceException {
//		return totalChans;
//	}

	public void countAsync(double time) {
		logger.debug("DummyCounterTimer " + getName() + " countAsync called with time: " + time);
		state = Detector.BUSY;
		sleepTime = (long) time;

		// Initialize gaussian if necessary (as in setCollectionTime)
		if (useGaussian && gaussian == null) {
			pointCounter = 0;
			incrementCounter = 1;
			gaussianPosition = 10.0;

			gaussian = new Gaussian(gaussianPosition, gaussianWidth, gaussianHeight);
		}
		synchronized (this) {
			simulatedCountRequired = true;
			notifyAll();
		}
	}

	@Override
	public int getStatus() throws DeviceException {
		// In StepScans the state is determined in the countAsync() method,
		// in ContinuousScans the scan will have been set up using addFrameSet()
		// and the state is determined by the dummy tfg.
		int status = state;
		if (continuousScanInProgress) {
			status = timer.getStatus();
		}
		return status;
	}

	/**
	 * @return boolean timeChannelRequired
	 */
	public boolean isTimeChannelRequired() {
		return timeChannelRequired;
	}

	/**
	 * @param timeChannelRequired
	 */
	public void setTimeChannelRequired(boolean timeChannelRequired) {
		this.timeChannelRequired = timeChannelRequired;
	}

	@Override
	public void start() throws DeviceException {
		frameBeingDone = 0;
		if (!slave) {
			timer.start();
		}
	}


	@Override
	public void addFrameSet(int frameCount, double requestedLiveTime, double requestedDeadTime) throws DeviceException {
		if (!slave) {
			timer.addFrameSet(frameCount, requestedDeadTime, requestedLiveTime);
		}

		frameSets.add(new FrameSet(frameCount, requestedDeadTime, requestedLiveTime));
		// This is the method which will be called by ContinuousScan when
		// it is setting up. The collectionTime is used to normalize the
		// so it is set even though it is not used during the scan.
		setCollectionTime(requestedLiveTime);

		// Create the ArrayList for the data frames. Some dummy dummy data is
		// added to position 0 so that we can put frame 1 in position 1 and
		// so on.
		pointCounter = 0;
		continuousScanInProgress = true;
		continuousScanData = new ArrayList<double[]>();
		continuousScanData.add(new double[1]);
	}

	@Override
	public void addFrameSet(int frameCount, double requestedLiveTime, double requestedDeadTime, int deadPort,
			int livePort, int deadPause, int livePause) throws DeviceException {
		if (!slave) {
			timer.addFrameSet(frameCount, requestedDeadTime, requestedLiveTime, deadPort, livePort, deadPause, livePause);
		}

		frameSets.add(new FrameSet(frameCount, requestedDeadTime, requestedLiveTime));
		// This is the method which will be called by ContinuousScan when
		// it is setting up. The collectionTime is used to normalize the
		// so it is set even though it is not used during the scan.
		setCollectionTime(requestedLiveTime);

		// Create the ArrayList for the data frames. Some dummy dummy data is
		// added to position 0 so that we can put frame 1 in position 1 and
		// so on.
		pointCounter = 0;
		continuousScanInProgress = true;
		continuousScanData = new ArrayList<double[]>();
		continuousScanData.add(new double[1]);
	}

	@Override
	public void clearFrameSets() throws DeviceException {
		if (!slave) {
			timer.clearFrameSets();
		}
		frameSets.clear();
	}

	public double[] readChans() {
		return data;
	}

	@Override
	public double[] readChannel(int startFrame, int frameCount, int channel) throws DeviceException {
		return null;
	}

	@Override
	public double[] readFrame(int startChannel, int channelCount, int frame) throws DeviceException {
		double[] frameData = continuousScanData.get(frame);
		double[] values = frameData;

		// If time channels is required - as in TfgScaler for instance, then stick time value into channel[0] and push
		// the others along one.
		if (timeChannelRequired) {
			values = new double[frameData.length];
			int whichFrameSet = 0;
			int frameCopy = frame;
			for (FrameSet fs : frameSets) {
				if (frameCopy <= fs.getFrameCount()) {
					break;
				}
				whichFrameSet++;
				frameCopy -= fs.getFrameCount();
			}
			System.out.println("££££££££ " + frame + " " + whichFrameSet + " " + frameSets.size() + " "
					+ frameSets.get(whichFrameSet));
			values[0] = round(frameSets.get(whichFrameSet).getRequestedLiveTime());
			// Data is always rescaled so that a collectionTime of 1000.0 gives default answers. This helps in detecting
			// problems in scans.
			for (int i = 1; i < frameData.length; i++) {
				values[i] = round(frameData[i - 1] * values[0] / 1000.0);
			}
		}

		return values;

	}

	@Override
	public void run() {
		try {
			while (runner != null) {
				synchronized (this) {
					waiting = true;
					do {
						logger.debug("dummy counter timer " + getName() + " main wait");
						wait();
						logger.debug("dummy counter timer " + getName() + " main wake up");
					} while (!simulatedCountRequired);
				}
				logger.debug("dummy counter timer " + getName() + " sleep wait");

				Thread.sleep(sleepTime);
				data = calculateData();
				logger.debug("dummy counter timer " + getName() + " sleep wake up");
				state = Detector.IDLE;
			}
		} catch (InterruptedException ex) {
			logger.error("Thread interrupted while waiting", ex);
		} catch (Exception ex) {
			logger.error("Error while running CounterTimer", ex);
		}
	}

	private double[] calculateData() {
		double[] data = new double[totalChans];
		if (useGaussian) {
			// The output for each channel is shifted by the channel number, and a random noise level applied.
			double baseValue = gaussian.yAtX(pointCounter);
			for (int i = 0; i < totalChans; i++) {
				data[i] = (baseValue + i) * (1.0 + noiseLevel * (2.0 * Math.random() - 1.0));

				data[i] = round(data[i]);
			}
		} else {
			for (int i = 0; i < totalChans; i++) {
				data[i] = (int) (Math.random() * 10.0) * (pointCounter + 1);
				data[i] = round(data[i]);
			}

		}
		return data;
	}

	private double round(double value) {
		return Math.rint(value * roundingFactor) / roundingFactor;
	}

	/**
	 * Overrides the DetectorBase method in order to set up the counters for the dummy data. This is the only place to
	 * do this but means that the dummy data mechanisms really only work in scans.
	 *
	 * @param collectionTime
	 *            the collectionTime in mS
	 * @throws DeviceException
	 */
	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		pointCounter = 0;
		incrementCounter = 1;
		gaussianPosition = 10.0;
		if (useGaussian) {
			gaussian = new Gaussian(gaussianPosition, gaussianWidth, gaussianHeight);
		}
		super.setCollectionTime(collectionTime);
	}

	/**
	 * @return Returns the noiseLevel.
	 */
	public double getNoiseLevel() {
		return noiseLevel;
	}

	/**
	 * @param noiseLevel
	 *            The noiseLevel to set.
	 */
	public void setNoiseLevel(double noiseLevel) {
		this.noiseLevel = noiseLevel;
	}

	/**
	 * @return Returns the gaussianWidth.
	 */
	public double getGaussianWidth() {
		return gaussianWidth;
	}

	/**
	 * @param gaussianWidth
	 *            The gaussianWidth to set.
	 */
	public void setGaussianWidth(double gaussianWidth) {
		this.gaussianWidth = gaussianWidth;
		gaussianPosition = 2.0 * gaussianWidth;
	}

	/**
	 * @return Returns the gaussianHeight.
	 */
	public double getGaussianHeight() {
		return gaussianHeight;
	}

	/**
	 * @param gaussianHeight
	 *            The gaussianHeight to set.
	 */
	public void setGaussianHeight(double gaussianHeight) {
		this.gaussianHeight = gaussianHeight;
	}

	/**
	 * @return Returns the useGaussian.
	 */
	public boolean isUseGaussian() {
		return useGaussian;
	}

	/**
	 * @param useGaussian
	 *            The useGaussian to set.
	 */
	public void setUseGaussian(boolean useGaussian) {
		this.useGaussian = useGaussian;
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (continuousScanInProgress && theObserved instanceof Timer) {
			TimerStatus ts = (TimerStatus) changeCode;
			if (ts.getCurrentStatus().contains("IDLE")) {
				continuousScanInProgress = false;
			} else if (ts.getCurrentFrame() != frameBeingDone) {
				// Perversely the TimerStatus getCurrentFrame() counts the
				// number of
				// live frames starting from 1 (cf TFG getCurrentFrame() which
				// counts the number of dead and live frames starting from 0. If
				// ts.getCurrentFrame() returns 1, frame 1 is currently being
				// counted.
				frameBeingDone = ts.getCurrentFrame();
				// Initialize gaussian if necessary (as in setCollectionTime)
				if (useGaussian && gaussian == null) {
					incrementCounter = 1;
					gaussianPosition = 10.0;

					gaussian = new Gaussian(gaussianPosition, gaussianWidth, gaussianHeight);
				}
				pointCounter = frameBeingDone;
				if (useGaussian) {
					resetGaussian();
				}

				// The data is placed one step behind where you might expect because of the way readFrame works in real
				// counter timers. To read the first frame 0 is passed in because you read 1 frame starting from 0.
				logger.debug("!!!!!!!!!!!!DummyCounterTimer " + getName() + " adding data for pointCounter "
						+ pointCounter + " to position " + (pointCounter - 1));
				continuousScanData.add(pointCounter - 1, calculateData());
			}
		}

	}

	/**
	 * @return number of decimal places
	 */
	public int getDataDecimalPlaces() {
		return dataDecimalPlaces;
	}

	/**
	 * @param dataDecimalPlaces
	 */
	public void setDataDecimalPlaces(int dataDecimalPlaces) {
		this.dataDecimalPlaces = dataDecimalPlaces;
		roundingFactor = Math.pow(10.0, dataDecimalPlaces);
	}

	/**
	 * @return readout filename
	 */
	public boolean isReadoutFileName() {
		return readoutFileName;
	}

	/**
	 * @param readoutFileName
	 */
	public void setReadoutFileName(boolean readoutFileName) {
		this.readoutFileName = readoutFileName;
	}

	public void prepareForCollection(Scan scan) {
		logger.debug("prepareForCollection(Scan) called with " + scan);
	}

	public void clearAndStart() {
		logger.debug("DummyCounterTimer " + getName() + " clear called");
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// readout() doesn't return a filename.
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Dummy Counter Timer";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "dumdum-2";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "DUMMY";
	}

	public int getTotalChans() {
		return totalChans;
	}

	public void setTotalChans(int totalChans) {
		this.totalChans = totalChans;
	}

}
