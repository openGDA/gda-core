/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.scanning.api.points.models.ArrayModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.MultiStepModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.ScannableMotion;
import gda.factory.Finder;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.impl.MappingScanDefinition;
import uk.ac.diamond.daq.mapping.impl.ScanPathModelWrapper;

/**
 * A section for configuring the outer scannables of a scan, e.g. temperature.
 * <p>
 * When only a subset of the configured scannables could be used as the outer axis in a scan,
 * they should be given as the {@code defaultOuterScannables} in the {@link MappingScanDefinition}.
 * <br>
 * If this list is not configured, then the user will be able to display and
 * configure any available scannable that implements {@link ScannableMotion} or a derived interface.
 */
class OuterScannablesSection extends AbstractMappingSection {

	private static final Logger logger = LoggerFactory.getLogger(OuterScannablesSection.class);

	/**
	 * Overall composite for section
	 */
	private Composite sectionComposite;

	/**
	 * Composite to hold list of scannables<br>
	 * The user can choose which scannables to show in this list: see comment on {@link #scannablesToShow}.
	 */
	private Composite scannablesComposite;

	/**
	 * The outer scannables to show in the view.<br>
	 * Initially empty, the user can add and remove scannables to/from the list.<br>
	 * The choice will be saved when the client is closed and restored when opened, unless the client is reset.
	 */
	private List<IScanModelWrapper<IScanPathModel>> scannablesToShow;

	/**
	 * Names of the scannables that the user can choose
	 * <p>
	 * This list will be either:<br>
	 * <li>the {@code defaultOuterScannables} configured</li>
	 * or, if no default scannables are configured:
	 * <li>all {@link ScannableMotion} configured for the beamline</li>
	 */
	private List<String> availableScannables;

	/**
	 * For each scannable displayed in this section, the binding between the scan path in the model and the text box in
	 * which it is displayed.
	 */
	private Map<String, Binding> axisBindings;

	/**
	 * For each scannable displayed in this section, the binding between the "includeInScan" flag in the model and the
	 * corresponding check box
	 */
	private Map<String, Binding> checkBoxBindings;


	@Override
	protected void initialize(MappingExperimentView mappingView) {
		super.initialize(mappingView);
		List<String> defaultScannables = getMappingBean().getScanDefinition().getDefaultOuterScannables();
		if (defaultScannables == null || defaultScannables.isEmpty()) {
			try {
				availableScannables = new ArrayList<>(Finder.getInstance().getFindablesOfType(ScannableMotion.class).keySet());
			} catch (Exception e) {
				logger.error("Exception getting list of scannables", e);
				availableScannables = Collections.emptyList();
			}
		} else {
			availableScannables = new ArrayList<>(defaultScannables);
		}
	}

	@Override
	public void createControls(Composite parent) {
		sectionComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sectionComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(sectionComposite);

		final Label otherScanAxesLabel = new Label(sectionComposite, SWT.NONE);
		otherScanAxesLabel.setText("Other Scan Axes");
		GridDataFactory.fillDefaults().applyTo(otherScanAxesLabel);

		// button to add a new scannable
		final Button btnAdd = new Button(sectionComposite, SWT.PUSH);
		btnAdd.setImage(MappingExperimentUtils.getImage("icons/plus.png"));
		btnAdd.setToolTipText("Add scannables");
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(btnAdd);
		btnAdd.addListener(SWT.Selection, event -> addScannables());

		createScannableControls();
	}

