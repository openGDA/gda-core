/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.adviewer.views;

import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.epics.adviewer.ADController;

public class ViewCreatorImpl implements ViewCreator, InitializingBean {

	protected ADController adController;

	public void setAdController(ADController adController) {
		this.adController = adController;
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		if (adController == null)
			throw new Exception("adController == null");

	}

	@Override
	public Object createLiveView() {
		return new MJPegView(adController);
	}

	@Override
	public Object createArrayView() {
		return new TwoDArrayView(adController);
	}

	@Override
	public Object createProfileView() {
		return new HistogramView(adController);
	}
}

