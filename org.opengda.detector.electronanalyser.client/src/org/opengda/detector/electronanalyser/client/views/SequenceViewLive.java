/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package org.opengda.detector.electronanalyser.client.views;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;
import org.opengda.detector.electronanalyser.client.SESPerspective;
import org.opengda.detector.electronanalyser.client.actions.SequenceViewLiveEnableToolbarSourceProvider;
import org.opengda.detector.electronanalyser.client.selection.CanEditRegionSelection;
import org.opengda.detector.electronanalyser.client.selection.EnergyChangedSelection;
import org.opengda.detector.electronanalyser.client.selection.RegionRunCompletedSelection;
import org.opengda.detector.electronanalyser.client.selection.TotalTimeSelection;
import org.opengda.detector.electronanalyser.client.sequenceeditor.AnimationHandler;
import org.opengda.detector.electronanalyser.client.sequenceeditor.AnimationUpdate;
import org.opengda.detector.electronanalyser.client.sequenceeditor.IRegionDefinitionView;
import org.opengda.detector.electronanalyser.event.RegionChangeEvent;
import org.opengda.detector.electronanalyser.event.RegionStatusEvent;
import org.opengda.detector.electronanalyser.event.ScanEndEvent;
import org.opengda.detector.electronanalyser.event.ScanPointStartEvent;
import org.opengda.detector.electronanalyser.event.ScanStartEvent;
import org.opengda.detector.electronanalyser.event.SequenceFileChangeEvent;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUISITION_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.STATUS;
import org.opengda.detector.electronanalyser.utils.RegionStepsTimeEstimation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.device.DeviceException;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController.MonitorType;
import gda.epics.connection.InitializationListener;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.jython.batoncontrol.BatonChanged;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObserver;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.gda.devices.vgscienta.IVGScientaAnalyserRMI;


@SuppressWarnings("restriction")
public class SequenceViewLive extends SequenceViewCreator implements ISelectionProvider, IRegionDefinitionView, ISaveablePart, IObserver, InitializationListener {

	public static final String ID = "org.opengda.detector.electronanalyser.client.sequenceeditor";
	private static final Logger logger = LoggerFactory.getLogger(SequenceViewLive.class);

	private Group grpElementset;
	private Text txtElementSet;
	private Text txtEstimatedTime;
	private Text txtNumberActives;
	private StyledText txtDataFilePath;
	private Text txtPointValue;
	private Text txtRegionValue;
	private Text txtTimeRemaining;
	private Text txtScanNumberValue;
	private ProgressBar progressBar;
	private Future<?> analyserScanProgressUpdates;

	private Text txtSequenceFileEditingStatus;
	private static final String LOCKED_DURING_SCAN = "Locked - A scan is running.";
	private static final String BATON_NOT_HELD = "Locked - You do not hold the baton.";
	private static final String EDITABLE = "Editable - You hold the baton.";


	private double currentregiontimeremaining;
	private volatile double totalScanTime;
	private volatile double time4ScanPointsCompleted;
	private int currentPointNumber;
	private int totalNumberOfPoints;
	private int crrentRegionNumber;
	private double totalSequenceTimes = 0.0;
	private int numActives = 0;

	private String analyserStatePV;
	private String analyserTotalTimeRemianingPV;

	private EpicsChannelManager channelmanager;

	@SuppressWarnings("unused")
	private Channel analyserStateChannel;
	@SuppressWarnings("unused")
	private Channel analyserTotalTimeRemainingChannel;

	private boolean first = true;

	private AnalyserStateListener analyserStateListener;
	private AnalyserTotalTimeRemainingListener analyserTotalTimeRemainingListener;

	private IVGScientaAnalyserRMI analyser;
	private Scriptcontroller scriptcontroller;

	private List<Region> regionsCompleted = new ArrayList<>();

	private boolean disableSequenceEditingDuringAnalyserScan = true;

	private boolean scanRunning = false;
	private boolean hasBatonCached = true;

