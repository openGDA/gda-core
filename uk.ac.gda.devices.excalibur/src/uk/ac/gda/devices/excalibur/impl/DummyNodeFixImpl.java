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
public class DummyNodeFixImpl extends DummyConfigFixImpl implements NodeFix {


	private double chipMin1_RBV;	
	private double chipMin2_RBV;
	private double chipMin3_RBV;
	private double chipMin4_RBV;
	private double chipMin5_RBV;
	private double chipMin6_RBV;
	private double chipMin7_RBV;
	private double chipMin8_RBV;
	private double chipSum1_RBV;	
	private double chipSum2_RBV;
	private double chipSum3_RBV;
	private double chipSum4_RBV;
	private double chipSum5_RBV;
	private double chipSum6_RBV;
	private double chipSum7_RBV;
	private double chipSum8_RBV;
	private double chipMax1_RBV;	
	private double chipMax2_RBV;
	private double chipMax3_RBV;
	private double chipMax4_RBV;
	private double chipMax5_RBV;
	private double chipMax6_RBV;
	private double chipMax7_RBV;
	private double chipMax8_RBV;


	@Override
	public double getChipSum1_RBV() throws CAException, InterruptedException, Exception {
		   return chipSum1_RBV; 
	}

	@Override
	public double getChipSum2_RBV() throws CAException, InterruptedException, Exception {
		   return chipSum2_RBV;
	}

	@Override
	public double getChipSum3_RBV() throws CAException, InterruptedException, Exception {
		   return chipSum3_RBV;
	}

	@Override
	public double getChipSum4_RBV() throws CAException, InterruptedException, Exception {
		   return chipSum4_RBV;
	}

	@Override
	public double getChipSum5_RBV() throws CAException, InterruptedException, Exception {
		   return chipSum5_RBV;
	}

	@Override
	public double getChipSum6_RBV() throws CAException, InterruptedException, Exception {
		   return chipSum6_RBV;
	}

	@Override
	public double getChipSum7_RBV() throws CAException, InterruptedException, Exception {
		   return chipSum7_RBV;
	}

	@Override
	public double getChipSum8_RBV() throws CAException, InterruptedException, Exception {
		   return chipSum8_RBV;
	}

	@Override
	public double getChipMin1_RBV() throws CAException, InterruptedException, Exception {
		   return chipMin1_RBV;
	}

	@Override
	public double getChipMin2_RBV() throws CAException, InterruptedException, Exception {
		   return chipMin2_RBV; 
	}

	@Override
	public double getChipMin3_RBV() throws CAException, InterruptedException, Exception {
		   return chipMin3_RBV; 
	}

	@Override
	public double getChipMin4_RBV() throws CAException, InterruptedException, Exception {
		   return chipMin4_RBV; 
	}

	@Override
	public double getChipMin5_RBV() throws CAException, InterruptedException, Exception {
		   return chipMin5_RBV; 
	}

	@Override
	public double getChipMin6_RBV() throws CAException, InterruptedException, Exception {
		   return chipMin6_RBV; 
	}

	@Override
	public double getChipMin7_RBV() throws CAException, InterruptedException, Exception {
		   return chipMin7_RBV; 
	}

	@Override
	public double getChipMin8_RBV() throws CAException, InterruptedException, Exception {
		   return chipMin8_RBV; 
	}

	@Override
	public double getChipMax1_RBV() throws CAException, InterruptedException, Exception {
		   return chipMax1_RBV; 
	}

	@Override
	public double getChipMax2_RBV() throws CAException, InterruptedException, Exception {
		   return chipMax2_RBV; 
	}

	@Override
	public double getChipMax3_RBV() throws CAException, InterruptedException, Exception {
		   return chipMax3_RBV;
	}

	@Override
	public double getChipMax4_RBV() throws CAException, InterruptedException, Exception {
		   return chipMax4_RBV; 
	}

	@Override
	public double getChipMax5_RBV() throws CAException, InterruptedException, Exception {
		   return chipMax5_RBV; 
	}

	@Override
	public double getChipMax6_RBV() throws CAException, InterruptedException, Exception {
		   return chipMax6_RBV; 
	}

	@Override
	public double getChipMax7_RBV() throws CAException, InterruptedException, Exception {
		   return chipMax7_RBV; 
	}

	@Override
	public double getChipMax8_RBV() throws CAException, InterruptedException, Exception {
		   return chipMax8_RBV; 
	}

}
