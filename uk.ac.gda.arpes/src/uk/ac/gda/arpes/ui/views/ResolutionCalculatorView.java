/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.gda.arpes.ui.views;

import static org.eclipse.jface.layout.GridLayoutFactory.fillDefaults;
import static org.eclipse.jface.widgets.WidgetFactory.button;
import static org.eclipse.jface.widgets.WidgetFactory.label;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.Scannable;
import gda.factory.Finder;
import uk.ac.diamond.daq.pes.api.EntranceSlitInformationProvider;
import uk.ac.diamond.daq.pes.api.IElectronAnalyser;
import uk.ac.gda.arpes.calculator.IResolutionCalculatorConfiguration;

public class ResolutionCalculatorView extends ViewPart{
	private static final Logger logger = LoggerFactory.getLogger(ResolutionCalculatorView.class);

	private IResolutionCalculatorConfiguration viewConfiguration;
	private EntranceSlitInformationProvider analyserEntranceSlitProvider;
	private IElectronAnalyser analyser;
	private EnumPositioner grating;
	private Scannable pgmEnergy;
	private Scannable exitSlit;

	private Spinner spinnerEnergy;
	private Spinner spinnerExitSlit;
	private CCombo comboGrating;
	private CCombo comboPass;
	private CCombo comboAnalyserSlit;
	private Text labelWorkFunction;
	private Text beamlineResolution;
	private Text analyserResolution;
	private Text totalResolution;

	private List<Double> passEnergyList;
	private List<Double> gratingPositionsList;
	private List<Integer> analyserSlitList;
	private Map<Integer, double[]> beamlineResolutionParameters = new HashMap<>();
	private Map<Integer, double[]> workFunctionParameters = new HashMap<>();


	@Override
	public void createPartControl(Composite parent) {
		try {
			viewConfiguration 	= Finder.findSingleton(IResolutionCalculatorConfiguration.class);
			pgmEnergy 			= Finder.find(viewConfiguration.getPhotonEnergyName());
			analyser 			= Finder.find(viewConfiguration.getAnalyserName());
			grating 			= Finder.find(viewConfiguration.getGratingName());
			exitSlit 			= Finder.find(viewConfiguration.getExitSlitName());
			analyserEntranceSlitProvider = Finder.find(viewConfiguration.getAnalyserEntranceSlitProviderName());
		} catch (IllegalArgumentException exception) {
			logger.error("Unable to find scannable for view:", exception);
			return;
		}

		try {
			gratingPositionsList = Arrays.stream(grating.getPositions()).
					map(e -> Double.parseDouble(e.replaceAll("\\D", ""))).toList();
			passEnergyList = analyser.getPassEnergies().
					stream().map(e -> Double.parseDouble(e.replaceAll("\\D", ""))).toList();
			analyserSlitList = analyserEntranceSlitProvider.getSlitsRawValueList();
		} catch (DeviceException e) {
			logger.error("Failed to get grating positions list", e);
		}

		String blParametersFilePath = viewConfiguration.getBlResolutionParamsFilePath();
		beamlineResolutionParameters = viewConfiguration.getParametersFromFile(blParametersFilePath);
		String wfParametersPath = viewConfiguration.getWorkFunctionFilePath();
		workFunctionParameters = viewConfiguration.getParametersFromFile(wfParametersPath);

		createInitialLayout(parent);
	}

	private void createInitialLayout(Composite parent){
		fillDefaults().equalWidth(true).numColumns(4).applyTo(parent);
		createBeamlineGroupLayout(parent);
		createAnalyserGroupLayout(parent);
		createTotalGroupLayout(parent);
		reset();
	}

	private void createBeamlineGroupLayout(Composite parent) {
		Group groupBeamline = new Group(parent, SWT.NONE);
		groupBeamline.setText("Beamline parameters");
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1);
		groupBeamline.setLayoutData(gridData);
		groupBeamline.setLayout(new GridLayout(4, false));

