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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
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
	protected Spinner numberOfIterationSpinner;
	protected Spinner spinnerSlices;
	protected Button btnFixed;
	protected Button btnSwept;

	//Excitation energy and mode
	protected Text txtExcitationEnergy;
	protected double excitationEnergy = 0.0;
	protected Button btnBinding;
	protected Button btnKinetic;
	protected boolean kineticSelected;

	//Spectrum energy range
	protected Label lblLow;
	protected Label lblHigh;
	protected Text txtLow;
	protected Text txtHigh;
	protected Text txtCenter;
	protected double fixedCentreEnergy;
	protected Text txtWidth;
	protected HashMap<Region, Pair<String,String>> regionSpectrumEnergyLimits = new HashMap<>();

	//Region error message
	protected StyledText txtRegionStateValue;
	protected HashMap<Region, String> regionValidationMessages = new HashMap<>();

	//Step
	protected Text txtTotalTime;
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
	protected List<Region> regions;
	protected Region region;

	protected Camera camera;
	protected IVGScientaAnalyserRMI analyser;

	protected List<ISelectionChangedListener> selectionChangedListeners;

	protected String sequenceViewID = SequenceViewCreator.ID;

	protected ISelectionListener selectionListener = (part, selection) -> {

		if (selection instanceof FileSelection fileSelection) {
			populateRegionNameCombo(regions);
			// sequence file changed
			try {
				regionDefinitionResourceUtil.setFileName(fileSelection.getFilename());

				regions = regionDefinitionResourceUtil.getRegions();
				if (regions.isEmpty()) {
					region = null;
					regionPageBook.showPage(plainComposite);
				} else {
					regionPageBook.showPage(regionComposite);
					if (regionName.getItemCount() > 0) {
						// if there is enabled regions
						region = (Region) regionName.getData("0");
						initialiseViewWithRegionData(region);
						fireSelectionChanged(region);
					} else {
						// no enabled region
						region = regions.get(0);
						initialiseViewWithRegionData(region);
						fireSelectionChanged(region);
					}
				}
			} catch (Exception e) {
				logger.error("Cannot get regions list from {}", regionDefinitionResourceUtil.getFileName(), e);
			}
		} else if (selection instanceof RegionActivationSelection regionActivationSelection) {
			region = regionActivationSelection.getRegion();
			regionName.setText(region.getName());
			initialiseViewWithRegionData(region);
			populateRegionNameCombo(regions);
		} else if (selection instanceof RegionValidationMessage valMessage){
			Region targetRegion = valMessage.getRegion();
			String message = valMessage.getMessage();

			regionValidationMessages.put(targetRegion, message);

			String lowLimitTooltip = "Lower limit = "
				+ (targetRegion.getEnergyMode() == ENERGY_MODE.BINDING ? Double.toString(excitationEnergy - valMessage.getSpectrumEnergyLowLimit())+ " = Excitation Energy - " : "")
				+ Double.toString(valMessage.getSpectrumEnergyLowLimit());

			String highLimitTooltip = "Upper limit = "
				+ (targetRegion.getEnergyMode() == ENERGY_MODE.BINDING ? Double.toString(excitationEnergy - valMessage.getSpectrumEnergyHighLimit())+ " = Excitation Energy - " : "")
				+ Double.toString(valMessage.getSpectrumEnergyHighLimit());

			regionSpectrumEnergyLimits.put(
				targetRegion,
				new Pair<> (
					lowLimitTooltip,
					highLimitTooltip
				)
			);

			if (regionName.getText().equals(targetRegion.getName())) {
				txtRegionStateValue.setText(message);
				lblLow.setToolTipText(lowLimitTooltip);
				lblHigh.setToolTipText(highLimitTooltip);
			}

			//Remove regions that no longer exist e.g ones deleted or sequence file changed
			regionValidationMessages.keySet().retainAll(regions);
			regionSpectrumEnergyLimits.keySet().retainAll(regions);

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

	};

	public RegionViewCreator() {
		setTitleToolTip("Edit parameters for selected region");
		// setContentDescription("A view for editing region parameters");
		setPartName("Region Editor");
		this.selectionChangedListeners = new ArrayList<>();
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

		Group grpLensMode = new Group(grpTop, SWT.NONE);
		grpLensMode.setText("Lens Mode");
		GridDataFactory.fillDefaults().applyTo(grpLensMode);
		grpLensMode.setLayout(new GridLayout());
		grpLensMode.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		lensMode = new Combo(grpLensMode, SWT.READ_ONLY);
		lensMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lensMode.setToolTipText("List of available modes to select");

		Group grpPassEnergy = new Group(grpTop, SWT.NONE);
		grpPassEnergy.setLayout(new GridLayout());
		grpPassEnergy.setText("Pass Energy");
		grpPassEnergy.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		passEnergy = new Combo(grpPassEnergy, SWT.READ_ONLY);
		passEnergy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		passEnergy.setToolTipText("Select a pass energy to use");

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

		numberOfIterationSpinner = new Spinner(grpRunMode, SWT.BORDER);
		numberOfIterationSpinner.setMinimum(1);
		numberOfIterationSpinner.setMaximum(Integer.MAX_VALUE);
		numberOfIterationSpinner.setToolTipText("Set number of iterations required");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(numberOfIterationSpinner);

		Label lblSclies = new Label(grpRunMode, SWT.NONE);
		lblSclies.setText("Number of Y Slices:");

		spinnerSlices = new Spinner(grpRunMode, SWT.BORDER);
		spinnerSlices.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(spinnerSlices)) {
					updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_Slices(), spinnerSlices.getSelection());
				}
			}
		});
		spinnerSlices.setToolTipText("Set number of slices required");
		spinnerSlices.setMinimum(1);
		spinnerSlices.setMaximum(camera.getCameraYSize());
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(spinnerSlices);

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

		btnFixed = new Button(grpAcquisitionMode, SWT.RADIO);
		btnFixed.setText("Fixed");
	}

	protected void createExcitationEnergyAndEnergyModeArea(Composite rootComposite, Composite parent) {
		Composite energyComposite = new Composite(rootComposite, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(energyComposite);
		GridLayout energylayout = new GridLayout(2, false);
		energylayout.marginWidth = 0;
		energyComposite.setLayout(energylayout);

		Group grpExcitationEnergy = new Group(energyComposite, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(grpExcitationEnergy);
		grpExcitationEnergy.setText("Excitation Energy [eV]");
		grpExcitationEnergy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpExcitationEnergy.setLayout(new GridLayout(2, false));
		grpExcitationEnergy.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		Label lblCurrentValue = new Label(grpExcitationEnergy, SWT.NONE);
		lblCurrentValue.setText("X-ray energy:");

		txtExcitationEnergy = new Text(grpExcitationEnergy, SWT.BORDER | SWT.READ_ONLY);
		txtExcitationEnergy.setToolTipText("Cached X-ray beam energy");
		txtExcitationEnergy.setEnabled(true);
		txtExcitationEnergy.setEditable(true);
		txtExcitationEnergy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(txtExcitationEnergy);
		txtExcitationEnergy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (e.getSource().equals(txtExcitationEnergy)) {
					updateExcitationEnergy(txtExcitationEnergy);
				}
			}
		});

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

		lblLow = new Label(grpEnergy, SWT.NONE);
		lblLow.setText("Low");

		txtLow = new Text(grpEnergy, SWT.BORDER | SWT.SINGLE);
		GridData lowLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		txtLow.setLayoutData(lowLayoutData);
		txtLow.setToolTipText("Start energy");

		Label lblCenter = new Label(grpEnergy, SWT.NONE);
		lblCenter.setText("Center");

		txtCenter = new Text(grpEnergy, SWT.BORDER | SWT.SINGLE);
		GridData centerLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		txtCenter.setLayoutData(centerLayoutData);
		txtCenter.setToolTipText("Center/Fixed energy");

		lblHigh = new Label(grpEnergy, SWT.NONE);
		lblHigh.setText("High");

		txtHigh = new Text(grpEnergy, SWT.BORDER | SWT.SINGLE);
		GridData highLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		txtHigh.setLayoutData(highLayoutData);
		txtHigh.setToolTipText("Stop energy");

		Label lblWidth = new Label(grpEnergy, SWT.NONE);
		lblWidth.setText("Width");

		txtWidth = new Text(grpEnergy, SWT.BORDER | SWT.SINGLE);
		txtWidth.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtWidth.setToolTipText("Energy width");
	}

	protected void createRegionErrorBoxArea(Composite rootComposite) {
		Group grpRegionValidation = new Group(rootComposite, SWT.NONE);
		grpRegionValidation.setText("Error message");
		GridDataFactory.fillDefaults().applyTo(grpRegionValidation);
		grpRegionValidation.setLayout(new GridLayout());
		grpRegionValidation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpRegionValidation.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		txtRegionStateValue = new StyledText(grpRegionValidation, SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
		txtRegionStateValue.setForeground(new Color(255,0,0));
		txtRegionStateValue.setEditable(false);
		GridData regionStateGridData = new GridData(GridData.FILL_BOTH);
		regionStateGridData.heightHint = 2 * txtRegionStateValue.getLineHeight();
		txtRegionStateValue.setLayoutData(regionStateGridData);
		int margin = 8;
		txtRegionStateValue.setMargins(margin, margin, margin, margin);

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

		Label lblMinimumSize = new Label(grpStep, SWT.NONE);
		lblMinimumSize.setText("Min. Size [meV]");

		txtMinimumSize = new Text(grpStep, SWT.BORDER);
		txtMinimumSize.setToolTipText("Minimum energy size per step allowed");
		txtMinimumSize.setEditable(false);
		txtMinimumSize.setEnabled(false);

		Label lblTotalTime = new Label(grpStep, SWT.NONE);
		lblTotalTime.setText("Estimated Time [s]");

		txtTotalTime = new Text(grpStep, SWT.BORDER);
		txtTotalTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtTotalTime.setToolTipText("Anticipated total time for this collection");
		txtTotalTime.setEditable(false);
		txtTotalTime.setEnabled(false);

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
					spinnerSlices.setMaximum(spinnerYChannelTo.getSelection() - spinnerYChannelFrom.getSelection()+1);
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
					spinnerSlices.setMaximum(spinnerYChannelTo.getSelection() - spinnerYChannelFrom.getSelection()+1);
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
					.map(energy -> energy.toString())
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
		regions = Collections.emptyList();
		try {
			regions = regionDefinitionResourceUtil.getRegions();
		} catch (Exception e1) {
			logger.error("Cannot get regions from resource: ", e1);
		}

		if (regions.isEmpty()) {
			// open an empty sequence - no region
			region = null;
			new Label(plainComposite, SWT.None).setText("There is no regions to be displayed in this sequence.");
			regionPageBook.showPage(plainComposite);
		} else {
			new Label(plainComposite, SWT.None).setText("There is no region selected in this sequence.");
			regionPageBook.showPage(regionComposite);
			populateRegionNameCombo(regions);

			Region selectedRegionInSequenceView = getSelectedRegionInSequenceView();

			//Ensure each region is showing correct valid colour for pgmenergy/dcmenergy
			for (Region r : regions) {
				initialiseRegionView(r);
				fireSelectionChanged(r);

				if (excitationEnergy != r.getExcitationEnergy()) {
					updateFeature(r, RegiondefinitionPackage.eINSTANCE.getRegion_ExcitationEnergy(), excitationEnergy);
				}
			}
			if (selectedRegionInSequenceView != null) {
				// open Region editor when sequence editor is already available
				initialiseRegionView(selectedRegionInSequenceView);
			} else if (regionName.getItemCount() > 0) {
				// if there is enabled regions
				region = (Region) regionName.getData("0");
				initialiseRegionView(region);
				fireSelectionChanged(region);
			} else {
				// no enabled region
				region = regions.get(0);
				initialiseRegionView(region);
				fireSelectionChanged(region);
			}
		}

		// add listener after initialisation otherwise return 'empty String'
		passEnergy.addSelectionListener(passEnerySelectionAdapter);
		btnSwept.addSelectionListener(sweptSelectionListener);
		btnFixed.addSelectionListener(fixedSelectionListener);
		txtLow.addSelectionListener(energySelectionListener);
		txtHigh.addSelectionListener(energySelectionListener);
		txtCenter.addSelectionListener(energySelectionListener);
		txtWidth.addSelectionListener(energySelectionListener);
		spinnerFrames.addSelectionListener(framesSelectionListener);
		txtTime.addSelectionListener(timeSelectionListener);
		txtSize.addSelectionListener(sizeSelectionListener);
		txtMinimumSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		regionName.addSelectionListener(regionNameSelAdapter);
		lensMode.addSelectionListener(lensModeSelAdaptor);
		numberOfIterationSpinner.addSelectionListener(numIterationSpinnerSelAdaptor);
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
			for (Region region : regionsToSelect) {
				if (region.isEnabled()) {
					regionName.add(region.getName());
					regionName.setData(String.valueOf(index), region);
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
				if (data instanceof Region) {
					initialiseViewWithRegionData((Region) data);
					fireSelectionChanged((Region) data);
				}
			}
		}
	};

	protected List<String> getRegionNames() {
		return regions.stream().map(Region::getName).toList();
	}

	protected SelectionAdapter lensModeSelAdaptor = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(lensMode)) {
				updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LensMode(), lensMode.getText());
				fireSelectionChanged(new EnergyChangedSelection(region, false));
			}
		}
	};

	SelectionAdapter numIterationSpinnerSelAdaptor = new SelectionAdapter() {
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			if (e.getSource().equals(numberOfIterationSpinner)) {
				updateFeature(region.getRunMode(), RegiondefinitionPackage.eINSTANCE.getRunMode_NumIterations(),
						numberOfIterationSpinner.getSelection());
				updateTotalTime();
			}
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(numberOfIterationSpinner)) {
				updateFeature(region.getRunMode(), RegiondefinitionPackage.eINSTANCE.getRunMode_NumIterations(),
						numberOfIterationSpinner.getSelection());
				updateTotalTime();
			}
		}
	};

	protected void initialiseViewWithRegionData(final Region region) {
		Display display = getViewSite().getShell().getDisplay();
		if (!display.isDisposed()) {
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					initialiseRegionView(region);
				}
			});

		}
	}

	protected SelectionAdapter passEnerySelectionAdapter = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			onModifyPassEnergy(source);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			Object source = e.getSource();
			onModifyPassEnergy(source);
		}
	};

	protected void onModifyPassEnergy(Object source) {
		if (source.equals(passEnergy)) {
			String passEnergyFromCombo = passEnergy.getText();
			int passEnergyIntValue = Integer.parseInt(passEnergyFromCombo);
			txtMinimumSize.setText(String.format("%.3f", camera.getEnergyResolution() * passEnergyIntValue));
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_PassEnergy(), passEnergyIntValue);
			updateEnergyStep();
			fireSelectionChanged(new EnergyChangedSelection(region, false));
		}
	}

	protected SelectionAdapter framesSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			onModifyFrames(source);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			Object source = e.getSource();
			onModifyFrames(source);
		}
	};

	protected void onModifyFrames(Object source) {
		if (source.equals(spinnerFrames)) {
			txtTime.setText(String.format("%.3f", Double.parseDouble(txtMinimumTime.getText()) * Integer.parseInt(spinnerFrames.getText())));
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_StepTime(), Double.parseDouble(txtTime.getText()));
			updateTotalTime();
		}
	}

	protected double sweptLowEnergy;
	protected double sweptHighEnergy;
	protected SelectionAdapter sizeSelectionListener = new SelectionAdapter() {

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

	protected void updateTotalTime() {
		calculateTotalTime();
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_TotalTime(), Double.parseDouble(txtTotalTime.getText()));
		fireSelectionChanged(new TotalTimeSelection());
	}

	protected void calculateTotalTime() {
		int numberOfIterations = numberOfIterationSpinner.getSelection();
		double calculateTotalTime = RegionStepsTimeEstimation.calculateTotalTime(Double.parseDouble(txtTime.getText()),
				Integer.parseInt(txtTotalSteps.getText()), numberOfIterations);
		txtTotalTime.setText(String.format("%.3f", calculateTotalTime));
	}

	protected void updateTotalSteps() {
		calculateTotalSteps();
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_TotalSteps(), Integer.parseInt(txtTotalSteps.getText()));
		updateTotalTime();
	}

	protected void calculateTotalSteps() {
		if (btnSwept.getSelection()) {
			txtTotalSteps.setText(String.format("%d", RegionStepsTimeEstimation.calculateTotalSteps(
					Double.parseDouble(txtWidth.getText()),
					Double.parseDouble(txtSize.getText()),
					(Double.parseDouble(txtMinimumSize.getText()) * (Integer.parseInt(spinnerEnergyChannelTo.getText())
							- Integer.parseInt(spinnerEnergyChannelFrom.getText()) + 1)))));
		}
		if (btnFixed.getSelection()) {
			txtTotalSteps.setText("1");
		}
	}

	protected SelectionAdapter timeSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			Object source = e.getSource();
			onModifyTime(source);
		}
	};

	protected void onModifyTime(Object source) {
		if (source.equals(txtTime) && !txtTime.getText().isEmpty()) {
			long frames = Math.round(Double.parseDouble(txtTime.getText()) / Double.parseDouble(txtMinimumTime.getText()));
			spinnerFrames.setSelection((int) frames);
			txtTime.setText(String.format("%.3f", Double.parseDouble(txtTime.getText())));
			updateTotalTime();
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_StepTime(), Double.parseDouble(txtTime.getText()));
		}
	}

	protected SelectionAdapter energySelectionListener = new SelectionAdapter() {
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			Object source = e.getSource();
			onModifyEnergy(source);
		}
	};

	protected void onModifyEnergy(Object source) {
		if (source.equals(txtLow) && txtLow.isFocusControl()) {
			updateEnergyFields(txtLow);
		} else if (source.equals(txtHigh) && txtHigh.isFocusControl()) {
			updateEnergyFields(txtHigh);
		} else if (source.equals(txtCenter) && txtCenter.isFocusControl()) {
			double low = Double.parseDouble(txtCenter.getText()) - Double.parseDouble(txtWidth.getText()) / 2;
			txtLow.setText(String.format("%.4f", low));
			double high = Double.parseDouble(txtCenter.getText()) + Double.parseDouble(txtWidth.getText()) / 2;
			txtHigh.setText(String.format("%.4f", high));
			txtCenter.setText(String.format("%.4f", Double.parseDouble(txtCenter.getText())));
//			double width = Double.parseDouble(txtHigh.getText()) - Double.parseDouble(txtLow.getText());
//			txtWidth.setText(String.format("%.4f", width));
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(), Double.parseDouble(txtLow.getText()));
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(), Double.parseDouble(txtHigh.getText()));
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_FixEnergy(), Double.parseDouble(txtCenter.getText()));
			updateEnergyFields(txtCenter);
		} else if (source.equals(txtWidth) && txtWidth.isFocusControl()) {
			double low = Double.parseDouble(txtCenter.getText()) - Double.parseDouble(txtWidth.getText()) / 2;
			txtLow.setText(String.format("%.4f", low));
			double high = Double.parseDouble(txtCenter.getText()) + Double.parseDouble(txtWidth.getText()) / 2;
			txtHigh.setText(String.format("%.4f", high));
			txtWidth.setText(String.format("%.4f", Double.parseDouble(txtWidth.getText())));
			double width = Double.parseDouble(txtHigh.getText()) - Double.parseDouble(txtLow.getText());
			txtWidth.setText(String.format("%.4f", width));
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(), Double.parseDouble(txtLow.getText()));
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(), Double.parseDouble(txtHigh.getText()));
			updateEnergyFields(txtWidth);
		}
		if (btnSwept.getSelection()) {
			sweptLowEnergy = Double.parseDouble(txtLow.getText());
			sweptHighEnergy = Double.parseDouble(txtHigh.getText());
		}
		if (btnFixed.getSelection()) {
			fixedCentreEnergy = Double.parseDouble(txtCenter.getText());
		}
		fireSelectionChanged(new EnergyChangedSelection(region, false));
	}

	protected void updateEnergyFields(Text txt) {
		if (Double.parseDouble(txtLow.getText()) > Double.parseDouble(txtHigh.getText())) {
			String low = txtHigh.getText();
			txtHigh.setText(String.format("%.4f", Double.parseDouble(txtLow.getText())));
			txtLow.setText(String.format("%.4f", Double.parseDouble(low)));
			// TODO set lowEnergy, highEnergy to EPICS to get updated total
			// steps.
		} else {
			txt.setText(String.format("%.4f", Double.parseDouble(txt.getText())));
		}
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(), Double.parseDouble(txtLow.getText()));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(), Double.parseDouble(txtHigh.getText()));

		double center = (Double.parseDouble(txtLow.getText()) + Double.parseDouble(txtHigh.getText())) / 2;
		txtCenter.setText(String.format("%.4f", center));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_FixEnergy(), Double.parseDouble(txtCenter.getText()));
		double width = Double.parseDouble(txtHigh.getText()) - Double.parseDouble(txtLow.getText());
		txtWidth.setText(String.format("%.4f", width));
		updateTotalSteps();
	}

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

	protected void setToFixedMode() {
		calculateFixedParameters();
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_FixEnergy(), fixedCentreEnergy);
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_EnergyStep(), Double.parseDouble(txtMinimumSize.getText()));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(), Double.parseDouble(txtLow.getText()));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(), Double.parseDouble(txtHigh.getText()));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_Slices(), spinnerSlices.getSelection());
		if (btnFixed.getSelection()) {
			updateTotalSteps();
		}
	}

	protected void calculateFixedParameters() {
		txtLow.setEditable(false);
		txtHigh.setEditable(false);
		txtSize.setEditable(false);
		txtWidth.setEditable(false);
		txtLow.setEnabled(false);
		txtHigh.setEnabled(false);
		txtSize.setEnabled(false);
		txtWidth.setEnabled(false);

		// restore the original energy step size for the FIXED
		txtCenter.setText(String.format("%.4f", fixedCentreEnergy));
		txtSize.setText(txtMinimumSize.getText().trim());
		txtWidth.setText(String.format("%.4f",fixedEnergyRange()/1000.0));
		txtLow.setText(String.format("%.4f",Double.parseDouble(txtCenter.getText()) - Double.parseDouble(txtWidth.getText())/2));
		txtHigh.setText(String.format("%.4f",Double.parseDouble(txtCenter.getText()) + Double.parseDouble(txtWidth.getText())/2));
	}

	protected double fixedEnergyRange() {
		return Double.parseDouble(txtMinimumSize.getText())
				* (Integer.parseInt(spinnerEnergyChannelTo.getText()) - Integer.parseInt(spinnerEnergyChannelFrom.getText()) + 1);
	}

	protected SelectionAdapter sweptSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(btnSwept) && btnSwept.getSelection()) {
				onModifyAcquisitionMode(e.getSource());
				updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_AcquisitionMode(), ACQUISITION_MODE.SWEPT);
				fireSelectionChanged(new TotalTimeSelection());
			}
		}
	};

	protected void onModifyAcquisitionMode(Object source) {
		if (source.equals(btnSwept)) {
			setToSweptMode();
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_EnergyStep(), sweptStepSize);
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_TotalSteps(), Integer.parseInt(txtTotalSteps.getText()));
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_TotalTime(),
					RegionStepsTimeEstimation.calculateTotalTime(Double.parseDouble(txtTime.getText()), Integer.parseInt(txtTotalSteps.getText()), numberOfIterationSpinner.getSelection()));
		} else if (source.equals(btnFixed)) {
			setToFixedMode();
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_EnergyStep(), Double.parseDouble(txtMinimumSize.getText().trim()));
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_TotalSteps(), Integer.parseInt(txtTotalSteps.getText()));
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_TotalTime(),
					RegionStepsTimeEstimation.calculateTotalTime(Double.parseDouble(txtTime.getText()), Integer.parseInt(txtTotalSteps.getText()), numberOfIterationSpinner.getSelection()));
			// fireSelectionChanged(new TotalTimeSelection());
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
			// Double.parseDouble(txtWidth.getText()),
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
			// txtTotalTime.setText(String.format("%.3f", calculateTotalTime));
		}
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(), sweptLowEnergy);
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(), sweptHighEnergy);
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_FixEnergy(), Double.parseDouble(txtCenter.getText()));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_EnergyStep(), sweptStepSize);
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_Slices(), spinnerSlices.getSelection());
	}

	protected void calculateSweptParameters() {
		txtLow.setEnabled(true);
		txtHigh.setEnabled(true);
		txtSize.setEnabled(true);
		txtWidth.setEnabled(true);
		txtLow.setEditable(true);
		txtHigh.setEditable(true);
		txtSize.setEditable(true);
		txtWidth.setEditable(true);
		// restore the original energy step size for the SWEPT
		txtLow.setText(String.format("%.4f", sweptLowEnergy));
		txtHigh.setText(String.format("%.4f", sweptHighEnergy));
		txtCenter.setText(String.format("%.4f", (sweptLowEnergy + sweptHighEnergy) / 2));
		txtWidth.setText(String.format("%.4f", (sweptHighEnergy - sweptLowEnergy)));
		txtSize.setText(String.format("%.3f", sweptStepSize));
		spinnerSlices.setSelection(sweptSlices);
		if (txtSize.getText().isEmpty() || (Double.parseDouble(txtSize.getText()) < Double.parseDouble(txtMinimumSize.getText()))) {
			sweptStepSize = Double.parseDouble(txtMinimumSize.getText());
			txtSize.setText(String.format("%.3f", sweptStepSize));
		}
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
			updateEnergyFields();
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_EnergyMode(), ENERGY_MODE.KINETIC);
			kineticSelected=true;
		} else if (kineticSelected && source.equals(btnBinding) && btnBinding.getSelection()) {
			updateEnergyFields();
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_EnergyMode(), ENERGY_MODE.BINDING);
			kineticSelected=false;
		}
		fireSelectionChanged(new EnergyChangedSelection(region, false));
	}

	protected void updateEnergyFields() {
		double low = Double.parseDouble(txtLow.getText());
		double high = Double.parseDouble(txtHigh.getText());
		double center = Double.parseDouble(txtCenter.getText());
		//excitationEnergy = getExcitationEnery(); //update this value from beamline
		txtLow.setText(String.format("%.4f", excitationEnergy - high));
		txtHigh.setText(String.format("%.4f", (excitationEnergy - low)));
		txtCenter.setText(String.format("%.4f", (excitationEnergy - center)));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(), Double.parseDouble(txtLow.getText()));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(), Double.parseDouble(txtHigh.getText()));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_FixEnergy(), Double.parseDouble(txtCenter.getText()));
		updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_ExcitationEnergy(), excitationEnergy);
	}

	@Override
	public void dispose() {
		getViewSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(getSequenceViewID(), selectionListener);
		super.dispose();
	}

	protected void updateExcitationEnergy(Text txt) {
		excitationEnergy = Double.parseDouble(txt.getText());

		double prevExcitationEnergy = region.getExcitationEnergy();

		if (excitationEnergy != prevExcitationEnergy) {
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_ExcitationEnergy(), excitationEnergy);
			fireSelectionChanged(new EnergyChangedSelection(region, false));
		}
	}

	protected void initialiseRegionView(final Region region) {
		setExcitationEnergy(region);

		regionName.setText(region.getName());
		lensMode.setText(region.getLensMode());
		passEnergy.setText(String.valueOf(region.getPassEnergy()));
		txtMinimumSize.setText(String.format("%.3f", camera.getEnergyResolution() * region.getPassEnergy()));
		numberOfIterationSpinner.setSelection(region.getRunMode().getNumIterations());
		btnSwept.setSelection(region.getAcquisitionMode().getLiteral().equalsIgnoreCase("Swept"));
		btnFixed.setSelection(region.getAcquisitionMode().getLiteral().equalsIgnoreCase("Fixed"));
		btnKinetic.setSelection(region.getEnergyMode().getLiteral().equalsIgnoreCase("Kinetic"));
		btnBinding.setSelection(region.getEnergyMode().getLiteral().equalsIgnoreCase("Binding"));
		if (btnKinetic.getSelection()) {
			kineticSelected=true;
		} else {
			kineticSelected=false;
		}
		sweptLowEnergy=region.getLowEnergy();
		sweptHighEnergy=region.getHighEnergy();
		sweptStepSize = region.getEnergyStep();
		sweptSlices=region.getSlices();
		fixedCentreEnergy=region.getFixEnergy();
		txtTime.setText(String.format("%.3f", region.getStepTime()));
		long frames = Math.round(Double.parseDouble(txtTime.getText()) / Double.parseDouble(txtMinimumTime.getText()));
		spinnerFrames.setSelection((int) frames);
		spinnerEnergyChannelFrom.setSelection(region.getFirstXChannel());
		spinnerEnergyChannelTo.setSelection(region.getLastXChannel());
		spinnerYChannelFrom.setSelection(region.getFirstYChannel());
		spinnerYChannelTo.setSelection(region.getLastYChannel());
		spinnerSlices.setSelection(region.getSlices());
		btnADCMode.setSelection(region.getDetectorMode().getLiteral().equalsIgnoreCase("ADC"));
		btnPulseMode.setSelection(region.getDetectorMode().getLiteral().equalsIgnoreCase("Pulse Counting"));
		if (btnSwept.getSelection()) {
			calculateSweptParameters();
		} else {
			calculateFixedParameters();
		}
		calculateTotalSteps();
		calculateTotalTime();
		fireSelectionChanged(new TotalTimeSelection());

		String message = "";
		String lowLimit = "";
		String highLimit = "";

		if (regionValidationMessages.containsKey(region)) {
			message = regionValidationMessages.get(region);
		}

		if (regionSpectrumEnergyLimits.containsKey(region)) {
			lowLimit = regionSpectrumEnergyLimits.get(region).getKey();
			highLimit =  regionSpectrumEnergyLimits.get(region).getValue();
		}

		lblLow.setToolTipText(lowLimit);
		lblHigh.setToolTipText(highLimit);
		txtRegionStateValue.setText(message);
	}

	protected void setExcitationEnergy(final Region region) {

		excitationEnergy = region.getExcitationEnergy();
		txtExcitationEnergy.setText(String.format("%.4f", excitationEnergy));
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
				List<Region> regions = regionDefinitionResourceUtil.getRegions();
				if (!regions.isEmpty()) {
					for (Region region : regions) {
						if (region.isEnabled()) {
							return new StructuredSelection(region);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return StructuredSelection.EMPTY;
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection) {

	}

	protected void fireSelectionChanged(Region region) {
		ISelection sel = StructuredSelection.EMPTY;
		if (region != null) {
			sel = new StructuredSelection(region);
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