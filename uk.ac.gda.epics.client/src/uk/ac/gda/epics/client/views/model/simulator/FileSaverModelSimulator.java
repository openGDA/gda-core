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

import uk.ac.gda.epics.client.views.controllers.IFileSaverViewController;
import uk.ac.gda.epics.client.views.model.FileSaverModel;

/**
 *
 */
public class FileSaverModelSimulator implements FileSaverModel {

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
	public short getCaptureState() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean registerFileSaverViewController(IFileSaverViewController adBaseViewController) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeFileSaverViewController(IFileSaverViewController adBaseViewController) {
		// TODO Auto-generated method stub
		return false;
	}

}
