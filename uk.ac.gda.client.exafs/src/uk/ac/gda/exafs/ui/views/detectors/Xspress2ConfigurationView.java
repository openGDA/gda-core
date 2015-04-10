/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.exafs.ui.composites.detectors.FluorescenceDetectorComposite;
import uk.ac.gda.exafs.ui.composites.detectors.Xspress2CompositeController;

/**
 * Configures the regions of interest of an Xspress2 detector.
 */
public class Xspress2ConfigurationView extends FluorescenceDetectorConfigurationView {

	public static final String ID = "uk.ac.gda.client.exafs.ui.views.detectors.xspress2";

	@Override
	public void createPartControl(Composite parent) {
		fluorescenceDetectorComposite = new FluorescenceDetectorComposite(parent, SWT.NONE);
		controller = new Xspress2CompositeController();
		controller.setEditorUI(fluorescenceDetectorComposite);
		controller.initialise();
	}
}
