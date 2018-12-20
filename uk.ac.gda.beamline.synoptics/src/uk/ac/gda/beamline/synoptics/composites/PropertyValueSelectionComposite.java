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

package uk.ac.gda.beamline.synoptics.composites;

import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObserver;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beamline.synoptics.utils.ListenableProperty;
/**
 * reusable composite that display a label on the left and a drop down list on the right.
 * This interface allows user to change the value of a java property dynamically at runtime.
 * The change is enforce on both server and client.
 *   
 * @author fy65
 *
 */
public class PropertyValueSelectionComposite extends Composite implements PropertyChangeListener, IObserver {

	private static final Logger logger = LoggerFactory.getLogger(PropertyValueSelectionComposite.class);
	private ComboViewer viewer;
	private String[] list;
	private ListenableProperty property;
	private Scriptcontroller eventAdmin;
	private String eventAdminName;
	private IStructuredSelection currentSelection;

	public PropertyValueSelectionComposite(Composite parent, int style, String label, ListenableProperty property2, String[] strings, String eventAdminName) {
		super(parent, style);
		property=property2;
		list=strings;
		this.eventAdminName=eventAdminName;
		
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);
		GridDataFactory.fillDefaults().applyTo(this);		
		
		Label lbl = new Label(this, SWT.NONE | SWT.CENTER);
		lbl.setText(label);
		
		viewer= new ComboViewer(parent, SWT.READ_ONLY);
		// the ArrayContentProvider  object does not store any state, 
		// therefore you can re-use instances
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof String) {
					return (String) element;
				}
				return super.getText(element);
			}
		});
		viewer.setInput(strings);
		// react to the selection change of the viewer
		// note that the viewer returns the actual object 
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			//implement property change by this viewer
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.size() > 0 && selection != currentSelection) {
					String firstElement = (String) selection.getFirstElement();
					if (property.getPropertyName().equals(LocalProperties.GDA_DATAWRITER_DIR)) {
						File file = new File(firstElement);
						if (file.exists() && file.isDirectory()) {
							property.set(firstElement);
							currentSelection=selection;
							InterfaceProvider.getTerminalPrinter().print("Data Directory changed to "+LocalProperties.get(LocalProperties.GDA_DATAWRITER_DIR));
							logger.info("Data directory changed to {}", LocalProperties.get(LocalProperties.GDA_DATAWRITER_DIR));
						} else {
							InterfaceProvider.getTerminalPrinter().print(firstElement+" is not a data directory.");
							logger.info("{} is not a data directory.", firstElement);
						}
					} else {
						property.set(firstElement);
						currentSelection=selection;
						InterfaceProvider.getTerminalPrinter().print("Property "+property.getPropertyName()+" changed to "+LocalProperties.get(property.getPropertyName()));
						logger.info("Property {} changed to {}", property.getPropertyName(),LocalProperties.get(property.getPropertyName()));
					}
				}
			}
		});
	}

	public void initialisation() {
		if (eventAdminName!=null) {
			eventAdmin=Finder.getInstance().find(eventAdminName);
		}
		property.addPropertyChangeListener(this);
		if (eventAdmin!=null) {
			eventAdmin.addIObserver(this);
		}
		if (property.getPropertyName().equals(LocalProperties.GDA_DATAWRITER_DIR)) {
			String currentPath = PathConstructor.createFromProperty(LocalProperties.GDA_DATAWRITER_DIR);
			viewer.setSelection(new StructuredSelection(currentPath));
		} else {
			viewer.setSelection(new StructuredSelection(LocalProperties.get(property.getPropertyName())));
		}
		currentSelection=(IStructuredSelection) (viewer.getSelection());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		//implement property changed by other client component 
		if (evt.getSource()!=this && evt != null) {
			if (evt.getPropertyName().equals(property.getPropertyName())){
				for (String item : list) {
					if (item.equals(evt.getNewValue().toString())) {
						logger.debug("Update property {} value to {} by component {}", evt.getPropertyName(), evt.getNewValue(),evt.getSource());
						viewer.setSelection(new StructuredSelection(evt.getNewValue()));
						currentSelection=(IStructuredSelection) viewer.getSelection();
					}
				}
			}
		}
	}

	@Override
	public void dispose() {
		property.removePropertyChangeListener(this);
		if (eventAdmin!=null) {
			eventAdmin.deleteIObserver(this);
		}
		super.dispose();
	}
	/**
	 * handling distributed event - i.e property change on the server side.
	 */
	@Override
	public void update(Object source, Object arg) {
		//implement property changed by a server component
		if (source instanceof String && ((String)source).equals(property.getPropertyName())) {
			logger.debug("Update property {} value to {} by the server", source, arg);
			viewer.setSelection(new StructuredSelection(arg));
			currentSelection=(IStructuredSelection) viewer.getSelection();
		}
	}
}
