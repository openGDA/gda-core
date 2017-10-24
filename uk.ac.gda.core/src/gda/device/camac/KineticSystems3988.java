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

package gda.device.camac;

import gda.device.Camac;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.Gpib;
import gda.factory.Finder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kinetic Systems 3988 Camac Controller device
 */
public class KineticSystems3988 extends DeviceBase implements Camac {

	private static final Logger logger = LoggerFactory.getLogger(KineticSystems3988.class);

	private Gpib gpib;

	private Integer camacUid = new Integer(-1);

	private int timeout = 10000;

	private String interfaceName = "";

	private String deviceName = "";

	private boolean deviceConnected = false;

	// Kinetic Systems 3988 Camac Controller internal function codes
	// private static final String nafRD1ReadXferCount =
	// String.copyValueOf(new char[] {0x1E, 0x00, 0x00});

	private static final String nafRD2ReadControlStatus = String.copyValueOf(new char[] { 0x1E, 0x00, 0x01 });

	// private static final String nafRD2ReadLAMRequest =
	// String.copyValueOf(new char[] {0x1E, 0x0C, 0x01});

	// private static final String nafWT1WriteXferCount =
	// String.copyValueOf(new char[] {0x1E, 0x00, 0x10});

	// private static final String nafWT1WriteSRQMask =
	// String.copyValueOf(new char[] {0x1E, 0x01, 0x10});

	private static final String nafWT2WriteControlStatus = String.copyValueOf(new char[] { 0x1E, 0x00, 0x11 });

	// private static final String nafWT2WriteLAMMask =
	// String.copyValueOf(new char[] {0x1E, 0x0D, 0x11});

	// biggest CAMAC function code involving a read. treat all others as a
	// write
	private static final int maxCamacReadFunction = 7;

	// minimum write function involving data transfer
	private static final int minCamacWriteXferFunction = 16;

	// maximum write function involving data transfer
	private static final int maxCamacWriteXferFunction = 23;

	/**
	 * Finds the gpib from the xml file makes sure the device is connected to the Gpib
	 */
	@Override
	public void configure() {
		// super.configure();

		logger.debug("Camac KS3988: configuring instance {}. Finding: Gpib Interface {}", getName(), interfaceName);

		if ((gpib = (Gpib) Finder.getInstance().find(interfaceName)) == null) {
			logger.debug("Camac KS3988: Gpib Board {} not found", interfaceName);
		} else {
			try {
				// Initialise KS3988 Controller via GPIB interface
				initialiseKS3988();
			} catch (DeviceException de) {
				logger.debug("Camac KS3988: Exception occured in configuring Camac instance {} Gpib Interface {}",
						getName(),
						interfaceName, de);
			}
		}
	}

	/**
	 * @return always 1
	 * @throws DeviceException
	 */
	private int initialiseKS3988() throws DeviceException {
		// gpib.sendDeviceClear(deviceName);
		// sendCommand("Y2X");
		// gpib.setTerminator(deviceName, READ_TERMINATOR);
		// gpib.setReadTermination(deviceName, true);

		logger.debug("Camac KS3988: Setting Gpib Timeout {}", deviceName);

		gpib.setTimeOut(deviceName, timeout);

		logger.debug("Camac KS3988: Finding Gpib Device {}", deviceName);

		// find the device to make sure it is actually connected to gpib
		camacUid = new Integer(gpib.findDevice(deviceName));

		logger.debug("Camac KS3988: Gpib Device {} found {}", deviceName, camacUid);

		// make sure interface is not in error state
		gpib.sendInterfaceClear(deviceName);

		deviceConnected = true;

		// device clear
		gpib.sendDeviceClear(deviceName);

		// init string for CAMAC - Z + clear
		final String csrInitialiseZClear = String.copyValueOf(new char[] { 0x00, 0x00, 0xC0 });

		// gpib/camac init
		gpib.write(deviceName, nafWT2WriteControlStatus + csrInitialiseZClear);

		return 1;
	}

