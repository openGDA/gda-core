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
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.composites.MotorPositionEditorControl;
import uk.ac.gda.ui.components.NumberEditorControl;


/**
 * Example view in a model view controller framework.
 * The model is provided by the OSGI service obtained by GDAClientActivator.getNamedService
 * The model is linked to the UI controls by Databinding.
 * Note that the mode provide property change support and so we use the BeanProperties factory
 */
public class MvcExampleView extends ViewPart {
	private static final Logger logger = LoggerFactory.getLogger(MvcExampleView.class);

	public static final String ID = "uk.ac.gda.example.mvcexample.MvcExampleView"; //$NON-NLS-1$
	private FormToolkit toolkit;
	DataBindingContext bindingContext;
	public MvcExampleView() {
	}



	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		MvcExampleModel model = (MvcExampleModel) GDAClientActivator.getNamedService(MvcExampleModel.class, null);
		toolkit = new FormToolkit(parent.getDisplay());
		Composite cmpRoot = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout();
		cmpRoot.setLayout(layout);
		Button btn1 = toolkit.createButton(cmpRoot, "Press Me", SWT.CHECK);
		Button btn2 = toolkit.createButton(cmpRoot, "Press Me", SWT.CHECK);
		IObservableValue btn1ObservableValue = SWTObservables.observeSelection(btn1);
		IObservableValue btn2ObservableValue = SWTObservables.observeSelection(btn2);
		IObservableValue btnSelectedObserveValue1 = BeanProperties.value(MvcExampleModel.SELECTED_PROPERTY_NAME).observe(model);

		bindingContext = new DataBindingContext();
		bindingContext.bindValue(btn1ObservableValue, btnSelectedObserveValue1);
		bindingContext.bindValue(btn2ObservableValue, btnSelectedObserveValue1);
		btnSelectedObserveValue1.setValue(true);		
		
		try {
			NumberEditorControl comp1 = new NumberEditorControl(cmpRoot, SWT.NONE, model, MvcExampleModel.POSITION_PROPERTY_NAME, false);
			comp1.setRange(0, 100);
			GridDataFactory.fillDefaults().applyTo(comp1);
			toolkit.adapt(comp1);
			NumberEditorControl comp2 = new NumberEditorControl(cmpRoot, SWT.NONE, model, MvcExampleModel.POSITION_PROPERTY_NAME, false);
			comp1.setRange(0, 100);
			GridDataFactory.fillDefaults().applyTo(comp2);
			toolkit.adapt(comp2);
		} catch (Exception e) {
			logger.error("Error creating UI", e);
		}

		try {
			MotorPositionEditorControl comp1 = new MotorPositionEditorControl(cmpRoot, SWT.NONE, model.getScannableWrapper(), false);
			comp1.setRange(0, 100);
			GridDataFactory.fillDefaults().applyTo(comp1);
			toolkit.adapt(comp1);
			NumberEditorControl comp2 = new MotorPositionEditorControl(cmpRoot, SWT.NONE, model.getScannableWrapper(), false);
			comp1.setRange(0, 100);
			GridDataFactory.fillDefaults().applyTo(comp2);
			toolkit.adapt(comp2);
		} catch (Exception e) {
			logger.error("Error creating UI", e);
		}

		
		createActions();
		initializeToolBar();
		initializeMenu();
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

}
