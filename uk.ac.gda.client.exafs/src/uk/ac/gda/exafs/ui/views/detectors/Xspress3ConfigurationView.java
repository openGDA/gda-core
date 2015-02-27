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

import gda.factory.Finder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.exafs.ui.composites.detectors.FluorescenceDetectorComposite;

/**
 * Configures the regions of interest of an Xspress3 detector. Name is hardcoded but could be replaced with a java
 * property if required.
 */
public class Xspress3ConfigurationView extends ViewPart implements FluorescenceConfigurationView {

	public static final String ID = "uk.ac.gda.client.exafs.ui.views.detectors.xspress3";
	private FluorescenceDetectorComposite x3Composite;

	public Xspress3ConfigurationView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		FluorescenceDetector theDetector = (FluorescenceDetector) Finder.getInstance().find("xspress3");
		x3Composite = new FluorescenceDetectorComposite(parent, SWT.NONE, this, theDetector);
	}

	@Override
	public void setFocus() {
		x3Composite.setFocus();
	}

	@Override
	public void dispose() {
		x3Composite.dispose();
		super.dispose();
	}

	public FluorescenceDetectorComposite getFluorescenceDetectorComposite() {
		return x3Composite;
	}

	@Override
	public void applyConfigurationToDetector() {
		getFluorescenceDetectorComposite().getController().applyConfigurationToDetector();
	}

	@Override
	public void fetchConfigurationFromDetector() {
		getFluorescenceDetectorComposite().getController().fetchConfigurationFromDetector();
	}

}