		label(SWT.NULL).text("Photon Energy (eV): ").create(groupBeamline);
		spinnerEnergy = new Spinner(groupBeamline, SWT.SINGLE | SWT.BORDER);
		spinnerEnergy.setValues(500, 100, 5000, 1, 1, 100);
		GridData gridDataSE = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		spinnerEnergy.setLayoutData(gridDataSE);
		spinnerEnergy.addSelectionListener(SelectionListener.widgetSelectedAdapter(e-> update()));

		label(SWT.NULL).text("Grating l/mm: ").create(groupBeamline);
		comboGrating = new CCombo(groupBeamline,SWT.SINGLE | SWT.BORDER);
		comboGrating.setItems(gratingPositionsList.stream().map(String::valueOf).toArray(String[]::new));
		GridData gridDataCG = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		comboGrating.setLayoutData(gridDataCG);
		comboGrating.addSelectionListener(SelectionListener.widgetSelectedAdapter(e-> update()));

		label(SWT.NULL).text("Work Function (eV): ").create(groupBeamline);
		labelWorkFunction = new Text(groupBeamline, SWT.SINGLE | SWT.BORDER);
		labelWorkFunction.setEditable(false);
		labelWorkFunction.addSelectionListener(SelectionListener.widgetSelectedAdapter(e-> update()));

		label(SWT.NULL).text("Exit Slit ("+Character.toString('\u03BC')+"m): ").create(groupBeamline);
		spinnerExitSlit = new Spinner(groupBeamline,SWT.SINGLE | SWT.BORDER);
		spinnerExitSlit.setValues(300, 0, 10000, 1, 10, 100);
		GridData gridDataSES = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		spinnerExitSlit.setLayoutData(gridDataSES);
		spinnerExitSlit.addSelectionListener(SelectionListener.widgetSelectedAdapter(e-> update()));

		label(SWT.NULL).text("Beamline resolution (meV): ").create(groupBeamline);
		GridData gridDataBR = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		beamlineResolution = new Text(groupBeamline, SWT.SINGLE | SWT.BORDER);
		beamlineResolution.setEditable(false);
		beamlineResolution.setTextLimit(5);
		beamlineResolution.setLayoutData(gridDataBR);

