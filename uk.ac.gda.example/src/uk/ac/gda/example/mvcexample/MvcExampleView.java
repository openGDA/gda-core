/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.example.mvcexample;

import gda.rcp.GDAClientActivator;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import uk.ac.gda.client.composites.MotorPositionEditorControl;
import uk.ac.gda.client.observablemodels.ScannableWrapper;
import uk.ac.gda.ui.components.NumberEditorControl;

/***
 * Example view in a model view controller framework. The model is provided by the OSGI service obtained by
 * GDAClientActivator.getNamedService The model is linked to the UI controls by Databinding. Note that the mode provide
 * property change support and so we use the BeanProperties factory. See
 * http://www.vogella.com/articles/EclipseDataBinding/article.html for a great introduction to JFace Data Binding
 */
public class MvcExampleView extends ViewPart {
	// private static final Logger logger = LoggerFactory.getLogger(MvcExampleView.class);

	public static final String ID = "uk.ac.gda.example.mvcexample.MvcExampleView"; //$NON-NLS-1$
	private FormToolkit toolkit;
	DataBindingContext bindingContext;

	protected MotorPositionEditorControl motorPosControl;

	protected NumberEditorControl numberControl;

	protected Button btn1;
	private MvcExampleModel model;
	protected TableViewer viewer;
	protected TableViewer viewer2;

	public MvcExampleView() {
	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {

		model = (MvcExampleModel) GDAClientActivator.getNamedService(MvcExampleModel.class, null);
		toolkit = new FormToolkit(parent.getDisplay());
		Composite cmpRoot = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout();
		cmpRoot.setLayout(layout);

		btn1 = toolkit.createButton(cmpRoot, "Press Me", SWT.CHECK);

		try {
			numberControl = new NumberEditorControl(cmpRoot, SWT.NONE, model, MvcExampleModel.POSITION_PROPERTY_NAME,
					false);
			numberControl.setRange(0, 100);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(numberControl);
			toolkit.adapt(numberControl);
		} catch (Exception e) {
			throw new RuntimeException("Error adding numberControl to UI", e);
		}

		try {
			ScannableWrapper scannableWrapper = model != null ? model.getScannableWrapper() : null;
			motorPosControl = new MotorPositionEditorControl(cmpRoot, SWT.NONE, scannableWrapper, false);
			motorPosControl.setRange(0, 100);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(motorPosControl);
			toolkit.adapt(motorPosControl);
		} catch (Exception e) {
			throw new RuntimeException("Error adding motorPosControl to UI", e);
		}

		// Define the viewer to display items from model - this is bound to model by ObservableListContentProvider and ObservableMapLabelProvider
		viewer = new TableViewer(parent);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		{
			TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setWidth(100);
			column.getColumn().setText(MvcExampleItem.VALUE_PROPERTY_NAME);
			viewer.getTable().setHeaderVisible(true);
		}

		// Define the viewer to display items from model - this will use ViewSupport to bind to model
		viewer2 = new TableViewer(parent);
		viewer2.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		{
			TableViewerColumn column = new TableViewerColumn(viewer2, SWT.NONE);
			column.getColumn().setWidth(100);
			column.getColumn().setText(MvcExampleItem.VALUE_PROPERTY_NAME);
			viewer2.getTable().setHeaderVisible(true);
		}

		createActions();
		initializeToolBar();
		initializeMenu();

		/***
		 * JFace Data Binding provides functionality to bind the data of JFace viewers , e.g. for TableViewers. Data
		 * binding for these viewers distinguish between changes in the collection and changes in the individual
		 * object.In the case that Data Binding observes a collection, it requires a ContentProvider which notifies it,
		 * once the data in the collection changes. ObservableListContentProvider is a ContentProvider which requires a
		 * list implementing the IObservableList interface. The Properties class allows you to wrap another list with
		 * its selfList() method into an IObservableList.
		 */
		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		viewer.setContentProvider(contentProvider);
		// IObservableList input = Properties.selfList(MvcExampleItem.class).observe(model.getItems());

		// create the label provider including monitoring
		// of the changes of the labels
		IObservableSet knownElements = contentProvider.getKnownElements();

		final IObservableMap values = BeanProperties.value(MvcExampleItem.class, MvcExampleItem.VALUE_PROPERTY_NAME)
				.observeDetail(knownElements);

		IObservableMap[] labelMaps = { values };

		ILabelProvider labelProvider = new ObservableMapLabelProvider(labelMaps) {
			@Override
			public String getText(Object element) {
				return values.get(element).toString();
			}
		};
		viewer.setLabelProvider(labelProvider);
		IObservableList input = model.getItems();
		viewer.setInput(input);

		ViewerSupport.bind(viewer2, input, 
			    BeanProperties.
			    values(new String[] { MvcExampleItem.VALUE_PROPERTY_NAME })); 		
		
		bindingContext = initDataBindings();
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		// IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		// IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	protected DataBindingContext initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();
		//
		IObservableValue observeSelectionBtn1ObserveWidget = WidgetProperties.selection().observe(btn1);
		IObservableValue selectedModelObserveValue = BeanProperties.value("selected").observe(model);
		bindingContext.bindValue(observeSelectionBtn1ObserveWidget, selectedModelObserveValue, null, null);
		//
		return bindingContext;
	}
}
