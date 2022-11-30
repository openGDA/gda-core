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

import uk.ac.gda.epics.client.views.controllers.INDROIModelViewController;
import uk.ac.gda.epics.client.views.model.NdRoiModel;

/**
 *
 */
public class NdRoiModelSimulator implements NdRoiModel {

	@Override
	public int getMinX() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMinY() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSizeX() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSizeY() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setSizeX(int sizeX) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSizeY(int sizeY) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStartX(int startX) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStartY(int startY) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public int getBinX() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getBinY() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setBinX(int binX) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBinY(int binY) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean registerNDRoiModelViewController(INDROIModelViewController viewController) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeNDRoiModelViewController(INDROIModelViewController viewController) {
		// TODO Auto-generated method stub
		return false;
	}

}
