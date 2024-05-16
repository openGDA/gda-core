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
import org.opengda.detector.electronanalyser.client.selection.RegionActivationSelection;
import org.opengda.detector.electronanalyser.client.selection.RegionValidationMessage;
import org.opengda.detector.electronanalyser.client.selection.TotalTimeSelection;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUISITION_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.DETECTOR_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage;
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
	protected static final Logger logger = LoggerFactory.getLogger(RegionViewCreator.class);

	protected PageBook regionPageBook;
	protected Composite plainComposite;
	protected ScrolledComposite regionComposite;

	protected Combo regionName;
	protected Combo lensMode;
	protected Combo passEnergy;

	//Acquisition Configuration / Mode
	protected Spinner spinnerNumberOfIterations;
	protected Spinner spinnerNumberOfYSlices;
	protected Button btnFixed;
	protected Button btnSwept;

	//Excitation energy and mode
	protected Button btnHard;
	protected Button btnSoft;
	protected Text txtHardExcitationEnergy;
	protected Text txtSoftExcitationEnergy;
	protected double excitationEnergy = 0.0;
	protected double hardXRayEnergy = 5000.0; // eV
	protected double softXRayEnergy = 500.0; // eV
	protected Button btnBinding;
	protected Button btnKinetic;
	protected boolean kineticSelected;

	//Spectrum energy range
	protected Label lblSpectrumEnergyLow;
	protected Label lblSpectrumEnergyHigh;
	protected Text txtSpectrumEnergyLow;
	protected Text txtSpectrumEnergyHigh;
	protected Text txtSpectrumEnergyCentre;
	protected double fixedSpectrumEnergyCentre;
	protected Text txtSpectrumEnergyWidth;
	protected double sweptLowEnergy;
	protected double sweptHighEnergy;
	protected HashMap<Region, Pair<String,String>> regionSpectrumEnergyLimits = new HashMap<>();

	//Region error message
	protected StyledText txtRegionStateValue;
	protected HashMap<Region, String> regionValidationMessages = new HashMap<>();

	//Step
	protected Text txtEstimatedTotalTime;
	protected Text txtSize;
	protected double sweptStepSize;
	protected Text txtTotalSteps;
	protected Text txtFramesPerSecond;
	protected int sweptSlices;
	protected Text txtMinimumSize;
	protected Text txtTime;
	protected Text txtMinimumTime;
	protected Spinner spinnerFrames;

	//Detector
	protected Spinner spinnerYChannelFrom;
	protected Spinner spinnerYChannelTo;
	protected Spinner spinnerEnergyChannelFrom;
	protected Spinner spinnerEnergyChannelTo;
	protected Button btnPulseMode;
	protected Button btnADCMode;

	protected RegionDefinitionResourceUtil regionDefinitionResourceUtil;
	protected EditingDomain editingDomain = null;
	protected List<Region> regions = Collections.emptyList() ;
	protected Region region = null;

	protected Camera camera;
	protected IVGScientaAnalyserRMI analyser;

	protected List<ISelectionChangedListener> selectionChangedListeners;

	protected String sequenceViewID = SequenceViewCreator.ID;

	protected boolean canEdit = true;

	protected static final String FORMAT_FLOAT = "%.4f";

	protected ISelectionListener selectionListener = RegionViewCreator.this::detectSelectionListener;

	public RegionViewCreator() {
		setTitleToolTip("Edit parameters for selected region");
		// setContentDescription("A view for editing region parameters");
		setPartName("Region Editor");
		this.selectionChangedListeners = new ArrayList<>();
	}

	protected void detectSelectionListener(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof FileSelection fileSelection) {
			handleFileSelection(fileSelection);
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
				if (firstElement instanceof Region) {
					if (!firstElement.equals(region)) {
						region = (Region) firstElement;
						initialiseViewWithRegionData(region);
						populateRegionNameCombo(regions);
					}
				}
				regionPageBook.showPage(regionComposite);
			}
		}
	}

	protected void handleFileSelection(FileSelection fileSelection) {
		//Sequence file changed, update UI with new region data. This class should only ever load
		//in new regions/sequence data from this method.
		try {
			regions = regionDefinitionResourceUtil.getRegions(fileSelection.getFilename());
			populateRegionNameCombo(regions);
			loadRegionExcitationEnergies(regions);
			//Correct all regions to the soft or hard values.
			for (Region r : regions) {
				setInitialExcitationEnergy(r);
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

	protected void handleRegionActivationSelection(RegionActivationSelection regionActivationSelection) {
		region = regionActivationSelection.getRegion();
		regionName.setText(region.getName());
		initialiseViewWithRegionData(region);
		populateRegionNameCombo(regions);
	}

	protected void handleRegionValidationMessage(RegionValidationMessage valMessage) {
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
		createExcitationEnergyAndEnergyModeArea(rootComposite, parent);
		createSpectrumEnergyRangeArea(rootComposite);
		createRegionErrorBoxArea(rootComposite);
		createStepArea(rootComposite);
		createDetectorArea(rootComposite);

		getViewSite().setSelectionProvider(this);
		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(getSequenceViewID(), selectionListener);
		initialisation();
	}

	protected void createNameAndLensModeAndPassEnergyArea(Composite rootComposite) {
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
					updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LensMode(), lensMode.getText());
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
					updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_PassEnergy(), passEnergyIntValue);
					updateEnergyStep();
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

	protected void createAcquisitionConfigurationAndModeArea(Composite rootComposite) {
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
					updateFeature(region.getRunMode(), RegiondefinitionPackage.eINSTANCE.getRunMode_NumIterations(),
							spinnerNumberOfIterations.getSelection());
					updateTotalTime();
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
					updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_Slices(), spinnerNumberOfYSlices.getSelection());
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
					updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_AcquisitionMode(), ACQUISITION_MODE.SWEPT);
					fireSelectionChanged(new TotalTimeSelection());
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
					updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_AcquisitionMode(), ACQUISITION_MODE.FIXED);
					fireSelectionChanged(new TotalTimeSelection());
				}
			}
		};
		btnFixed.addSelectionListener(fixedSelectionListener);
	}

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

	protected void createSpectrumEnergyRangeArea(Composite rootComposite) {
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

	protected void createRegionErrorBoxArea(Composite rootComposite) {
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

	protected void calculateRegionErrorBoxSize(int width) {
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

	protected void createStepArea(Composite rootComposite) {
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
					updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_StepTime(), Double.parseDouble(txtTime.getText()));
					updateTotalTime();
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
					updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_StepTime(), Double.parseDouble(txtTime.getText()));
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
					updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_EnergyStep(), Double.parseDouble(txtSize.getText()));
					// set Total steps
					// TODO set to EPICS size PV to get total size update
					updateTotalSteps();
					if (btnSwept.getSelection()) {
						sweptStepSize = Double.parseDouble(txtSize.getText());
					}
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

	protected void createDetectorArea(Composite rootComposite) {
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
					updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_FirstXChannel(), spinnerEnergyChannelFrom.getSelection());
					if (btnFixed.getSelection()) {
						txtSize.setText(String.format("%.3f", fixedEnergyRange()));
					}
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
					updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LastXChannel(), spinnerEnergyChannelTo.getSelection());
					if (btnFixed.getSelection()) {
						txtSize.setText(String.format("%.3f", fixedEnergyRange()));
					}
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
					updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_FirstYChannel(), spinnerYChannelFrom.getSelection());
					spinnerNumberOfYSlices.setMaximum(spinnerYChannelTo.getSelection() - spinnerYChannelFrom.getSelection()+1);
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
					updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LastYChannel(), spinnerYChannelTo.getSelection());
					spinnerNumberOfYSlices.setMaximum(spinnerYChannelTo.getSelection() - spinnerYChannelFrom.getSelection()+1);
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
				if (e.getSource().equals(btnADCMode)) {
					updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_DetectorMode(), DETECTOR_MODE.ADC);
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
				if (e.getSource().equals(btnPulseMode)) {
					updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_DetectorMode(), DETECTOR_MODE.PULSE_COUNTING);
				}
			}
		});
		btnPulseMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnPulseMode.setText("Pulse Counting");
	}

	protected void openMessageBox(String title, String message, int iconStyle) {
		MessageBox dialog=new MessageBox(getSite().getShell(), iconStyle | SWT.OK);
		dialog.setText(title);
		dialog.setMessage(message);
		dialog.open();
	}

	protected Region getSelectedRegionInSequenceView() {

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

		if (regionDefinitionResourceUtil.isSourceSelectable()) {
			Double hard = null;
			Double soft = null;

			for (Region r : listOfRegions) {
				if (r.getExcitationEnergy() > regionDefinitionResourceUtil.getXRaySourceEnergyLimit()) {
					if (hard == null) hard = r.getExcitationEnergy();
				}
				else {
					if (soft == null) soft = r.getExcitationEnergy();
				}

				if (hard != null && soft != null) {
					hardXRayEnergy = hard;
					softXRayEnergy = soft;
					excitationEnergy = r.getExcitationEnergy();
					break;
				}
			}
		}
		else {
			//Single source
			hardXRayEnergy = listOfRegions.get(0).getExcitationEnergy();
			excitationEnergy = listOfRegions.get(0).getExcitationEnergy();
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
			onSelectEnergySource(e.getSource());
		}
	};

	protected void onSelectEnergySource(Object source) {
		Text txtEnergy = null;
		if (source.equals(btnHard) && btnHard.getSelection()) {
			excitationEnergy = hardXRayEnergy;
			txtEnergy = txtHardExcitationEnergy;
			txtSoftExcitationEnergy.setEnabled(false);
		} else if (source.equals(btnSoft) && btnSoft.getSelection()){
			excitationEnergy = softXRayEnergy;
			txtEnergy = txtSoftExcitationEnergy;
			txtHardExcitationEnergy.setEnabled(false);
		}
		if (txtEnergy != null) {
			txtEnergy.setEnabled(true);
			txtEnergy.setText(String.format(FORMAT_FLOAT, excitationEnergy));
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_ExcitationEnergy(), excitationEnergy);
			fireSelectionChanged(new EnergyChangedSelection(region, true));
		}
	}

	protected void updateAllRegionsWithNewExcitationEnergyUpdate(double hardEnergy, double softEnergy, boolean isFromExcitationEnergyMoving) {
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
		txtHardExcitationEnergy.setText(String.format(FORMAT_FLOAT, hardEnergy));
		excitationEnergy = hardEnergy;
		if (regionDefinitionResourceUtil.isSourceSelectable()) {
			txtSoftExcitationEnergy.setText(String.format(FORMAT_FLOAT, softEnergy));
			excitationEnergy = btnHard.getSelection() ? hardEnergy : softEnergy;
		}
	}

	protected void populateRegionNameCombo(List<Region> regions) {
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

	// Update features when it changes in Region Editor
	protected void updateFeature(EObject region, Object feature, Object value) {
		if (region != null &&  (editingDomain != null)) {
			Command setNameCmd = SetCommand.create(editingDomain, region, feature, value);
			editingDomain.getCommandStack().execute(setNameCmd);
		}
	}

	protected SelectionAdapter regionNameSelAdapter = new SelectionAdapter() {
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// on enter - change region name
			if (e.getSource().equals(regionName)) {
				String newName=regionName.getText().trim();
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
				updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_Name(), regionName.getText());
			}
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
		//If outside of range, bring it back to max limit range
		if(currentExcitationEnergy > regionDefinitionResourceUtil.getXRaySourceEnergyLimit() && newExcitationEnergy < regionDefinitionResourceUtil.getXRaySourceEnergyLimit()) {
			newExcitationEnergy = regionDefinitionResourceUtil.getXRaySourceEnergyLimit() + 1;
		} else if (currentExcitationEnergy < regionDefinitionResourceUtil.getXRaySourceEnergyLimit() && newExcitationEnergy > regionDefinitionResourceUtil.getXRaySourceEnergyLimit()) {
			newExcitationEnergy = regionDefinitionResourceUtil.getXRaySourceEnergyLimit() - 1;
		}
		return newExcitationEnergy;
	}

	protected void onModifyExcitationEnergy(SelectionEvent e) {
		if (e.getSource() == txtHardExcitationEnergy || e.getSource() == txtSoftExcitationEnergy) {
			excitationEnergy = Double.parseDouble(((Text) e.getSource()).getText());
			excitationEnergy = excitationEnergy < 0 ? 0 : excitationEnergy;

			if (regionDefinitionResourceUtil.isSourceSelectable()) {
				excitationEnergy = correctExcitationEnergyLimit(region.getExcitationEnergy(), excitationEnergy);
				if(excitationEnergy > regionDefinitionResourceUtil.getXRaySourceEnergyLimit()) {
					hardXRayEnergy = excitationEnergy;
				} else {
					softXRayEnergy = excitationEnergy;
				}
			}
			else {
				hardXRayEnergy = excitationEnergy;
			}
			updateAllRegionsWithNewExcitationEnergyUpdate(hardXRayEnergy, softXRayEnergy, true);
		}
	}

	protected void initialiseViewWithRegionData(final Region region) {
		regionComposite.getDisplay().asyncExec(() -> initialiseRegionView(region));
	}

	protected void updateTotalTime() {
		calculateTotalTime();
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_TotalTime(), Double.parseDouble(txtEstimatedTotalTime.getText()));
		fireSelectionChanged(new TotalTimeSelection());
	}

	protected void calculateTotalTime() {
		int numberOfIterations = spinnerNumberOfIterations.getSelection();
		double calculateTotalTime = RegionStepsTimeEstimation.calculateTotalTime(Double.parseDouble(txtTime.getText()),
				Integer.parseInt(txtTotalSteps.getText()), numberOfIterations);
		txtEstimatedTotalTime.setText(String.format("%.3f", calculateTotalTime));
	}

	protected void updateTotalSteps() {
		calculateTotalSteps();
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_TotalSteps(), Integer.parseInt(txtTotalSteps.getText()));
		updateTotalTime();
	}

	protected void calculateTotalSteps() {
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

	protected SelectionAdapter spectrumEnergySelectionListener = new SelectionAdapter() {
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
				updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(), Double.parseDouble(txtSpectrumEnergyLow.getText()));
				updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(), Double.parseDouble(txtSpectrumEnergyHigh.getText()));
				updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_FixEnergy(), Double.parseDouble(txtSpectrumEnergyCentre.getText()));
				updateSpectrumEnergyFields(txtSpectrumEnergyCentre);
			} else if (source.equals(txtSpectrumEnergyWidth) && txtSpectrumEnergyWidth.isFocusControl()) {
				double low = Double.parseDouble(txtSpectrumEnergyCentre.getText()) - Double.parseDouble(txtSpectrumEnergyWidth.getText()) / 2;
				txtSpectrumEnergyLow.setText(String.format(FORMAT_FLOAT, low));
				double high = Double.parseDouble(txtSpectrumEnergyCentre.getText()) + Double.parseDouble(txtSpectrumEnergyWidth.getText()) / 2;
				txtSpectrumEnergyHigh.setText(String.format(FORMAT_FLOAT, high));
				txtSpectrumEnergyWidth.setText(String.format(FORMAT_FLOAT, Double.parseDouble(txtSpectrumEnergyWidth.getText())));
				double width = Double.parseDouble(txtSpectrumEnergyHigh.getText()) - Double.parseDouble(txtSpectrumEnergyLow.getText());
				txtSpectrumEnergyWidth.setText(String.format(FORMAT_FLOAT, width));
				updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(), Double.parseDouble(txtSpectrumEnergyLow.getText()));
				updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(), Double.parseDouble(txtSpectrumEnergyHigh.getText()));
				updateSpectrumEnergyFields(txtSpectrumEnergyWidth);
			}
			if (btnSwept.getSelection()) {
				sweptLowEnergy = Double.parseDouble(txtSpectrumEnergyLow.getText());
				sweptHighEnergy = Double.parseDouble(txtSpectrumEnergyHigh.getText());
			}
			if (btnFixed.getSelection()) {
				fixedSpectrumEnergyCentre = Double.parseDouble(txtSpectrumEnergyCentre.getText());
			}
			fireSelectionChanged(new EnergyChangedSelection(region, false));
		}
	};

	protected void updateSpectrumEnergyFields(Text txt) {
		if (Double.parseDouble(txtSpectrumEnergyLow.getText()) > Double.parseDouble(txtSpectrumEnergyHigh.getText())) {
			String low = txtSpectrumEnergyHigh.getText();
			txtSpectrumEnergyHigh.setText(String.format(FORMAT_FLOAT, Double.parseDouble(txtSpectrumEnergyLow.getText())));
			txtSpectrumEnergyLow.setText(String.format(FORMAT_FLOAT, Double.parseDouble(low)));
			// TODO set lowEnergy, highEnergy to EPICS to get updated total
			// steps.
		} else {
			txt.setText(String.format(FORMAT_FLOAT, Double.parseDouble(txt.getText())));
		}
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(), Double.parseDouble(txtSpectrumEnergyLow.getText()));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(), Double.parseDouble(txtSpectrumEnergyHigh.getText()));

		double center = (Double.parseDouble(txtSpectrumEnergyLow.getText()) + Double.parseDouble(txtSpectrumEnergyHigh.getText())) / 2;
		txtSpectrumEnergyCentre.setText(String.format(FORMAT_FLOAT, center));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_FixEnergy(), Double.parseDouble(txtSpectrumEnergyCentre.getText()));
		double width = Double.parseDouble(txtSpectrumEnergyHigh.getText()) - Double.parseDouble(txtSpectrumEnergyLow.getText());
		txtSpectrumEnergyWidth.setText(String.format(FORMAT_FLOAT, width));
		updateTotalSteps();
	}

	protected void setToFixedMode() {
		calculateFixedParameters();
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_FixEnergy(), fixedSpectrumEnergyCentre);
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_EnergyStep(), Double.parseDouble(txtMinimumSize.getText()));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(), Double.parseDouble(txtSpectrumEnergyLow.getText()));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(), Double.parseDouble(txtSpectrumEnergyHigh.getText()));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_Slices(), spinnerNumberOfYSlices.getSelection());
		if (btnFixed.getSelection()) {
			updateTotalSteps();
		}
	}

	protected void calculateFixedParameters() {
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

	protected double fixedEnergyRange() {
		return Double.parseDouble(txtMinimumSize.getText())
				* (Integer.parseInt(spinnerEnergyChannelTo.getText()) - Integer.parseInt(spinnerEnergyChannelFrom.getText()) + 1);
	}

	protected void onModifyAcquisitionMode(Object source) {
		if (source.equals(btnSwept)) {
			setToSweptMode();
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_EnergyStep(), sweptStepSize);
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_TotalSteps(), Integer.parseInt(txtTotalSteps.getText()));
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_TotalTime(),
					RegionStepsTimeEstimation.calculateTotalTime(Double.parseDouble(txtTime.getText()), Integer.parseInt(txtTotalSteps.getText()), spinnerNumberOfIterations.getSelection()));
		} else if (source.equals(btnFixed)) {
			setToFixedMode();
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_EnergyStep(), Double.parseDouble(txtMinimumSize.getText().trim()));
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_TotalSteps(), Integer.parseInt(txtTotalSteps.getText()));
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_TotalTime(),
					RegionStepsTimeEstimation.calculateTotalTime(Double.parseDouble(txtTime.getText()), Integer.parseInt(txtTotalSteps.getText()), spinnerNumberOfIterations.getSelection()));
		}
	}

	protected void setToSweptMode() {
		calculateSweptParameters();
		if (btnSwept.getSelection()) {
			updateTotalSteps();
			// txtTotalSteps
			// .setText(String.format(
			// "%d",
			// RegionStepsTimeEstimation.calculateTotalSteps(
			// Double.parseDouble(txtSpectrumEnergyWidth.getText()),
			// Double.parseDouble(txtSize.getText()),
			// (Double.parseDouble(txtMinimumSize
			// .getText()) * (Integer
			// .parseInt(spinnerEnergyChannelTo
			// .getText())
			// - Integer
			// .parseInt(spinnerEnergyChannelFrom
			// .getText()) + 1)))));
			// double calculateTotalTime = RegionStepsTimeEstimation
			// .calculateTotalTime(Double.parseDouble(txtTime.getText()),
			// Integer.parseInt(txtTotalSteps.getText()));
			// txtEstimatedTotalTime.setText(String.format("%.3f", calculateTotalTime));
		}
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(), sweptLowEnergy);
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(), sweptHighEnergy);
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_FixEnergy(), Double.parseDouble(txtSpectrumEnergyCentre.getText()));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_EnergyStep(), sweptStepSize);
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_Slices(), spinnerNumberOfYSlices.getSelection());
	}

	protected void calculateSweptParameters() {
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

	protected void updateEnergyStep() {
		// if (txtSize.getText().isEmpty()
		// || (Double.parseDouble(txtSize.getText()) < Double
		// .parseDouble(txtMinimumSize.getText()))) {
		// sweptStepSize = Double.parseDouble(txtMinimumSize.getText());
		// txtSize.setText(String.format("%.3f", sweptStepSize));
		// updateFeature(region,
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

	protected void onModifyEnergyMode(Object source) {
		if (!kineticSelected && source.equals(btnKinetic) && btnKinetic.getSelection()) {
			updateSpectrumEnergyFields();
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_EnergyMode(), ENERGY_MODE.KINETIC);
			kineticSelected=true;
			fireSelectionChanged(new EnergyChangedSelection(region, false));
		} else if (kineticSelected && source.equals(btnBinding) && btnBinding.getSelection()) {
			updateSpectrumEnergyFields();
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_EnergyMode(), ENERGY_MODE.BINDING);
			kineticSelected=false;
			fireSelectionChanged(new EnergyChangedSelection(region, false));
		}
	}

	protected void updateSpectrumEnergyFields() {
		double low = Double.parseDouble(txtSpectrumEnergyLow.getText());
		double high = Double.parseDouble(txtSpectrumEnergyHigh.getText());
		double center = Double.parseDouble(txtSpectrumEnergyCentre.getText());
		//excitationEnergy = getExcitationEnery(); //update this value from beamline
		txtSpectrumEnergyLow.setText(String.format(FORMAT_FLOAT, excitationEnergy - high));
		txtSpectrumEnergyHigh.setText(String.format(FORMAT_FLOAT, (excitationEnergy - low)));
		txtSpectrumEnergyCentre.setText(String.format(FORMAT_FLOAT, (excitationEnergy - center)));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(), Double.parseDouble(txtSpectrumEnergyLow.getText()));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(), Double.parseDouble(txtSpectrumEnergyHigh.getText()));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_FixEnergy(), Double.parseDouble(txtSpectrumEnergyCentre.getText()));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_ExcitationEnergy(), excitationEnergy);
	}

	protected void initialiseRegionView(final Region region) {
		regionName.setText(region.getName());
		lensMode.setText(region.getLensMode());
		passEnergy.setText(String.valueOf(region.getPassEnergy()));

		//Acquisition
		spinnerNumberOfIterations.setSelection(region.getRunMode().getNumIterations());
		spinnerNumberOfYSlices.setSelection(region.getSlices());
		btnSwept.setSelection(region.getAcquisitionMode().getLiteral().equalsIgnoreCase("Swept"));
		btnFixed.setSelection(region.getAcquisitionMode().getLiteral().equalsIgnoreCase("Fixed"));

		//ExcitationEnergy
		setInitialExcitationEnergy(region);
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
		if (regionValidationMessages.containsKey(region)) {
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

	protected void setInitialExcitationEnergy(final Region region) {
		if (regionDefinitionResourceUtil.isSourceSelectable()) {
			if (region.getExcitationEnergy() > regionDefinitionResourceUtil.getXRaySourceEnergyLimit()) {
				excitationEnergy = hardXRayEnergy;
			} else {
				excitationEnergy = softXRayEnergy;
			}
		} else {
			excitationEnergy = hardXRayEnergy;
		}
	}

	protected void setupInitialExcitationEnergyUI(final Region region) {
		if (regionDefinitionResourceUtil.isSourceSelectable()) {
			if (region.getExcitationEnergy() > regionDefinitionResourceUtil.getXRaySourceEnergyLimit()) {
				btnHard.setSelection(true);
				btnSoft.setSelection(false);
				txtHardExcitationEnergy.setEnabled(true);
				txtSoftExcitationEnergy.setEnabled(false);
			} else {
				btnHard.setSelection(false);
				btnSoft.setSelection(true);
				txtSoftExcitationEnergy.setEnabled(true);
				txtHardExcitationEnergy.setEnabled(false);
			}
			txtSoftExcitationEnergy.setText(String.format(FORMAT_FLOAT, softXRayEnergy));
		}
		txtHardExcitationEnergy.setText(String.format(FORMAT_FLOAT, hardXRayEnergy));
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

	protected void fireSelectionChanged(Region r) {
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

	protected List<String> getRegionNames() {
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

	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}
}