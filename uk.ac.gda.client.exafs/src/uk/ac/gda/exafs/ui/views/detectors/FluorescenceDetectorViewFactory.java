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

import org.eclipse.ui.part.ViewPart;

import gda.rcp.views.FindableViewFactoryBase;

/**
 * Simple Factory class to create a {@link FluorescenceDetectorConfigurationView} for named detector.
 * This provides an alternative to the secondaryId mechanism to pass the name of the detector to the view. <p>
 * Create an instance of this factory in the client side spring for each detector you want to open from a menu. e.g.
 *
 * <pre> {@code
<bean id="xspress3ViewFactory" class="uk.ac.gda.exafs.ui.views.detectors.FluorescenceDetectorViewFactory" >
	<property name="name" value="xspress3ViewFactory" />
	<property name="detectorName" value="xspress3" />
</bean>
}
 * </pre>
 *
 * and add the view to the extensions part of plugin.xml :
 <pre>{@code
  <view
        class="gda.rcp.views.ViewFactoryFinder:xspress3ViewFactory"
        id="uk.ac.gda.exafs.ui.views.xspress3View"
        name="New Xspress3 view"
        restorable="true">
  </view>
}</pre>
. The command to open the view then uses the id 'uk.ac.gda.exafs.ui.views.xspress3View' for the viewId :

 <pre>{@code
 <command
       commandId="org.eclipse.ui.views.showView"
       label="Xspress3"
       style="push">
    <parameter
          name="org.eclipse.ui.views.showView.viewId"
          value="uk.ac.gda.exafs.ui.views.xspress3View">
    </parameter>
 </command>
}</pre>

 */
public class FluorescenceDetectorViewFactory extends FindableViewFactoryBase {

	private String detectorName = "";

	public String getDetectorName() {
		return detectorName;
	}

	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}

	@Override
	public ViewPart createView() {
		FluorescenceDetectorConfigurationView fluoDetectorView = new FluorescenceDetectorConfigurationView();
		fluoDetectorView.setDetectorName(detectorName);
		return fluoDetectorView;
	}
}
