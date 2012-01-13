/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.richbeans.components.cell;

import java.lang.reflect.Constructor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.richbeans.beans.IFieldWidget;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.event.ValueListener;


/**
 * A cell editor that uses the field widget specified by the
 * class string specified in the argument. Used in Rich Bean
 * Framework - even though references may not show up to the 
 * class - please do not delete.
 * 
 * This class enables rich bean components to be used inside
 * tables as their editor part.
 * 
 */
public class FieldComponentCellEditor extends CellEditor {

	private static Logger logger = LoggerFactory.getLogger(FieldComponentCellEditor.class);
	
	private FieldComposite                  fieldWidget;
	private Class<? extends FieldComposite> fieldWidgetClass;
	private int                             widgetStyle;
           
	/**
	 * Loads the fieldWidgetClass using the common.rcp classloader or fails straight away.
	 * 
	 * @param viewer
	 * @throws ClassNotFoundException 
	 */
	public FieldComponentCellEditor(final TableViewer viewer,
			                        final String      fieldWidgetClass) throws ClassNotFoundException {
		this(viewer.getTable(), fieldWidgetClass);
	}
	
	public FieldComponentCellEditor(final Composite  viewerControl,
                                    final String     fieldWidgetClass) throws ClassNotFoundException {
		this(viewerControl, fieldWidgetClass, SWT.NONE);
	}
	
	@SuppressWarnings("unchecked")
	public FieldComponentCellEditor(final Composite  viewerControl,
                                    final String     fieldWidgetClass,
                                    final int        style) throws ClassNotFoundException {
	
		this.fieldWidgetClass = (Class<? extends FieldComposite>)getClass().getClassLoader().loadClass(fieldWidgetClass);
	    this.widgetStyle = style;
		this.setStyle(SWT.NONE);
	    create(viewerControl);
	}

	@Override
	protected Control createControl(Composite parent) {
		
		parent.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		
		final Composite  area   = new Composite(parent, SWT.NONE);
		area.setLayout(new GridLayout(3, false));
		area.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		
		GridUtils.removeMargins(area);
		
		try {
			final Constructor<? extends FieldComposite> constructor = fieldWidgetClass.getConstructor(new Class[]{Composite.class, int.class});
			fieldWidget = constructor.newInstance(area, widgetStyle);
			fieldWidget.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			fieldWidget.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			if (fieldWidget.getLayout() instanceof GridLayout){
				GridUtils.removeMargins(fieldWidget);
			}
			
			if (fieldWidget.getControl() != null) {
				fieldWidget.getControl().addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.keyCode == 13) {
							FieldComponentCellEditor.this.applyEditorValueAndDeactivate();
						}
					}
				});
			}
			
			fieldWidget.on();
		} catch (Exception ne) {
			logger.error("Cannot load and instantiate class as FieldComposite "+fieldWidgetClass, ne);
		}
        
        return area;
	}
	

	@Override
	public void activate() {
		((Composite)this.getControl()).getParent().layout();
	}

	@Override
	public void deactivate() {
		if (fieldWidget!=null) fieldWidget.closeDialog();
        super.deactivate();
	}

	@Override
	protected Object doGetValue() {
		if (fieldWidget!=null)  return fieldWidget.getValue();
		return null;
	}


	@Override
	protected void doSetFocus() {
		if (fieldWidget!=null) fieldWidget.setFocus();
	}


	@Override
	protected void doSetValue(Object value) {
		if (fieldWidget!=null)  fieldWidget.setValue(value);
	}

	/**
	 * Applies the currently selected value and deactivates the cell editor
	 */
	void applyEditorValueAndDeactivate() {
		// must set the selection before getting value
		Object newValue = doGetValue();
		markDirty();
		boolean isValid = isCorrect(newValue);
		setValueValid(isValid);
		fireApplyEditorValue();
		deactivate();
	}

	public void addValueListener(ValueListener l) {
		fieldWidget.addValueListener(l);
	}

	public IFieldWidget getFieldWidget() {
		return fieldWidget;
	}
}
