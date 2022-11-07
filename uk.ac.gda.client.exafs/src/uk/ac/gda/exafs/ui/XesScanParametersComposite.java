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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.richbeans.api.widget.IFieldWidget;
import org.eclipse.richbeans.widgets.FieldComposite;
import org.eclipse.richbeans.widgets.wrappers.ComboWrapper;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.exafs.xes.IXesEnergyScannable;
import gda.factory.Finder;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import uk.ac.gda.beans.exafs.ScanColourType;
import uk.ac.gda.beans.exafs.SpectrometerScanParameters;
import uk.ac.gda.beans.exafs.XesScanParameters;
import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.exafs.ui.xes.MonoFixedEnergyControls;
import uk.ac.gda.exafs.ui.xes.MonoScanRangeControls;
import uk.ac.gda.exafs.ui.xes.XesDiagramComposite;
import uk.ac.gda.exafs.ui.xes.XesScanRangeControls;
import uk.ac.gda.exafs.ui.xes.XesScanWithFileControls;

public final class XesScanParametersComposite extends Composite implements IObservable {

	private static final Logger logger = LoggerFactory.getLogger(XesScanParametersComposite.class);

	private ComboWrapper scanType;

	private XesScanRangeControls xesScanControls;
	private MonoScanRangeControls monoScanControls;
	private XesScanWithFileControls scanFileControls;
	private XesDiagramComposite diagramComposite;
	private MonoFixedEnergyControls monoFixedEnergyControls;
	private List<TextWrapper> offsetStoreNames;

	private List<IXesEnergyScannable> xesEnergyScannables;

	private XesScanParameters bean;

	private ObservableComponent obsComponent = new ObservableComponent();

	private volatile boolean guiUpdateInProgress;

	private boolean controlsCreated = false;

