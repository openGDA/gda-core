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

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.api.event.ValueListener;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.selector.ListEditor;
import org.eclipse.richbeans.widgets.selector.ListEditorUIAdapter;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
import org.eclipse.richbeans.widgets.wrappers.ComboAndNumberWrapper;
import org.eclipse.richbeans.widgets.wrappers.ComboWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.device.DeviceException;
import gda.factory.Finder;
import gda.jython.accesscontrol.AccessDeniedException;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.gda.beans.ElementCountsData;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.xspress.ResGrades;
import uk.ac.gda.beans.xspress.XspressDetector;
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
import uk.ac.gda.richbeans.editors.DirtyContainer;

/**
 *
 */
@Deprecated
public class XspressParametersUIEditor extends DetectorEditor {

	private static final String GDA_DEVICE_XSPRESS_SPOOL_DIR = "gda.device.xspress.spoolDir";

	private static final Logger logger = LoggerFactory.getLogger(XspressParametersUIEditor.class);
	private static final Map<String, Object> RES_ALL;
	private static final Map<String, Object> RES_NO_16;
	private boolean writeToDisk = LocalProperties.check("gda.detectors.save.single.acquire");
	// super mode override property to set the mode always to Scalers and MCA for the parameters file sent to the server
	// this flag will also be used to fix some of the gui display parameters for I18
	// readout mode will be set to regions of Interest for display, but will be sent as Scalers and MCa to the server
	// res grade will be fixed to none and not shown
	// readout type will not be shown
	private boolean modeOverride = LocalProperties.check("gda.xspress.mode.override");
	static {
		RES_ALL = new HashMap<String, Object>(3);
		RES_ALL.put("Sum all grades", ResGrades.NONE);
		RES_ALL.put("Individual grades", ResGrades.ALLGRADES);
		RES_ALL.put("Threshold", ResGrades.THRESHOLD);

		RES_NO_16 = new HashMap<String, Object>(3);
		RES_NO_16.put("Sum all grades", ResGrades.NONE);
		RES_NO_16.put("Threshold", ResGrades.THRESHOLD);
	}

	private ComboWrapper readoutMode;
	private ComboAndNumberWrapper resGrade;
	private XspressParameters xspressParameters;

	private Button applyToAllButton;
	private Button applyToAllLabel;

	private SelectionAdapter applyToAllListener;

	private Composite acquire;
	private ScaleBox acquireTime;
	private BooleanWrapper showIndividualElements;

	private Group detectorElementsGroup;
	private boolean isAdditiveResolutionGradeMode = false;
	private Action additiveResModeAction;
	private Label resGradeLabel;
	private Composite topComposite;
	private Composite parentComposite;
	private FileDialog openDialog;

	private Button autoSave;

	Label acquireFileLabel;

	private BooleanWrapper onlyShowFF;
	private BooleanWrapper showDTRawValues;
	private BooleanWrapper saveRawSpectrum;

	private SelectionAdapter xspressOptionsListener;
	private Label lblRegionBins;
	private ComboWrapper regionType;
	private ValueListener detectorElementCompositeValueListener;
	private Composite middleComposite;

