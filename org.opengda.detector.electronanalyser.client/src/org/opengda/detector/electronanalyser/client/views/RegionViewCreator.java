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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.util.Pair;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;
import org.opengda.detector.electronanalyser.client.Camera;
import org.opengda.detector.electronanalyser.client.selection.EnergyChangedSelection;
import org.opengda.detector.electronanalyser.client.selection.FileSelection;
import org.opengda.detector.electronanalyser.client.selection.RefreshRegionDisplaySelection;
import org.opengda.detector.electronanalyser.client.selection.RegionActivationSelection;
import org.opengda.detector.electronanalyser.client.selection.RegionValidationMessage;
import org.opengda.detector.electronanalyser.client.selection.TotalTimeSelection;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUISITION_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.DETECTOR_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.STATUS;
import org.opengda.detector.electronanalyser.utils.RegionDefinitionResourceUtil;
import org.opengda.detector.electronanalyser.utils.RegionStepsTimeEstimation;
import org.opengda.detector.electronanalyser.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import uk.ac.diamond.daq.pes.api.AnalyserEnergyRangeConfiguration;
import uk.ac.gda.devices.vgscienta.IVGScientaAnalyserRMI;

public class RegionViewCreator extends ViewPart implements ISelectionProvider {
	public static final String ID = "org.opengda.detector.electronanalyser.client.regioncreator";
	private static final Logger logger = LoggerFactory.getLogger(RegionViewCreator.class);
	protected static final String FORMAT_FLOAT = "%.4f";

	private PageBook regionPageBook;
	private Composite plainComposite;
	private ScrolledComposite regionComposite;

	private Combo regionName;
	private Combo lensMode;
	private Combo passEnergy;

	//Acquisition Configuration / Mode
	private Spinner spinnerNumberOfIterations;
	private Spinner spinnerNumberOfYSlices;
	private Button btnFixed;
	private Button btnSwept;

	//Excitation energy and mode
	private Button btnHard;
	private Button btnSoft;
	protected Text txtHardExcitationEnergy;
	protected Text txtSoftExcitationEnergy;
	private double excitationEnergy = 0.0;
	protected double hardXRayEnergy = 5000.0; // eV
	protected double softXRayEnergy = 500.0; // eV
	private Button btnBinding;
	private Button btnKinetic;
	private boolean kineticSelected;

	//Spectrum energy range
	private Label lblSpectrumEnergyLow;
	private Label lblSpectrumEnergyHigh;
	private Text txtSpectrumEnergyLow;
	private Text txtSpectrumEnergyHigh;
	private Text txtSpectrumEnergyCentre;
	private double fixedSpectrumEnergyCentre;
	private Text txtSpectrumEnergyWidth;
	private double sweptLowEnergy;
	private double sweptHighEnergy;
	private HashMap<Region, Pair<String,String>> regionSpectrumEnergyLimits = new HashMap<>();

	//Region error message
	private StyledText txtRegionStateValue;
	private HashMap<Region, String> regionValidationMessages = new HashMap<>();

	//Step
	private Text txtEstimatedTotalTime;
	private Text txtSize;
	private double sweptStepSize;
	private Text txtTotalSteps;
	private Text txtFramesPerSecond;
	private int sweptSlices;
	private Text txtMinimumSize;
	private Text txtTime;
	private Text txtMinimumTime;
	private Spinner spinnerFrames;

	//Detector
	private Spinner spinnerYChannelFrom;
	private Spinner spinnerYChannelTo;
	private Spinner spinnerEnergyChannelFrom;
	private Spinner spinnerEnergyChannelTo;
	private Button btnPulseMode;
	private Button btnADCMode;

	private RegionDefinitionResourceUtil regionDefinitionResourceUtil;
	private EditingDomain editingDomain = null;
	private List<Region> regions = Collections.emptyList() ;
	private Region region = null;

	private Camera camera;
	private IVGScientaAnalyserRMI analyser;

	private List<ISelectionChangedListener> selectionChangedListeners;

	private String sequenceViewID = SequenceViewCreator.ID;

	protected boolean canEdit = true;

