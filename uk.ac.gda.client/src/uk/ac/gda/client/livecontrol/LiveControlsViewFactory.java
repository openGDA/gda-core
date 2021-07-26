/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.client.livecontrol;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.ui.part.ViewPart;

/**
 * View factory for creating a {@link LiveControlsView} using a specific controlSet object.
 * To use it, add a view to the org.eclipse.ui.views extension points in the client plugin.xml. e.g. :
 * <pre>
 *  {@code
	<view
        category="uk.ac.gda.client.exafs.category"
        class="uk.ac.gda.client.livecontrol.LiveControlsViewFactory:controlSetName"
        icon="platform:/plugin/uk.ac.gda.client.exafs/icons/control_play_blue.png"
        id="uk.ac.gda.client.livecontrol.liveControlSet"
        name="Live controls"
    	restorable="true">
    </view>
      }
 * </pre>
 *
 * 'controlSetName' should be replaced with the ID of the controlSet object in the client side spring configuration
 * that you want the view to use. The view can then be referenced in the usual way from menu items etc.
 *
 */
public class LiveControlsViewFactory implements IExecutableExtensionFactory, IExecutableExtension {

		private String configName = "";

		@Override
		public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
				throws CoreException {
			if (propertyName.equals("class") && data instanceof String) {
				configName = (String) data;
			}
		}

		@Override
		public ViewPart create() {
			LiveControlsView liveControlsView = new LiveControlsView();
			liveControlsView.setConfigName(configName);
			return liveControlsView;
		}
}
