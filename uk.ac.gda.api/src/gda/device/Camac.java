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

package gda.device;

/*
 * Q's would Camac instances be permanently assigned to a specific controller on a specific branch/crate/GPIB_device? or
 * would there be Camac instance per controller (crate)? ie would we want to reassign a camac instance to a different
 * controller? branch/crate number doesnt seem to apply to KS3988/GPIB stuff? ie is branch/crate no. meaningless in
 * context of GPIB bus? just doing basic cfssa/cssa io xfer calls initially, do we need to write block transfer routines
 * yet? or later? can we get esone dll, header(s) & libs from Hytec? do we want to expose (subset of?) ESONE standard
 * routine interface? (would mean writing implementation of ESONE calls via GPIB on KS3988 side)
 */

/**
 * Interface for interacting with CAMAC crate controllers.
 */
public interface Camac extends Device {

	// CAMAC calls (ESONE standard subroutine names)

	// public static final int CDREG = 0; // Declare identifier for
	// subsequent
	// CAMAC operation (branch, crate, N, A)
	// N.B. CDREG may only make sense for Hytec/ESONE, since GPIB doesnt use
	// branch/crate???
	// public static final int CCINIT = 1; //
	/**
	 * CSSA is the 16-bit CAMAC data i/o transfer
	 */
	public static final int CSSA = 2;

	/**
	 * CFSA is the 24-bit CAMAC data i/o transfer
	 */
	public static final int CFSA = 3;

	/**
	 * CFUBC is the 24-bit CAMAC block data i/o transfer (+/-Q)
	 */
	public static final int CFUBC = 4;

	// public static final int CINI = 5; // Initialise camac and book crate
	// public static final int CCCI = 6; // Set or clear I (crate inhibit)
	// line
	// public static final int CCCZ = 7; // Carry out crate initialise - set
	// Z

	// CAMAC command function codes

	// Read Commands (0-7)
	/**
	 * RD1 is the code to read external group 1 register
	 */
	public static final int RD1 = 0;

	/**
	 * RD2 is the code to read internal group 2 register
	 */
	public static final int RD2 = 1;

	/**
	 * RC1 is the code to read & clear external group 1 register
	 */
	public static final int RC1 = 2;

	// public static final int RCOMP1 = 3; // read complement of group1
	// register

	// 4-7 unassigned

	// Control Commands (8-15)
	/**
	 * TLM is the code to test overflow (LAM) status
	 */
	public static final int TLM = 8;

	/**
	 * CL1 is the code to clear group 1 register
	 */
	public static final int CL1 = 9;

	/**
	 * CLM is the code to clear module LAM
	 */
	public static final int CLM = 10;

	// public static final int CL2 = 11; // clear group 2 register

	// 12-15 unassigned

	// Write Commands (16-23)
	/**
	 * WT1 is the code to overwrite external group 1 register
	 */
	public static final int WT1 = 16;

	/**
	 * WT2 is the code to overwrite internal group 2 register
	 */
	public static final int WT2 = 17; // 

	// public static final int SW1 = 18; // selective overwrite of external
	// group
	// 1 register
	// public static final int SW2 = 19; // selective overwrite of internal
	// group
	// 2 register
	// SC2 = 23 in mark's code? ask if correct, as disagrees with camac docs
	// found on web
	/**
	 * SC2 is the code to selectivly clear internal group 2 register
	 */
	public static final int SC2 = 23; // 

	// 20-23 unassigned

	// Control Commands (24-31)
	/**
	 * DIS is the code to disable module (LAM or operation)
	 */
	public static final int DIS = 24;

	// public static final int INP = 25; // inc preselected registers
	/**
	 * ENB is the code to enable module (LAM or operation)
	 */
	public static final int ENB = 26;

	// 28-31 unassigned

	// Non-standard Camac function? - presumably used by PINCER?
	/**
	 * ZINHOFF is the code to do crate Z & turn inhibit off
	 */
	public static final int ZINHOFF = 34;

	/**
	 * @return Returns the deviceName.
	 */
	public String getDeviceName();

	/**
	 * @param deviceName
	 *            The deviceName to set.
	 */
	public void setDeviceName(String deviceName);

	/**
	 * @return Returns the interfaceName.
	 */
	public String getInterfaceName();

	/**
	 * @param interfaceName
	 *            The interfaceName to set.
	 */
	public void setInterfaceName(String interfaceName);

	/**
	 * @return Returns the timeout.
	 */
	public int getTimeout();

	/**
	 * @param timeout
	 *            The timeout to set.
	 */
	public void setTimeout(int timeout);

	// public boolean getStatus(String checkCommand);

	/**
	 * @param camacCall
	 * @param station
	 * @param subAddress
	 * @param functionCode
	 * @param qReq
	 * @param nCycle
	 * @param data
	 * @param qSet
	 * @return status code
	 * @throws DeviceException
	 */
	public int camacAction(int camacCall, int station, int subAddress, int functionCode, boolean qReq, int nCycle,
			int data[], Boolean qSet/*
									 * , Integer status
									 */) throws DeviceException;
}
