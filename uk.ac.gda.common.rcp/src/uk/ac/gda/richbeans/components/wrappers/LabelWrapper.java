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

import java.text.NumberFormat;
import java.text.ParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color; 
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.event.ValueEvent;
import uk.ac.gda.ui.utils.StringUtils;

/**
 * @author fcp94556
 * 
 * You have to be a widget (even though not needed) so that RCP developer
 * can deal with using the class. Therefore in inherits from Composite.
 */
public class LabelWrapper extends FieldComposite {

	/**
	 *
	 */
	public enum TEXT_TYPE {
	/**
	 * Optional
	 */
	PLAIN_TEXT, 
	/**
	 * Default
	 */
	NUMBER_WITH_UNIT}
	

	
	private int decimalPlaces = 2;
	private Label label;
	
	public void setLabelColor(Color color){ 
 		label.setForeground(color); 
 	} 

	/**
	 * @return the decimalPlaces
	 */
	public int getDecimalPlaces() {
		return decimalPlaces;
	}
	/**
	 * @param decimalPlaces the decimalPlaces to set
	 */
	public void setDecimalPlaces(int decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
		numberFormat.setMaximumFractionDigits(decimalPlaces);
	}

	private NumberFormat numberFormat;
	/**
	 * @param parent
	 * @param style
	 */
	public LabelWrapper(final Composite parent, final int style) {
		super(parent, SWT.NONE);
		setLayout(new FillLayout());
		label = new Label(this, style);
		mainControl = label;
		this.numberFormat  = NumberFormat.getInstance();
		numberFormat.setMaximumFractionDigits(decimalPlaces);
		numberFormat.setGroupingUsed(false);
	}
	
	private String text;
	private TEXT_TYPE textType;
	@Override
	public String getValue() {
		return text;
	}
	
	/**
	 * @param type
	 */
	public void setTextType(TEXT_TYPE type) {
		this.textType = type;
	}
	
	
	@Override
	public void setValue(Object value) {
		if (textType==null||textType==TEXT_TYPE.NUMBER_WITH_UNIT) {
			if (value!=null) {
				text = numberFormat.format(value);
			} else {
				text = "";
			}
			
			String boxValue="";
			if (!"".equals(text)) {
				boxValue = StringUtils.keepDigits(text, decimalPlaces).toString();
				if (unit!=null) boxValue+=" "+unit;
			}
			label.setText(boxValue);
		} else {
			if (value!=null) {
				text = value+"";
			} else {
				text = "";
			}
			label.setText(text);
		}
		
		if (notifyType!=null&&notifyType==NOTIFY_TYPE.VALUE_CHANGED) {
			final ValueEvent evt = new ValueEvent(label,getFieldName());
			evt.setValue(value);
			eventDelegate.notifyValueListeners(evt);
		}
	}
	
	/**
	 * @return the current numeric value.
	 * @throws ParseException
	 */
	public double getNumericValue() throws ParseException {
		final Object val = getValue();
		if (val==null) return Double.NaN;
		if (val instanceof Number) return ((Number)val).doubleValue();
		return (numberFormat.parse(val.toString())).doubleValue();
	}
	
	/*******************************************************************/
	/**        This section will be the same for many wrappers.       **/
	/*******************************************************************/
	@Override
	protected void checkSubclass () {
	}
	
	private String unit;
	/**
	 * @return the unit
	 */
	public String getUnit() {
		return unit;
	}
	/**
	 * @param unit the unit to set
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}

	/**
	 * @param text
	 */
	public void setText(String text) {
		if (textType==TEXT_TYPE.NUMBER_WITH_UNIT&&getUnit()!=null) {
			label.setText(text+" "+unit);			
		} else {
			label.setText(text);
		}
	}
	
	/**
	 * Wrapper function for {@link org.eclipse.swt.widgets.Label#setAlignment(int)}
	 * @see org.eclipse.swt.widgets.Label#setAlignment(int)
	 * @param alignment LEFT, RIGHT or CENTER
	 */
	public void setAlignment(int alignment) {
		label.setAlignment(alignment);
	}

}

	
