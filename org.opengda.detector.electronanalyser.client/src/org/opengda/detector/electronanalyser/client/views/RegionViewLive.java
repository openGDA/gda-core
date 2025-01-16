package org.opengda.detector.electronanalyser.client.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.opengda.detector.electronanalyser.api.SESExcitationEnergySource;
import org.opengda.detector.electronanalyser.client.selection.CanEditRegionSelection;
import org.opengda.detector.electronanalyser.client.selection.ExcitationEnergyChangedSelection;
import org.opengda.detector.electronanalyser.client.selection.FileSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.observable.IObserver;

/**
 * A Region Editor View for defining new or editing existing Region Definition for VG Scienta Electron Analyser.
 *
 * @author fy65
 *
 */
public class RegionViewLive extends RegionViewCreator implements ISelectionProvider {

	public static final String ID = "org.opengda.detector.electronanalyser.client.regioneditor";
	private static final Logger logger = LoggerFactory.getLogger(RegionViewLive.class);

	private RegionProgressComposite progressComposite;
	private double scannableExcitationEnergyToleranceChange = 0.075;

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

	public RegionViewLive() {
		super();
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
	public void createAdditionalPartControlAreas(Composite parent) {
		createProgressArea(parent);
		createAnalyserArea(parent);
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

		((ScrolledComposite) rootComposite.getParent()).setMinSize(rootComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	@Override
	protected void initialisation() {
		super.initialisation();
		excitationEnergySelectorList.forEach(e -> addExcitationEnergyListeners(e.getScannable(), e.getText()));

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

	@Override
	protected void handleFileSelection(FileSelection fileSelection) {
		super.handleFileSelection(fileSelection);
		excitationEnergySelectorList.forEach(e -> setupInitialExcitationEnergyValue(e.getScannable(), e.getText()));
	}

	private void addExcitationEnergyListeners(Scannable scannable, Text textArea) {
		final Job job = new Job("Update " + scannable.getName() + " positioner readback value") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				return scannableMonitorExcitationEnergyJob(scannable, textArea, monitor);
			}
		};
		// Add an observer to the scannable to start the updateReadbackJob when an event occurs such as starting to move.
		final IObserver iObserver = (source, arg) -> {
			if (job.getState() != Job.RUNNING || job.getState() != Job.SLEEPING) {
				job.schedule();
			}
		};
		scannable.addIObserver(iObserver);
		textArea.addDisposeListener((e -> {
			scannable.deleteIObserver(iObserver);
			job.cancel();
		}));
		logger.info("Add listeners to {}", scannable.getName());
	}

	private void setupInitialExcitationEnergyValue(Scannable scannable, Text textArea) {
		try {
			final double newPosition = (double) scannable.getPosition();
			updateExcitationEnergyCachedPosition(scannable.getName(), newPosition);
			updateExcitationEnergyUIValues(textArea, newPosition, false);
		}
		catch(DeviceException e) {
			logger.error("Cannot get initial values", e);
		}
	}

	private IStatus scannableMonitorExcitationEnergyJob(Scannable scannable, Text textPosition, IProgressMonitor monitor) {
		try {
			final SESExcitationEnergySource excitationEnergySource = getSequence().getExcitationEnergySourceByScannableName(scannable.getName());
			double previousPosition = excitationEnergySource.getValue();
			Double newPosition = (Double) scannable.getPosition();
			//When a motor moves, we receive two updates, one while motor moving and one when motor finished moving.
			boolean fromIdleMotorUpdate = !scannable.isBusy();
			if(fromIdleMotorUpdate) {
				//However, we can receive external updates when motor is not busy.
				//Only allow updating of cache values and validation to happen if above/below threshold
				//so that the logs are not spammed with validation of regions.
				if (newPosition <= previousPosition + scannableExcitationEnergyToleranceChange && newPosition >= previousPosition - scannableExcitationEnergyToleranceChange) {
					return Status.OK_STATUS;
				} else {
					logger.info("Recieved IDLE update from {} which is outside the tolerance level {}. Updating cached excitation energy values...", scannable.getName(), scannableExcitationEnergyToleranceChange);
				}
			}
			else {
				logger.info("Recieved BUSY update from {}. Starting UI update thread to monitor changing position...", scannable.getName());
			}
			final boolean readOnly = true;
			boolean moving = true;
			while (moving) { // Loop which runs while scannable is moving
				moving = scannable.isBusy();
				// Check if the user has cancelled the job
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				// Pause to stop loop running to fast. ~ 20 Hz
				Thread.sleep(200);
				newPosition = (Double) scannable.getPosition();
				updateExcitationEnergyUIValues(textPosition, newPosition, readOnly);
			}
			if (newPosition == null) {
				throw new NullPointerException("Error getting new position for " + scannable.getName() + ". New position is null.");
			}
			final double finalNewPosition = newPosition;
			updateExcitationEnergyCachedPosition(scannable.getName(), finalNewPosition);
			logger.info("Finishing UI update thread for {}", scannable.getName());
			textPosition.getDisplay().asyncExec(() -> fireSelectionChanged(new ExcitationEnergyChangedSelection(excitationEnergySource.getName(), finalNewPosition)));
		} catch (DeviceException e) {
			logger.error("Error with scannable {} in update excitation energy UI thread", scannable.getName(), e);
			return Status.CANCEL_STATUS;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("Thread interrupted during update job for {}", scannable.getName(), e);
			return Status.CANCEL_STATUS; // Thread interrupted so cancel update job
		} catch (Exception e) {
			logger.error("An exception was thrown in update excitation energy UI thread.", e);
			return Status.CANCEL_STATUS; // Thread interrupted so cancel update job
		}
		return Status.OK_STATUS;
	}

	@Override
	protected double switchExcitationEnergySource(String name) {
		final Scannable scannable = getSequence().getExcitationEnergySourceByName(name).getScannable();
		try {
			return (double) scannable.getPosition();
		} catch (DeviceException e) {
			logger.error("Cannot get updated excitation energy when selecting energy source", e);
			return super.switchExcitationEnergySource(name);
		}
	}

	@Override
	protected void onModifyExcitationEnergy(SelectionEvent e) {
		//Make empty as excitation energy is updated via update(Object, Object) method for live.
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

	public double getScannableExcitationEnergyToleranceChange() {
		return scannableExcitationEnergyToleranceChange;
	}

	public void setScannableExcitationEnergyToleranceChange(double scannableExcitationEnergyToleranceChange) {
		this.scannableExcitationEnergyToleranceChange = scannableExcitationEnergyToleranceChange;
	}

	@Override
	protected boolean isExcitationEnergyReadOnly() {
		return true;
	}
}
