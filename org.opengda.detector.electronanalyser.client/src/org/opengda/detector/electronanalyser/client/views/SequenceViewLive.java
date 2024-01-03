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
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.action.MenuManager;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPart;
import org.opengda.detector.electronanalyser.client.selection.EnergyChangedSelection;
import org.opengda.detector.electronanalyser.client.selection.RegionRunCompletedSelection;
import org.opengda.detector.electronanalyser.client.selection.TotalTimeSelection;
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
import gda.device.Scannable;
import gda.device.scannable.ScannableStatus;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.EpicsController.MonitorType;
import gda.epics.connection.InitializationListener;
import gda.factory.Finder;
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

	private Scannable dcmenergy;
	private Scannable pgmenergy;

	private Button btnHardShutter;
	private Composite hardShutterState;
	private Button btnSoftShutter;
	private Composite softShutterState;
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
	private String hardShutterPV;
	private String softShutterPV;

	private EpicsChannelManager channelmanager;
	private Channel hardShutterChannel;
	private Channel softShutterChannel;
	@SuppressWarnings("unused")
	private Channel analyserStateChannel;
	@SuppressWarnings("unused")
	private Channel analyserTotalTimeRemainingChannel;

	private boolean first = true;
	private boolean firstTime;

	private AnalyserStateListener analyserStateListener;
	private AnalyserTotalTimeRemainingListener analyserTotalTimeRemainingListener;
	private SoftShutterStateListener softShutterStateListener;
	private HardShutterStateListener hardShutterStateListener;

	private IVGScientaAnalyserRMI analyser;
	private Scriptcontroller scriptcontroller;

	private List<Region> regionsCompleted = new ArrayList<>();

	private boolean elementSetConnected = true;

	private Runnable elementSetMonitor = () -> {

		if (!elementSetConnected) {
			return;
		}
		try {
			final String liveElementSetMode = getAnalyser().getPsuMode();

			Display.getCurrent().asyncExec(() -> {
				final String currentUIElementSet = txtElementSet.getText();
				if (!currentUIElementSet.equals(liveElementSetMode)) {
					updateFeature(sequence, RegiondefinitionPackage.eINSTANCE.getSequence_ElementSet(), liveElementSetMode);
					txtElementSet.setText(liveElementSetMode);
					logger.info("Detected change in elementSet. Changing from {} to {}", currentUIElementSet, liveElementSetMode);
					updateAllRegionStatus(false, false);
				}

			});
		}
		catch (Exception e) {
			logger.error("Unable to check electron analyser element set value.");
			//Prevent the log being spammed with the same error message
			elementSetConnected = false;
		}
	};

	@Override
	protected void selectionListenerDetectedUpdate(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof TotalTimeSelection) {
			updateAllRegionStatus(true, true);
			updateCalculatedData();
		} else if (selection instanceof EnergyChangedSelection energyChangeSelection) {
			Region region = energyChangeSelection.getRegion();
			boolean valid = isValidRegion(region, false);
			boolean isFromExcitationEnergyChange = energyChangeSelection.isExcitationEnergyChange();

			if (!valid && !isFromExcitationEnergyChange) {
				try {
					runCommand(SetCommand.create(editingDomain, region, RegiondefinitionPackage.eINSTANCE.getRegion_Enabled(), valid));
				} catch (Exception e) {
					logger.error("Unable to update status and show popup", e);
				}
			}

		} else if (selection instanceof IStructuredSelection sel) {
			Object firstElement = sel.getFirstElement();
			if (firstElement instanceof Region) {
				sequenceTableViewer.setSelection(sel);
			}
		}
		sequenceTableViewer.refresh();
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

		createShutters(controlArea);
		createElementSet(controlArea);
		createTotalSequenceTime(controlArea);
		createNumberOfActiveRegions(controlArea);
		createSequenceFile(controlArea, numberOfColumns);
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

	private void createShutters(Composite controlArea) {

		Group grpShutters = null;

		if (getHardShutterPV() != null || getSoftShutterPV() != null) {
			grpShutters = new Group(controlArea, SWT.BORDER);
			GridDataFactory.fillDefaults().span(3, 1).grab(true, false).applyTo(grpShutters);
			grpShutters.setText("Fast Shutters");
			grpShutters.setLayout(new GridLayout(2, true));
			grpShutters.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		}

		if (getHardShutterPV() != null) {
			Group grpHardShutter = new Group(grpShutters, SWT.NONE);
			GridData gdGrpHardShutter = new GridData(SWT.LEFT, SWT.CENTER, true, false);
			grpHardShutter.setLayoutData(gdGrpHardShutter);
			grpHardShutter.setLayout(new GridLayout(3, false));
			grpHardShutter.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

			Label lblHardXray = new Label(grpHardShutter, SWT.None);
			String name = "Hard X-Ray";
			lblHardXray.setText(name + ": ");

			/* Composite to contain the status composite so that a border can be displayed. */
			Composite borderComposite = new Composite(grpHardShutter, SWT.NONE);
			borderComposite.setBackground(borderComposite.getDisplay().getSystemColor(SWT.COLOR_BLACK));
			FillLayout fillLayout = new FillLayout();
			fillLayout.marginWidth = 2;
			fillLayout.marginHeight = 2;
			borderComposite.setLayout(fillLayout);
			GridDataFactory.fillDefaults().indent(3, 0).hint(20, 20).applyTo(borderComposite);
			hardShutterState = new Composite(borderComposite, SWT.FILL);

			btnHardShutter = new Button(grpHardShutter, SWT.PUSH);
			btnHardShutter.setText("Close");
			btnHardShutter.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					moveShutters(btnHardShutter, hardShutterChannel, name, event);
				}
			});

		} else {
			new Label(grpShutters, SWT.None);
		}

		if (getSoftShutterPV() != null) {
			Group grpSoftShutter = new Group(grpShutters, SWT.NONE);
			GridData gdGrpSoftShutter = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
			grpSoftShutter.setLayoutData(gdGrpSoftShutter);
			grpSoftShutter.setLayout(new GridLayout(4, false));
			grpSoftShutter.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

			Label lblSoftXray = new Label(grpSoftShutter, SWT.None);
			String name = "Soft X-Ray";
			lblSoftXray.setText(name + ": ");

			/* Composite to contain the status composite so that a border can be displayed. */
			Composite borderComposite = new Composite(grpSoftShutter, SWT.NONE);
			borderComposite.setBackground(borderComposite.getDisplay().getSystemColor(SWT.COLOR_BLACK));
			FillLayout fillLayout = new FillLayout();
			fillLayout.marginWidth = 2;
			fillLayout.marginHeight = 2;
			borderComposite.setLayout(fillLayout);
			GridDataFactory.fillDefaults().indent(3, 0).hint(20, 20).applyTo(borderComposite);
			softShutterState = new Composite(borderComposite, SWT.FILL);

			btnSoftShutter = new Button(grpSoftShutter, SWT.PUSH);
			btnSoftShutter.setText("Close");
			btnSoftShutter.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					moveShutters(btnSoftShutter, softShutterChannel, name, event);
				}
			});
		} else {
			new Label(grpShutters, SWT.None);
		}
	}

	private void moveShutters(Button shutter, Channel channel, String name, SelectionEvent event) {

		if (event.getSource() == shutter) {
			int value = shutter.getText().equalsIgnoreCase("Open") ? 0 : 1;
			try {
				EpicsController.getInstance().caput(channel, value);
			} catch (CAException | InterruptedException e) {
				logger.error("Failed to " + (value == 0 ? "open" : "close") + " fast shutter for " + name, e);
			}
		}
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

		Async.scheduleAtFixedRate(elementSetMonitor, 3, 3, TimeUnit.SECONDS);
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

		try { // initialise with the current PV value
			txtElementSet.setText(getAnalyser().getPsuMode());
		} catch (Exception e) {
			logger.error("Cannot get the current element set from analyser.", e);
			txtElementSet.setText(sequence.getElementSet());
		}

		// server event admin or handler
		scriptcontroller = Finder.find("SequenceFileObserver");
		scriptcontroller.addIObserver(this);
		// EPICS monitor to update current region status
		channelmanager = new EpicsChannelManager(this);
		analyserStateListener = new AnalyserStateListener();
		analyserTotalTimeRemainingListener = new AnalyserTotalTimeRemainingListener();
		hardShutterStateListener = new HardShutterStateListener();
		softShutterStateListener = new SoftShutterStateListener();
		try {
			createChannels();
		} catch (CAException e1) {
			logger.error("failed to create required spectrum channels", e1);
		}
		updateRegionNumber(crrentRegionNumber, numActives);
		dcmenergy = Finder.find("dcmenergy");
		if (dcmenergy == null) {
			logger.error("Finder failed to find 'dcmenergy'");
		} else {
			dcmenergy.addIObserver(this);
		}
		pgmenergy = Finder.find("pgmenergy");
		if (pgmenergy == null) {
			logger.error("Finder failed to find 'pgmenergy'");
		} else {
			pgmenergy.addIObserver(this);
		}
		updateHardXRayEnergy();
		updateSoftXRayEnergy();
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

	@Override
	protected void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(SequenceViewLive.this::fillContextMenu);
		Menu menu = menuMgr.createContextMenu(sequenceTableViewer.getControl());
		sequenceTableViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, sequenceTableViewer);
	}

	protected void updateRegionStatus(final Region region, final STATUS status) {
		getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				region.setStatus(status);
				sequenceTableViewer.refresh();
			}
		});
	}

	protected void resetRegionStatus() {
		getViewSite().getShell().getDisplay().asyncExec(() ->{
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
			sequenceTableViewer.refresh();
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
		if (getHardShutterPV() != null) {
			hardShutterChannel = channelmanager.createChannel(getHardShutterPV(), hardShutterStateListener, MonitorType.NATIVE, false);
		}
		if (getSoftShutterPV() != null) {
			softShutterChannel = channelmanager.createChannel(getSoftShutterPV(), softShutterStateListener, MonitorType.NATIVE, false);
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
		if (elementSetConnected) {
			Thread thread = new Thread(elementSetMonitor);
			thread.start();
		}
		else {
			txtElementSet.getDisplay().asyncExec(() -> {
				txtElementSet.setText(sequence.getElementSet());
			});
		}
	}


	@Override
	public void dispose() {
		scriptcontroller.deleteIObserver(this);
		super.dispose();
	}

	@Override
	public void update(Object source, final Object arg) {
		if (source == scriptcontroller) {
			handleEvent(arg);
		}
		if (arg == ScannableStatus.IDLE) {
			if (source == dcmenergy) {
				updateHardXRayEnergy();
			}
			if (source == pgmenergy) {
				updateSoftXRayEnergy();
			}
		}
	}

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
				if (currentRegion != region) {
					updateRegionStatus(currentRegion, STATUS.COMPLETED);
					regionsCompleted.add(currentRegion);
				}
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
		firstTime = true;
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
						if (firstTime) {
							txtTimeRemaining.setText(String.format("%.3f", totalScanTime));
							firstTime = false;
						} else if (scanTimeRemaining < Double.valueOf(txtTimeRemaining.getText().trim())) {
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
		});
		analyserScanProgressUpdates.cancel(true);
	}

	protected void updateHardXRayEnergy() {
		try {
			hardXRayEnergy = (double) dcmenergy.getPosition() * 1000; // eV
		} catch (DeviceException e) {
			logger.error("Cannot get X-ray energy from DCM.", e);
		}
	}

	protected void updateSoftXRayEnergy() {
		try {
			softXRayEnergy = (double) pgmenergy.getPosition();
		} catch (DeviceException e) {
			logger.error("Cannot get X-ray energy from PGM.", e);
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

	protected class HardShutterStateListener implements MonitorListener {

		@Override
		public void monitorChanged(final MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			short state = 0;
			if (dbr.isENUM()) {
				state = ((DBR_Enum) dbr).getEnumValue()[0];
				setShutterState(hardShutterState, state);
				setShutterControlButtonText(btnHardShutter, state);
			}
		}
	}

	protected class SoftShutterStateListener implements MonitorListener {

		@Override
		public void monitorChanged(final MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			short state = 0;
			if (dbr.isENUM()) {
				state = ((DBR_Enum) dbr).getEnumValue()[0];
				setShutterState(softShutterState, state);
				setShutterControlButtonText(btnSoftShutter, state);
			}
		}
	}

	@Override
	public void initializationCompleted() throws InterruptedException, DeviceException, TimeoutException, CAException {
		logger.debug("EPICS channel {} initialisation completed.", getDetectorStatePV());
	}

	protected void setShutterControlButtonText(final Button btnShutter, final short state) {
		if (btnShutter != null && !btnShutter.isDisposed()) {
			btnShutter.getDisplay().asyncExec(() -> {
				if (!btnShutter.isDisposed()) {
					if (state == 0) {
						btnShutter.setText("Close");
					} else if (state == 1) {
						btnShutter.setText("Open");
					} else {
						btnShutter.setText("Unknown");
						btnShutter.setForeground(btnShutter.getDisplay().getSystemColor(SWT.COLOR_RED));
					}
				}
			});
		}
	}

	public String getDetectorStatePV() {
		return analyserStatePV;
	}

	public void setDetectorStatePV(String statePV) {
		this.analyserStatePV = statePV;
	}

	public String getHardShutterPV() {
		return hardShutterPV;
	}

	public void setHardShutterPV(String hardShutterPV) {
		this.hardShutterPV = hardShutterPV;
	}

	public String getSoftShutterPV() {
		return softShutterPV;
	}

	public void setSoftShutterPV(String softShutterPV) {
		this.softShutterPV = softShutterPV;
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
}