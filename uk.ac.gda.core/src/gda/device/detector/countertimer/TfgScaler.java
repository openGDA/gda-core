/*-
 * Copyright © 2012 Diamond Light Source Ltd., Science and Technology
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

import gda.device.CounterTimer;
import gda.device.DeviceException;
import gda.device.Memory;
import gda.device.timer.FrameSet;
import gda.factory.FactoryException;
import gda.factory.Finder;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A distributed counter/timer class implemented using DA.Server at present on OS-9 systems using VME Time Frame
 * Generator and multichannel scaler.
 */
public class TfgScaler extends TFGCounterTimer implements CounterTimer {

	private class ScalerFrame {
		double time;
		double[] data;
	}

	private static final Logger logger = LoggerFactory.getLogger(TfgScaler.class);

	protected Memory scaler = null;

	/*
	 * The first channel of output should always be livetime. For TFGv2 this is always supplied from DAServer, but for
	 * older v1 systems this must be explicitly added to the output. <p> So this is effectively a TFG version flag: set
	 * to true if TFGv1 and need to calculate and add the livetime.
	 */
	protected boolean timeChannelRequired;

	// if there are more channels in the TFG which this class is to read out. If this property is null then all will be
	// read, i.e. the full width of the Memory object
	protected Integer numChannelsToRead = null;

	// to optionally not start reading from the first channel. If null then data will be read from which ever is
	// the first channel of actual data.
	private Integer firstDataChannel = null;

	// TfgScaler must keep its own copy of the FrameSets used so that
	// the correct counting time can be put into the values array returned
	// by readFrame(int, int, int).
	private ArrayList<FrameSet> frameSets;

	private String scalerName;
	private int minimumReadoutDelay = 0;
	private int lastFrameCollected = 0;
	private long countAsyncTime = 0;
	private long readoutTime = 0;

	public TfgScaler() {
		super();
	}

	@Override
	public void configure() throws FactoryException {
		if (scaler == null) {
			logger.debug("Finding: " + scalerName);
			if ((scaler = (Memory) Finder.getInstance().find(scalerName)) == null) {
				logger.error("Scaler " + scalerName + " not found");
			}
		}

		frameSets = new ArrayList<FrameSet>();
		super.configure();
		setSlave(false);
	}

	/**
	 * Set the name of the scaler for the Finder
	 * 
	 * @param scalerName
	 */
	public void setScalerName(String scalerName) {
		this.scalerName = scalerName;
	}

	/**
	 * Used by Castor for instantiation.
	 * 
	 * @return the scaler name
	 */
	public String getScalerName() {
		return scalerName;
	}

	public Memory getScaler() {
		return scaler;
	}

	public void setScaler(Memory scaler) {
		this.scaler = scaler;
	}

	public boolean isTimeChannelRequired() {
		return timeChannelRequired;
	}

	public void setTimeChannelRequired(boolean timeChannelRequired) {
		this.timeChannelRequired = timeChannelRequired;
	}

	/**
	 * Returns the total number of available counter-timer readout channels that will be returned by calls to
	 * readChans() For a time-framing device it is the number of channels per frame. All counter-timers must fully
	 * implement this.
	 * 
	 * @return total number of readout channels
	 * @throws DeviceException
	 */
	public int getTotalChans() throws DeviceException {
		int cols = scaler.getDimension()[0];
		if (numChannelsToRead != null) {
			cols = numChannelsToRead;
		}
		if (timeChannelRequired) {
			cols++;
		}
		return cols;
	}

	/**
	 * Initiates a single specified timing period and allows the counter-timer to proceed asynchronously. The end of
	 * period can be determined by calls to isCounting() returning false. All counter-timers must fully implement this.
	 * 
	 * @param time
	 *            the requested counting time in milliseconds
	 * @throws DeviceException
	 */
	public void countAsync(double time) throws DeviceException {
		// if using time frames then simply call "tfg cont", else clear
		if (frameSets.size() > 0) {
			timer.restart();
		} else {
			scaler.clear();
			scaler.start();
			timer.countAsync(time);
		}
		countAsyncTime = System.currentTimeMillis();
	}

	@Override
	public void start() throws DeviceException {
		scaler.clear();
		scaler.start();
		super.start();
	}

	@Override
	public void stop() throws DeviceException {
		super.stop();
		scaler.stop();
	}

