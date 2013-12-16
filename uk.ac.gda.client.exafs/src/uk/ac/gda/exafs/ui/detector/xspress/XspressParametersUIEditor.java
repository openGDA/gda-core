/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.detector.xspress;

import gda.configuration.properties.LocalProperties;
import gda.device.detector.xspress.ResGrades;
import gda.device.detector.xspress.XspressDetector;

import java.awt.Color;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.xspress.DetectorElement;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.beans.xspress.XspressROI;
import uk.ac.gda.client.experimentdefinition.ExperimentBeanManager;
import uk.ac.gda.client.experimentdefinition.ui.handlers.XMLCommandHandler;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.data.ScanObjectManager;
import uk.ac.gda.exafs.ui.detector.Counts;
import uk.ac.gda.exafs.ui.detector.DetectorEditor;
import uk.ac.gda.exafs.ui.detector.DetectorElementComposite;
import uk.ac.gda.exafs.ui.detector.IDetectorROICompositeFactory;
import uk.ac.gda.exafs.ui.detector.XspressROIComposite;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.richbeans.components.selector.BeanSelectionEvent;
import uk.ac.gda.richbeans.components.selector.BeanSelectionListener;
import uk.ac.gda.richbeans.components.selector.ListEditor;
import uk.ac.gda.richbeans.components.selector.ListEditorUIAdapter;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;
import uk.ac.gda.richbeans.event.ValueListener;

public class XspressParametersUIEditor extends DetectorEditor {
	private static final Logger logger = LoggerFactory.getLogger(XspressParametersUIEditor.class);
	private Composite parentComposite;
	private Composite middleComposite;
	private XspressAcquire xspressAcquire;
	private XspressParameters xspressParameters;
	private Button applyToAllButton;
	private Button applyToAllLabel;
	private SelectionAdapter applyToAllListener;
	private Group detectorElementsGroup;
	private BooleanWrapper showIndividualElements;
	private Label lblRegionBins;
	private ValueListener detectorElementCompositeValueListener;
	private ResolutionGrade resolutionGrade;
	private ReadoutMode readoutMode;
	private RegionType regionType;
	private XspressPreferences xspressPreferences;
	