	/**
	 * @param path
	 * @param mappingURL
	 * @param dirtyContainer
	 * @param editingBean
	 */
	public XspressParametersUIEditor(final String path, final URL mappingURL, final DirtyContainer dirtyContainer,
			final Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean, "xspressConfig");
		this.xspressParameters = (XspressParameters) editingBean;
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
	public void createPartControl(final Composite parent) {
		parentComposite = parent;

		super.createPartControl(parent);

		final Composite left = sashPlotForm.getLeft();

		topComposite = new Composite(left, SWT.NONE);
		topComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		topComposite.setLayout(gridLayout_1);

		final Label readoutModeLabel = new Label(topComposite, SWT.NONE);
		readoutModeLabel.setText("Read out mode");
		readoutModeLabel.setToolTipText("The type of data which will be written to file");
		readoutModeLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		this.readoutMode = new ComboWrapper(topComposite, SWT.READ_ONLY);
		readoutMode.setItems(new String[] { XspressDetector.READOUT_SCALERONLY, XspressDetector.READOUT_MCA,
				XspressDetector.READOUT_ROIS });
		readoutMode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		readoutMode.select(1);
		readoutMode.addValueListener(new ValueAdapter("readoutMode") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				GridUtils.startMultiLayout(parentComposite);
				try {
					updateOverrideMode();
					updateResModeItems();
					updateRoiVisibility();
					updateResGradeVisibility();
					configureUI();
				} finally {
					GridUtils.endMultiLayout();
				}
			}
		});

		resGradeLabel = new Label(topComposite, SWT.NONE);
		resGradeLabel.setText("Resolution Grade");
		resGradeLabel.setToolTipText("The resolution setting during calibration and XAS scans");
		resGradeLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		this.resGrade = new ComboAndNumberWrapper(topComposite, SWT.READ_ONLY,
				Arrays.asList(new String[] { ResGrades.THRESHOLD }));
		resGrade.setItems(RES_ALL);
		resGrade.getValueField().setMaximum(15.99);
		resGrade.getValueField().setMinimum(0.0);
		resGrade.getValueField().setDecimalPlaces(1);
		resGrade.getValueField().setNumericValue(1d);
		resGrade.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		resGrade.addValueListener(new ValueAdapter("resGrade") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateAdditiveMode();
			}
		});
		if (modeOverride) {
			GridUtils.setVisibleAndLayout(readoutModeLabel, false);
			GridUtils.setVisibleAndLayout(readoutMode, false);
			GridUtils.setVisibleAndLayout(resGradeLabel, false);
			GridUtils.setVisibleAndLayout(resGrade, false);
			GridUtils.setVisibleAndLayout(lblRegionBins, false);
			GridUtils.setVisibleAndLayout(regionType, false);
		}
		lblRegionBins = new Label(topComposite, SWT.NONE);
		lblRegionBins.setText("Region type");

		regionType = new ComboWrapper(topComposite, SWT.READ_ONLY);
		regionType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		regionType.setItems(new String[]{XspressParameters.VIRTUALSCALER, XspressROI.MCA});
		regionType.select(0);

		Group grpAcquire = new Group(left, SWT.NONE);
		grpAcquire.setText("Acquire Spectra");
		grpAcquire.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		grpAcquire.setLayout(gridLayout);

		Button loadBtn = new Button(grpAcquire, SWT.NONE);
		loadBtn.setImage(SWTResourceManager.getImage(DetectorEditor.class, "/icons/folder.png"));
		loadBtn.setText("Load Saved mca");
		loadBtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					LoadAcquireFromFile();
				} catch (Exception e1) {
					logger.error("Cannot acquire xspress data", e1);
				}
			}
		});

		acquire = new Composite(grpAcquire, SWT.NONE);
		final GridLayout gridLayoutAcq = new GridLayout();
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
					singleAcquire();
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
		autoSave.setSelection(writeToDisk);
		autoSave.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		autoSave.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				writeToDisk = autoSave.getSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		autoSave.setSelection(writeToDisk);

		acquireFileLabel = new Label(grpAcquire, SWT.NONE);
		acquireFileLabel.setText("										");
		acquireFileLabel.setToolTipText("The file path for the acquire data");
		acquireFileLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		openDialog = new FileDialog(parent.getShell(), SWT.OPEN);
		openDialog.setFilterPath(LocalProperties.get(LocalProperties.GDA_DATAWRITER_DIR));

		final Composite grid = new Composite(left, SWT.BORDER);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(grid);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(grid);

		this.showIndividualElements = new BooleanWrapper(grid, SWT.NONE);
		showIndividualElements.setText("Show individual elements");

		middleComposite = new Composite(grid, SWT.BORDER);
		middleComposite.setLayout(new GridLayout(2, false));
		GridDataFactory.fillDefaults()/*.grab(true, false)*/.applyTo(middleComposite);

		this.applyToAllLabel = new Button(middleComposite, SWT.CHECK);
		applyToAllLabel.setText("Apply Changes To All Elements ");
		applyToAllLabel.setEnabled(true);
		applyToAllLabel.setSelection(true);
		applyToAllLabel.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (applyToAllLabel.getSelection()) {
					if (applyToAll(true)) {
						if (detectorElementCompositeValueListener == null){
							createApplyToAllObserver();
						}
					} else {
						applyToAllLabel.setSelection(false);
					}
				}
				applyToAllButton.setEnabled(!applyToAllLabel.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		this.applyToAllButton = new Button(middleComposite, SWT.NONE);
		applyToAllButton.setEnabled(false);
		GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gridData.widthHint = 90;
		gridData.minimumWidth = 90;
		applyToAllButton.setLayoutData(gridData);
		applyToAllButton.setText("Apply now");
		applyToAllButton.setToolTipText("Apply current detector regions of interest to all other detector elements.");
		this.applyToAllListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				applyToAll(true);
			}
		};
		applyToAllButton.addSelectionListener(applyToAllListener);

		showIndividualElements.addValueListener(new ValueAdapter("editIndividualElements") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				if ((Boolean) e.getValue() == false) {
					if (!applyToAll(true)) {
						// user didn't want to lose individual settings, cancel change
						showIndividualElements.setValue(true);
						return;
					}
				}
				updateElementsVisibility();
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

		if (!ExafsActivator.getDefault().getPreferenceStore()
				.getBoolean(ExafsPreferenceConstants.DETECTOR_OUTPUT_IN_OUTPUT_PARAMETERS)) {
			addOutputPreferences(left);
		}

		sashPlotForm.setWeights(new int[] { 30, 74 });

		configureUI();

		createApplyToAllObserver();
	}

	protected void createApplyToAllObserver() {

		detectorElementCompositeValueListener = new ValueListener() {

			@Override
			public void valueChangePerformed(ValueEvent e) {
				if (applyToAllLabel.getSelection()) {
					applyToAll(false);
				}
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
		// Xspress not an option for I20 XESscans
		if (!ScanObjectManager.isXESOnlyMode()) {
			final Group xspressParametersGroup = new Group(comp, SWT.NONE);
			xspressParametersGroup.setText("Output Preferences");
			xspressParametersGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			GridLayout gridLayout = new GridLayout();
			gridLayout.numColumns = 1;
			xspressParametersGroup.setLayout(gridLayout);
			this.onlyShowFF = new BooleanWrapper(xspressParametersGroup, SWT.NONE);
			onlyShowFF.setText("Hide individual elements");
			onlyShowFF
					.setToolTipText("In ascii output, only display the total in-window counts (FF) from the Xspress detector");
			onlyShowFF.setValue(Boolean.FALSE);
			addXspressOptionsListener(onlyShowFF);
			this.showDTRawValues = new BooleanWrapper(xspressParametersGroup, SWT.NONE);
			showDTRawValues.setText("Show DT values");
			showDTRawValues
					.setToolTipText("Add the raw scaler values used in deadtime (DT) calculations to ascii output");
			showDTRawValues.setValue(Boolean.FALSE);
			addXspressOptionsListener(showDTRawValues);

			this.saveRawSpectrum = new BooleanWrapper(xspressParametersGroup, SWT.NONE);
			saveRawSpectrum.setText("Save raw spectrum to file");
			saveRawSpectrum.setValue(false);
		}
	}

	private void addXspressOptionsListener(BooleanWrapper booleanwrapper) {

		if (xspressOptionsListener == null) {
			createXspressOptionsListener();
		}
		// BooleanWrappers have Buttons as their control
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
						if (params.getExperimentType().equalsIgnoreCase(DetectorParameters.TRANSMISSION_TYPE)) {
							showWarning();
						} else if (params.getExperimentType().equalsIgnoreCase(DetectorParameters.FLUORESCENCE_TYPE)) {
							if (!params.getFluorescenceParameters().getDetectorType().equalsIgnoreCase("Germanium")) {
								showWarning();
							}
						}
					} catch (Exception e1) {
						logger.warn("Exception while retrieving current DetectorParameters object", e1);
					}
				}
			}

			protected void showWarning() {
				// popup warning that these parameters will have no effect
				getSite().getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog
								.openWarning(
										getSite().getShell(),
										"Inconsistent options",
										"The Xspress detector is not currently selected in the detector parameters editor.\n\nIf you wish to use the Xspress then change the detector parameters.");
					}
				});
			}
		};
	}

	private void updateResGradeVisibility() {
		GridUtils.startMultiLayout(parentComposite);
		try {
			if (readoutMode.getSelectionIndex() == 2 && !modeOverride) {
				GridUtils.setVisibleAndLayout(resGradeLabel, true);
				GridUtils.setVisibleAndLayout(resGrade, true);
				GridUtils.setVisibleAndLayout(lblRegionBins, true);
				GridUtils.setVisibleAndLayout(regionType, true);
			} else {
				GridUtils.setVisibleAndLayout(resGradeLabel, false);
				GridUtils.setVisibleAndLayout(resGrade, false);
				GridUtils.setVisibleAndLayout(lblRegionBins, false);
				GridUtils.setVisibleAndLayout(regionType, false);
			}
		} finally {
			GridUtils.endMultiLayout();
		}

	}

	protected void updateAdditiveMode() {
		additiveResModeAction.setEnabled(resGrade.getValue().equals(ResGrades.ALLGRADES));
	}

	private void updateOverrideMode() {
		if (modeOverride && !this.readoutMode.equals(XspressDetector.READOUT_ROIS)) {
			this.readoutMode.setValue(XspressDetector.READOUT_ROIS);
		}
	}

	/**
	 * Override to ass custom actions.
	 *
	 * @param parent
	 * @return SashFormPlotComposite
	 * @throws Exception
	 */
	@Override
	protected SashFormPlotComposite createSashPlot(Composite parent) throws Exception {
		this.additiveResModeAction = new Action("Show resolution grades added", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				isAdditiveResolutionGradeMode = isChecked();
				plot(getDetectorList().getSelectedIndex(),false);
			}
		};
		additiveResModeAction.setImageDescriptor(ResourceManager.getImageDescriptor(XspressParametersUIEditor.class,
				"/icons/chart_line_add.png"));
		return new SashFormPlotComposite(parent, this,new RegionSynchronizer(), additiveResModeAction,createUpLoadAction());
	}

	@Override
	protected java.awt.Color getChannelColor(int iChannel) {
		final String resGrade = getResGradeAllowingForReadoutMode();
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

		final String uiReadoutMode = (String) getReadoutMode().getValue();
		return uiReadoutMode.equals(XspressDetector.READOUT_ROIS) ? (String) getResGrade().getValue() : ResGrades.NONE;
	}

	@Override
	protected String getChannelName(int iChannel) {
		final String resGrade = getResGradeAllowingForReadoutMode();
		if (resGrade.equals(ResGrades.ALLGRADES)) {
			return "Resolution Grade " + (iChannel + 1);
		} else if (resGrade.startsWith(ResGrades.THRESHOLD)) {
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

	protected void updateElementsVisibility() {
		GridUtils.startMultiLayout(parentComposite);
		try {
			boolean currentEditIndividual = showIndividualElements.getValue();

			if (currentEditIndividual) {
				detectorElementsGroup.setText("Detector Elements");
			} else {
				detectorElementsGroup.setText("All Elements");
			}

			GridUtils.setVisibleAndLayout(middleComposite, currentEditIndividual);
			GridUtils.setVisibleAndLayout(applyToAllLabel, currentEditIndividual);
			GridUtils.setVisibleAndLayout(applyToAllButton, currentEditIndividual);
			GridUtils.setVisibleAndLayout(getDetectorElementComposite().getName(), currentEditIndividual);
			GridUtils.setVisibleAndLayout(getDetectorElementComposite().getExcluded(), currentEditIndividual);
			getDetectorElementComposite().setIndividualElements(currentEditIndividual);
			GridUtils.layoutFull(getDetectorElementComposite().getExcluded().getParent());
			getDetectorList().setListVisible(currentEditIndividual);
			autoApplyToAll(!currentEditIndividual);
			calculateAndPlotCountTotals(currentEditIndividual);
		} finally {
			GridUtils.endMultiLayout();
		}
	}

	protected void updateResModeItems() {

		final Object val = resGrade.getValue();
		if (readoutMode.getValue().equals(XspressDetector.READOUT_ROIS)) {
			resGrade.setItems(RES_ALL);
		} else {
			resGrade.setItems(RES_NO_16);
		}
		resGrade.setValue(val);
	}

	protected void updateRoiVisibility() {
		final boolean isRoi = readoutMode.getValue().equals(XspressDetector.READOUT_ROIS);
		getDetectorElementComposite().setWindowsEditable(!isRoi);
		final Composite roi = getDetectorElementComposite().getRegionList();
		GridUtils.setVisibleAndLayout(roi, isRoi);
		setImportCompositeVisible(isRoi);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		if (!(Boolean) showIndividualElements.getValue() || applyToAllLabel.getSelection()) {
			applyToAll(false);
		}
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
			updateResModeItems();
			updateRoiVisibility();
			updateElementsVisibility();
			updateAdditiveMode();
			updateResGradeVisibility();

			// notify the composite immediately of a possible change
			listEditorUI.notifySelected(getDetectorElementComposite().getRegionList());
			// setup for all future notifications
			getDetectorElementComposite().getRegionList().setListEditorUI(listEditorUI);

			updateROIAfterElementCompositeChange();

			sashPlotForm.getPlottingSystem().autoscaleAxes();
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
		return resGrade;
	}

	public BooleanWrapper getEditIndividualElements() {
		return showIndividualElements;
	}

	public ComboWrapper getReadoutMode() {
		return readoutMode;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void notifyFileSaved(File file) {
		final FluorescenceComposite comp = (FluorescenceComposite) controller.getBeanField("fluorescenceParameters",
				DetectorParameters.class);
		if (comp == null || comp.isDisposed())
			return;
		comp.getDetectorType().setValue("Germanium");
		comp.getConfigFileName().setValue(file.getAbsolutePath());
	}

	private String uiResGrade = null;
	private String uiReadoutMode = null;

	@Override
	protected void acquire(IProgressMonitor monitor, double collectionTime) {

		if (monitor != null)
			monitor.beginTask("Acquire xspress data", 100);

		// Get detector
		final XspressDetector xsDetector = Finder.getInstance().find(xspressParameters.getDetectorName());

		String resGrade_orig;
		String readoutMode_orig;
		try {
			resGrade_orig = xsDetector.getResGrade();
			readoutMode_orig = xsDetector.getReadoutMode();
		} catch (DeviceException e1) {
			logger.error("Cannot get current res grade", e1);
			return;
		}

		// Get res grade for calibration.
		getSite().getShell().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				uiReadoutMode = (String) getReadoutMode().getValue();
				uiResGrade = getResGradeAllowingForReadoutMode();
			}
		});

		sashPlotForm.appendStatus("Collecting a single frame of MCA data with resolution grade set to '" + uiResGrade
				+ "'.", logger);

		try {
			xsDetector.setAttribute("readoutModeForCalibration", new String[] { uiReadoutMode, uiResGrade });

			// Get MCA Data
			final int[][][] data = xsDetector.getMCData((int) collectionTime);
			// Int array above is [element][grade (1, 2 or all 16)][mca channel]

			getDataWrapper().setValue(ElementCountsData.getDataFor(data));
			this.dirtyContainer.setDirty(true);
			detectorData = getData(data);
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					calculateAndPlotCountTotals(showIndividualElements.getValue());
				}
			});

			if (writeToDisk) {
				String spoolDirPath;
				try {
					spoolDirPath = PathConstructor.createFromProperty(GDA_DEVICE_XSPRESS_SPOOL_DIR);
				} catch (Exception e) {
					spoolDirPath = PathConstructor.createFromDefaultProperty();
				}

				long snapShotNumber = new NumTracker("Xspress_snapshot").incrementNumber();
				String fileName = "xspress_snap_" + snapShotNumber + ".mca";
				final File filePath = new File(spoolDirPath + "/" + fileName);
				save(detectorData, filePath.getAbsolutePath());
				sashPlotForm.appendStatus("Xspress snapshot saved to " + filePath, logger);
				getSite().getShell().getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						acquireFileLabel.setText("Saved: " + filePath.getAbsolutePath());
					}
				});
			}

			if (monitor != null)
				monitor.done();
			sashPlotForm.appendStatus("Collected data from detector successfully.", logger);

		} catch (IllegalArgumentException e) {
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog
							.openWarning(getSite().getShell(), "Cannot write out detector data",
									"The Java property gda.device.xspress.spoolDir has not been defined or is invalid. Contact Data Acquisition.");
				}
			});
			logger.error("Cannot read out detector data.", e);
			return;
		} catch (AccessDeniedException e) {
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openWarning(getSite().getShell(), "Cannot operate detector",
							"You do not hold the baton and so cannot operate the detector.");
				}
			});
			sashPlotForm
					.appendStatus("Cannot read out detector data. Check the log and inform beamline staff.", logger);
			return;
		} catch (Exception e) {
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openWarning(getSite().getShell(), "Cannot read out detector data",
							"Problem acquiring data. See log for details.");
				}
			});
			sashPlotForm
					.appendStatus("Cannot read out detector data. Check the log and inform beamline staff.", logger);
			return;
		} finally {
			try {
				xsDetector.setResGrade(resGrade_orig);
				xsDetector.setReadoutMode(readoutMode_orig);
			} catch (DeviceException e) {
				sashPlotForm.appendStatus("Cannot reset res grade, detector may be in an error state.", logger);
			}
			sashPlotForm.appendStatus("Reset detector to resolution grade '" + resGrade_orig + "'.", logger);
		}

		// Note: currently has to be in this order.
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				getDetectorElementComposite().setEndMaximum((detectorData[0][0].length) - 1);
				plot(getDetectorList().getSelectedIndex(),true);
				setEnabled(true);
			}
		});
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
	protected Logger getLogger() {
		return logger;
	}

	@Override
	protected List<Dataset> unpackDataSets(int ielement) {

		if (ielement < 0 || detectorData == null || !isAdditiveResolutionGradeMode
				|| !resGrade.getValue().equals(ResGrades.ALLGRADES)) {
			return super.unpackDataSets(ielement);
		}

		// We are ResGrades.ALLGRADES and isAdditiveResolutionGradeMode, so we add them.
		final List<Dataset> ret = new ArrayList<Dataset>(7);
		final double[][] elementData = detectorData[ielement];
		for (int resGrade = 0; resGrade < elementData.length; resGrade++) {
			// must pass by value as we are going to do some maths on it!!!
			Dataset d = DatasetFactory.createFromObject(Arrays.copyOf(elementData[resGrade],elementData[resGrade].length));
			if (!ret.isEmpty()) {
				final Dataset p = ret.get(resGrade - 1);
				d.iadd(p);
			}
			ret.add(d);
		}
		return ret;
	}

	@Override
	protected void LoadAcquireFromFile() {
		String dataDir = PathConstructor.createFromDefaultProperty();
		dataDir += "processing";
		if (openDialog.getFilterPath() == null){
			openDialog.setFilterPath(dataDir);
		}
		final String filePath = openDialog.open();
		if (filePath != null) {
			final String msg = ("Loading map from " + filePath);
			Job job = new Job(msg) {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					BufferedReader reader = null;
					try {

						reader = new BufferedReader(new FileReader(filePath));
						String line = reader.readLine();
						ArrayList<double[]> data = new ArrayList<double[]>();
						while (line != null) {
							StringTokenizer tokens = new StringTokenizer(line);
							double elementData[] = new double[tokens.countTokens()];
							for (int i = 0; i < elementData.length; i++) {
								elementData[i] = Double.parseDouble(tokens.nextToken());
							}
							data.add(elementData);
							line = reader.readLine();
						}
						// find the res grade

						int resGrade = data.size() / xspressParameters.getDetectorList().size();
						detectorData = new double[xspressParameters.getDetectorList().size()][resGrade][];
						int dataIndex = 0;
						// Int array above is [element][grade (1, 2 or all 16)][mca channel]
						for (int i = 0; i < detectorData.length; i++) {
							for (int j = 0; j < resGrade; j++) {
								detectorData[i][j] = data.get(dataIndex++);
							}
						}

						getSite().getShell().getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								acquireFileLabel.setText("Loaded: " + filePath);
								getDetectorElementComposite().setEndMaximum((detectorData[0][0].length) - 1);
								plot(getDetectorList().getSelectedIndex(),true);
								setEnabled(true);
							}
						});

					} catch (Exception e) {
						logger.warn("Exception while reading data from xspress parameters xml file", e);
					} finally {
						if (reader != null) {
							try {
								reader.close();
							} catch (IOException e) {
								logger.warn("Exception while reading data from xspress parameters xml file", e);
							}
						}

					}

					return Status.OK_STATUS;
				}

			};
			job.setUser(true);
			job.schedule();
		}

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
}
