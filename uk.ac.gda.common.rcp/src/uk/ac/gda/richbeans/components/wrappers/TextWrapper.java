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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.nfunk.jep.JEP;

import swing2swt.layout.BorderLayout;
import uk.ac.gda.richbeans.components.ButtonComposite;
import uk.ac.gda.richbeans.event.ValueEvent;

import com.swtdesigner.SWTResourceManager;

/**
 * Designed to wrap Text objects to allow then to work with BeanUI
 * @author fcp94556
 *
 */
public class TextWrapper extends ButtonComposite {
	
	protected static final Color BLUE   = SWTResourceManager.getColor(SWT.COLOR_BLUE);
	protected static final Color RED    = SWTResourceManager.getColor(SWT.COLOR_RED);
	protected static final Color BLACK  = SWTResourceManager.getColor(SWT.COLOR_BLACK);
	protected static final Color DARK_RED  = SWTResourceManager.getColor(SWT.COLOR_DARK_RED);
	
	private JEP jepParser;

	/**
	 * The text type, effects how the text is checked.
	 */
	public enum TEXT_TYPE {
		/**
		 * Any text
		 */
		FREE_TXT,
		/**
		 * Legal expressions
		 */
		EXPRESSION,
		/**
		 * Legal Linux filenames
		 */
		FILENAME
	}
	
	private TEXT_TYPE textType = TEXT_TYPE.FREE_TXT;
	
	/**
	 * @return Returns the textType.
	 */
	public TEXT_TYPE getTextType() {
		return textType;
	}

	/**
	 * @param textType The textType to set.
	 */
	public void setTextType(TEXT_TYPE textType) {
		this.textType = textType;
	}

	protected StyledText text;
	private ModifyListener modifyListener;

	/**
	 * The variables to use in expression validation.
	 */
	private Map<String, Object> expressionVariables;

	/**
	 * Simply calls super and adds some listeners.
	 * @param parent
	 * @param style
	 */
	public TextWrapper(Composite parent, int style) {
		
		super(parent, SWT.NONE);
//		GridLayoutFactory.fillDefaults().applyTo(this);
		setLayout(new BorderLayout());
		
		this.text = new StyledText(this, style);
//		GridDataFactory.fillDefaults().applyTo(text);
		text.setLayoutData(BorderLayout.CENTER);
		mainControl = text;
		
		this.modifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				
				final Object newValue = getValue();
				
				if (textType==TEXT_TYPE.EXPRESSION) {
					if (jepParser==null) {
						jepParser = new JEP();
						jepParser.addStandardFunctions();
						jepParser.addStandardConstants();
						jepParser.setAllowUndeclared(true);
						jepParser.setImplicitMul(true);
					}
					try {
					    jepParser.parse(newValue+"");
					    text.setToolTipText("Expression has valid syntax. Variables have not been checked for existence.");
					    
					    if (expressionVariables!=null && !expressionVariables.isEmpty()) {
					    	for (String varName : expressionVariables.keySet()) {
					    		jepParser.addVariable(varName, expressionVariables.get(varName));
							}
					    	jepParser.parseExpression(newValue+"");
					    	text.setToolTipText("Expression value '"+jepParser.getValue()+"'");
					    }
					    
					    if(!BLUE.isDisposed()) text.setForeground(BLUE);
					    
					} catch (Throwable ne) {
						if(!RED.isDisposed()) text.setForeground(RED);
					    text.setToolTipText("Expression has invalid syntax");
					}
					
				} else if (textType == TEXT_TYPE.FILENAME) {
					jepParser = null;
					String testString = newValue.toString().trim();
					if (testString.contains(" ") || testString.startsWith("-")
							|| testString.contains(";") || testString.contains("<") || testString.contains("\t")
							|| testString.contains("'") || testString.contains("\"") || testString.contains("\\")
							|| testString.contains("\n")|| testString.contains("..")) {
						if (!RED.isDisposed()) {
							text.setForeground(RED);
						}
						text.setToolTipText("Expression has invalid syntax");

					} else {
						text.setToolTipText("Enter a valid filename. Do NOT use spaces, commas, backslash etc.");
						if (!BLACK.isDisposed()) {
							text.setForeground(BLACK);
						}
					}
				}

				final ValueEvent evt = new ValueEvent(text,getFieldName());
				evt.setValue(newValue);
				eventDelegate.notifyValueListeners(evt);
			}
		};
		text.addModifyListener(modifyListener);

	}
	
	@Override
	public void setToolTipText(String text) {
		this.text.setToolTipText(text);
	}
	
	@Override
	public void dispose() {
		if (text!=null&&!text.isDisposed()) text.removeModifyListener(modifyListener);
		super.dispose();
	}

	private boolean multiLineMode = false;
	
	@Override
	public Object getValue() {
		if (multiLineMode) {
			final String [] sa = getText().split(text.getLineDelimiter());
			return Arrays.asList(sa);
		}
		
	    return getText();
	}
	
	/**
	 * @return text
	 */
	public String getText() {
		if (text.isDisposed()) {
			return null;
		}
		return text.getText();
	}

	@Override
	public void setValue(Object value) {
		if (isDisposed()) return;
		if (value instanceof List<?>) {
			multiLineMode = true;
			final List<?> lines = (List<?>)value;
			final StringBuilder buf  = new StringBuilder();
			for (Object line : lines) {
				buf.append(line.toString());
				buf.append(text.getLineDelimiter());
			}
			text.setText(buf.toString());
		} else {
			multiLineMode = false;
			text.setText(value!=null?value.toString():"");
		}
	}
	/*******************************************************************/
	/**        This section will be the same for many wrappers.       **/
	/*******************************************************************/
	@Override
	protected void checkSubclass () {
	}

	/**
	 * @param active the active to set
	 */
	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		setVisible(active);
	}

	/**
	 * @param i
	 */
	public void setTextLimit(int i) {
		text.setTextLimit(i);
	}

	/**
	 * If you have a variable set with values which the box in expression
	 * mode should check, send them here. Otherwise the expression box simply
	 * checks legal syntax.
	 * 
	 * Variables are Jep ones therefore Strings or Numbers
	 * 
	 * @param vars
	 */
	public void setExpressionVariables(final Map<String, Object> vars) {
		this.expressionVariables = vars;
	}
	
	/*******************************************************************/

}

	