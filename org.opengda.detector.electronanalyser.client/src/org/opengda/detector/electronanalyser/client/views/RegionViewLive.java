package org.opengda.detector.electronanalyser.client.views;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.PageBook;
import org.opengda.detector.electronanalyser.client.selection.CanEditRegionSelection;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableStatus;
import gda.factory.Findable;
import gda.observable.IObserver;
import uk.ac.diamond.daq.concurrent.Async;

/**
 * A Region Editor View for defining new or editing existing Region Definition for VG Scienta Electron Analyser.
 *
 * @author fy65
 *
 */
public class RegionViewLive extends RegionViewCreator implements ISelectionProvider {

	public static final String ID = "org.opengda.detector.electronanalyser.client.regioneditor";
	protected static final Logger logger = LoggerFactory.getLogger(RegionViewLive.class);

	private Scannable dcmenergy;
	private Scannable pgmenergy;
	Future<?> movingDcmMonitor;
	Future<?> movingPgmMonitor;
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
		spinnerNumberOfIterations.setEnabled(enabled);
		spinnerNumberOfYSlices.setEnabled(enabled);
		btnFixed.setEnabled(enabled);
		btnSwept.setEnabled(enabled);
		//Excitation energy and mode
		btnHard.setEnabled(enabled);
		btnSoft.setEnabled(enabled);
		btnBinding.setEnabled(enabled);
		btnKinetic.setEnabled(enabled);
		//Spectrum energy range
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

	@Override
	protected void initialisation() {
		super.initialisation();

		initialiseLiveExcitationEnergy();

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

	private void updateExcitationEnergyUIWhileMoving(Scannable scannable, Text ui) {
		try {
			if (scannable.isBusy()) {
				double liveEnergy = scannable.getName().equals(getDcmEnergy().getName()) ? (double) getDcmEnergy().getPosition() : (double) getPgmEnergy().getPosition();
				regionComposite.getDisplay().asyncExec(() -> {
					final double uiEnergy = Double.parseDouble(ui.getText());
					if (uiEnergy != liveEnergy) {
						ui.setText(String.format(FORMAT_FLOAT, liveEnergy));
					}
				});
			}
		}
		catch (Exception e) {
			logger.error("Unable to update UI {} value.", scannable.getName(), e);
		}
	}

	@Override
	protected void loadRegionExcitationEnergies(List<Region> listOfRegions) {
		//Do nothing - we don't want to use sequence file excitation energy, use live ones
	}

	protected void initialiseLiveExcitationEnergy() {
		try {
			if (getDcmEnergy() == null) {
				logger.error("Error in configuration, dcmenergy is null!");
				return;
			}
			IObserver updater = this::updateExcitaitonEnergy;
			getDcmEnergy().addIObserver(updater);
			regionComposite.addDisposeListener(e -> getDcmEnergy().deleteIObserver(updater));
			hardXRayEnergy =  (double) getDcmEnergy().getPosition();

			txtHardExcitationEnergy.setEnabled(false);
			txtHardExcitationEnergy.setEditable(false);

			movingDcmMonitor = Async.scheduleAtFixedRate(() -> {
				if (!regionComposite.isDisposed()) {
					updateExcitationEnergyUIWhileMoving(getDcmEnergy(), txtHardExcitationEnergy);
				}
				else {
					movingDcmMonitor.cancel(true);
					logger.debug("moving dcmenergy monitor has closed.");
				}
			}, 2, 2, TimeUnit.SECONDS);

			if (regionDefinitionResourceUtil.isSourceSelectable()) {
				if (getPgmEnergy() == null) {
					logger.error("Error in configuration, pgmenergy is null!");
					return;
				}
				getPgmEnergy().addIObserver(updater);
				regionComposite.addDisposeListener(e -> getPgmEnergy().deleteIObserver(updater));
				softXRayEnergy =  (double) getPgmEnergy().getPosition();

				txtSoftExcitationEnergy.setEnabled(false);
				txtSoftExcitationEnergy.setEditable(false);

				//Only if pgmenergy is moving, update UI values.
				movingPgmMonitor = Async.scheduleAtFixedRate(() -> {
					if (!regionComposite.isDisposed()) {
						updateExcitationEnergyUIWhileMoving(getPgmEnergy(), txtSoftExcitationEnergy);
					}
					else {
						movingPgmMonitor.cancel(true);
						logger.debug("moving pgmenergy monitor has closed.");
					}
				}, 2, 2, TimeUnit.SECONDS);
			}
		}
		catch (DeviceException e) {
			logger.error("Failed to get X-ray energy position for initialisation.", e);
		}
	}

	@Override
	protected void onSelectEnergySource(Object source) {
		try {
			boolean canUndoCommand = false;
			if (source.equals(btnHard) && btnHard.getSelection()) {
				hardXRayEnergy = (double) getDcmEnergy().getPosition();
				addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_ExcitationEnergy(), hardXRayEnergy, region.getExcitationEnergy());
				updateAllRegionsWithNewExcitationEnergyUpdate(hardXRayEnergy, softXRayEnergy, canUndoCommand);
			} else if (source.equals(btnSoft) && btnSoft.getSelection()){
				softXRayEnergy =  (double) getPgmEnergy().getPosition();
				addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_ExcitationEnergy(), softXRayEnergy, region.getExcitationEnergy());
				updateAllRegionsWithNewExcitationEnergyUpdate(hardXRayEnergy, softXRayEnergy, canUndoCommand);
			}
		}
		catch (DeviceException e) {
			logger.error("Cannot get updated excitation energy when selecting energy source", e);
		}
	}

	@Override
	protected void setupInitialExcitationEnergyUI(final Region region) {
		if (regionDefinitionResourceUtil.isSourceSelectable()) {
			if (region.getExcitationEnergy() > regionDefinitionResourceUtil.getXRaySourceEnergyLimit()) {
				btnHard.setSelection(true);
				btnSoft.setSelection(false);
			} else {
				btnHard.setSelection(false);
				btnSoft.setSelection(true);
			}
			txtSoftExcitationEnergy.setText(String.format(FORMAT_FLOAT, softXRayEnergy));
		}
		txtHardExcitationEnergy.setText(String.format(FORMAT_FLOAT, hardXRayEnergy));
	}

	@Override
	protected void onModifyExcitationEnergy(SelectionEvent e) {
		//Make empty as excitation energy is updated via update(Object, Object) method for live.
	}

	private void updateExcitaitonEnergy(Object source, Object arg) {
		// Cast the update
		Findable adaptor = (Findable) source; // Findable so we can getName

		// Check if any move has just completed. If not return
		if (arg == ScannableStatus.IDLE) {
			double cachedSoftXRayEnergy = softXRayEnergy;
			try {
				// Check if update is from dcm or pgm and cached values in fields
				if (adaptor.getName().equals(dcmenergy.getName())) {
					hardXRayEnergy = (double) getDcmEnergy().getPosition();
					logger.debug("Got new hard xray energy: {} eV", hardXRayEnergy);
				}
				else if (adaptor.getName().equals(pgmenergy.getName())) {
					softXRayEnergy = (double) getPgmEnergy().getPosition();
				}
			}
			catch (DeviceException e) {
				logger.error("Cannot get X-ray energy from {}", adaptor.getName(), e);
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
				display.asyncExec(() -> updateAllRegionsWithNewExcitationEnergyUpdate(hardXRayEnergy, softXRayEnergy, false));
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
