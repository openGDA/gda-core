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

package uk.ac.gda.richbeans.components.wrappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import swing2swt.layout.BorderLayout;
import uk.ac.gda.richbeans.beans.IFieldWidget;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.event.ValueEvent;

/**
 * Class designed to wrap combo and allow BeanUI to talk to the class
 * to build up XML files from the values and link them back to the bean.
 * 
 * You have to be a widget (even though not needed) so that RCP developer
 * can deal with using the class. Therefore in inherits from Composite.
 */
public class PrintfWrapper  extends FieldComposite implements IFieldWidget{

	private static final String [] DEFAULT_FORMATS = {"10.7f", "5.2f", "6.4f", "10.7g", "5.2g", "6.4g"};
	
	protected final Combo combo;
	protected final Label formatLabel;
	
	protected AutoCompleteField autoComplete;

	private SelectionAdapter selectionListener;

	private ModifyListener modifyListener;

	private SelectionAdapter oneListener;

	private SelectionAdapter hundredListener;

	private SelectionAdapter ninesListener;

	private MenuItem oneItem;

	private MenuItem hundredItem;

	private MenuItem ninesItem;
	
	/**
	 * Construct combo.
	 * @param parent
	 * @param style
	 */
	public PrintfWrapper(final Composite parent, int style) {
		super(parent, SWT.NONE);
		setLayout(new BorderLayout());
		
		combo = new Combo(this, style);
		combo.setLayoutData(BorderLayout.CENTER);
		mainControl = combo;
		
		this.selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final ValueEvent evt = new ValueEvent(combo,getFieldName());
				evt.setDoubleValue(combo.getSelectionIndex());
				evt.setValue(getValue());
				eventDelegate.notifyValueListeners(evt);
			}
		};
		combo.addSelectionListener(selectionListener);
		
	    this.modifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				final String txt = combo.getText();
				addDrop(txt);
				final ValueEvent evt = new ValueEvent(combo,getFieldName());
				evt.setDoubleValue(combo.getSelectionIndex());
				evt.setValue(getValue());
				eventDelegate.notifyValueListeners(evt);
			}
		};
		combo.addModifyListener(modifyListener);
		
		formatLabel = new Label(this, SWT.NONE);
		formatLabel.setAlignment(SWT.CENTER);
		formatLabel.setLayoutData(BorderLayout.EAST);
		formatLabel.setText("        ");
		formatLabel.setToolTipText("This is "+templateFormatNumber+" formatted with the current format. Right click to change the test number.");

		final Menu menu = new Menu(formatLabel);
		formatLabel.setMenu(menu);

		this.oneItem = new MenuItem(menu, SWT.NONE);
		oneItem.setText("Format '1.0'");
		this.oneListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				templateFormatNumber = 1.0;
				updateFormat(getValue()+"");
			}			
		};
		oneItem.addSelectionListener(oneListener);

		this.hundredItem = new MenuItem(menu, SWT.NONE);
		hundredItem.setText("Format '100.0001'");
		this.hundredListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				templateFormatNumber = 100.0001;
				updateFormat(getValue()+"");
			}			
		};
		hundredItem.addSelectionListener(hundredListener);

		this.ninesItem = new MenuItem(menu, SWT.NONE);
		ninesItem.setText("Format '99999'");
		this.ninesListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				templateFormatNumber = 99999d;
				updateFormat(getValue()+"");
			}			
		};
		ninesItem.addSelectionListener(ninesListener);

	
		setItems(DEFAULT_FORMATS);
	}
	
	@Override
	public void dispose() {
		combo.removeSelectionListener(selectionListener);
		combo.removeModifyListener(modifyListener);
		oneItem.removeSelectionListener(oneListener);
		hundredItem.removeSelectionListener(hundredListener);
		ninesItem.removeSelectionListener(ninesListener);
		super.dispose();
	}
	
	private Double templateFormatNumber = 1.0;
	
	private void addDrop(final String txt) {
		
		if (!updateFormat(txt)) return;
		final List<String> itemList = new ArrayList<String>(11);
		itemList.addAll(Arrays.asList(combo.getItems()));
		if (!itemList.contains(txt)) {
			itemList.add(0,txt);
			setItems(itemList.toArray(new String[itemList.size()]));
			combo.setText(txt);
		}						
	}
	
	private boolean updateFormat(final String txt) {
		try {
			final String form   = txt.startsWith("%") ? txt : "%"+txt;
			final String format = String.format(form, templateFormatNumber);
			formatLabel.setText("   "+format+"   ");
			layout();
			return true;
		} catch (Exception ne) {
			return false;
		}
	}
	
	/**
	 * @param items
	 */
	public void setItems(final String [] items) {
		combo.setItems(items);
		// Auto-complete in Jface not working well.
		if (autoComplete==null) autoComplete = new AutoCompleteField(combo, new ComboContentAdapter(), items) ;
		autoComplete.setProposals(items);
	}

	@Override
	public String getValue() {
		final String text = combo.getText();
		final int index   = combo.getSelectionIndex();
		if (index<0&&text!=null) {
			return text;
		} else if (index>-1) {
			return combo.getItem(index);
		}
		return null;
	}
	
	@Override
	public void setValue(Object value) {
		if (value==null) {
			combo.clearSelection();
			if (combo.getSelectionIndex() != -1) {
				throw new IllegalArgumentException("Attempted to clearSelection of Read Only PrintfWrapper " + getFieldName());
			}
			return;
		}
		
		final String textValue = value.toString();
		final int index        = combo.indexOf(textValue);
		if (index<0) {
			combo.setText(textValue);
			addDrop(textValue);
		} else {
			combo.select(index);
		}
	}

	/*******************************************************************/
	/**        This section will be the same for many wrappers.       **/
	/*******************************************************************/
	/**
	 * @param active the active to set
	 */
	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		combo.setVisible(active);
	}

	/**
	 * @param i
	 */
	public void setTextLimit(int i) {
		combo.setTextLimit(i);
	}

	/**
	 * @param indexOf
	 */
	public void select(int indexOf) {
		combo.select(indexOf);
	}

	/**
	 * @param systemColor
	 */
	@Override
	public void setForeground(Color systemColor) {
		if (systemColor.isDisposed()) return;
		combo.setForeground(systemColor);
	}

	/**
	 * @return i
	 */
	public int getSelectionIndex() {
		return combo.getSelectionIndex();
	}

	/**
	 * @param index
	 * @return i
	 */
	public String getItem(int index) {
		return combo.getItem(index);
	}

	/**
	 * 
	 * @return items
	 */
	public String[] getItems() {
		return combo.getItems();
	}
	
	/*******************************************************************/

}

	