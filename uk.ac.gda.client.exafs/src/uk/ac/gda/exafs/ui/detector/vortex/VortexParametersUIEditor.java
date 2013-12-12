/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

import gda.configuration.properties.LocalProperties;
import gda.device.Timer;
import gda.device.XmapDetector;
import gda.factory.Finder;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.RegionOfInterest;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentBeanManager;
import uk.ac.gda.client.experimentdefinition.ui.handlers.XMLCommandHandler;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.composites.FluorescenceComposite;
import uk.ac.gda.exafs.ui.detector.DetectorEditor;
import uk.ac.gda.exafs.ui.detector.IDetectorROICompositeFactory;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.selector.BeanSelectionEvent;
import uk.ac.gda.richbeans.components.selector.BeanSelectionListener;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.editors.DirtyContainer;

import com.swtdesigner.SWTResourceManager;

public class VortexParametersUIEditor extends DetectorEditor {
	private static final Logger logger = LoggerFactory.getLogger(VortexParametersUIEditor.class);
	public Label acquireFileLabel;
	protected VortexParameters vortexParameters;
	private ComboWrapper countType;
	private Button autoSave;
	private BooleanWrapper saveRawSpectrum;
	private Button acquireBtn;
	private VortexAcquire vortexAcquire;
	private XmapDetector xmapDetector;
	
	public VortexParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean, "vortexConfig");
		vortexParameters = (VortexParameters) editingBean;
	}

	@Override
	protected String getRichEditorTabText() {
		return "Vortex";
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		String detectorName = vortexParameters.getDetectorName();
		xmapDetector = (XmapDetector) Finder.getInstance().find(detectorName);
		String tfgName = vortexParameters.getTfgName();
		Timer tfg = (Timer) Finder.getInstance().find(tfgName);
		vortexAcquire = new VortexAcquire(sashPlotFormComposite, xmapDetector, tfg, getSite().getShell().getDisplay(), plot);
		Composite left = sashPlotFormComposite.getLeft();
		vortexAcquire.createAcquire(parent, left);
		createROIPanel(left);
		vortexAcquire.addAcquireListener(plotData, dataWrapper, getCurrentSelectedElementIndex(), getDetectorList(), getDetectorElementComposite());
		vortexAcquire.addLoadListener(vortexParameters, getDetectorList(), getDetectorElementComposite(), getCurrentSelectedElementIndex());
		sashPlotFormComposite.setWeights(new int[] { 35, 74 });
		if (!ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.DETECTOR_OUTPUT_IN_OUTPUT_PARAMETERS))
			addOutputPreferences(left);
		configureUI();
		
		getDetectorList().addBeanSelectionListener(new BeanSelectionListener() {
			@Override
			public void selectionChanged(BeanSelectionEvent evt) {
				int currentSelectedElementIndex = getCurrentSelectedElementIndex();
				int[][][] data3d = vortexAcquire.getData3d();
				plot.plot(evt.getSelectionIndex(),false, data3d, getDetectorElementComposite(), currentSelectedElementIndex, false, null);
				getDetectorElementComposite().getRegionList().setSelectedIndex(vortexParameters.getSelectedRegionNumber());
				
			}
		});
	}

	private void createROIPanel(final Composite left) {
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
			IDetectorROICompositeFactory factory = VortexParametersUIHelper.INSTANCE.getDetectorROICompositeFactory();
			createDetectorList(grid, DetectorElement.class, detectorList.size(), RegionOfInterest.class, factory, false);
			VortexParametersUIHelper.INSTANCE.setDetectorListGridOrder(getDetectorList());
			getDetectorElementComposite().setWindowsEditable(false);
			getDetectorElementComposite().setMinimumRegions(VortexParametersUIHelper.INSTANCE.getMinimumRegions());
			getDetectorElementComposite().setMaximumRegions(VortexParametersUIHelper.INSTANCE.getMaximumRegions());
		} catch (Exception e1) {
			logger.error("Cannot create ui for VortexParameters", e1);
		}
	}

	private void addOutputPreferences(Composite comp) {
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

	@Override
	public void linkUI(final boolean isPageChange) {
		super.linkUI(isPageChange);
		getDetectorElementComposite().setIndividualElements(true);
	}


	@Override
	public void notifyFileSaved(File file) {
		@SuppressWarnings("unchecked")
		final FluorescenceComposite comp = (FluorescenceComposite) BeanUI.getBeanField("fluorescenceParameters", DetectorParameters.class);
		if (comp == null || comp.isDisposed())
			return;
		comp.getDetectorType().setValue("Silicon");
		comp.getConfigFileName().setValue(file.getAbsolutePath());
	}

	public ScaleBox getCollectionTime() {
		return vortexAcquire.getAcquireTime();
	}

	public ComboWrapper getCountType() {
		return countType;
	}

	public BooleanWrapper getSaveRawSpectrum() {
		return saveRawSpectrum;
	}

	@Override
	protected String getDataXMLName() {
		String varDir = LocalProperties.get(LocalProperties.GDA_VAR_DIR);
		return varDir + "/vortex_editor_data.xml";
	}

	@Override
	public void dispose() {
		if (countType != null)
			countType.dispose();
		autoSave.dispose();
		saveRawSpectrum.dispose();
		acquireFileLabel.dispose();
		acquireBtn.dispose();
		super.dispose();
	}
	
	protected double getDetectorCollectionTime() {
		return (Double) getCollectionTime().getValue();
	}
	
	protected long getAcquireWaitTime() {
		return Math.round((Double) getCollectionTime().getValue() * 0.1d);
	}
	
	public XMLCommandHandler getXMLCommandHandler() {
		return ExperimentBeanManager.INSTANCE.getXmlCommandHandler(VortexParameters.class);
	}
	
	protected String getDetectorName() {
		return vortexParameters.getDetectorName();
	}
	
}