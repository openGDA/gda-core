/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.views.detectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;

/**
 * Class to allow a specific Synoptic view to be created from a 'org.eclipse.ui.views' extension point.
 * Class name for view is SynopticViewFactory with class of view to open appended to it. e.g.
 * <pre>{@code
 *       <view
            class="uk.ac.gda.ui.views.synoptic.SynopticViewFactory:uk.ac.gda.ui.views.synoptic.XesStageView"
            id="uk.ac.gda.ui.views.synoptic.xesStageView"
            name="XES Stage view"
            restorable="true">
      </view> }
     </pre>
 * The view id can then be used in a showView command in usual way.
 */
public class DetectorRateViewFactory implements IExecutableExtensionFactory, IExecutableExtension {
	protected static final Logger logger = LoggerFactory.getLogger(DetectorRateViewFactory.class);

	private String viewName = "";

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		if (propertyName.equals("class") && data instanceof String) {
			viewName = (String) data;
		}
	}

	@Override
	public ViewPart create() {
		DetectorRateViewConfig viewConfig = Finder.getInstance().find(viewName);
		DetectorRateView detectorRateView = new DetectorRateView();
		detectorRateView.setViewConfig(viewConfig);
		return detectorRateView;
	}

}