	private Future<?> elementSetConnected;
	private Runnable elementSetMonitor = () -> {

		if (txtElementSet.isDisposed()) {
			elementSetConnected.cancel(true);
			return;
		}
		try {
			final String liveElementSetMode = getAnalyser().getPsuMode();

			txtElementSet.getDisplay().asyncExec(() -> {
				final String currentElementSet = sequence.getElementSet();
				String uiElementSet = txtElementSet.getText();
				if (!currentElementSet.equals(liveElementSetMode)) {
					//Set elementSet on sequence only. Do not add to command stack using addCommandToGroupToUpdateFeature(...) otherwise this can be removed by undo command.
					//ElementSet is determined by EPICS value.
					if (!sequence.getElementSet().equals(liveElementSetMode)) {
						sequence.setElementSet(liveElementSetMode);
					}
					txtElementSet.setText(liveElementSetMode);
					logger.info("Detected change in elementSet. Changing from {} to {}", currentElementSet, liveElementSetMode);
					validateAllRegions();
				}
				else if (!uiElementSet.equals(liveElementSetMode)) {
					txtElementSet.setText(liveElementSetMode);
				}
			});
		}
		catch (Exception e) {
			logger.error("Unable to check electron analyser element set value.", e);
			//Prevent the log being spammed with the same error message
			elementSetConnected.cancel(true);
			elementSetDisconnectMessage();
		}
	};

	private void elementSetDisconnectMessage() {
		String errorText = "Can't connect to electron analyser to check element set value. Unable to validate regions. Check the connection and then restart server and client.";
		txtElementSet.getDisplay().asyncExec(() -> {
			txtElementSet.setText("UNKNOWN");
			txtElementSet.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
			txtElementSet.setToolTipText(errorText);
			sequence.setElementSet(errorText);
			openMessageBox("Element set UNKNOWN", errorText, SWT.ICON_ERROR);
		});
	}


