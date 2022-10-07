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

import static org.metawidget.inspector.InspectionResultConstants.ACTION;
import static org.metawidget.inspector.InspectionResultConstants.HIDDEN;
import static org.metawidget.inspector.InspectionResultConstants.NAME;
import static org.metawidget.inspector.InspectionResultConstants.TRUE;
import static uk.ac.diamond.daq.guigenerator.RichbeansAnnotationsInspector.DELETE_METHOD;
import static uk.ac.diamond.daq.guigenerator.RichbeansAnnotationsInspector.MAXIMUM_VALUE;
import static uk.ac.diamond.daq.guigenerator.RichbeansAnnotationsInspector.MINIMUM_VALUE;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.metawidget.swt.SwtMetawidget;
import org.metawidget.util.ClassUtils;
import org.metawidget.util.WidgetBuilderUtils;
import org.metawidget.util.XmlUtils;
import org.metawidget.widgetbuilder.iface.WidgetBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TableWidgetBuilder implements WidgetBuilder<Control, SwtMetawidget> {

	private final Logger logger = LoggerFactory.getLogger(TableWidgetBuilder.class);

	@Override
	public Control buildWidget(String elementName, Map<String, String> attributes, SwtMetawidget metawidget) {

		// If hidden or an action, return null
		if (TRUE.equals(attributes.get(HIDDEN)) || ACTION.equals(elementName)) {
			return null;
		}

		// Otherwise get the type and if a List, build a table
		Class<?> container = WidgetBuilderUtils.getActualClassOrType( attributes, Object.class );
		String contained = WidgetBuilderUtils.getComponentType(attributes);
		Class<?> containedClazz = loadContainedClass(contained);
		Method deleteMethod = getMethod(metawidget.getToInspect(), containedClazz, attributes);

		if (List.class.isAssignableFrom(container) && contained != null && containedClazz != null){
			TableViewer tableViewer = new TableViewer(metawidget);
			Table table = tableViewer.getTable();
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			tableViewer.setContentProvider(new ListStructuredContentProvider());

			List<TableColumnModel> columns = readPropertiesFromType(metawidget, contained);
			createColumns(columns, tableViewer);
			createDeleteButtons(tableViewer, deleteMethod, metawidget.getToInspect());

			setupDataInput(containedClazz, columns, attributes, metawidget, tableViewer);

			return tableViewer.getControl();
		}
		return null;
	}

	private Method getMethod(Object toInspect, Class<?> contained, Map<String,String> attributes){
		String methodName = attributes.get(DELETE_METHOD);
		if (toInspect != null && contained != null && methodName != null){
			try {
				return toInspect.getClass().getMethod(methodName, contained);
			} catch (NoSuchMethodException | SecurityException e) {
				logger.warn("couldn't find specified delete method", e);
			}
		}
		return null;
	}

	private Class<?> loadContainedClass(String contained) {
		if (contained != null){
			return ClassUtils.niceForName(contained);
		}
		return null;
	}

	private void setupDataInput(Class<?> containedClazz, List<TableColumnModel> columns, Map<String, String> attributes, SwtMetawidget metawidget, TableViewer tableViewer) {
		Object toInspect = metawidget.getToInspect();

		String propertyName = attributes.get(NAME);
		IBeanValueProperty value = BeanProperties.value(toInspect.getClass(), propertyName);

		Realm realm = DisplayRealm.getRealm(metawidget.getDisplay());
		IObservableValue observableValue = value.observe(realm, toInspect);

		List<IBeanValueProperty> itemProperties = columns.stream()
				.map(column -> column.getName())
				.map(property -> BeanProperties.value(containedClazz, property))
				.collect(Collectors.toList());

		observableValue.addValueChangeListener(new IValueChangeListener() {
			@Override
			public void handleValueChange(ValueChangeEvent event) {
				listenToItems(tableViewer, toInspect, value, realm, itemProperties);
				tableViewer.setInput(value.getValue(toInspect));
				tableViewer.refresh();
			}
		});

		listenToItems(tableViewer, toInspect, value, realm, itemProperties);
		tableViewer.setInput(value.getValue(toInspect));
	}

	private void listenToItems(TableViewer tableViewer, Object toInspect, IBeanValueProperty value, Realm realm, List<IBeanValueProperty> itemProperties) {
		for (Object item : (List<?>)value.getValue(toInspect)) {
			for (IBeanValueProperty itemValue : itemProperties) {
				IObservableValue observableItem = itemValue.observe(realm, item);
				observableItem.addValueChangeListener((event) -> tableViewer.refresh());
			}
		}
	}

	private void createColumns(List<TableColumnModel> columns, TableViewer tableViewer) {
		for (TableColumnModel column : columns.stream().filter(column -> !column.isHidden()).collect(Collectors.toList())) {
			TableViewerColumn columnViewer = new TableViewerColumn(tableViewer, SWT.NONE);
			columnViewer.getColumn().setText(column.getLabel());
			columnViewer.setLabelProvider(new TableColumnLabelProvider(column.getName()));
			columnViewer.setEditingSupport(new TableCellEditingSupport(columnViewer.getViewer(), tableViewer.getTable(), column));
			columnViewer.getColumn().pack();
		}
	}

	private void createDeleteButtons(TableViewer tableViewer, Method action, Object toInspect) {
		if (action != null){
			TableViewerColumn columnViewer = new TableViewerColumn(tableViewer, SWT.NONE);
			columnViewer.setLabelProvider(new DeleteLableProvider(action, toInspect));
			columnViewer.getColumn().pack();
		}
	}

	private List<TableColumnModel> readPropertiesFromType(SwtMetawidget metawidget, String componentType) {
		String inspectedType = metawidget.inspect( null, componentType, (String[]) null );

		List<TableColumnModel> columns = new ArrayList<>();
		Element root = XmlUtils.documentFromString( inspectedType ).getDocumentElement();
		NodeList elements = root.getFirstChild().getChildNodes();

		for ( int i = 0; i < elements.getLength(); i++ ) {
		   Node node = elements.item(i);
		   Map<String, String> attributesAsMap = XmlUtils.getAttributesAsMap( node );
		   TableColumnModel model = new TableColumnModel(
				   attributesAsMap.get(NAME),
				   metawidget.getLabelString(attributesAsMap),
				   attributesAsMap.get(HIDDEN),
				   attributesAsMap.get(MINIMUM_VALUE),
				   attributesAsMap.get(MAXIMUM_VALUE)
				   );
		   columns.add(model);
		}
		return columns;
	}
}
