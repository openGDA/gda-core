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

package org.myls.scan;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.components.FieldComposite.NOTIFY_TYPE;
import uk.ac.gda.richbeans.components.scalebox.*;
import uk.ac.gda.richbeans.components.wrappers.*;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;

/**
 *
 */
public final class SimpleScanParametersComposite extends Composite {

	private FieldComposite name;
	private ScaleBox start;
	private FieldComposite end;
	private FieldComposite seconds;
	BooleanWrapper onoff;
	LabelWrapper labelWrapper;
	ComboWrapper option;

	public SimpleScanParametersComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		Label label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("name");
		this.name = new TextWrapper(this, SWT.NONE);
		name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("start");
		this.start = new ScaleBox(this, SWT.NONE);
		start.setMinimum(0.0);
		start.setMaximum(100.0);
		start.setUnit("eV");
		start.setDecimalPlaces(2);
		start.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("end");
		this.end = new ScaleBox(this, SWT.NONE);
		end.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("seconds");
		this.seconds = new SpinnerWrapper(this, SWT.NONE);
		seconds.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("On");
		onoff = new BooleanWrapper(this, SWT.NONE);
		onoff.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("pressure");
		labelWrapper = new LabelWrapper(this, SWT.NONE);
		labelWrapper.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		labelWrapper.setUnit("bar");
		labelWrapper.setNotifyType(NOTIFY_TYPE.VALUE_CHANGED);
		labelWrapper.setDecimalPlaces(6);
		labelWrapper.setValue(999.9);
		

		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("option");
		option = new ComboWrapper(this, SWT.DROP_DOWN);
		option.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		option.setNotifyType(NOTIFY_TYPE.ALWAYS);
		option.setItems(new String[] { "one", "two", "three" });
		option.select(0);
		option.addValueListener(new ValueAdapter("option") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				int choice = ((Combo) e.getSource()).getSelectionIndex();
				switch (choice) {
				case 0:
					labelWrapper.setValue(9.9);
					break;
				case 1:
					labelWrapper.setValue(99.9);
					break;
				case 2:
					labelWrapper.setValue(999.9);
					break;
				}
				layout();
			}
		});
		
		
	}

	public FieldComposite getName() {
		return name;
	}

	public FieldComposite getStart() {
		return start;
	}

	public FieldComposite getEnd() {
		return end;
	}

	public FieldComposite getSeconds() {
		return seconds;
	}

	public static void main(String[] args) {
		Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setSize(300, 300);
		shell.setLayout(new RowLayout());

		shell.setText("Composite Example");

		new SimpleScanParametersComposite(shell, SWT.NONE);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
