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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.util.Pair;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;
import org.opengda.detector.electronanalyser.api.SESConfigExcitationEnergySource;
import org.opengda.detector.electronanalyser.api.SESRegion;
import org.opengda.detector.electronanalyser.api.SESSequence;
import org.opengda.detector.electronanalyser.api.SESSettingsService;
import org.opengda.detector.electronanalyser.client.Camera;
import org.opengda.detector.electronanalyser.client.selection.CaptureSequenceSnapshot;
import org.opengda.detector.electronanalyser.client.selection.EnergyChangedSelection;
import org.opengda.detector.electronanalyser.client.selection.ExcitationEnergyChangedSelection;
import org.opengda.detector.electronanalyser.client.selection.FileSelection;
import org.opengda.detector.electronanalyser.client.selection.RegionValidationMessage;
import org.opengda.detector.electronanalyser.utils.RegionStepsTimeEstimation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import uk.ac.diamond.daq.pes.api.AnalyserEnergyRangeConfiguration;
import uk.ac.diamond.osgi.services.ServiceProvider;
import uk.ac.gda.devices.vgscienta.IVGScientaAnalyserRMI;

public class RegionViewCreator extends ViewPart implements ISelectionProvider {
	public static final String ID = "org.opengda.detector.electronanalyser.client.regioncreator";
	private static final Logger logger = LoggerFactory.getLogger(RegionViewCreator.class);
	protected static final String FORMAT_FLOAT = "%.4f";

	private PageBook regionPageBook;
	private Composite plainComposite;
	private ScrolledComposite regionComposite;

	private Text txtRegionName;
	private ComboViewer comboViewerLensMode;
	private ComboViewer comboViewerPassEnergy;

	//Acquisition Configuration / Mode
	private Spinner spinnerNumberOfIterations;
	private Spinner spinnerNumberOfYSlices;
	private Button btnFixed;
	private Button btnSwept;

	//Excitation energy and mode
	protected List<ExcitationEnergySelector> excitationEnergySelectorList = new ArrayList<>();
	private Button btnBinding;
	private Button btnKinetic;

	//Spectrum energy range
	private Label lblSpectrumEnergyLow;
	private Label lblSpectrumEnergyHigh;
	private Text txtSpectrumEnergyLow;
	private Text txtSpectrumEnergyHigh;
	private Text txtSpectrumEnergyCentre;
	private Text txtSpectrumEnergyWidth;
	private double fixedSpectrumEnergyCentre;
	private double sweptLowEnergy;
	private double sweptHighEnergy;
	private HashMap<String, Pair<String,String>> regionSpectrumEnergyLimits = new HashMap<>();

	//Region error message
	private StyledText txtRegionStateValue;
	private HashMap<String, String> regionValidationMessages = new HashMap<>();

	//Step
	private Text txtStepEstimatedTotalTime;
	private Text txtStepSize;
	private Text txtStepTotalSteps;
	private Text txtStepFramesPerSecond;
	private Text txtMinimumSize;
	private Text txtStepTime;
	private Text txtStepMinimumTime;
	private Spinner spinnerStepFrames;
	private int sweptSlices;
	private double sweptStepSize;

	//Detector
	private Spinner spinnerYChannelFrom;
	private Spinner spinnerYChannelTo;
	private Spinner spinnerEnergyChannelFrom;
	private Spinner spinnerEnergyChannelTo;
	private Button btnPulseMode;
	private Button btnADCMode;

	private SESRegion region = null;
	private SESSequence sequence = null;

	private Camera camera;
	private IVGScientaAnalyserRMI analyser;

	private List<ISelectionChangedListener> selectionChangedListeners;

	private String sequenceViewID = SequenceViewCreator.ID;

	protected boolean canEdit = true;

	private ISelectionListener selectionListener = RegionViewCreator.this::detectSelectionListener;

	public RegionViewCreator() {
		setTitleToolTip("Edit parameters for selected region");
		// setContentDescription("A view for editing region parameters");
		setPartName("Region Editor");
		this.selectionChangedListeners = new ArrayList<>();
	}

