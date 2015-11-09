/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;
import gda.exafs.xes.XesUtils;
import gda.exafs.xes.XesUtils.XesMaterial;
import gda.factory.Finder;
import gda.util.Element;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.reflection.BeansFactory;
import org.eclipse.richbeans.widgets.FieldComposite;
import org.eclipse.richbeans.widgets.file.FileBox;
import org.eclipse.richbeans.widgets.file.FileBox.ChoiceType;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.scalebox.ScaleBoxAndFixedExpression;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
import org.eclipse.richbeans.widgets.wrappers.ComboWrapper;
import org.eclipse.richbeans.widgets.wrappers.LabelWrapper;
import org.eclipse.richbeans.widgets.wrappers.LabelWrapper.TEXT_TYPE;
import org.eclipse.richbeans.widgets.wrappers.RadioWrapper;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.beans.exafs.XesScanParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentBeanManager;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.common.rcp.util.EclipseUtils;

import com.swtdesigner.SWTResourceManager;

public final class XesScanParametersComposite extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(XesScanParametersComposite.class);

	private ComboWrapper scanType;
	private Label radiusOfCurvature;
	private ScaleBox xesIntegrationTime;
	private FileBox scanFileName;
	private ScaleBoxAndFixedExpression xesInitialEnergy;
	private ScaleBoxAndFixedExpression xesFinalEnergy;
	private ScaleBox xesStepSize;
	private ScaleBox monoInitialEnergy;
	private ScaleBox monoFinalEnergy;
	private ScaleBox monoStepSize;
	private ScaleBoxAndFixedExpression xesEnergy;
	private ScaleBox monoEnergy;
	private ComboWrapper element;
	private ComboWrapper edge;
	private Link lblMonoEnergy;
	private SelectionAdapter lblMonoEnergySelectionListener;
	private Composite scanFileComposite;
	private Group xasEnergyRangeComposite;
	private Composite monoFixedEnergyComposite;
	private Group monoEnergyRangeComposite;
	private BooleanWrapper additionalCrystal0;
	private BooleanWrapper additionalCrystal1;
	private BooleanWrapper additionalCrystal2;
	private BooleanWrapper additionalCrystal3;
	private TextWrapper offsetsStoreName;
	private ValueAdapter updateListener;

	private RadioWrapper loopChoice;

	private Label lblLiveAnalyzerType;
	private Label lblAnalyserCut0;
	private Label lblAnalyserCut1;
	private Label lblAnalyserCut2;

	private int[] analyserCutValues;
	private String analyserTypeValue;
	private String radiusOfCurvatureValue;

	private Composite scanTypeComposite;
	private Group xesDataComp;

	private Color red, black;
	private Label lblFileName;
	private File editorFolder;
	private IFile editingFile;
	private Label lblL;
	private LabelWrapper L;
	private Label lblDx;
	private LabelWrapper dx;
	private Label lblDy;
	private LabelWrapper dy;

	public XesScanParametersComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));
		updateListener = new ValueAdapter("XesScanParametersComposite Listener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				try {
					updateProperties();
					scanTypeComposite.layout();
				} catch (DeviceException e1) {
					logger.error("Error trying to get latest XES spectrometer crysal values./n XES Editor will not run its calculations correctly.", e1);
				}
			}
		};

		Composite left = new Composite(this, SWT.NONE);
		left.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		left.setLayout(new GridLayout(1, false));

		Group crystallGroup = new Group(left, SWT.NONE);
		crystallGroup.setText("Analyser Properties");
		crystallGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		crystallGroup.setLayout(new GridLayout(4, false));

		Label lblAnalyzerType = new Label(crystallGroup, SWT.NONE);
		lblAnalyzerType.setText("Type");
		lblLiveAnalyzerType = new Label(crystallGroup, SWT.BORDER);

		try {
			lblLiveAnalyzerType.setText("      " + getAnalyserTypeValue()+ "      ");
			Label lblAnalyzerCut = new Label(crystallGroup, SWT.NONE);
			lblAnalyzerCut.setText("Crystal Cut");
			Group analyserCutGroup = new Group(crystallGroup, SWT.NONE);
			analyserCutGroup.setLayout(new org.eclipse.swt.layout.RowLayout());
			int[] values = getAnalyserCutValues();
			lblAnalyserCut0 = new Label(analyserCutGroup, SWT.NONE);
			lblAnalyserCut0.setText("    " + Integer.toString(values[0]));
			lblAnalyserCut1 = new Label(analyserCutGroup, SWT.NONE);
			lblAnalyserCut1.setText("    " + Integer.toString(values[1]));
			lblAnalyserCut2 = new Label(analyserCutGroup, SWT.NONE);
			lblAnalyserCut2.setText("    " + Integer.toString(values[2])+"    ");
		} catch (DeviceException e2) {
			logger.error("Error trying to get latest XES spectrometer crysal values./n XES Editor will not run its calculations correctly.", e2);
		}

		Label lblRadiusOfCurvature = new Label(crystallGroup, SWT.NONE);
		lblRadiusOfCurvature.setText("Radius");
		radiusOfCurvature = new Label(crystallGroup, SWT.BORDER);
		try {
			radiusOfCurvature.setText("      " + getRadiusOfCurvatureValue()+ "      ");
		} catch (DeviceException e2) {
			logger.error("Error trying to get latest XES spectrometer crysal values./n XES Editor will not run its calculations correctly.", e2);
		}

		Group grpCrystals = new Group(left, SWT.NONE);
		grpCrystals.setText("Analyser Crystals");
		grpCrystals.setLayout(new GridLayout(5, false));
		grpCrystals.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		Label lblCrystalChoices = new Label(grpCrystals, SWT.NONE);
		lblCrystalChoices.setText("Other Crystals In Use");

		additionalCrystal0 = new BooleanWrapper(grpCrystals, SWT.NONE);
		additionalCrystal0.addValueListener(updateListener);
		additionalCrystal0.setText("crystal -2");
		additionalCrystal1 = new BooleanWrapper(grpCrystals, SWT.NONE);
		additionalCrystal1.addValueListener(updateListener);
		additionalCrystal1.setText("crystal -1");
		additionalCrystal2 = new BooleanWrapper(grpCrystals, SWT.NONE);
		additionalCrystal2.addValueListener(updateListener);
		additionalCrystal2.setText("crystal 1");
		additionalCrystal3 = new BooleanWrapper(grpCrystals, SWT.NONE);
		additionalCrystal3.addValueListener(updateListener);
		additionalCrystal3.setText("crystal 2");

		Group offsetsGroup = new Group(left, SWT.NONE);
		offsetsGroup.setText("Spectrometer calibration");
		offsetsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		offsetsGroup.setLayout(new GridLayout(2, false));

		Label lblOffsetsStoreName = new Label(offsetsGroup, SWT.NONE);
		lblOffsetsStoreName.setText("Offsets store");
		lblOffsetsStoreName.setToolTipText("The name of the set of spectrometer motor offsets to use.\nIf not set then the current offsets will be used.");
		offsetsStoreName = new TextWrapper(offsetsGroup, SWT.BORDER);
		offsetsStoreName.setToolTipText("The name of the set of spectrometer motor offsets to use.\nIf not set then the current offsets will be used.");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(offsetsStoreName);

		Group grpScan = new Group(left, SWT.NONE);
		grpScan.setText("Scan");
		grpScan.setLayout(new GridLayout(2, false));
		grpScan.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		Label lblScanType = new Label(grpScan, SWT.NONE);
		lblScanType.setText("Type");

		scanType = new ComboWrapper(grpScan, SWT.READ_ONLY);
		GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gridData.widthHint = 200;
		scanType.setLayoutData(gridData);

		final Map<String, Object> items = new HashMap<String, Object>(4);
		items.put("Scan Ef, Fixed Eo", 1);
		items.put("Fixed Ef, Scan Eo - XAS", 2);
		items.put("Fixed Ef, Scan Eo - XANES", 3);
		items.put("Scan Ef, Scan Eo", 4);
		scanType.setItems(items);
		scanType.addValueListener(new ValueAdapter("Prepopulate XAS/XANES file") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				try {
					if (e.getValue().equals(XesScanParameters.FIXED_XES_SCAN_XAS)) {
						String name = BeansFactory.getFirstFileName(editorFolder, XasScanParameters.class);
						if (name == null) // Create one if not there
							name = ExperimentBeanManager.INSTANCE.getXmlCommandHandler(XasScanParameters.class).doCopy((IFolder) editingFile.getParent()).getName();
						scanFileName.setText(name);
					} else if (e.getValue().equals(XesScanParameters.FIXED_XES_SCAN_XANES)) {
						String name = BeansFactory.getFirstFileName(editorFolder, XanesScanParameters.class);
						if (name == null) // Create one if not there
							name = ExperimentBeanManager.INSTANCE.getXmlCommandHandler(XanesScanParameters.class).doCopy((IFolder) editingFile.getParent()).getName();
						scanFileName.setText(name);
					}
				} catch (Exception ne) {
					logger.error("Cannot get bean file", ne);
				}
			}
		});
		scanType.addValueListener(updateListener);
		Composite spacer = new Composite(grpScan, SWT.NONE);
		spacer.setLayout(new GridLayout(1, false));

		scanTypeComposite = new Composite(grpScan, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		scanTypeComposite.setLayout(gridLayout);
		scanTypeComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		scanFileComposite = new Composite(scanTypeComposite, SWT.NONE);

		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = 500;
		scanFileComposite.setLayoutData(gridData);

		GridLayout gridLayout_1 = new GridLayout(3, false);
		gridLayout_1.marginRight = 5;
		gridLayout_1.marginLeft = 5;
		gridLayout_1.marginWidth = 0;
		scanFileComposite.setLayout(gridLayout_1);

		Label label;
		lblFileName = new Label(scanFileComposite, SWT.NONE);
		lblFileName.setText("File Name");
		scanFileName = new FileBox(scanFileComposite, SWT.NONE);
		scanFileName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		scanFileName.setChoiceType(ChoiceType.NAME_ONLY);
		scanFileName.setFilterExtensions(new String[] { "*.xml" });
		scanFileName.addValueListener(new ValueAdapter("Test Eo scan file name") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				String name = (String) e.getValue();
				File file = new File(scanFileName.getFolder(), name);
				if (!file.exists())
					return;
				try {
					if (BeansFactory.isBean(file, XasScanParameters.class) && scanType.getValue().equals(XesScanParameters.FIXED_XES_SCAN_XAS))
						scanFileName.setError(false, null);
					else if (BeansFactory.isBean(file, XanesScanParameters.class) && scanType.getValue().equals(XesScanParameters.FIXED_XES_SCAN_XANES))
						scanFileName.setError(false, null);
					else {
						String fileType = scanType.getValue().equals(XesScanParameters.FIXED_XES_SCAN_XAS) ? "XAS" : "XANES";
						scanFileName.setError(true, "File chosen is not of a scan type. It must be a " + fileType + " file.");
					}
					if (file.getParent().equals(editorFolder))
						scanFileName.setError(true, "Please choose a detector file in the same folder.");
				} catch (Exception e1) {
					logger.error("Cannot get bean type of '" + file.getName() + "'.", e1);
				}
			}
		});

		Link openFile = new Link(scanFileComposite, SWT.NONE);
		openFile.setText("    <a>Open</a>");
		openFile.setToolTipText("Open monochromator scan file.");
		openFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final IFolder folder = (IFolder) XesScanParametersComposite.this.editingFile.getParent();
				final IFile scanFile = folder.getFile(scanFileName.getText());
				ExperimentFactory.getExperimentEditorManager().openEditor(scanFile);
			}
		});

		label = new Label(scanFileComposite, SWT.NONE);
		label.setText("Spectrometer Energy (Ef)");
		xesEnergy = new ScaleBoxAndFixedExpression(scanFileComposite, SWT.NONE);
		xesEnergy.setPrefix("   θ");
		xesEnergy.setLabelUnit("°");
		xesEnergy.addValueListener(updateListener);
		xesEnergy.setUnit("eV");
		xesEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		xesEnergy.setExpressionLabelTooltip("65° < θ < 85°");
		xesEnergy.setValue(3000);

		loopChoice = new RadioWrapper(scanTypeComposite, SWT.NONE, XesScanParameters.LOOPOPTIONS);

		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = 500;
		loopChoice.setLayoutData(gridData);

		loopChoice.setValue(XesScanParameters.LOOPOPTIONS[0]);
		loopChoice.setText("Loop order");

		xasEnergyRangeComposite = new Group(scanTypeComposite, SWT.NONE);
		xasEnergyRangeComposite.setText("XES Scan");

		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = 500;
		xasEnergyRangeComposite.setLayoutData(gridData);

		gridLayout_1 = new GridLayout(2, false);
		gridLayout_1.marginRight = 5;
		gridLayout_1.marginBottom = 5;
		gridLayout_1.marginLeft = 5;
		gridLayout_1.marginHeight = 0;
		gridLayout_1.marginWidth = 0;
		xasEnergyRangeComposite.setLayout(gridLayout_1);

		Label lblInitialEnergy = new Label(xasEnergyRangeComposite, SWT.NONE);
		lblInitialEnergy.setText("Initial Energy");
		xesInitialEnergy = new ScaleBoxAndFixedExpression(xasEnergyRangeComposite, SWT.NONE);
		xesInitialEnergy.setPrefix("   θ");
		xesInitialEnergy.setLabelUnit("°");
		xesInitialEnergy.addValueListener(updateListener);
		xesInitialEnergy.setUnit("eV");
		xesInitialEnergy.setExpressionLabelTooltip("65° < θ < 85°");

		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = 150;
		xesInitialEnergy.setLayoutData(gridData);

		label = new Label(xasEnergyRangeComposite, SWT.NONE);
		label.setText("Final Energy");
		xesFinalEnergy = new ScaleBoxAndFixedExpression(xasEnergyRangeComposite, SWT.NONE);
		xesFinalEnergy.setPrefix("   θ");
		xesFinalEnergy.setLabelUnit("°");
		xesFinalEnergy.addValueListener(updateListener);
		xesFinalEnergy.setUnit("eV");
		xesFinalEnergy.setExpressionLabelTooltip("65° < θ < 85°");

		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = 150;
		xesFinalEnergy.setLayoutData(gridData);

		label = new Label(xasEnergyRangeComposite, SWT.NONE);
		label.setText("Step Size");
		xesStepSize = new ScaleBox(xasEnergyRangeComposite, SWT.NONE);
		xesStepSize.setUnit("eV");

		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = 150;
		xesStepSize.setLayoutData(gridData);

		label = new Label(xasEnergyRangeComposite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("Integration Time");
		xesIntegrationTime = new ScaleBox(xasEnergyRangeComposite, SWT.NONE);
		xesIntegrationTime.setUnit("s");
		xesIntegrationTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		monoFixedEnergyComposite = new Composite(scanTypeComposite, SWT.NONE);

		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = 500;
		monoFixedEnergyComposite.setLayoutData(gridData);

		gridLayout_1 = new GridLayout(6, false);
		gridLayout_1.marginRight = 5;
		gridLayout_1.marginLeft = 5;
		gridLayout_1.marginWidth = 0;
		monoFixedEnergyComposite.setLayout(gridLayout_1);

		lblMonoEnergy = new Link(monoFixedEnergyComposite, SWT.NONE);
		lblMonoEnergy.setText("<a>Mono Energy</a>");
		lblMonoEnergy.setToolTipText("Click to toggle element and edge controls for looking up edge energy.");

		monoEnergy = new ScaleBox(monoFixedEnergyComposite, SWT.NONE);

		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gridData.widthHint = 150;
		monoEnergy.setLayoutData(gridData);

		monoEnergy.setMinimum(2000.0);
		monoEnergy.setMaximum(35000.0);
		monoEnergy.setUnit("eV");

		final Label lblElement = new Label(monoFixedEnergyComposite, SWT.NONE);
		lblElement.setText("  Element");

		element = new ComboWrapper(monoFixedEnergyComposite, SWT.DROP_DOWN);

		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gridData.widthHint = 69;
		element.setLayoutData(gridData);

		element.setItems(Element.getSortedEdgeSymbols("Sc", "U"));
		element.setValue("Fe");

		final Label lblEdge = new Label(monoFixedEnergyComposite, SWT.NONE);
		lblEdge.setText("Edge");

		edge = new ComboWrapper(monoFixedEnergyComposite, SWT.READ_ONLY);

		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gridData.widthHint = 69;
		edge.setLayoutData(gridData);

		edge.setItems(Element.getElement("Fe").getAllowedEdges().toArray(new String[1]));
		edge.select(0);

		monoEnergyRangeComposite = new Group(scanTypeComposite, SWT.NONE);
		monoEnergyRangeComposite.setText("Mono Scan");

		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = 500;
		monoEnergyRangeComposite.setLayoutData(gridData);

		gridLayout_1 = new GridLayout(2, false);
		gridLayout_1.marginRight = 5;
		gridLayout_1.marginBottom = 5;
		gridLayout_1.marginLeft = 5;
		gridLayout_1.marginHeight = 0;
		gridLayout_1.marginWidth = 0;
		monoEnergyRangeComposite.setLayout(gridLayout_1);

		lblInitialEnergy = new Label(monoEnergyRangeComposite, SWT.NONE);
		lblInitialEnergy.setText("Initial Energy");
		monoInitialEnergy = new ScaleBox(monoEnergyRangeComposite, SWT.NONE);
		monoInitialEnergy.setUnit("eV");

		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = 150;
		monoInitialEnergy.setLayoutData(gridData);

		label = new Label(monoEnergyRangeComposite, SWT.NONE);
		label.setText("Final Energy");
		monoFinalEnergy = new ScaleBox(monoEnergyRangeComposite, SWT.NONE);
		monoFinalEnergy.setUnit("eV");

		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = 150;
		monoFinalEnergy.setLayoutData(gridData);

		label = new Label(monoEnergyRangeComposite, SWT.NONE);
		label.setText("Step Size");
		monoStepSize = new ScaleBox(monoEnergyRangeComposite, SWT.NONE);
		monoStepSize.setUnit("eV");

		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = 150;
		monoStepSize.setLayoutData(gridData);

		final Composite right = new Composite(this, SWT.NONE);

		gridData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gridData.widthHint = 200;
		right.setLayoutData(gridData);
		right.setLayout(new GridLayout(1, false));

		ExpandableComposite xesDiagramComposite = new ExpandableComposite(right, ExpandableComposite.COMPACT | ExpandableComposite.TWISTIE);
		xesDiagramComposite.marginWidth = 5;
		xesDiagramComposite.marginHeight = 5;
		xesDiagramComposite.setText("XES Diagram");
		xesDiagramComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

		Composite xesComp = new Composite(xesDiagramComposite, SWT.NONE);
		xesComp.setLayout(new GridLayout(1, false));

		Label xesLabel = new Label(xesComp, SWT.NONE);
		xesLabel.setImage(SWTResourceManager.getImage(getClass(), "/icons/XESDiagram.png"));
		xesDiagramComposite.setClient(xesComp);
		xesDiagramComposite.setExpanded(true);
		xesDiagramComposite.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				layout();
				right.layout();
				final ScrolledComposite sc = (ScrolledComposite) getParent();
				sc.setMinSize(computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		});

		xesDataComp = new Group(xesComp, SWT.NONE);
		xesDataComp.setText("Properties");
		xesDataComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		xesDataComp.setLayout(new GridLayout(2, false));

		lblL = new Label(xesDataComp, SWT.NONE);
		lblL.setText("L        ");

		L = new LabelWrapper(xesDataComp, SWT.NONE);
		L.setTextType(TEXT_TYPE.NUMBER_WITH_UNIT);
		L.setUnit("mm");
		L.setText("790");
		L.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblDx = new Label(xesDataComp, SWT.NONE);
		lblDx.setText("dx");

		dx = new LabelWrapper(xesDataComp, SWT.NONE);
		dx.setTextType(TEXT_TYPE.NUMBER_WITH_UNIT);
		dx.setUnit("mm");
		dx.setText("30");
		dx.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblDy = new Label(xesDataComp, SWT.NONE);
		lblDy.setText("dy");

		dy = new LabelWrapper(xesDataComp, SWT.NONE);
		dy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		dy.setUnit("mm");
		dy.setTextType(TEXT_TYPE.NUMBER_WITH_UNIT);
		dy.setText("600");

		scanType.addValueListener(new ValueAdapter("Change XES Scan Type") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateScanType();
			}
		});

		element.addValueListener(new ValueAdapter("elementListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateElement();
			}
		});

		edge.addValueListener(new ValueAdapter("edgeListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateEdge((String) e.getValue());
			}
		});

		lblElement.setVisible(false);
		element.setVisible(false);
		lblEdge.setVisible(false);
		edge.setVisible(false);

		lblMonoEnergySelectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				lblElement.setVisible(!lblElement.isVisible());
				element.setVisible(!element.isVisible());
				lblEdge.setVisible(!lblEdge.isVisible());
				edge.setVisible(!edge.isVisible());
			}
		};
		lblMonoEnergy.addSelectionListener(lblMonoEnergySelectionListener);

		createBounds();
	}


	protected void updateScanType() {
		int val = (Integer) scanType.getValue();
		setExcluded(scanFileComposite, val == XesScanParameters.FIXED_XES_SCAN_XAS || val == XesScanParameters.FIXED_XES_SCAN_XANES);
		setExcluded(xasEnergyRangeComposite, val == XesScanParameters.SCAN_XES_FIXED_MONO || val == XesScanParameters.SCAN_XES_SCAN_MONO);
		setExcluded(monoFixedEnergyComposite, val == XesScanParameters.SCAN_XES_FIXED_MONO);
		setExcluded(monoEnergyRangeComposite, val == XesScanParameters.SCAN_XES_SCAN_MONO);
		setExcluded(loopChoice, val == XesScanParameters.SCAN_XES_SCAN_MONO);
		layout();
		pack();
	}

	public void linkUI() {
		updateScanType();
		try {
			updateProperties();
		} catch (DeviceException e) {
			logger.error("Error trying to get latest XES spectrometer crysal values./n XES Editor will not run its calculations correctly.", e);
		}
	}

	private void updateProperties() throws DeviceException {
		XesMaterial material = "Si".equals(getAnalyserTypeValue()) ? XesMaterial.SILICON : XesMaterial.GERMANIUM;
		double minXESEnergy= XesUtils.getFluoEnergy(XesUtils.MAX_THETA, material, getAnalyserCutValues());
		double maxXESEnergy= XesUtils.getFluoEnergy(XesUtils.MIN_THETA, material, getAnalyserCutValues());
		xesInitialEnergy.setMinimum(minXESEnergy);
		xesInitialEnergy.setMaximum(xesFinalEnergy.getNumericValue());
		xesFinalEnergy.setMinimum(xesInitialEnergy.getNumericValue());
		xesFinalEnergy.setMaximum(maxXESEnergy);
		xesEnergy.setMinimum(minXESEnergy);
		xesEnergy.setMaximum(maxXESEnergy);
		double thetaE = updateXesTheta(xesEnergy);
		double thetaS = updateXesTheta(xesInitialEnergy);
		updateXesTheta(xesFinalEnergy);
		double theta = ((Integer) scanType.getValue() == XesScanParameters.FIXED_XES_SCAN_XAS || (Integer) scanType.getValue() == XesScanParameters.FIXED_XES_SCAN_XANES) ? thetaE : thetaS;
		double radius = Double.parseDouble(getRadiusOfCurvatureValue());
		L.setValue(XesUtils.getL(radius, theta));
		dx.setValue(XesUtils.getDx(radius, theta));
		dy.setValue(XesUtils.getDy(radius, theta));
		xesDataComp.getParent().layout();
	}

	private double updateXesTheta(ScaleBoxAndFixedExpression energyBox) throws DeviceException {
		XesMaterial material = "Si".equals(getAnalyserTypeValue()) ? XesMaterial.SILICON : XesMaterial.GERMANIUM;
		double energy = energyBox.getNumericValue();
		double theta = XesUtils.getBragg(energy, material, getAnalyserCutValues());
		energyBox.setFixedExpressionValue(theta);
		return theta;
	}

	public String getAnalyserTypeValue() throws DeviceException {
		if (analyserTypeValue == null)
			analyserTypeValue = getScannableSinglePosition("material");
		return analyserTypeValue;
	}

	public int[] getAnalyserCutValues() throws DeviceException {
		if (analyserCutValues == null) {
			int int0 = getIntegerValue("cut1");
			int int1 = getIntegerValue("cut2");
			int int2 = getIntegerValue("cut3");
			analyserCutValues = new int[] { int0, int1, int2 };
		}
		return analyserCutValues;
	}

	private String getRadiusOfCurvatureValue() throws DeviceException {
		if (radiusOfCurvatureValue == null)
			radiusOfCurvatureValue = getScannableSinglePosition("radius");
		return radiusOfCurvatureValue;
	}

	private int getIntegerValue(String scannableName) throws DeviceException {
		String pos = getScannableSinglePosition(scannableName);
		return Math.round(Float.parseFloat(pos));
	}

	private String getScannableSinglePosition(String scannableName) throws DeviceException {
		final Scannable scannable = (Scannable) Finder.getInstance().find(scannableName);
		if (scannable == null)
			throw new DeviceException( "Scannable " + scannableName + " cannot be found");
		String[] position;
		try {
			position = ScannableUtils.getFormattedCurrentPositionArray(scannable);
		} catch (DeviceException e) {
			throw new DeviceException( "Scannable " + scannableName + " position cannot be resolved.");
		}
		return position[0];
	}

	private void setExcluded(Composite comp, boolean b) {
		GridData gridData = (GridData) comp.getLayoutData();
		gridData.exclude = !b;
		comp.setVisible(b);
	}

	private void createBounds() {
		xesInitialEnergy.setMaximum(xesFinalEnergy);
		xesFinalEnergy.setMinimum(xesInitialEnergy);
		xesStepSize.setMinimum(0.01);
		xesStepSize.setMaximum(1000);
		monoInitialEnergy.setMinimum(2000d);
		monoInitialEnergy.setMaximum(monoFinalEnergy);
		monoFinalEnergy.setMinimum(monoInitialEnergy);
		monoFinalEnergy.setMaximum(35000d);
		monoStepSize.setMinimum(0.01);
		monoStepSize.setMaximum(1000);
	}

	private void updateElement() {
		Element ele = getSelectedElement();
		if (ele == null) {
			if (red == null || red.isDisposed())
				red = getShell().getDisplay().getSystemColor(SWT.COLOR_RED);
			element.setForeground(red);
			return;
		}
		if (black == null || black.isDisposed())
			black = getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK);
		element.setForeground(black);
		String currentEdge = (String) edge.getValue();
		List<String> edges = ele.getAllowedEdges();
		edge.setItems(edges.toArray(new String[edges.size()]));
		if (currentEdge == null || !edges.contains(currentEdge))
			currentEdge = edges.get(0);
		edge.select(edges.indexOf(currentEdge));
		double edgeEn = ele.getEdgeEnergy(currentEdge);
		getMonoEnergy().setValue(edgeEn); // Its in eV in Element.
	}

	private void updateEdge(final String edge) {
		Element ele = getSelectedElement();
		if (ele == null)
			return;
		final double edgeEn = ele.getEdgeEnergy(edge);
		getMonoEnergy().setValue(edgeEn); // Its in eV in Element.
	}

	@Override
	public void dispose() {
		if (lblMonoEnergy != null && !lblMonoEnergy.isDisposed())
			lblMonoEnergy.removeSelectionListener(lblMonoEnergySelectionListener);
		super.dispose();
	}

	protected Element getSelectedElement() {
		final String symbol = (String) element.getValue();
		final Element ele = Element.getElement(symbol);
		return ele;
	}

	public FieldComposite getAdditionalCrystal0() {
		return additionalCrystal0;
	}

	public FieldComposite getAdditionalCrystal1() {
		return additionalCrystal1;
	}

	public FieldComposite getAdditionalCrystal2() {
		return additionalCrystal2;
	}

	public FieldComposite getAdditionalCrystal3() {
		return additionalCrystal3;
	}

	public FieldComposite getScanType() {
		return scanType;
	}

	public FieldComposite getXesIntegrationTime() {
		return xesIntegrationTime;
	}

	public FieldComposite getScanFileName() {
		return scanFileName;
	}

	public FieldComposite getElement() {
		return element;
	}

	public FieldComposite getEdge() {
		return edge;
	}

	public FieldComposite getXesInitialEnergy() {
		return xesInitialEnergy;
	}

	public FieldComposite getXesFinalEnergy() {
		return xesFinalEnergy;
	}

	public FieldComposite getXesStepSize() {
		return xesStepSize;
	}

	public FieldComposite getMonoInitialEnergy() {
		return monoInitialEnergy;
	}

	public FieldComposite getMonoFinalEnergy() {
		return monoFinalEnergy;
	}

	public FieldComposite getMonoStepSize() {
		return monoStepSize;
	}

	public FieldComposite getXesEnergy() {
		return xesEnergy;
	}

	public FieldComposite getMonoEnergy() {
		return monoEnergy;
	}

	public RadioWrapper getLoopChoice() {
		return loopChoice;
	}

	public TextWrapper getOffsetsStoreName() {
		return offsetsStoreName;
	}

	public void setEditingInput(final IEditorInput editing) {
		editingFile = EclipseUtils.getIFile(editing);
		editorFolder = EclipseUtils.getFile(editing).getParentFile();
		scanFileName.setFolder(editorFolder);
	}

}