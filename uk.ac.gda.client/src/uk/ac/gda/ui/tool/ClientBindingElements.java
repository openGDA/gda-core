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

package uk.ac.gda.ui.tool;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateSetStrategy;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.SelectObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import gda.observable.IObservable;
import gda.observable.IObserver;

/**
 * @author Maurizio Nagni
 */
public final class ClientBindingElements {

	private ClientBindingElements() {
	}

	/**
	 * @param dbc
	 *            the binding context
	 * @param enumClass
	 *            the enum to map
	 * @param modelProperty
	 *            the property name to bind
	 * @param model
	 *            the model containing the property
	 * @param enumRadioMap
	 *            the map between enum elements and their Widget counterpart
	 */
	public static final <T> void bindEnumToRadio(final DataBindingContext dbc, final Class<T> enumClass,
			String modelProperty, final Object model, final Map<T, Object> enumRadioMap) {
		SelectObservableValue<T> enumObservable = new SelectObservableValue<>(enumClass);
		enumRadioMap.keySet().stream()
				.forEach(k -> enumObservable.addOption(k, WidgetProperties.selection().observe(enumRadioMap.get(k))));
		dbc.bindValue(enumObservable, PojoProperties.value(modelProperty).observe(model));
	}

	public static final <T> void bindText(DataBindingContext dbc, Text target, Class<T> clazz, String modelProperty,
			final Object model) {
		IObservableValue<Text> iTarget = WidgetProperties.text(SWT.Modify).observe(target);
		IObservableValue<T> iModel = PojoProperties.value(modelProperty, clazz).observe(model);
		UpdateValueStrategy iTargetToModelStrategy = new UpdateValueStrategy();
		UpdateValueStrategy iModelToTargetStrategy = new UpdateValueStrategy();
		dbc.bindValue(iTarget, iModel, iTargetToModelStrategy, iModelToTargetStrategy);
	}

	public static final void bindCheckBox(DataBindingContext dbc, Button target, String modelProperty,
			final Object model) {
		IObservableValue<Button> iTarget = WidgetProperties.selection().observe(target);
		IObservableValue<Boolean> iModel = PojoProperties.value(modelProperty, Boolean.class).observe(model);
		UpdateValueStrategy iTargetToModelStrategy = new UpdateValueStrategy();
		UpdateValueStrategy iModelToTargetStrategy = new UpdateValueStrategy();
		dbc.bindValue(iTarget, iModel, iTargetToModelStrategy, iModelToTargetStrategy);
	}

	public static final <T> void bindCheckBoxToSet(DataBindingContext dbc, Set target, String modelProperty,
			final Object model, final Class<T> clazz) {
		IObservableSet<T> iTarget = new WritableSet<>(target, clazz);
		IObservableSet<T> iModel = PojoProperties.set(modelProperty, clazz).observe(model);
		UpdateSetStrategy iTargetToModelStrategy = new UpdateSetStrategy();
		UpdateSetStrategy iModelToTargetStrategy = new UpdateSetStrategy();
		dbc.bindSet(iTarget, iModel, iTargetToModelStrategy, iModelToTargetStrategy);
	}

	/**
	 * Adds and removes an {@link IObserver} following a {@link Composite} lifecycle.
	 * <p>
	 * More in details, this method
	 * <ul>
	 * <li>adds an {@code observer} to an {@code observable}</li>
	 * <li>removes the {@code observer} when the {@code container} is disposed</li>
	 * </ul>
	 * </p>
	 *
	 * @param container the GUI element requesting to add the {@code observer}
	 * @param observable the element to add the {@code observer}
	 * @param observer the observing element
	 */
	public static final void addDisposableObserver(Composite container, IObservable observable, IObserver observer) {
		observable.addIObserver(observer);
		container.addDisposeListener(e -> {
			observable.deleteIObserver(observer);
		});
	}
}
