/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.util.Iterator;
import java.util.Optional;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableGetPosition;
import gda.device.scannable.ScannableGetPositionWrapper;
import gda.factory.Finder;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.ui.Activator;

/**
 * Create a scan path editor, consisting of a text box for direct entry of the scan path, and a button to bring up a
 * {@link MultiStepEditorDialog} as an alternative way of specifying the path.
 * <p>
 * <img src="scanpatheditor.png" />
 */
public class ScanPathEditor extends Composite implements IObservable {
	private static final Logger logger = LoggerFactory.getLogger(ScanPathEditor.class);

	private ObservableComponent observable = new ObservableComponent();

	private final Text axisText;
	private final DataBindingContext dataBindingContext = new DataBindingContext();
	private Binding axisBinding;
	private final Label currentValueLabel;
	private Scannable scannable;

	public ScanPathEditor(Composite parent, int style, IScanModelWrapper<IScanPathModel> scannableAxisParameters) {
		super(parent, style);
		final String scannableName = scannableAxisParameters.getName();
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(this);

		// Create text box to display/edit scan path definition
		axisText = new Text(this, SWT.BORDER);
		axisText.setToolTipText("A range <start stop step>\n"
				+ "or a list of points <pos1,pos2,pos3,pos4...>\n"
				+ "or a list of ranges <start1 stop1 step1; start2 stop2 step2>");
		GridDataFactory.fillDefaults().hint(300, SWT.DEFAULT).grab(true, false).applyTo(axisText);
		final IObservableValue<?> axisTextValue = WidgetProperties.text(SWT.Modify).observe(axisText);

		// Box to show current value
		currentValueLabel = new Label(this, SWT.NONE);
		GridDataFactory.swtDefaults().hint(50, SWT.DEFAULT).applyTo(currentValueLabel);
		final Optional<Scannable> optScannable = Finder.getInstance().findOptional(scannableName);
		if (optScannable.isPresent()) {
			scannable = optScannable.get();
			updateCurrentValue();
			scannable.addIObserver((source, arg) -> updateCurrentValue());
		} else {
			logger.error("Cannot find scannable '{}'", scannableName);
		}

		// Button to display a MultiStepEditorDialog as an alternative way of editing the scan path definition
		final Button multiStepButton = new Button(this, 0);
		multiStepButton.setImage(Activator.getImage("icons/pencil.png"));
		multiStepButton.setToolTipText("Edit a multi-step scan");
		multiStepButton.addSelectionListener(widgetSelectedAdapter(event -> {
			final Shell activeShell = Display.getDefault().getActiveShell();
			final MultiStepEditorDialog editorDialog = new MultiStepEditorDialog(activeShell, scannableName);
			final AxialMultiStepModel multiAxialStepModel = convertTextToModel(axisText.getText(), scannableName);
			editorDialog.setModel(multiAxialStepModel);
			if (editorDialog.open() == Window.OK) {
				try {
					setScanPathModel(editorDialog.getEditor().getModel());
				} catch (Exception e) {
					logger.error("Cannot retrieve MultiAxialStepModel from dialog", e);
				}
			}
		}));

		// Bind scan path text box to the model
		bindScanPathModelToTextField(scannableAxisParameters, axisTextValue);
	}

	private void updateCurrentValue() {
		Display.getDefault().asyncExec(() -> {
			if (currentValueLabel.isDisposed()) {
				logger.warn("Attempt to update current value label when disposed");
				return;
			}
			try {
				final ScannableGetPosition wrapper = new ScannableGetPositionWrapper(scannable.getPosition(), scannable.getOutputFormat());
				final String position = wrapper.getStringFormattedValues()[0];
				currentValueLabel.setText(position);
				currentValueLabel.setToolTipText(position); // set tooltip in case value is too long for label
			} catch (DeviceException e) {
				logger.error("Cannot get current value of '{}'", scannable.getName(), e);
			}
		});
	}

	/**
	 * @return scan path as displayed in the text box
	 */
	public String getAxisText() {
		return axisText.getText();
	}

	public void setScanPathModel(IScanPathModel model) {
		axisText.setText((String) new ScanPathToStringConverter().convert(model));
		observable.notifyIObservers(this, model);
	}

	private void bindScanPathModelToTextField(IScanModelWrapper<IScanPathModel> scannableAxisParameters, IObservableValue<?> axisTextValue) {
		final String scannableName = scannableAxisParameters.getName();
		@SuppressWarnings("unchecked")
		final IObservableValue<?> axisValue = BeanProperties.value("model").observe(scannableAxisParameters);

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
		axisBinding = dataBindingContext.bindValue(axisTextValue, axisValue, axisTextToModelStrategy, modelToAxisTextStrategy);
		ControlDecorationSupport.create(axisBinding, SWT.LEFT | SWT.TOP);

		// Update Mapping status label after model is modified from text widget
		axisBinding.getModel().addChangeListener(evt -> observable.notifyIObservers(this, axisBinding.getModel()));
	}

	/**
	 * Revalidate the model as this determines the severity of the validation status
	 */
	public void revalidate() {
		axisBinding.validateTargetToModel();
	}