	@Override
	public void addFrameSet(int frameCount, double requestedDeadTime, double requestedLiveTime) throws DeviceException {
		super.addFrameSet(frameCount, requestedDeadTime, requestedLiveTime);
		frameSets.add(new FrameSet(frameCount, requestedDeadTime, requestedLiveTime));
	}

	@Override
	public void addFrameSet(int frameCount, double requestedDeadTime, double requestedLiveTime, int deadPort,
			int livePort, int deadPause, int livePause) throws DeviceException {
		super.addFrameSet(frameCount, requestedLiveTime, requestedDeadTime, deadPort, livePort, deadPause, livePause);
		frameSets.add(new FrameSet(frameCount, requestedDeadTime, requestedLiveTime));
	}

	@Override
	public void clearFrameSets() throws DeviceException {
		super.clearFrameSets();
		frameSets.clear();
		lastFrameCollected = -1;
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		// if this class was used to define framesets, then memeory is only cleared at the start of the scan
		if (frameSets.size() > 0) {
			scaler.clear();
			scaler.start();
			loadFrameSets(); // ?? this should be needed I think
			timer.start();
		}
		lastFrameCollected = -1;
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		atScanEnd();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		// if this class was used to define framesets, then ensure they are cleared at the end of the scan
		if (frameSets.size() > 0 || !timer.getAttribute("TotalFrames").equals(0)) {
			stop(); // stops the TFG - useful if scan aborted and so TFG still in a PAUSE state rather than an IDLE
					// state
			clearFrameSets();
		}
	}

	/**
	 * Obtain an array of available readout channels. This should be available at any time. If the hardware does not
	 * allow it during active counter-timing periods, it should return zero values. High level counter-timers may return
	 * values in user units. All counter-timers must fully implement this.
	 * 
	 * @return array of all channel readout values
	 * @throws DeviceException
	 */
	public double[] readChans() throws DeviceException {
		// returns rawdata only. See readFrame for proper inclusion of time etc.
		return scaler.read(0, 0, 0, scaler.getDimension()[0], 1, 1);
	}

	@Override
	public double[] readChannel(int startFrame, int frameCount, int channel) throws DeviceException {
		// returns rawdata only. See readFrame for proper inclusion of time etc.
		return scaler.read(channel, 0, startFrame, 1, 1, frameCount);
	}

	@Override
	public double[] readFrame(int startChannel, int channelCount, int frame) throws DeviceException {
		// returns rawdata only. See readFrame for proper inclusion of time etc.
		double[] values = scaler.read(startChannel, 0, frame, channelCount, 1, 1);
		return values;
	}

	/**
	 * For a time framing counter-timer, read out from channel 0 beginning from the specified start frame number using
	 * all the scaler dimensions.
	 * 
	 * @param frame
	 *            read this frame
	 * @return array of requested readout counter-timer data
	 * @throws DeviceException
	 */
	public double[] readFrame(int frame) throws DeviceException {
		return readoutFrames(frame, frame)[0];
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		timer.setAttribute(attributeName, value);
		scaler.setAttribute(attributeName, value);
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {

		if (attributeName.equals("collectionTime")) {
			return collectionTime;
		}

		Object obj;
		if ((obj = timer.getAttribute(attributeName)) == null) {
			obj = scaler.getAttribute(attributeName);
		}
		if (obj == null) {
			obj = super.getAttribute(attributeName);
		}
		return obj;
	}

	/**
	 * @throws DeviceException
	 */
	@Override
	public void collectData() throws DeviceException {
		countAsync(collectionTime * 1000); // convert from seconds (Detector interface) to milliseconds (CounterTimer
											// interface)
		// increment the frame counter if framing is being used
		if (frameSets.size() > 0) {
			lastFrameCollected++;
		} else {
			lastFrameCollected = 0;
		}
	}

	/**
	 * @see gda.device.Detector#readout()
	 */
	@Override
	public double[] readout() throws DeviceException {
		double[] output = readoutCurrentFrame();
		return output;
	}

	/**
	 * Returns the current data but does not increment the frame counter if frames are being used.
	 * 
	 * @return double[]
	 * @throws DeviceException
	 */
	public double[] readoutCurrentFrame() throws DeviceException {
		if (minimumReadoutDelay != 0) {
			readoutTime = System.currentTimeMillis();
			long delay = readoutTime - countAsyncTime + minimumReadoutDelay;
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				throw new DeviceException("InterruptedException during minimumReadoutDelay", e);
			}
		}
		// logger.debug("Current tfg frame is " + this.getCurrentFrame() + " and reading frame " + framesRead);
		// return readoutFrames(framesRead, framesRead)[0];
		logger.debug("Reading frame " + lastFrameCollected + ". Current tfg frame is " + this.getCurrentFrame());
		return readoutFrames(lastFrameCollected, lastFrameCollected)[0];
	}

