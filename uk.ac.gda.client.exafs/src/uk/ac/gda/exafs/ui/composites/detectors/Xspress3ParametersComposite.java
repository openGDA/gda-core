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

import org.dawnsci.common.richbeans.components.FieldBeanComposite;
import org.dawnsci.common.richbeans.components.scalebox.ScaleBox;
import org.dawnsci.common.richbeans.components.selector.GridListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.devices.detector.FluorescenceDetector;

/**
 * Xspress3 specific wrapper for the FluorescenceDetectorComposite.
 * <p>
 * Assumes the Xspress3 detector is called "xspress3" but this could be refactored to use a Java property.
 */
public class Xspress3ParametersComposite extends FieldBeanComposite {
	FluorescenceDetectorComposite x3Composite;

	public Xspress3ParametersComposite(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());
		FluorescenceDetector theDetector = (FluorescenceDetector) Finder.getInstance().find("xspress3");
		x3Composite = new FluorescenceDetectorComposite(this, SWT.NONE, theDetector);
	}

	public ScaleBox getCollectionTime() {
		return x3Composite.getCollectionTime();
	}

	public GridListEditor getDetectorList() {
		return x3Composite.getDetectorList();
	}

	public FluorescenceDetectorComposite getFluorescenceDetectorComposite() {
		return x3Composite;
	}
}
