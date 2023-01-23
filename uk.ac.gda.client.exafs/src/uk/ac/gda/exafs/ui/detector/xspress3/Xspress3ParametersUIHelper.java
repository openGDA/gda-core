/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.detector.xspress3;

import org.eclipse.richbeans.widgets.selector.GridListEditor;
import org.eclipse.richbeans.widgets.selector.GridListEditor.GRID_ORDER;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;
import uk.ac.gda.exafs.ui.detector.DetectorROIComposite;
import uk.ac.gda.exafs.ui.detector.IDetectorROICompositeFactory;
import uk.ac.gda.exafs.ui.detector.VortexROIComposite;

@Deprecated(since="GDA 8.44")
public enum Xspress3ParametersUIHelper {
	INSTANCE;

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(Xspress3ParametersUIHelper.class);

	public IDetectorROICompositeFactory getDetectorROICompositeFactory() {
		logger.deprecatedMethod("getDetectorROICompositeFactory()");
		IDetectorROICompositeFactory factory = new IDetectorROICompositeFactory() {
			@Override
			public DetectorROIComposite createDetectorROIComposite(Composite parent, int style) {
				return new VortexROIComposite(parent, style);
			}
		};
		return factory;
	}

	public void setDetectorListGridOrder(GridListEditor gridListEditor) {
		logger.deprecatedMethod("setDetectorListGridOrder(GridListEditor)");
		gridListEditor.setGridOrder(GRID_ORDER.LEFT_TO_RIGHT_TOP_TO_BOTTOM);
	}

	public int getMinimumRegions() {
		logger.deprecatedMethod("getMinimumRegions()");
		return 1;
	}

	public int getMaximumRegions() {
		logger.deprecatedMethod("getMaximumRegions()");
		return 16;
	}
}
