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

package gda.device.detector;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.factory.FactoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A counter/timer class to drive a PC-based National Instruments PCI6602 counter timer card. It uses the Java native
 * interface to provide access to the nidaq32.dll driver file supplied by NI. This file and the java wrapper file,
 * jNI6602 need to be in the Windows system directory.
 */
public class NI6602 extends gda.device.detector.DetectorBase implements Detector {

	private static final Logger logger = LoggerFactory.getLogger(NI6602.class);

	private static final String NI6602LibraryName = "jNI6602_GDA_v6.12.0";

	// Local variables
	private int deviceNumber = 0;

	private int timingChannel = 1;

	private int totalChans;

	// private boolean activate = false;
	private int[] counterNo;

	private int[] counterGate;

	private int statusFlag = 0;

	private double[] data;

	// private final String className = getClass().getName();
	private final int EXTERNAL_TIMING_CHANNEL = -1;

	private final int MAX_CHANNELS = 8;

	// Pre-defined constants as used by NI
	private final int ND_COUNTER_0 = 13300;

	private final int ND_COUNTER_1 = 13400;

	private final int ND_COUNTER_2 = 13310;

	private final int ND_COUNTER_3 = 13320;

	private final int ND_COUNTER_4 = 13330;

	private final int ND_COUNTER_5 = 13340;

	private final int ND_COUNTER_6 = 13350;

	private final int ND_COUNTER_7 = 13360;

	private final int ND_PFI_38 = 50560;

	private final int ND_PFI_34 = 50520;

	private final int ND_PFI_30 = 50480;

	private final int ND_PFI_26 = 50440;

	private final int ND_PFI_22 = 50400;

	private final int ND_PFI_18 = 50360;

	private final int ND_PFI_14 = 50320;

	private final int ND_PFI_10 = 50280;

	private final int ND_PROGRAM = 29300;

	private final int ND_ARMED = 11200;

	private final int ND_SIMPLE_EVENT_CNT = 33100;

	private final int ND_GATE = 17100;

	private final int ND_GPCTR0_OUTPUT = 17400;

	private final int ND_LOW_TO_HIGH = 24100;

	private final int ND_RESET = 31200;

	private final int ND_GATE_POLARITY = 17200;

	private final int ND_POSITIVE = 29100;

	private final int ND_SINGLE_PULSE_GNR = 33300;

	private final int ND_SOURCE = 33700;

	private final int ND_INTERNAL_100_KHZ = 19200;

	private final int ND_COUNT = 13200;

	private final int ND_COUNT_1 = 13500;

	private final int ND_COUNT_2 = 13600;

	private final int ARMED = 39100;

	// private final int UNARMED = 26200;

	// Declarations of mapping methods in JNI based jNI6602.dll file
	private native int GpctrWatch(int device, int counterNo, int parameter, int[] value);

	private native int GpctrControl(int device, int counterNo, int parameter);

	private native int GpctrSetApplication(int device, int counterNo, int parameter);

	private native int GpctrChangeParameter(int device, int counterNo, int parameter, int counterGate);

	private native int GpctrSelectSignal(int device, int parameter1, int parameter2, int parameter3);

	// private native int GpctrTest();

	// Load the 'jNI6602.dll" library which interfaces this Java class
	// to the vendor-supplied Windows dll file "nidaq32.dll"
	static {
		try {
			System.loadLibrary(NI6602LibraryName);
		} catch (Throwable e) {
			logger.error("Exception in NI6602", e);
		}
	}

	/**
	 * Constructor.
	 */
	public NI6602() {
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
		statusFlag = IDLE;
		counterNo = new int[MAX_CHANNELS];
		counterNo[0] = ND_COUNTER_0;
		counterNo[1] = ND_COUNTER_1;
		counterNo[2] = ND_COUNTER_2;
		counterNo[3] = ND_COUNTER_3;
		counterNo[4] = ND_COUNTER_4;
		counterNo[5] = ND_COUNTER_5;
		counterNo[6] = ND_COUNTER_6;
		counterNo[7] = ND_COUNTER_7;

		counterGate = new int[MAX_CHANNELS];
		counterGate[0] = ND_PFI_38;
		counterGate[1] = ND_PFI_34;
		counterGate[2] = ND_PFI_30;
		counterGate[3] = ND_PFI_26;
		counterGate[4] = ND_PFI_22;
		counterGate[5] = ND_PFI_18;
		counterGate[6] = ND_PFI_14;
		counterGate[7] = ND_PFI_10;

	}

