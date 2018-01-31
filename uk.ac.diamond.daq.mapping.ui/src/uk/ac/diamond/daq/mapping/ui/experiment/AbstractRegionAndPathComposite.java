/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.ui.IStageScanConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;
import uk.ac.diamond.daq.mapping.ui.NumberAndUnitsComposite;
import uk.ac.diamond.daq.mapping.ui.NumberUnitsWidgetProperty;

public abstract class AbstractRegionAndPathComposite extends Composite {

	protected final GridDataFactory gdControls = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);
	private final DataBindingContext dbc = new DataBindingContext();
	private final IStageScanConfiguration mappingStageInfo;


	public AbstractRegionAndPathComposite(Composite parent, int style) {
		super(parent, style);
		mappingStageInfo = getMappingStageInfo();
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(this);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);
	}

	protected String getFastAxisName() {
		return mappingStageInfo.getActiveFastScanAxis();
	}

	protected String getSlowAxisName() {
		return mappingStageInfo.getActiveSlowScanAxis();
	}

	private IStageScanConfiguration getMappingStageInfo() {
		BundleContext context = FrameworkUtil.getBundle(AbstractRegionAndPathComposite.class).getBundleContext();
		MappingStageInfo stageInfo = context.getService(context.getServiceReference(MappingStageInfo.class));
		if (Objects.isNull(stageInfo)) {
			stageInfo = new MappingStageInfo();
			stageInfo.setActiveFastScanAxis("Fast Axis");
			stageInfo.setActiveSlowScanAxis("Slow Axis");
		}
		return stageInfo;
	}



	// Some methods to simplify data binding & validation


	/**
	 * Validates that value > 0
	 */
	protected static final IValidator GREATER_THAN_ZERO = value -> ((double) value > 0.0) ? ValidationStatus.ok() : ValidationStatus.error("Value must be greater than zero!");
	private static final NumberUnitsWidgetProperty nuwProperty = new NumberUnitsWidgetProperty();

	/**
	 * Get an IObservableValue from a widget.
	 * @param widget the source widget
	 * @return an observable value observing this value property on the given widget - null if widget is not yet supported
	 */
	protected IObservableValue getObservableValue(Widget widget) {
		if (widget instanceof Text) return WidgetProperties.text(SWT.Modify).observe(widget);
		if (widget instanceof NumberAndUnitsComposite) return nuwProperty.observe(widget);
		if (widget instanceof Spinner || widget instanceof Button) return WidgetProperties.selection().observe(widget);
		return null;
	}

	/**
	 * Get an IObservableValue from a bean's field
	 * @param bean
	 * @param field e.g. "xStart"
	 * @return an observable value observing this value property on the given property source
	 */
	protected IObservableValue getObservableValue(String field, Object bean) {
		return BeanProperties.value(field).observe(bean);
	}

	/**
	 * Bind widget and bean's property. No validation required
	 * @param widget the source widget
	 * @param modelProperty the property name
	 * @param bean the property source
	 * @return created binding
	 */
	protected Binding bind(Widget widget, String modelProperty, Object bean) {
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
	protected Binding bind(Widget widget, String modelProperty, Object bean, IValidator validator) {
		IObservableValue target = getObservableValue(widget);
		IObservableValue model  = getObservableValue(modelProperty, bean);
		Binding binding;

		if (Objects.nonNull(validator)) {
			UpdateValueStrategy strategy = new UpdateValueStrategy();
			strategy.setBeforeSetValidator(validator);
			binding = dbc.bindValue(target, model, strategy, new UpdateValueStrategy());
			ControlDecorationSupport.create(binding, SWT.LEFT);
		} else {
			binding = dbc.bindValue(target, model);
		}

		return binding;
	}

	/**
	 * Bind two IObservableValues. Assumes they are already validated.
	 * @param targetObservableValue target value, commonly a UI widget
	 * @param modelObservableValue model value
	 * @return created binding
	 */
	protected Binding bind(IObservableValue targetObservableValue, IObservableValue modelObservableValue) {
		return dbc.bindValue(targetObservableValue, modelObservableValue);
	}
}