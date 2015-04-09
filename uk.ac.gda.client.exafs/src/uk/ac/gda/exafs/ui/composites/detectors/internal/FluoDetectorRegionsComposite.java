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
import org.dawnsci.common.richbeans.components.selector.GridListEditor;
import org.dawnsci.common.richbeans.components.wrappers.LabelWrapper;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.IDetectorElement;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.exafs.ui.detector.wizards.ImportFluoDetROIWizard;

import com.swtdesigner.SWTResourceManager;

public class FluoDetectorRegionsComposite extends Composite {

	private static Logger logger = LoggerFactory.getLogger(FluoDetectorRegionsComposite.class);

	private GridListEditor detectorElementTable;
	private FluoDetectorRegionListComposite regionListComposite;
	private FluoDetectorCompositeController controller;

	public FluoDetectorRegionsComposite(Composite composite, FluoDetectorCompositeController controller, Class<? extends FluorescenceDetectorParameters> detectorParametersClazz) {
		super(composite, SWT.NONE);
		this.setLayout(new FillLayout());

		this.controller = controller;

		Group regionsGroup = new Group(this, SWT.NONE);
		regionsGroup.setText("Regions");
		GridLayoutFactory.swtDefaults().applyTo(regionsGroup);

		createApplyToAllPanel(regionsGroup);

		createSeparator(regionsGroup);
		
		createImportButton(regionsGroup, detectorParametersClazz);

		createDetectorElementTable(regionsGroup);

		createRegionsList(regionsGroup);

		configureDetectorElementTable();

		createBindings();
	}

	private void createImportButton(Group regionsGroup, final Class<? extends FluorescenceDetectorParameters> detectorParametersClass) {
		Composite importComposite = new Composite(regionsGroup, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(importComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(importComposite);
		
		final Label importLabel = new Label(importComposite, SWT.NONE);
		importLabel.setText("Import Regions Of Interest");
		final Button importButton = new Button(importComposite, SWT.NONE);
		GridDataFactory grab = GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(true, false);
		grab.hint(60, SWT.DEFAULT).applyTo(importButton);
		importButton.setImage(SWTResourceManager.getImage(FluoDetectorRegionsComposite.class, "/icons/calculator_edit.png"));
		importButton.setToolTipText("Import Regions Of Interest from other Parameters files");
		final SelectionAdapter importButtonListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				WizardDialog dialog = new WizardDialog(importButton.getShell(), new ImportFluoDetROIWizard(
						regionListComposite.getRegionList(), detectorElementTable, detectorParametersClass));
				dialog.create();
				dialog.open();
			}
		};
		importButton.addSelectionListener(importButtonListener);
	}

	private void createApplyToAllPanel(Group regionsGroup) {
		final Composite applyToAllPanel = new Composite(regionsGroup, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(applyToAllPanel);

		Button applyToAllCheckbox = new Button(applyToAllPanel, SWT.CHECK);
		applyToAllCheckbox.setText("Apply changes to all elements");
		applyToAllCheckbox.setSelection(true);
		applyToAllCheckbox.setEnabled(false);
	}

	private void createSeparator(Group regionsGroup) {
		Label sep = new Label(regionsGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sep);
	}

	private void createDetectorElementTable(Group regionsGroup) {

		Label elementsLabel = new Label(regionsGroup, SWT.NONE);
		elementsLabel.setText("Detector Elements:");

		int elementListSize = controller.getDetector().getNumberOfChannels();
		double elementListSizeSquareRoot = Math.sqrt(elementListSize);

		// Squared table of Detector Elements
		int columns = elementListSize / 2;
		int rows = 2;

		if (Double.compare(elementListSizeSquareRoot, (int) elementListSizeSquareRoot) == 0) {
			columns = (int) elementListSizeSquareRoot;
			rows = (int) elementListSizeSquareRoot;
		} else if ((elementListSize % 2) != 0) {
			logger.warn("Non-even, non-square number of detector elements: not sure how to layout the grid!");
		}

		this.detectorElementTable = new GridListEditor(regionsGroup, SWT.NONE, columns, rows);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(detectorElementTable);
	}

	private void createRegionsList(Group regionsGroup) {
		regionListComposite = new FluoDetectorRegionListComposite(regionsGroup, SWT.NONE, controller);
		GridDataFactory.fillDefaults().applyTo(regionListComposite);
	}

	private void configureDetectorElementTable() {
		detectorElementTable.setEditorClass(DetectorElement.class);
		detectorElementTable.setEditorUI(regionListComposite);
		detectorElementTable.setGridWidth(200);
		detectorElementTable.setEnabled(true);
		detectorElementTable.setAdditionalLabelProvider(new ColumnLabelProvider() {
			private final Color lightGray = SWTResourceManager.getColor(SWT.COLOR_GRAY);

			@Override
			public Color getForeground(Object element) {
				if (element instanceof IDetectorElement) {
					IDetectorElement detectorElement = (IDetectorElement) element;
					if (detectorElement.isExcluded()) {
						return lightGray;
					}
				}
				return null;
			}

			@Override
			public String getText(Object element) {
				return null;
			}
		});
	}

	private void createBindings() {
		detectorElementTable.addBeanSelectionListener(new BeanSelectionListener() {
			@Override
			public void selectionChanged(BeanSelectionEvent evt) {
				controller.replot();
			}
		});
	}

	public GridListEditor getDetectorList() {
		return detectorElementTable;
	}

	public int getSelectedDetectorElement() {
		return detectorElementTable.getSelectedIndex();
	}

	public LabelWrapper getElementNameLabel() {
		return regionListComposite.getElementNameLabel();
	}

	public LabelWrapper getTotalCountsLabel() {
		return regionListComposite.getTotalCountsLabel();
	}

	public LabelWrapper getElementCountsLabel() {
		return regionListComposite.getElementCountsLabel();
	}

	public ScaleBox getRoiStart() {
		return regionListComposite.getRoiStart();
	}

	public ScaleBox getRoiEnd() {
		return regionListComposite.getRoiEnd();
	}

	public LabelWrapper getROICountsLabel() {
		return regionListComposite.getROICountsLabel();
	}
}
