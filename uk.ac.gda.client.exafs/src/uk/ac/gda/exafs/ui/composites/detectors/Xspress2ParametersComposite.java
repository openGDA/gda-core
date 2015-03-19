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

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Xspress3 specific wrapper for the FluorescenceDetectorComposite.
 * <p>
 * Assumes the Xspress2 detector is called "xspress2system" but this could be changed to use a Java property.
 */
public class Xspress2ParametersComposite extends Composite {

	FluorescenceDetectorComposite x2Composite;

	public Xspress2ParametersComposite(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());
		x2Composite = FluorescenceDetectorCompositeFactory.createNewXspress2Composite(this);
	}

	@Override
	public void dispose() {
		super.dispose();
		x2Composite.dispose();
	}

	public FluorescenceDetectorComposite getFluorescenceDetectorComposite() {
		return x2Composite;
	}
}
