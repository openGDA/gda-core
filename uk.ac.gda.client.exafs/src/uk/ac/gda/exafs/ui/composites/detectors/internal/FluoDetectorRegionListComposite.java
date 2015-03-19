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

import org.dawnsci.common.richbeans.components.scalebox.ScaleBox;
import org.dawnsci.common.richbeans.components.selector.BeanSelectionEvent;
import org.dawnsci.common.richbeans.components.selector.BeanSelectionListener;
import org.dawnsci.common.richbeans.components.selector.VerticalListEditor;
import org.dawnsci.common.richbeans.components.wrappers.BooleanWrapper;
import org.dawnsci.common.richbeans.components.wrappers.BooleanWrapper.BOOLEAN_MODE;
import org.dawnsci.common.richbeans.components.wrappers.LabelWrapper;
import org.dawnsci.common.richbeans.components.wrappers.LabelWrapper.TEXT_TYPE;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.beans.DetectorROI;

public class FluoDetectorRegionListComposite extends Composite {

	private VerticalListEditor regionList;
	private FluoDetectorROIComposite detectorROIComposite;
	private BooleanWrapper excluded;
	private LabelWrapper elementName;
	private LabelWrapper totalCounts;
	private LabelWrapper elementCounts;

	public FluoDetectorRegionListComposite(Composite parent, int style, final FluoDetectorCompositeController controller) {
		super(parent, style);

		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

		elementName = new LabelWrapper(this, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(elementName);
		elementName.setTextType(TEXT_TYPE.PLAIN_TEXT);

		excluded = new BooleanWrapper(this, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(excluded);
		excluded.setBooleanMode(BOOLEAN_MODE.REVERSE);
		excluded.setText("Enabled");

		Label totalCountsLabel = new Label(this, SWT.NONE);
		totalCountsLabel.setText("All elements total counts:");

		totalCounts = new LabelWrapper(this, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(totalCounts);
		totalCounts.setTextType(TEXT_TYPE.PLAIN_TEXT);

		Label elementCountsLabel = new Label(this, SWT.NONE);
		elementCountsLabel.setText("This element total counts:");

		elementCounts = new LabelWrapper(this, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(elementCounts);
		elementCounts.setTextType(TEXT_TYPE.PLAIN_TEXT);

		regionList = new VerticalListEditor(this, SWT.BORDER);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, true).applyTo(regionList);
		regionList.setRequireSelectionPack(false);
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
		GridDataFactory.fillDefaults().span(2, 1).applyTo(detectorROIComposite);

		regionList.setEditorUI(detectorROIComposite);

		regionList.setVisible(true);
		regionList.setEnabled(true);
		detectorROIComposite.setVisible(true);
	}

	public BooleanWrapper getExcluded() {
		return excluded;
	}

	public VerticalListEditor getRegionList() {
		return regionList;
	}

	public LabelWrapper getElementNameLabel() {
		return elementName;
	}

	public LabelWrapper getTotalCountsLabel() {
		return totalCounts;
	}

	public LabelWrapper getElementCountsLabel() {
		return elementCounts;
	}

	public ScaleBox getRoiStart() {
		return detectorROIComposite.getRoiStart();
	}

	public ScaleBox getRoiEnd() {
		return detectorROIComposite.getRoiEnd();
	}

	public LabelWrapper getROICountsLabel() {
		return detectorROIComposite.getCountsLabel();
	}
}