	/**
	 * @return Returns the deviceNumber.
	 */
	public int getDeviceNumber() {
		return deviceNumber;
	}

	/**
	 * @param deviceNumber
	 *            The deviceNumber to set.
	 */
	public void setDeviceNumber(int deviceNumber) {
		this.deviceNumber = deviceNumber;
	}

	/**
	 * @param totalChans
	 */
	public void setTotalChans(int totalChans) {
		this.totalChans = totalChans;
		data = new double[totalChans];
		String[] newExtraNames = new String[totalChans];
		for (int i = 0; i < totalChans; i++) {
			newExtraNames[i] = "element_"+ i;
		}
		setExtraNames(newExtraNames);
	}

	public int getTotalChans() {
		return totalChans;
	}

	/**
	 * @return Returns the timingChannel.
	 */
	public int getTimingChannel() {
		return timingChannel;
	}

	/**
	 * @param timingChannel
	 *            The timingChannel to set.
	 */
	public void setTimingChannel(int timingChannel) {
		this.timingChannel = timingChannel;
	}

	/**
	 * Sets up a single specified timing period and allows the counter-timer to proceed asynchronously. The end of
	 * period can be determined by calls to getStatus() returning 0 if idle, 1 if counting or 2 if paused.
	 *
	 * @param time
	 *            the requested counting time in milliseconds
	 * @throws DeviceException
	 */
	public void countAsync(double time) throws DeviceException {
		logger.debug("NI6602 " + getName() + " countAsync called with time " + time);
		// Initialises timing and counter channels
		// Counter channels first
		for (int i = 0; i < totalChans; i++) {
			if (i != timingChannel) {
				try {
					// Reset channel
					if (GpctrControl(deviceNumber, counterNo[i], ND_RESET) != 0) {
						throw new DeviceException("Error resetting counter channel " + Integer.toString(i));
					}
					// Set event counting
					if (GpctrSetApplication(deviceNumber, counterNo[i], ND_SIMPLE_EVENT_CNT) != 0) {
						throw new DeviceException("Error setting event counting on channel " + Integer.toString(i));
					}
					// Gating on gate pin,
					if (GpctrChangeParameter(deviceNumber, counterNo[i], ND_GATE, counterGate[i]) != 0) {
						throw new DeviceException("Error setting gate input for counter channel " + Integer.toString(i));
					}
					// +ve gate polarity
					if (GpctrChangeParameter(deviceNumber, counterNo[i], ND_GATE_POLARITY, ND_POSITIVE) != 0) {
						throw new DeviceException("Error setting gate polarity for counter channel "
								+ Integer.toString(i));
					}
					// arm waiting for gate to go positive
					if (GpctrControl(deviceNumber, counterNo[i], ND_PROGRAM) != 0) {
						throw new DeviceException("Error arming counter channel " + Integer.toString(i));
					}
				} catch (Throwable e) {
					logger.error("Error initialising counter channels in {}.countAsync({})", getName(), time, e);
					throw new DeviceException(getName() + " countAsync error", e);
				}
			}
		}

		// Then the timing channel if used
		if (timingChannel != EXTERNAL_TIMING_CHANNEL) {
			try {
				// Reset channel
				if (GpctrControl(deviceNumber, counterNo[timingChannel], ND_RESET) != 0) {
					throw new DeviceException("Error resetting counter channel " + Integer.toString(timingChannel));
				}
				// Set pulse generation
				if (GpctrSetApplication(deviceNumber, counterNo[timingChannel], ND_SINGLE_PULSE_GNR) != 0) {
					throw new DeviceException("Error setting single pulse operation counter channel "
							+ Integer.toString(timingChannel));
				}
				// Set clock to 100kHz
				if (GpctrChangeParameter(deviceNumber, counterNo[timingChannel], ND_SOURCE, ND_INTERNAL_100_KHZ) != 0) {
					throw new DeviceException("Error setting clock to 100kHz for operation counter channel "
							+ Integer.toString(timingChannel));
				}
				// Set zero delay
				if (GpctrChangeParameter(deviceNumber, counterNo[timingChannel], ND_COUNT_1, 5) != 0) {
					throw new DeviceException("Error setting initial delay for counter channel "
							+ Integer.toString(timingChannel));
				}
				// Set counting period using the 100KHz clock rounding to the
				// nearest integer to account for floating point inaccuracy
				int val = (int) (time + 0.5) * 100;
				if (GpctrChangeParameter(deviceNumber, counterNo[timingChannel], ND_COUNT_2, val) != 0) {
					throw new DeviceException("Error setting counting period of " + val + " for counter channel "
							+ Integer.toString(timingChannel));
				}
				// Set low-to-high priority
				if (GpctrSelectSignal(deviceNumber, ND_GPCTR0_OUTPUT, ND_GPCTR0_OUTPUT, ND_LOW_TO_HIGH) != 0) {
					throw new DeviceException("Error setting low-to-high priority for counter channel "
							+ Integer.toString(timingChannel));
				}
			} catch (Throwable e) {
				logger.error("Error initialising timer channel in {}.countAsync({})", getName(), time, e);
				throw new DeviceException(getName() + " countAsync error", e);
			}
		}
		start();
	}