	/**
	 * Create a control (checkbox + scan path specification) for each scannable chosen to be displayed
	 */
	private void createScannableControls() {
		removeOldBindings();

		scannablesToShow = new ArrayList<>(getMappingBean().getScanDefinition().getOuterScannables());

		// Ensure scannables are shown in alphabetical order (case insensitive)
		scannablesToShow.sort(comparing(IScanModelWrapper<IScanPathModel>::getName, CASE_INSENSITIVE_ORDER));

		if (scannablesComposite != null) {
			scannablesComposite.dispose();
		}
		dataBindingContext = new DataBindingContext();
		axisBindings = new HashMap<>();
		checkBoxBindings = new HashMap<>();

		scannablesComposite = new Composite(sectionComposite, SWT.NONE);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(scannablesComposite);
		GridLayoutFactory.swtDefaults().numColumns(4).margins(0, 0).applyTo(scannablesComposite);

		// Create a control for each scannable to be shown
		for (IScanModelWrapper<IScanPathModel> scannableAxisParameters : scannablesToShow) {
			final String scannableName = scannableAxisParameters.getName();

			// Create checkbox and bind to "includeInScan" in the model
			final Button checkBox = new Button(scannablesComposite, SWT.CHECK);
			checkBox.setText(scannableName);
			final IObservableValue<?> checkBoxValue = WidgetProperties.selection().observe(checkBox);
			@SuppressWarnings("unchecked")
			final IObservableValue<?> activeValue = PojoProperties.value("includeInScan").observe(scannableAxisParameters);
			final Binding checkBoxBinding = dataBindingContext.bindValue(checkBoxValue, activeValue);
			checkBoxBindings.put(scannableName, checkBoxBinding);

			// Create text box to display/edit scan path definition
			final Text axisText = new Text(scannablesComposite, SWT.BORDER);
			axisText.setToolTipText("A range <start stop step>\n"
					+ "or a list of points <pos1,pos2,pos3,pos4...>\n"
					+ "or a list of ranges <start1 stop1 step1; start2 stop2 step2>");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(axisText);
			final IObservableValue<?> axisTextValue = WidgetProperties.text(SWT.Modify).observe(axisText);

			final Button multiStepButton = new Button(scannablesComposite, 0);
			multiStepButton.setImage(MappingExperimentUtils.getImage("icons/pencil.png"));
			multiStepButton.setToolTipText("Edit a multi-step scan");

			// Button to display a MultiStepEditorDialog as an alternative way of editing the scan path definition
			final MultiStepEditorDialog dialog = new MultiStepEditorDialog(getShell(), scannableName);
			multiStepButton.addListener(SWT.Selection, event -> editModelThroughDialog(dialog, scannableName, axisText));

			// Bind scan path text box to the model
			bindScanPathModelToTextField(scannableAxisParameters, axisTextValue, checkBoxBinding);

			// Button to delete scannable
			final Button deleteScannableButton = new Button(scannablesComposite, SWT.NONE);
			deleteScannableButton.setImage(MappingExperimentUtils.getImage("icons/cross.png"));
			deleteScannableButton.setToolTipText("Delete scannable");
			deleteScannableButton.addListener(SWT.Selection, event -> deleteScannable(scannableAxisParameters));
		}

		// Keep the mapping bean in sync with the GUI
		getMappingBean().getScanDefinition().setOuterScannables(scannablesToShow);
	}

	private void deleteScannable(IScanModelWrapper<IScanPathModel> scannable) {
		if (MessageDialog.openQuestion(getShell(), "Confirm deletion", String.format("Do you want to delete %s?", scannable.getName()))) {
			scannablesToShow.remove(scannable);
			updateLayout();
		}
	}

	private void addScannables() {
		// Get the scannables to show in the dialog: exclude the scannables that are already shown.
		final List<String> scannablesShown = extractScannableNames(scannablesToShow);
		final List<String> scannablesToChoose = availableScannables.stream()
				.filter(scannable -> !scannablesShown.contains(scannable))
				.collect(Collectors.toList());

		if (scannablesToChoose.isEmpty()) {
			MessageDialog.openError(getShell(), "No scannables to add",	"There are no more scannables available to add");
			return;
		}

		final String titleText = "Select scannable(s) to add";
		final String headerText = "Select one or more scannables";
		final MultiSelectDialog dialog = new MultiSelectDialog(getShell(), titleText, headerText, scannablesToChoose);
		if (dialog.open() == Window.OK) {
			final List<String> scannablesSelected = dialog.getSelected();
			if (!scannablesSelected.isEmpty()) {
				for (String scannable : scannablesSelected) {
					scannablesToShow.add(new ScanPathModelWrapper(scannable, null, false));
				}
				updateLayout();
			}
		}
	}

	private static List<String> extractScannableNames(Collection<IScanModelWrapper<IScanPathModel>> scannables) {
		return scannables.stream()
				.map(IScanModelWrapper<IScanPathModel>::getName)
				.collect(Collectors.toList());
	}

	private void updateLayout() {
		createScannableControls();
		relayoutMappingView();
		updateStatusLabel();
	}

	private void editModelThroughDialog(MultiStepEditorDialog dialog, String scannableName, Text axisText) {
		MultiStepModel multiStepModel = new MultiStepModel();

		if (!axisText.getText().isEmpty()) {
			final Object oldModel = (new StringToScanPathConverter(scannableName)).convert(axisText.getText());

			if (oldModel instanceof MultiStepModel) {
				multiStepModel = (MultiStepModel) oldModel;
			} else if (oldModel instanceof StepModel) {
				multiStepModel.getStepModels().add((StepModel) oldModel);
			} else if (oldModel instanceof ArrayModel) {
				final double[] positions = ((ArrayModel) oldModel).getPositions();
				for (int i = 0; i < positions.length - 1; i++) {
					final StepModel stepModel = new StepModel(scannableName, positions[i], positions[i + 1], positions[i + 1] - positions[i]);
					multiStepModel.getStepModels().add(stepModel);
				}
			}
		}

		multiStepModel.setName(scannableName);
		dialog.setModel(multiStepModel);
		if (dialog.open() == Window.OK) {
			try {
				axisText.setText((String) new ScanPathToStringConverter().convert(dialog.getEditor().getModel()));
				updateStatusLabel();
			} catch (Exception e) {
				logger.error("Cannot retrieve MultiStepModel from dialog", e);
			}
		}
	}