	public XspressParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean, "xspressConfig");
		xspressParameters = (XspressParameters) editingBean;
	}

	@Override
	protected String getRichEditorTabText() {
		return "Xspress";
	}

	@Override
	public void createPartControl(Composite composite) {
		parentComposite = composite;
		super.createPartControl(parentComposite);
		counts = new Counts();
		Composite left = sashPlotFormComposite.getLeft();
		Composite topComposite = new Composite(left, SWT.NONE);
		topComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		topComposite.setLayout(gridLayout_1);
		resolutionGrade = new ResolutionGrade(topComposite);
		readoutMode = new ReadoutMode(topComposite, resolutionGrade);
		if(readoutMode.isModeOveride()) {
			GridUtils.setVisibleAndLayout(readoutMode.getReadoutMode(), false);
			GridUtils.setVisibleAndLayout(resolutionGrade.getResGradeLabel(), false);
			GridUtils.setVisibleAndLayout(resolutionGrade.getResolutionGradeCombo(), false);
			GridUtils.setVisibleAndLayout(lblRegionBins, false);
			GridUtils.setVisibleAndLayout(regionType.getRegionType(), false);
		}
		lblRegionBins = new Label(topComposite, SWT.NONE);
		lblRegionBins.setText("Region type");
		regionType = new RegionType(topComposite);
		xspressAcquire = new XspressAcquire(left, sashPlotFormComposite, getSite().getShell().getDisplay(), readoutMode.getReadoutMode(), resolutionGrade.getResolutionGradeCombo(), plot, dirtyContainer);
		
		createElements(left);
		if(!ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.DETECTOR_OUTPUT_IN_OUTPUT_PARAMETERS) && !ScanObjectManager.isXESOnlyMode())
			xspressPreferences = new XspressPreferences(left);
		sashPlotFormComposite.setWeights(new int[] { 30, 74 });
		configureUI();
		createApplyToAllObserver();
		xspressAcquire.init(xspressParameters, counts, showIndividualElements);
		xspressAcquire.addAcquireListener(getCurrentSelectedElementIndex(), getDetectorList(), getDetectorElementComposite());
		getDetectorList().addBeanSelectionListener(new BeanSelectionListener() {
			@Override
			public void selectionChanged(BeanSelectionEvent evt) {
				int[][][] mcaData = xspressAcquire.getMcaData();
				plot.plot(evt.getSelectionIndex(), mcaData, false, null);
				DetectorElementComposite detectorElementComposite = getDetectorElementComposite();
				detectorElementComposite.setTotalElementCounts(counts.getTotalElementCounts(evt.getSelectionIndex(), mcaData));
				detectorElementComposite.setTotalCounts(counts.getTotalCounts(mcaData));
			}
		});
		xspressAcquire.addLoadListener(getDetectorList(), getDetectorElementComposite(), xspressParameters.getDetectorList().size());
	}
	
	private void createElements(final Composite composite){
		Composite grid = new Composite(composite, SWT.BORDER);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(grid);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(grid);
		showIndividualElements = new BooleanWrapper(grid, SWT.NONE);
		showIndividualElements.setText("Show individual elements");
		Composite middleComposite = new Composite(grid, SWT.BORDER);
		middleComposite.setLayout(new GridLayout(2, false));
		GridDataFactory.fillDefaults().applyTo(middleComposite);
		applyToAllLabel = new Button(middleComposite, SWT.CHECK);
		applyToAllLabel.setText("Apply Changes To All Elements ");
		applyToAllLabel.setEnabled(true);
		applyToAllLabel.setSelection(true);
		applyToAllLabel.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (applyToAllLabel.getSelection()) {
					if (applyToAll(true)) {
						if (detectorElementCompositeValueListener == null)
							createApplyToAllObserver();
					} 
					else
						applyToAllLabel.setSelection(false);
				}
				applyToAllButton.setEnabled(!applyToAllLabel.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);				
			}
		});
		applyToAllButton = new Button(middleComposite, SWT.NONE);
		applyToAllButton.setEnabled(false);
		GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gridData.widthHint = 90;
		gridData.minimumWidth = 90;
		applyToAllButton.setLayoutData(gridData);
		applyToAllButton.setText("Apply now");
		applyToAllButton.setToolTipText("Apply current detector regions of interest to all other detector elements.");
		applyToAllListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				applyToAll(true);
			}
		};
		applyToAllButton.addSelectionListener(applyToAllListener);
		showIndividualElements.addValueListener(new ValueAdapter("editIndividualElements") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				if ((Boolean) e.getValue() == false)
					if (!applyToAll(true)) {
						showIndividualElements.setValue(true);
						return;
					}
				updateElementsVisibility(composite);
			}
		});

		detectorElementsGroup = new Group(grid, SWT.BORDER);
		GridLayoutFactory.fillDefaults().applyTo(detectorElementsGroup);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(detectorElementsGroup);
		detectorElementsGroup.setText("Detector Elements");
		try {
			IDetectorROICompositeFactory factory = XspressParametersUIHelper.INSTANCE.getDetectorROICompositeFactory();
			if (xspressParameters != null) {
				List<DetectorElement> detectorList = xspressParameters.getDetectorList();
				createDetectorList(detectorElementsGroup, DetectorElement.class, detectorList.size(), XspressROI.class, factory,false);
				XspressParametersUIHelper.INSTANCE.setDetectorListGridOrder(getDetectorList());
				getDetectorElementComposite().setMinimumRegions(XspressParametersUIHelper.INSTANCE.getMinimumRegions());
				getDetectorElementComposite().setMaximumRegions(XspressParametersUIHelper.INSTANCE.getMaximumRegions());
			}
		} catch (Exception e1) {
			logger.error("Cannot create region editor.", e1);
		}
	}
	
	protected void createApplyToAllObserver() {
		detectorElementCompositeValueListener = new ValueListener() {
			
			@Override
			public void valueChangePerformed(ValueEvent e) {
				if (applyToAllLabel.getSelection())
					applyToAll(false);
			}
			
			@Override
			public String getValueListenerName() {
				return null;
			}
		};
		getDetectorElementComposite().getWindowStart().addValueListener(detectorElementCompositeValueListener);
		getDetectorElementComposite().getWindowEnd().addValueListener(detectorElementCompositeValueListener);
		getDetectorElementComposite().getRegionList().addValueListener(detectorElementCompositeValueListener);
	}

	@Override
	protected java.awt.Color getChannelColor(int iChannel) {
		String resGrade = getResGradeAllowingForReadoutMode();
		if (resGrade.startsWith(ResGrades.THRESHOLD)) {
			switch (iChannel) {
			case 0:
				return Color.RED; // BAD
			case 1:
				return Color.BLUE; // GOOD
			default:
				return super.getChannelColor(iChannel);
			}
		}
		return super.getChannelColor(iChannel);
	}

	private String getResGradeAllowingForReadoutMode() {
		String uiReadoutMode = (String) getReadoutMode().getValue();
		return uiReadoutMode.equals(XspressDetector.READOUT_ROIS) ? (String) getResGrade().getValue() : ResGrades.NONE;
	}

	protected String getChannelName(int iChannel) {
		String resGrade = getResGradeAllowingForReadoutMode();
		if (resGrade.equals(ResGrades.ALLGRADES))
			return "Resolution Grade " + (iChannel + 1);
		else if (resGrade.startsWith(ResGrades.THRESHOLD)) {
			switch (iChannel) {
			case 0:
				return "Bad";
			case 1:
				return "Good";
			default:
				return "" + iChannel;
			}
		}
		return "" + iChannel;
	}

	protected void updateElementsVisibility(Composite composite) {
		GridUtils.startMultiLayout(composite.getParent());
		try {
			boolean currentEditIndividual = showIndividualElements.getValue();
			if (currentEditIndividual)
				detectorElementsGroup.setText("Detector Elements");
			else
				detectorElementsGroup.setText("All Elements");
			GridUtils.setVisibleAndLayout(middleComposite, currentEditIndividual);
			GridUtils.setVisibleAndLayout(applyToAllLabel, currentEditIndividual);
			GridUtils.setVisibleAndLayout(applyToAllButton, currentEditIndividual);
			GridUtils.setVisibleAndLayout(getDetectorElementComposite().getName(), currentEditIndividual);
			GridUtils.setVisibleAndLayout(getDetectorElementComposite().getExcluded(), currentEditIndividual);
			getDetectorElementComposite().setIndividualElements(currentEditIndividual);
			GridUtils.layoutFull(getDetectorElementComposite().getExcluded().getParent());
			getDetectorList().setListVisible(currentEditIndividual);
			autoApplyToAll(!currentEditIndividual);
			counts.calculateAndPlotCountTotals(currentEditIndividual, true, detectorData, getDetectorElementComposite(),  getCurrentSelectedElementIndex());
		} finally {
			GridUtils.endMultiLayout();
		}
	}

	protected void updateRoiVisibility() {
		boolean isRoi = readoutMode.getReadoutMode().getValue().equals(XspressDetector.READOUT_ROIS);
		getDetectorElementComposite().setWindowsEditable(!isRoi);
		Composite roi = getDetectorElementComposite().getRegionList();
		GridUtils.setVisibleAndLayout(roi, isRoi);
		setImportCompositeVisible(isRoi);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		if (!(Boolean) showIndividualElements.getValue() || applyToAllLabel.getSelection())
			applyToAll(false);
		super.doSave(monitor);
	}

	private ListEditorUIAdapter listEditorUI = new ListEditorUIAdapter() {
		@Override
		public void notifySelected(ListEditor listEditor) {
			XspressROIComposite xspressROIComposite = (XspressROIComposite) (listEditor.getEditorUI());
			xspressROIComposite.setFitTypeVisibility();
		}
	};

	@Override
	public void linkUI(final boolean isPageChange) {
		GridUtils.startMultiLayout(parentComposite);
		try {
			super.linkUI(isPageChange);
			readoutMode.updateOverrideMode();
			boolean readoutRois = false;
			if(resolutionGrade.getResolutionGradeCombo().getValue().equals(XspressDetector.READOUT_ROIS))
				readoutRois = true;
			resolutionGrade.updateResModeItems(readoutRois);
			updateRoiVisibility();
			updateElementsVisibility(parentComposite);
			resolutionGrade.updateResGradeVisibility(parentComposite);
			listEditorUI.notifySelected(getDetectorElementComposite().getRegionList());
			getDetectorElementComposite().getRegionList().setListEditorUI(listEditorUI);
			updateROIAfterElementCompositeChange();
		} finally {
			GridUtils.endMultiLayout();
		}
	}

	@Override
	public void dispose() {
		if (!applyToAllButton.isDisposed())
			applyToAllButton.removeSelectionListener(applyToAllListener);
		if (!super.isDisposed())
			super.dispose();
	}

	public ComboWrapper getResGrade() {
		return resolutionGrade.getResolutionGradeCombo();
	}

	public BooleanWrapper getEditIndividualElements() {
		return showIndividualElements;
	}

	public ComboWrapper getReadoutMode() {
		return readoutMode.getReadoutMode();
	}