	@Override
	protected void selectionListenerDetectedUpdate(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof TotalTimeSelection) {
			updateCalculatedData();
		} else if (selection instanceof EnergyChangedSelection energyChangeSelection) {
			boolean isFromExcitationEnergyChange = energyChangeSelection.isExcitationEnergyChange();

			for (Region region : energyChangeSelection.getRegions()) {
				boolean valid;
				if (region.isEnabled()) {
					//Make pop up appear only if not from excitation energy change
					valid = isFromExcitationEnergyChange ? isValidRegion(region, false) : isValidRegion(region, true);
				}
				else {
					valid = isValidRegion(region, false);
				}

				if (!valid && !isFromExcitationEnergyChange) {
					try {
						addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_Enabled(), valid, region.isEnabled());
						executeCommand(groupCommand);
					} catch (Exception e) {
						logger.error("Unable to update status and show popup", e);
					}
				}
			}
			updateCalculatedData();
		} else if (selection instanceof IStructuredSelection sel) {
			Object firstElement = sel.getFirstElement();
			if (firstElement instanceof Region region) {
				sequenceTableViewer.refresh(region);
				sequenceTableViewer.setSelection(sel);
			}
		}
	}

	public SequenceViewLive() {
		setTitleToolTip("Create a new or edit an existing sequence");
		setPartName("Sequence Editor");
		this.selectionChangedListeners = new ArrayList<>();

		setRegionViewID(RegionViewLive.ID);
		setCanEnableInvalidRegions(false);
		setShowInvalidDialogOnSave(false);
	}

	@Override
	public void createPartControl(final Composite parent) {
		int numberOfColumns = 3;
		Composite rootComposite = createRootComposite(parent);
		createSequenceTableArea(rootComposite);
		Composite controlArea = createControlArea(rootComposite, numberOfColumns);

		createSequenceFile(controlArea, numberOfColumns);
		createSequenceFileEditingControl(controlArea, numberOfColumns + 1);

		createElementSet(controlArea);
		createTotalSequenceTime(controlArea);
		createNumberOfActiveRegions(controlArea);
		createDataFile(controlArea, numberOfColumns);
		createAnalyserScanProgress(controlArea, numberOfColumns);

		controlArea.addControlListener(new ControlListener() {
			//Adjust number of columns if can't fit on one line
			@Override
			public void controlResized(ControlEvent e) {
				int columns = numberOfColumns;
				GridData gridDataElementSet = new GridData();
				gridDataElementSet.horizontalAlignment = GridData.FILL;
				gridDataElementSet.horizontalSpan = 1;
				gridDataElementSet.grabExcessHorizontalSpace = true;
				int width = controlArea.getSize().x;
				if (width < 320){
					columns = 1;
				}
				else if (width < 420) {
					columns = 2;
					gridDataElementSet.horizontalSpan = 2;
				}
				controlArea.setLayout(new GridLayout(columns, false));
				grpElementset.setLayoutData(gridDataElementSet);
			}
			@Override
			public void controlMoved(ControlEvent e) {

			}
		});
		registerSelectionProviderAndCreateHelpContext();
	}

	private void createSequenceFileEditingControl(Composite controlArea, int horizontalSpan) {
		Group grpSequenceFile = new Group(controlArea, SWT.None);
		GridDataFactory.fillDefaults().grab(true, false).span(horizontalSpan, 1).applyTo(grpSequenceFile);
		grpSequenceFile.setText("Editing control");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = horizontalSpan;
		grpSequenceFile.setLayout(gridLayout);
		grpSequenceFile.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		updateBatonHolder();

		/* Composite to contain the status composite so that a border can be displayed. */
		Composite borderComposite = createColourControl(grpSequenceFile);

		txtSequenceFileEditingStatus = new Text(grpSequenceFile, SWT.NONE);
		txtSequenceFileEditingStatus.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtSequenceFileEditingStatus.setEditable(false);
		txtSequenceFileEditingStatus.setText(EDITABLE);

		txtSequenceFileEditingStatus.addListener(SWT.Selection, (e) -> {
			if (txtSequenceFileEditingStatus.getText().equals(EDITABLE)) {
				borderComposite.setBackground(borderComposite.getDisplay().getSystemColor(SWT.COLOR_GREEN));
			}
			else {
				borderComposite.setBackground(borderComposite.getDisplay().getSystemColor(SWT.COLOR_RED));
			}
		});

		Button requestbaton = new Button(grpSequenceFile, SWT.PUSH);
		requestbaton.setText("Request Baton");
		requestbaton.setLayoutData(new GridData());
		requestbaton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				InterfaceProvider.getBatonStateProvider().requestBaton();
			}
		});

		txtSequenceFileEditingStatus.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				if (txtSequenceFileEditingStatus.getText().equals(EDITABLE)) {
					borderComposite.setBackground(borderComposite.getDisplay().getSystemColor(SWT.COLOR_GREEN));
					requestbaton.setEnabled(false);
				}
				else {
					borderComposite.setBackground(borderComposite.getDisplay().getSystemColor(SWT.COLOR_RED));

					if (hasBaton() && scanRunning) {
						requestbaton.setEnabled(false);
					}
					else {
						requestbaton.setEnabled(true);
					}
				}
			}
		});

		InterfaceProvider.getBatonStateProvider().addBatonChangedObserver(this);

		controlArea.addControlListener(new ControlListener() {
			//Adjust number of columns if can't fit on one line
			@Override
			public void controlResized(ControlEvent e) {
				GridData gridDataBaton = (GridData) requestbaton.getLayoutData();

				gridDataBaton.horizontalAlignment = GridData.FILL;

				int width = controlArea.getSize().x;
				if (width < 420){
					gridDataBaton.horizontalSpan = horizontalSpan;
					gridDataBaton.grabExcessHorizontalSpace = false;
				}
				else {
					gridDataBaton.horizontalSpan = 1;
					gridDataBaton.grabExcessHorizontalSpace = true;
				}
				requestbaton.requestLayout();
			}
			@Override
			public void controlMoved(ControlEvent e) {
			}
		});
	}

	private Composite createColourControl(Composite parent) {
		/* Composite to contain the status composite so that a border can be displayed. */
		Composite borderComposite = new Composite(parent, SWT.NONE);
		borderComposite.setBackground(borderComposite.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		FillLayout fillLayout = new FillLayout();
		fillLayout.marginWidth = 2;
		fillLayout.marginHeight = 2;
		borderComposite.setLayout(fillLayout);
		GridDataFactory.fillDefaults().indent(3, 0).hint(20, 20).applyTo(borderComposite);

		return borderComposite;
	}


	private void updateBatonHolder() {
		Display.getDefault().asyncExec(() -> {
			boolean currentHasBatonCache = hasBatonCached;
			String message = !hasBatonCached ? BATON_NOT_HELD : EDITABLE;
			if (getDisableSequenceEditingDuringAnalyserScan()) {
				if (scanRunning) {
					message = LOCKED_DURING_SCAN;
				}
			}
			enableSequenceEditorAndToolbar(hasBaton() && !scanRunning);
			txtSequenceFileEditingStatus.setText(message);

			String perspectiveID = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective().getId();
			if (currentHasBatonCache && !hasBaton() && perspectiveID.equals(SESPerspective.ID)) {
				openMessageBox("Baton changed", "You're not holding the baton and therefore can no longer edit the sequence file.", SWT.ICON_WARNING);
			}
		});
	}

	private boolean hasBaton() {
		hasBatonCached = InterfaceProvider.getBatonStateProvider().getMyDetails().hasBaton();
		return hasBatonCached;
	}

	@Override
	protected void createElementSet(Composite controlArea) {
		grpElementset = new Group(controlArea, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(grpElementset);
		grpElementset.setLayout(new GridLayout());
		grpElementset.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpElementset.setText("Element Set");
		grpElementset.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		txtElementSet = new Text(grpElementset, SWT.NONE | SWT.RIGHT);
		txtElementSet.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtElementSet.setEditable(false);
		txtElementSet.setText("Low");
	}

	private void createTotalSequenceTime(Composite controlArea) {
		Group grpTotalTime = new Group(controlArea, SWT.None);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(grpTotalTime);
		grpTotalTime.setText("Total Sequence Time");
		grpTotalTime.setLayout(new GridLayout());
		grpTotalTime.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		txtEstimatedTime = new Text(grpTotalTime, SWT.NONE | SWT.RIGHT);
		txtEstimatedTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtEstimatedTime.setEditable(false);
	}

	private void createNumberOfActiveRegions(Composite controlArea) {
		Group grpActiveRegions = new Group(controlArea, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(grpActiveRegions);
		grpActiveRegions.setText("Number of Active Regions");
		grpActiveRegions.setLayout(new GridLayout());
		grpActiveRegions.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		txtNumberActives = new Text(grpActiveRegions, SWT.NONE | SWT.RIGHT);
		txtNumberActives.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtNumberActives.setEditable(false);
	}

	private void createDataFile(Composite controlArea, int horizontalSpan) {
		Group grpDataFile = new Group(controlArea, SWT.None);
		GridDataFactory.fillDefaults().grab(true, false).span(horizontalSpan, 1).applyTo(grpDataFile);
		grpDataFile.setText("Data File");
		grpDataFile.setLayout(new GridLayout());
		grpDataFile.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		txtDataFilePath = new StyledText(grpDataFile, SWT.NONE | SWT.READ_ONLY | SWT.H_SCROLL);
		txtDataFilePath.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		txtDataFilePath.setEditable(false);
		txtDataFilePath.setText("Data file to be collected");

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		//adjust size to be slightly bigger to allow room for vertical scrollbar
		gridData.heightHint = (int) Math.ceil(txtSequenceFilePath.getLineHeight() * 1.25) ;
		txtDataFilePath.setLayoutData(gridData);
	}

	private void createAnalyserScanProgress(Composite controlArea, int horizontalSpan) {

		Group grpScanProgress = new Group(controlArea, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(horizontalSpan, 1).applyTo(grpScanProgress);
		grpScanProgress.setText("Analyser Scan Progress");
		grpScanProgress.setLayout(new GridLayout(4, false));
		grpScanProgress.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		Label lblScanNumber = new Label(grpScanProgress, SWT.None);
		lblScanNumber.setText("Current Scan Number: ");

		txtScanNumberValue = new Text(grpScanProgress, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).hint(50, SWT.DEFAULT).applyTo(txtScanNumberValue);
		txtScanNumberValue.setEditable(false);
		try {
			txtScanNumberValue.setText(String.format("%d", new NumTracker(LocalProperties.GDA_BEAMLINE_NAME).getCurrentFileNumber()));
		} catch (IOException e) {
			logger.warn("Failed to get scan number tracker");
		}

		Label lblPoint = new Label(grpScanProgress, SWT.None);
		lblPoint.setText("Scan Point Number: ");

		txtPointValue = new Text(grpScanProgress, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).hint(50, SWT.DEFAULT).applyTo(txtPointValue);
		txtPointValue.setEditable(false);
		updateScanPointNumber(currentPointNumber, totalNumberOfPoints);

		Label lblRegion = new Label(grpScanProgress, SWT.NONE);
		lblRegion.setText("Active Region Number:");

		txtRegionValue = new Text(grpScanProgress, SWT.BORDER);
		txtRegionValue.setEditable(false);
		txtRegionValue.setText("0");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).hint(50, SWT.DEFAULT).applyTo(txtRegionValue);

		Label lblTimeRemaining = new Label(grpScanProgress, SWT.NONE);
		lblTimeRemaining.setText("Time Remaining:");

		txtTimeRemaining = new Text(grpScanProgress, SWT.BORDER);
		txtTimeRemaining.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).hint(50, SWT.DEFAULT).applyTo(txtTimeRemaining);
		txtTimeRemaining.setEditable(false);

		Label lblProgress = new Label(grpScanProgress, SWT.NONE);
		lblProgress.setText("Scan Progress:");

		progressBar = new ProgressBar(grpScanProgress, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().span(horizontalSpan, 1).applyTo(progressBar);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
	}

	@Override
	protected void initialisation() {
		super.initialisation();

		elementSetConnected = Async.scheduleAtFixedRate(elementSetMonitor, 0, 2, TimeUnit.SECONDS);

		// server event admin or handler
		scriptcontroller = Finder.find("SequenceFileObserver");
		scriptcontroller.addIObserver(this);
		// EPICS monitor to update current region status
		channelmanager = new EpicsChannelManager(this);
		analyserStateListener = new AnalyserStateListener();
		analyserTotalTimeRemainingListener = new AnalyserTotalTimeRemainingListener();

		try {
			createChannels();
		} catch (CAException e1) {
			logger.error("failed to create required spectrum channels", e1);
		}
		updateRegionNumber(crrentRegionNumber, numActives);
		checkIfScanIsRunningAndPeformSetup();
	}

	private void checkIfScanIsRunningAndPeformSetup() {

		final String ANALYSER = "ew4000";
		boolean checkCanEdit = true;

		//Check if analyserscan is running
		if (Boolean.parseBoolean(InterfaceProvider.getCommandRunner().evaluateCommand(ANALYSER + ".isBusy()"))) {

			scanRunning = true;

			//If running, we need to update sequence file to one running on server
			String sequenceFileName = InterfaceProvider.getCommandRunner().evaluateCommand(ANALYSER + ".getSequenceFilename()");

			if (!regionDefinitionResourceUtil.getFileName().equals(sequenceFileName)) {
				refreshTable(sequenceFileName, false);
			}
			checkCanEdit = false;

			//Sync the GUI to show the current region running on server and the already completed regions
			String currentRegionId = InterfaceProvider.getCommandRunner().evaluateCommand(ANALYSER + ".getCurrentRegion().getRegionId()");
			Optional<Region> filteredRegions = regions.stream().filter(r -> r.getRegionId().equals(currentRegionId)).findFirst();
			if (filteredRegions.isPresent()) {
				Region serverCurrentRegion = filteredRegions.get();
				for (Region r : regions) {
					if (r == serverCurrentRegion) {
						updateRegionStatus(serverCurrentRegion, STATUS.RUNNING);
						currentRegion = serverCurrentRegion;
						fireSelectionChanged(currentRegion);
						break;
					}
					else if (r.isEnabled()){
						updateRegionStatus(r, STATUS.COMPLETED);
					}
				}
			}
		}
		if (getDisableSequenceEditingDuringAnalyserScan()) {
			canEdit = checkCanEdit;
		}
		else {
			canEdit = true;
		}
		enableSequenceEditorAndToolbar(canEdit && hasBaton());
	}


	protected void setShutterState(Composite shutterState, int status) {
		setColourControl(shutterState, status, SWT.COLOR_DARK_GREEN, SWT.COLOR_RED);
	}

	protected void setColourControl(final Control control, final int statusInt, final int openColour, final int closeColour) {
		if (control != null && !control.isDisposed()) {
			control.getDisplay().asyncExec(() -> {
				if (!control.isDisposed()) {
					if (statusInt == 0) {
						control.setBackground(control.getDisplay().getSystemColor(openColour));
					} else if (statusInt == 1) {
						control.setBackground(control.getDisplay().getSystemColor(closeColour));
					}
				}
			});
		}
	}

	protected void updateRegionNumber(int currentRegionNumber, int totalActiveRegions) {
		txtRegionValue.setText(String.valueOf(currentRegionNumber) + '/' + String.valueOf(totalActiveRegions));
	}

	protected void updateScanPointNumber(int currentPointNumber, int totalNumberOfPoints) {
		txtPointValue.setText(String.valueOf(currentPointNumber) + '/' + String.valueOf(totalNumberOfPoints));
	}

	protected void updateRegionStatus(final Region region, final STATUS status) {
		if (region.getStatus() == STATUS.RUNNING && status != STATUS.RUNNING) {
			stopRunningAnimation();
		}
		else if (status == STATUS.RUNNING) {
			startRunningAnimation();
		}
		else if (status == STATUS.COMPLETED && !regionsCompleted.contains(region)) {
			regionsCompleted.add(region);
		}

		getViewSite().getShell().getDisplay().asyncExec(() -> {
				region.setStatus(status);
				sequenceTableViewer.refresh(region);
		});
	}

	protected void createChannels() throws CAException {
		first = true;
		if (getDetectorStatePV() != null) {
			analyserStateChannel = channelmanager.createChannel(getDetectorStatePV(), analyserStateListener, MonitorType.NATIVE, false);
		}
		if (getAnalyserTotalTimeRemianingPV() != null) {
			analyserTotalTimeRemainingChannel = channelmanager.createChannel(getAnalyserTotalTimeRemianingPV(), analyserTotalTimeRemainingListener,
					MonitorType.NATIVE, false);
		}

		channelmanager.creationPhaseCompleted();
		logger.debug("analyser state channel and monitor are created");
	}

	@Override
	protected void updateCalculatedData() {

		double newTotalTimesValue = 0.0;
		int newNumActivesValue = 0;
		if (!regions.isEmpty()) {
			for (Region region : regions) {
				if (region.isEnabled()) {
					newNumActivesValue++;
					if (region.getAcquisitionMode() == ACQUISITION_MODE.SWEPT) {
						newTotalTimesValue += region.getStepTime() *region.getRunMode().getNumIterations()
								* RegionStepsTimeEstimation.calculateTotalSteps((region.getHighEnergy() - region.getLowEnergy()), region.getEnergyStep(),
										camera.getEnergyResolution() * region.getPassEnergy() * (region.getLastXChannel() - region.getFirstXChannel() + 1));
					} else if (region.getAcquisitionMode() == ACQUISITION_MODE.FIXED) {
						newTotalTimesValue += region.getStepTime() *region.getRunMode().getNumIterations() * 1;
					}
				}
			}
		}
		txtNumberActives.setText(String.format("%d", newNumActivesValue));
		txtEstimatedTime.setText(String.format("%.3f", newTotalTimesValue));
		totalSequenceTimes = newTotalTimesValue;
		numActives = newNumActivesValue;
	}

	/**
	 * refresh the table viewer with the sequence file name provided. If it is a new file, an empty sequence will be created.
	 */
	@Override
	public void refreshTable(String seqFileName, boolean newFile) {
		super.refreshTable(seqFileName, newFile);
		if(elementSetConnected.isCancelled()) {
			elementSetDisconnectMessage();
		}
		else {
			Thread thread = new Thread(elementSetMonitor);
			thread.start();
		}

		checkIfScanIsRunningAndPeformSetup();
	}

	@Override
	public void dispose() {
		scriptcontroller.deleteIObserver(this);
		InterfaceProvider.getBatonStateProvider().deleteBatonChangedObserver(this);
		super.dispose();
	}

	@Override
	public void update(Object source, final Object arg) {
		if (source == scriptcontroller) {
			handleEvent(arg);
		}

		if (arg instanceof BatonChanged) {
			logger.warn("baton has changed");

			Display.getDefault().asyncExec(() -> {
				refreshTable(txtSequenceFilePath.getText().trim(), false);
			});
			updateBatonHolder();
		}
	}

	//Events received from server
	protected void handleEvent(Object event) {
		if (event instanceof SequenceFileChangeEvent sequenceFileChangeEvent) {
			Display.getDefault().asyncExec(() -> handleSequenceFileChange(sequenceFileChangeEvent));
		}
		else if (event instanceof RegionChangeEvent regionChangeEvent) {
			Display.getDefault().asyncExec(() -> handleRegionChange(regionChangeEvent));
		}
		else if (event instanceof RegionStatusEvent regionStatusEvent) {
			handleRegionStatusChange(regionStatusEvent);
		}
		else if (event instanceof ScanStartEvent scanStartEvent) {
			handleScanStart(scanStartEvent);
		}
		else if (event instanceof ScanPointStartEvent scanPointStartEvent) {
			handleScanPointStart(scanPointStartEvent);
		}
		else if (event instanceof ScanEndEvent) {
			handleScanEnd();
		}
	}

	protected void handleSequenceFileChange(SequenceFileChangeEvent changeEvent) {
		logger.debug("Sequence file changed to {}", changeEvent.getFilename());
		refreshTable(changeEvent.getFilename(), false);
	}

	protected void handleRegionChange(RegionChangeEvent event) {
		logger.debug("region update to {}", event.getRegionName());
		String regionId = event.getRegionId();
		for (Region region : regions) {
			if (region.getRegionId().equalsIgnoreCase(regionId)) {
				currentRegion = region;
			}
		}
		fireSelectionChanged(currentRegion);
		sequenceTableViewer.setSelection(new StructuredSelection(currentRegion));
	}

	protected void handleRegionStatusChange(RegionStatusEvent event) {
		final String regionId = event.getRegionId();
		final STATUS status = event.getStatus();
		currentRegionNumber = event.getRegionNumber();

		for (Region region : regions) {
			if (region.getRegionId().equalsIgnoreCase(regionId)) {
				logger.info("Updating status of region {} from {} to {}", region.getName(), region.getStatus(), status);
				break;
			}
		}
		Display.getDefault().asyncExec(() -> {
			updateRegionNumber(currentRegionNumber, numActives);
			logger.debug("region {} update to {}", regionId, status);
			for (Region region : regions) {
				if (region.getRegionId().equalsIgnoreCase(regionId)) {
					updateRegionStatus(region, status);
				}
			}

			if (status == STATUS.COMPLETED) {
				fireSelectionChanged(new RegionRunCompletedSelection());
			}
		});
	}

	protected void handleScanStart(ScanStartEvent event) {

		scanRunning = true;

		if (getDisableSequenceEditingDuringAnalyserScan()) {
			enableSequenceEditorAndToolbar(false);
			Display.getDefault().asyncExec(() -> txtSequenceFileEditingStatus.setText(LOCKED_DURING_SCAN));
		}

		resetCurrentRegion();

		totalNumberOfPoints = event.getNumberOfPoints();
		final String scanFilename = event.getScanFilename();
		final int scanNumber = event.getScanNumber();
		totalScanTime = totalNumberOfPoints * totalSequenceTimes;

		Display.getDefault().asyncExec(() -> {
			updateScanPointNumber(currentPointNumber, totalNumberOfPoints);
			txtDataFilePath.setText(scanFilename);
			txtScanNumberValue.setText(String.valueOf(scanNumber));
			txtTimeRemaining.setText(String.format("%.3f", totalScanTime));
		});
		time4ScanPointsCompleted=0.0;
		time4RegionsCompletedInCurrentPoint=0.0;

		analyserScanProgressUpdates = Async.scheduleAtFixedRate(
			new TimerTask() {
				@Override
				public void run() {
					Display.getDefault().asyncExec(() -> {
						double scanTimeRemaining = totalScanTime - time4ScanPointsCompleted - getCompletedRegionsTimeTotal(regionsCompleted) - currentRegion.getTotalTime() + currentregiontimeremaining;
						if (scanTimeRemaining < 1) {
							scanTimeRemaining = 0;
						}
						if (scanTimeRemaining < Double.valueOf(txtTimeRemaining.getText().trim())) {
							txtTimeRemaining.setText(String.format("%.3f", scanTimeRemaining));
						}
						progressBar.setSelection((int) (100 * ((totalScanTime - scanTimeRemaining) / totalScanTime)));
					});
				}
			},
			1000,
			1000,
			TimeUnit.MILLISECONDS
		);
	}

	protected void handleScanPointStart(ScanPointStartEvent event) {
		regionsCompleted.clear();
		currentPointNumber = event.getCurrentPointNumber();
		time4ScanPointsCompleted = (currentPointNumber-1) * totalSequenceTimes;
		Display.getDefault().asyncExec(() -> updateScanPointNumber(currentPointNumber, totalNumberOfPoints));
	}

	protected void handleScanEnd() {

		scanRunning = false;

		if (getDisableSequenceEditingDuringAnalyserScan()) {
			enableSequenceEditorAndToolbar(hasBaton());
		}

		Display.getDefault().asyncExec(() -> {
			for (Region region : regions) {
				if (region.isEnabled()) {
					if (isValidRegion(region, false)) {
						region.setStatus(STATUS.READY);
					}
					else {
						region.setStatus(STATUS.INVALID);
					}
				}
			}
			txtTimeRemaining.setText(String.format("%.3f", 0.0));
			progressBar.setSelection(100);

			if (!hasBatonCached){
				txtSequenceFileEditingStatus.setText(BATON_NOT_HELD);
			}
			else {
				txtSequenceFileEditingStatus.setText(EDITABLE);
			}
		});

		analyserScanProgressUpdates.cancel(true);
	}

	private void startRunningAnimation() {
		logger.debug("Setting up running animation thread");

		stopRunningAnimation();

		AnimationUpdate animationUpdate = () -> {
			AnimationHandler animationHandler = AnimationHandler.getInstance();

			//When the next frame is ready, tell it what it needs to update
			//to display next frame
			if (!sequenceTableViewer.getTable().isDisposed()) {
				regions.stream()
					.filter(r -> r.getStatus() == STATUS.RUNNING)
					.forEach(r -> sequenceTableViewer.refresh(r));
			}
			else {
				animationHandler.cancel();
			}
		};

		AnimationHandler animationHandler = AnimationHandler.getInstance();
		animationHandler.setAnimationUpdate(animationUpdate);
		animationHandler.start();
	}

	private void stopRunningAnimation() {
		AnimationHandler animationHandler = AnimationHandler.getInstance();
		if (animationHandler.isThreadAlive()) {
			logger.debug("Stopping the running animation thread");
			animationHandler.cancel();
		}
	}

	private void enableSequenceEditorAndToolbar(boolean canEdit) {
		this.canEdit = canEdit;

		//Tell region editor if we can edit
		Display.getDefault().asyncExec(() -> {
			fireSelectionChanged(new CanEditRegionSelection(sequence.getFilename(), canEdit));
		});

		//get the service and get our source provider by querying by the variable name
		SequenceViewLiveEnableToolbarSourceProvider toolbarSourceProvider = (SequenceViewLiveEnableToolbarSourceProvider) this.getViewSite()
			.getWorkbenchWindow()
			.getService(ISourceProviderService.class)
			.getSourceProvider(SequenceViewLiveEnableToolbarSourceProvider.SOURCE_NAME);

		toolbarSourceProvider.setEnabled(canEdit);

		//Update local actions to be enabled / disabled
		IContributionItem[] items = getViewSite().getActionBars().getToolBarManager().getItems();
		for (IContributionItem item : items) {
			if (item instanceof ActionContributionItem actionItem) {
				actionItem.getAction().setEnabled(canEdit);
			}
		}
	}

	protected class AnalyserTotalTimeRemainingListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				currentregiontimeremaining = ((DBR_Double) dbr).getDoubleValue()[0];
				logger.debug("iteration time remaining changed to {}", currentregiontimeremaining);
			}
		}
	}

	protected double getCompletedRegionsTimeTotal(List<Region> regionsCompleted) {
		double timeCompleted = 0.0;
		for (Region region : regionsCompleted) {
			timeCompleted += region.getTotalTime();
		}
		return timeCompleted;
	}

	protected class AnalyserStateListener implements MonitorListener {

		protected boolean running = false;

		@Override
		public void monitorChanged(final MonitorEvent arg0) {
			if (first) {
				first = false;
				logger.debug("analyser state listener connected.");
				return;
			}
			DBR dbr = arg0.getDBR();
			short state = 0;
			if (dbr.isENUM()) {
				state = ((DBR_Enum) dbr).getEnumValue()[0];
			}
			if (currentRegion != null) {
				switch (state) {
				case 0:
					if (running) {
						updateRegionStatus(currentRegion, STATUS.COMPLETED);
						running = false;
						logger.debug("analyser is in completed state for current region: {}", currentRegion.toString());
					}
					break;
				case 1:
					running = true;
					logger.debug("analyser is in running state for current region: {}", currentRegion.toString());
					break;
				case 6:
					updateRegionStatus(currentRegion, STATUS.ABORTED);
					running = false;
					if (analyserScanProgressUpdates != null) {
						analyserScanProgressUpdates.cancel(true);
					}
					logger.error("analyser in error state for region; {}", currentRegion.toString());
					break;
				case 10:
					updateRegionStatus(currentRegion, STATUS.ABORTED);
					running = false;
					if (analyserScanProgressUpdates != null) {
						analyserScanProgressUpdates.cancel(true);
					}
					logger.warn("analyser is in aborted state for currentregion: {}", currentRegion.toString());
					break;
				default:
					logger.debug("analyser is in a unhandled state: {}", state);
					break;

				}
			} else {
				logger.debug("currentRegion object is null, no region state to update!!!");
			}
		}
	}

	@Override
	public void initializationCompleted() throws InterruptedException, DeviceException, TimeoutException, CAException {
		logger.debug("EPICS channel {} initialisation completed.", getDetectorStatePV());
	}

	public String getDetectorStatePV() {
		return analyserStatePV;
	}

	public void setDetectorStatePV(String statePV) {
		this.analyserStatePV = statePV;
	}

	public String getAnalyserTotalTimeRemianingPV() {
		return analyserTotalTimeRemianingPV;
	}

	public void setAnalyserTotalTimeRemianingPV(String analyserTotalTimeRemianingPV) {
		this.analyserTotalTimeRemianingPV = analyserTotalTimeRemianingPV;
	}

	public void setAnalyser(IVGScientaAnalyserRMI analyser) {
		this.analyser = analyser;
	}

	public IVGScientaAnalyserRMI getAnalyser() {
		return analyser;
	}

	public void setDisableSequenceEditingDuringAnalyserScan(boolean value) {
		this.disableSequenceEditingDuringAnalyserScan = value;
	}

	public boolean getDisableSequenceEditingDuringAnalyserScan() {
		return disableSequenceEditingDuringAnalyserScan;
	}
}