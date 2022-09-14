/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.ui.driver;

import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.STRETCH;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.daq.experiment.api.driver.DriverProfileSection;
import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;
import uk.ac.diamond.daq.experiment.ui.widget.ElementEditor;

public class DriverProfileSectionEditor implements ElementEditor {

	private Text start;
	private Text stop;
	private Text duration;
	private Label startUnits;
	private Label stopUnits;

	private final Color invalidEntryColour;

	private ControlDecoration durationDecoration;

	private DriverProfileSection section;
	private final DataBindingContext dbc;
	private List<Binding> bindings;

	public DriverProfileSectionEditor() {
		dbc = new DataBindingContext();
		bindings = new ArrayList<>();
		invalidEntryColour = new Color(Display.getDefault(), 255, 210, 198);
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().margins(10, 10).numColumns(4).equalWidth(true).applyTo(composite);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);

		new Label(composite, SWT.NONE).setText("Start");
		start = new Text(composite, SWT.BORDER);
		STRETCH.copy().span(2, 1).applyTo(start);
		startUnits = new Label(composite, SWT.NONE);
		STRETCH.applyTo(startUnits);

		new Label(composite, SWT.NONE).setText("Stop");
		stop = new Text(composite, SWT.BORDER);
		STRETCH.copy().span(2, 1).applyTo(stop);
		stopUnits = new Label(composite, SWT.NONE);
		STRETCH.applyTo(stopUnits);

		new Label(composite, SWT.NONE).setText("Duration");
		duration = new Text(composite, SWT.BORDER);
		durationDecoration = new ControlDecoration(duration, SWT.RIGHT | SWT.TOP);
		STRETCH.copy().span(2, 1).applyTo(duration);
		new Label(composite, SWT.NONE).setText("min");

		enableControls(false);
	}

	@Override
	public void load(EditableWithListWidget element) {
		removeOldBindings();
		section = (DriverProfileSection) element;
		recreateBindings();
		enableControls(true);
	}

	private void recreateBindings() {
		IObservableValue<String> startWidget = WidgetProperties.text(SWT.Modify).observe(start);
		IObservableValue<String> stopWidget = WidgetProperties.text(SWT.Modify).observe(stop);
		IObservableValue<String> durationWidget = WidgetProperties.text(SWT.Modify).observe(duration);

		IObservableValue<Double> startModel = BeanProperties.value("start", double.class).observe(section);
		IObservableValue<Double> stopModel = BeanProperties.value("stop", double.class).observe(section);
		IObservableValue<Double> durationModel = BeanProperties.value("duration", double.class).observe(section);

		bindings.add(dbc.bindValue(startWidget, startModel));
		bindings.add(dbc.bindValue(stopWidget, stopModel));

		IValidator<Double> positiveDuration = value -> {
			if (value >= 0) {
				durationDecoration.hide();
				duration.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
				return ValidationStatus.ok();
			} else {
				duration.setBackground(invalidEntryColour);
				durationDecoration.show();
				durationDecoration.showHoverText("Duration cannot be negative!");

				return ValidationStatus.error("Duration cannot be negative!");
			}
		};

		UpdateValueStrategy<String, Double> strategy = new UpdateValueStrategy<>();
		strategy.setBeforeSetValidator(positiveDuration);
		bindings.add(dbc.bindValue(durationWidget, durationModel, strategy, new UpdateValueStrategy<>()));

	}

	private void removeOldBindings() {
		bindings.forEach(binding -> {
			dbc.removeBinding(binding);
			binding.dispose();
		});
		bindings.clear();
	}

	@Override
	public void clear() {
		removeOldBindings();
		section = null;
		enableControls(false);
	}

	private void enableControls(boolean enable) {
		Arrays.asList(start, stop, duration).forEach(widget -> widget.setEnabled(enable));
	}

	public void setUnits(String units) {
		startUnits.setText(units);
		stopUnits.setText(units);
	}

}
