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

import java.util.List;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.points.models.ArrayModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.daq.mapping.api.IScanPathModelWrapper;

/**
 * A section for configuring the outer scannables or a scan, e.g. temperature.
 */
class OuterScannablesSection extends AbstractMappingSection {

	@Override
	public boolean shouldShow() {
		List<IScanPathModelWrapper> outerScannables = getMappingBean().getScanDefinition().getOuterScannables();
		return outerScannables != null && !outerScannables.isEmpty();
	}

	@Override
	public void createControls(Composite parent) {
		List<IScanPathModelWrapper> outerScannables = getMappingBean().getScanDefinition().getOuterScannables();
		Composite otherScanAxesComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(otherScanAxesComposite);
		final int axesColumns = 2;
		GridLayoutFactory.swtDefaults().numColumns(axesColumns).spacing(8, 5).applyTo(otherScanAxesComposite);
		Label otherScanAxesLabel = new Label(otherScanAxesComposite, SWT.NONE);
		otherScanAxesLabel.setText("Other Scan Axes");
		GridDataFactory.fillDefaults().span(axesColumns, 1).applyTo(otherScanAxesLabel);

		DataBindingContext dataBindingContext = new DataBindingContext();
		for (IScanPathModelWrapper scannableAxisParameters : outerScannables) {
			Button checkBox = new Button(otherScanAxesComposite, SWT.CHECK);
			checkBox.setText(scannableAxisParameters.getName());
			IObservableValue checkBoxValue = WidgetProperties.selection().observe(checkBox);
			IObservableValue activeValue = PojoProperties.value("includeInScan").observe(scannableAxisParameters);
			dataBindingContext.bindValue(checkBoxValue, activeValue);

			// FIXME make a proper widget for this?
			Text axisText = new Text(otherScanAxesComposite, SWT.BORDER);
			axisText.setToolTipText("<start stop step> or <pos1,pos2,pos3,pos4...>");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(axisText);
			IObservableValue axisTextValue = WidgetProperties.text(SWT.Modify).observe(axisText);
			IObservableValue axisValue = PojoProperties.value("model").observe(scannableAxisParameters);
			UpdateValueStrategy axisTextToModelStrategy = new UpdateValueStrategy();
			axisTextToModelStrategy.setConverter(new Converter(String.class, IScanPathModel.class) {
				@Override
				public Object convert(Object fromObject) {
					try {
						String text = (String) fromObject;
						String[] startStopStep = text.split(" ");
						if (startStopStep.length == 3) {
							StepModel stepModel = new StepModel();
							stepModel.setName(scannableAxisParameters.getName());
							stepModel.setStart(Double.parseDouble(startStopStep[0]));
							stepModel.setStop(Double.parseDouble(startStopStep[1]));
							stepModel.setStep(Double.parseDouble(startStopStep[2]));
							return stepModel;
						} else {
							String[] strings = text.split(",");
							double[] positions = new double[strings.length];
							for (int index = 0; index < strings.length; index++) {
								positions[index] = Double.parseDouble(strings[index]);
							}
							ArrayModel arrayModel = new ArrayModel();
							arrayModel.setName(scannableAxisParameters.getName());
							arrayModel.setPositions(positions);
							return arrayModel;
						}
					} catch (NumberFormatException nfe) {
						return null;
					}
				}
			});
			axisTextToModelStrategy.setBeforeSetValidator(value -> {
				if (value instanceof IScanPathModel) {
					return ValidationStatus.ok();
				}
				String message = "Text is incorrectly formatted";
				if (scannableAxisParameters.isIncludeInScan()) {
					return ValidationStatus.error(message);
				} else {
					return ValidationStatus.warning(message);
				}
			});
			Binding bindValue = dataBindingContext.bindValue(axisTextValue, axisValue, axisTextToModelStrategy,
					new UpdateValueStrategy());
			ControlDecorationSupport.create(bindValue, SWT.LEFT | SWT.TOP);
		}
	}

}
