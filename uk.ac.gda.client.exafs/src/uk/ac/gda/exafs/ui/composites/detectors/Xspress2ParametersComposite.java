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

package uk.ac.gda.exafs.ui.composites.detectors;

import gda.factory.Finder;

import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.devices.detector.FluorescenceDetector;

/**
 * Xspress3-specific subclass of FluorescenceDetectorComposite.
 */
public class Xspress2ParametersComposite extends FluorescenceDetectorComposite {

	public Xspress2ParametersComposite(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected FluorescenceDetector getDetectorInstance() {
		return (FluorescenceDetector) Finder.getInstance().find("xspress2system");
	}
}
