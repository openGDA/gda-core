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
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import uk.ac.gda.client.composites.MotorPositionEditorControl;
import uk.ac.gda.ui.components.NumberEditorControl;


/**
 * Example view in a model view controller framework.
 * The model is provided by the OSGI service obtained by GDAClientActivator.getNamedService
 * The model is linked to the UI controls by Databinding.
 * Note that the mode provide property change support and so we use the BeanProperties factory
 */
public class MvcExampleView extends ViewPart {
//	private static final Logger logger = LoggerFactory.getLogger(MvcExampleView.class);

	public static final String ID = "uk.ac.gda.example.mvcexample.MvcExampleView"; //$NON-NLS-1$
	private FormToolkit toolkit;
	DataBindingContext bindingContext;

	protected MotorPositionEditorControl motorPosControl;

	protected NumberEditorControl numberControl;

	protected Button btn1;
	private MvcExampleModel model;
	public MvcExampleView() {
	}



	/**
	 * Create contents of the view part.
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
			numberControl = new NumberEditorControl(cmpRoot, SWT.NONE, model, MvcExampleModel.POSITION_PROPERTY_NAME, false);
			numberControl.setRange(0, 100);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(numberControl);
			toolkit.adapt(numberControl);
		} catch (Exception e) {
			throw new RuntimeException("Error adding numberControl to UI", e);
		}

		try {
			motorPosControl = new MotorPositionEditorControl(cmpRoot, SWT.NONE, model.getScannableWrapper(), false);
			motorPosControl.setRange(0, 100);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(motorPosControl);
			toolkit.adapt(motorPosControl);
		} catch (Exception e) {
			throw new RuntimeException("Error adding motorPosControl to UI", e);
		}

		
		createActions();
		initializeToolBar();
		initializeMenu();
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
//		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
//		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
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
