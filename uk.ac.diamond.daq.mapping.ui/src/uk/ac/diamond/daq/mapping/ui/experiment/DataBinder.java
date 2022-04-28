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

import java.util.Objects;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import uk.ac.gda.client.NumberAndUnitsComposite;
import uk.ac.gda.client.NumberUnitsWidgetProperty;

/**
 * Instances of this class hold a {@code DataBindingContext} and overloaded methods
 * {@code bind} and {@code getObservableValue} covering common binding blocks for the sake of code reuse.
 * <p>Say you have a Text widget 'myTxt' which you want to bind to the 'name' field of bean 'myBean'...
 * <pre>
 * {@code DataBinder dataBinder = new DataBinder();
 * Binding binding = dataBinder.bind(myTxt, "name", myBean);}
 * </pre>
 */
public class DataBinder {

	private final DataBindingContext bindingContext = new DataBindingContext();

	/**
	 * Validates that value > 0
	 */
	public static final IValidator<Double> GREATER_THAN_ZERO = value -> value > 0.0 ? ValidationStatus.ok() : ValidationStatus.error("Value must be greater than zero!");

	private static final NumberUnitsWidgetProperty<?, NumberAndUnitsComposite<?>> NUMBER_AND_UNIT_WIDGET_PROPERTY = new NumberUnitsWidgetProperty<>();

	/**
	 * Get an IObservableValue from a widget.
	 * @param widget the source widget
	 * @return an observable value observing this value property on the given widget - null if widget is not yet supported
	 */
	@SuppressWarnings("unchecked")
	public <T> IObservableValue<T> getObservableValue(Widget widget) {
		if (widget instanceof Text) {
			return (IObservableValue<T>)WidgetProperties.text(SWT.Modify).observe(widget);
		} else if (widget instanceof NumberAndUnitsComposite) {
			return (IObservableValue<T>) NUMBER_AND_UNIT_WIDGET_PROPERTY.observe((NumberAndUnitsComposite<?>) widget);
		} else if (widget instanceof Spinner || widget instanceof Button) {
			return (IObservableValue<T>) WidgetProperties.widgetSelection().observe(widget);
		}
		return null;
	}

	/**
	 * Get an IObservableValue from a bean's field
	 * @param bean
	 * @param field e.g. "xStart"
	 * @return an observable value observing this value property on the given property source
	 */
	@SuppressWarnings("unchecked")
	public <T> IObservableValue<T> getObservableValue(String field, Object bean) {
		return (IObservableValue<T>) BeanProperties.value(field).observe(bean);
	}

	/**
	 * Bind widget and bean's property. No validation required
	 * @param widget the source widget
	 * @param modelProperty the property name
	 * @param bean the property source
	 * @return created binding
	 */
	public Binding bind(Widget widget, String modelProperty, Object bean) {
		return bind(widget, modelProperty, bean, null);
	}

	/**
	 * Bind a widget and bean's property using validation and add widget decoration
	 * @param widget the source widget
	 * @param modelProperty the property name
	 * @param bean the property source
	 * @param validator can be null if no validation required
	 * @return created binding
	 */
	public <T, M> Binding bind(Widget widget, String modelProperty, Object bean, IValidator<M> validator) {
		final IObservableValue<T> target = getObservableValue(widget);
		final IObservableValue<M> model  = getObservableValue(modelProperty, bean);

		if (Objects.nonNull(validator)) {
			return bind(target, model, validator);
		}

		return bindingContext.bindValue(target, model);
	}

	/**
	 * Bind two IObservableValues. Assumes they are already validated.
	 * @param targetObservableValue target value, commonly a UI widget
	 * @param modelObservableValue model value
	 * @return created binding
	 */
	public <T, M> Binding bind(IObservableValue<T> targetObservableValue, IObservableValue<M> modelObservableValue) {
		return bindingContext.bindValue(targetObservableValue, modelObservableValue);
	}

	/**
	 * Bind two IObservableValues validating with provided IValidator, and create default decoration support
	 * @param target target value, commonly a UI widget
	 * @param model model value
	 * @param validator validator to use
	 * @return created binding
	 */
	public <T, M> Binding bind(IObservableValue<T> target, IObservableValue<M> model, IValidator<M> validator) {
		final UpdateValueStrategy<T, M> strategy = new UpdateValueStrategy<>();
		strategy.setBeforeSetValidator(validator);
		final Binding binding = bindingContext.bindValue(target, model, strategy, new UpdateValueStrategy<M, T>());
		ControlDecorationSupport.create(binding, SWT.LEFT);
		return binding;
	}

	/**
	 * Bind a widget and bean property, using custom update strategies.
	 * @param widget widget
	 * @param modelBean model bean
	 * @param modelProperty model property name
	 * @param modelToTarget custom strategy to update target from model
	 * @param targetToModel custom strategy to update model from target
	 * @return binding
	 *
	 */
	public <T, M> Binding bind(Widget widget, Object modelBean, String modelProperty,
			UpdateValueStrategy<M, T> modelToTarget, UpdateValueStrategy<T, M> targetToModel) {
		final IObservableValue<T> target = getObservableValue(widget);
		final IObservableValue<M> model  = getObservableValue(modelProperty, modelBean);

		final Binding binding = bindingContext.bindValue(target, model, targetToModel, modelToTarget);
		ControlDecorationSupport.create(binding, SWT.LEFT);
		return binding;
	}

	public void dispose() {
		bindingContext.dispose();
	}

}