	public XesScanParametersComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));
	}

	public void addControls() {
		var allXesEnergyScannables = Finder.getFindablesOfType(IXesEnergyScannable.class);

		xesEnergyScannables = new ArrayList<>();
		if (bean.getSpectrometerScanParameters() != null) {
			xesEnergyScannables = bean.getSpectrometerScanParameters().stream()
				.map(SpectrometerScanParameters::getScannableName)
				.filter(allXesEnergyScannables::containsKey)
				.map(allXesEnergyScannables::get)
				.collect(Collectors.toList());
		} else {
			if (allXesEnergyScannables.containsKey(bean.getScannableName())) {
				xesEnergyScannables.add(allXesEnergyScannables.get(bean.getScannableName()));
			}
		}
		if (xesEnergyScannables.isEmpty()) {
			logger.warn("XES energy scannable name is not set in parameters, using first available one on server");
			if (!allXesEnergyScannables.isEmpty()) {
				xesEnergyScannables.add(allXesEnergyScannables.values().iterator().next());
				bean.setScannableName(xesEnergyScannables.get(0).getName());
			}
		}
		if (xesEnergyScannables.isEmpty()) {
			Label infoLabel = new Label(this, SWT.NONE);
			infoLabel.setText("Could not create view - no XesEnergyScannable object is available in the client!");
			infoLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			return;
		}

		convertBean();


		logger.info("Using XesEnergyScannable objects : {}", xesEnergyScannables);

		Composite left = new Composite(this, SWT.NONE);
		left.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		left.setLayout(new GridLayout(1, false));

		createAnalyserComposite(left);
		createSpectrometerCalibrationComposite(left);

		Group grpScan = new Group(left, SWT.NONE);
		grpScan.setText("Scan");
		grpScan.setLayout(new GridLayout(2, false));
		grpScan.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		Label lblScanType = new Label(grpScan, SWT.NONE);
		lblScanType.setText("Type");

		createScanTypeCombo(grpScan);

		Composite spacer = new Composite(grpScan, SWT.NONE);
		spacer.setLayout(new GridLayout(1, false));

		Composite scanTypeComposite = new Composite(grpScan, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);

		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		scanTypeComposite.setLayout(gridLayout);
		scanTypeComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		if (xesEnergyScannables.size()>1) {
			addScanColourControls(scanTypeComposite);
		}
		addScanFileControls(scanTypeComposite);
		addScanStepControls(scanTypeComposite);
		addMonoFixedEnergyControls(scanTypeComposite);
		addMonoEnergyRangeControls(scanTypeComposite);
		addDiagramComposite(this);

		setupListeners();

		controlsCreated = true;
	}

	/**
	 *
	 * Copy parameters for XES scan from old locations (XesScanParameters#initialXesEnergy, XesScanParameterfinalXesEnergy etc)
	 * to new {@link SpectrometerScanParameters} object.
	 * Original values to null (so they are not persisted during the XML serialization)
	 * THis is only done if {@link XesScanParameters#getSpectrometerScanParameters()} is not present.
	 */
	private void convertBean() {
		if (bean.getSpectrometerScanParameters() == null) {
			SpectrometerScanParameters params = new SpectrometerScanParameters();
			params.setInitialEnergy(bean.getXesInitialEnergy());
			params.setFinalEnergy(bean.getXesFinalEnergy());
			params.setStepSize(bean.getXesStepSize());
			params.setIntegrationTime(bean.getXesIntegrationTime());
			params.setFixedEnergy(bean.getXesEnergy());
			params.setOffsetsStoreName(bean.getOffsetsStoreName());
			params.setScannableName(bean.getScannableName());
			bean.addSpectrometerScanParameter(params);
		}
//		clear out the old parameters by setting them to null, so they aren't persisted during XML serialization
		bean.setXesInitialEnergy(null);
		bean.setXesFinalEnergy(null);
		bean.setXesStepSize(null);
		bean.setXesIntegrationTime(null);
		bean.setXesEnergy(null);
		bean.setOffsetsStoreName(null);
		bean.setScannableName(null);
	}

	private void setupListeners() {
		// Make sure observers are notified when something changes
		Stream.of(scanFileControls, xesScanControls, monoFixedEnergyControls, monoScanControls)
		.forEach(w -> w.addIObserver(this::updateEvent));

		// Add Listeners to notify observers of changes
		scanType.addValueListener(v -> updateEvent(scanType, "scan type event"));

		// Listener to update the file name when switching to XES with scan file
		// Update the visibility of the widgets for each scan type
		scanType.addValueListener(e -> {
			int xesScanType = (Integer) e.getValue();
			try {
				// Update the Mono scan file name, adjust it to point to XAS/XANES scan file
				scanFileControls.autoSetMonoFileName(xesScanType, bean.getScanFileName());
			} catch (Exception ne) {
				logger.error("Cannot get bean file", ne);
			}
			updateWidgetVisibilityForScanType();
		});

		offsetStoreNames.forEach(store -> store.addValueListener(v -> updateEvent(store, "offset name change event")));

		// 'Switch on' some widgets to make sure Richbeans widgets fire events
		scanType.on();
		offsetStoreNames.forEach(IFieldWidget::on);
	}

	/**
	 * Update bean from GUI widgets and notify observers
	 * @param source
	 * @param event
	 */
	private void updateEvent(Object source, Object event) {
		if (guiUpdateInProgress) {
			return;
		}
		updateBeanFromUi();
		updateDiagramTable();
		notifyObservers(source, event);
	}

	private void createAnalyserComposite(Composite parent) {
		String[] labels = {"", "Type", "Crystal Cut", "Radius"};

		Group crystallGroup = new Group(parent, SWT.NONE);
		crystallGroup.setText("Analyser Properties");
		crystallGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		crystallGroup.setLayout(new GridLayout(4, false));

		var gdFactory = GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).hint(120, SWT.DEFAULT);

		for(String label : labels) {
			Label l = new Label(crystallGroup, SWT.CENTER);
			l.setText(label);
			gdFactory.applyTo(l);
		}

		for(IXesEnergyScannable scn : xesEnergyScannables) {
			try {
				createAnalyserInfoRow(crystallGroup, scn, gdFactory);
			} catch (DeviceException e2) {
				logger.error("Problem setting up analyser property info for {}", scn.getName(), e2);
			}
		}
	}

	private void createAnalyserInfoRow(Composite parent, IXesEnergyScannable scn, GridDataFactory gdFactory) throws DeviceException {
		Label rowLabel = new Label(parent,SWT.CENTER);
		rowLabel.setText(scn.getName());

		Label typeValue = new Label(parent, SWT.CENTER);
		typeValue.setText(scn.getMaterialType().getDisplayString());

		String crystalCutstring = IntStream.of(scn.getCrystalCut())
			.mapToObj(String::valueOf)
			.collect(Collectors.joining(" "));

		Label cutLabel = new Label(parent, SWT.CENTER);
		cutLabel.setText(crystalCutstring);

		Label radiusOfCurvatureValue = new Label(parent, SWT.CENTER);
		radiusOfCurvatureValue.setText(Double.toString(scn.getRadius()));

		Stream.of(rowLabel, typeValue, cutLabel, radiusOfCurvatureValue).forEach(gdFactory::applyTo);
	}

	private void createSpectrometerCalibrationComposite(Composite parent) {
		Group offsetsGroup = new Group(parent, SWT.NONE);
		offsetsGroup.setText("Spectrometer offsets");
		offsetsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		offsetsGroup.setLayout(new GridLayout(2, false));

		offsetStoreNames = new ArrayList<>();
		for(var params : bean.getSpectrometerScanParameters()) {
			Label storeLabel = new Label(offsetsGroup, SWT.NONE);
			storeLabel.setText(params.getScannableName());
			storeLabel.setToolTipText("The name of the set of spectrometer motor offsets to use.\nIf not set then the current offsets will be used.");
			TextWrapper storeName = new TextWrapper(offsetsGroup, SWT.BORDER);
			storeName.setToolTipText(storeLabel.getToolTipText());
			GridDataFactory.fillDefaults().grab(true, false).applyTo(storeName);

			offsetStoreNames.add(storeName);
		}

	}

	private void createScanTypeCombo(Composite parent) {
		scanType = new ComboWrapper(parent, SWT.READ_ONLY);
		GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gridData.widthHint = 200;
		scanType.setLayoutData(gridData);

		final Map<String, Object> items = new LinkedHashMap<>();
		items.put("Scan Ef, Fixed Eo", XesScanParameters.SCAN_XES_FIXED_MONO);
		items.put("Scan Ef Region, Fixed Eo", XesScanParameters.SCAN_XES_REGION_FIXED_MONO);
		items.put("Fixed Ef, Scan Eo - XAS", XesScanParameters.FIXED_XES_SCAN_XAS);
		items.put("Fixed Ef, Scan Eo - XANES", XesScanParameters.FIXED_XES_SCAN_XANES);
		items.put("Scan Ef, Scan Eo", XesScanParameters.SCAN_XES_SCAN_MONO);

		scanType.setItems(items);
	}

	private boolean requiresXmlFile(int xesScanType) {
		return xesScanType == XesScanParameters.FIXED_XES_SCAN_XAS ||
			xesScanType == XesScanParameters.FIXED_XES_SCAN_XANES ||
			xesScanType == XesScanParameters.SCAN_XES_REGION_FIXED_MONO;
	}

	private void addScanFileControls(Composite parent) {
		scanFileControls = new XesScanWithFileControls();
		scanFileControls.setRowScannables(xesEnergyScannables);
		scanFileControls.setShowRow2Controls(false);
		scanFileControls.setRow1Suffix("(Ef)");
		if (xesEnergyScannables.size()>1) {
			scanFileControls.setShowRow2Controls(true);
			scanFileControls.setRow1Suffix("(Ef, lower)");
			scanFileControls.setRow1Suffix("(Ef, upper)");
		}
		scanFileControls.createControls(parent);
	}

	private void addScanStepControls(Composite parent) {
		xesScanControls = new XesScanRangeControls();
		xesScanControls.setRowScannables(xesEnergyScannables);
		xesScanControls.setRow1Suffix("(Ef)");
		xesScanControls.setShowRow2Controls(false);
		if (xesEnergyScannables.size()>1) {
			xesScanControls.setShowRow2Controls(true);
			xesScanControls.setRow1Suffix("(Ef, lower)");
			xesScanControls.setRow2Suffix("(Ef, upper)");
		}
		xesScanControls.createControls(parent);
	}

	private void addMonoFixedEnergyControls(Composite parent) {
		monoFixedEnergyControls = new MonoFixedEnergyControls();
		monoFixedEnergyControls.createControls(parent);
	}

	private void addMonoEnergyRangeControls(Composite parent) {
		monoScanControls = new MonoScanRangeControls();
		monoScanControls.createControls(parent);
	}

	private void addDiagramComposite(Composite parent) {
		diagramComposite = new XesDiagramComposite();
		diagramComposite.createControls(parent);
		diagramComposite.setRowScannables(xesEnergyScannables);
	}

	private Map<ScanColourType, Button> colButtons = Collections.emptyMap();

	private void addScanColourControls(Composite parent) {
		GridDataFactory gridFactory = GridDataFactory.createFrom(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		gridFactory.hint(200,SWT.DEFAULT);

		Group mainComposite = new Group(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout(1, false));
		mainComposite.setText("Scan colour options");
		gridFactory.applyTo(mainComposite);

		colButtons = new EnumMap<>(ScanColourType.class);
		for(var str : ScanColourType.values()) {
			Button but = new Button(mainComposite, SWT.RADIO);
			but.setText(str.getDescription());
			colButtons.put(str, but);
		}
		colButtons.get(ScanColourType.ONE_COLOUR).setSelection(true);
		colButtons.values().forEach(b -> b.addListener(SWT.Selection, e -> {
			updateWidgetVisibilityForScanType();
			updateBeanFromUi();
		}));
	}

	/**
	 * Loop over the 'scan colour' selection radio buttons, return the {@link ScanColourType} for the
	 * one currently selected.
	 *
	 * @return Optional {@link ScanColourType} corresponding to currently selected radio button. Empty if controls are not present.
	 */
	private Optional<ScanColourType> getSelectedColourType() {
		return colButtons.entrySet()
				.stream()
				.filter(entry -> entry.getValue().getSelection())
				.map(Entry::getKey)
				.findFirst();
	}

	/**
	 * Set the 'scan colour' radio button selection to match a colour type
	 *
	 * @param colourType
	 */
	private void setSelectedColourType(ScanColourType colourType) {
		if (colourType == null) {
			logger.warn("Cannot set colour type in the GUI - colour type is null");
			return;
		}
		if (colButtons.containsKey(colourType)) {
			colButtons.get(colourType).setSelection(true);
		} else {
			logger.warn("Cannot set colour type to {} ({}) - colour value not available in GUI", colourType.getIndex(), colourType.getDescription());
		}
	}

	private int getScanTypeFromCombo() {
		int typeIndex = scanType.getSelectionIndex();
		return typeIndex < 0 ? 0 : (int) scanType.getValue();
	}

	/**
	 * Setup the visibility of the widgets depending on currently set scan type.
	 */
	protected void updateWidgetVisibilityForScanType() {
		if (!controlsCreated || guiUpdateInProgress) {
			return;
		}
		notifyObservers(null);

		// Enable/disable the XES range file widgets and xes energy scan widgets depending on selected colour type
		getSelectedColourType().ifPresent(colourType -> {
			boolean useRow1 = colourType.useRow1Controls();
			boolean useRow2 = colourType.useRow2Controls();
			logger.debug("Colour type selected : {} (enabled rows : row1 = {}, row2 = {})", colourType.getDescription(), useRow1, useRow2);
			scanFileControls.enableXesFileControls(useRow1, useRow2);
			scanFileControls.enableXesEnergyControls(useRow1, useRow2);
			xesScanControls.enableRowControls(useRow1, useRow2);
		});

		int scanTypeVal = getScanTypeFromCombo();

		scanFileControls.setScanTypeNum(scanTypeVal);
		scanFileControls.setupWidgetsForScanType();

		// Hide/show the widgets for setting Xas/Xanes/Region file names, and fixed mono and XES energies
		scanFileControls.showMain(requiresXmlFile(scanTypeVal));

		// Hide/show the XES energy start/stop/step widgets, Mono energy start/stop/step widgets
		xesScanControls.setScanType(scanTypeVal);
		xesScanControls.setupWidgetsForScanType();

		boolean showFixedMono = scanTypeVal == XesScanParameters.SCAN_XES_REGION_FIXED_MONO || scanTypeVal == XesScanParameters.SCAN_XES_FIXED_MONO;
		monoFixedEnergyControls.showMain(showFixedMono);

		if (scanTypeVal == XesScanParameters.SCAN_XES_SCAN_MONO) {
			// 2d scan, start, stop, step for XES and Mono energy
			xesScanControls.showMain(true);
			monoScanControls.showMain(true);
		} else if (scanTypeVal == XesScanParameters.SCAN_XES_FIXED_MONO) {
			// 1d scan with start, stop, step for XES, fixed energy for mono
			xesScanControls.showMain(true);
			monoScanControls.showMain(false);
		} else {
			xesScanControls.showMain(false);
			monoScanControls.showMain(false);
		}

		// re-layout to make sure sizes adjust to accommodate shown/hidden widgets correctly.
		scanFileControls.getMainComposite().layout(true);
		xesScanControls.getMainComposite().layout(true);

		layout();
		pack();
	}

	public void linkUI() {
		updateWidgetVisibilityForScanType();
		updateDiagramTable();
	}

	private void updateDiagramTable() {
		int scanTypeVal = getScanTypeFromCombo();

		for(int row = 0; row < xesEnergyScannables.size(); row++) {
			double energy = 0;
			if (scanTypeVal == XesScanParameters.FIXED_XES_SCAN_XAS|| scanTypeVal == XesScanParameters.FIXED_XES_SCAN_XANES) {
				energy = scanFileControls.getXesEnergy(row).getNumericValue();
			} else {
				energy = xesScanControls.getInitialEnergy(row).getNumericValue();
			}

			try {
				diagramComposite.updateValues(xesEnergyScannables.get(row), energy);
			} catch (DeviceException e) {
				logger.warn("Problem updating scan ranges for row {}", row, e);
			}
		}
	}

	public void setEditingInput(final IEditorInput editing) {
		if (controlsCreated) {
			File editorFolder = EclipseUtils.getFile(editing).getParentFile();
			scanFileControls.setEditingFile(editing);
			scanFileControls.setFolder(editorFolder);
		}
	}

	public void setupUiFromBean() {
		if (guiUpdateInProgress) {
			return;
		}
		if (!controlsCreated) {
			return;
		}
		logger.debug("Update UI from bean");

		guiUpdateInProgress = true;

		try {

			scanType.setValue(bean.getScanType());
			for(int row=0; row<bean.getSpectrometerScanParameters().size(); row++) {
				SpectrometerScanParameters params = bean.getSpectrometerScanParameters().get(row);
				xesScanControls.getInitialEnergy(row).setValue(params.getInitialEnergy());
				xesScanControls.getFinalEnergy(row).setValue(params.getFinalEnergy());
				xesScanControls.getStepSize(row).setValue(params.getStepSize());
				xesScanControls.getIntegrationTime(row).setValue(params.getIntegrationTime());
				scanFileControls.getScanFileName(row).setValue(params.getScanFileName());
				scanFileControls.getXesEnergy(row).setValue(params.getFixedEnergy());
				offsetStoreNames.get(row).setValue(params.getOffsetsStoreName());
			}
			if (bean.getScanColourType() != null) {
				setSelectedColourType(bean.getScanColourType());
			}

			xesScanControls.getLoopChoice().setValue(bean.getLoopChoice());
			monoScanControls.getInitialEnergy().setValue(bean.getMonoInitialEnergy());
			monoScanControls.getFinalEnergy().setValue(bean.getMonoFinalEnergy());
			monoScanControls.getStepSize().setValue(bean.getMonoStepSize());

			monoFixedEnergyControls.getEdge().setValue(bean.getEdge());
			monoFixedEnergyControls.getElement().setValue(bean.getElement());
			monoFixedEnergyControls.getMonoEnergy().setValue(bean.getMonoEnergy());

			scanFileControls.getMonoScanFileName().setValue(bean.getScanFileName());
		} finally {
			guiUpdateInProgress = false;
		}
	}

	public void updateBeanFromUi() {
		if (guiUpdateInProgress) {
			return;
		}

		logger.debug("Update bean from UI");

		bean.setScanType((int) scanType.getValue());

		getSelectedColourType().ifPresent(scanColour -> {
			bean.setScanColourType(scanColour);
		});
		List<SpectrometerScanParameters> specParameters = IntStream.range(0, xesEnergyScannables.size())
			.mapToObj(this::getSpectrometerParams)
			.collect(Collectors.toList());

		bean.setSpectrometerScanParameters(specParameters);

		Object loopChoice = xesScanControls.getLoopChoice().getValue();
		if (loopChoice != null) {
			bean.setLoopChoice(loopChoice.toString());
		} else {
			bean.setScanColourType(ScanColourType.ONE_COLOUR);
		}
		bean.setMonoInitialEnergy(monoScanControls.getInitialEnergy().getNumericValue());
		bean.setMonoFinalEnergy(monoScanControls.getFinalEnergy().getNumericValue());
		bean.setMonoStepSize(monoScanControls.getStepSize().getNumericValue());

		bean.setEdge(monoFixedEnergyControls.getEdge().getValue().toString());
		bean.setElement(monoFixedEnergyControls.getElement().getValue().toString());
		bean.setMonoEnergy(monoFixedEnergyControls.getMonoEnergy().getNumericValue());

		bean.setScanFileName(scanFileControls.getMonoScanFileName().getText());
	}

	/**
	 * Get {@link SpectrometerScanParameters} object from current state of the GUI
	 * for row of spectrometer.
	 * @param row
	 * @return {@link SpectrometerScanParameters} object
	 */
	private SpectrometerScanParameters getSpectrometerParams(int row) {
		SpectrometerScanParameters params = new SpectrometerScanParameters();
		params.setInitialEnergy(xesScanControls.getInitialEnergy(row).getNumericValue());
		params.setFinalEnergy(xesScanControls.getFinalEnergy(row).getNumericValue());
		params.setStepSize(xesScanControls.getStepSize(row).getNumericValue());
		params.setIntegrationTime(xesScanControls.getIntegrationTime(row).getNumericValue());
		params.setFixedEnergy(scanFileControls.getXesEnergy(row).getNumericValue());
		params.setScanFileName(scanFileControls.getScanFileName(row).getText());
		params.setOffsetsStoreName(offsetStoreNames.get(row).getText());
		if (xesEnergyScannables.size()>row) {
			params.setScannableName(xesEnergyScannables.get(row).getName());
		}
		return params;
	}

	private void notifyObservers(Object source, Object event) {
		obsComponent.notifyIObservers(source, event);
	}

	private void notifyObservers(Object event) {
		obsComponent.notifyIObservers(this, event);
	}

	@Override
	public void addIObserver(IObserver observer) {
		obsComponent.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		obsComponent.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		obsComponent.deleteIObservers();
	}

	public void setBean(XesScanParameters bean) {
		this.bean = bean;
	}

	public FieldComposite getMonoScanFileName() {
		return scanFileControls.getMonoScanFileName();
	}
}