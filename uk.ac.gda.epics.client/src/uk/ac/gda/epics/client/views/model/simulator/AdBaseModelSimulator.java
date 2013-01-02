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

package uk.ac.gda.epics.client.views.model.simulator;

import uk.ac.gda.epics.client.views.controllers.IAdBaseViewController;
import uk.ac.gda.epics.client.views.model.AdBaseModel;

/**
 *
 */
public class AdBaseModelSimulator implements AdBaseModel {

	@Override
	public short getDetectorState_RBV() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getArrayCounter_RBV() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getTimeRemaining_RBV() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getArrayRate_RBV() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumExposuresCounter_RBV() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumImagesCounter_RBV() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getAcqExposureRBV() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAcqExposure(double exposureTime) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public double getAcqPeriodRBV() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public short getAcquireState() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getDatatype() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean registerAdBaseViewController(IAdBaseViewController takeFlatController) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAdBaseViewController(IAdBaseViewController takeFlatController) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getPortName() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
