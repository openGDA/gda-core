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

package uk.ac.gda.richbeans.components.scalebox;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import swing2swt.layout.BorderLayout;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;
import uk.ac.gda.richbeans.event.ValueListener;
import uk.ac.gda.ui.utils.SWTUtils;

/**
 * Standard box has the following event properties:
 * 1. Bounds are checked whenever a value is changed.
 * 2. ValueListeners are notified when the enter key is pressed or when focus is lost.
 */
public class StandardBox extends NumberBox {
	
	public StandardBox(Composite parent, int style) {
		super(parent, style);
	}
	
	
//	addSelectionListerner(ISelectionListener listener)
	
	/**
	 * Change listeners to only notify if focus lost or enter pressed.
	 */
	@Override
	protected void createTextListeners(final StyledText text) {
		createFocusListener(text);
		createModifyListener(text);
		createVerifyKeyListener(text);
	}
	
	@Override
	protected void createVerifyKeyListener(StyledText text) {
		
		verifyListener = new VerifyKeyListener() {	
			@Override
			public void verifyKey(VerifyEvent event) {
				if (event.character == SWT.CR) {// They hit enter
					textUpdateAndFireListeners();
				}
			}
		};
		text.addVerifyKeyListener(verifyListener);
	}

	@Override
	protected void createModifyListener(final StyledText text) {
	    this.modifyListener = new ModifyListener()  {
			@Override
			public void modifyText(final ModifyEvent e) {
				textUpdate(); // Checks the entry for bounds (and unit)
				              // Do not fire events.
			} 	
	    };
	    text.addModifyListener(modifyListener);
	}
	
	/**
	 * Change loss of focus to fire value.
	 */
	@Override
	protected void createFocusListener(final StyledText text) {
		this.focusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				textUpdateAndFireListeners();
			}
	    };
	    text.addFocusListener(focusListener);
	}
	
	/**
	 * Testing use only.
	 * @param args
	 */
	public static void main(String... args) {
		
		Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setLayout(new BorderLayout());
		
		final Composite comp = new Composite(shell,SWT.NONE);
		comp.setLayoutData(BorderLayout.NORTH);
		
		comp.setLayout(new GridLayout(1,false));
		

		final StandardBox x = new StandardBox(comp, SWT.NONE);
		x.setUnit("micron");
		x.setLabel("x");
		x.setLabelWidth(20);
		x.setValue(1);
		x.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		final StandardBox y = new StandardBox(comp, SWT.NONE);
		y.setUnit("micron");
		y.setLabel("y");
		y.setLabelWidth(20);
		y.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		y.setValue(2);

		final StandardBox z = new StandardBox(comp, SWT.NONE);
		z.setUnit("micron");
		z.setLabel("x");
		z.setLabelWidth(20);
		z.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		z.setValue(2);
		
		final Label value = new Label(comp, SWT.NONE);
		value.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		
		final ValueListener l = new ValueAdapter("test") {		
			@Override
			public void valueChangePerformed(ValueEvent e) {
				final StringBuilder buf = new StringBuilder();
				addValue("x", x, buf);
				addValue("y", y, buf);
				addValue("z", z, buf);
				value.setText(buf.toString());
				shell.layout();
			}

			private void addValue(String label, StandardBox box, StringBuilder buf) {
				buf.append(label);
				buf.append("=");
				buf.append(box.getValue());
				buf.append(" ");
				buf.append(box.getUnit());
				buf.append("    ");
			}
		};

		x.addValueListener(l);
		y.addValueListener(l);
		z.addValueListener(l);
		
		shell.pack();
		shell.setSize(400,400);

		x.on();
		y.on();
		z.on();
		SWTUtils.showCenteredShell(shell);
	}

}