	public double[][] readoutFrames(int startFrame, int finalFrame) throws DeviceException {

		// read the lot
		int numFrames = finalFrame - startFrame + 1;
		int width = scaler.getDimension()[0];
		double rawData[] = scaler.read(0, 0, startFrame, width, 1, numFrames);
		double[][] rawDataAsFrames = unpackRawDataToFrames(rawData, numFrames, width);

		// if no change required to the raw data
		if ((firstDataChannel == null || firstDataChannel == 0) && !timeChannelRequired && !isTFGv2
				&& (numChannelsToRead == null || numChannelsToRead == width)) {
			return rawDataAsFrames;
		}

		// convert to frame objects
		ScalerFrame[] frames = convertToFrames(rawDataAsFrames);

		// remove unwanted channels based on firstDataChannel and numChannelsToRead
		frames = reduceFrames(frames);

		// output basic 2D array
		double[][] output = new double[numFrames][];
		for (int i = 0; i < numFrames; i++) {
			if (timeChannelRequired) {
				output[i] = ArrayUtils.add(frames[i].data, 0, frames[i].time);
			} else {
				output[i] = frames[i].data;
			}
		}
		return output;
	}

	/*
	 * remove unwanted channels based on firstDataChannel and numChannelsToRead
	 */
	private ScalerFrame[] reduceFrames(ScalerFrame[] frames) {

		int firstChannel = 0;
		if (firstDataChannel != null) {
			firstChannel = firstDataChannel;
		}

		ScalerFrame[] reducedFrames = new ScalerFrame[frames.length];
		for (int i = 0; i < frames.length; i++) {
			reducedFrames[i] = new ScalerFrame();
			reducedFrames[i].time = frames[i].time;
			if (numChannelsToRead == null) {
				reducedFrames[i].data = ArrayUtils.subarray(frames[i].data, firstChannel, frames[i].data.length);
			} else {
				reducedFrames[i].data = ArrayUtils.subarray(frames[i].data, firstChannel, firstChannel
						+ numChannelsToRead);
			}
		}
		return reducedFrames;
	}

	/*
	 * Convert the raw double data to internal structure
	 */
	private ScalerFrame[] convertToFrames(double[][] output) {
		ScalerFrame[] frames = new ScalerFrame[output.length];
		for (int i = 0; i < output.length; i++) {
			frames[i] = new ScalerFrame();

			if (isTFGv2) {
				// TFG2 always returns first channel as #clock counts (TFG has a 100MHz clock cycle)
				frames[i].time = output[i][0] / 100000000;
			} else {
				if (frameSets == null || frameSets.size() == 0) {
					frames[i].time = collectionTime;
				} else {
					frames[i].time = frameSets.get(i).requestedLiveTime / 1000.0;
				}
			}

			if (isTFGv2) {
				frames[i].data = ArrayUtils.subarray(output[i], 1, output[i].length);
			} else {
				frames[i].data = output[i];
			}
		}
		return frames;
	}

	private double[][] unpackRawDataToFrames(double[] scalerData, int numFrames, int channelCount) {

		double[][] unpacked = new double[numFrames][channelCount];
		int iterator = 0;

		for (int frame = 0; frame < numFrames; frame++) {
			for (int datum = 0; datum < channelCount; datum++) {
				unpacked[frame][datum] = scalerData[iterator];
				iterator++;
			}
		}
		return unpacked;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// readout() doesn't return a filename.
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Tfg Scaler";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "unknown";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Scaler";
	}

	/**
	 * Override ScannableBase to work within scans
	 */
	@Override
	public String[] getInputNames() {
		return new String[] { "Time" };
	}

	public void setNumChannelsToRead(int numChannelsToRead) {
		this.numChannelsToRead = numChannelsToRead;
	}

	public int getNumChannelsToRead() {
		return numChannelsToRead;
	}

	public int getMinimumReadoutDelay() {
		return minimumReadoutDelay;
	}

	public void setMinimumReadoutDelay(int minimumReadoutDelay) {
		this.minimumReadoutDelay = minimumReadoutDelay;
	}

	public Integer getFirstDataChannel() {
		return firstDataChannel;
	}

	public void setFirstDataChannel(Integer firstDataChannel) {
		this.firstDataChannel = firstDataChannel;
	}
}