//	@Override
//	public void notifyFileSaved(File file) {
//		FluorescenceComposite fluorescenceComposite = (FluorescenceComposite) BeanUI.getBeanField("fluorescenceParameters", DetectorParameters.class);
//		if (fluorescenceComposite == null || fluorescenceComposite.isDisposed())
//			return;
//		fluorescenceComposite.getDetectorType().setValue("Germanium");
//		fluorescenceComposite.getConfigFileName().setValue(file.getAbsolutePath());
//	}

	public BooleanWrapper getOnlyShowFF() {
		return xspressPreferences.getOnlyShowFF();
	}

	public BooleanWrapper getShowDTRawValues() {
		return xspressPreferences.getShowDTRawValues();
	}

	public BooleanWrapper getSaveRawSpectrum() {
		return xspressPreferences.getSaveRawSpectrum();
	}

	@Override
	protected String getDataXMLName() {
		String varDir = LocalProperties.get(LocalProperties.GDA_VAR_DIR);
		return varDir + "/xspress_editor_data.xml";
	}
	
	public ComboWrapper getRegionType() {
		return regionType.getRegionType();
	}
	
	protected String getDetectorName() {
		return xspressParameters.getDetectorName();
	}
	
	public XMLCommandHandler getXMLCommandHandler() {
		return ExperimentBeanManager.INSTANCE.getXmlCommandHandler(XspressParameters.class);
	}
	
}