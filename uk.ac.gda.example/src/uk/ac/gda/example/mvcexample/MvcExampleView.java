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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import gda.rcp.GDAClientActivator;
import uk.ac.gda.client.composites.MotorPositionEditorControl;
import uk.ac.gda.client.observablemodels.ScannableWrapper;
import uk.ac.gda.common.rcp.jface.viewers.ObservableMapCellControlProvider;
import uk.ac.gda.common.rcp.jface.viewers.ObservableMapCellControlProvider.ControlFactoryAndUpdater;
import uk.ac.gda.common.rcp.jface.viewers.ObservableMapColumnLabelProvider;
import uk.ac.gda.common.rcp.jface.viewers.ObservableMapOwnerDrawProvider;
import uk.ac.gda.ui.components.NumberEditorControl;

/***
 * Example view in a model view controller framework. The model is provided by the OSGI service obtained by
 * GDAClientActivator.getNamedService The model is linked to the UI controls by DataBinding. Note that the mode provide
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

		model = GDAClientActivator.getNamedService(MvcExampleModel.class, null);
		Object elementType = model.getItems().getElementType();
		if (!elementType.equals(MvcExampleItem.class))
			throw new RuntimeException("model is invalid. elementType = " + elementType);
		toolkit = new FormToolkit(parent.getDisplay());
		Composite cmpRoot = toolkit.createComposite(parent);
		cmpRoot.setLayout(new GridLayout(1, false));

		btn1 = toolkit.createButton(cmpRoot, "Press Me", SWT.CHECK);

		try {
			numberControl = new NumberEditorControl(cmpRoot, SWT.NONE, model, MvcExampleModel.POSITION_PROPERTY_NAME,
					false);
			numberControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
			numberControl.setRange(0, 100);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(numberControl);
			toolkit.adapt(numberControl);
		} catch (Exception e) {
			throw new RuntimeException("Error adding numberControl to UI", e);
		}

		try {
			ScannableWrapper scannableWrapper = model != null ? model.getScannableWrapper() : null;
			motorPosControl = new MotorPositionEditorControl(cmpRoot, SWT.NONE, scannableWrapper, false);
			motorPosControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
			motorPosControl.setRange(0, 100);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(motorPosControl);
			toolkit.adapt(motorPosControl);
		} catch (Exception e) {
			throw new RuntimeException("Error adding motorPosControl to UI", e);
		}
		{

			// Define the viewer to display items from model - this is bound to model by ObservableListContentProvider
			// and
			// ObservableMapLabelProvider
			IObservableList input = model.getItems();
			createTable1(cmpRoot, input);
			createTable2(cmpRoot, input);
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

		bindingContext = initDataBindings();
	}

	/**
	 * Creates a table viewer of the model using ViewerSupport to bind to the model
	 */
	private void createTable2(Composite cmpRoot, IObservableList input) {

		// Define the viewer to display items from model - this will use ViewSupport to bind to model
		viewer2 = new TableViewer(cmpRoot);
		Table table_1 = viewer2.getTable();
		toolkit.adapt(table_1);
		viewer2.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableViewerColumn column = new TableViewerColumn(viewer2, SWT.NONE);
		viewer2.getTable().setHeaderVisible(true);
		{
			column.getColumn().setWidth(100);
			column.getColumn().setText(MvcExampleItem.VALUE_PROPERTY_NAME);
		}

		ViewerSupport.bind(viewer2, input, BeanProperties.values(new String[] { MvcExampleItem.VALUE_PROPERTY_NAME }));
	}

	/**
	 * Creates a table viewer of the model using ObservableListContentProvider and Observable label providers to bind to
	 * the model
	 */
	private void createTable1(Composite cmpRoot, IObservableList input) {
		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		viewer = new TableViewer(cmpRoot);
		Table table = viewer.getTable();
		table.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		table.setLinesVisible(true);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TableViewerColumn column0 = new TableViewerColumn(viewer, SWT.NONE);
		column0.getColumn().setWidth(100);
		column0.getColumn().setText(MvcExampleItem.NAME_PROPERTY_NAME);

		IObservableSet knownElements0 = contentProvider.getKnownElements();

		final IObservableMap values0 = BeanProperties.value(MvcExampleItem.class, MvcExampleItem.NAME_PROPERTY_NAME)
				.observeDetail(knownElements0);

		column0.setLabelProvider(new ObservableMapColumnLabelProvider(values0));


		TableViewerColumn column1 = new TableViewerColumn(viewer, SWT.NONE);
		column1.getColumn().setWidth(100);
		column1.getColumn().setText(MvcExampleItem.VALUE_PROPERTY_NAME);

		IObservableSet knownElements = contentProvider.getKnownElements();

		final IObservableMap values = BeanProperties.value(MvcExampleItem.class, MvcExampleItem.VALUE_PROPERTY_NAME)
				.observeDetail(knownElements);

		column1.setLabelProvider(new ObservableMapColumnLabelProvider(values));

		TableViewerColumn column2 = new TableViewerColumn(viewer, SWT.NONE);
		column2.getColumn().setText("Progress");
		column2.getColumn().setWidth(52);
		ControlFactoryAndUpdater factory = new ObservableMapCellControlProvider.ControlFactoryAndUpdater() {

			@Override
			public Control createControl(Composite parent) {
				ProgressBar progressBar = new ProgressBar(parent, SWT.NONE);
				progressBar.setMaximum(100);
				return progressBar;
			}

			@Override
			public void updateControl(Control control, Object value) {
				((ProgressBar)control).setSelection( ((Double) value).intValue());

			}
		};
		column2.setLabelProvider(new ObservableMapCellControlProvider(values, factory, "Column2"));

		TableViewerColumn column3 = new TableViewerColumn(viewer, SWT.NONE);
		column3.getColumn().setText("Done");
		column3.getColumn().setWidth(20);
		ControlFactoryAndUpdater factory2 = new ObservableMapCellControlProvider.ControlFactoryAndUpdater() {

			org.eclipse.swt.graphics.Color green = null;
			org.eclipse.swt.graphics.Color yellow = null;

			@Override
			public Control createControl(Composite parent) {
				Button button = new Button(parent, SWT.CHECK | SWT.FLAT);
				button.setVisible(true);
				button.setSelection(false);
				button.setAlignment(SWT.CENTER);
				button.setEnabled(false);
				return button;
			}

			@Override
			public void updateControl(Control control, Object value) {
				if( green == null){
					green = control.getDisplay().getSystemColor(SWT.COLOR_GREEN);
					yellow = control.getDisplay().getSystemColor(SWT.COLOR_YELLOW);
				}
				boolean complete = ((Double) value).intValue()>10;
				Button button = (Button)control;
				button.setSelection( complete);
				button.setBackground(complete? green : yellow);
			}
		};
		column3.setLabelProvider(new ObservableMapCellControlProvider(values, factory2, "Column3"));


		TableViewerColumn column4 = new TableViewerColumn(viewer, SWT.NONE);
		column4.getColumn().setText("Done");
		column4.getColumn().setWidth(20);
		column4.setLabelProvider(new ObservableMapOwnerDrawProvider(values){
			org.eclipse.swt.graphics.Color green = null;
			org.eclipse.swt.graphics.Color yellow = null;
			org.eclipse.swt.graphics.Color original = null;

			@Override
			protected void measure(Event event, Object element) {
				event.setBounds(new Rectangle(event.x, event.y, 20 , 10));
			}

			@Override
			protected void erase(Event event, Object element) {
				if( original != null){
					event.gc.setBackground(original);
					event.gc.fillRectangle(event.getBounds());
				}
				super.erase(event, element);
			}

			@Override
			protected void paint(Event event, Object element) {
				if( green == null){
					original=event.gc.getBackground();
					green = event.display.getSystemColor(SWT.COLOR_GREEN);
					yellow = event.display.getSystemColor(SWT.COLOR_YELLOW);
				}
				Object value = attributeMaps[0].get(element);
				boolean complete = ((Double) value).intValue()>10;
				event.gc.setBackground(complete? green : yellow);
				event.gc.fillRectangle(event.getBounds());
			}});


		viewer.getTable().setHeaderVisible(true);
		viewer.setContentProvider(contentProvider);
		viewer.setInput(input);
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