	/**
	 * @param command
	 * @throws DeviceException
	 */
	/*
	 * private void sendCommand(String command) throws DeviceException { if (!deviceConnected) { throw new
	 * DeviceException("Camac KS3988: KS3988 not connected"); } gpib.write(deviceName, command); }
	 */
	/**
	 * @param command
	 * @return the reply string
	 * @throws DeviceException
	 */
	/*
	 * private String sendReplyCommand(String command) throws DeviceException { if (!deviceConnected) { throw new
	 * DeviceException("Camac KS3988: KS3988 not connected"); } String reply = ""; gpib.write(deviceName, command);
	 * reply = gpib.read(deviceName, 80); return reply; }
	 */
	/**
	 * converts camac stn (N), subaddress (A) and fn_code (F) to a binary string for gpib 3988. Has mods to set up
	 * initialisation data for ZINHOF function which is specific to GPIB 3988.
	 *
	 * @param station
	 *            camac station
	 * @param subAddress
	 *            camac subaddress
	 * @param functionCode
	 *            camac function code
	 * @param data
	 *            single element array to camac data
	 * @return output NAF string
	 */
	private String camacGetNaf(int station, int subAddress, int functionCode, int data[]) {
		String naf = "";

		// status register data for do Z and turn off inhibit
		final int initDat = 0x80;

		/*
		 * initialise code is a special case which needs internal GPIB naf for WT2,0 N=30, A=0, F=17 and set data to set
		 * status register (0x80)
		 */
		// TODO - MOVE THIS CODE OUTSIDE? - AS ITS SPECIAL CASE
		if (functionCode == ZINHOFF) {
			station = 0x1e;
			subAddress = 0x0;
			functionCode = 0x11;
			data[0] = initDat;
		}

		// set each byte from input data
		naf = String.valueOf((char) station) + String.valueOf((char) subAddress) + String.valueOf((char) functionCode);

		return naf;
	}

	/**
	 * converts a long 32 bit int to gpib 24bit camac data string for Kinetic Systems 3988 CAMAC controller.
	 *
	 * @param input
	 *            32bit integer
	 * @return 24bits camac data string
	 */
	private String camacIntToStr24(int input) {
		String output = "";

		// temporary value in conversion
		int temp;

		// ensures only lsb kept in conversion
		int byte1Only = 0xFFFFFF00;

		// loop counter
		int i;

		/*
		 * derive each string byte from 3 bytes of 32 bit number by shifting a byte right and ignoring bytes higher than
		 * 1. Bytes are in order MSB-LSB
		 */
		for (i = 0; i < 3; i++) {
			temp = input;
			temp = temp >> (2 - i) * 8;
			output += String.valueOf((char) (temp ^ byte1Only));
		}

		return output;
	}

	/**
	 * Converts a gpib 24bit camac data string to a long 32 bit int.
	 *
	 * @param input
	 *            24bits camac data string
	 * @return converted 32bit integer
	 */
	private int camacStr24ToInt(String input) {
		int output = 0;

		// temporary value in conversion
		int tmpVal;
		// negative number in 24 bits
		int bit24Set = 0x800000;
		// -ve top byte to add to -ve 24 bits
		int neg32 = 0xFF000000;
		// number for -ve char to +ve 32 bits
		int noNeg32 = 0xFFFFFF00;
		// loop counter
		int i;

		/*
		 * add each byte to total camac integer ensuring negative bytes are held as +ve bit pattern of long
		 */
		for (i = 0, output = 0; i < 3; i++) {
			tmpVal = input.charAt(i);

			if (tmpVal < 0) {
				tmpVal = tmpVal ^ noNeg32;
			}

			// TODO - SORT OUT WHAT TRYING TO DO HERE!!
			// COMPARE C OPERATOR PRECEDENCE WITH JAVA!
			output += (tmpVal << (2 - i) * 8);
		}

		// set -ve if bit 24 is set
		if ((output & bit24Set) == bit24Set)
			// TODO - IS THIS CORRECT? ORIGINAL CODE JUST HAD "|" NOT "|="
			output |= neg32;

		return output;
	}

	/**
	 * Do a read camac gpib cycle. Camac always transfers 24 bits to avoid slow transfer size switching at 3988
	 * controller. Calling routine sets to <= 16 bits.
	 *
	 * @param naf
	 *            string of binary camac NAF
	 * @param camacData
	 *            camac data word ptr
	 * @return 1 if successful
	 * @throws DeviceException
	 */
	private int camacRead(String naf, int camacData[]/*
																	 * , Integer status
																	 */) throws DeviceException {
		// Send Camac request for read cycle
		gpib.write(deviceName, naf);

		String readData = "";

		// Read back 3 bytes of data (24-bit data word) as character string
		readData = gpib.read(deviceName, 3);

		// Convert from character string to 32-bit binary value
		// with care that -ve numbers in 24 bits are set to 24 bits in 32 bit
		// variable
		camacData[0] = camacStr24ToInt(readData);

		return 1;
	}

