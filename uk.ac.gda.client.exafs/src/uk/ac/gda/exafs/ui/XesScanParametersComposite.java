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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.widgets.FieldComposite;
import org.eclipse.richbeans.widgets.scalebox.ScaleBoxAndFixedExpression;
import org.eclipse.richbeans.widgets.wrappers.ComboWrapper;
import org.eclipse.richbeans.widgets.wrappers.RadioWrapper;
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
import gda.util.CrystalParameters.CrystalMaterial;
import uk.ac.gda.beans.exafs.XesScanParameters;
import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.exafs.ui.xes.MonoFixedEnergyControls;
import uk.ac.gda.exafs.ui.xes.MonoScanRangeControls;
import uk.ac.gda.exafs.ui.xes.XesDiagramComposite;
import uk.ac.gda.exafs.ui.xes.XesScanRangeControls;
import uk.ac.gda.exafs.ui.xes.XesScanWithFileControls;

public final class XesScanParametersComposite extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(XesScanParametersComposite.class);

	private ComboWrapper scanType;

	private XesScanRangeControls xesScanControls;
	private MonoScanRangeControls monoScanControls;
	private XesScanWithFileControls scanFileControls;
	private XesDiagramComposite diagramComposite;
	private MonoFixedEnergyControls monoFixedEnergyControls;

	private TextWrapper offsetsStoreName;
	private ValueAdapter updateListener;

	private Composite scanTypeComposite;

	private IXesEnergyScannable xesEnergyScannable;

	private static final String XES_ERROR_MESSAGE = "Error trying to get latest XES spectrometer crysal values.\n" +
											"XES Editor will not run its calculations correctly.";

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

		updateListener = new ValueAdapter("XesScanParametersComposite Listener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				try {
					updateProperties();
					scanTypeComposite.layout();
				} catch (DeviceException e1) {
					logger.error(XES_ERROR_MESSAGE, e1);
				}
			}
		};

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

		scanType.addValueListener(e -> updateScanType());

		xesScanControls.getInitialEnergy().addValueListener(updateListener);
		scanFileControls.getXesEnergy().addValueListener(updateListener);
		// Update the other mono energy textbox to match the one in file controls if it changes.
		scanFileControls.addIObserver((source, arg) -> monoFixedEnergyControls.getMonoEnergy().setValue(scanFileControls.getMonoEnergy().getValue()));
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
		scanType.addValueListener(e -> {
			int xesScanType = (Integer) e.getValue();
			if (requiresXmlFile(xesScanType)) {
				try {
					scanFileControls.autoSetFileName(xesScanType);
				} catch (Exception ne) {
					logger.error("Cannot get bean file", ne);
				}
			}
		});

		scanType.addValueListener(updateListener);
	}

	private boolean requiresXmlFile(int xesScanType) {
		if (xesScanType == XesScanParameters.FIXED_XES_SCAN_XAS || xesScanType == XesScanParameters.FIXED_XES_SCAN_XANES) {
			return true;
		}
		if (xesScanType == XesScanParameters.SCAN_XES_REGION_FIXED_MONO) {
			return true;
		}
		return false;
	}

	private void addScanFileControls(Composite parent) {
		scanFileControls = new XesScanWithFileControls();
		scanFileControls.createControls(parent);
	}

	private void addScanStepControls(Composite parent) {
		xesScanControls = new XesScanRangeControls();
		try {
			xesScanControls.setCrystalMaterial(getAnalyserCrystalMaterial());
			xesScanControls.setCrystalCutValues(getAnalyserCutValues());
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

	protected void updateScanType() {
		int val = (Integer) scanType.getValue();
		boolean isXasXanes = val == XesScanParameters.FIXED_XES_SCAN_XAS || val == XesScanParameters.FIXED_XES_SCAN_XANES ||
				   val == XesScanParameters.SCAN_XES_REGION_FIXED_MONO;
		setVisible(scanFileControls.getMainComposite(), isXasXanes);
		if (isXasXanes) {
			scanFileControls.setScanTypeNum(val);
			// hide/show the Xas/Xanes fixed energy mono and spectromter energy controls
			boolean isXesXanes = val == XesScanParameters.SCAN_XES_REGION_FIXED_MONO;
			scanFileControls.setShowMonoEnergy(isXesXanes);
			scanFileControls.setShowXesEnergy(!isXesXanes);
		}

		setVisible(xesScanControls.getMainComposite(), val == XesScanParameters.SCAN_XES_FIXED_MONO || val == XesScanParameters.SCAN_XES_SCAN_MONO);
		setVisible(monoFixedEnergyControls.getMainComposite(), val == XesScanParameters.SCAN_XES_FIXED_MONO);
		setVisible(monoScanControls.getMainComposite(), val == XesScanParameters.SCAN_XES_SCAN_MONO);
		setVisible(xesScanControls.getLoopChoice(), val == XesScanParameters.SCAN_XES_SCAN_MONO);
		layout();
		pack();
	}

	public void linkUI() {
		updateScanType();
		try {
			updateProperties();
		} catch (DeviceException e) {
			logger.error("Error trying to get latest XES spectrometer crystal values.\n XES Editor will not run its calculations correctly.", e);
		}
	}

	private void updateProperties() throws DeviceException {
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
		xesScanControls.fireValueListeners();
		monoScanControls.fireValueListeners();
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

	private void setVisible(Composite comp, boolean visible) {
		GridData gridData = (GridData) comp.getLayoutData();
		gridData.exclude = !visible;
		comp.setVisible(visible);
	}

	public FieldComposite getScanType() {
		return scanType;
	}

	public FieldComposite getXesIntegrationTime() {
		return xesScanControls.getIntegrationTime();
	}

	public FieldComposite getScanFileName() {
		return scanFileControls.getScanFileName();
	}

	public FieldComposite getElement() {
		return monoFixedEnergyControls.getElement();
	}

	public FieldComposite getEdge() {
		return monoFixedEnergyControls.getEdge();
	}

	public FieldComposite getXesInitialEnergy() {
		return xesScanControls.getInitialEnergy();
	}

	public FieldComposite getXesFinalEnergy() {
		return xesScanControls.getFinalEnergy();
	}

	public FieldComposite getXesStepSize() {
		return xesScanControls.getStepSize();
	}

	public FieldComposite getMonoInitialEnergy() {
		return monoScanControls.getInitialEnergy();
	}

	public FieldComposite getMonoFinalEnergy() {
		return monoScanControls.getFinalEnergy();
	}

	public FieldComposite getMonoStepSize() {
		return monoScanControls.getStepSize();
	}

	public FieldComposite getXesEnergy() {
		return scanFileControls.getXesEnergy();
	}

	public FieldComposite getMonoEnergy() {
		return monoFixedEnergyControls.getMonoEnergy();
	}

	public RadioWrapper getLoopChoice() {
		return xesScanControls.getLoopChoice();
	}

	public TextWrapper getOffsetsStoreName() {
		return offsetsStoreName;
	}

	public void setEditingInput(final IEditorInput editing) {
		File editorFolder = EclipseUtils.getFile(editing).getParentFile();
		scanFileControls.setEditingFile(editing);
		scanFileControls.getScanFileName().setFolder(editorFolder);
	}
}