		label(SWT.LEAD).layoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL)).create(groupBeamline);
	}

	private void createAnalyserGroupLayout(Composite parent) {
		Group groupAnalyser = new Group(parent, SWT.NONE);
		groupAnalyser.setText("Analyser parameters");
		GridData gridDataAn = new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1);
		groupAnalyser.setLayoutData(gridDataAn);
		groupAnalyser.setLayout(new GridLayout(4, false));

		label(SWT.NONE).text("Pass Energy (eV): ").create(groupAnalyser);
		comboPass = new CCombo(groupAnalyser,SWT.SINGLE | SWT.BORDER);
		comboPass.setItems(passEnergyList.stream().map(String::valueOf).toArray(String[]::new));
		GridData gridDataCP = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		comboPass.setLayoutData(gridDataCP);
		comboPass.addSelectionListener(SelectionListener.widgetSelectedAdapter(e-> update()));

		label(SWT.NONE).text("Analyser Slit: ").create(groupAnalyser);
		comboAnalyserSlit = new CCombo(groupAnalyser,SWT.SINGLE | SWT.BORDER);
		comboAnalyserSlit.setItems(analyserSlitList.stream().map(String::valueOf).toArray(String[]::new));
		GridData gridDataCA = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		comboAnalyserSlit.setLayoutData(gridDataCA);
		comboAnalyserSlit.addSelectionListener(SelectionListener.widgetSelectedAdapter(e-> update()));

		label(SWT.NULL).text("Analyser resolution (meV): ").create(groupAnalyser);
		GridData gridDataAR = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridDataAR.widthHint = 50;
		analyserResolution = new Text(groupAnalyser, SWT.SINGLE | SWT.BORDER);
		analyserResolution.setEditable(false);
		analyserResolution.setTextLimit(5);
		analyserResolution.setLayoutData(gridDataAR);

		label(SWT.LEAD).layoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL)).create(groupAnalyser);
	}

	private void createTotalGroupLayout(Composite parent) {

		Group groupTotal = new Group(parent, SWT.NONE);
		GridData gridDataAn = new GridData(SWT.FILL, SWT.FILL, false, false, 4, 1);
		groupTotal.setLayoutData(gridDataAn);
		groupTotal.setLayout(new GridLayout(4, false));

		// Bold font
		FontData[] fontData = analyserResolution.getFont().getFontData();
		for(int i = 0; i < fontData.length; ++i) {
			fontData[i].setHeight(14);
			fontData[i].setStyle(SWT.BOLD);
		}
		final Font newFont = new Font(Display.getCurrent(), fontData);

		Label labelTotal = new Label(groupTotal, SWT.NULL);
		labelTotal.setText("Combined resolution (meV): ");
		labelTotal.setFont(newFont);
		labelTotal.addDisposeListener(e->newFont.dispose());

		GridData gridDataTR = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridDataTR.widthHint = 50;
		totalResolution = new Text(groupTotal, SWT.SINGLE | SWT.BORDER);
		totalResolution.setEditable(false);
		totalResolution.setTextLimit(5);
		totalResolution.setLayoutData(gridDataTR);
		totalResolution.setFont(newFont);
		totalResolution.addDisposeListener(e->newFont.dispose());

		button(SWT.PUSH | SWT.BORDER)
			.layoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING))
			.text("Reset").onSelect(e -> reset()).create(groupTotal);
	}

	private void update() {
		double exitSlitVal = Double.parseDouble(spinnerExitSlit.getText());
		double gratingVal = Double.parseDouble(comboGrating.getText());
		double photonEnergy = Double.parseDouble(spinnerEnergy.getText());
		double passEnergy = Double.parseDouble(comboPass.getText());
		int analyserSlit = Integer.parseInt(comboAnalyserSlit.getText());

		labelWorkFunction.setText(String.format("%.3f",viewConfiguration.getWorkFunction(gratingVal, photonEnergy, workFunctionParameters)));

		Double brResolvingPower = viewConfiguration.calculateResolvingPower(exitSlitVal, gratingVal, beamlineResolutionParameters);
		Double blResolution = viewConfiguration.calculateBeamlineResolution(photonEnergy, brResolvingPower);
		beamlineResolution.setText(String.format("%.1f",blResolution));

		Double anResolution = viewConfiguration.calculateAnalyserResolution(passEnergy,
				analyserEntranceSlitProvider.getSizeByRawValue(analyserSlit));
		analyserResolution.setText(String.format("%.1f",anResolution));

		Double totResoultion = viewConfiguration.calculateTotalResolution(blResolution, anResolution);
		totalResolution.setText(String.format("%.1f",totResoultion));

		if (Math.abs(anResolution-0.0)< viewConfiguration.getDoublesPrecision()) return;
		if (Math.abs(blResolution-0.0)<viewConfiguration.getDoublesPrecision()) return;

		// Change colors proportional to resolutions variation
		int colorDif;
		if (anResolution.compareTo(blResolution)>=0){
			colorDif = (int) Math.round(Math.abs(blResolution/anResolution)*255);
			beamlineResolution.setBackground(new Color(colorDif,255,colorDif,255));
			analyserResolution.setBackground(new Color(255,colorDif,colorDif,255));
		} else {
			colorDif = (int) Math.round(Math.abs(anResolution/blResolution)*255);
			analyserResolution.setBackground(new Color(colorDif,255,colorDif,255));
			beamlineResolution.setBackground(new Color(255,colorDif,colorDif,255));
		}
	}

	private void reset() {
		try {
			spinnerEnergy.setSelection((int) (Math.ceil((double) pgmEnergy.getPosition())*10));
			comboGrating.setText(((String) grating.getPosition()).replaceAll("\\D", ""));
			spinnerExitSlit.setSelection((int) (Math.ceil((double) exitSlit.getPosition()*10000)));
			comboPass.setText(analyser.getPassEnergy().toString());
			comboAnalyserSlit.setText(String.valueOf(analyserEntranceSlitProvider.getRawValue()));
		} catch (DeviceException e) {
			logger.error("Failed to get values from PVs", e);
		} catch (Exception e) {
			logger.error("Failed to get analyser parameter", e);
		}
		update();
	}

	@Override
	public void setFocus() {
		//Noop
	}

}
