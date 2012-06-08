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

import uk.ac.gda.epics.client.views.controllers.IMJpegViewController;
import uk.ac.gda.epics.client.views.model.FfMpegModel;

/**
 *
 */
public class FfMpegModelSimulator implements FfMpegModel {

	@Override
	public String getNdArrayPort() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNdArrayPort(String ndArrayPort) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public int getDim0Size() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDim1Size() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getTimeStamp() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getMjpegUrl() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getJpegUrl() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean registerMJpegViewController(IMJpegViewController viewController) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeMJpegViewController(IMJpegViewController viewController) {
		// TODO Auto-generated method stub
		return false;
	}

}
