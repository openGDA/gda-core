/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.excalibur.impl;

import gov.aps.jca.CAException;
import uk.ac.gda.devices.excalibur.NodeFix;

/**
 *
 */
public class NodeFixImpl extends ConfigFixImpl implements NodeFix {

	private static final String CHIP_MAX8_RBV = "ChipMax8_RBV";
	private static final String CHIP_MAX7_RBV = "ChipMax7_RBV";
	private static final String CHIP_MAX6_RBV = "ChipMax6_RBV";
	private static final String CHIP_MAX5_RBV = "ChipMax5_RBV";
	private static final String CHIP_MAX4_RBV = "ChipMax4_RBV";
	private static final String CHIP_MAX3_RBV = "ChipMax3_RBV";
	private static final String CHIP_MAX2_RBV = "ChipMax2_RBV";
	private static final String CHIP_MAX1_RBV = "ChipMax1_RBV";
	private static final String CHIP_MIN8_RBV = "ChipMin8_RBV";
	private static final String CHIP_MIN7_RBV = "ChipMin7_RBV";
	private static final String CHIP_MIN6_RBV = "ChipMin6_RBV";
	private static final String CHIP_MIN5_RBV = "ChipMin5_RBV";
	private static final String CHIP_MIN4_RBV = "ChipMin4_RBV";
	private static final String CHIP_MIN3_RBV = "ChipMin3_RBV";
	private static final String CHIP_MIN2_RBV = "ChipMin2_RBV";
	private static final String CHIP_MIN1_RBV = "ChipMin1_RBV";
	private static final String CHIP_SUM8_RBV = "ChipSum8_RBV";
	private static final String CHIP_SUM7_RBV = "ChipSum7_RBV";
	private static final String CHIP_SUM6_RBV = "ChipSum6_RBV";
	private static final String CHIP_SUM5_RBV = "ChipSum5_RBV";
	private static final String CHIP_SUM4_RBV = "ChipSum4_RBV";
	private static final String CHIP_SUM3_RBV = "ChipSum3_RBV";
	private static final String CHIP_SUM2_RBV = "ChipSum2_RBV";
	private static final String CHIP_SUM1_RBV = "ChipSum1_RBV";

	@Override
	public double getChipSum1_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_SUM1_RBV)); 
	}

	@Override
	public double getChipSum2_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_SUM2_RBV)); 
	}

	@Override
	public double getChipSum3_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_SUM3_RBV)); 
	}

	@Override
	public double getChipSum4_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_SUM4_RBV)); 
	}

	@Override
	public double getChipSum5_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_SUM5_RBV)); 
	}

	@Override
	public double getChipSum6_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_SUM6_RBV)); 
	}

	@Override
	public double getChipSum7_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_SUM7_RBV)); 
	}

	@Override
	public double getChipSum8_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_SUM8_RBV)); 
	}

	@Override
	public double getChipMin1_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_MIN1_RBV)); 
	}

	@Override
	public double getChipMin2_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_MIN2_RBV)); 
	}

	@Override
	public double getChipMin3_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_MIN3_RBV)); 
	}

	@Override
	public double getChipMin4_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_MIN4_RBV)); 
	}

	@Override
	public double getChipMin5_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_MIN5_RBV)); 
	}

	@Override
	public double getChipMin6_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_MIN6_RBV)); 
	}

	@Override
	public double getChipMin7_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_MIN7_RBV)); 
	}

	@Override
	public double getChipMin8_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_MIN8_RBV)); 
	}

	@Override
	public double getChipMax1_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_MAX1_RBV)); 
	}

	@Override
	public double getChipMax2_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_MAX2_RBV)); 
	}

	@Override
	public double getChipMax3_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_MAX3_RBV)); 
	}

	@Override
	public double getChipMax4_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_MAX4_RBV)); 
	}

	@Override
	public double getChipMax5_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_MAX5_RBV)); 
	}

	@Override
	public double getChipMax6_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_MAX6_RBV)); 
	}

	@Override
	public double getChipMax7_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_MAX7_RBV)); 
	}

	@Override
	public double getChipMax8_RBV() throws CAException, InterruptedException, Exception {
		   return EPICS_CONTROLLER.cagetDouble(getChannel(CHIP_MAX8_RBV)); 
	}

}
