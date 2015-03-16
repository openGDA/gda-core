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

package uk.ac.gda.exafs.ui.composites.detectors.internal;

import org.dawnsci.common.richbeans.components.FieldBeanComposite;
import org.dawnsci.common.richbeans.components.selector.BeanSelectionEvent;
import org.dawnsci.common.richbeans.components.selector.BeanSelectionListener;
import org.dawnsci.common.richbeans.components.selector.VerticalListEditor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.beans.DetectorROI;

public class FluoDetectorRegionListComposite extends FieldBeanComposite {

	private VerticalListEditor regionList;
	private FluoDetectorROIComposite detectorROIComposite;

	public FluoDetectorRegionListComposite(Composite parent, int style, FluoDetectorCompositeController controller) {
		super(parent, style);

		GridLayoutFactory.fillDefaults().applyTo(this);

		regionList = new VerticalListEditor(this, SWT.NONE);
		regionList.setRequireSelectionPack(false);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(regionList);
		regionList.setTemplateName("ROI");
		regionList.setNameField("roiName");
		regionList.setListHeight(100);
		regionList.setEditorClass(DetectorROI.class);

		regionList.addBeanSelectionListener(new BeanSelectionListener() {
			@Override
			public void selectionChanged(BeanSelectionEvent evt) {
				detectorROIComposite.updateUIAfterDetectorElementCompositeChange();
			}
		});

		detectorROIComposite = new FluoDetectorROIComposite(regionList, SWT.NONE, controller);
		GridDataFactory.fillDefaults().applyTo(detectorROIComposite);

		regionList.setEditorUI(detectorROIComposite);

		regionList.setVisible(true);
		regionList.setEnabled(true);
		detectorROIComposite.setVisible(true);
	}

	public VerticalListEditor getRegionList() {
		return regionList;
	}
}
