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
import gda.device.DeviceException;
import gda.device.detector.xspress.ResGrades;
import gda.device.detector.xspress.XspressDetector;
import gda.factory.Finder;

import java.awt.Color;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.gda.beans.ElementCountsData;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.xspress.DetectorElement;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.beans.xspress.XspressROI;
import uk.ac.gda.client.experimentdefinition.ExperimentBeanManager;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.ui.handlers.XMLCommandHandler;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.composites.FluorescenceComposite;
import uk.ac.gda.exafs.ui.data.ScanObject;
import uk.ac.gda.exafs.ui.data.ScanObjectManager;
import uk.ac.gda.exafs.ui.detector.DetectorEditor;
import uk.ac.gda.exafs.ui.detector.IDetectorROICompositeFactory;
import uk.ac.gda.exafs.ui.detector.XspressROIComposite;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.selector.ListEditor;
import uk.ac.gda.richbeans.components.selector.ListEditorUIAdapter;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;
import uk.ac.gda.richbeans.components.wrappers.ComboAndNumberWrapper;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;
import uk.ac.gda.richbeans.event.ValueListener;

import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;

public class XspressParametersUIEditor extends DetectorEditor {
	private Composite parentComposite;
	private Composite middleComposite;
	private static final String xspressSaveDir = "gda.device.xspress.spoolDir";
	private static final Logger logger = LoggerFactory.getLogger(XspressParametersUIEditor.class);
	private boolean saveMcaOnAcquire;
	// super mode override property to set the mode always to Scalers and MCA for the parameters file sent to the server
	// this flag will also be used to fix some of the gui display parameters for I18
	// readout mode will be set to regions of Interest for display, but will be sent as Scalers and MCa to the server
	// res grade will be fixed to none and not shown
	// readout type will not be shown
	private boolean modeOverride = LocalProperties.check("gda.xspress.mode.override");
	private ComboWrapper readoutMode;
	private ComboAndNumberWrapper resolutionGradeCombo;
	private XspressParameters xspressParameters;
	private Button applyToAllButton;
	private Button applyToAllLabel;
	private SelectionAdapter applyToAllListener;
	private ScaleBox acquireTime;
	private BooleanWrapper showIndividualElements;
	private Group detectorElementsGroup;
	private boolean isAdditiveResolutionGradeMode = false;
	private Action plotAction;
	private Label resGradeLabel;
	private FileDialog openDialog;
	private Button autoSave;
	private Label acquireFileLabel;
	private BooleanWrapper onlyShowFF;
	private BooleanWrapper showDTRawValues;
	private BooleanWrapper saveRawSpectrum;
	private SelectionAdapter xspressOptionsListener;
	private Label lblRegionBins;
	private ComboWrapper regionType;
	private ValueListener detectorElementCompositeValueListener;
	private ResolutionGrade resolutionGrade;
	private XspressData xspressData;
	private XspressAcquire xspressAcquire;
	
