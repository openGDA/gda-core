package org.opengda.detector.electronanalyser.client.views;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.PageBook;
import org.opengda.detector.electronanalyser.client.selection.CanEditRegionSelection;
import org.opengda.detector.electronanalyser.client.selection.EnergyChangedSelection;
import org.opengda.detector.electronanalyser.client.selection.FileSelection;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableStatus;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.diamond.daq.concurrent.Async;

/**
 * A Region Editor View for defining new or editing existing Region Definition for VG Scienta Electron Analyser.
 *
 * @author fy65
 *
 */
public class RegionViewLive extends RegionViewCreator implements ISelectionProvider, IObserver{

	public static final String ID = "org.opengda.detector.electronanalyser.client.regioneditor";
	protected static final Logger logger = LoggerFactory.getLogger(RegionViewLive.class);

	private Button btnHard;
	private Button btnSoft;
	private Scannable dcmenergy;
	private Scannable pgmenergy;
	private Text txtSoftEnergy;
	private Text txtHardEnergy;
	private double hardXRayEnergy = 5000.0; // eV
	private double softXRayEnergy = 500.0; // eV
	private RegionProgressComposite progressComposite;

	private String currentIterationRemainingTimePV;
	private String iterationLeadPointsPV;
	private String iterationProgressPV;
	private String totalDataPointsPV;
	private String iterationCurrentPointPV;
	private String totalRemianingTimePV;
	private String totalProgressPV;
	private String totalPointsPV;
	private String currentPointPV;
	private String currentIterationPV;
	private String totalIterationsPV;

	private String statePV;
	private String acquirePV;
	private String messagePV;
	private String zeroSuppliesPV;
	private AnalyserComposite analyserComposite;

	private double pgmEnergyDetectChangeGuiToleranceLevel = 0.075;

	public RegionViewLive() {
		setTitleToolTip("Edit parameters for selected region");
		// setContentDescription("A view for editing region parameters");
		setPartName("Region Editor");
		this.selectionChangedListeners = new ArrayList<>();

		setSequenceViewID(SequenceViewLive.ID);
	}

	@Override
	protected void handleFileSelection(FileSelection fileSelection) {
		// sequence file changed
		try {
			regions = regionDefinitionResourceUtil.getRegions();
			populateRegionNameCombo(regions);

			//Ensure each region is showing correct valid state for pgmenergy/dcmenergy on initial file load
			for (Region r : regions) {
				setExcitationEnergy(r);
				if (excitationEnergy != r.getExcitationEnergy()) {
					updateFeature(r, RegiondefinitionPackage.eINSTANCE.getRegion_ExcitationEnergy(), excitationEnergy);
				}
			}

			if (regions.isEmpty()) {
				region = null;
				regionPageBook.showPage(plainComposite);
			} else {
				regionPageBook.showPage(regionComposite);
				region = getSelectedRegionInSequenceView();
				initialiseViewWithRegionData(region);
			}
		} catch (Exception e) {
			logger.error("Cannot get regions list from {}", regionDefinitionResourceUtil.getFileName(), e);
		}
	}