	private void bindScanPathModelToTextField(IScanModelWrapper<IScanPathModel> scannableAxisParameters, IObservableValue<?> axisTextValue, Binding checkBoxBinding) {
		final String scannableName = scannableAxisParameters.getName();
		@SuppressWarnings("unchecked")
		final IObservableValue<?> axisValue = PojoProperties.value("model").observe(scannableAxisParameters);

		// create an update strategy from text to model with a converter and a validator
		final UpdateValueStrategy axisTextToModelStrategy = new UpdateValueStrategy();
		axisTextToModelStrategy.setConverter(new StringToScanPathConverter(scannableName));
		axisTextToModelStrategy.setBeforeSetValidator(value -> {
			// the value created by the converter will be an IScanPathModel if the text value is valid, or null if not
			if (value instanceof IScanPathModel) {
				return ValidationStatus.ok();
			}
			final boolean isEmpty = ((String) axisTextValue.getValue()).isEmpty();
			final String message = isEmpty ? "Enter a range or list of values for this scannable" : "Text is incorrectly formatted";
			if (scannableAxisParameters.isIncludeInScan()) {
				return ValidationStatus.error(message);
			} else {
				// empty value is ok if this scannable is not included in the scan
				return isEmpty ? ValidationStatus.ok() : ValidationStatus.warning(message);
			}
		});

		// create an update strategy from model back to text
		final UpdateValueStrategy modelToAxisTextStrategy = new UpdateValueStrategy();
		modelToAxisTextStrategy.setConverter(new ScanPathToStringConverter());

		// create the binding from the values and the two update strategies
		final Binding axisBinding = dataBindingContext.bindValue(axisTextValue, axisValue,
				axisTextToModelStrategy, modelToAxisTextStrategy);
		ControlDecorationSupport.create(axisBinding, SWT.LEFT | SWT.TOP);
		axisBindings.put(scannableName, axisBinding);

		// Update Mapping status label after model is modified from text widget
		axisBinding.getModel().addChangeListener(evt -> updateStatusLabel());

		// when the include in scan checkbox is changed we need to revalidate the model
		// as this determines the severity of the validation status.
		checkBoxBinding.getModel().addChangeListener(evt -> {
			axisBinding.validateTargetToModel();
			updateStatusLabel();
		});
	}

	@Override
	public void updateControls() {
		// update the bindings between the model and the GUI widgets, as we may have new models
		for (IScanModelWrapper<IScanPathModel> scannableAxisParameters : scannablesToShow) {
			final String scannableName = scannableAxisParameters.getName();

			// remove the old binding between the checkbox and the old model and create a new one
			final Binding oldCheckBoxBinding = checkBoxBindings.get(scannableName);
			final IObservableValue<?> checkBoxValue = (IObservableValue<?>) oldCheckBoxBinding.getTarget();
			dataBindingContext.removeBinding(oldCheckBoxBinding);
			oldCheckBoxBinding.dispose();

			@SuppressWarnings("unchecked")
			final IObservableValue<?> activeValue = PojoProperties.value("includeInScan").observe(scannableAxisParameters);
			final Binding newCheckBoxBinding = dataBindingContext.bindValue(checkBoxValue, activeValue);
			checkBoxBindings.put(scannableName, newCheckBoxBinding);

			// remove the binding between the text field and old model
			final Binding oldTextFieldBinding = axisBindings.get(scannableName);
			final IObservableValue<?> axisTextValue = (IObservableValue<?>) oldTextFieldBinding.getTarget();
			dataBindingContext.removeBinding(oldTextFieldBinding);
			oldTextFieldBinding.dispose();

			bindScanPathModelToTextField(scannableAxisParameters, axisTextValue, newCheckBoxBinding);
		}

		dataBindingContext.updateTargets();
	}

	/**
	 * Class to convert a path model to a string
	 */
	private static class ScanPathToStringConverter extends Converter {

		public ScanPathToStringConverter() {
			super(IScanPathModel.class, String.class);
		}

