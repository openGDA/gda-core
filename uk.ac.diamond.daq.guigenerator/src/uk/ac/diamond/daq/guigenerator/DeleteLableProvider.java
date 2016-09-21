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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteLableProvider extends CellLabelProvider {
	private final Logger logger = LoggerFactory.getLogger(DeleteLableProvider.class);

    private final Map<Object, Button> buttons = new HashMap<Object, Button>();
	private Method action;
	private Object toModify;

	public DeleteLableProvider(Method action, Object toModify) {
		this.action = action;
		this.toModify = toModify;
	}

	@Override
	public void update(ViewerCell cell) {
        TableItem item = (TableItem) cell.getItem();
        Button button;
        Object element = cell.getElement();
		if(buttons.containsKey(element))
        {
            button = buttons.get(cell.getElement());
        }
        else
        {
            button = new Button((Composite) cell.getViewerRow().getControl(), SWT.NONE);
            button.setText(action.getName());
            buttons.put(cell.getElement(), button);
            button.addSelectionListener(new SelectionAdapter() {
            	@Override
            	public void widgetSelected(SelectionEvent e) {
            		try {
						action.invoke(toModify, element);
						button.dispose();
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
						logger.error("could not apply action", ex);
					}
            	}
			});
        }
        TableEditor editor = new TableEditor(item.getParent());
        editor.grabHorizontal  = true;
        editor.grabVertical = true;
        editor.setEditor(button, item, cell.getColumnIndex());
        editor.layout();
	}
}
