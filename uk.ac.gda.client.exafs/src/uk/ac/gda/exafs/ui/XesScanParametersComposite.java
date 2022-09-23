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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.widgets.FieldComposite;
import org.eclipse.richbeans.widgets.scalebox.ScaleBoxAndFixedExpression;
import org.eclipse.richbeans.widgets.wrappers.ComboWrapper;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.exafs.xes.IXesEnergyScannable;
import gda.exafs.xes.XesUtils;
import gda.factory.Finder;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.util.CrystalParameters.CrystalMaterial;
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

	private TextWrapper offsetsStoreName;

	private Composite scanTypeComposite;

	private IXesEnergyScannable xesEnergyScannable;

	private XesScanParameters bean;

	private ObservableComponent obsComponent = new ObservableComponent();

	private static final String XES_ERROR_MESSAGE = "Error trying to get latest XES spectrometer crysal values.\n" +
											"XES Editor will not run its calculations correctly.";

	private volatile boolean beanUpdateInProgress;
	private volatile boolean guiUpdateInProgress;

	public XesScanParametersComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		var xesScannables = Finder.getFindablesOfType(IXesEnergyScannable.class);
		if (xesScannables.isEmpty()) {
			Label infoLabel = new Label(this, SWT.NONE);
			infoLabel.setText("Could not create view - no XesEnergyScannable object is available in the client!");
			return;
		}

		xesEnergyScannable = xesScannables.values().iterator().next();
		logger.info("Using XesEnergyScannable object : {}", xesEnergyScannable.getName());

		Composite left = new Composite(this, SWT.NONE);
		left.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
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

		scanTypeComposite = new Composite(grpScan, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);

		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		scanTypeComposite.setLayout(gridLayout);
		scanTypeComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		addScanFileControls(scanTypeComposite);
		addScanStepControls(scanTypeComposite);
		addMonoFixedEnergyControls(scanTypeComposite);
		addMonoEnergyRangeControls(scanTypeComposite);
		addDiagramComposite(this);

		setupListeners();
	}

	private void setupListeners() {
		// Listeners to update range limits in energy textboxes
		xesScanControls.getInitialEnergy().addValueListener(this::updateEnergyRangeListener);
		scanFileControls.getXesEnergy().addValueListener(this::updateEnergyRangeListener);
		// Update the other mono energy textbox to match the one in file controls if it changes.
		scanFileControls.addIObserver((source, arg) -> monoFixedEnergyControls.getMonoEnergy().setValue(scanFileControls.getMonoEnergy().getValue()));

		// Make sure observers are notified when something changes
		Stream.of(scanFileControls, xesScanControls, monoFixedEnergyControls, monoScanControls)
		.forEach(w -> w.addIObserver(this::updateEvent));

		// Add Listeners to notify observers of changes
		scanType.addValueListener(v -> updateEvent(scanType, "scan type event"));

		// Listener to update the file name when switching to XES with scan file
		// Update the visibility of the widgets for each scan type
		scanType.addValueListener(e -> {
			int xesScanType = (Integer) e.getValue();
			if (requiresXmlFile(xesScanType)) {
				try {
					scanFileControls.autoSetFileName(xesScanType, bean.getScanFileName());
				} catch (Exception ne) {
					logger.error("Cannot get bean file", ne);
				}
			}
			updateWidgetVisibilityForScanType();
		});


		offsetsStoreName.addValueListener(v -> updateEvent(offsetsStoreName, "offset name change event"));

		// 'Switch on' some widgets to make sure Richbeans widgets fire events
		scanType.on();
		offsetsStoreName.on();
	}

	private void updateEnergyRangeListener(ValueEvent event) {
		try {
			updateEnergyRanges();
			scanTypeComposite.layout();
		} catch (DeviceException e1) {
			logger.error(XES_ERROR_MESSAGE, e1);
		}
	}

	/**
	 * Update bean from GUI widgets and notify observers
	 * @param source
	 * @param event
	 */
	private void updateEvent(Object source, Object event) {
		updateBeanFromUi();
		notifyObservers(source, event);
	}

	private void createAnalyserComposite(Composite parent) {
		Group crystallGroup = new Group(parent, SWT.NONE);
		crystallGroup.setText("Analyser Properties");
		crystallGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		crystallGroup.setLayout(new GridLayout(6, false));

		Label lblAnalyzerType = new Label(crystallGroup, SWT.NONE);
		lblAnalyzerType.setText("Type");
		Label lblLiveAnalyzerType = new Label(crystallGroup, SWT.BORDER);

		try {
			lblLiveAnalyzerType.setText("      " + getAnalyserCrystalMaterial().getDisplayString() + "      ");
			Label lblAnalyzerCut = new Label(crystallGroup, SWT.NONE);
			lblAnalyzerCut.setText("Crystal Cut");
			Group analyserCutGroup = new Group(crystallGroup, SWT.NONE);
			analyserCutGroup.setLayout(new org.eclipse.swt.layout.RowLayout());
			int[] values = getAnalyserCutValues();
			for(int cutValue : values) {
				Label lblAnalyserCut = new Label(analyserCutGroup, SWT.NONE);
				lblAnalyserCut.setText("    " + Integer.toString(cutValue));
			}
		} catch (DeviceException e2) {
			logger.error(XES_ERROR_MESSAGE, e2);
		}

		Label lblRadiusOfCurvature = new Label(crystallGroup, SWT.NONE);
		lblRadiusOfCurvature.setText("Radius");
		Label radiusOfCurvature = new Label(crystallGroup, SWT.BORDER);
		try {
			radiusOfCurvature.setText("      " + getRadiusOfCurvatureValue() + "      ");
		} catch (DeviceException e2) {
			logger.error(XES_ERROR_MESSAGE, e2);
		}
	}

	private void createSpectrometerCalibrationComposite(Composite parent) {
		Group offsetsGroup = new Group(parent, SWT.NONE);
		offsetsGroup.setText("Spectrometer calibration");
		offsetsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		offsetsGroup.setLayout(new GridLayout(2, false));

		Label lblOffsetsStoreName = new Label(offsetsGroup, SWT.NONE);
		lblOffsetsStoreName.setText("Offsets store");
		lblOffsetsStoreName.setToolTipText("The name of the set of spectrometer motor offsets to use.\nIf not set then the current offsets will be used.");
		offsetsStoreName = new TextWrapper(offsetsGroup, SWT.BORDER);
		offsetsStoreName.setToolTipText("The name of the set of spectrometer motor offsets to use.\nIf not set then the current offsets will be used.");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(offsetsStoreName);
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
		scanFileControls.setShowRow2Controls(false);
		scanFileControls.setRow1Suffix("(Ef)");
		scanFileControls.createControls(parent);
	}

	private void addScanStepControls(Composite parent) {
		xesScanControls = new XesScanRangeControls();
		try {
			xesScanControls.setCrystalMaterial(getAnalyserCrystalMaterial());
			xesScanControls.setCrystalCutValues(getAnalyserCutValues());
			xesScanControls.setShowRow2Controls(false);
			xesScanControls.setRow1Suffix("(Ef)");
			xesScanControls.createControls(parent);
		} catch (DeviceException e) {
			logger.error("Problem creating XES scan range controls", e);
		}
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
	}

	/**
	 * Setup the visibility of the widgets depending on currently set scan type.
	 *
	 */
	protected void updateWidgetVisibilityForScanType() {

		notifyObservers(null);
		int typeIndex = scanType.getSelectionIndex();
		int scanTypeVal = typeIndex < 0 ? 0 : (int) scanType.getValue();

		scanFileControls.setScanTypeNum(scanTypeVal);
		scanFileControls.setupWidgetsForScanType();

		// Hide/show the widgets for setting Xas/Xanes/Region file names, and fixed mono and XES energies
		scanFileControls.showMain(requiresXmlFile(scanTypeVal));

		// Hide/show the XES energy start/stop/step widgets, Mono energy start/stop/step widgets
		xesScanControls.setScanType(scanTypeVal);
		xesScanControls.setupWidgetsForScanType();

		if (scanTypeVal == XesScanParameters.SCAN_XES_SCAN_MONO) {
			// 2d scan, start, stop, step for XES and Mono energy
			xesScanControls.showMain(true);
			monoScanControls.showMain(true);
			monoFixedEnergyControls.showMain(false);
		} else if (scanTypeVal == XesScanParameters.SCAN_XES_FIXED_MONO) {
			// 1d scan with start, stop, step for XES, fixed energy for mono
			xesScanControls.showMain(true);
			monoScanControls.showMain(false);
			monoFixedEnergyControls.showMain(true);
		} else {
			xesScanControls.showMain(false);
			monoScanControls.showMain(false);
			monoFixedEnergyControls.showMain(false);
		}

		// re-layout to make sure sizes adjust to accommodate shown/hidden widgets correctly.
		scanFileControls.getMainComposite().layout(true);
		xesScanControls.getMainComposite().layout(true);

		layout();
		pack();
	}

	public void linkUI() {
		updateWidgetVisibilityForScanType();
		try {
			updateEnergyRanges();
		} catch (DeviceException e) {
			logger.error("Error trying to get latest XES spectrometer crystal values.\n XES Editor will not run its calculations correctly.", e);
		}
	}

	private void updateEnergyRanges() throws DeviceException {
		CrystalMaterial material = getAnalyserCrystalMaterial();
		double minXESEnergy= XesUtils.getFluoEnergy(XesUtils.MAX_THETA, material, getAnalyserCutValues());
		double maxXESEnergy= XesUtils.getFluoEnergy(XesUtils.MIN_THETA, material, getAnalyserCutValues());
		scanFileControls.getXesEnergy().setMinimum(minXESEnergy);
		scanFileControls.getXesEnergy().setMaximum(maxXESEnergy);

		double thetaE = updateXesTheta(scanFileControls.getXesEnergy());
		double thetaS = updateXesTheta(xesScanControls.getInitialEnergy());
		double theta = ((Integer) scanType.getValue() == XesScanParameters.FIXED_XES_SCAN_XAS || (Integer) scanType.getValue() == XesScanParameters.FIXED_XES_SCAN_XANES) ? thetaE : thetaS;
		double radius = getRadiusOfCurvatureValue();

		diagramComposite.updateValues(radius, theta);

		scanFileControls.getMonoEnergy().setValue(monoFixedEnergyControls.getMonoEnergy().getNumericValue());
	}

	private double updateXesTheta(ScaleBoxAndFixedExpression energyBox) throws DeviceException {
		double theta = getThetaForEnergy(energyBox.getNumericValue());
		energyBox.setFixedExpressionValue(theta);
		return theta;
	}

	private double getThetaForEnergy(double energy) throws DeviceException {
		CrystalMaterial material = getAnalyserCrystalMaterial();
		return XesUtils.getBragg(energy, material, getAnalyserCutValues());
	}


	public CrystalMaterial getAnalyserCrystalMaterial() throws DeviceException {
		return xesEnergyScannable.getMaterialType();
	}

	public int[] getAnalyserCutValues() throws DeviceException {
		return xesEnergyScannable.getCrystalCut();
	}

	private double getRadiusOfCurvatureValue() throws DeviceException {
		return xesEnergyScannable.getRadius();
	}

	public FieldComposite getScanFileName() {
		return scanFileControls.getScanFileName();
	}

	public void setEditingInput(final IEditorInput editing) {
		File editorFolder = EclipseUtils.getFile(editing).getParentFile();
		scanFileControls.setEditingFile(editing);
		scanFileControls.getScanFileName().setFolder(editorFolder);
	}

	public void setupUiFromBean() {
		if (guiUpdateInProgress) {
			return;
		}

		logger.info("Update UI from bean (beanUpdate = {}, guiUpdate = {})", beanUpdateInProgress, guiUpdateInProgress);

		guiUpdateInProgress = true;

		try {
			scanType.setValue(bean.getScanType());

			xesScanControls.getInitialEnergy().setValue(bean.getXesInitialEnergy());
			xesScanControls.getFinalEnergy().setValue(bean.getXesFinalEnergy());
			xesScanControls.getStepSize().setValue(bean.getXesStepSize());
			xesScanControls.getIntegrationTime().setValue(bean.getXesIntegrationTime());
			xesScanControls.getLoopChoice().setValue(bean.getLoopChoice());

			monoScanControls.getInitialEnergy().setValue(bean.getMonoInitialEnergy());
			monoScanControls.getFinalEnergy().setValue(bean.getMonoFinalEnergy());
			monoScanControls.getStepSize().setValue(bean.getMonoStepSize());

			// Apply file name to Mono or XES settings widget
			if (requiresXmlFile(bean.getScanType())) {
				if (bean.getScanType()==XesScanParameters.SCAN_XES_REGION_FIXED_MONO) {
					scanFileControls.getScanFileName().setValue(bean.getScanFileName());
				} else {
					scanFileControls.getMonoScanFileName().setValue(bean.getScanFileName());
				}
			}

			scanFileControls.getXesEnergy().setValue(bean.getXesEnergy());
			scanFileControls.getMonoEnergy().setValue(bean.getMonoEnergy());

			offsetsStoreName.setValue(bean.getOffsetsStoreName());

			monoFixedEnergyControls.getEdge().setValue(bean.getEdge());
			monoFixedEnergyControls.getElement().setValue(bean.getElement());
			monoFixedEnergyControls.getMonoEnergy().setValue(bean.getMonoEnergy());
		} finally {
			guiUpdateInProgress = false;
		}
	}

	public void updateBeanFromUi() {
		if (guiUpdateInProgress) {
			return;
		}

		logger.info("Update bean from UI (beanUpdate = {}, guiUpdate = {})", beanUpdateInProgress, guiUpdateInProgress);

		bean.setScanType((int) scanType.getValue());

		bean.setXesInitialEnergy(xesScanControls.getInitialEnergy().getNumericValue());
		bean.setXesFinalEnergy(xesScanControls.getFinalEnergy().getNumericValue());
		bean.setXesStepSize(xesScanControls.getStepSize().getNumericValue());
		bean.setXesIntegrationTime(xesScanControls.getIntegrationTime().getNumericValue());
		bean.setXesEnergy(scanFileControls.getXesEnergy().getNumericValue());
		Object loopChoice = xesScanControls.getLoopChoice().getValue();
		if (loopChoice != null) {
			bean.setLoopChoice(loopChoice.toString());
		}
		bean.setMonoInitialEnergy(monoScanControls.getInitialEnergy().getNumericValue());
		bean.setMonoFinalEnergy(monoScanControls.getFinalEnergy().getNumericValue());
		bean.setMonoStepSize(monoScanControls.getStepSize().getNumericValue());

		// Update the filename from correct FileBox widget, depending on scan type
		// ('scan file name' is currently shared between several scan types)
		if (requiresXmlFile(bean.getScanType())) {
			if (bean.getScanType()==XesScanParameters.SCAN_XES_REGION_FIXED_MONO) {
				bean.setScanFileName(scanFileControls.getScanFileName().getText());
			} else {
				bean.setScanFileName(scanFileControls.getMonoScanFileName().getText());
			}
		}

		bean.setMonoEnergy(scanFileControls.getXesEnergy().getNumericValue());
		bean.setMonoEnergy(scanFileControls.getMonoEnergy().getValue());
		bean.setOffsetsStoreName(offsetsStoreName.getText());

		bean.setEdge(monoFixedEnergyControls.getEdge().getValue().toString());
		bean.setElement(monoFixedEnergyControls.getElement().getValue().toString());
		bean.setMonoEnergy(monoFixedEnergyControls.getMonoEnergy().getNumericValue());
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
}