	/**
	 * Convert text representing a scan path to a {@link AxialMultiStepModel}
	 *
	 * @param text
	 *            The text to convert (can be empty)
	 * @param scannableName
	 *            The name of the scannable to which the scan path refers
	 * @return The corresponding multi-step model
	 */
	private AxialMultiStepModel convertTextToModel(String text, String scannableName) {
		AxialMultiStepModel multiAxialStepModel = new AxialMultiStepModel();

		if (!text.isEmpty()) {
			final Object oldModel = (new StringToScanPathConverter(scannableName)).convert(text);

			if (oldModel instanceof AxialMultiStepModel) {
				multiAxialStepModel = (AxialMultiStepModel) oldModel;
			} else if (oldModel instanceof AxialStepModel) {
				multiAxialStepModel.getStepModels().add((AxialStepModel) oldModel);
			} else if (oldModel instanceof AxialArrayModel) {
				final double[] positions = ((AxialArrayModel) oldModel).getPositions();
				for (int i = 0; i < positions.length - 1; i++) {
					final AxialStepModel stepModel = new AxialStepModel(scannableName, positions[i], positions[i + 1], positions[i + 1] - positions[i]);
					multiAxialStepModel.getStepModels().add(stepModel);
				}
			}
		}
		multiAxialStepModel.setName(scannableName);
		return multiAxialStepModel;
	}

	/**
	 * Delete all observers when widget is disposed
	 */
	@Override
	public void dispose() {
		deleteIObservers();
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
			} else if (fromObject instanceof AxialStepModel) {
				return convertAxialStepModel((AxialStepModel) fromObject);
			} else if (fromObject instanceof AxialArrayModel) {
				return convertAxialArrayModel((AxialArrayModel) fromObject);
			} else if (fromObject instanceof AxialMultiStepModel) {
				return convertMultiAxialStepModel((AxialMultiStepModel) fromObject);
			} else {
				// We only expect path model types that can be created from this GUI
				throw new IllegalArgumentException("Unknown model type: " + fromObject.getClass());
			}
		}

		private String convertAxialStepModel(AxialStepModel stepModel) {
			final StringBuilder stringBuilder = new StringBuilder();
			appendAxialStepModel(stepModel, stringBuilder);

			return stringBuilder.toString();
		}

		private void appendAxialStepModel(AxialStepModel stepModel, StringBuilder stringBuilder) {
			stringBuilder.append(doubleToString(stepModel.getStart()));
			stringBuilder.append(' ');
			stringBuilder.append(doubleToString(stepModel.getStop()));
			stringBuilder.append(' ');
			stringBuilder.append(doubleToString(stepModel.getStep()));
		}

		private String convertMultiAxialStepModel(AxialMultiStepModel multiAxialStepModel) {
			final Iterator<AxialStepModel> iter = multiAxialStepModel.getStepModels().iterator();

			final StringBuilder stringBuilder = new StringBuilder();
			while (iter.hasNext()) {
				appendAxialStepModel(iter.next(), stringBuilder);
				if (iter.hasNext()) {
					stringBuilder.append(";");
				}
			}

			return stringBuilder.toString();
		}

		private String convertAxialArrayModel(AxialArrayModel arrayModel) {
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
	 * step2</li>ObservableComponent
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
					return convertStringToAxialArrayModel(text);
				} else if (text.contains(";")) {
					return convertStringToMultiAxialStepModel(text);
				} else {
					return convertStringToAxialStepModel(text);
				}
			} catch (Exception e) {
				logger.error("Could not convert string to model", e);
				return null;
			}
		}

		private AxialStepModel convertStringToAxialStepModel(String text) {
			final String[] startStopStep = text.split(" ");
			if (startStopStep.length == 3) {
				AxialStepModel stepModel = new AxialStepModel();
				stepModel.setName(scannableName);
				stepModel.setStart(Double.parseDouble(startStopStep[0]));
				stepModel.setStop(Double.parseDouble(startStopStep[1]));
				stepModel.setStep(Double.parseDouble(startStopStep[2]));
				return stepModel;
			}
			return null;
		}

		private IScanPathModel convertStringToMultiAxialStepModel(String text) {
			final String[] stepModelStrs = text.split(";");

			// If there is only one step specified, return a AxialStepModel
			if (stepModelStrs.length == 1) {
				return convertStringToAxialStepModel(stepModelStrs[0].trim());
			}

			final AxialMultiStepModel multiAxialStepModel = new AxialMultiStepModel();
			multiAxialStepModel.setName(scannableName);
			for (String stepModelStr : stepModelStrs) {
				final AxialStepModel stepModel = convertStringToAxialStepModel(stepModelStr.trim());
				if (stepModel == null) {
					return null;
				}
				multiAxialStepModel.addRange(stepModel);
			}

			return multiAxialStepModel;
		}

		private AxialArrayModel convertStringToAxialArrayModel(String text) {
			final String[] strings = text.split(",");
			final double[] positions = new double[strings.length];
			for (int index = 0; index < strings.length; index++) {
				positions[index] = Double.parseDouble(strings[index]);
			}
			final AxialArrayModel arrayModel = new AxialArrayModel();
			arrayModel.setName(scannableName);
			arrayModel.setPositions(positions);
			return arrayModel;
		}
	}

	@Override
	public void addIObserver(IObserver observer) {
		observable.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observable.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observable.deleteIObservers();
	}
}
