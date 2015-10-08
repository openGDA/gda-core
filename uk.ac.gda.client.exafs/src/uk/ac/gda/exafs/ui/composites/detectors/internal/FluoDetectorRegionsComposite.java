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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.richbeans.api.widget.IFieldWidget;
import org.eclipse.richbeans.widgets.scalebox.NumberBox;
import org.eclipse.richbeans.widgets.selector.ListEditor;
import org.eclipse.richbeans.widgets.selector.VerticalListEditor;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper.BOOLEAN_MODE;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import uk.ac.gda.beans.DetectorROI;

import com.swtdesigner.SWTResourceManager;

public class FluoDetectorRegionsComposite extends Composite {

	private FluoDetectorElementsComposite elementsComposite;
	private Button importButton;
	private BooleanWrapper applyToAllCheckbox;
	private VerticalListEditor regionList;
	private FluoDetectorROIComposite detectorROIComposite;

	public FluoDetectorRegionsComposite(Composite parent, int style,
			final FluoDetectorElementsComposite elementsComposite) {
		super(parent, style);
		this.setLayout(new FillLayout());

		this.elementsComposite = elementsComposite;

		Group regionsGroup = new Group(this, SWT.NONE);
		regionsGroup.setText("Regions");
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(regionsGroup);

		importButton = new Button(regionsGroup, SWT.NONE);
		importButton.setImage(SWTResourceManager.getImage(FluoDetectorRegionsComposite.class,
				"/icons/calculator_edit.png"));
		importButton.setText("Import");
		importButton.setToolTipText("Import Regions Of Interest from other Parameters files");

		applyToAllCheckbox = new BooleanWrapper(regionsGroup, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(applyToAllCheckbox);
		applyToAllCheckbox.setValue(true);
		applyToAllCheckbox.setBooleanMode(BOOLEAN_MODE.REVERSE); // because the XspressParameters object field isEditIndividualElements has the opposite sense
		applyToAllCheckbox.setText("Apply to all");
		applyToAllCheckbox.setToolTipText("Apply the same ROIs to all detector elements");

		regionList = new VerticalListEditor(regionsGroup, SWT.NONE);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, true).applyTo(regionList);
		regionList.setRequireSelectionPack(false);
		regionList.setTemplateName("ROI");
		regionList.setNameField("roiName");
		regionList.setAdditionalFields(new String[] { "roiStart", "roiEnd" });
		regionList.setColumnNames("Name", "Start", "End");
		regionList.setColumnWidths(200, 50, 50);
		regionList.setShowAdditionalFields(true);
		regionList.setListHeight(100);
		regionList.setEditorClass(DetectorROI.class);

		detectorROIComposite = new FluoDetectorROIComposite(regionList, SWT.NONE);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(detectorROIComposite);
		regionList.setEditorUI(detectorROIComposite);
		regionList.setVisible(true);
		regionList.setEnabled(true);
		detectorROIComposite.setVisible(true);
	}

	/*
	 * For access by BeanUI only. This name must match the field name in DetectorElement.
	 */
	public IFieldWidget getExcluded() {
		// This is a bit of a hack to get around the fact that in the parameters bean, each element has a separate list
		// of regions, but in the detector class there is just a single list. BeanUI expects to find the getExcluded()
		// method on the editor UI object for the detector element, but conceptually it fits much more naturally to put
		// the "Enabled" box in the elements composite next to the element grid. To do this we keep a reference to the
		// element composite and pass the getExcluded() call on to it.
		return elementsComposite.getExcluded();
	}

	public Button getImportButton() {
		return importButton;
	}

	public BooleanWrapper getApplyToAllCheckbox() {
		return applyToAllCheckbox;
	}

	public ListEditor getRegionList() {
		return regionList;
	}

	public FluoDetectorROIComposite getDetectorROIComposite() {
		return detectorROIComposite;
	}

	public NumberBox getRoiStart() {
		return detectorROIComposite.getRoiStart();
	}

	public NumberBox getRoiEnd() {
		return detectorROIComposite.getRoiEnd();
	}
}
