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

package uk.ac.gda.exafs.ui.detectors.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.beans.IRichBean;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.exafs.ui.detector.DetectorROIComposite;
import uk.ac.gda.exafs.ui.detector.VortexROIComposite;
import uk.ac.gda.exafs.ui.detector.XspressROIComposite;
import uk.ac.gda.exafs.ui.detectors.DetectorType;

public class ConfigurationComposite extends Composite {

	private DetectorType detType;
	private Composite modeComposite;
	private AcquireComposite acquireComposite;
	private DetectorROIComposite roiComposite;
	private Composite outputOptionsComposite;

	public ConfigurationComposite(Composite parent, int style, DetectorType detType, IRichBean detectorParameters) {
		super(parent, style);
		parent.setLayout(new FillLayout(SWT.VERTICAL));

		this.detType = detType;
		createModeComposite();
		createAcquireComposite(detectorParameters);
		createROICompoiste();
		createOutputOptionsComposite();
	}

	private void createModeComposite() {
		switch (detType) {
		case XSPRESS:
			modeComposite = new XspressModeComposite(getParent(), SWT.NONE);
			break;
		case VORTEX:
			modeComposite = new VortexModeComposite(getParent(), SWT.NONE);
			break;
		}
	}

	private void createAcquireComposite(IRichBean detectorParameters) {		
		switch (detType) {
		case XSPRESS:
			modeComposite = new XspressAcquireComposite(getParent(), SWT.NONE, (XspressParameters) detectorParameters);
			break;
		case VORTEX:
			modeComposite = new VortexAcquireComposite(getParent(), SWT.NONE);
			break;
		}
	}

	private void createROICompoiste() {
		switch (detType) {
		case XSPRESS:
			modeComposite = new XspressROIComposite(getParent(), SWT.NONE);
			break;
		case VORTEX:
			modeComposite = new VortexROIComposite(getParent(), SWT.NONE);
			break;
		}
	}

	private void createOutputOptionsComposite() {
		outputOptionsComposite = new Composite(getParent(), SWT.NONE);

	}

}