	@Override
	protected void detectSelectionListener(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof CanEditRegionSelection canEditRegionSelection) {
			setCanEdit(canEditRegionSelection.getCanEdit());
			canEdit = canEditRegionSelection.getCanEdit();
		}
		else {
			super.detectSelectionListener(part, selection);
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		regionPageBook = new PageBook(parent, SWT.None);
		plainComposite = new Composite(regionPageBook, SWT.None);
		plainComposite.setLayout(new FillLayout());
		plainComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		regionComposite = new ScrolledComposite(regionPageBook, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		regionComposite.setExpandHorizontal(true);
		regionComposite.setExpandVertical(true);
		regionComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		Composite rootComposite = new Composite(regionComposite, SWT.NONE);
		regionComposite.setContent(rootComposite);
		GridLayoutFactory.fillDefaults().margins(10, SWT.DEFAULT).spacing(2, 8).applyTo(rootComposite);

		createNameAndLensModeAndPassEnergyArea(rootComposite);
		createAcquisitionConfigurationAndModeArea(rootComposite);
		createExcitationEnergyAndEnergyModeArea(rootComposite, parent);
		createSpectrumEnergyRangeArea(rootComposite);
		createRegionErrorBoxArea(rootComposite);
		createStepArea(rootComposite);
		createDetectorArea(rootComposite);
		createProgressArea(rootComposite);
		createAnalyserArea(rootComposite);

		getViewSite().setSelectionProvider(this);
		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(getSequenceViewID(), selectionListener);
		initialisation();
	}

	protected void setCanEdit(boolean enabled) {
		regionName.setEnabled(enabled);
		lensMode.setEnabled(enabled);
		passEnergy.setEnabled(enabled);
		//Acquisition Configuration / Mode
		numberOfIterationSpinner.setEnabled(enabled);
		spinnerSlices.setEnabled(enabled);
		btnFixed.setEnabled(enabled);
		btnSwept.setEnabled(enabled);
		//Excitation energy and mode
		btnHard.setEnabled(enabled);
		btnSoft.setEnabled(enabled);
		btnBinding.setEnabled(enabled);
		btnKinetic.setEnabled(enabled);

		if (btnSwept.getSelection()) {
			toggleSweptModeParameters(enabled);
		}
		else {
			toggleFixedModeParameters(enabled);
		}

		//Step
		spinnerFrames.setEnabled(enabled);
		txtTime.setEnabled(enabled);

		//Detector
		spinnerYChannelFrom.setEnabled(enabled);
		spinnerYChannelTo.setEnabled(enabled);
		spinnerEnergyChannelFrom.setEnabled(enabled);
		spinnerEnergyChannelTo.setEnabled(enabled);
		btnPulseMode.setEnabled(enabled);
		btnADCMode.setEnabled(enabled);
	}

	@Override
	protected void createExcitationEnergyAndEnergyModeArea(Composite rootComposite, Composite parent) {
		Composite energyComposite = new Composite(rootComposite, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(energyComposite);
		GridLayout energylayout = new GridLayout(2, false);
		energylayout.marginWidth = 0;
		energyComposite.setLayout(energylayout);

		Group grpExcitationEnergy = new Group(energyComposite, SWT.NONE);
		grpExcitationEnergy.setText("Excitation Energy [eV]");
		grpExcitationEnergy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpExcitationEnergy.setLayout(new GridLayout(2, false));
		grpExcitationEnergy.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		if (regionDefinitionResourceUtil.isSourceSelectable()) {
			btnHard = new Button(grpExcitationEnergy, SWT.RADIO);
			btnHard.setText("Hard X-Ray:");
			btnHard.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (e.getSource().equals(btnHard) && btnHard.getSelection()) {
						updateExcitationEnergy(txtHardEnergy);
					}
				}
			});

			txtHardEnergy = new Text(grpExcitationEnergy, SWT.BORDER | SWT.READ_ONLY);
			txtHardEnergy.setToolTipText("Current hard X-ray beam energy");
			txtHardEnergy.setEnabled(false);
			txtHardEnergy.setEditable(false);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(txtHardEnergy);

			btnSoft = new Button(grpExcitationEnergy, SWT.RADIO);
			btnSoft.setText("Soft X-Ray:");
			btnSoft.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (e.getSource().equals(btnSoft) && btnSoft.getSelection()) {
						updateExcitationEnergy(txtSoftEnergy);
					}
				}
			});

			txtSoftEnergy = new Text(grpExcitationEnergy, SWT.BORDER | SWT.READ_ONLY);
			txtSoftEnergy.setToolTipText("Current soft X-ray beam energy");
			txtSoftEnergy.setEnabled(false);
			txtSoftEnergy.setEditable(false);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(txtSoftEnergy);

			//Only if pgmenergy is moving, update UI values.
			Runnable movingPgmMonitor = () -> {
				if (parent.getDisplay().isDisposed()) {
					return;
				}
				parent.getDisplay().asyncExec(() -> {
					updateExcitationEnergyUIWhileMoving(getPgmEnergy(), txtSoftEnergy);
				});
			};
			Async.scheduleAtFixedRate(movingPgmMonitor, 2, 2, TimeUnit.SECONDS);

		} else {
			Label lblCurrentValue = new Label(grpExcitationEnergy, SWT.NONE);
			lblCurrentValue.setText("X-Ray energy:");

			txtHardEnergy = new Text(grpExcitationEnergy, SWT.BORDER | SWT.READ_ONLY);
			txtHardEnergy.setToolTipText("Current X-ray beam energy");
			txtHardEnergy.setEnabled(false);
			txtHardEnergy.setEditable(false);
		}

		//Only if dcmenergy is moving, update UI values.
		Runnable movingDcmMonitor = () -> {
			if (parent.getDisplay().isDisposed()) {
				return;
			}
			parent.getDisplay().asyncExec(() -> {
				updateExcitationEnergyUIWhileMoving(getDcmEnergy(), txtHardEnergy);
			});
		};
		Async.scheduleAtFixedRate(movingDcmMonitor, 2, 2, TimeUnit.SECONDS);

		Group grpEnergyMode = new Group(energyComposite, SWT.NONE);
		grpEnergyMode.setText("Energy Mode");
		GridLayoutFactory.fillDefaults().margins(0, 8).applyTo(grpEnergyMode);
		grpEnergyMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpEnergyMode.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		btnKinetic = new Button(grpEnergyMode, SWT.RADIO);
		btnKinetic.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object source = e.getSource();
				onModifyEnergyMode(source);
			}
		});
		btnKinetic.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnKinetic.setText("Kinetic");

		btnBinding = new Button(grpEnergyMode, SWT.RADIO);
		btnBinding.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object source = e.getSource();
				onModifyEnergyMode(source);
			}
		});
		btnBinding.setText("Binding");
	}

	private void createProgressArea(Composite rootComposite) {
		GridLayout insertLayout = new GridLayout();
		insertLayout.marginTop = -20;
		insertLayout.marginWidth = 0;

		Group grpProgress = new Group(rootComposite, SWT.NONE);
		grpProgress.setText("Progress");
		GridDataFactory.fillDefaults().applyTo(grpProgress);
		grpProgress.setLayout(insertLayout);
		grpProgress.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		progressComposite = new RegionProgressComposite(grpProgress, SWT.None);
		grpProgress.pack();
	}

	private void createAnalyserArea(Composite rootComposite) {
		GridLayout insertLayout = new GridLayout();
		insertLayout.marginTop = -20;
		insertLayout.marginWidth = 0;

		Group grpAnalyser = new Group(rootComposite, SWT.NONE);
		grpAnalyser.setText("Analyser IOC");
		GridDataFactory.fillDefaults().applyTo(grpAnalyser);
		grpAnalyser.setLayout(insertLayout);
		grpAnalyser.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		analyserComposite = new AnalyserComposite(grpAnalyser, SWT.NONE);
		grpAnalyser.pack();

		regionComposite.setMinSize(rootComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private void updateExcitationEnergyUIWhileMoving(Scannable scannable, Text ui) {
		try {
			if (scannable.isBusy()) {
				double liveEnergy = (double) scannable.getPosition();
				if (scannable.getName().equals("dcmenergy")) {
					liveEnergy = liveEnergy * 1000;
				}
				final double uiEnergy = Double.parseDouble(ui.getText());
				if (uiEnergy != liveEnergy) {
					ui.setText(String.format("%.4f", liveEnergy));
				}
			}
		}
		catch (Exception e) {
			logger.error("Unable to update UI {} value.", scannable.getName());
		}
	}

	@Override
	protected void initialisation() {
		super.initialisation();

		dcmenergy = Finder.find("dcmenergy");
		if (dcmenergy == null) {
			logger.error("Finder failed to find 'dcmenergy'");
		} else {
			dcmenergy.addIObserver(this);

			try {
				hardXRayEnergy = (double) dcmenergy.getPosition() * 1000; // eV
			} catch (DeviceException e) {
				logger.error("Cannot get X-ray energy from DCM.", e);
			}
		}
		pgmenergy = Finder.find("pgmenergy");
		if (pgmenergy == null) {
			logger.error("Finder failed to find 'pgmenergy'");
		} else {
			pgmenergy.addIObserver(this);
			try {
				softXRayEnergy = (double) pgmenergy.getPosition();
			} catch (DeviceException e) {
				logger.error("Cannot get X-ray energy from PGM.", e);
			}
		}

		if (regionDefinitionResourceUtil.isSourceSelectable()) {
			btnHard.addSelectionListener(xRaySourceSelectionListener);
			btnSoft.addSelectionListener(xRaySourceSelectionListener);
		}

		progressComposite.setCurrentIterationRemainingTimePV(getCurrentIterationRemainingTimePV());
		progressComposite.setIterationLeadPointsPV(getIterationLeadPointsPV());
		progressComposite.setIterationProgressPV(getIterationProgressPV());
		progressComposite.setTotalDataPointsPV(getTotalDataPointsPV());
		progressComposite.setIterationCurrentPointPV(getIterationCurrentPointPV());
		progressComposite.setTotalRemianingTimePV(getTotalRemianingTimePV());
		progressComposite.setTotalProgressPV(getTotalProgressPV());
		progressComposite.setTotalPointsPV(getTotalPointsPV());
		progressComposite.setCurrentPointPV(getCurrentPointPV());
		progressComposite.setCurrentIterationPV(getCurrentIterationPV());
		progressComposite.setTotalIterationsPV(getTotalIterationsPV());
		progressComposite.initialise();

		analyserComposite.setStatePV(statePV);
		analyserComposite.setAcquirePV(acquirePV);
		analyserComposite.setMessagePV(messagePV);
		analyserComposite.setZeroSuppliesPV(zeroSuppliesPV);
		analyserComposite.initialise();
	}

	private SelectionAdapter xRaySourceSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			onSelectEnergySource(source);
		}
	};

	protected void onSelectEnergySource(Object source) {
		try {
			if (source.equals(btnHard) && btnHard.getSelection()) {
				hardXRayEnergy = (double) getDcmEnergy().getPosition() * 1000;
				updateAllRegionsWithNewExcitationEnergyUpdate(hardXRayEnergy, softXRayEnergy, true);
			} else if (source.equals(btnSoft) && btnSoft.getSelection()){
				softXRayEnergy = (double) getPgmEnergy().getPosition();
				updateAllRegionsWithNewExcitationEnergyUpdate(hardXRayEnergy, softXRayEnergy, true);
			}
		}
		catch (DeviceException e) {
			logger.error("Cannot get updated excitation energy when selecting energy source", e);
		}
	}

	private void updateAllRegionsWithNewExcitationEnergyUpdate(double hardEnergy, double softEnergy, boolean isFromExcitationEnergyMoving) {
		double energy;
		for (Region r : regions) {
			energy = hardEnergy;
			if (regionDefinitionResourceUtil.isSourceSelectable()) {
				energy = r.getExcitationEnergy() > regionDefinitionResourceUtil.getXRaySourceEnergyLimit() ? hardEnergy : softEnergy;
			}
			if (r.getExcitationEnergy() != energy) {
				updateFeature(r, RegiondefinitionPackage.eINSTANCE.getRegion_ExcitationEnergy(), energy);
				fireSelectionChanged(new EnergyChangedSelection(r, isFromExcitationEnergyMoving));
			}
		}

		txtHardEnergy.setText(String.format("%.4f", hardEnergy));
		excitationEnergy = hardEnergy;
		if (regionDefinitionResourceUtil.isSourceSelectable()) {
			txtSoftEnergy.setText(String.format("%.4f", softEnergy));
			excitationEnergy = btnHard.getSelection() ? hardEnergy : softEnergy;
		}
	}

	@Override
	protected void updateSpectrumEnergyFields() {
		double low = Double.parseDouble(txtSpectrumEnergyLow.getText());
		double high = Double.parseDouble(txtSpectrumEnergyHigh.getText());
		double center = Double.parseDouble(txtSpectrumEnergyCentre.getText());

		txtSpectrumEnergyLow.setText(String.format("%.4f", excitationEnergy - high));
		txtSpectrumEnergyHigh.setText(String.format("%.4f", (excitationEnergy - low)));
		txtSpectrumEnergyCentre.setText(String.format("%.4f", (excitationEnergy - center)));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(), Double.parseDouble(txtSpectrumEnergyLow.getText()));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(), Double.parseDouble(txtSpectrumEnergyHigh.getText()));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_FixEnergy(), Double.parseDouble(txtSpectrumEnergyCentre.getText()));
	}

	@Override
	protected void setExcitationEnergy(final Region region) {
		if (regionDefinitionResourceUtil.isSourceSelectable()) {
			if (region.getExcitationEnergy() > regionDefinitionResourceUtil.getXRaySourceEnergyLimit()) {
				btnHard.setSelection(true);
				btnSoft.setSelection(false);
				excitationEnergy = hardXRayEnergy;
			} else {
				btnHard.setSelection(false);
				btnSoft.setSelection(true);
				excitationEnergy = softXRayEnergy;
			}
			txtHardEnergy.setText(String.format("%.4f", hardXRayEnergy));
			txtSoftEnergy.setText(String.format("%.4f", softXRayEnergy));
		} else {
			excitationEnergy = hardXRayEnergy;
			txtHardEnergy.setText(String.format("%.4f", hardXRayEnergy));
		}
	}

	@Override
	public void update(Object source, Object arg) {

		// Cast the update
		Findable adaptor = (Findable) source; // Findable so we can getName

		// Check if any move has just completed. If not return
		if (arg == ScannableStatus.IDLE) {
			// Check if update is from dcm or pgm and cached values in fields
			if (adaptor.getName().equals("dcmenergy")) {
				try {
					hardXRayEnergy = (double) dcmenergy.getPosition() * 1000; // eV
					logger.debug("Got new hard xray energy: {} eV", hardXRayEnergy);
				} catch (DeviceException e) {
					logger.error("Cannot get X-ray energy from DCM.", e);
				}
			}

			double cachedSoftXRayEnergy = softXRayEnergy;

			if (adaptor.getName().equals("pgmenergy")) {
				try {
					softXRayEnergy = (double) pgmenergy.getPosition();
				} catch (DeviceException e) {
					logger.error("Cannot get X-ray energy from PGM.", e);
				}
			}

			//TODO pgmenergy fluctuates around a point. Use case for this is it receives a single IDLE update when scannable finished moving.
			//However, we get other unnecessary updates from ScannableMotor.handleMotorUpdates -> MotorStatus.READY which passes on to here as
			//ScannableStatus.IDLE so we cannot distinguish between events MotorEvent.MOVE_COMPLETE and MotorStatus.READY.
			//Causes file to be dirty when it shouldn't as pgmenergy position changes a small amount each time. Temp fix is to
			//only update file with new value if above/below small tolerance level.
			//However there should be a better way to do this in the future.
			if (softXRayEnergy <= cachedSoftXRayEnergy + pgmEnergyDetectChangeGuiToleranceLevel && softXRayEnergy >= cachedSoftXRayEnergy - pgmEnergyDetectChangeGuiToleranceLevel) {
				softXRayEnergy = cachedSoftXRayEnergy;
			}
			else {
				logger.debug("Got new soft x-ray energy: {} eV", softXRayEnergy);
			}

			// Update the GUI in UI thread
			Display display = regionComposite.getDisplay();
			if (!display.isDisposed()) {
				display.asyncExec(() -> {
					updateAllRegionsWithNewExcitationEnergyUpdate(hardXRayEnergy, softXRayEnergy, true);
				});
			}
		}
	}

	public String getCurrentIterationRemainingTimePV() {
		return currentIterationRemainingTimePV;
	}

	public void setCurrentIterationRemainingTimePV(
			String currentIterationRemainingTimePV) {
		this.currentIterationRemainingTimePV = currentIterationRemainingTimePV;
	}

	public String getIterationLeadPointsPV() {
		return iterationLeadPointsPV;
	}

	public void setIterationLeadPointsPV(String iterationLeadPointsPV) {
		this.iterationLeadPointsPV = iterationLeadPointsPV;
	}

	public String getIterationProgressPV() {
		return iterationProgressPV;
	}

	public void setIterationProgressPV(String iterationProgressPV) {
		this.iterationProgressPV = iterationProgressPV;
	}

	public String getTotalDataPointsPV() {
		return totalDataPointsPV;
	}

	public void setTotalDataPointsPV(String totalDataPointsPV) {
		this.totalDataPointsPV = totalDataPointsPV;
	}

	public String getIterationCurrentPointPV() {
		return iterationCurrentPointPV;
	}

	public void setIterationCurrentPointPV(String iterationCurrentPointPV) {
		this.iterationCurrentPointPV = iterationCurrentPointPV;
	}

	public String getTotalRemianingTimePV() {
		return totalRemianingTimePV;
	}

	public void setTotalRemianingTimePV(String totalRemianingTimePV) {
		this.totalRemianingTimePV = totalRemianingTimePV;
	}

	public String getTotalProgressPV() {
		return totalProgressPV;
	}

	public void setTotalProgressPV(String totalProgressPV) {
		this.totalProgressPV = totalProgressPV;
	}

	public String getTotalPointsPV() {
		return totalPointsPV;
	}

	public void setTotalPointsPV(String totalPointsPV) {
		this.totalPointsPV = totalPointsPV;
	}

	public String getCurrentPointPV() {
		return currentPointPV;
	}

	public void setCurrentPointPV(String currentPointPV) {
		this.currentPointPV = currentPointPV;
	}

	public String getCurrentIterationPV() {
		return currentIterationPV;
	}

	public void setCurrentIterationPV(String currentIterationPV) {
		this.currentIterationPV = currentIterationPV;
	}

	public String getTotalIterationsPV() {
		return totalIterationsPV;
	}

	public void setTotalIterationsPV(String totalIterationsPV) {
		this.totalIterationsPV = totalIterationsPV;
	}

	public String getStatePV() {
		return statePV;
	}

	public void setStatePV(String statePV) {
		this.statePV = statePV;
	}

	public String getAcquirePV() {
		return acquirePV;
	}

	public void setAcquirePV(String acquirePV) {
		this.acquirePV = acquirePV;
	}

	public String getMessagePV() {
		return messagePV;
	}

	public void setMessagePV(String messagePV) {
		this.messagePV = messagePV;
	}

	public String getZeroSuppliesPV() {
		return zeroSuppliesPV;
	}

	public void setZeroSuppliesPV(String zeroSuppliesPV) {
		this.zeroSuppliesPV = zeroSuppliesPV;
	}

	public void setDcmEnergy(Scannable energy) {
		this.dcmenergy = energy;
	}

	public Scannable getDcmEnergy() {
		return this.dcmenergy;
	}

	public void setPgmEnergy(Scannable energy) {
		this.pgmenergy = energy;
	}

	public Scannable getPgmEnergy() {
		return this.pgmenergy;
	}

	public void setPgmEnergyDetectChangeGuiToleranceLevel(double pgmenergyDetectChangeGuiToleranceLevel) {
		this.pgmEnergyDetectChangeGuiToleranceLevel = pgmenergyDetectChangeGuiToleranceLevel;
	}

	public double getPgmEnergyDetectChangeGuiToleranceLevel() {
		return pgmEnergyDetectChangeGuiToleranceLevel;
	}
}
