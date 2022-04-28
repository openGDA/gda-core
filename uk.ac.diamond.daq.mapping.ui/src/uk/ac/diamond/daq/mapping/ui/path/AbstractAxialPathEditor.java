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

package uk.ac.diamond.daq.mapping.ui.path;

import java.util.function.Function;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Abstract superclass for model editors that edit some type of {@link IAxialModel}
 * @param <T> model class
 */
public abstract class AbstractAxialPathEditor<T extends IAxialModel> extends AbstractPathEditor<T> {

	private static final class FunctionConverter<T1, T2> extends Converter<T1, T2> {

		private final Function<T1, T2> converterFunction;

		public FunctionConverter(Class<T1> fromClass, Class<T2> toClass, Function<T1, T2> converterFunction) {
			super(fromClass, toClass);
			this.converterFunction = converterFunction;
		}

		@Override
		public T2 convert(T1 fromObject) {
			return converterFunction.apply(fromObject);
		}

	}

	@Override
	public Composite createEditorPart(Composite parent) {
		// override to return null, we can't create the composite here as we don't know
		// the number of columns required
		return null;
	}

	/**
	 * @return a new composite with the given number of columns
	 */
	protected Composite makeComposite(Composite parent, int numColumns) {
		final Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.TOP).applyTo(composite);
		GridLayoutFactory.fillDefaults().numColumns(numColumns).applyTo(composite);
		return composite;
	}

	protected Text createLabelledText(Composite parent, String label, String propertyName) {
		new Label(parent, SWT.NONE).setText(label);

		final Text text = new Text(parent, SWT.BORDER);
		binder.bind(text, propertyName, getModel());
		grabHorizontalSpace.applyTo(text);
		return text;
	}

	protected <P> Binding createBinding(Text text, String propertyName, Class<P> propertyClass,
			Function<String, P> stringToPropertyValue, Function<P, String> propertyValueToString,
			IValidator<P> validator) {
		final UpdateValueStrategy<P, String> modelToTargetStrategy = new UpdateValueStrategy<>();
		modelToTargetStrategy.setConverter(new FunctionConverter<>(propertyClass, String.class, propertyValueToString));

		final UpdateValueStrategy<String, P> targetToModelStrategy = new UpdateValueStrategy<>();
		targetToModelStrategy.setConverter(new FunctionConverter<>(String.class, propertyClass, stringToPropertyValue));
		targetToModelStrategy.setBeforeSetValidator(validator);

		return binder.bind(text, getModel(), propertyName, modelToTargetStrategy, targetToModelStrategy);
	}

	protected String doubleToString(double doubleVal) {
		final String stringVal = Double.toString(doubleVal);
		if (stringVal.endsWith(".0")) {
			return stringVal.substring(0, stringVal.length() - 2);
		}
		return stringVal;
	}

	@Override
	public void dispose() {
		super.dispose();

		binder.dispose();
	}

}
