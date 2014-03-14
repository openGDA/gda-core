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

import org.eclipse.core.runtime.IConfigurationElement;
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
	public Object createLiveView(IConfigurationElement configurationElement) throws Exception {
		MJPegView mjPegView = new MJPegView(adController, configurationElement);
		mjPegView.afterPropertiesSet();
		return mjPegView;
	}

	@Override
	public Object createArrayView(IConfigurationElement configurationElement) throws Exception {
		TwoDArrayView twoDArrayView = new TwoDArrayView(adController, configurationElement);
		twoDArrayView.afterPropertiesSet();
		return twoDArrayView;
	}

	@Override
	public Object createProfileView(IConfigurationElement configurationElement) throws Exception {
		HistogramView histogramView = new HistogramView(adController, configurationElement);
		histogramView.afterPropertiesSet();
		return histogramView;
	}
}

