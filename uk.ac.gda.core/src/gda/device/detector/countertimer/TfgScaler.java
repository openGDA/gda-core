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

import gda.device.CounterTimer;
import gda.device.DeviceException;
import gda.device.Memory;
import gda.device.timer.FrameSet;
import gda.factory.FactoryException;
import gda.factory.Finder;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A distributed counter/timer class implemented using DA.Server at present on OS-9 systems using VME Time Frame
 * Generator and multichannel scaler.
 */
public class TfgScaler extends TFGCounterTimer implements CounterTimer {

	private static final Logger logger = LoggerFactory.getLogger(TfgScaler.class);

	protected Memory scaler = null;

	private String scalerName;

	/**
	 * The first channel of output should always be livetime. For TFGv2 this is always supplied from DAServer, but for
	 * older v1 systems this must be explicitly added to the output.
	 * <p>
	 * So this is effectively a TFG version flag: set to true if TFGv1 and need to calculate and add the livetime.
	 */
	protected boolean timeChannelRequired;
	
	private int framesRead = 0;
	
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
	
	private int minimumReadoutDelay = 0;
	private long countAsyncTime = 0;
	private long readoutTime = 0;

	/**
	 * 
	 */
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

	/**
	 * @return Returns the scaler.
	 */
	public Memory getScaler() {
		return scaler;
	}

	/**
	 * @param scaler
	 *            The scaler to set.
	 */
	public void setScaler(Memory scaler) {
		this.scaler = scaler;
	}

	/**
	 * @return Returns the timeChannelRequired.
	 */
	public boolean isTimeChannelRequired() {
		return timeChannelRequired;
	}

	/**
	 * @param timeChannelRequired
	 *            The timeChannelRequired to set.
	 */
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
		if (numChannelsToRead != null){
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
		if (frameSets.size() > 0){
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

	/**
	 * Aborts any current counter-timing operations and returns it to an idle state. This should not register errors if
	 * there are no current operations in progress. All counter-timers must fully implement this.
	 * 
	 * @throws DeviceException
	 */
	@Override
	public void stop() throws DeviceException {
		super.stop();
		scaler.stop();
	}

	@Override
	public void addFrameSet(int frameCount, double requestedDeadTime,  double requestedLiveTime) throws DeviceException {
		super.addFrameSet(frameCount, requestedDeadTime, requestedLiveTime);
		frameSets.add(new FrameSet(frameCount, requestedDeadTime, requestedLiveTime));
	}

	@Override
	public void addFrameSet(int frameCount, double requestedDeadTime, double requestedLiveTime,  int deadPort,
			int livePort, int deadPause, int livePause) throws DeviceException {
		super.addFrameSet(frameCount, requestedLiveTime,requestedDeadTime,  deadPort, livePort, deadPause, livePause);
		frameSets.add(new FrameSet(frameCount, requestedDeadTime, requestedLiveTime));
	}

	@Override
	public void clearFrameSets() throws DeviceException {
		super.clearFrameSets();
		frameSets.clear();
		framesRead = 0;
	}
	
	@Override
	public void atScanLineStart() throws DeviceException{
		// if this class was used to define framesets, then memeory is only cleared at the start of the scan
		if (frameSets.size() > 0){
			scaler.clear();
			scaler.start();
			loadFrameSets(); //?? this should be needed I think
			timer.start();
		}
		framesRead = 0;
	}
	
	@Override
	public void atCommandFailure() throws DeviceException
	{
		if(frameSets.size() > 0)
		{
			clearFrameSets();
		}
	}
	
	@Override
	public void atScanEnd() throws DeviceException{
		// if this class was used to define framesets, then ensure they are cleared at the end of the scan
		if (frameSets.size() > 0 || !timer.getAttribute("TotalFrames").equals(0)){
			stop();  // stops the TFG - useful if scan aborted and so TFG still in a PAUSE state rather than an IDLE state
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
		// NB do not change this scaler.getDimension().width to getTotalChans(),
		// getTotalChans() may add on 1 for the fake time channel.
		return scaler.read(0, 0, 0, scaler.getDimension()[0], 1, 1);
	}

	/**
	 * For a time framing counter-timer, read out a specified channel beginning from the specified start frame number
	 * using the requested frame count.
	 * 
	 * @return array of requested readout counter-timer data
	 * @param startFrame
	 *            starting frame number (1st=0)
	 * @param frameCount
	 *            number of frames to read the counter data out from
	 * @param channel
	 *            read this channel
	 * @throws DeviceException
	 */
	@Override
	public double[] readChannel(int startFrame, int frameCount, int channel) throws DeviceException {
		return scaler.read(channel, 0, startFrame, 1, 1, frameCount);
	}

	/**
	 * For a time framing counter-timer, read out a specified channel beginning from the specified start frame number
	 * using the requested frame count.
	 * 
	 * @return array of requested readout counter-timer data
	 * @param startChannel
	 *            starting channel number (1st=0)
	 * @param channelCount
	 *            number of channels to read the counter data out from
	 * @param frame
	 *            read this frame
	 * @throws DeviceException
	 */
	@Override
	public double[] readFrame(int startChannel, int channelCount, int frame) throws DeviceException {
		// For legacy XAFS scans the time channel is in column 1
		if (timeChannelRequired) {
			double[] scalerReadings = scaler.read(startChannel, 0, frame, channelCount - 1, 1, 1);
			double[] values = new double[scalerReadings.length + 1];
			int whichFrameSet = 0;
			int frameCopy = frame;
			for (FrameSet fs : frameSets) {
				if (frameCopy <= fs.getFrameCount()) {
					break;
				}
				whichFrameSet++;
				frameCopy -= fs.getFrameCount();
			}
			if (frameSets.size() > 0){
				values[0] = frameSets.get(whichFrameSet).getRequestedLiveTime();
			}
			else {
				values[0] = collectionTime;
			}
			for (int i = 1; i < values.length; i++) {
				values[i] = scalerReadings[i - 1];
			}
			return values;
		}

		double[] values = scaler.read(startChannel, 0, frame, channelCount, 1, 1);
		return values;

	}
	
	/**
	 * For a time framing counter-timer, read out from channel 0 beginning from the specified start frame number
	 * using all the scaler dimensions.
	 * @param frame
	 *            read this frame
	 * @return array of requested readout counter-timer data
	 * @throws DeviceException
	 */
	public double[] readFrame(int frame) throws DeviceException {
		// For legacy XAFS scans the time channel is in column 1
		int startChannel = 0;
		if (firstDataChannel != null) {
			startChannel = firstDataChannel;
		}
		int channelCount = scaler.getDimension()[0];
		if (numChannelsToRead != null){
			channelCount = numChannelsToRead;
		}
		double[]values =  readFrame(startChannel, channelCount, frame);
		if(isTFGv2() && !timeChannelRequired)
			values = ArrayUtils.subarray(values, 1, values.length);
		return values;

	}


	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		timer.setAttribute(attributeName, value);
		scaler.setAttribute(attributeName, value);
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		
		if (attributeName.equals("collectionTime")){
			return collectionTime;
		}
		
		Object obj;
		if ((obj = timer.getAttribute(attributeName)) == null){
			obj = scaler.getAttribute(attributeName);
		}
		if (obj == null){
			obj = super.getAttribute(attributeName);
		}
		return obj;
	}

	/**
	 * @throws DeviceException
	 */
	@Override
	public void collectData() throws DeviceException {
		countAsync(collectionTime * 1000); // convert from seconds (Detector interface) to milliseconds (CounterTimer interface)
	}

	/**
	 * @see gda.device.Detector#readout()
	 */
	@Override
	public double[] readout() throws DeviceException {
		
		double[] output = readoutCurrentFrame();

		//increment the frame counter if framing is being used
		if (frameSets.size() > 0){
			framesRead ++;
		}

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
				e.printStackTrace();
			}
		}
		logger.debug("Current tfg frame is " + this.getCurrentFrame() + " and reading frame " + framesRead);
		return readoutFrames(framesRead, framesRead)[0];
	}
	