	protected void detectSelectionListener(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof FileSelection fileSelection) {
			handleFileSelection(fileSelection);
		} else if (selection instanceof RegionValidationMessage valMessage){
			handleRegionValidationMessage(valMessage);
		} else if (selection instanceof IStructuredSelection sel) {
			if (StructuredSelection.EMPTY.equals(selection)) {
				region = null;
				regionPageBook.showPage(plainComposite);
			} else {
				Object firstElement = sel.getFirstElement();
				if (firstElement instanceof SESRegion r) {
					initialiseRegionView(r);
				}
				regionPageBook.showPage(regionComposite);
			}
		}
	}

	protected void handleFileSelection(FileSelection fileSelection) {
		sequence = fileSelection.getSequence();
		region = getSelectedRegionInSequenceView();
		if (region != null) setupInitialExcitationEnergyUI(region);
	}

	private void handleRegionValidationMessage(RegionValidationMessage valMessage) {
		final SESRegion targetRegion = valMessage.getRegion();
		final String message = valMessage.getMessage();
		regionValidationMessages.put(targetRegion.getRegionId(), message);

		String lowLimitTooltip = "";
		String highLimitTooltip = "";
		final Double spectrumEnergyLowLimit = valMessage.getSpectrumEnergyLowLimit();
		final Double spectrumEnergyHighLimit = valMessage.getSpectrumEnergyHighLimit();

		if (spectrumEnergyLowLimit != null) {
			lowLimitTooltip = "Lower limit = "
				+ (targetRegion.isEnergyModeBinding() ? String.format(FORMAT_FLOAT, getExcitationEnergy() - spectrumEnergyLowLimit) + " = Excitation Energy - " : "")
				+  String.format(FORMAT_FLOAT, spectrumEnergyLowLimit);
		}
		if (spectrumEnergyHighLimit != null) {
			highLimitTooltip = "Upper limit = "
				+ (targetRegion.isEnergyModeBinding() ? String.format(FORMAT_FLOAT, getExcitationEnergy() - spectrumEnergyHighLimit) + " = Excitation Energy - " : "")
				+ String.format(FORMAT_FLOAT, spectrumEnergyHighLimit);
		}
		regionSpectrumEnergyLimits.put(
			targetRegion.getRegionId(),
			new Pair<> (lowLimitTooltip, highLimitTooltip)
		);
		if (sequence != null) {
			final List<String> regionIds = sequence.getRegions().stream().map(r -> r.getRegionId()).toList();
			//Remove regions that no longer exist e.g ones deleted or sequence file changed
			regionValidationMessages.keySet().retainAll(regionIds);
			regionSpectrumEnergyLimits.keySet().retainAll(regionIds);
		}
		updateRegionValidationMessage(targetRegion);
	}

	private void updateRegionValidationMessage(SESRegion r) {
		final String regionId = r.getRegionId();
		final String lowLimit = regionSpectrumEnergyLimits.containsKey(regionId) ? regionSpectrumEnergyLimits.get(regionId).getKey() : "";
		final String highLimit = regionSpectrumEnergyLimits.containsKey(regionId) ? regionSpectrumEnergyLimits.get(regionId).getValue() : "";
		lblSpectrumEnergyLow.setToolTipText(lowLimit);
		lblSpectrumEnergyHigh.setToolTipText(highLimit);
		//Error message
		String message = "";
		if (regionValidationMessages.containsKey(regionId) && r.getStatus() == SESRegion.Status.INVALID) {
			message = regionValidationMessages.get(regionId);
		}
		if (region == r) {
			txtRegionStateValue.setText(message);
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

		final Composite rootComposite = new Composite(regionComposite, SWT.NONE);
		regionComposite.setContent(rootComposite);
		GridLayoutFactory.fillDefaults().margins(10, SWT.DEFAULT).spacing(2, 8).applyTo(rootComposite);

		createNameAndLensModeAndPassEnergyArea(rootComposite);
		createAcquisitionConfigurationAndModeArea(rootComposite);
		createExcitationEnergyAndEnergyModeArea(rootComposite);
		createSpectrumEnergyRangeArea(rootComposite);
		createRegionErrorBoxArea(rootComposite);
		createStepArea(rootComposite);
		createDetectorArea(rootComposite);
		createAdditionalPartControlAreas(rootComposite);

		getViewSite().setSelectionProvider(this);
		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(getSequenceViewID(), selectionListener);
		initialisation();
	}

	private void createNameAndLensModeAndPassEnergyArea(Composite rootComposite) {
		Composite grpTop = new Composite(rootComposite, SWT.NONE);
		grpTop.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpTop.setLayout(new GridLayout(3, false));
		grpTop.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		Group grpName = new Group(grpTop, SWT.NONE);
		grpName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpName.setText("Name");
		grpName.setLayout(new GridLayout(2, false));
		grpName.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		txtRegionName = new Text(grpName, SWT.BORDER | SWT.SINGLE);
		GridData namelayoutData = new GridData(GridData.FILL_HORIZONTAL);
		txtRegionName.setLayoutData(namelayoutData);
		txtRegionName.setToolTipText("Name to give to the region. Must be unique.");
		txtRegionName.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (region != null) txtRegionName.setText(region.getName());
			}
		});
		txtRegionName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				final String newName = txtRegionName.getText();
				final List<String> regionNames = sequence.getRegions().stream().map(SESRegion::getName).toList();

				if (containsIllegals(newName)) {
					openMessageBox("Region Name Error", "Region name '"+  newName + "' contains illegal character.", SWT.ICON_ERROR);
				}
				else if (regionNames.contains(newName)) {
					openMessageBox("Region Name Error", "Region name '"+ newName + "' cannot be the same as another one.", SWT.ICON_ERROR);
				}
				else {
					region.setName(newName);
					//Force region to be validated if name changes so that error message can be updated.
					fireSelectionChanged(new EnergyChangedSelection(region, false));
				}
			}
			private boolean containsIllegals(String toExamine) {
				Pattern pattern = Pattern.compile("[~#@*+%{}<>\\[\\]|\"/^]");
				Matcher matcher = pattern.matcher(toExamine);
				return matcher.find();
			}
		});
		Group grpLensMode = new Group(grpTop, SWT.NONE);
		grpLensMode.setText("Lens Mode");
		grpLensMode.setLayout(new GridLayout());
		grpLensMode.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		comboViewerLensMode = new ComboViewer(new CCombo(grpLensMode, SWT.READ_ONLY | SWT.BORDER | SWT.LEAD));
		comboViewerLensMode.getCCombo().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		comboViewerLensMode.getControl().setToolTipText("List of available modes to select");
		comboViewerLensMode.getCCombo().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				region.setLensMode(comboViewerLensMode.getCCombo().getText());
				fireSelectionChanged(new EnergyChangedSelection(region));
				fireSelectionChanged(new CaptureSequenceSnapshot());
			}
		});

		Group grpPassEnergy = new Group(grpTop, SWT.NONE);
		grpPassEnergy.setLayout(new GridLayout());
		grpPassEnergy.setText("Pass Energy");
		grpPassEnergy.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		comboViewerPassEnergy = new ComboViewer(new CCombo(grpPassEnergy, SWT.READ_ONLY | SWT.BORDER));
		comboViewerPassEnergy.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		comboViewerPassEnergy.getControl().setToolTipText("Select a pass energy to use");
		comboViewerPassEnergy.getCCombo().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String passEnergyFromCombo = comboViewerPassEnergy.getCCombo().getText();
				int passEnergyIntValue = Integer.parseInt(passEnergyFromCombo);
				txtMinimumSize.setText(String.format("%.3f", camera.getEnergyResolution() * passEnergyIntValue));
				region.setPassEnergy(passEnergyIntValue);
				updateEnergyStep();
				fireSelectionChanged(new EnergyChangedSelection(region));
				fireSelectionChanged(new CaptureSequenceSnapshot());
			}
		});

		grpTop.addControlListener(new ControlListener() {
			//Utilise space more efficiently, if large enough area they will be on same line.
			@Override
			public void controlResized(ControlEvent e) {
				int columns = 3;
				GridData gridData = new GridData();
				gridData.horizontalAlignment = GridData.FILL;
				gridData.horizontalSpan = 1;
				gridData.grabExcessHorizontalSpace = true;
				final int width = grpTop.getSize().x;
				if (width < 530) {
					columns = 2;
					gridData.horizontalSpan = 2;
					grpPassEnergy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					grpLensMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				}
				else {
					grpPassEnergy.setLayoutData(new GridData());
					grpLensMode.setLayoutData(new GridData());
				}
				grpName.setLayoutData(gridData);
				grpTop.setLayout(new GridLayout(columns, false));
				grpTop.requestLayout();
			}
			@Override
			public void controlMoved(ControlEvent e) {
				controlResized(e);
			}
		});
	}

	private void createAcquisitionConfigurationAndModeArea(Composite rootComposite) {
		Composite modeComposite = new Composite(rootComposite, SWT.None);
		GridDataFactory.fillDefaults().applyTo(modeComposite);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		modeComposite.setLayout(layout);

		Group grpRunMode = new Group(modeComposite, SWT.NONE);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		grpRunMode.setLayoutData(layoutData);
		grpRunMode.setLayout(new GridLayout(2, false));
		grpRunMode.setText("Acquisition Configuration");
		grpRunMode.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		Label lblLabelNumberOfIterations = new Label(grpRunMode, SWT.NONE);
		lblLabelNumberOfIterations.setText("Number of Iterations:");

		spinnerNumberOfIterations = new Spinner(grpRunMode, SWT.BORDER);
		spinnerNumberOfIterations.setMinimum(1);
		spinnerNumberOfIterations.setMaximum(Integer.MAX_VALUE);
		spinnerNumberOfIterations.setToolTipText("Set number of iterations required");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(spinnerNumberOfIterations);
		spinnerNumberOfIterations.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
					region.setIterations(spinnerNumberOfIterations.getSelection());
					updateTotalTime();
					fireSelectionChanged(new CaptureSequenceSnapshot());
			}
		});

		Label lblSclies = new Label(grpRunMode, SWT.NONE);
		lblSclies.setText("Number of Y Slices:");

		spinnerNumberOfYSlices = new Spinner(grpRunMode, SWT.BORDER);
		spinnerNumberOfYSlices.setToolTipText("Set number of slices required");
		spinnerNumberOfYSlices.setMinimum(1);
		spinnerNumberOfYSlices.setMaximum(camera.getCameraYSize());
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(spinnerNumberOfYSlices);
		spinnerNumberOfYSlices.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				region.setSlices(spinnerNumberOfYSlices.getSelection());
				fireSelectionChanged(new CaptureSequenceSnapshot());
			}
		});

		Group grpAcquisitionMode = new Group(modeComposite, SWT.NONE);
		grpAcquisitionMode.setText("Acquisition Mode");
		GridLayoutFactory.fillDefaults().margins(0, 8).applyTo(grpAcquisitionMode);
		grpAcquisitionMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpAcquisitionMode.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		btnSwept = new Button(grpAcquisitionMode, SWT.RADIO);
		btnSwept.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnSwept.setText("Swept");
		btnSwept.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(btnSwept) && !btnSwept.getSelection()) {
					return;
				}
				setToSweptMode();
				region.setEnergyStep(sweptStepSize);
				region.setTotalSteps(Integer.parseInt(txtStepTotalSteps.getText()));
				region.setTotalTime(Integer.parseInt(txtStepTotalSteps.getText()));
				region.setAcquisitionMode(SESRegion.SWEPT);
				fireSelectionChanged(new CaptureSequenceSnapshot());
			}
		});

		btnFixed = new Button(grpAcquisitionMode, SWT.RADIO);
		btnFixed.setText("Fixed");
		btnFixed.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(btnFixed) && !btnFixed.getSelection()) {
					return;
				}
				setToFixedMode();
				region.setEnergyStep(Double.parseDouble(txtMinimumSize.getText().trim()));
				region.setTotalSteps(Integer.parseInt(txtStepTotalSteps.getText()));
				region.setTotalTime(Integer.parseInt(txtStepTotalSteps.getText()));
				region.setAcquisitionMode(SESRegion.FIXED);
				fireSelectionChanged(new CaptureSequenceSnapshot());
			}
		});
	}

	private void createExcitationEnergyAndEnergyModeArea(Composite rootComposite) {
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
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(grpExcitationEnergy);

		SelectionAdapter txtExcitationEnergySelAdaptor = new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				onModifyExcitationEnergy(e);
			}
		};
		final SelectionAdapter xRaySourceSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final ExcitationEnergySelector excitationEnergySelected = excitationEnergySelectorList.stream().filter(x -> {
					final Button button = (Button) x.control;
					return e.getSource().equals(x.control) && button.getSelection();
				}).findFirst().orElse(null);
				if (excitationEnergySelected == null) return;
				excitationEnergySelected.getText().setEnabled(true);
				region.setExcitationEnergySource(excitationEnergySelected.getName());

				final List<ExcitationEnergySelector> excitiationEnergyUnselected = excitationEnergySelectorList.stream().filter(x -> !e.getSource().equals(x.control)).toList();
				final double newPosition = switchExcitationEnergySource(excitationEnergySelected.getName());
				updateExcitationEnergyCachedPosition(excitationEnergySelected.getScannable().getName(), newPosition);
				updateExcitationEnergyUIValues(excitationEnergySelected.getText(), newPosition, isExcitationEnergyReadOnly());

				excitiationEnergyUnselected.forEach(x -> x.getText().setEnabled(false));
				fireSelectionChanged(new EnergyChangedSelection(region));
				fireSelectionChanged(new CaptureSequenceSnapshot());
			}
		};
		final SESSettingsService settings = ServiceProvider.getService(SESSettingsService.class);
		final boolean useLabel = !settings.isExcitationEnergySourceSelectable();
		for (final SESConfigExcitationEnergySource config : settings.getSESConfigExcitationEnergySourceList()) {
			final ExcitationEnergySelector excitationEnergySelector = new ExcitationEnergySelector(config, grpExcitationEnergy, useLabel);
			excitationEnergySelectorList.add(excitationEnergySelector);
			if (!useLabel) {
				final Button button = (Button) excitationEnergySelector.getControl();
				button.addSelectionListener(xRaySourceSelectionListener);
			}
			excitationEnergySelector.getText().addSelectionListener(txtExcitationEnergySelAdaptor);
			excitationEnergySelector.getText().addFocusListener(FocusListener.focusLostAdapter(
				ev -> excitationEnergySelector.getText().setText(String.format(FORMAT_FLOAT, sequence.getExcitationEnergySourceByName(excitationEnergySelector.getName()).getValue()))
			));
		}

		Group grpEnergyMode = new Group(energyComposite, SWT.NONE);
		grpEnergyMode.setText("Energy Mode");
		GridLayoutFactory.fillDefaults().margins(0, 8).applyTo(grpEnergyMode);
		grpEnergyMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpEnergyMode.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		SelectionAdapter buttonEnergyModeSelAdaptor = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final boolean isKinetic = e.getSource().equals(btnKinetic) && btnKinetic.getSelection();
				final boolean isBinding = e.getSource().equals(btnBinding) && btnBinding.getSelection();
				if (isKinetic || isBinding) {
					region.setEnergyMode(isKinetic ? SESRegion.KINETIC : SESRegion.BINDING);
					final double low = Double.parseDouble(txtSpectrumEnergyLow.getText());
					final double high = Double.parseDouble(txtSpectrumEnergyHigh.getText());
					final double centre = Double.parseDouble(txtSpectrumEnergyCentre.getText());
					final double spectrumEnergyLow = getExcitationEnergy() - high;
					final double spectrumEnergyHigh = getExcitationEnergy() - low;
					final double spectrumEnergyCentre = getExcitationEnergy() - centre;
					txtSpectrumEnergyLow.setText(String.format(FORMAT_FLOAT, spectrumEnergyLow));
					txtSpectrumEnergyHigh.setText(String.format(FORMAT_FLOAT, spectrumEnergyHigh));
					txtSpectrumEnergyCentre.setText(String.format(FORMAT_FLOAT, spectrumEnergyCentre));
					region.setLowEnergy(spectrumEnergyLow);
					region.setHighEnergy(spectrumEnergyHigh);
					region.setFixEnergy(spectrumEnergyCentre);
					updateTotalSteps();
					fireSelectionChanged(new EnergyChangedSelection(region));
					fireSelectionChanged(new CaptureSequenceSnapshot());
				}
			}
		};
		btnKinetic = new Button(grpEnergyMode, SWT.RADIO);
		btnKinetic.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnKinetic.setText("Kinetic");
		btnKinetic.addSelectionListener(buttonEnergyModeSelAdaptor);

		btnBinding = new Button(grpEnergyMode, SWT.RADIO);
		btnBinding.setText("Binding");
		btnBinding.addSelectionListener(buttonEnergyModeSelAdaptor);
	}

	protected double switchExcitationEnergySource(String name) {
		return sequence.getExcitationEnergySourceByName(name).getValue();
	}

	private void createSpectrumEnergyRangeArea(Composite rootComposite) {
		final SelectionAdapter spectrumEnergyLimitSelection = new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				//If energy low above high or high below low, set them back to their original values
				final double lowEnergy = Double.parseDouble(txtSpectrumEnergyLow.getText());
				final double highEnergy = Double.parseDouble(txtSpectrumEnergyHigh.getText());
				if (lowEnergy > highEnergy) {
					txtSpectrumEnergyHigh.setText(String.valueOf(region.getHighEnergy()));
					txtSpectrumEnergyLow.setText(String.valueOf(region.getLowEnergy()));
				}
			}
		};

		final Group grpEnergy = new Group(rootComposite, SWT.NONE);
		grpEnergy.setText("Spectrum energy range [eV]");
		final GridData grpEnergyGridData = new GridData(GridData.FILL_HORIZONTAL);
		grpEnergyGridData.horizontalSpan = 2;
		grpEnergy.setLayoutData(grpEnergyGridData);
		grpEnergy.setLayout(new GridLayout(4, false));
		grpEnergy.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		lblSpectrumEnergyLow = new Label(grpEnergy, SWT.NONE);
		lblSpectrumEnergyLow.setText("Low");

		txtSpectrumEnergyLow = new Text(grpEnergy, SWT.BORDER | SWT.SINGLE);
		final GridData lowLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		txtSpectrumEnergyLow.setLayoutData(lowLayoutData);
		txtSpectrumEnergyLow.setToolTipText("Start energy");
		txtSpectrumEnergyLow.addSelectionListener(spectrumEnergyLimitSelection);
		txtSpectrumEnergyLow.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				txtSpectrumEnergyLow.setText(String.format(FORMAT_FLOAT, region.getLowEnergy()));
			}
		});

		final Label lblCenter = new Label(grpEnergy, SWT.NONE);
		lblCenter.setText("Center");

		txtSpectrumEnergyCentre = new Text(grpEnergy, SWT.BORDER | SWT.SINGLE);
		final GridData centerLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		txtSpectrumEnergyCentre.setLayoutData(centerLayoutData);
		txtSpectrumEnergyCentre.setToolTipText("Center/Fixed energy");
		txtSpectrumEnergyCentre.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				txtSpectrumEnergyCentre.setText(String.format(FORMAT_FLOAT, region.getFixEnergy()));
			}
		});

		lblSpectrumEnergyHigh = new Label(grpEnergy, SWT.NONE);
		lblSpectrumEnergyHigh.setText("High");

		txtSpectrumEnergyHigh = new Text(grpEnergy, SWT.BORDER | SWT.SINGLE);
		final GridData highLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		txtSpectrumEnergyHigh.setLayoutData(highLayoutData);
		txtSpectrumEnergyHigh.setToolTipText("Stop energy");
		txtSpectrumEnergyHigh.addSelectionListener(spectrumEnergyLimitSelection);
		txtSpectrumEnergyHigh.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				txtSpectrumEnergyHigh.setText(String.format(FORMAT_FLOAT, region.getHighEnergy()));
			}
		});

		final Label lblWidth = new Label(grpEnergy, SWT.NONE);
		lblWidth.setText("Width");

		txtSpectrumEnergyWidth = new Text(grpEnergy, SWT.BORDER | SWT.SINGLE);
		txtSpectrumEnergyWidth.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtSpectrumEnergyWidth.setToolTipText("Energy width");
		txtSpectrumEnergyWidth.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				txtSpectrumEnergyWidth.setText(String.format(FORMAT_FLOAT, region.getHighEnergy() - region.getLowEnergy()));
			}
		});

		txtSpectrumEnergyLow.addSelectionListener(spectrumEnergySelectionListener);
		txtSpectrumEnergyHigh.addSelectionListener(spectrumEnergySelectionListener);
		txtSpectrumEnergyCentre.addSelectionListener(spectrumEnergySelectionListener);
		txtSpectrumEnergyWidth.addSelectionListener(spectrumEnergySelectionListener);
	}

	private SelectionAdapter spectrumEnergySelectionListener = new SelectionAdapter() {
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			final Object source = e.getSource();
			if (source.equals(txtSpectrumEnergyLow) && txtSpectrumEnergyLow.isFocusControl()) {
				updateSpectrumEnergyFieldsWithNewValues(txtSpectrumEnergyLow);
			} else if (source.equals(txtSpectrumEnergyHigh) && txtSpectrumEnergyHigh.isFocusControl()) {
				updateSpectrumEnergyFieldsWithNewValues(txtSpectrumEnergyHigh);
			} else if (source.equals(txtSpectrumEnergyCentre) && txtSpectrumEnergyCentre.isFocusControl()) {
				final double centre = Double.parseDouble(txtSpectrumEnergyCentre.getText());
				final double width = Double.parseDouble(txtSpectrumEnergyWidth.getText());
				final double low = centre - width / 2;
				final double high = centre + width / 2;
				txtSpectrumEnergyLow.setText(String.format(FORMAT_FLOAT, low));
				txtSpectrumEnergyHigh.setText(String.format(FORMAT_FLOAT, high));
				txtSpectrumEnergyCentre.setText(String.format(FORMAT_FLOAT, centre));
				region.setLowEnergy(low);
				region.setHighEnergy(high);
				region.setFixEnergy(centre);
				updateTotalSteps();
			} else if (source.equals(txtSpectrumEnergyWidth) && txtSpectrumEnergyWidth.isFocusControl()) {
				final double centre = Double.parseDouble(txtSpectrumEnergyCentre.getText());
				final double width = Double.parseDouble(txtSpectrumEnergyWidth.getText());
				final double low = centre - width / 2;
				final double high = centre + width / 2;
				txtSpectrumEnergyLow.setText(String.format(FORMAT_FLOAT, low));
				txtSpectrumEnergyHigh.setText(String.format(FORMAT_FLOAT, high));
				txtSpectrumEnergyWidth.setText(String.format(FORMAT_FLOAT, width));
				region.setLowEnergy(low);
				region.setHighEnergy(high);
				updateTotalSteps();
			}
			if (btnSwept.getSelection()) {
				sweptLowEnergy = Double.parseDouble(txtSpectrumEnergyLow.getText());
				sweptHighEnergy = Double.parseDouble(txtSpectrumEnergyHigh.getText());
			}
			if (btnFixed.getSelection()) {
				fixedSpectrumEnergyCentre = Double.parseDouble(txtSpectrumEnergyCentre.getText());
			}
			fireSelectionChanged(new EnergyChangedSelection(region));
			fireSelectionChanged(new CaptureSequenceSnapshot());
		}

		private void updateSpectrumEnergyFieldsWithNewValues(Text txt) {
			final double low = Double.parseDouble(txtSpectrumEnergyLow.getText());
			final double high = Double.parseDouble(txtSpectrumEnergyHigh.getText());
			if (low > high) {
				txtSpectrumEnergyHigh.setText(String.format(FORMAT_FLOAT, low));
				txtSpectrumEnergyLow.setText(String.format(FORMAT_FLOAT, high));
			} else {
				txt.setText(String.format(FORMAT_FLOAT, Double.parseDouble(txt.getText())));
			}
			region.setLowEnergy(low);
			region.setHighEnergy(high);
			final double centre = (low + high) / 2;
			final double width = high - low;
			txtSpectrumEnergyCentre.setText(String.format(FORMAT_FLOAT, centre));
			region.setFixEnergy(centre);
			txtSpectrumEnergyWidth.setText(String.format(FORMAT_FLOAT, width));
			updateTotalSteps();
		}
	};

	private void createRegionErrorBoxArea(Composite rootComposite) {
		Group grpRegionValidation = new Group(rootComposite, SWT.NONE);
		grpRegionValidation.setText("Error message");
		GridDataFactory.fillDefaults().applyTo(grpRegionValidation);
		grpRegionValidation.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		grpRegionValidation.setLayout(new GridLayout());
		GridData grpLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		grpRegionValidation.setLayoutData(grpLayoutData);

		txtRegionStateValue = new StyledText(grpRegionValidation, SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
		txtRegionStateValue.setForeground(new Color(255,0,0));
		GridData txtRegionStateGridData = new GridData(GridData.FILL_BOTH);
		txtRegionStateGridData.heightHint = 2 * txtRegionStateValue.getLineHeight();
		txtRegionStateValue.setLayoutData(txtRegionStateGridData);
		int margin = 8;
		txtRegionStateValue.setMargins(margin, margin, margin, margin);
		txtRegionStateValue.setEditable(false);

		//Hide the region error message on initial load and reveal it only when needed
		grpRegionValidation.setVisible(false);
		grpLayoutData.exclude = true;
		txtRegionStateValue.setVisible(false);
		txtRegionStateGridData.exclude = true;

		ModifyListener validationListener = e -> {
			final GridData gridGrp = (GridData) grpRegionValidation.getLayoutData();
			final GridData gridTxt = (GridData) txtRegionStateValue.getLayoutData();
			boolean exclude = false;
			boolean visible = true;
			if (txtRegionStateValue.getText().isBlank()) {
				exclude = true;
				visible = false;
			}
			gridGrp.exclude = exclude;
			gridTxt.exclude = exclude;
			grpRegionValidation.setVisible(visible);
			txtRegionStateValue.setVisible(visible);
			regionComposite.setMinSize(rootComposite.computeSize(rootComposite.getBorderWidth(), SWT.DEFAULT));
			rootComposite.requestLayout();

			calculateRegionErrorBoxSize(grpRegionValidation.getSize().x);
		};
		txtRegionStateValue.addModifyListener(validationListener);

		//Resizes the text box to dynamically fit error message.
		ControlListener controlListener = new ControlListener() {
			@Override
			public void controlResized(ControlEvent e) {
				calculateRegionErrorBoxSize(grpRegionValidation.getSize().x);
			}
			@Override
			public void controlMoved(ControlEvent e) {
				controlResized(e);
			}
		};
		txtRegionStateValue.addControlListener(controlListener);
	}

	private void calculateRegionErrorBoxSize(int width) {
		final GridData gridData = (GridData) txtRegionStateValue.getLayoutData();
		if (width < 475) {
			gridData.heightHint = 4 * txtRegionStateValue.getLineHeight();
		}
		else if (width < 640) {
			gridData.heightHint = 3 * txtRegionStateValue.getLineHeight();
		}
		else {
			gridData.heightHint = 2 * txtRegionStateValue.getLineHeight();
		}
		txtRegionStateValue.setLayoutData(gridData);
		txtRegionStateValue.requestLayout();
		txtRegionStateValue.redraw();
	}

	private void createStepArea(Composite rootComposite) {
		final Group grpStep = new Group(rootComposite, SWT.NONE);
		grpStep.setText("Step");
		grpStep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpStep.setLayout(new GridLayout(4, false));
		grpStep.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		final Label lblFrames = new Label(grpStep, SWT.NONE);
		lblFrames.setText("Frames");

		spinnerStepFrames = new Spinner(grpStep, SWT.BORDER);
		spinnerStepFrames.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		spinnerStepFrames.setToolTipText("Number of frames per step");
		spinnerStepFrames.setMinimum(1);
		spinnerStepFrames.setMaximum(Integer.MAX_VALUE);
		SelectionAdapter framesSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final double stepTime = Double.parseDouble(txtStepMinimumTime.getText()) * Integer.parseInt(spinnerStepFrames.getText());
				txtStepTime.setText(String.format("%.3f", stepTime));
				region.setStepTime(stepTime);
				updateTotalTime();
				fireSelectionChanged(new CaptureSequenceSnapshot());
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		};
		spinnerStepFrames.addSelectionListener(framesSelectionListener);

		final Label lblFramesPerSecond = new Label(grpStep, SWT.NONE);
		lblFramesPerSecond.setText("Frames/s");

		txtStepFramesPerSecond = new Text(grpStep, SWT.BORDER);
		txtStepFramesPerSecond.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtStepFramesPerSecond.setToolTipText("Camera frame rate");
		txtStepFramesPerSecond.setEditable(false);
		txtStepFramesPerSecond.setEnabled(false);
		txtStepFramesPerSecond.setText(String.format("%d", camera.getFrameRate()));

		final Label lblTime = new Label(grpStep, SWT.NONE);
		lblTime.setText("Time [s]");

		txtStepTime = new Text(grpStep, SWT.BORDER | SWT.SINGLE);
		txtStepTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtStepTime.setToolTipText("Time per step");
		txtStepTime.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				double stepTime = Double.parseDouble(txtStepTime.getText());
				final double minStepTime = Double.parseDouble(txtStepMinimumTime.getText());
				if (stepTime < minStepTime) {
					stepTime = minStepTime;
				}
				txtStepTime.setText(String.format(FORMAT_FLOAT, stepTime));
				spinnerStepFrames.setSelection((int) calculateStepFrames());
				txtStepTime.setText(String.format("%.3f", stepTime));
				region.setStepTime(stepTime);
				updateTotalTime();
				fireSelectionChanged(new CaptureSequenceSnapshot());
			}
		});

		final Label lblMinimumTime = new Label(grpStep, SWT.NONE);
		lblMinimumTime.setText("Min. Time [s]");

		txtStepMinimumTime = new Text(grpStep, SWT.BORDER);
		txtStepMinimumTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtStepMinimumTime.setToolTipText("Minimum time per step allowed");
		txtStepMinimumTime.setEditable(false);
		txtStepMinimumTime.setEnabled(false);
		txtStepMinimumTime.setText(String.format("%f", 1.0 / camera.getFrameRate()));

		final Label lblSize = new Label(grpStep, SWT.NONE);
		lblSize.setText("Size [meV]");

		txtStepSize = new Text(grpStep, SWT.BORDER | SWT.SINGLE);
		txtStepSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtStepSize.setToolTipText("Energy size per step");
		final SelectionAdapter sizeSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				final double minStepSize = Double.parseDouble(txtMinimumSize.getText());
				double stepSize = Double.parseDouble(txtStepSize.getText());
				if (stepSize < minStepSize) {
					stepSize = minStepSize;
				}
				txtStepSize.setText(String.format("%.3f", stepSize));
				region.setEnergyStep(stepSize);
				updateTotalSteps();
				if (btnSwept.getSelection()) {
					sweptStepSize = stepSize;
				}
				fireSelectionChanged(new CaptureSequenceSnapshot());
			}
		};
		txtStepSize.addSelectionListener(sizeSelectionListener);

		final Label lblMinimumSize = new Label(grpStep, SWT.NONE);
		lblMinimumSize.setText("Min. Size [meV]");

		txtMinimumSize = new Text(grpStep, SWT.BORDER);
		txtMinimumSize.setToolTipText("Minimum energy size per step allowed");
		txtMinimumSize.setEditable(false);
		txtMinimumSize.setEnabled(false);
		txtMinimumSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label lblTotalTime = new Label(grpStep, SWT.NONE);
		lblTotalTime.setText("Estimated Time [s]");

		txtStepEstimatedTotalTime = new Text(grpStep, SWT.BORDER);
		txtStepEstimatedTotalTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtStepEstimatedTotalTime.setToolTipText("Anticipated total time for this collection");
		txtStepEstimatedTotalTime.setEditable(false);
		txtStepEstimatedTotalTime.setEnabled(false);

		final Label lblTotalSteps = new Label(grpStep, SWT.NONE);
		lblTotalSteps.setText("Total Steps");

		txtStepTotalSteps = new Text(grpStep, SWT.BORDER);
		txtStepTotalSteps.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtStepTotalSteps.setToolTipText("Total number of steps for this collection");
		txtStepTotalSteps.setEditable(false);
		txtStepTotalSteps.setEnabled(false);
	}

	private void createDetectorArea(Composite rootComposite) {
		Group grpDetector = new Group(rootComposite, SWT.NONE);
		grpDetector.setText("Detector");
		grpDetector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpDetector.setLayout(new GridLayout(5, false));
		grpDetector.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		Label lblXChannel = new Label(grpDetector, SWT.NONE);
		lblXChannel.setText("Energy Channels:");

		Label lblEnergyChannelFrom = new Label(grpDetector, SWT.NONE);
		lblEnergyChannelFrom.setText("From");

		spinnerEnergyChannelFrom = new Spinner(grpDetector, SWT.BORDER);
		spinnerEnergyChannelFrom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		spinnerEnergyChannelFrom.setToolTipText("Low bound");
		spinnerEnergyChannelFrom.setMinimum(1);
		spinnerEnergyChannelFrom.setMaximum(camera.getCameraXSize());
		spinnerEnergyChannelFrom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final int firstXChannel = spinnerEnergyChannelFrom.getSelection();
				region.setFirstXChannel(firstXChannel);
				if (btnFixed.getSelection()) {
					txtStepSize.setText(String.format("%.3f", getFixedEnergyRange()));
				}
				fireSelectionChanged(new CaptureSequenceSnapshot());
			}
		});

		Label lblEnergyChannelTo = new Label(grpDetector, SWT.NONE);
		lblEnergyChannelTo.setText("To");

		spinnerEnergyChannelTo = new Spinner(grpDetector, SWT.BORDER);
		spinnerEnergyChannelTo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		spinnerEnergyChannelTo.setToolTipText("High bound");
		spinnerEnergyChannelTo.setMinimum(1);
		spinnerEnergyChannelTo.setMaximum(camera.getCameraXSize());
		spinnerEnergyChannelTo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final int lastXChannel = spinnerEnergyChannelTo.getSelection();
				region.setLastXChannel(lastXChannel);
				if (btnFixed.getSelection()) {
					txtStepSize.setText(String.format("%.3f", getFixedEnergyRange()));
				}
				fireSelectionChanged(new CaptureSequenceSnapshot());
			}
		});

		Label lblYChannel = new Label(grpDetector, SWT.NONE);
		lblYChannel.setText("Y Channels:");

		Label lblYChannelFrom = new Label(grpDetector, SWT.NONE);
		lblYChannelFrom.setText("From");

		spinnerYChannelFrom = new Spinner(grpDetector, SWT.BORDER);
		spinnerYChannelFrom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		spinnerYChannelFrom.setToolTipText("Low bound");
		spinnerYChannelFrom.setMinimum(1);
		spinnerYChannelFrom.setMaximum(camera.getCameraYSize());
		spinnerYChannelFrom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final int firstYChannel = spinnerYChannelFrom.getSelection();
				region.setFirstYChannel(firstYChannel);
				fireSelectionChanged(new CaptureSequenceSnapshot());
			}
		});

		Label lblYChannelTo = new Label(grpDetector, SWT.NONE);
		lblYChannelTo.setText("To");

		spinnerYChannelTo = new Spinner(grpDetector, SWT.BORDER);
		spinnerYChannelTo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		spinnerYChannelTo.setToolTipText("High bound");
		spinnerYChannelTo.setMinimum(1);
		spinnerYChannelTo.setMaximum(camera.getCameraYSize());
		spinnerYChannelTo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final int lastYChannel = spinnerYChannelTo.getSelection();
				region.setLastYChannel(lastYChannel);
				fireSelectionChanged(new CaptureSequenceSnapshot());
			}
		});

		final SelectionAdapter spinnerYChannelSelectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final int maximum = spinnerYChannelTo.getSelection() - spinnerYChannelFrom.getSelection() + 1;
				spinnerNumberOfYSlices.setMaximum(maximum);
				fireSelectionChanged(new CaptureSequenceSnapshot());
			}
		};
		spinnerYChannelFrom.addSelectionListener(spinnerYChannelSelectionAdapter);
		spinnerYChannelTo.addSelectionListener(spinnerYChannelSelectionAdapter);

		Label lblDetectorMode = new Label(grpDetector, SWT.NONE);
		lblDetectorMode.setText("Mode:");

		new Label(grpDetector, SWT.NONE);

		btnADCMode = new Button(grpDetector, SWT.RADIO);
		btnADCMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnADCMode.setText("ADC");
		btnADCMode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!(e.getSource().equals(btnADCMode) && btnADCMode.getSelection())) {
					return;
				}
				region.setDetectorMode(SESRegion.ADC);
				fireSelectionChanged(new CaptureSequenceSnapshot());
			}
		});

		new Label(grpDetector, SWT.NONE);

		btnPulseMode = new Button(grpDetector, SWT.RADIO);
		btnPulseMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnPulseMode.setText("Pulse Counting");
		btnPulseMode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!(e.getSource().equals(btnPulseMode) && btnPulseMode.getSelection())) {
					return;
				}
				region.setDetectorMode(SESRegion.PULSE_COUNTING);
				fireSelectionChanged(new CaptureSequenceSnapshot());
			}
		});
	}

	protected void createAdditionalPartControlAreas(Composite parent) {
		//Do nothing, this is for other classes that extend to override and implement
	}

	private void updateEnergyStep() {
		if (btnSwept.getSelection()) {
			setToSweptMode();
			updateTotalSteps();
		}
		if (btnFixed.getSelection()) {
			setToFixedMode();
		}
	}

	private void setToSweptMode() {
		calculateSweptParameters();
		if (btnSwept.getSelection()) {
			updateTotalSteps();
		}
		final double centre = Double.parseDouble(txtSpectrumEnergyCentre.getText());
		region.setLowEnergy(sweptLowEnergy);
		region.setHighEnergy(sweptHighEnergy);
		region.setFixEnergy(centre);
		region.setEnergyStep(sweptStepSize);
		region.setSlices(spinnerNumberOfYSlices.getSelection());
	}

	private void calculateSweptParameters() {
		toggleSweptModeParameters(canEdit);
		// restore the original energy step size for the SWEPT
		txtSpectrumEnergyLow.setText(String.format(FORMAT_FLOAT, sweptLowEnergy));
		txtSpectrumEnergyHigh.setText(String.format(FORMAT_FLOAT, sweptHighEnergy));
		txtSpectrumEnergyCentre.setText(String.format(FORMAT_FLOAT, (sweptLowEnergy + sweptHighEnergy) / 2));
		txtSpectrumEnergyWidth.setText(String.format(FORMAT_FLOAT, (sweptHighEnergy - sweptLowEnergy)));
		txtStepSize.setText(String.format("%.3f", sweptStepSize));
		spinnerNumberOfYSlices.setSelection(sweptSlices);
		if (txtStepSize.getText().isEmpty() || (Double.parseDouble(txtStepSize.getText()) < Double.parseDouble(txtMinimumSize.getText()))) {
			sweptStepSize = Double.parseDouble(txtMinimumSize.getText());
			txtStepSize.setText(String.format("%.3f", sweptStepSize));
		}
	}

	private void setToFixedMode() {
		calculateFixedParameters();
		region.setFixEnergy(fixedSpectrumEnergyCentre);
		region.setEnergyStep(Double.parseDouble(txtMinimumSize.getText()));
		region.setLowEnergy(Double.parseDouble(txtSpectrumEnergyLow.getText()));
		region.setHighEnergy(Double.parseDouble(txtSpectrumEnergyHigh.getText()));
		region.setSlices(spinnerNumberOfYSlices.getSelection());
		if (btnFixed.getSelection()) {
			updateTotalSteps();
		}
	}

	private void updateTotalSteps() {
		calculateTotalSteps();
		region.setTotalSteps(Integer.parseInt(txtStepTotalSteps.getText()));
		updateTotalTime();
	}

	private void calculateTotalSteps() {
		long totalSteps = 1;
		if (btnSwept.getSelection()) {
			final double energyWidth = Double.parseDouble(txtSpectrumEnergyWidth.getText());
			final double stepSize = Double.parseDouble(txtStepSize.getText());
			final int lastXChannel = Integer.parseInt(spinnerEnergyChannelTo.getText());
			final int firstXChannel = Integer.parseInt(spinnerEnergyChannelFrom.getText());
			final double minStepSize = Double.parseDouble(txtMinimumSize.getText());
			final double energyRangePerImage = minStepSize * (lastXChannel - firstXChannel + 1);
			totalSteps = RegionStepsTimeEstimation.calculateTotalSteps(energyWidth, stepSize, energyRangePerImage);
		}
		txtStepTotalSteps.setText(String.format("%d", totalSteps));
	}

	private void updateTotalTime() {
		calculateTotalTime();
		region.setTotalTime(Double.parseDouble(txtStepEstimatedTotalTime.getText()));
	}

	private void calculateTotalTime() {
		final int numberOfIterations = spinnerNumberOfIterations.getSelection();
		final double stepTime = Double.parseDouble(txtStepTime.getText());
		final int totalSteps = Integer.parseInt(txtStepTotalSteps.getText());
		final double calculateTotalTime = RegionStepsTimeEstimation.calculateTotalTime(stepTime, totalSteps, numberOfIterations);
		txtStepEstimatedTotalTime.setText(String.format("%.3f", calculateTotalTime));
	}

	private void calculateFixedParameters() {
		toggleFixedModeParameters(canEdit);
		// restore the original energy step size for the FIXED
		txtSpectrumEnergyCentre.setText(String.format(FORMAT_FLOAT, fixedSpectrumEnergyCentre));
		txtStepSize.setText(txtMinimumSize.getText().trim());
		final double energyWidth = getFixedEnergyRange()/1000.0;
		txtSpectrumEnergyWidth.setText(String.format(FORMAT_FLOAT, energyWidth));
		txtSpectrumEnergyLow.setText(String.format(FORMAT_FLOAT, fixedSpectrumEnergyCentre - energyWidth / 2));
		txtSpectrumEnergyHigh.setText(String.format(FORMAT_FLOAT, fixedSpectrumEnergyCentre + energyWidth / 2));
	}

	private long calculateStepFrames() {
		return Math.round(Double.parseDouble(txtStepTime.getText()) / Double.parseDouble(txtStepMinimumTime.getText()));
	}

	private double getFixedEnergyRange() {
		return Double.parseDouble(txtMinimumSize.getText())
				* (Integer.parseInt(spinnerEnergyChannelTo.getText()) - Integer.parseInt(spinnerEnergyChannelFrom.getText()) + 1);
	}

	private void openMessageBox(String title, String message, int iconStyle) {
		MessageBox dialog=new MessageBox(getSite().getShell(), iconStyle | SWT.OK);
		dialog.setText(title);
		dialog.setMessage(message);
		dialog.open();
	}

	private SESRegion getSelectedRegionInSequenceView() {
		final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart findView = null;
		if (activePage != null) {
			findView = activePage.findView(getSequenceViewID());
		}
		if (findView == null) return null;
		final ISelection selection = findView.getViewSite().getSelectionProvider().getSelection();
		if (selection instanceof IStructuredSelection structuredSel) {
			final Object firstElement = structuredSel.getFirstElement();
			if (firstElement instanceof SESRegion regionFirstElement) {
				region = regionFirstElement;
				return region;
			}
		}
		return null;
	}

	protected void initialisation() {
		final AnalyserEnergyRangeConfiguration energyRange = analyser.getEnergyRange();
		comboViewerLensMode.add(energyRange.getAllLensModes().toArray(Object[]::new));
		comboViewerPassEnergy.add(energyRange.getAllPassEnergies().stream().toArray(Object[]::new));

		new Label(plainComposite, SWT.None).setText("There is no region selected in this sequence.");
		regionPageBook.showPage(regionComposite);
	}

	protected void updateExcitationEnergyCachedPosition(String scannableName, double newExcitationEnergy) {
		final double previousExcitationEnergy = sequence.getExcitationEnergySourceByScannableName(scannableName).getValue();
		if (previousExcitationEnergy != newExcitationEnergy) {
			sequence.getExcitationEnergySourceByScannableName(scannableName).setValue(newExcitationEnergy);
			logger.debug("Got new cached {} x-ray energy. Previous position: {}eV, new position: {}eV", scannableName, previousExcitationEnergy, newExcitationEnergy);
		}
	}

	protected void updateExcitationEnergyUIValues(Text textArea, final Object currentPosition, final boolean readOnly) {
		if (textArea == null || textArea.isDisposed()) return;
		// Update the GUI in the UI thread
		textArea.getDisplay().asyncExec(() -> {
			String currentPositionString = String.format(FORMAT_FLOAT, currentPosition).trim();
			if (currentPositionString == null) {
				currentPositionString = "null";
			}
			textArea.setText(currentPositionString);
			textArea.setEditable(!readOnly);
		});
	}

	protected double getExcitationEnergy() {
		return sequence.getExcitationEnergySourceByRegion(region).getValue();
	}

	protected void onModifyExcitationEnergy(SelectionEvent e) {
		final ExcitationEnergySelector excitationEnergySelector = excitationEnergySelectorList.stream().filter(ex -> ex.getText().equals(e.getSource())).findFirst().orElseThrow();
		final double newExcitationEnergy = Double.parseDouble(((Text) e.getSource()).getText());
		updateExcitationEnergyCachedPosition(excitationEnergySelector.getScannable().getName(), newExcitationEnergy);
		updateExcitationEnergyUIValues(excitationEnergySelector.getText(), sequence.getExcitationEnergySourceByName(excitationEnergySelector.getName()).getValue(), isExcitationEnergyReadOnly());
		fireSelectionChanged(new ExcitationEnergyChangedSelection(excitationEnergySelector.getName(), getExcitationEnergy()));
	}

	protected void toggleFixedModeParameters(boolean enabled) {
		txtSpectrumEnergyLow.setEditable(false);
		txtSpectrumEnergyHigh.setEditable(false);
		txtSpectrumEnergyWidth.setEditable(false);
		txtSpectrumEnergyCentre.setEditable(enabled);
		txtStepSize.setEditable(false);
		txtSpectrumEnergyLow.setEnabled(false);
		txtSpectrumEnergyHigh.setEnabled(false);
		txtSpectrumEnergyWidth.setEnabled(false);
		txtSpectrumEnergyCentre.setEnabled(enabled);
		txtStepSize.setEnabled(false);
	}

	protected void toggleSweptModeParameters(boolean enabled) {
		txtSpectrumEnergyLow.setEditable(enabled);
		txtSpectrumEnergyHigh.setEditable(enabled);
		txtSpectrumEnergyWidth.setEditable(enabled);
		txtSpectrumEnergyCentre.setEditable(enabled);
		txtStepSize.setEditable(enabled);
		txtSpectrumEnergyLow.setEnabled(enabled);
		txtSpectrumEnergyHigh.setEnabled(enabled);
		txtSpectrumEnergyWidth.setEnabled(enabled);
		txtSpectrumEnergyCentre.setEnabled(enabled);
		txtStepSize.setEnabled(enabled);
	}

	private void initialiseRegionView(SESRegion region) {
		this.region = region;
		if (region == null) {
			return;
		}
		txtRegionName.setText(region.getName());
		comboViewerLensMode.getCCombo().setText(region.getLensMode());
		comboViewerPassEnergy.getCCombo().setText(String.valueOf(region.getPassEnergy()));
		//Acquisition configuration
		spinnerNumberOfIterations.setSelection(region.getIterations());
		spinnerNumberOfYSlices.setSelection(region.getSlices());
		//Acquisition mode
		btnSwept.setSelection(region.isAcquisitionModeSwept());
		btnFixed.setSelection(region.isAcquisitionModeFixed());
		//ExcitationEnergy
		setupInitialExcitationEnergyUI(region);
		//Energy mode
		btnKinetic.setSelection(region.isEnergyModeKinetic());
		btnBinding.setSelection(region.isEnergyModeBinding());
		//Spectrum energy range
		sweptLowEnergy = region.getLowEnergy();
		sweptHighEnergy = region.getHighEnergy();
		sweptStepSize = region.getEnergyStep();
		sweptSlices = region.getSlices();
		fixedSpectrumEnergyCentre = region.getFixEnergy();
		//Region error message
		updateRegionValidationMessage(region);
		//Step
		txtStepTime.setText(String.format("%.3f", region.getStepTime()));
		spinnerStepFrames.setSelection((int) calculateStepFrames());
		txtMinimumSize.setText(String.format("%.3f", camera.getEnergyResolution() * region.getPassEnergy()));
		//Detector
		spinnerEnergyChannelFrom.setSelection(region.getFirstXChannel());
		spinnerEnergyChannelTo.setSelection(region.getLastXChannel());
		spinnerYChannelFrom.setSelection(region.getFirstYChannel());
		spinnerYChannelTo.setSelection(region.getLastYChannel());
		btnADCMode.setSelection(region.isDetectorModeADC());
		btnPulseMode.setSelection(region.isDetectorModePulseCounting());

		if (region.isAcquisitionModeSwept()) {
			calculateSweptParameters();
		} else {
			calculateFixedParameters();
		}
		updateTotalSteps();
	}

	private void setupInitialExcitationEnergyUI(final SESRegion region) {
		final String regionEnergySource = region.getExcitationEnergySource();
		for (final ExcitationEnergySelector excitationEnergySelector : excitationEnergySelectorList) {
			final boolean isSelected = excitationEnergySelector.getName().equals(regionEnergySource);
			final Control control = excitationEnergySelector.getControl();
			if (control instanceof Button button) {
				button.setSelection(isSelected);
			}
			excitationEnergySelector.getText().setEnabled(isSelected);
			final String name = excitationEnergySelector.getName();
			if (sequence != null) updateExcitationEnergyUIValues(excitationEnergySelector.getText(), sequence.getExcitationEnergySourceByName(name).getValue(), isExcitationEnergyReadOnly());
		}
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		try {
			SESRegion selectedRegionInSequenceView = getSelectedRegionInSequenceView();
			if (selectedRegionInSequenceView != null) {
				return new StructuredSelection(selectedRegionInSequenceView);
			}
		} catch (Exception e) {
			logger.error("Error getting selection", e);
		}
		return StructuredSelection.EMPTY;
	}

	protected void setCanEdit(boolean enabled) {
		txtRegionName.setEnabled(enabled);
		comboViewerLensMode.getControl().setEnabled(enabled);
		comboViewerPassEnergy.getControl().setEnabled(enabled);
		//Acquisition Configuration / Mode
		spinnerNumberOfIterations.setEnabled(enabled);
		spinnerNumberOfYSlices.setEnabled(enabled);
		btnFixed.setEnabled(enabled);
		btnSwept.setEnabled(enabled);
		//Excitation energy and mode
		for (final ExcitationEnergySelector excitationEnergySelector : excitationEnergySelectorList) {
			if (ServiceProvider.getService(SESSettingsService.class).isExcitationEnergySourceSelectable() && enabled) {
				final Button button = (Button) excitationEnergySelector.getControl();
				final boolean buttonSelected = button.getSelection();
				button.setEnabled(enabled);
				excitationEnergySelector.getText().setEnabled(buttonSelected);
			} else {
				excitationEnergySelector.getControl().setEnabled(enabled);
				excitationEnergySelector.getText().setEnabled(enabled);
			}
		}
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
		spinnerStepFrames.setEnabled(enabled);
		txtStepTime.setEnabled(enabled);
		//Detector
		spinnerYChannelFrom.setEnabled(enabled);
		spinnerYChannelTo.setEnabled(enabled);
		spinnerEnergyChannelFrom.setEnabled(enabled);
		spinnerEnergyChannelTo.setEnabled(enabled);
		btnPulseMode.setEnabled(enabled);
		btnADCMode.setEnabled(enabled);
	}

	@Override
	public void dispose() {
		getViewSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(getSequenceViewID(), selectionListener);
		super.dispose();
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		fireSelectionChanged(selection);
	}

	private void fireSelectionChanged(SESRegion r) {
		ISelection sel = StructuredSelection.EMPTY;
		if (r != null) {
			sel = new StructuredSelection(r);
		}
		fireSelectionChanged(sel);
	}

	protected void fireSelectionChanged(ISelection sel) {
		SelectionChangedEvent event = new SelectionChangedEvent(this, sel);
		for (ISelectionChangedListener listener : selectionChangedListeners) {
			listener.selectionChanged(event);
		}
	}

	public IVGScientaAnalyserRMI getAnalyser() {
		return analyser;
	}

	public void setAnalyser(IVGScientaAnalyserRMI analyser) {
		this.analyser = analyser;
	}

	public String getSequenceViewID() {
		return sequenceViewID;
	}

	public void setSequenceViewID(String sequenceViewID) {
		this.sequenceViewID = sequenceViewID;
	}

	@Override
	public void setFocus() {

	}

	public void setViewPartName(String viewPartName) {
		setPartName(viewPartName);
	}

	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	protected boolean isExcitationEnergyReadOnly() {
		return false;
	}

	public SESSequence getSequence() {
		return sequence;
	}

	public class ExcitationEnergySelector extends SESConfigExcitationEnergySource {
		private static final long serialVersionUID = 1L;
		private transient Control control;
		private transient  Text text;

		ExcitationEnergySelector(SESConfigExcitationEnergySource config, Composite parent, boolean useLabel) {
			super(config.getName(), config.getDisplayName() == null ? config.getScannableName() : config.getDisplayName(), config.getScannableName());
			if (useLabel) {
				final Label label = new Label(parent, SWT.NONE);
				label.setText(this.getDisplayName());
				this.control = label;
			} else {
				final Button button = new Button(parent, SWT.RADIO);
				button.setText(this.getDisplayName());
				this.control = button;
			}
			this.text = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
			text.setToolTipText("Current " + this.getDisplayName() + " X-ray beam energy");
			text.setEditable(true);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(text);
		}

		public Control getControl() {
			return control;
		}

		public Text getText() {
			return text;
		}
	}
}