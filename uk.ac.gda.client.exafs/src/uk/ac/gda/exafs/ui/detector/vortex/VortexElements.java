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

package uk.ac.gda.exafs.ui.detector.vortex;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.beans.vortex.VortexROI;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.exafs.ui.detector.Counts;
import uk.ac.gda.exafs.ui.detector.Elements;
import uk.ac.gda.exafs.ui.detector.wizards.vortex.ImportVortexROIWizard;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;
import uk.ac.gda.richbeans.editors.DirtyContainer;

import com.swtdesigner.SWTResourceManager;

public class VortexElements extends Elements{
	private static final Logger logger = LoggerFactory.getLogger(VortexElements.class);
	private BooleanWrapper saveRawSpectrum;
	private VortexParameters vortexParameters;
	private Composite importComposite;
	
	public VortexElements(Shell shell, DirtyContainer dirtyContainer, SashFormPlotComposite sashPlotFormComposite, VortexParameters vortexParameters, final Counts counts) {
		super(shell, dirtyContainer, sashPlotFormComposite, counts);
		this.vortexParameters = vortexParameters;
	}

	public void addOutputPreferences(Composite comp) {
		Group xspressParametersGroup = new Group(comp, SWT.NONE);
		xspressParametersGroup.setText("Output Preferences");
		xspressParametersGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		xspressParametersGroup.setLayout(gridLayout);
		saveRawSpectrum = new BooleanWrapper(xspressParametersGroup, SWT.NONE);
		saveRawSpectrum.setText("Save raw spectrum to file");
		saveRawSpectrum.setValue(false);
	}
	
	public void createROI(final Composite left) {
		Composite grid = new Composite(left, SWT.BORDER);
		grid.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grid.setLayout(new GridLayout());
		List<DetectorElement> detectorList = vortexParameters.getDetectorList();
		if (detectorList.size() > 1) {
			Composite buttonPanel = new Composite(grid, SWT.NONE);
			buttonPanel.setLayout(new GridLayout(2, false));
			Label applyToAllLabel = new Label(buttonPanel, SWT.NONE);
			applyToAllLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			applyToAllLabel.setText("Apply To All Elements ");
			Button applyToAllButton = new Button(buttonPanel, SWT.NONE);
			GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gridData.widthHint = 60;
			gridData.minimumWidth = 60;
			applyToAllButton.setLayoutData(gridData);
			applyToAllButton.setImage(SWTResourceManager.getImage(VortexParametersUIEditor.class, "/icons/camera_go.png"));
			applyToAllButton.setToolTipText("Apply current detector regions of interest to all other detector elements.");
			SelectionAdapter applyToAllListener = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					applyToAll(true);
				}
			};
			applyToAllButton.addSelectionListener(applyToAllListener);
			Label sep = new Label(grid, SWT.SEPARATOR | SWT.HORIZONTAL);
			sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		}
		Label detectorElementsLabel = new Label(grid, SWT.NONE);
		detectorElementsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (detectorList.size() > 1)
			detectorElementsLabel.setText(" Detector Element");
		else
			detectorElementsLabel.setText(" Regions of Interest");
		try {
			createImportButton(grid, detectorList.size());
			createDetectorList(grid, DetectorElement.class, detectorList.size(), VortexROI.class, true);
			detectorListComposite.getDetectorElementComposite().setWindowsEditable(false);
			detectorListComposite.getDetectorElementComposite().setMinimumRegions(VortexParametersUIHelper.INSTANCE.getMinimumRegions());
			detectorListComposite.getDetectorElementComposite().setMaximumRegions(VortexParametersUIHelper.INSTANCE.getMaximumRegions());
		} catch (Exception e1) {
			logger.error("Cannot create ui for VortexParameters", e1);
		}
	}
	
	public void createImportButton(Composite parent, final int elementListSize){
		importComposite = new Composite(parent, SWT.NONE);
		importComposite.setLayout(new GridLayout(2, false));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(importComposite);
		Label importLabel = new Label(importComposite, SWT.NONE);
		importLabel.setText("Import Regions Of Interest");
		final Button importButton = new Button(importComposite, SWT.NONE);
		GridDataFactory grab = GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(true, false);
		grab.hint(60, SWT.DEFAULT).applyTo(importButton);
		importButton.setImage(SWTResourceManager.getImage(VortexParametersUIEditor.class, "/icons/calculator_edit.png"));
		importButton.setToolTipText("Import Regions Of Interest from other Parameters files");
		final SelectionAdapter importButtonListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				WizardDialog dialog = new WizardDialog(importButton.getShell(), new ImportVortexROIWizard(elementListSize, detectorListComposite.getDetectorElementComposite(), detectorListComposite.getDetectorList()));
				dialog.create();
				dialog.open();
			}
		};
		importButton.addSelectionListener(importButtonListener);
	}

	public BooleanWrapper getSaveRawSpectrum() {
		return saveRawSpectrum;
	}
	
	protected void setImportCompositeVisible(boolean visible) {
		GridUtils.setVisibleAndLayout(importComposite, visible);
	}
	
}