	/**
	 * @param path
	 * @param mappingURL
	 * @param dirtyContainer
	 * @param editingBean
	 */
	public XspressParametersUIEditor(final String path, final URL mappingURL, final DirtyContainer dirtyContainer, final Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean, "xspressConfig");
		xspressParameters = (XspressParameters) editingBean;
	}

	@Override
	protected String getDetectorName() {
		return xspressParameters.getDetectorName();
	}

	@Override
	protected String getRichEditorTabText() {
		return "Xspress";
	}

	@Override
	public void createPartControl(Composite composite) {
		parentComposite = composite;
		super.createPartControl(parentComposite);
		xspressData = new XspressData();
		xspressAcquire = new XspressAcquire();
		Composite left = sashPlotFormComposite.getLeft();
		Composite topComposite = new Composite(left, SWT.NONE);
		topComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		topComposite.setLayout(gridLayout_1);
		createReadoutMode(topComposite);
		resolutionGrade = new ResolutionGrade(topComposite);
		resolutionGradeCombo = resolutionGrade.getResolutionGradeCombo();
		resolutionGradeCombo.addValueListener(new ValueAdapter("resGrade") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateAdditiveMode();
			}
		});
		createAdditiveResModeAction();
		if (modeOverride) {
			GridUtils.setVisibleAndLayout(readoutMode, false);
			GridUtils.setVisibleAndLayout(resGradeLabel, false);
			GridUtils.setVisibleAndLayout(resolutionGradeCombo, false);
			GridUtils.setVisibleAndLayout(lblRegionBins, false);
			GridUtils.setVisibleAndLayout(regionType, false);
		}
		lblRegionBins = new Label(topComposite, SWT.NONE);
		lblRegionBins.setText("Region type");
		regionType = new ComboWrapper(topComposite, SWT.READ_ONLY);
		regionType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		regionType.setItems(new String[]{XspressParameters.VIRTUALSCALER, XspressROI.MCA});
		regionType.select(0);
		createAcquireControl(left);
		createElements(left);
		if (!ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.DETECTOR_OUTPUT_IN_OUTPUT_PARAMETERS))
			addOutputPreferences(left);
		sashPlotFormComposite.setWeights(new int[] { 30, 74 });
		configureUI();
		createApplyToAllObserver();
	}
	
	private void createReadoutMode(final Composite composite){
		Label readoutModeLabel = new Label(composite, SWT.NONE);
		readoutModeLabel.setText("Read out mode");
		readoutModeLabel.setToolTipText("The type of data which will be written to file");
		readoutModeLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		if (modeOverride)
			GridUtils.setVisibleAndLayout(readoutModeLabel, false);
		readoutMode = new ComboWrapper(composite, SWT.READ_ONLY);
		readoutMode.setItems(new String[] { XspressDetector.READOUT_SCALERONLY, XspressDetector.READOUT_MCA, XspressDetector.READOUT_ROIS });
		readoutMode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		readoutMode.select(0);
		readoutMode.addValueListener(new ValueAdapter("readoutMode") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				GridUtils.startMultiLayout(composite.getParent());
				try {
					updateOverrideMode();
					boolean readoutRois = false;
					if(resolutionGrade.getResolutionGradeCombo().getValue().equals(XspressDetector.READOUT_ROIS))
						readoutRois = true;
					resolutionGrade.updateResModeItems(readoutRois);
					updateRoiVisibility();
					updateResGradeVisibility(composite);
					configureUI();
				} finally {
					GridUtils.endMultiLayout();
				}
			}
		});
	}
	
	private void createAcquireControl(Composite composite){
		Group grpAcquire = new Group(composite, SWT.NONE);
		grpAcquire.setText("Acquire Spectra");
		grpAcquire.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		grpAcquire.setLayout(gridLayout);
		Button loadBtn = new Button(grpAcquire, SWT.NONE);
		loadBtn.setImage(SWTResourceManager.getImage(DetectorEditor.class, "/icons/folder.png"));
		loadBtn.setText("Load Saved mca");
		loadBtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {			
				try {
					final String filePath = openDialog.open();
					xspressData.load(openDialog, xspressParameters, filePath);
					getSite().getShell().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							acquireFileLabel.setText("Loaded: " + filePath);
							getDetectorElementComposite().setEndMaximum((detectorData[0][0].length) - 1);
							plot(getDetectorList().getSelectedIndex(),false);
							setWindowsEnabled(true);
						}
					});
				} catch (Exception e1) {
					logger.error("Cannot acquire xspress data", e1);
				}
			}
		});
		Composite acquire = new Composite(grpAcquire, SWT.NONE);
		GridLayout gridLayoutAcq = new GridLayout();
		gridLayoutAcq.numColumns = 5;
		gridLayoutAcq.marginWidth = 0;
		acquire.setLayout(gridLayoutAcq);
		
		Button acquireBtn = new Button(acquire, SWT.NONE);
		acquireBtn.setImage(SWTResourceManager.getImage(DetectorEditor.class, "/icons/application_side_expand.png"));
		acquireBtn.setText("Acquire");
		acquireBtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					String uiReadoutMode = (String) getReadoutMode().getValue();
					String uiResolutionGrade = getResGradeAllowingForReadoutMode();
					XspressDetector xspressDetector = Finder.getInstance().find(xspressParameters.getDetectorName());
					Display display = getSite().getShell().getDisplay();
					xspressAcquire.acquire(xspressDetector, null, acquireTime.getNumericValue(), sashPlotFormComposite, uiReadoutMode, uiResolutionGrade);
					int[][][] mcaData = xspressAcquire.getMcaData();
					dirtyContainer.setDirty(true);
					ElementCountsData[] elementCountsData = ElementCountsData.getDataFor(mcaData);
					getDataWrapper().setValue(elementCountsData);
					counts.calculateAndPlotCountTotals(showIndividualElements.getValue(), true, mcaData, getDetectorElementComposite(), getCurrentSelectedElementIndex());
					if (saveMcaOnAcquire)
						xspressAcquire.saveMca(sashPlotFormComposite, xspressSaveDir, plotData);
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							String acquireFileLabelText = xspressAcquire.getAcquireFileLabelText();
							if(acquireFileLabelText!=null)
								acquireFileLabel.setText(acquireFileLabelText);
							getDetectorElementComposite().setEndMaximum((detectorData[0][0].length) - 1);
							plot(getDetectorList().getSelectedIndex(),true);
							setWindowsEnabled(true);
						}
					});
				} catch (Exception e1) {
					logger.error("Cannot acquire xmap data", e1);
				}
			}
		});
		acquireTime = new ScaleBox(acquire, SWT.NONE);
		acquireTime.setMinimum(1);
		acquireTime.setValue(1000);
		acquireTime.setMaximum(50000);
		acquireTime.setUnit("ms");
		acquireTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		autoSave = new Button(acquire, SWT.CHECK);
		autoSave.setText("Save on Acquire");
		autoSave.setSelection(LocalProperties.check("gda.detectors.save.single.acquire"));
		autoSave.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		autoSave.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				saveMcaOnAcquire = autoSave.getSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		autoSave.setSelection(saveMcaOnAcquire);
		
		acquireFileLabel = new Label(grpAcquire, SWT.NONE);
		acquireFileLabel.setText("										");
		acquireFileLabel.setToolTipText("The file path for the acquire data");
		acquireFileLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		openDialog = new FileDialog(composite.getShell(), SWT.OPEN);
		openDialog.setFilterPath(LocalProperties.get(LocalProperties.GDA_DATAWRITER_DIR));
	}
	
	private void createElements(final Composite composite){
		Composite grid = new Composite(composite, SWT.BORDER);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(grid);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(grid);
		showIndividualElements = new BooleanWrapper(grid, SWT.NONE);
		showIndividualElements.setText("Show individual elements");
		Composite middleComposite = new Composite(grid, SWT.BORDER);
		middleComposite.setLayout(new GridLayout(2, false));
		GridDataFactory.fillDefaults()/*.grab(true, false)*/.applyTo(middleComposite);
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
						// user didn't want to lose individual settings, cancel change
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
		
		// if any changes in certain UI components then apply to all elements
		getDetectorElementComposite().getWindowStart().addValueListener(detectorElementCompositeValueListener);
		getDetectorElementComposite().getWindowEnd().addValueListener(detectorElementCompositeValueListener);
		getDetectorElementComposite().getRegionList().addValueListener(detectorElementCompositeValueListener);
	}

	private void addOutputPreferences(Composite comp) {
		if (!ScanObjectManager.isXESOnlyMode()) {
			Group xspressParametersGroup = new Group(comp, SWT.NONE);
			xspressParametersGroup.setText("Output Preferences");
			xspressParametersGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			GridLayout gridLayout = new GridLayout();
			gridLayout.numColumns = 1;
			xspressParametersGroup.setLayout(gridLayout);
			onlyShowFF = new BooleanWrapper(xspressParametersGroup, SWT.NONE);
			onlyShowFF.setText("Hide individual elements");
			onlyShowFF.setToolTipText("In ascii output, only display the total in-window counts (FF) from the Xspress detector");
			onlyShowFF.setValue(Boolean.FALSE);
			addXspressOptionsListener(onlyShowFF);
			showDTRawValues = new BooleanWrapper(xspressParametersGroup, SWT.NONE);
			showDTRawValues.setText("Show DT values");
			showDTRawValues.setToolTipText("Add the raw scaler values used in deadtime (DT) calculations to ascii output");
			showDTRawValues.setValue(Boolean.FALSE);
			addXspressOptionsListener(showDTRawValues);
			saveRawSpectrum = new BooleanWrapper(xspressParametersGroup, SWT.NONE);
			saveRawSpectrum.setText("Save raw spectrum to file");
			saveRawSpectrum.setValue(false);
		}
	}

	private void addXspressOptionsListener(BooleanWrapper booleanwrapper) {
		if (xspressOptionsListener == null)
			createXspressOptionsListener();
		((Button) booleanwrapper.getControl()).addSelectionListener(xspressOptionsListener);
	}

	protected void createXspressOptionsListener() {
		this.xspressOptionsListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final ScanObject ob = (ScanObject) ExperimentFactory.getExperimentEditorManager().getSelectedScan();
				if (ob != null) {
					try {
						IDetectorParameters params = ob.getDetectorParameters();
						if (params.getExperimentType().equalsIgnoreCase(DetectorParameters.TRANSMISSION_TYPE))
							showWarning();
						else if (params.getExperimentType().equalsIgnoreCase(DetectorParameters.FLUORESCENCE_TYPE)) {
							if (!params.getFluorescenceParameters().getDetectorType().equalsIgnoreCase("Germanium"))
								showWarning();
						}
					} catch (Exception e1) {
						logger.warn("Exception while retrieving current DetectorParameters object", e1);
					}
				}
			}

			protected void showWarning() {
				getSite().getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openWarning(getSite().getShell(),
							"Inconsistent options",
							"The Xspress detector is not currently selected in the detector parameters editor.\n\nIf you wish to use the Xspress then change the detector parameters.");
					}
				});
			}
		};
	}

	private void updateResGradeVisibility(Composite composite) {
		GridUtils.startMultiLayout(composite.getParent());
		try {
			if (readoutMode.getSelectionIndex() == 2 && !modeOverride) {
				GridUtils.setVisibleAndLayout(resGradeLabel, true);
				GridUtils.setVisibleAndLayout(resolutionGradeCombo, true);
				GridUtils.setVisibleAndLayout(lblRegionBins, true);
				GridUtils.setVisibleAndLayout(regionType, true);
			} 
			else {
				GridUtils.setVisibleAndLayout(resGradeLabel, false);
				GridUtils.setVisibleAndLayout(resolutionGradeCombo, false);
				GridUtils.setVisibleAndLayout(lblRegionBins, false);
				GridUtils.setVisibleAndLayout(regionType, false);
			}
		} finally {
			GridUtils.endMultiLayout();
		}
	}

	private void updateOverrideMode() {
		if (modeOverride)
			this.readoutMode.setValue(XspressDetector.READOUT_ROIS);
	}

	private Action createAdditiveResModeAction(){
		plotAction = new Action("Show resolution grades added", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				isAdditiveResolutionGradeMode = isChecked();
				plot(getDetectorList().getSelectedIndex(),false);
			}
		};
		plotAction.setImageDescriptor(ResourceManager.getImageDescriptor(XspressParametersUIEditor.class, "/icons/chart_line_add.png"));
		return plotAction;
	}
	
	protected void updateAdditiveMode() {
		plotAction.setEnabled(resolutionGradeCombo.getValue().equals(ResGrades.ALLGRADES));
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

	@Override
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
				return super.getChannelName(iChannel);
			}
		}
		return super.getChannelName(iChannel);
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
		boolean isRoi = readoutMode.getValue().equals(XspressDetector.READOUT_ROIS);
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
			updateOverrideMode();
			boolean readoutRois = false;
			if(resolutionGrade.getResolutionGradeCombo().getValue().equals(XspressDetector.READOUT_ROIS))
				readoutRois = true;
			resolutionGrade.updateResModeItems(readoutRois);
			updateRoiVisibility();
			updateElementsVisibility(parentComposite);
			updateAdditiveMode();
			updateResGradeVisibility(parentComposite);
			// notify the composite immediately of a possible change
			listEditorUI.notifySelected(getDetectorElementComposite().getRegionList());
			// setup for all future notifications
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
		return resolutionGradeCombo;
	}

	public BooleanWrapper getEditIndividualElements() {
		return showIndividualElements;
	}

	public ComboWrapper getReadoutMode() {
		return readoutMode;
	}

	@Override
	public void notifyFileSaved(File file) {
		FluorescenceComposite comp = (FluorescenceComposite) BeanUI.getBeanField("fluorescenceParameters", DetectorParameters.class);
		if (comp == null || comp.isDisposed())
			return;
		comp.getDetectorType().setValue("Germanium");
		comp.getConfigFileName().setValue(file.getAbsolutePath());
	}
	
	@Override
	protected double getDetectorCollectionTime() {
		return acquireTime.getNumericValue(); // convert to ms
	}

	@Override
	protected long getAcquireWaitTime() {
		return 1100l;
	}

	@SuppressWarnings("unchecked")
	public int _testGetNumberOfRegions() {
		return ((List<? extends Object>) this.getDetectorElementComposite().getRegionList().getValue()).size();
	}

	@Override
	public XMLCommandHandler getXMLCommandHandler() {
		return ExperimentBeanManager.INSTANCE.getXmlCommandHandler(XspressParameters.class);
	}

	@Override
	protected List<AbstractDataset> unpackDataSets(int ielement) {
		if (ielement < 0 || detectorData == null || !isAdditiveResolutionGradeMode || !resolutionGradeCombo.getValue().equals(ResGrades.ALLGRADES))
			return super.unpackDataSets(ielement);
		final List<AbstractDataset> ret = new ArrayList<AbstractDataset>(7);
		final int[][] elementData = detectorData[ielement];
		for (int resGrade = 0; resGrade < elementData.length; resGrade++) {
			AbstractDataset d = new DoubleDataset(Arrays.copyOf(elementData[resGrade],elementData[resGrade].length));
			if (!ret.isEmpty()) {
				AbstractDataset p = ret.get(resGrade - 1);
				d.iadd(p);
			}
			ret.add(d);
		}
		return ret;
	}

	@Override
	public void acquireStarted() {
	}

	@Override
	public void acquireFinished() {
	}

	public BooleanWrapper getOnlyShowFF() {
		return onlyShowFF;
	}

	public BooleanWrapper getShowDTRawValues() {
		return showDTRawValues;
	}

	public BooleanWrapper getSaveRawSpectrum() {
		return saveRawSpectrum;
	}

	@Override
	protected String getDataXMLName() {
		String varDir = LocalProperties.get(LocalProperties.GDA_VAR_DIR);
		return varDir + "/xspress_editor_data.xml";
	}
	
	public ComboWrapper getRegionType() {
		return regionType;
	}
	
	public String _testGetRegionName(final int index) {
		final List<XspressROI> rois = (List<XspressROI>) getDetectorElementComposite().getRegionList().getValue();
		return rois.get(index).getRoiName();
	}
	
}