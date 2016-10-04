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

package uk.ac.diamond.daq.guigenerator;

import static org.metawidget.inspector.InspectionResultConstants.NAME;
import static org.metawidget.inspector.InspectionResultConstants.PROPERTY;
import static org.metawidget.inspector.InspectionResultConstants.TRUE;
import static uk.ac.diamond.daq.guigenerator.RichbeansAnnotationsInspector.FILENAME;

import java.util.Map;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.richbeans.api.generator.RichbeansAnnotations.UiFilename;
import org.eclipse.richbeans.widgets.file.SelectorWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.metawidget.inspector.InspectionResultConstants;
import org.metawidget.swt.SwtMetawidget;
import org.metawidget.util.simple.PathUtils;
import org.metawidget.util.simple.StringUtils;
import org.metawidget.widgetbuilder.iface.WidgetBuilder;

/**
 * Widget builder for fields annotated with {@link UiFilename}.
 * Renders such fields with a {@link SelectorWidget}.
 */
public class FileChooserWidgetBuilder implements WidgetBuilder<Control, SwtMetawidget>{

	@Override
	public Control buildWidget(String elementName, Map<String, String> attributes, SwtMetawidget metaWidget) {
		if (TRUE.equals(attributes.get(FILENAME))) {
			Composite parent = metaWidget.getCurrentLayoutComposite();
			SelectorWidget selector = new SelectorWidget(parent, false, false, true);

			addDataBinding(selector, attributes, elementName, metaWidget);
			return selector.getComposite();
		}

		return null;
	}

	private void addDataBinding(SelectorWidget selector, Map<String, String> attributes,
			String elementName, SwtMetawidget metaWidget) {
		// The code in this method is based on DataBindingProcessor.processWidget()
		// An alternative implementation would be to create our own IObservableValue for a SelectorWidget

		// get the text control from the selector
		Composite composite = selector.getComposite();
		Text text = (Text) composite.getChildren()[0];

		// Observe the control
		Realm realm = DisplayRealm.getRealm(metaWidget.getDisplay());
		IObservableValue observeTarget = WidgetProperties.text(SWT.Modify).observe(realm, text);

		UpdateValueStrategy targetToModel;
		if (TRUE.equals(attributes.get(InspectionResultConstants.NO_SETTER))) {
			targetToModel = new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER);
		} else {
			targetToModel = new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE);
		}

		// Observe the model
		Object toInspect = metaWidget.getToInspect();
		String propertyName = PathUtils.parsePath(metaWidget.getInspectionPath()).getNames()
				.replace(StringUtils.SEPARATOR_FORWARD_SLASH_CHAR, StringUtils.SEPARATOR_DOT_CHAR);
		if (PROPERTY.equals(elementName)) {
			if (propertyName.length() > 0) {
				propertyName += StringUtils.SEPARATOR_DOT_CHAR;
			}

			propertyName += attributes.get(NAME);
		}

		IObservableValue observeModel = BeanProperties.value(toInspect.getClass(), propertyName).observe(realm, toInspect);
		UpdateValueStrategy modelToTarget = new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST);

		// Note: no need to add converters as both target and model values are of type String
		DataBindingContext bindingContext = getDataBindingContext(metaWidget);
		Binding binding = bindingContext.bindValue(observeTarget, observeModel, targetToModel, modelToTarget);
		binding.updateModelToTarget();
	}

	private DataBindingContext getDataBindingContext(SwtMetawidget metaWidget) {
		DataBindingContext context = (DataBindingContext) metaWidget.getData(FileChooserWidgetBuilder.class.getName());
		if (context == null) {
			context = new DataBindingContext(DisplayRealm.getRealm(metaWidget.getDisplay()));
			metaWidget.setData(FileChooserWidgetBuilder.class.getName(), context);
		}

		return context;
	}

}
