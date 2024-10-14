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

import org.eclipse.emf.common.notify.Notification;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;
import org.opengda.detector.electronanalyser.client.SESLivePerspective;
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
import org.opengda.detector.electronanalyser.nxdetector.IEW4000;
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
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.gda.devices.vgscienta.IVGScientaAnalyserRMI;

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
	private int totalNumberOfPoints;
	private int crrentRegionNumber;
	private double totalSequenceTimes = 0.0;
	private int numActives = 0;

	private IEW4000 ew4000;
	private String analyserStatePV;
	private String analyserTotalTimeRemianingPV;

	private IVGScientaAnalyserRMI analyser;
	private Scriptcontroller scriptcontroller;

	private List<Region> regionsCompleted = new ArrayList<>();

	private boolean disableSequenceEditingDuringAnalyserScan = true;

	private boolean scanRunning = false;
	private boolean hasBatonCached = true;
	private int regionNumber = 0;

	private Future<?> elementSetConnected;
	private Runnable elementSetMonitor = () -> {
		if (txtElementSet.isDisposed()) {
			elementSetConnected.cancel(true);
			return;
		}
		try {
			final String liveElementSetMode = getAnalyser().getPsuMode();
			txtElementSet.getDisplay().asyncExec(() -> setElementSet(liveElementSetMode));
		}
		catch (Exception e) {
			logger.error("Unable to check electron analyser element set value.", e);
			//Prevent the log being spammed with the same error message
			elementSetConnected.cancel(true);
			logger.error("Stopping elementSet thread.");
			txtElementSet.getDisplay().asyncExec(() -> setElementSet(ELEMENTSET_UNKNOWN));
		}
	};

	//Have this method be only way to update elementSet value and display to UI
	@Override
	protected void setElementSet(String newElementSet) {
		final boolean unknown = newElementSet.equals(ELEMENTSET_UNKNOWN);
		//This is needed because when initial elementSet is set, file is not loaded yet so this is used to get back in sync.
		if(sequence != null && !unknown && !sequence.getElementSet().equals(newElementSet)) {
			logger.info("Saving elementSet value to sequence file: {}", newElementSet);
			sequence.setElementSet(newElementSet);
		}
		if(elementSet.equals(newElementSet) && txtElementSet.getText().equals(newElementSet)) {
			return;
		}
		logger.info("Updating elementSet from {} to {}", elementSet, newElementSet);
		elementSet = newElementSet;
		if(sequence != null) {
			validateAllRegions();
		}
		txtElementSet.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		txtElementSet.setText(elementSet);
		if(unknown) {
			final String errorText = "Can't connect to electron analyser to check element set value. Unable to validate regions. Check the connection and then restart server and client.";
			txtElementSet.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
			txtElementSet.setToolTipText(errorText);
			Display.getCurrent().asyncExec(() -> openMessageBox("Element set " + ELEMENTSET_UNKNOWN, errorText, SWT.ICON_ERROR));
		}
	}

	@Override
	protected void selectionListenerDetectedUpdate(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof TotalTimeSelection) {
			updateCalculatedData();
		} else if (selection instanceof EnergyChangedSelection energyChangeSelection) {
			final boolean isFromExcitationEnergyChange = energyChangeSelection.isExcitationEnergyChange();
			final boolean showDialogIfInvalid = !isFromExcitationEnergyChange;
			for (Region region : energyChangeSelection.getRegions()) {
				final boolean valid = isValidRegion(region, showDialogIfInvalid);
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

	@Override
	protected boolean notifySaveNeeded(Notification notification) {
		boolean saveNeeded = super.notifySaveNeeded(notification);
		//Make excitation energy change make file dirty only if it switches to the opposite limit e.g soft limit to hard, hard to soft (user selected change)
		if (saveNeeded && notification.getFeature().equals(RegiondefinitionPackage.eINSTANCE.getRegion_ExcitationEnergy())) {
			double oldValue = notification.getOldDoubleValue();
			double newValue = notification.getNewDoubleValue();
			boolean softToHard = getRegionDefinitionResourceUtil().isSourceSoft(oldValue) && getRegionDefinitionResourceUtil().isSourceHard(newValue);
			boolean hardToSoft = getRegionDefinitionResourceUtil().isSourceHard(oldValue) && getRegionDefinitionResourceUtil().isSourceSoft(newValue);
			saveNeeded = softToHard || hardToSoft;
		}
		return saveNeeded;
	}

	public SequenceViewLive() {
		super();
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

		txtSequenceFileEditingStatus.addModifyListener((event) -> {
			if (txtSequenceFileEditingStatus.getText().equals(EDITABLE)) {
				borderComposite.setBackground(borderComposite.getDisplay().getSystemColor(SWT.COLOR_GREEN));
				requestbaton.setEnabled(false);
			}
			else {
				borderComposite.setBackground(borderComposite.getDisplay().getSystemColor(SWT.COLOR_RED));
				requestbaton.setEnabled(!(hasBaton() && scanRunning));
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
			if (getDisableSequenceEditingDuringAnalyserScan() && scanRunning) {
				message = LOCKED_DURING_SCAN;
			}
			enableSequenceEditorAndToolbar(hasBaton() && !scanRunning);
			txtSequenceFileEditingStatus.setText(message);

			String perspectiveID = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective().getId();
			if (currentHasBatonCache && !hasBaton() && perspectiveID.equals(SESLivePerspective.ID)) {
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
		try {
			final String liveElementSet = getAnalyser().getPsuMode();
			setElementSet(liveElementSet);
		} catch (Exception e) {
			logger.error("Unable to get initial elementSet value", e);
			setElementSet(ELEMENTSET_UNKNOWN);
		}
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
		gridData.heightHint = (int) Math.ceil(txtDataFilePath.getLineHeight() * 1.25) ;
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
		updateScanPointNumber(0, 0);

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

		// server event admin or handler
		scriptcontroller = Finder.find("SequenceFileObserver");
		scriptcontroller.addIObserver(this);
		try {
			createChannels();
		} catch (CAException e1) {
			logger.error("failed to create required spectrum channels", e1);
		}
		updateRegionNumber(crrentRegionNumber, numActives);
		canEdit = hasBaton();
	}

	private void checkIfScanIsRunningAndPeformSetup() {
		final boolean isDetectorBusy = getEw4000().isBusy();

		//Check if analyserscan is running
		if (!isDetectorBusy) {
			return;
		}
		scanRunning = true;
		final boolean canEditSequence = !getDisableSequenceEditingDuringAnalyserScan();
		enableSequenceEditorAndToolbar(canEditSequence && hasBaton());

		//If running, we need to update sequence file to one running on server
		final String sequenceFileName = getEw4000().getSequenceFile();
		final String currentSequenceFileName = getRegionDefinitionResourceUtil().getFileName();
		if (!currentSequenceFileName.equals(sequenceFileName)) {
			refreshTable(sequenceFileName, false);
		}
		//Sync the GUI to show the current region running on server and the already completed regions
		final String currentRegionId = getEw4000().getCurrentRegionID();
		Optional<Region> filteredRegions = regions.stream().filter(r -> r.getRegionId().equals(currentRegionId)).findFirst();
		if (!filteredRegions.isPresent()) {
			return;
		}
		final Region serverCurrentRegion = filteredRegions.get();
		for (Region r : regions) {
			if (r == serverCurrentRegion) {
				updateRegionStatus(serverCurrentRegion, STATUS.RUNNING);
				currentRegion = serverCurrentRegion;
				fireSelectionChanged(currentRegion);
				break;
			}
			else if (r.isEnabled()){
				final boolean valid = isValidRegion(r, false);
				updateRegionStatus(r, valid ? STATUS.COMPLETED : STATUS.INVALID);
			}
		}
	}

	private void updateRegionNumber(int currentRegionNumber, int totalActiveRegions) {
		txtRegionValue.setText(String.valueOf(currentRegionNumber) + '/' + String.valueOf(totalActiveRegions));
	}

	private void updateScanPointNumber(int currentPointNumber, int totalNumberOfPoints) {
		txtPointValue.setText(String.valueOf(currentPointNumber) + '/' + String.valueOf(totalNumberOfPoints));
	}

	@Override
	protected void updateRegionStatus(final Region region, final STATUS newStatus) {
		if (region.getStatus() == STATUS.RUNNING && newStatus != STATUS.RUNNING) {
			stopRunningAnimation();
		}
		else if (newStatus == STATUS.RUNNING) {
			startRunningAnimation();
		}

		if (newStatus == STATUS.COMPLETED) {
			regionsCompleted.add(region);
		}
		super.updateRegionStatus(region, newStatus);
	}

	private void createChannels() throws CAException {
		// EPICS monitor to update current region status
		final EpicsChannelManager channelmanager = new EpicsChannelManager(this);
		final AnalyserTotalTimeRemainingListener analyserTotalTimeRemainingListener = new AnalyserTotalTimeRemainingListener();
		if (getAnalyserTotalTimeRemianingPV() != null) {
			channelmanager.createChannel(getAnalyserTotalTimeRemianingPV(), analyserTotalTimeRemainingListener, MonitorType.NATIVE, false);
		}
		channelmanager.creationPhaseCompleted();
		sequenceTableViewer.getTable().addDisposeListener(e -> channelmanager.destroy());
		logger.debug("Analyser state channel and monitor are created.");
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
										getCamera().getEnergyResolution() * region.getPassEnergy() * (region.getLastXChannel() - region.getFirstXChannel() + 1));
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
		if(elementSetConnected == null || elementSetConnected.isCancelled()) {
			elementSetConnected = Async.scheduleAtFixedRate(elementSetMonitor, 0, 2, TimeUnit.SECONDS);
		}
		super.refreshTable(seqFileName, newFile);
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
			Display.getDefault().asyncExec(() ->
				refreshTable(getRegionDefinitionResourceUtil().getFileName(), false)
			);
			updateBatonHolder();
		}
	}

	//Events received from server
	private void handleEvent(Object event) {
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

	private void handleSequenceFileChange(SequenceFileChangeEvent changeEvent) {
		logger.debug("Sequence file changed to {}", changeEvent.getFilename());
		refreshTable(changeEvent.getFilename(), false);
	}

	private void handleRegionChange(RegionChangeEvent event) {
		logger.debug("region update to {}", event.getRegionName());
		String regionId = event.getRegionId();
		for (Region region : regions) {
			if (region.getRegionId().equalsIgnoreCase(regionId)) {
				currentRegion = region;
				regionNumber++;
				updateRegionNumber(regionNumber, numActives);
			}
		}
		fireSelectionChanged(currentRegion);
		sequenceTableViewer.setSelection(new StructuredSelection(currentRegion));
	}

	private void handleRegionStatusChange(RegionStatusEvent event) {
		final String regionId = event.getRegionId();
		final STATUS status = event.getStatus();

		Display.getDefault().asyncExec(() -> {
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

	private void handleScanStart(ScanStartEvent event) {
		scanRunning = true;
		regionsCompleted.clear();
		if (getDisableSequenceEditingDuringAnalyserScan()) {
			enableSequenceEditorAndToolbar(false);
			Display.getDefault().asyncExec(() -> txtSequenceFileEditingStatus.setText(LOCKED_DURING_SCAN));
		}
		resetCurrentRegion();

		totalNumberOfPoints = event.getNumberOfPoints();
		final String scanFilename = event.getScanFilename();
		final int scanNumber = event.getScanNumber();
		final double totalScanTime = totalNumberOfPoints * totalSequenceTimes;

		Display.getDefault().asyncExec(() -> {
			updateScanPointNumber(0, totalNumberOfPoints);
			txtDataFilePath.setText(scanFilename);
			txtScanNumberValue.setText(String.valueOf(scanNumber));
			txtTimeRemaining.setText(String.format("%.3f", totalScanTime));
		});

		analyserScanProgressUpdates = Async.scheduleAtFixedRate(
			new TimerTask() {
				@Override
				public void run() {
					Display.getDefault().asyncExec(() -> {
						double scanTimeRemaining = totalScanTime - getCompletedRegionsTimeTotal(regionsCompleted) - currentRegion.getTotalTime() + currentregiontimeremaining;
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

	private void handleScanPointStart(ScanPointStartEvent event) {
		regionNumber = 0;
		Display.getDefault().asyncExec(() -> updateScanPointNumber(event.getCurrentPointNumber(), totalNumberOfPoints));
	}

	private void handleScanEnd() {
		scanRunning = false;
		if (getDisableSequenceEditingDuringAnalyserScan()) {
			enableSequenceEditorAndToolbar(hasBaton());
		}

		Display.getDefault().asyncExec(() -> {
			//Reset regions back to ready and then validate regions
			logger.debug("Resetting all regions back to READY state to be validated.");
			regions.stream().forEach(r -> updateRegionStatus(r, STATUS.READY));
			regions.stream().forEach(r -> isValidRegion(r, false));

			txtTimeRemaining.setText(String.format("%.3f", 0.0));
			progressBar.setSelection(100);
			if (!hasBatonCached){
				txtSequenceFileEditingStatus.setText(BATON_NOT_HELD);
			}
			else {
				txtSequenceFileEditingStatus.setText(EDITABLE);
			}
		});
		if (analyserScanProgressUpdates != null) analyserScanProgressUpdates.cancel(true);
	}

	private void startRunningAnimation() {
		final AnimationHandler animationHandler = AnimationHandler.getInstance();
		if (animationHandler.isThreadAlive()) {
			return;
		}
		logger.debug("Setting up running animation thread");
		final AnimationUpdate animationUpdate = () -> {
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

	private class AnalyserTotalTimeRemainingListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				currentregiontimeremaining = ((DBR_Double) dbr).getDoubleValue()[0];
				logger.debug("iteration time remaining changed to {}", currentregiontimeremaining);
			}
		}
	}

	private double getCompletedRegionsTimeTotal(List<Region> regionsCompleted) {
		double timeCompleted = 0.0;
		for (Region region : regionsCompleted) {
			timeCompleted += region.getTotalTime();
		}
		return timeCompleted;
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

	public IEW4000 getEw4000() {
		return ew4000;
	}

	public void setEw4000(IEW4000 ew4000) {
		this.ew4000 = ew4000;
	}
}