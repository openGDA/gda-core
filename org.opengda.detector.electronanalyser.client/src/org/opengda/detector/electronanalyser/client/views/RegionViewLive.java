package org.opengda.detector.electronanalyser.client.views;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
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
import org.eclipse.ui.part.PageBook;
import org.opengda.detector.electronanalyser.client.selection.EnergyChangedSelection;
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

	public RegionViewLive() {
		setTitleToolTip("Edit parameters for selected region");
		// setContentDescription("A view for editing region parameters");
		setPartName("Region Editor");
		this.selectionChangedListeners = new ArrayList<>();

		setSequenceViewID(SequenceViewLive.ID);
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
		}
		pgmenergy = Finder.find("pgmenergy");
		if (pgmenergy == null) {
			logger.error("Finder failed to find 'pgmenergy'");
		} else {
			pgmenergy.addIObserver(this);
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
			if (source.equals(btnHard)) {
				excitationEnergy = (double) getDcmEnergy().getPosition() * 1000;
				txtHardEnergy.setText(String.format("%.4f", excitationEnergy));
			} else if (source.equals(btnSoft)) {
				excitationEnergy = (double) getPgmEnergy().getPosition();
				txtSoftEnergy.setText(String.format("%.4f", excitationEnergy));
			}
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_ExcitationEnergy(), excitationEnergy);
		} catch (DeviceException e) {
			logger.error("Cannot set excitation energy", e);
		}
	}

	@Override
	protected void updateEnergyFields() {
		double low = Double.parseDouble(txtLow.getText());
		double high = Double.parseDouble(txtHigh.getText());
		double center = Double.parseDouble(txtCenter.getText());
		excitationEnergy = getExcitationEnery(); //update this value from beamline
		txtLow.setText(String.format("%.4f", excitationEnergy - high));
		txtHigh.setText(String.format("%.4f", (excitationEnergy - low)));
		txtCenter.setText(String.format("%.4f", (excitationEnergy - center)));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(), Double.parseDouble(txtLow.getText()));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(), Double.parseDouble(txtHigh.getText()));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_FixEnergy(), Double.parseDouble(txtCenter.getText()));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_ExcitationEnergy(), excitationEnergy);
	}

	private double getExcitationEnery() {
		try {
			if (btnHard.getSelection()) {
				excitationEnergy = (double) getDcmEnergy().getPosition() * 1000;
				txtHardEnergy.setText(String.format("%.4f", excitationEnergy));
			} else if (btnSoft.getSelection()) {
				excitationEnergy = (double) getPgmEnergy().getPosition();
				txtSoftEnergy.setText(String.format("%.4f", excitationEnergy));
			}
		} catch (DeviceException e) {
			logger.error("Cannot set excitation energy", e);
		}
		return excitationEnergy;
	}

	@Override
	protected void setExcitationEnergy(final Region region) {
		if (regionDefinitionResourceUtil.isSourceSelectable()) {
			if (region.getExcitationEnergy() > regionDefinitionResourceUtil.getXRaySourceEnergyLimit()) {
				btnHard.setSelection(true);
				btnSoft.setSelection(false);
				if (dcmenergy != null) {
					try {
						hardXRayEnergy = (double) dcmenergy.getPosition() * 1000; // eV
					} catch (DeviceException e) {
						logger.error("Cannot get X-ray energy from DCM.", e);
					}
				}
				excitationEnergy = hardXRayEnergy;
				txtHardEnergy.setText(String.format("%.4f", hardXRayEnergy));
				if (pgmenergy != null) {
					try {
						softXRayEnergy = (double) pgmenergy.getPosition();
					} catch (DeviceException e) {
						logger.error("Cannot get X-ray energy from PGM.", e);
					}
				}
				txtSoftEnergy.setText(String.format("%.4f", softXRayEnergy));
			} else {
				btnHard.setSelection(false);
				btnSoft.setSelection(true);
				if (dcmenergy != null) {
					try {
						hardXRayEnergy = (double) dcmenergy.getPosition() * 1000; // eV
					} catch (DeviceException e) {
						logger.error("Cannot get X-ray energy from DCM.", e);
					}
				}
				txtHardEnergy.setText(String.format("%.4f", hardXRayEnergy));
				if (pgmenergy != null) {
					try {
						softXRayEnergy = (double) pgmenergy.getPosition();
					} catch (DeviceException e) {
						logger.error("Cannot get X-ray energy from PGM.", e);
					}
				}
				excitationEnergy = softXRayEnergy;
				txtSoftEnergy.setText(String.format("%.4f", softXRayEnergy));
			}
		} else {
			if (dcmenergy != null) {
				try {
					hardXRayEnergy = (double) dcmenergy.getPosition() * 1000;
				} catch (DeviceException e) {
					logger.error("Cannot get X-ray energy from DCM.", e);
				}
			}
			excitationEnergy = hardXRayEnergy;
			txtHardEnergy.setText(String.format("%.4f", hardXRayEnergy));
		}
	}

	@Override
	public void update(Object source, Object arg) {

		// Cast the update
		Findable adaptor = (Findable) source; // Findable so we can getName
		ScannableStatus status = (ScannableStatus) arg;

		// Check if any move has just completed. If not return
		if (status != ScannableStatus.IDLE) {
			return;
		}

		// Check if update is from dcm or pgm and cached values in fields
		if (adaptor.getName().equals("dcmenergy")) {
			try {
				hardXRayEnergy = (double) dcmenergy.getPosition() * 1000; // eV
				logger.debug("Got new hard xray energy: {} eV", hardXRayEnergy);
			} catch (DeviceException e) {
				logger.error("Cannot get X-ray energy from DCM.", e);
			}
		}
		if (adaptor.getName().equals("pgmenergy")) {

			try {
				softXRayEnergy = (double) pgmenergy.getPosition();
				logger.debug("Got new soft xray energy: {} eV", softXRayEnergy);

			} catch (DeviceException e) {
				logger.error("Cannot get X-ray energy from PGM.", e);
			}

		}
		// Update the GUI in UI thread
		Display display = getViewSite().getShell().getDisplay();

		if (!display.isDisposed()) {
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					if (regionDefinitionResourceUtil.isSourceSelectable()) {
						txtHardEnergy.setText(String.format("%.4f", hardXRayEnergy));
						txtSoftEnergy.setText(String.format("%.4f", softXRayEnergy));

						if (btnHard.getSelection()) {
							excitationEnergy = hardXRayEnergy;
						}
						else {
							excitationEnergy = softXRayEnergy;
						}
						for (Region r : regions) {
							if (r.getExcitationEnergy() > regionDefinitionResourceUtil.getXRaySourceEnergyLimit()) {
								updateFeature(r, RegiondefinitionPackage.eINSTANCE.getRegion_ExcitationEnergy(), hardXRayEnergy);
							}
							else{
								updateFeature(r, RegiondefinitionPackage.eINSTANCE.getRegion_ExcitationEnergy(), softXRayEnergy);
							}
							fireSelectionChanged(new EnergyChangedSelection(r, true));
						}
					}
					else {
						txtHardEnergy.setText(String.format("%.4f", excitationEnergy));
						for (Region r : regions) {
							updateFeature(r, RegiondefinitionPackage.eINSTANCE.getRegion_ExcitationEnergy(), excitationEnergy);
							fireSelectionChanged(new EnergyChangedSelection(r, true));
						}
					}
				}
			});
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
}
