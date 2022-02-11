/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.gasrig.ui;

import java.text.DecimalFormat;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.factory.Finder;
import uk.ac.diamond.daq.gasrig.api.GasRigException;
import uk.ac.diamond.daq.gasrig.api.IGasRig;
import uk.ac.diamond.daq.gasrig.ui.viewmodels.CabinetViewModel;
import uk.ac.diamond.daq.gasrig.ui.viewmodels.GasFlowViewModel;
import uk.ac.diamond.daq.gasrig.ui.viewmodels.GasMixViewModel;
import uk.ac.diamond.daq.gasrig.ui.viewmodels.GasRigViewModel;
import uk.ac.diamond.daq.gasrig.ui.viewmodels.GasViewModel;
import uk.ac.gda.client.livecontrol.ScannablePositionerControl;

public class GasRigControls {

	private static final Logger logger = LoggerFactory.getLogger(GasRigControls.class);

	private static final String TWO_DECIMAL_PLACES = "#0.00";
	private static final String TWO_DECIMAL_PLACES_PERCENT = "#0.00' %'";
	private static final String TWO_DECIMAL_PLACES_MBAR = "#0.00 'mbar'";

	private static final String NORMALISED_FLOW_TOOLTIP = "= Mass Max Flow * √Molar Mass / Pressure";
	private static final String MASS_FLOW_TOOLTIP = "= Max Total Weighted Flow * (Pressure / Total Pressure) * (1 / √Molar Mass)";
	private static final String MAXIMUM_TOTAL_WEIGHTED_FLOW_TOOLTIP = "= MIN(Total Weighted Flow Limit, Lowest Normalised Flow Rate * Total Pressure";

	private static final int COLUMNS_PER_GAS = 4;
	private static final int COLUMNS_PER_LINE = 4;

	private GasRigViewModel gasRigViewModel;

	private int numberOfGasListColumns;
	private int numberOfGasMixes;

	Composite mainComposite;
	Composite gasList;

	private DataBindingContext bindingContext = new DataBindingContext();

	@PostConstruct
	public void postConstruct(Composite parent) {

		try {
			IGasRig gasRig = Finder.findOptionalSingleton(IGasRig.class).orElseThrow(() -> new GasRigException("No gas rig found in configuration"));
			gasRigViewModel = new GasRigViewModel(gasRig);
		} catch (GasRigException exception) {
			showError(exception.getMessage());
			return;
		}

		numberOfGasMixes = gasRigViewModel.getNumberOfMixes();
		numberOfGasListColumns = COLUMNS_PER_GAS + (COLUMNS_PER_LINE * numberOfGasMixes);

		mainComposite = parent;
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(mainComposite);

		gasList = new Composite(mainComposite, SWT.BORDER);
		GridDataFactory.fillDefaults()
			.grab(true, true)
			.align(SWT.FILL, SWT.FILL)
			.applyTo(gasList);

		GridLayoutFactory.fillDefaults().numColumns(numberOfGasListColumns).applyTo(gasList);

		addHeadingsToGasList();
		addGasesToGasList();
		addTotalRowToGasList();
		addDebugSection();
	}


	private void addHeadingsToGasList() {
		addLabel(gasList, "", span(COLUMNS_PER_GAS), false);
		IntStream.rangeClosed(1, numberOfGasMixes).forEach(line -> addLabel(gasList, "Line " + line, span(COLUMNS_PER_LINE), true, 14));

		addLabel(gasList, "Gases", span(1), true, 14);
		addLabel(gasList, "Current Mass Flow", span(1), false);
		addLabel(gasList, "Max Mass Flow", span(1), false);
		addLabel(gasList, "Molar Mass", span(1), false);

		for (int i = 0; i < numberOfGasMixes; i++ ) {
			addLabel(gasList, "Pressure (mbar)", span(1), false);
			addLabel(gasList, "Pressure %", span(1), false);
			addLabel(gasList, "Normalised Flow", span(1), false);
			addLabel(gasList, "Mass Flow", span(1), false);
		}
	}

	private void addGasesToGasList() {
		gasRigViewModel.getNonCabinetGases().forEach(this::addGasToGasList);
		gasRigViewModel.getCabinets().forEach(this::addCabinetToGasList);
	}

	private void addCabinetToGasList(CabinetViewModel cabinet) {
		if (!cabinet.getGases().isEmpty()) {
			addLabel(gasList, cabinet.getName(), span(numberOfGasListColumns), true, 14);
			cabinet.getGases().forEach(this::addGasToGasList);
		}
	}

	private void addGasToGasList(GasViewModel gas) {

		addLabel(gasList, gas.getName(), hint(75), false, 14);
		addMassFlowLiveControlToGasList(gas);
		addUneditableUnboundTextBox(gasList, hint(75), String.valueOf(gas.getMaxMassFlow()));
		addUneditableUnboundTextBox(gasList, hint(75), String.valueOf(gas.getMolarMass()));
		addMixControlsToGasList(gas);
	}