	/**
	 * Do a write camac gpib cycle for KS 3988 crate cntlr. Real writes are in range F16 - F23 and involve 24 bits of
	 * data transferred while other functions are control with no data. calling routine sets bits above 16 to zero.
	 * 24-bit transfers always to reduce switching time. naf string must be long enough for naf + 3 bytes data
	 *
	 * @param naf
	 *            NAF string for cycle
	 * @param camacData
	 *            ptr to camac write data
	 * @return 1 if successful
	 * @throws DeviceException
	 */
	private int camacWrite(String naf, int camacData[]/*
														 * , Integer status
														 */) throws DeviceException {
		// only real write Functions need data
		if (((naf.charAt(2)) >= minCamacWriteXferFunction) && ((naf.charAt(2)) <= maxCamacWriteXferFunction)) {
			// convert camac write data into command string
			String writeData = camacIntToStr24(camacData[0]);

			// append naf with write data
			String command = naf + writeData;

			// write out camac data bytes for 24bit write
			gpib.write(deviceName, command);
		} else {
			gpib.write(deviceName, naf);
		}

		return 1;
	}

	/**
	 * Do a read camac gpib CFUBC block transfer in (repeated) Q-stop mode or timeout after 10 attempted transfers have
	 * failed.
	 *
	 * @param nCycle
	 *            no. of CAMAC cycles reqd
	 * @param naf
	 *            string of binary camac naf
	 * @param camacData
	 *            camac data word
	 * @return 1 if succesful. 0 if unsuccessful.
	 * @throws DeviceException
	 */
	private int camacBlockRead(int nCycle, String naf, int camacData[]/*
																		 * , Integer status
																		 */) throws DeviceException {
		// TODO

		int i, j, // loop counter / temporary variables
		istat = 1; // call function status
		int cyclesLeft = nCycle, // no of cycles remaining as set in
		// TC register
		localData; // local copy of CAMAC data for writes

		// transfer count remaining data
		String transferCountData = "";

		// string of binary camac read TC NAF with space for 3 bytes of write
		// data
		String nafDataRdtc = "";

		// string of binary camac read NAF with space for 3 bytes of write data
		String nafData = "";

		byte dmaBuffer[] = null;

		try {
			// dynamic allocation of buffer for DMA read block transfer
			// - 3 bytes (24-bits) per cycle plus terminator.
			dmaBuffer = new byte[(nCycle * 3) + 1];

			// get GPIB string for set Q-stop block with status byte /EOI
			// enabled
			// CSR transfer mode and send it
			int dummy[] = new int[1];
			nafData = camacGetNaf(30, 0, 17, dummy);
			localData = 0x1400;
			nafData += camacIntToStr24(localData);

			gpib.write(deviceName, nafData/* , status */);

			// get GPIB string for set TC register and action it
			nafData = camacGetNaf(30, 0, 16, dummy);
			localData = cyclesLeft;
			nafData += camacIntToStr24(localData);

			gpib.write(deviceName, nafData/* , status */);

			// get GPIB string for read current TC contents (cycles left)
			nafDataRdtc = camacGetNaf(30, 0, 0, dummy);

			// write camac request for DMA read cycle & continue until TC
			// goes to
			// zero or
			// 10 cycle failures have occurred
			for (i = 0; (i < 10 && cyclesLeft != 0); i++) {
				gpib.write(deviceName, naf/* , status */);

				// read data back into DMA buffer at next
				// position based on data in so far
				String readData = gpib.read(deviceName, nCycle * 3/* , status */);

				// convert chars in Gpib read data string into dmaBuffer bytes
				for (j = 0; j < nCycle * 3; j++) {
					dmaBuffer[nCycle - cyclesLeft + j] = (byte) readData.charAt(j);
				}

				// get no of cycles that have completed
				gpib.write(deviceName, nafDataRdtc/* , status */);

				transferCountData = gpib.read(deviceName/* , status */);

				// convert TC bytes to a 32 bit number with
				// care that -ve numbers in 24 bits are
				// set to 24 bits in 32 bit variable
				cyclesLeft = camacStr24ToInt(transferCountData);
			}

			// if DMA camac cycles over, set data to return long integer
			// array
			if (cyclesLeft != 0) {
				// camacBlockTidy(/*status*/);
				// status = HW_IO_ERROR;
				// return 0;//(NOT_ALL_MOTORS_SUCCEEDED);
				istat = 0;
			} else {
				char tmp[] = new char[3];
				String temp = "";

				for (i = 0, j = 0; i < (nCycle - cyclesLeft); i++, j += 3) {
					// TODO - MESSY, HACKY AND UNPLEASANT!! REFACTOR ME!
					// convert 3 bytes into char array (ie double-bytes)
					tmp[0] = (char) dmaBuffer[j];
					tmp[1] = (char) dmaBuffer[j + 1];
					tmp[2] = (char) dmaBuffer[j + 2];

					// convert into proper string
					temp = String.copyValueOf(tmp, 0, 3);

					// convert DMA data into Camac data output
					camacData[i] = camacStr24ToInt(temp);
				}

				istat = camacBlockTidy(/* status */);

				// hint to Garbage Collector to cleanup DMA buffer
				dmaBuffer = null;
				System.gc();

				return (istat);
			}

		} catch (DeviceException de) {
			// succeeded = false;
			logger.debug("Camac KS3988 ({}): DeviceException occurred in camacBlockRead", getName());

			camacBlockTidy(/* status */);
			return 0; // NOT_ALL_MOTORS_SUCCEEDED
		}

		camacBlockTidy(/* status */);

		// hint to Garbage Collector to cleanup DMA buffer
		dmaBuffer = null;
		System.gc();

		return istat;// succeeded;
	}

