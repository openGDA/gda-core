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

package uk.ac.gda.exafs.ui.detector;

import org.eclipse.swt.widgets.Composite;
import org.springframework.util.Assert;

import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.LabelWrapper;

/**
 * Common for all Detector Element Composites
 */
abstract public class DetectorROIComposite extends Composite {
	public DetectorROIComposite(Composite parent, int style) {
		super(parent, style);
	}

	
	public static class FieldWidgetsForDetectorElementsComposite {
		public ScaleBox roiStart;
		public ScaleBox roiEnd;
		public LabelWrapper counts;

		public FieldWidgetsForDetectorElementsComposite(ScaleBox roiStart, ScaleBox roiEnd, LabelWrapper counts) {
			Assert.notNull(roiStart);
			Assert.notNull(roiEnd);
			Assert.notNull(counts);
			this.roiStart = roiStart;
			this.roiEnd = roiEnd;
			this.counts = counts;
		}

		public ScaleBox getRoiStart() {
			return roiStart;
		}

		public ScaleBox getRoiEnd() {
			return roiEnd;
		}

		public LabelWrapper getCounts() {
			return counts;
		}
	}
	
	abstract public FieldWidgetsForDetectorElementsComposite getFieldWidgetsForDetectorElementsComposite();
}