	private void addMassFlowLiveControlToGasList(GasViewModel gas) {
		ScannablePositionerControl scannablePositionerControl = new ScannablePositionerControl();
		scannablePositionerControl.setScannableName(gas.getMassFlowScannableName());
		scannablePositionerControl.setDisplayName("");
		scannablePositionerControl.setReadOnly(true);
		scannablePositionerControl.setShowIncrement(false);
		scannablePositionerControl.setShowStop(false);
		scannablePositionerControl.setHorizontalLayout(true);
		scannablePositionerControl.createControl(gasList);
	}

	private void addMixControlsToGasList(GasViewModel gas) {

		try {
			for (GasMixViewModel gasMix : gasRigViewModel.getGasMixes()) {
				GasFlowViewModel gasFlow;

				gasFlow = gasMix.getGasFlowViewModelByGasId(gas.getId());

				addTwoWayBoundSpinner(gasList, GasFlowViewModel.class, gasFlow, GasFlowViewModel.PRESSURE, 99.99, span(1));
				addOneWayBoundDecimalTextBox(gasList, GasFlowViewModel.class, gasFlow, GasFlowViewModel.PRESSURE_PERCENTAGE, TWO_DECIMAL_PLACES_PERCENT, hint(75), false);
				addOneWayBoundDecimalTextBox(gasList, GasFlowViewModel.class, gasFlow, GasFlowViewModel.NORMALISED_FLOW_RATE, TWO_DECIMAL_PLACES, hint(75), false, NORMALISED_FLOW_TOOLTIP);
				addOneWayBoundDecimalTextBox(gasList, GasFlowViewModel.class, gasFlow, GasFlowViewModel.MASS_FLOW, TWO_DECIMAL_PLACES, hint(75), false, MASS_FLOW_TOOLTIP);
			}
		} catch (GasRigException exception) {
			String error = "Unable to find gas flow for gas id " + gas.getId() + ". Gas rig is misconfigured. Please notify GDA support.";
			logger.error(error, exception);
			showError(error);
		}
	}

	private void addTotalRowToGasList() {
		addLabel(gasList, "Totals", span(COLUMNS_PER_GAS), true, 14);

		for (GasMixViewModel gasMix : gasRigViewModel.getGasMixes()) {
			addOneWayBoundDecimalTextBox(gasList, GasMixViewModel.class, gasMix, GasMixViewModel.TOTAL_PRESSURE, TWO_DECIMAL_PLACES_MBAR, spanAndHint(COLUMNS_PER_LINE - 1, 75), true);
			addOneWayBoundDecimalTextBox(gasList, GasMixViewModel.class, gasMix, GasMixViewModel.TOTAL_MASS_FLOW, TWO_DECIMAL_PLACES, spanAndHint(1, 75), true);
		}
	}

	private void addDebugSection() {

		Composite debugPanel = new Composite(mainComposite, SWT.BORDER);

		GridDataFactory.fillDefaults()
			.grab(true, true)
			.align(SWT.FILL, SWT.FILL)
			.applyTo(debugPanel);

		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(debugPanel);


		addLabel(debugPanel, "Debug", span(2), true, 14);

		for (GasMixViewModel gasMix : gasRigViewModel.getGasMixes()) {
			addLabel(debugPanel, "Line " + gasMix.getLineNumber(), span(2), true);

			addLabel(debugPanel, "Lowest Normalised Flow Rate", span(1), false);
			addOneWayBoundDecimalTextBox(debugPanel, GasMixViewModel.class, gasMix, GasMixViewModel.LOWEST_NORMALISED_FLOW_RATE, TWO_DECIMAL_PLACES, spanAndHint(1, 75), false);

			addLabel(debugPanel, "Lowest Normalised Flow Rate * Total Pressure", span(1), false);
			addOneWayBoundDecimalTextBox(debugPanel, GasMixViewModel.class, gasMix, GasMixViewModel.LOWEST_WEIGHTED_FLOW, TWO_DECIMAL_PLACES, spanAndHint(1, 75), false);

			addLabel(debugPanel, "Total Weighted Flow Limit", span(1), false);
			addTwoWayBoundSpinner(debugPanel, GasMixViewModel.class, gasMix, GasMixViewModel.TOTAL_WEIGHTED_FLOW_LIMIT, 1000, span(1));

			addLabel(debugPanel, "Maximum Total Weighted Flow", span(1), false);
			addOneWayBoundDecimalTextBox(debugPanel, GasMixViewModel.class, gasMix, GasMixViewModel.MAXIMUM_TOTAL_WEIGHTED_FLOW, TWO_DECIMAL_PLACES, spanAndHint(1, 75), false, MAXIMUM_TOTAL_WEIGHTED_FLOW_TOOLTIP);
		}
	}