	/**
	 * Returns the current counting state of the counter-timer All counter-timers must fully implement this.
	 *
	 * @return BUSY if the counter-timer has not finished the requested operation(s), IDLE if in an completely idle
	 *         state and PAUSED if temporarily suspended.
	 * @throws DeviceException
	 */
	@Override
	public int getStatus() throws DeviceException {
		int[] sts = new int[1];

		// See if timing channel still armed
		statusFlag = IDLE;
		if (timingChannel != EXTERNAL_TIMING_CHANNEL) {
			try {
				GpctrWatch(deviceNumber, counterNo[timingChannel], ND_ARMED, sts);
				if (sts[0] == ARMED) {
					statusFlag = BUSY;
				}
			} catch (Throwable e) {
				logger.error("{} Error getting status", getName(), e);
				throw new DeviceException("Error getting status", e);
			}
		}
		return statusFlag;
	}


	/**
	 * Starts the counter counting for the period set up by the countAsync method
	 *
	 * @throws DeviceException
	 */
	public void start() throws DeviceException {
		// Start timing channel going
		statusFlag = BUSY;
		try {
			if (timingChannel != EXTERNAL_TIMING_CHANNEL
					&& GpctrControl(deviceNumber, counterNo[timingChannel], ND_PROGRAM) != 0) {
				statusFlag = IDLE;
				throw new DeviceException("Error starting measurement ");
			}
		} catch (Throwable e) {
			logger.error("Error starting {}", getName(), e);
			throw new DeviceException("Error at start", e);
		}
	}

	/**
	 * Aborts any current counter-timing operations and returns it to an idle state. This should not register errors if
	 * there are no current operations in progress. All counter-timers must fully implement this.
	 *
	 * @throws DeviceException
	 */
	@Override
	public void stop() throws DeviceException {
		// Stop timing channel
		statusFlag = IDLE;
		try {
			if (timingChannel != EXTERNAL_TIMING_CHANNEL && statusFlag != IDLE
					&& GpctrControl(deviceNumber, counterNo[timingChannel], ND_RESET) != 0) {
				throw new DeviceException("Error stopping measurement ");
			}
		} catch (Throwable e) {
			logger.error("{}: Error stopping", getName(), e);
			throw new DeviceException("Error stopping " + getName(), e);
		}
	}

	/**
	 * Returns an array of size give by getTotalChans method containing the latest measured counts.
	 *
	 * @return array of all channel readout values
	 * @throws DeviceException
	 */
	public double[] readChans() throws DeviceException {
		// Read counter values and return them in data array
		int[] val = new int[1];
		for (int i = 0; i < totalChans; i++) {
			try {
				int j = GpctrWatch(deviceNumber, counterNo[i], ND_COUNT, val);

				if (j != 0) {
					throw new DeviceException("Error reading counter channel " + Integer.toString(i));
				}
				data[i] = val[0];
			} catch (Throwable e) {
				logger.error("{}: Error reading channels", getName(), e);
				throw new DeviceException("Error reading channels for " + getName(), e);
			}
		}
		logger.debug("NI6602 " + getName() + " readChans returning " + dataToString());
		return data;
	}

	/**
	 * Returns the data array converted to a String (mostly for debugging purposes
	 *
	 * @return a string version of the data array
	 */
	private String dataToString() {
		String string = "";
		for (int i = 0; i < data.length; i++)
			string = string + data[i] + " ";

		return string;

	}

//	public double[] readChannel(int startFrame, int frameCount, int channel) throws DeviceException {
//		return null;
//	}

	@Override
	public void collectData() throws DeviceException {
		countAsync(collectionTime);
	}

	@Override
	public double[] readout() throws DeviceException {
		return readChans();
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// readout() doesn't return a filename.
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "NI6602";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "unknown";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "NI6602";
	}

}