	private CompoundCommand groupCommand = new CompoundCommand();
	private List<ISelection> selectionEventsToFire = new ArrayList<>();

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
		} else if (selection instanceof RefreshRegionDisplaySelection) {
			//Must reload region data if done via undo / redo
			loadRegionExcitationEnergies(regions);
			displayRegion();
		} else if (selection instanceof RegionActivationSelection regionActivationSelection) {
			handleRegionActivationSelection(regionActivationSelection);
		} else if (selection instanceof RegionValidationMessage valMessage){
			handleRegionValidationMessage(valMessage);
		} else if (selection instanceof IStructuredSelection sel) {
			if (StructuredSelection.EMPTY.equals(selection)) {
				region = null;
				regionPageBook.showPage(plainComposite);
			} else {
				Object firstElement = sel.getFirstElement();
				if (firstElement instanceof Region r &&  (!r.equals(region))) {
					region = r;
					initialiseViewWithRegionData(region);
					populateRegionNameCombo(regions);
				}
				regionPageBook.showPage(regionComposite);
			}
		}
	}

	private void handleFileSelection(FileSelection fileSelection) {
		//Sequence file changed, update UI with new region data. This class should only ever load
		//in new regions/sequence data from this method.
		try {
			regions = regionDefinitionResourceUtil.getRegions(fileSelection.getFilename());
			populateRegionNameCombo(regions);
			loadRegionExcitationEnergies(regions);
			//Correct all regions to the soft or hard values.
			for (Region r : regions) {
				double regionExcitationEnergy = hardXRayEnergy;
				if (regionDefinitionResourceUtil.isSourceSelectable()) {
					regionExcitationEnergy = regionDefinitionResourceUtil.isSourceHard(r) ? hardXRayEnergy : softXRayEnergy;
				}
				if (r.getExcitationEnergy() != regionExcitationEnergy) {
					r.setExcitationEnergy(regionExcitationEnergy);
				}
			}
			displayRegion();
		} catch (Exception e) {
			logger.error("Cannot get regions list from {}", regionDefinitionResourceUtil.getFileName(), e);
		}
	}

	private void displayRegion() {
		if (regions.isEmpty()) {
			region = null;
			regionPageBook.showPage(plainComposite);
		} else {
			regionPageBook.showPage(regionComposite);
			region = getSelectedRegionInSequenceView();
			initialiseViewWithRegionData(region);
		}
	}

	private void handleRegionActivationSelection(RegionActivationSelection regionActivationSelection) {
		region = regionActivationSelection.getRegion();
		regionName.setText(region.getName());
		initialiseViewWithRegionData(region);
		populateRegionNameCombo(regions);
	}

	private void handleRegionValidationMessage(RegionValidationMessage valMessage) {
		Region targetRegion = valMessage.getRegion();
		String message = valMessage.getMessage();
		regionValidationMessages.put(targetRegion, message);

		String lowLimitTooltip = "";
		String highLimitTooltip = "";
		Double spectrumEnergyLowLimit = valMessage.getSpectrumEnergyLowLimit();
		Double spectrumEnergyHighLimit = valMessage.getSpectrumEnergyHighLimit();

		if (spectrumEnergyLowLimit != null) {
			lowLimitTooltip = "Lower limit = "
				+ (targetRegion.getEnergyMode() == ENERGY_MODE.BINDING ? String.format(FORMAT_FLOAT, excitationEnergy - spectrumEnergyLowLimit) + " = Excitation Energy - " : "")
				+  String.format(FORMAT_FLOAT, spectrumEnergyLowLimit);
		}
		if (spectrumEnergyHighLimit != null) {
			highLimitTooltip = "Upper limit = "
				+ (targetRegion.getEnergyMode() == ENERGY_MODE.BINDING ? String.format(FORMAT_FLOAT, excitationEnergy - spectrumEnergyHighLimit) + " = Excitation Energy - " : "")
				+ String.format(FORMAT_FLOAT, spectrumEnergyHighLimit);
		}
		regionSpectrumEnergyLimits.put(
			targetRegion,
			new Pair<> (lowLimitTooltip, highLimitTooltip)
		);
		if (regionName.getText().equals(targetRegion.getName())) {
			txtRegionStateValue.setText(message);
			lblSpectrumEnergyLow.setToolTipText(lowLimitTooltip);
			lblSpectrumEnergyHigh.setToolTipText(highLimitTooltip);
		}
		//Remove regions that no longer exist e.g ones deleted or sequence file changed
		regionValidationMessages.keySet().retainAll(regions);
		regionSpectrumEnergyLimits.keySet().retainAll(regions);
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

		regionName = new Combo(grpName, SWT.NONE);
		GridData namelayoutData = new GridData(GridData.FILL_HORIZONTAL);
		regionName.setLayoutData(namelayoutData);
		regionName.setToolTipText("List of available active regions to select");
		final ControlDecoration regionNameControlDecorator = new ControlDecoration(regionName, SWT.TOP | SWT.LEFT);
		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION);
		regionNameControlDecorator.setImage(fieldDecoration.getImage());
		FieldDecoration dec = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		namelayoutData.horizontalIndent = dec.getImage().getBounds().width;
		regionName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (regionName.getText().length() > 0) {
					if (containsIllegals(regionName.getText())) {
						openMessageBox("Region Name Error", "Region name '"+ regionName.getText()+"' contains illegal character.", SWT.ICON_ERROR);
					}
					regionNameControlDecorator.hide();
				} else {
					regionNameControlDecorator.show();
					if (region != null) {
						regionNameControlDecorator.setDescriptionText(region.getName() + " is not enabled");
					}
					regionNameControlDecorator.setShowHover(true);
				}
			}
			private boolean containsIllegals(String toExamine) {
				Pattern pattern = Pattern.compile("[~#@*+%{}<>\\[\\]|\"/^]");
				Matcher matcher = pattern.matcher(toExamine);
				return matcher.find();
			}
		});
		regionName.addSelectionListener(regionNameSelAdapter);

		Group grpLensMode = new Group(grpTop, SWT.NONE);
		grpLensMode.setText("Lens Mode");
		GridDataFactory.fillDefaults().applyTo(grpLensMode);
		grpLensMode.setLayout(new GridLayout());
		grpLensMode.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		lensMode = new Combo(grpLensMode, SWT.READ_ONLY);
		lensMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lensMode.setToolTipText("List of available modes to select");
		SelectionAdapter lensModeSelAdaptor = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(lensMode)) {
					addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LensMode(), lensMode.getText(), region.getLensMode());
					executeCommand(groupCommand);
					fireSelectionChanged(new EnergyChangedSelection(region, false));
				}
			}
		};
		lensMode.addSelectionListener(lensModeSelAdaptor);

		Group grpPassEnergy = new Group(grpTop, SWT.NONE);
		grpPassEnergy.setLayout(new GridLayout());
		grpPassEnergy.setText("Pass Energy");
		grpPassEnergy.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		passEnergy = new Combo(grpPassEnergy, SWT.READ_ONLY);
		passEnergy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		passEnergy.setToolTipText("Select a pass energy to use");
		SelectionAdapter passEnerySelectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(passEnergy)) {
					String passEnergyFromCombo = passEnergy.getText();
					int passEnergyIntValue = Integer.parseInt(passEnergyFromCombo);
					txtMinimumSize.setText(String.format("%.3f", camera.getEnergyResolution() * passEnergyIntValue));
					addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_PassEnergy(), passEnergyIntValue, region.getPassEnergy());
					updateEnergyStep();
					executeCommand(groupCommand);
					fireSelectionChanged(new EnergyChangedSelection(region, false));
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		};
		passEnergy.addSelectionListener(passEnerySelectionAdapter);

		grpTop.addControlListener(new ControlListener() {

			//Utilise space more efficiently, if large enough area they will be on same line.
			@Override
			public void controlResized(ControlEvent e) {
				int columns = 3;
				GridData gridData = new GridData();
				gridData.horizontalAlignment = GridData.FILL;
				gridData.horizontalSpan = 1;
				gridData.grabExcessHorizontalSpace = true;
				int width = grpTop.getSize().x;

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
		SelectionAdapter spinnerNumberOfIterationSelAdaptor = new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (e.getSource().equals(spinnerNumberOfIterations)) {
					addCommandToGroupToUpdateFeature(
						region.getRunMode(),
						RegiondefinitionPackage.eINSTANCE.getRunMode_NumIterations(),
						spinnerNumberOfIterations.getSelection(),
						region.getRunMode().getNumIterations()
					);
					updateTotalTime();
					executeCommand(groupCommand);
				}
			}
			@Override
			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
			}
		};
		spinnerNumberOfIterations.addSelectionListener(spinnerNumberOfIterationSelAdaptor);

		Label lblSclies = new Label(grpRunMode, SWT.NONE);
		lblSclies.setText("Number of Y Slices:");

		spinnerNumberOfYSlices = new Spinner(grpRunMode, SWT.BORDER);
		spinnerNumberOfYSlices.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(spinnerNumberOfYSlices)) {
					addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_Slices(), spinnerNumberOfYSlices.getSelection(), region.getSlices());
					executeCommand(groupCommand);
				}
			}
		});
		spinnerNumberOfYSlices.setToolTipText("Set number of slices required");
		spinnerNumberOfYSlices.setMinimum(1);
		spinnerNumberOfYSlices.setMaximum(camera.getCameraYSize());
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(spinnerNumberOfYSlices);

		Group grpAcquisitionMode = new Group(modeComposite, SWT.NONE);
		grpAcquisitionMode.setText("Acquisition Mode");
		GridLayoutFactory.fillDefaults().margins(0, 8).applyTo(grpAcquisitionMode);
		grpAcquisitionMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpAcquisitionMode.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		btnSwept = new Button(grpAcquisitionMode, SWT.RADIO);
		btnSwept.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				sweptStepSize = Double.parseDouble(txtSize.getText());
			}
		});
		btnSwept.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnSwept.setText("Swept");
		SelectionAdapter sweptSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(btnSwept) && btnSwept.getSelection()) {
					onModifyAcquisitionMode(e.getSource());
					addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_AcquisitionMode(), ACQUISITION_MODE.SWEPT, region.getAcquisitionMode());
					fireSelectionChanged(new TotalTimeSelection());
					executeCommand(groupCommand);
				}
			}
		};
		btnSwept.addSelectionListener(sweptSelectionListener);

		btnFixed = new Button(grpAcquisitionMode, SWT.RADIO);
		btnFixed.setText("Fixed");
		SelectionAdapter fixedSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(btnFixed) && btnFixed.getSelection()) {
					onModifyAcquisitionMode(e.getSource());
					addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_AcquisitionMode(), ACQUISITION_MODE.FIXED, region.getAcquisitionMode());
					fireSelectionChanged(new TotalTimeSelection());
					executeCommand(groupCommand);
				}
			}
		};
		btnFixed.addSelectionListener(fixedSelectionListener);
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

		if (regionDefinitionResourceUtil.isSourceSelectable()) {
			btnHard = new Button(grpExcitationEnergy, SWT.RADIO);
			btnHard.setText("Hard X-Ray:");

			txtHardExcitationEnergy = new Text(grpExcitationEnergy, SWT.BORDER | SWT.READ_ONLY);
			txtHardExcitationEnergy.setToolTipText("Current hard X-ray beam energy");

			btnSoft = new Button(grpExcitationEnergy, SWT.RADIO);
			btnSoft.setText("Soft X-Ray:");

			txtSoftExcitationEnergy = new Text(grpExcitationEnergy, SWT.BORDER | SWT.READ_ONLY);
			txtSoftExcitationEnergy.setToolTipText("Current soft X-ray beam energy");
			txtSoftExcitationEnergy.setEditable(true);
			txtSoftExcitationEnergy.addSelectionListener(txtExcitationEnergySelAdaptor);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(txtSoftExcitationEnergy);
//			//If started typing and then click off, reset back to previous cached value
			txtSoftExcitationEnergy.addFocusListener(FocusListener.focusLostAdapter(
				ev -> txtSoftExcitationEnergy.setText(String.format(FORMAT_FLOAT, softXRayEnergy))
			));
			btnSoft.addSelectionListener(xRaySourceSelectionListener);
			btnHard.addSelectionListener(xRaySourceSelectionListener);
		}
		else {
			Label lblCurrentValue = new Label(grpExcitationEnergy, SWT.NONE);
			lblCurrentValue.setText("X-Ray energy:");
			txtHardExcitationEnergy = new Text(grpExcitationEnergy, SWT.BORDER | SWT.READ_ONLY);
			txtHardExcitationEnergy.setToolTipText("Current X-ray beam energy");
		}
		txtHardExcitationEnergy.addSelectionListener(txtExcitationEnergySelAdaptor);
		txtHardExcitationEnergy.setEditable(true);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(txtHardExcitationEnergy);