	public double[][] readoutFrames(int startFrame, int finalFrame) throws DeviceException {

		int numFrames = finalFrame - startFrame + 1;
		int numberOfChannels = numChannelsToRead;
		if (numChannelsToRead == null){
			numberOfChannels = scaler.getDimension()[0];
		}
		int firstChannel = 0;
		if (firstDataChannel != null){
			firstChannel = firstDataChannel;
		}
		if (isTFGv2 && firstChannel == 0){
			numberOfChannels++;
		}
		double rawData[] = scaler.read(firstChannel, 0, startFrame, numberOfChannels, 1, 1);
//		double rawData2[] = scaler.read(0, 0, startFrame + 1, numberOfChannels + 1, 1, 1);
		double[][] output = unpackRawDataToFrames(rawData,numFrames,numberOfChannels);

		// if no change required to the raw data
		if (firstDataChannel != null || (!timeChannelRequired && !isTFGv2)) {
			return output;
		}
		
		/*if (firstDataChannel != null){
			output = removeUnwantedChannels(output);
		} else */if (isTFGv2 && !timeChannelRequired){
			// for TFGv2 always get the number of live time clock counts as the first scaler item
			output = removeUnwantedTimeChannel(output);
		} else if (isTFGv2 && timeChannelRequired){
			//convert the live time clock counts into seconds (TFG has a 100MHz clock cycle)
			for (int i = 0; i < output.length; i++) {
				output[i][0] = output[i][0] / 100000000;
			}
		} else if (!isTFGv2 && timeChannelRequired){
			double time = getCollectionTime();
			double[][] framesWithTimeAdded = new double[output.length][];
			for (int i = 0; i < output.length; i++) {
				framesWithTimeAdded[i] = ArrayUtils.addAll(new double[]{time}, output[i]);
			}
			output = framesWithTimeAdded;
		}
		return output;
	}

	private double[][] removeUnwantedChannels(double[][] rawFrames) {
		double[][] newoutput = new double[rawFrames.length][];
		for (int i = 0; i < rawFrames.length; i++) {
			double[] frame = rawFrames[i];
			double[] dataChannels = Arrays.copyOfRange(frame, firstDataChannel, frame.length);
			if (timeChannelRequired && isTFGv2) {
				double[] time = new double[]{frame[0]};
				dataChannels = ArrayUtils.addAll(time, dataChannels);
			}
			newoutput[i] = dataChannels;
		}
		rawFrames = newoutput;
		return rawFrames;
	}

	private double[][] removeUnwantedTimeChannel(double[][] rawFrames) {
		double[][] newoutput = new double[rawFrames.length][];
		for (int i = 0; i < rawFrames.length; i++) {
			double[] frame = rawFrames[i];
			double[] dataChannels = Arrays.copyOfRange(frame, 1, frame.length);
			newoutput[i] = dataChannels;
		}
		rawFrames = newoutput;
		return rawFrames;
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

	public void setNumChannelsToRead(Integer numChannelsToRead) {
		this.numChannelsToRead = numChannelsToRead;
	}

	public Integer getNumChannelsToRead() {
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