	/**
	 * Tidy up after a block read - set single transfer mode again.
	 *
	 * @return 1 if successful.
	 * @throws DeviceException
	 */
	private int camacBlockTidy(/* Integer status */) throws DeviceException {
		// 24-bit CSR value string for 24-bit single transfer mode
		final String csr24BitSingleTransferMode = String.copyValueOf(new char[] { 0x00, 0x00, 0x00 });

		// get GPIB string for set single transfers CSR transfer mode and send
		// it
		gpib.write(deviceName, nafWT2WriteControlStatus + csr24BitSingleTransferMode);

		return 1;
	}

	/**
	 * read status register of 3988 and return flag containing last Q status found.
	 *
	 * @return return Q status - true if set
	 * @throws DeviceException
	 */
	private Boolean camacGetQ(/* Integer status */) throws DeviceException {
		Boolean qSet = Boolean.FALSE;

		// value read from 3988 status register
		int statusRegister[] = new int[1];

		// bit of 3988 status register for no q set
		int noQSetStatusBit = 0x1;

		// read status register
		camacRead(nafRD2ReadControlStatus, statusRegister/* , status */);

		// return value of Q from status register
		qSet = Boolean.valueOf(!((statusRegister[0] & noQSetStatusBit) == 1));

		return qSet;
	}

	//
	// The following methods implement the Camac interface.
	//

	@Override
	public String getDeviceName() {
		return deviceName;
	}

	@Override
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	@Override
	public String getInterfaceName() {
		return interfaceName;
	}

	@Override
	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	@Override
	public int getTimeout() {
		return timeout;
	}

	@Override
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * Gets the status of the Camac controller
	 *
	 * @return status
	 */
	public boolean getStatus() {
		// TODO
		return true;
	}

	/**
	 * @param camacCall
	 * @param station
	 * @param subAddress
	 * @param functionCode
	 * @param qReq
	 * @param nCycle
	 * @param data
	 * @param qSet
	 * @return 1 if successful, 0 if unsuccesful
	 * @throws DeviceException
	 */
	@Override
	public int camacAction(int camacCall, int station, int subAddress, int functionCode, boolean qReq, int nCycle,
			int data[], Boolean qSet/*
									 * , Integer status
									 */) throws DeviceException {
		// conversion factor for ensuring 16-bit
		// data is used for cssa reads & writes
		int convert16Bit = 0x0000FFFF;

		// local copy of CAMAC data for writes
		int localData[] = new int[1];

		// string of binary camac NAF with space for 3 bytes of write data
		String nafData = "";

		if (!deviceConnected) {
			throw new DeviceException("Camac KS3988: KS3988 not connected");
		}

		switch (camacCall) {
		case CSSA:
		case CFSA:

			// get naf string params set and do read/write with 16-bit data
			// if
			// cssa
			nafData = camacGetNaf(station, subAddress, functionCode, data);

			if (functionCode <= maxCamacReadFunction) {
				camacRead(nafData, data/* , status */);

				if (camacCall == CSSA) {
					data[0] = data[0] & convert16Bit;
				}
			} else {
				localData[0] = data[0];

				if (camacCall == CSSA) {
					localData[0] = localData[0] & convert16Bit;
				}

				camacWrite(nafData, localData/* , status */);
			}
			break;

		case CFUBC:
			// Q-stop reads only with 24-bit data
			if (functionCode <= maxCamacReadFunction) {
				nafData = camacGetNaf(station, subAddress, functionCode, localData);

				camacBlockRead(nCycle, nafData, data/* , status */);
			} else {
				logger.debug("Camac KS3988 {}: CFUBC Writes NOT supported in Gpib", getName());
				return 0;
			}
			break;

		default:
			logger.debug("Camac KS3988: Unknown Camac call {}", camacCall);
			break;
		}

		// get CAMAC Q-response if specified
		if (qReq == true) {
			qSet = camacGetQ(/* status */);
		}

		return 1;
	}

}