//		//If started typing and then click off, reset back to previous cached value
		txtHardExcitationEnergy.addFocusListener(FocusListener.focusLostAdapter(
			ev -> txtHardExcitationEnergy.setText(String.format(FORMAT_FLOAT, hardXRayEnergy))
		));

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

	private void createSpectrumEnergyRangeArea(Composite rootComposite) {
		Group grpEnergy = new Group(rootComposite, SWT.NONE);
		grpEnergy.setText("Spectrum energy range [eV]");
		GridData grpEnergyGridData = new GridData(GridData.FILL_HORIZONTAL);
		grpEnergyGridData.horizontalSpan = 2;
		grpEnergy.setLayoutData(grpEnergyGridData);
		grpEnergy.setLayout(new GridLayout(4, false));
		grpEnergy.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		lblSpectrumEnergyLow = new Label(grpEnergy, SWT.NONE);
		lblSpectrumEnergyLow.setText("Low");

		txtSpectrumEnergyLow = new Text(grpEnergy, SWT.BORDER | SWT.SINGLE);
		GridData lowLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		txtSpectrumEnergyLow.setLayoutData(lowLayoutData);
		txtSpectrumEnergyLow.setToolTipText("Start energy");

		Label lblCenter = new Label(grpEnergy, SWT.NONE);
		lblCenter.setText("Center");

		txtSpectrumEnergyCentre = new Text(grpEnergy, SWT.BORDER | SWT.SINGLE);
		GridData centerLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		txtSpectrumEnergyCentre.setLayoutData(centerLayoutData);
		txtSpectrumEnergyCentre.setToolTipText("Center/Fixed energy");

		lblSpectrumEnergyHigh = new Label(grpEnergy, SWT.NONE);
		lblSpectrumEnergyHigh.setText("High");

		txtSpectrumEnergyHigh = new Text(grpEnergy, SWT.BORDER | SWT.SINGLE);
		GridData highLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		txtSpectrumEnergyHigh.setLayoutData(highLayoutData);
		txtSpectrumEnergyHigh.setToolTipText("Stop energy");

		Label lblWidth = new Label(grpEnergy, SWT.NONE);
		lblWidth.setText("Width");

		txtSpectrumEnergyWidth = new Text(grpEnergy, SWT.BORDER | SWT.SINGLE);
		txtSpectrumEnergyWidth.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtSpectrumEnergyWidth.setToolTipText("Energy width");

		txtSpectrumEnergyLow.addSelectionListener(spectrumEnergySelectionListener);
		txtSpectrumEnergyHigh.addSelectionListener(spectrumEnergySelectionListener);
		txtSpectrumEnergyCentre.addSelectionListener(spectrumEnergySelectionListener);
		txtSpectrumEnergyWidth.addSelectionListener(spectrumEnergySelectionListener);
	}

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

		ModifyListener validationListener = (e) -> {
			if (txtRegionStateValue.getText().equals("")) {
				GridData gridGrp = (GridData) grpRegionValidation.getLayoutData();
				gridGrp.exclude = true;
				grpRegionValidation.setVisible(false);

				GridData gridTxt = (GridData) txtRegionStateValue.getLayoutData();
				gridTxt.exclude = true;
				txtRegionStateValue.setVisible(false);
			}
			else {
				GridData gridGrp = (GridData) grpRegionValidation.getLayoutData();
				gridGrp.exclude = false;
				grpRegionValidation.setVisible(true);

				GridData gridTxt = (GridData) txtRegionStateValue.getLayoutData();
				gridTxt.exclude = false;
				txtRegionStateValue.setVisible(true);
			}
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
		GridData gridData = (GridData) txtRegionStateValue.getLayoutData();

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
		Group grpStep = new Group(rootComposite, SWT.NONE);
		grpStep.setText("Step");
		grpStep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpStep.setLayout(new GridLayout(4, false));
		grpStep.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		Label lblFrames = new Label(grpStep, SWT.NONE);
		lblFrames.setText("Frames");

		spinnerFrames = new Spinner(grpStep, SWT.BORDER);
		spinnerFrames.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		spinnerFrames.setToolTipText("Number of frames per step");
		spinnerFrames.setMinimum(1);
		spinnerFrames.setMaximum(Integer.MAX_VALUE);
		SelectionAdapter framesSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(spinnerFrames)) {
					txtTime.setText(String.format("%.3f", Double.parseDouble(txtMinimumTime.getText()) * Integer.parseInt(spinnerFrames.getText())));
					addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_StepTime(), Double.parseDouble(txtTime.getText()), region.getStepTime());
					updateTotalTime();
					executeCommand(groupCommand);
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		};
		spinnerFrames.addSelectionListener(framesSelectionListener);

		Label lblFramesPerSecond = new Label(grpStep, SWT.NONE);
		lblFramesPerSecond.setText("Frames/s");

		txtFramesPerSecond = new Text(grpStep, SWT.BORDER);
		txtFramesPerSecond.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtFramesPerSecond.setToolTipText("Camera frame rate");
		txtFramesPerSecond.setEditable(false);
		txtFramesPerSecond.setEnabled(false);
		txtFramesPerSecond.setText(String.format("%d", camera.getFrameRate()));

		Label lblTime = new Label(grpStep, SWT.NONE);
		lblTime.setText("Time [s]");

		txtTime = new Text(grpStep, SWT.BORDER | SWT.SINGLE);
		txtTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtTime.setToolTipText("Time per step");
		SelectionAdapter timeSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				Object source = e.getSource();
				if (source.equals(txtTime) && !txtTime.getText().isEmpty()) {
					long frames = Math.round(Double.parseDouble(txtTime.getText()) / Double.parseDouble(txtMinimumTime.getText()));
					spinnerFrames.setSelection((int) frames);
					txtTime.setText(String.format("%.3f", Double.parseDouble(txtTime.getText())));
					updateTotalTime();
					addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_StepTime(), Double.parseDouble(txtTime.getText()), region.getStepTime());
					executeCommand(groupCommand);
				}
			}
		};
		txtTime.addSelectionListener(timeSelectionListener);

		Label lblMinimumTime = new Label(grpStep, SWT.NONE);
		lblMinimumTime.setText("Min. Time [s]");

		txtMinimumTime = new Text(grpStep, SWT.BORDER);
		txtMinimumTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtMinimumTime.setToolTipText("Minimum time per step allowed");
		txtMinimumTime.setEditable(false);
		txtMinimumTime.setEnabled(false);
		txtMinimumTime.setText(String.format("%f", 1.0 / camera.getFrameRate()));

		Label lblSize = new Label(grpStep, SWT.NONE);
		lblSize.setText("Size [meV]");

		txtSize = new Text(grpStep, SWT.BORDER | SWT.SINGLE);
		txtSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtSize.setToolTipText("Energy size per step");
		SelectionAdapter sizeSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				Object source = e.getSource();
				if (source.equals(txtSize)) {
					if (Double.parseDouble(txtSize.getText()) < Double.parseDouble(txtMinimumSize.getText())) {
						txtSize.setText(txtMinimumSize.getText());
					} else {
						txtSize.setText(String.format("%.3f", Double.parseDouble(txtSize.getText())));
					}
					addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_EnergyStep(), Double.parseDouble(txtSize.getText()), region.getEnergyStep());
					// set Total steps
					// TODO set to EPICS size PV to get total size update
					updateTotalSteps();
					if (btnSwept.getSelection()) {
						sweptStepSize = Double.parseDouble(txtSize.getText());
					}
					executeCommand(groupCommand);
				}
			}
		};
		txtSize.addSelectionListener(sizeSelectionListener);

		Label lblMinimumSize = new Label(grpStep, SWT.NONE);
		lblMinimumSize.setText("Min. Size [meV]");

		txtMinimumSize = new Text(grpStep, SWT.BORDER);
		txtMinimumSize.setToolTipText("Minimum energy size per step allowed");
		txtMinimumSize.setEditable(false);
		txtMinimumSize.setEnabled(false);
		txtMinimumSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblTotalTime = new Label(grpStep, SWT.NONE);
		lblTotalTime.setText("Estimated Time [s]");

		txtEstimatedTotalTime = new Text(grpStep, SWT.BORDER);
		txtEstimatedTotalTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtEstimatedTotalTime.setToolTipText("Anticipated total time for this collection");
		txtEstimatedTotalTime.setEditable(false);
		txtEstimatedTotalTime.setEnabled(false);

		Label lblTotalSteps = new Label(grpStep, SWT.NONE);
		lblTotalSteps.setText("Total Steps");

		txtTotalSteps = new Text(grpStep, SWT.BORDER);
		txtTotalSteps.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtTotalSteps.setToolTipText("Total number of steps for this collection");
		txtTotalSteps.setEditable(false);
		txtTotalSteps.setEnabled(false);
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

		spinnerEnergyChannelFrom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(spinnerEnergyChannelFrom)) {
					addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_FirstXChannel(), spinnerEnergyChannelFrom.getSelection(), region.getFirstXChannel());
					if (btnFixed.getSelection()) {
						txtSize.setText(String.format("%.3f", fixedEnergyRange()));
					}
					executeCommand(groupCommand);
				}
			}
		});
		spinnerEnergyChannelFrom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		spinnerEnergyChannelFrom.setToolTipText("Low bound");
		spinnerEnergyChannelFrom.setMinimum(1);
		spinnerEnergyChannelFrom.setMaximum(camera.getCameraXSize());

		Label lblEnergyChannelTo = new Label(grpDetector, SWT.NONE);
		lblEnergyChannelTo.setText("To");

		spinnerEnergyChannelTo = new Spinner(grpDetector, SWT.BORDER);
		spinnerEnergyChannelTo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(spinnerEnergyChannelTo)) {
					addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LastXChannel(), spinnerEnergyChannelTo.getSelection(), region.getLastXChannel());
					if (btnFixed.getSelection()) {
						txtSize.setText(String.format("%.3f", fixedEnergyRange()));
					}
					executeCommand(groupCommand);
				}
			}
		});
		spinnerEnergyChannelTo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		spinnerEnergyChannelTo.setToolTipText("High bound");
		spinnerEnergyChannelTo.setMinimum(1);
		spinnerEnergyChannelTo.setMaximum(camera.getCameraXSize());

		Label lblYChannel = new Label(grpDetector, SWT.NONE);
		lblYChannel.setText("Y Channels:");

		Label lblYChannelFrom = new Label(grpDetector, SWT.NONE);
		lblYChannelFrom.setText("From");

		spinnerYChannelFrom = new Spinner(grpDetector, SWT.BORDER);
		spinnerYChannelFrom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(spinnerYChannelFrom)) {
					addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_FirstYChannel(), spinnerYChannelFrom.getSelection(), region.getFirstYChannel());
					spinnerNumberOfYSlices.setMaximum(spinnerYChannelTo.getSelection() - spinnerYChannelFrom.getSelection()+1);
					executeCommand(groupCommand);
				}
			}
		});
		spinnerYChannelFrom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		spinnerYChannelFrom.setToolTipText("Low bound");
		spinnerYChannelFrom.setMinimum(1);
		spinnerYChannelFrom.setMaximum(camera.getCameraYSize());

		Label lblYChannelTo = new Label(grpDetector, SWT.NONE);
		lblYChannelTo.setText("To");

		spinnerYChannelTo = new Spinner(grpDetector, SWT.BORDER);
		spinnerYChannelTo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(spinnerYChannelTo)) {
					addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LastYChannel(), spinnerYChannelTo.getSelection(), region.getLastYChannel());
					spinnerNumberOfYSlices.setMaximum(spinnerYChannelTo.getSelection() - spinnerYChannelFrom.getSelection()+1);
					executeCommand(groupCommand);
				}
			}
		});
		spinnerYChannelTo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		spinnerYChannelTo.setToolTipText("High bound");
		spinnerYChannelTo.setMinimum(1);
		spinnerYChannelTo.setMaximum(camera.getCameraYSize());

		Label lblDetectorMode = new Label(grpDetector, SWT.NONE);
		lblDetectorMode.setText("Mode:");

		new Label(grpDetector, SWT.NONE);

		btnADCMode = new Button(grpDetector, SWT.RADIO);
		btnADCMode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(btnADCMode) && btnADCMode.getSelection()) {
					addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_DetectorMode(), DETECTOR_MODE.ADC, region.getDetectorMode());
					executeCommand(groupCommand);
				}
			}
		});
		btnADCMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnADCMode.setText("ADC");

		new Label(grpDetector, SWT.NONE);

		btnPulseMode = new Button(grpDetector, SWT.RADIO);
		btnPulseMode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(btnPulseMode) && btnPulseMode.getSelection()) {
					addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_DetectorMode(), DETECTOR_MODE.PULSE_COUNTING, region.getDetectorMode());
					executeCommand(groupCommand);
				}
			}
		});
		btnPulseMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnPulseMode.setText("Pulse Counting");
	}

	protected void createAdditionalPartControlAreas(Composite parent) {
		//Do nothing, this is for other classes that extend to override and implement
	}

	private void openMessageBox(String title, String message, int iconStyle) {
		MessageBox dialog=new MessageBox(getSite().getShell(), iconStyle | SWT.OK);
		dialog.setText(title);
		dialog.setMessage(message);
		dialog.open();
	}

	private Region getSelectedRegionInSequenceView() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		IViewPart findView = null;
		if (activePage != null) {
			findView = activePage.findView(getSequenceViewID());
		}
		if (findView != null) {
			ISelection selection = findView.getViewSite().getSelectionProvider().getSelection();
			if (selection instanceof IStructuredSelection structuredSel) {
				Object firstElement = structuredSel.getFirstElement();
				if (firstElement instanceof Region regionFirstElement) {
					region = regionFirstElement;
					return region;
				}
			}
		}

		return null;
	}

	//Load excitation energies from file. Currently, each region has it's own excitation energy. The current implementation
	//however only allows you to choose from hard or soft. Changing hard energy for example will change all regions that are above the threshold
	//to the new value. As we can load in multiple excitation energies from multiple regions, this means we must choose which regions we define
	//all other regions as either soft or hard. This functions finds the first regions that are defined as soft and hard and then will set all other
	//regions as these values.
	protected void loadRegionExcitationEnergies(List<Region> listOfRegions) {
		//Single source
		if (!regionDefinitionResourceUtil.isSourceSelectable()) {
			excitationEnergy = hardXRayEnergy = listOfRegions.get(0).getExcitationEnergy();
			return;
		}
		boolean hardFound = false;
		boolean softFound = false;
		for (Region r : listOfRegions) {
			if (regionDefinitionResourceUtil.isSourceHard(r) && !hardFound) {
				hardXRayEnergy = r.getExcitationEnergy();
				hardFound = true;
			}
			else if (regionDefinitionResourceUtil.isSourceSoft(r) && !softFound){
				softXRayEnergy = r.getExcitationEnergy();
				softFound = true;
			}
			if(hardFound && softFound) {
				break;
			}
		}
	}

	protected void initialisation() {
		AnalyserEnergyRangeConfiguration energyRange = analyser.getEnergyRange();

		try {
			// I09-137 Remove Transmission mode from UI
			// I09-203 Remove Angular60 mode from UI
			List<String> modes = new ArrayList<>(energyRange.getAllLensModes());
			modes.remove("Transmission");
			modes.remove("Angular60");
			lensMode.setItems(modes.toArray(new String[] {}));
		} catch (NullPointerException e) {
			logger.error("Cannot get lens mode list from analyser.", e);
		}
		// new String[] { "Transmission", "Angular45", "Angular60" });
		try {
			String[] passEnergies = energyRange.getAllPassEnergies()
					.stream()
					.map(Object::toString)
					.toArray(String[]::new);
			passEnergy.setItems(passEnergies);
		} catch (NullPointerException e) {
			logger.error("Cannot get pass energy list from analyser.", e);
		}
		// (new String[] { "5", "10", "20","50", "75", "100", "200","500" });

		try {
			editingDomain = regionDefinitionResourceUtil.getEditingDomain();
		} catch (Exception e1) {
			logger.error("Cannot get Editing Domain object.", e1);
		}

		new Label(plainComposite, SWT.None).setText("There is no region selected in this sequence.");
		regionPageBook.showPage(regionComposite);
	}

	private SelectionAdapter xRaySourceSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			final boolean isSourceHard = e.getSource().equals(btnHard) && btnHard.getSelection();
			final boolean isSourceSoft = e.getSource().equals(btnSoft) && btnSoft.getSelection();
			//This is needed because otherwise this is called twice
			if(!isSourceHard && !isSourceSoft) {
				return;
			}
			final boolean canUndoCommand = true;
			final Text textSelected = isSourceHard ? txtHardExcitationEnergy : txtSoftExcitationEnergy;
			final Text textUnselected = !isSourceHard ? txtHardExcitationEnergy : txtSoftExcitationEnergy;
			textSelected.setEnabled(true);
			textUnselected.setEnabled(false);
			final double newPosition = switchExcitationEnergySource(isSourceHard);
			updateExcitaitonEnergyCachedPosition(newPosition);
			updateExcitationEnergyUIValues(textSelected, newPosition, isExcitationEnergyReadOnly());
			addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_ExcitationEnergy(), newPosition, region.getExcitationEnergy());
			updateAllRegionsWithNewExcitationEnergyUpdateAndValidate(isSourceHard ? hardXRayEnergy : softXRayEnergy ,canUndoCommand);
		}
	};

	protected double switchExcitationEnergySource(boolean isSourceHard) {
		return isSourceHard ? hardXRayEnergy : softXRayEnergy;
	}

	protected void updateExcitaitonEnergyCachedPosition(double newExcitationEnergy) {
		double previousExcitationEnergy = hardXRayEnergy ;
		if (regionDefinitionResourceUtil.isSingleSource()) {
			hardXRayEnergy = excitationEnergy = newExcitationEnergy;
		}
		else {
			boolean isDcmenergy = regionDefinitionResourceUtil.isSourceHard(newExcitationEnergy);
			previousExcitationEnergy = isDcmenergy ? hardXRayEnergy : softXRayEnergy;
			hardXRayEnergy = isDcmenergy ? newExcitationEnergy : hardXRayEnergy;
			softXRayEnergy = !isDcmenergy ? newExcitationEnergy : softXRayEnergy;
			if(region != null) {
				boolean sourceHard = regionDefinitionResourceUtil.isSourceHard(region);
				excitationEnergy = sourceHard ? hardXRayEnergy : softXRayEnergy;
			}
		}
		if (previousExcitationEnergy != newExcitationEnergy) {
			logger.debug("Got new cached x-ray energy. Previous position: {}eV, new position: {}eV", previousExcitationEnergy, newExcitationEnergy);
		}
	}

	protected void updateAllRegionsWithNewExcitationEnergyUpdateAndValidate(double excitationEnergyValue, boolean canUndoCommand) {
		//Loop through regions and determine if we need to update it with a new value
		for (Region r : regions) {
			updateRegionsWithNewExcitationEnergy(r, excitationEnergyValue, canUndoCommand);
		}
		executeCommand(groupCommand);

		List<Region> regionsAtExcitationEnergyValue = regions;
		if (regionDefinitionResourceUtil.isSourceSelectable()) {
			//Only validate regions that are from the soft or hard source update. Wasted to do both
			boolean isSourceHard = regionDefinitionResourceUtil.isSourceHard(excitationEnergyValue);
			boolean isSourceSoft = regionDefinitionResourceUtil.isSourceSoft(excitationEnergyValue);
			regionsAtExcitationEnergyValue = regions.stream().filter(
				r -> regionDefinitionResourceUtil.isSourceHard(r.getExcitationEnergy()) && isSourceHard ||
				regionDefinitionResourceUtil.isSourceSoft(r.getExcitationEnergy()) && isSourceSoft
			).toList();
		}
		logger.info("About to validate {}", regionsAtExcitationEnergyValue.stream().map(Region::getName).toList());
		fireSelectionChanged(new EnergyChangedSelection(regionsAtExcitationEnergyValue, true));
	}

	private void updateRegionsWithNewExcitationEnergy(Region r, double excitationEnergyValue, boolean canUndoCommand) {
		//Determine if we need to update region with a new value
		final boolean isHardExcitationEnergyValue = regionDefinitionResourceUtil.isSourceHard(excitationEnergyValue);
		final boolean isRegionHard = regionDefinitionResourceUtil.isSourceHard(r);
		final boolean updateThisRegionExcitationEnergy = regionDefinitionResourceUtil.isSingleSource() || (isHardExcitationEnergyValue == isRegionHard);

		if (updateThisRegionExcitationEnergy) {
			if (canUndoCommand) {
				addCommandToGroupToUpdateFeature(r, RegiondefinitionPackage.eINSTANCE.getRegion_ExcitationEnergy(), excitationEnergyValue, r.getExcitationEnergy());
			}
			else {
				if(r.getExcitationEnergy() != excitationEnergyValue) {
					r.setExcitationEnergy(excitationEnergyValue);
				}
			}
		}
	}

	private void populateRegionNameCombo(List<Region> regions) {
		// file regionName combo with active regions from region list
		int index = 0;
		Region selectedRegion = getSelectedRegionInSequenceView();

		List<Region> regionsToSelect = new ArrayList<>(regions);
		regionsToSelect.remove(selectedRegion);
		regionName.removeAll();

		if (selectedRegion !=null) {
			regionName.setText(selectedRegion.getName());
		}
		if (!regionsToSelect.isEmpty()) {
			for (Region r : regionsToSelect) {
				if (r.isEnabled()) {
					regionName.add(r.getName());
					regionName.setData(String.valueOf(index), r);
					index++;
				}
			}
		}
	}

	//Add a sub groupCommand to update a feature in the model. This means it can be used in undo / redo.
	private void addCommandToGroupToUpdateFeature(EObject eObject, Object feature, Object newValue, Object oldValue) {
		if (eObject != null && editingDomain != null && !oldValue.equals(newValue)) {
			Command newCommand = SetCommand.create(editingDomain, eObject, feature, newValue);
			groupCommand.append(newCommand);
		}
	}

	//Execute groupCommand that contains all sub groupCommands when ready. Allows for clean redo / undo of group changes
	//Wipe once done for new set groupCommands to be executed next
	private void executeCommand(Command commandToExecute) {
		editingDomain.getCommandStack().execute(commandToExecute);
		if (commandToExecute.equals(groupCommand)) {
			groupCommand = new CompoundCommand();
		}

		//Fire any events stored after command has been executed so that events received use correct data.
		if (!selectionEventsToFire.isEmpty()) {
			for (ISelection selectionEvent : selectionEventsToFire) {
				fireSelectionChanged(selectionEvent);
			}
			selectionEventsToFire.clear();
		}
	}

	private SelectionAdapter regionNameSelAdapter = new SelectionAdapter() {
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// on enter - change region name
			if (!e.getSource().equals(regionName)) {
				return;
			}
			String newName = regionName.getText().trim();
			if (region.getName().equals(newName)) {
				return;
			}
			if (getRegionNames().contains(newName)) {
				MessageDialog msgDialog = new MessageDialog(getViewSite().getShell(), "Duplicated region name", null,
						"Region name must be unique in the sequence definition. Select 'No' to re-enter it again, otherwise an unique name will be generated for you",
						MessageDialog.ERROR, new String[] { "Yes", "No" }, 0);
				int result = msgDialog.open();
				if (result == 0) {
					String regionNamePrefix = StringUtils.prefixBeforeInt(regionName.getText());
					if (!regionNamePrefix.isEmpty()) {
						int largestIntInNames = StringUtils.largestIntAtEndStringsWithPrefix(getRegionNames(), regionNamePrefix);
						if (largestIntInNames != -1) {
							largestIntInNames++;
							regionName.setText(regionNamePrefix + largestIntInNames);
						}
					}
				} else {
					//must remove this duplicated new region name
					regionName.setText("");
				}
			}
			addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_Name(), regionName.getText(), region.getName());
			executeCommand(groupCommand);
		}

		// on selection from list
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(regionName)) {
				Object data = regionName.getData(String.valueOf(regionName.getSelectionIndex()));
				if (data instanceof Region r) {
					initialiseViewWithRegionData(r);
					fireSelectionChanged(r);
				}
			}
		}
	};

	private double correctExcitationEnergyLimit(double currentExcitationEnergy, double newExcitationEnergy) {
		if(regionDefinitionResourceUtil.isSourceSelectable()) {
			//If outside of range, bring it back to max limit range
			if(regionDefinitionResourceUtil.isSourceHard(currentExcitationEnergy) && regionDefinitionResourceUtil.isSourceSoft(newExcitationEnergy)) {
				newExcitationEnergy = regionDefinitionResourceUtil.getXRaySourceEnergyLimit() + 1;
			} else if (regionDefinitionResourceUtil.isSourceSoft(currentExcitationEnergy) && regionDefinitionResourceUtil.isSourceHard(newExcitationEnergy)) {
				newExcitationEnergy = regionDefinitionResourceUtil.getXRaySourceEnergyLimit() - 1;
			}
		}
		return newExcitationEnergy < 0 ? 0 : newExcitationEnergy;
	}

	protected void onModifyExcitationEnergy(SelectionEvent e) {
		if (!(e.getSource() == txtHardExcitationEnergy || e.getSource() == txtSoftExcitationEnergy)) {
			return;
		}
		double newExcitationEnergy = Double.parseDouble(((Text) e.getSource()).getText());
		newExcitationEnergy = correctExcitationEnergyLimit(region.getExcitationEnergy(), newExcitationEnergy);
		updateExcitaitonEnergyCachedPosition(newExcitationEnergy);
		updateExcitationEnergyUIValues(txtHardExcitationEnergy, hardXRayEnergy, isExcitationEnergyReadOnly());
		if (regionDefinitionResourceUtil.isSourceSelectable()) {
			updateExcitationEnergyUIValues(txtSoftExcitationEnergy, softXRayEnergy, isExcitationEnergyReadOnly());
		}
		final boolean canUndo = true;
		updateAllRegionsWithNewExcitationEnergyUpdateAndValidate(excitationEnergy, canUndo);
	}

	protected void updateExcitationEnergyUIValues(final Text textArea, final Object currentPosition, final boolean readOnly) {
		if (textArea == null || textArea.isDisposed()) {
			return;
		}
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

	private void initialiseViewWithRegionData(final Region region) {
		regionComposite.getDisplay().asyncExec(() -> initialiseRegionView(region));
	}

	private void updateTotalTime() {
		calculateTotalTime();
		addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_TotalTime(), Double.parseDouble(txtEstimatedTotalTime.getText()), region.getTotalTime());
		//Add to list of events to fire later after commands have been executed to use the correct data at time event is fired.
		selectionEventsToFire.add(new TotalTimeSelection());
	}

	private void calculateTotalTime() {
		int numberOfIterations = spinnerNumberOfIterations.getSelection();
		double calculateTotalTime = RegionStepsTimeEstimation.calculateTotalTime(Double.parseDouble(txtTime.getText()),
				Integer.parseInt(txtTotalSteps.getText()), numberOfIterations);
		txtEstimatedTotalTime.setText(String.format("%.3f", calculateTotalTime));
	}

	private void updateTotalSteps() {
		calculateTotalSteps();
		addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_TotalSteps(), Integer.parseInt(txtTotalSteps.getText()), region.getTotalSteps());
		updateTotalTime();
	}

	private void calculateTotalSteps() {
		if (btnSwept.getSelection()) {
			txtTotalSteps.setText(String.format("%d", RegionStepsTimeEstimation.calculateTotalSteps(
					Double.parseDouble(txtSpectrumEnergyWidth.getText()),
					Double.parseDouble(txtSize.getText()),
					(Double.parseDouble(txtMinimumSize.getText()) * (Integer.parseInt(spinnerEnergyChannelTo.getText())
							- Integer.parseInt(spinnerEnergyChannelFrom.getText()) + 1)))));
		}
		if (btnFixed.getSelection()) {
			txtTotalSteps.setText("1");
		}
	}

	private SelectionAdapter spectrumEnergySelectionListener = new SelectionAdapter() {
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source.equals(txtSpectrumEnergyLow) && txtSpectrumEnergyLow.isFocusControl()) {
				updateSpectrumEnergyFields(txtSpectrumEnergyLow);
			} else if (source.equals(txtSpectrumEnergyHigh) && txtSpectrumEnergyHigh.isFocusControl()) {
				updateSpectrumEnergyFields(txtSpectrumEnergyHigh);
			} else if (source.equals(txtSpectrumEnergyCentre) && txtSpectrumEnergyCentre.isFocusControl()) {
				double low = Double.parseDouble(txtSpectrumEnergyCentre.getText()) - Double.parseDouble(txtSpectrumEnergyWidth.getText()) / 2;
				txtSpectrumEnergyLow.setText(String.format(FORMAT_FLOAT, low));
				double high = Double.parseDouble(txtSpectrumEnergyCentre.getText()) + Double.parseDouble(txtSpectrumEnergyWidth.getText()) / 2;
				txtSpectrumEnergyHigh.setText(String.format(FORMAT_FLOAT, high));
				txtSpectrumEnergyCentre.setText(String.format(FORMAT_FLOAT, Double.parseDouble(txtSpectrumEnergyCentre.getText())));
				addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(), Double.parseDouble(txtSpectrumEnergyLow.getText()), region.getLowEnergy());
				addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(), Double.parseDouble(txtSpectrumEnergyHigh.getText()), region.getHighEnergy());
				addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_FixEnergy(), Double.parseDouble(txtSpectrumEnergyCentre.getText()), region.getFixEnergy());
				updateSpectrumEnergyFields(txtSpectrumEnergyCentre);
			} else if (source.equals(txtSpectrumEnergyWidth) && txtSpectrumEnergyWidth.isFocusControl()) {
				double low = Double.parseDouble(txtSpectrumEnergyCentre.getText()) - Double.parseDouble(txtSpectrumEnergyWidth.getText()) / 2;
				txtSpectrumEnergyLow.setText(String.format(FORMAT_FLOAT, low));
				double high = Double.parseDouble(txtSpectrumEnergyCentre.getText()) + Double.parseDouble(txtSpectrumEnergyWidth.getText()) / 2;
				txtSpectrumEnergyHigh.setText(String.format(FORMAT_FLOAT, high));
				txtSpectrumEnergyWidth.setText(String.format(FORMAT_FLOAT, Double.parseDouble(txtSpectrumEnergyWidth.getText())));
				double width = Double.parseDouble(txtSpectrumEnergyHigh.getText()) - Double.parseDouble(txtSpectrumEnergyLow.getText());
				txtSpectrumEnergyWidth.setText(String.format(FORMAT_FLOAT, width));
				addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(), Double.parseDouble(txtSpectrumEnergyLow.getText()), region.getLowEnergy());
				addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(), Double.parseDouble(txtSpectrumEnergyHigh.getText()), region.getHighEnergy());
				updateSpectrumEnergyFields(txtSpectrumEnergyWidth);
			}
			if (btnSwept.getSelection()) {
				sweptLowEnergy = Double.parseDouble(txtSpectrumEnergyLow.getText());
				sweptHighEnergy = Double.parseDouble(txtSpectrumEnergyHigh.getText());
			}
			if (btnFixed.getSelection()) {
				fixedSpectrumEnergyCentre = Double.parseDouble(txtSpectrumEnergyCentre.getText());
			}
			executeCommand(groupCommand);
			fireSelectionChanged(new EnergyChangedSelection(region, false));
		}
	};

	private void updateSpectrumEnergyFields(Text txt) {
		if (Double.parseDouble(txtSpectrumEnergyLow.getText()) > Double.parseDouble(txtSpectrumEnergyHigh.getText())) {
			String low = txtSpectrumEnergyHigh.getText();
			txtSpectrumEnergyHigh.setText(String.format(FORMAT_FLOAT, Double.parseDouble(txtSpectrumEnergyLow.getText())));
			txtSpectrumEnergyLow.setText(String.format(FORMAT_FLOAT, Double.parseDouble(low)));
			// TODO set lowEnergy, highEnergy to EPICS to get updated total
			// steps.
		} else {
			txt.setText(String.format(FORMAT_FLOAT, Double.parseDouble(txt.getText())));
		}
		addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(), Double.parseDouble(txtSpectrumEnergyLow.getText()), region.getLowEnergy());
		addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(), Double.parseDouble(txtSpectrumEnergyHigh.getText()), region.getHighEnergy());

		double center = (Double.parseDouble(txtSpectrumEnergyLow.getText()) + Double.parseDouble(txtSpectrumEnergyHigh.getText())) / 2;
		txtSpectrumEnergyCentre.setText(String.format(FORMAT_FLOAT, center));
		addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_FixEnergy(), Double.parseDouble(txtSpectrumEnergyCentre.getText()), region.getFixEnergy());
		double width = Double.parseDouble(txtSpectrumEnergyHigh.getText()) - Double.parseDouble(txtSpectrumEnergyLow.getText());
		txtSpectrumEnergyWidth.setText(String.format(FORMAT_FLOAT, width));
		updateTotalSteps();
	}

	private void setToFixedMode() {
		calculateFixedParameters();
		addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_FixEnergy(), fixedSpectrumEnergyCentre, region.getFixEnergy());
		addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_EnergyStep(), Double.parseDouble(txtMinimumSize.getText()), region.getEnergyStep());
		addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(), Double.parseDouble(txtSpectrumEnergyLow.getText()), region.getLowEnergy());
		addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(), Double.parseDouble(txtSpectrumEnergyHigh.getText()), region.getHighEnergy());
		addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_Slices(), spinnerNumberOfYSlices.getSelection(), region.getSlices());
		if (btnFixed.getSelection()) {
			updateTotalSteps();
		}
	}

	private void calculateFixedParameters() {
		toggleFixedModeParameters(canEdit);

		// restore the original energy step size for the FIXED
		txtSpectrumEnergyCentre.setText(String.format(FORMAT_FLOAT, fixedSpectrumEnergyCentre));
		txtSize.setText(txtMinimumSize.getText().trim());
		txtSpectrumEnergyWidth.setText(String.format(FORMAT_FLOAT,fixedEnergyRange()/1000.0));
		txtSpectrumEnergyLow.setText(String.format(FORMAT_FLOAT,Double.parseDouble(txtSpectrumEnergyCentre.getText()) - Double.parseDouble(txtSpectrumEnergyWidth.getText())/2));
		txtSpectrumEnergyHigh.setText(String.format(FORMAT_FLOAT,Double.parseDouble(txtSpectrumEnergyCentre.getText()) + Double.parseDouble(txtSpectrumEnergyWidth.getText())/2));
	}

	protected void toggleFixedModeParameters(boolean enabled) {
		txtSpectrumEnergyLow.setEditable(false);
		txtSpectrumEnergyHigh.setEditable(false);
		txtSpectrumEnergyWidth.setEditable(false);
		txtSpectrumEnergyCentre.setEditable(enabled);
		txtSize.setEditable(false);

		txtSpectrumEnergyLow.setEnabled(false);
		txtSpectrumEnergyHigh.setEnabled(false);
		txtSpectrumEnergyWidth.setEnabled(false);
		txtSpectrumEnergyCentre.setEnabled(enabled);
		txtSize.setEnabled(false);
	}

	private double fixedEnergyRange() {
		return Double.parseDouble(txtMinimumSize.getText())
				* (Integer.parseInt(spinnerEnergyChannelTo.getText()) - Integer.parseInt(spinnerEnergyChannelFrom.getText()) + 1);
	}

	private void onModifyAcquisitionMode(Object source) {
		if (source.equals(btnSwept) && btnSwept.getSelection()) {
			setToSweptMode();
			addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_EnergyStep(), sweptStepSize, region.getEnergyStep());
			addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_TotalSteps(), Integer.parseInt(txtTotalSteps.getText()), region.getTotalSteps());
			addCommandToGroupToUpdateFeature(
				region,
				RegiondefinitionPackage.eINSTANCE.getRegion_TotalTime(),
				RegionStepsTimeEstimation.calculateTotalTime(Double.parseDouble(txtTime.getText()), Integer.parseInt(txtTotalSteps.getText()), spinnerNumberOfIterations.getSelection()),
				region.getTotalTime()
			);
		} else if (source.equals(btnFixed) && btnFixed.getSelection()) {
			setToFixedMode();
			addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_EnergyStep(), Double.parseDouble(txtMinimumSize.getText().trim()), region.getEnergyStep());
			addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_TotalSteps(), Integer.parseInt(txtTotalSteps.getText()), region.getTotalSteps());
			addCommandToGroupToUpdateFeature(
				region,
				RegiondefinitionPackage.eINSTANCE.getRegion_TotalTime(),
				RegionStepsTimeEstimation.calculateTotalTime(Double.parseDouble(txtTime.getText()), Integer.parseInt(txtTotalSteps.getText()), spinnerNumberOfIterations.getSelection()),
				region.getTotalTime()
			);
		}
	}

	private void setToSweptMode() {
		calculateSweptParameters();
		if (btnSwept.getSelection()) {
			updateTotalSteps();
		}
		addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(), sweptLowEnergy, region.getLowEnergy());
		addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(), sweptHighEnergy, region.getHighEnergy());
		addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_FixEnergy(), Double.parseDouble(txtSpectrumEnergyCentre.getText()), region.getFixEnergy());
		addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_EnergyStep(), sweptStepSize, region.getEnergyStep());
		addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_Slices(), spinnerNumberOfYSlices.getSelection(), region.getSlices());
	}

	private void calculateSweptParameters() {
		toggleSweptModeParameters(canEdit);

		// restore the original energy step size for the SWEPT
		txtSpectrumEnergyLow.setText(String.format(FORMAT_FLOAT, sweptLowEnergy));
		txtSpectrumEnergyHigh.setText(String.format(FORMAT_FLOAT, sweptHighEnergy));
		txtSpectrumEnergyCentre.setText(String.format(FORMAT_FLOAT, (sweptLowEnergy + sweptHighEnergy) / 2));
		txtSpectrumEnergyWidth.setText(String.format(FORMAT_FLOAT, (sweptHighEnergy - sweptLowEnergy)));
		txtSize.setText(String.format("%.3f", sweptStepSize));
		spinnerNumberOfYSlices.setSelection(sweptSlices);
		if (txtSize.getText().isEmpty() || (Double.parseDouble(txtSize.getText()) < Double.parseDouble(txtMinimumSize.getText()))) {
			sweptStepSize = Double.parseDouble(txtMinimumSize.getText());
			txtSize.setText(String.format("%.3f", sweptStepSize));
		}
	}

	protected void toggleSweptModeParameters(boolean enabled) {
		txtSpectrumEnergyLow.setEditable(enabled);
		txtSpectrumEnergyHigh.setEditable(enabled);
		txtSpectrumEnergyWidth.setEditable(enabled);
		txtSpectrumEnergyCentre.setEditable(enabled);
		txtSize.setEditable(enabled);

		txtSpectrumEnergyLow.setEnabled(enabled);
		txtSpectrumEnergyHigh.setEnabled(enabled);
		txtSpectrumEnergyWidth.setEnabled(enabled);
		txtSpectrumEnergyCentre.setEnabled(enabled);
		txtSize.setEnabled(enabled);
	}

	private void updateEnergyStep() {
		// if (txtSize.getText().isEmpty()
		// || (Double.parseDouble(txtSize.getText()) < Double
		// .parseDouble(txtMinimumSize.getText()))) {
		// sweptStepSize = Double.parseDouble(txtMinimumSize.getText());
		// txtSize.setText(String.format("%.3f", sweptStepSize));
		// addCommandToGroupToUpdateFeature(region,
		// RegiondefinitionPackage.eINSTANCE.getRegion_EnergyStep(),
		// sweptStepSize);
		// }
		if (btnSwept.getSelection()) {
			setToSweptMode();
			updateTotalSteps();
		}
		if (btnFixed.getSelection()) {
			setToFixedMode();
		}
	}

	private void onModifyEnergyMode(Object source) {
		if (!kineticSelected && source.equals(btnKinetic) && btnKinetic.getSelection()) {
			updateSpectrumEnergyFields();
			addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_EnergyMode(), ENERGY_MODE.KINETIC, region.getEnergyMode());
			kineticSelected=true;
			executeCommand(groupCommand);
			fireSelectionChanged(new EnergyChangedSelection(region, false));
		} else if (kineticSelected && source.equals(btnBinding) && btnBinding.getSelection()) {
			updateSpectrumEnergyFields();
			addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_EnergyMode(), ENERGY_MODE.BINDING, region.getEnergyMode());
			kineticSelected=false;
			executeCommand(groupCommand);
			fireSelectionChanged(new EnergyChangedSelection(region, false));
		}
	}

	private void updateSpectrumEnergyFields() {
		double low = Double.parseDouble(txtSpectrumEnergyLow.getText());
		double high = Double.parseDouble(txtSpectrumEnergyHigh.getText());
		double centre = Double.parseDouble(txtSpectrumEnergyCentre.getText());

		double spectrumEnergyLow = excitationEnergy - high;
		double spectrumEnergyHigh = excitationEnergy - low;
		double spectrumEnergyCentre = excitationEnergy - centre;

		txtSpectrumEnergyLow.setText(String.format(FORMAT_FLOAT, spectrumEnergyLow));
		txtSpectrumEnergyHigh.setText(String.format(FORMAT_FLOAT, spectrumEnergyHigh));
		txtSpectrumEnergyCentre.setText(String.format(FORMAT_FLOAT, spectrumEnergyCentre));
		addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(), spectrumEnergyLow, region.getLowEnergy());
		addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(), spectrumEnergyHigh, region.getHighEnergy());
		addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_FixEnergy(), spectrumEnergyCentre, region.getLowEnergy());
	}

	private void initialiseRegionView(final Region region) {
		regionName.setText(region.getName());
		lensMode.setText(region.getLensMode());
		passEnergy.setText(String.valueOf(region.getPassEnergy()));
		//Acquisition
		spinnerNumberOfIterations.setSelection(region.getRunMode().getNumIterations());
		spinnerNumberOfYSlices.setSelection(region.getSlices());
		btnSwept.setSelection(region.getAcquisitionMode().getLiteral().equalsIgnoreCase("Swept"));
		btnFixed.setSelection(region.getAcquisitionMode().getLiteral().equalsIgnoreCase("Fixed"));
		//ExcitationEnergy
		updateExcitaitonEnergyCachedPosition(region.getExcitationEnergy());
		setupInitialExcitationEnergyUI(region);
		btnKinetic.setSelection(region.getEnergyMode().getLiteral().equalsIgnoreCase("Kinetic"));
		btnBinding.setSelection(region.getEnergyMode().getLiteral().equalsIgnoreCase("Binding"));
		if (btnKinetic.getSelection()) {
			kineticSelected=true;
		} else {
			kineticSelected=false;
		}
		//Spectrum energy range
		sweptLowEnergy = region.getLowEnergy();
		sweptHighEnergy = region.getHighEnergy();
		sweptStepSize = region.getEnergyStep();
		sweptSlices = region.getSlices();
		fixedSpectrumEnergyCentre = region.getFixEnergy();
		String lowLimit = regionSpectrumEnergyLimits.containsKey(region) ? regionSpectrumEnergyLimits.get(region).getKey() : "";
		String highLimit = regionSpectrumEnergyLimits.containsKey(region) ? regionSpectrumEnergyLimits.get(region).getValue() : "";
		lblSpectrumEnergyLow.setToolTipText(lowLimit);
		lblSpectrumEnergyHigh.setToolTipText(highLimit);
		//Error message
		String message = "";
		if (regionValidationMessages.containsKey(region) && region.getStatus() == STATUS.INVALID) {
			message = regionValidationMessages.get(region);
		}
		txtRegionStateValue.setText(message);
		//Step
		txtTime.setText(String.format("%.3f", region.getStepTime()));
		long frames = Math.round(Double.parseDouble(txtTime.getText()) / Double.parseDouble(txtMinimumTime.getText()));
		spinnerFrames.setSelection((int) frames);
		txtMinimumSize.setText(String.format("%.3f", camera.getEnergyResolution() * region.getPassEnergy()));
		//Detector
		spinnerEnergyChannelFrom.setSelection(region.getFirstXChannel());
		spinnerEnergyChannelTo.setSelection(region.getLastXChannel());
		spinnerYChannelFrom.setSelection(region.getFirstYChannel());
		spinnerYChannelTo.setSelection(region.getLastYChannel());
		btnADCMode.setSelection(region.getDetectorMode().getLiteral().equalsIgnoreCase("ADC"));
		btnPulseMode.setSelection(region.getDetectorMode().getLiteral().equalsIgnoreCase("Pulse Counting"));
		//Calculate remaining variables
		if (btnSwept.getSelection()) {
			calculateSweptParameters();
		} else {
			calculateFixedParameters();
		}
		calculateTotalSteps();
		calculateTotalTime();
		fireSelectionChanged(new TotalTimeSelection());
	}

	private void setupInitialExcitationEnergyUI(final Region region) {
		updateExcitationEnergyUIValues(txtHardExcitationEnergy, hardXRayEnergy, isExcitationEnergyReadOnly());
		if (regionDefinitionResourceUtil.isSourceSelectable()) {
			final boolean sourceHard = regionDefinitionResourceUtil.isSourceHard(region);
			final boolean sourceSoft = regionDefinitionResourceUtil.isSourceSoft(region);
			updateExcitationEnergyUIValues(txtSoftExcitationEnergy, softXRayEnergy, isExcitationEnergyReadOnly());
			btnHard.setSelection(sourceHard);
			btnSoft.setSelection(sourceSoft);
			txtHardExcitationEnergy.setEnabled(sourceHard && canEdit);
			txtSoftExcitationEnergy.setEnabled(sourceSoft && canEdit);
		}
		else {
			txtHardExcitationEnergy.setEnabled(canEdit);
		}
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		try {
			Region selectedRegionInSequenceView = getSelectedRegionInSequenceView();
			if (selectedRegionInSequenceView != null) {
				return new StructuredSelection(selectedRegionInSequenceView);
			} else {
				if (!regions.isEmpty()) {
					for (Region r : regions) {
						if (r.isEnabled()) {
							return new StructuredSelection(r);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error getting selection", e);
		}
		return StructuredSelection.EMPTY;
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
		if (getRegionDefinitionResourceUtil().isSourceSelectable()) {
			if(enabled) {
				if(btnHard.getSelection()) {
					txtHardExcitationEnergy.setEnabled(enabled);
				}
				else {
					txtSoftExcitationEnergy.setEnabled(enabled);
				}
			}
			else {
				txtHardExcitationEnergy.setEnabled(enabled);
				txtSoftExcitationEnergy.setEnabled(enabled);
			}
		} else {
			txtHardExcitationEnergy.setEnabled(enabled);
		}
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

	}

	private void fireSelectionChanged(Region r) {
		ISelection sel = StructuredSelection.EMPTY;
		if (r != null) {
			sel = new StructuredSelection(r);
		}
		fireSelectionChanged(sel);
	}

	private void fireSelectionChanged(ISelection sel) {
		SelectionChangedEvent event = new SelectionChangedEvent(this, sel);
		for (ISelectionChangedListener listener : selectionChangedListeners) {
			listener.selectionChanged(event);
		}
	}

	private List<String> getRegionNames() {
		return regions.stream().map(Region::getName).toList();
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

	public void setRegionDefinitionResourceUtil(RegionDefinitionResourceUtil regionDefinition) {
		this.regionDefinitionResourceUtil = regionDefinition;
	}

	public RegionDefinitionResourceUtil getRegionDefinitionResourceUtil() {
		return regionDefinitionResourceUtil;
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
}