		@Override
		public Object convert(Object fromObject) {
			if (fromObject == null) {
				return ""; // this is the case when the outer scannable is not specified
			} else if (fromObject instanceof StepModel) {
				return convertStepModel((StepModel) fromObject);
			} else if (fromObject instanceof ArrayModel) {
				return convertArrayModel((ArrayModel) fromObject);
			} else if (fromObject instanceof MultiStepModel) {
				return convertMultiStepModel((MultiStepModel) fromObject);
			} else {
				// We only expect path model types that can be created from this GUI
				throw new IllegalArgumentException("Unknown model type: " + fromObject.getClass());
			}
		}

		private String convertStepModel(StepModel stepModel) {
			final StringBuilder stringBuilder = new StringBuilder();
			appendStepModel(stepModel, stringBuilder);

			return stringBuilder.toString();
		}

		private void appendStepModel(StepModel stepModel, StringBuilder stringBuilder) {
			stringBuilder.append(doubleToString(stepModel.getStart()));
			stringBuilder.append(' ');
			stringBuilder.append(doubleToString(stepModel.getStop()));
			stringBuilder.append(' ');
			stringBuilder.append(doubleToString(stepModel.getStep()));
		}

		private String convertMultiStepModel(MultiStepModel multiStepModel) {
			final Iterator<StepModel> iter = multiStepModel.getStepModels().iterator();

			final StringBuilder stringBuilder = new StringBuilder();
			while (iter.hasNext()) {
				appendStepModel(iter.next(), stringBuilder);
				if (iter.hasNext()) {
					stringBuilder.append(";");
				}
			}

			return stringBuilder.toString();
		}

		private String convertArrayModel(ArrayModel arrayModel) {
			final double[] positions = arrayModel.getPositions();
			final StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < positions.length; i++) {
				stringBuilder.append(doubleToString(positions[i]));
				if (i < positions.length - 1) {
					stringBuilder.append(",");
				}
			}
			return stringBuilder.toString();
		}

		private String doubleToString(double doubleVal) {
			final String stringVal = Double.toString(doubleVal);
			if (stringVal.endsWith(".0")) {
				return stringVal.substring(0, stringVal.length() - 2);
			}
			return stringVal;
		}
	}

	/**
	 * Converter for converting a string to a path model.<br>
	 * The string specifies the path of the outer scannable. It can be:
	 * <ul>
	 * <li>A comma separated list of values, i.e. pos1,pos2,pos3,pos4,...</li>
	 * <li>A single step range with start stop and step values separated by spaces, i.e. start stop step</li>
	 * <li>Multiple step ranges with each step range separated by a semi-colon, i.e. start1 stop1 step1; start2 stop2
	 * step2</li>
	 * </ul>
	 * <p>
	 * If the string contains a comma, it is interpreted a sequence of points, otherwise as one or more ranges.
	 */
	private static final class StringToScanPathConverter extends Converter {
		private final String scannableName;

		private StringToScanPathConverter(String scannableName) {
			super(String.class, IScanPathModel.class);
			this.scannableName = scannableName;
		}

		@Override
		public Object convert(Object fromObject) {
			final String text = (String) fromObject;
			if (text.isEmpty()) {
				return null;
			}
			try {
				if (text.contains(",")) {
					return convertStringToArrayModel(text);
				} else if (text.contains(";")) {
					return convertStringToMultiStepModel(text);
				} else {
					return convertStringToStepModel(text);
				}
			} catch (Exception e) {
				logger.error("Could not convert string to model", e);
				return null;
			}
		}

		private StepModel convertStringToStepModel(String text) {
			final String[] startStopStep = text.split(" ");
			if (startStopStep.length == 3) {
				StepModel stepModel = new StepModel();
				stepModel.setName(scannableName);
				stepModel.setStart(Double.parseDouble(startStopStep[0]));
				stepModel.setStop(Double.parseDouble(startStopStep[1]));
				stepModel.setStep(Double.parseDouble(startStopStep[2]));
				return stepModel;
			}
			return null;
		}

		private MultiStepModel convertStringToMultiStepModel(String text) {
			final MultiStepModel multiStepModel = new MultiStepModel();
			multiStepModel.setName(scannableName);
			final String[] stepModelStrs = text.split(";");
			for (String stepModelStr : stepModelStrs) {
				final StepModel stepModel = convertStringToStepModel(stepModelStr.trim());
				if (stepModel == null) {
					return null;
				}
				multiStepModel.addRange(stepModel);
			}

			return multiStepModel;
		}

		private ArrayModel convertStringToArrayModel(String text) {
			final String[] strings = text.split(",");
			final double[] positions = new double[strings.length];
			for (int index = 0; index < strings.length; index++) {
				positions[index] = Double.parseDouble(strings[index]);
			}
			final ArrayModel arrayModel = new ArrayModel();
			arrayModel.setName(scannableName);
			arrayModel.setPositions(positions);
			return arrayModel;
		}
	}
}