	private Label addLabel(Composite parent, String labelText, GridDataFactory layout, boolean bold, int fontSize) {
		Label label = addLabel(parent, labelText, layout, bold);
		setLabelFontSize(label, fontSize);

		return label;
	}

	private Label addLabel(Composite parent, String labelText, GridDataFactory layout, boolean bold) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(labelText);

		if (bold) {
			setControlBold(label);
		}

		layout.applyTo(label);

		return label;
	}

	private GridDataFactory spanAndHint(int span, int hint) {
		return span(span).hint(hint, SWT.DEFAULT);
	}

	private GridDataFactory span(int span) {
		return GridDataFactory.fillDefaults()
				.span(span, 1)
				.grab(false, false)
				.align(SWT.LEFT, SWT.CENTER);
	}

	private GridDataFactory hint(int hint) {
		return GridDataFactory.fillDefaults()
				.hint(hint, SWT.DEFAULT)
				.grab(false, false)
				.align(SWT.LEFT, SWT.CENTER);
	}

	private void setControlBold(Control control) {
		FontDescriptor fontDescriptor = FontDescriptor.createFrom(control.getFont());
		fontDescriptor = fontDescriptor.setStyle(SWT.BOLD);
		control.setFont(fontDescriptor.createFont(control.getDisplay()));
	}

	private void setLabelFontSize(Label label, int fontSize) {
		FontDescriptor fontDescriptor = FontDescriptor.createFrom(label.getFont());
		fontDescriptor = fontDescriptor.setHeight(fontSize);
		label.setFont(fontDescriptor.createFont(label.getDisplay()));
	}

	private void addUneditableUnboundTextBox(Composite parent, GridDataFactory layout, String text) {
		Text textBox = new Text(parent, SWT.BORDER);

		layout.applyTo(textBox);

		textBox.setEditable(false);
		textBox.setText(text);
	}

	private <T> Text addOneWayBoundDecimalTextBox(Composite parent, Class<T> modelClass, T model, String propertyname, String decimalFormatString, GridDataFactory layout, boolean bold) {

		Text textBox = new Text(parent, SWT.BORDER);

		layout.applyTo(textBox);
		textBox.setEditable(false);

		if (bold) {
			setControlBold(textBox);
		}

		var modelObservable = BeanProperties.value(modelClass, propertyname, Double.class).observe(model);
		var uiObservable = WidgetProperties.text(SWT.Modify).observe(textBox);

		var uiToModelStrategy = new UpdateValueStrategy<String, Double>(false, UpdateValueStrategy.POLICY_NEVER);

		var percentFormatter = new DecimalFormat(decimalFormatString);
		var modelToUiStrategy = new UpdateValueStrategy<Double, String>(false, UpdateValueStrategy.POLICY_UPDATE)
				.setConverter(IConverter.create(Double.class, String.class, percentFormatter::format));

		bindingContext.bindValue(uiObservable, modelObservable, uiToModelStrategy, modelToUiStrategy);

		return textBox;
	}

	private <T> void addOneWayBoundDecimalTextBox(Composite parent, Class<T> modelClass, T model, String propertyname, String decimalFormatString, GridDataFactory layout, boolean bold, String toolTip) {

		var textBox = addOneWayBoundDecimalTextBox(parent, modelClass, model, propertyname, decimalFormatString, layout, bold);
		textBox.setToolTipText(toolTip);
	}

	private <T> void addTwoWayBoundSpinner(Composite parent, Class<T> modelClass, T model, String propertyname, double maxValue, GridDataFactory layout) {

		var spinner = new Spinner(parent, SWT.BORDER);
		spinner.setMinimum(0);
		spinner.setDigits(2);
		spinner.setMaximum((int) (maxValue * 100));
		spinner.setIncrement(1);
		spinner.setPageIncrement(100);
		spinner.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		layout.applyTo(spinner);

		var modelObservable = BeanProperties.value(modelClass, propertyname, Double.class).observe(model);
		var uiObservable = WidgetProperties.spinnerSelection().observe(spinner);

		var uiToModelStrategy = new UpdateValueStrategy<Integer, Double>(false, UpdateValueStrategy.POLICY_UPDATE)
				.setConverter(IConverter.create(Integer.class, Double.class, x -> (double)x / 100));

		var modelToUiStrategy = new UpdateValueStrategy<Double, Integer>(false, UpdateValueStrategy.POLICY_UPDATE)
				.setConverter(IConverter.create(Double.class, Integer.class, x -> (int)Math.round(x * 100)));

		bindingContext.bindValue(uiObservable, modelObservable, uiToModelStrategy, modelToUiStrategy);
	}

	private void showError(String message) {
		MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR | SWT.OK);
		messageBox.setText("Error");
		messageBox.setMessage(message);
		messageBox.open();
	}
}
