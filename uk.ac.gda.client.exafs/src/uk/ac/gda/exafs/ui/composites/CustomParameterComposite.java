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

package uk.ac.gda.exafs.ui.composites;

import gda.device.Scannable;
import gda.factory.Finder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.components.wrappers.FindableNameWrapper;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.TextWrapper;

/**
 * @author fcp94556
 *
 */
public class CustomParameterComposite extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(CustomParameterComposite.class);
	
	private TextWrapper deviceName;
	private ScaleBox value;

	private SelectionAdapter selectionListener;

	private Link valueLabel;
	/**
	 * @param parent
	 * @param style
	 */
	public CustomParameterComposite(Composite parent, int style) {
		super(parent, style);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);
		setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		final Label deviceNameLabel = new Label(this, SWT.NONE);
		deviceNameLabel.setText("Device Name");

		deviceName = new FindableNameWrapper(this, SWT.BORDER, Scannable.class);
		deviceName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		this.valueLabel = new Link(this, SWT.NONE);
		valueLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		valueLabel.setText("<a>Value</a>");
		valueLabel.setToolTipText("Gets the value of the device named in device name and sets the value in this box.");
		this.selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final String name = deviceName.getValue() != null ? deviceName.getValue().toString() : null;
				if (name == null)  return;
				
				final Scannable scannable = Finder.getInstance().find(name);
				try {
					value.setValue(scannable.getPosition());
				} catch (Exception e1) {
					logger.error("Cannot retrieve value for '"+name+"'", e1);
					value.setValue(null);
				}
			}
		};
		valueLabel.addSelectionListener(selectionListener);

		value = new ScaleBox(this, SWT.NONE);
		value.setMaximum(Integer.MAX_VALUE);
		value.setMinimum(-Integer.MAX_VALUE);
		value.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}
	
	@Override
	public void dispose() {
		valueLabel.removeSelectionListener(selectionListener);
		super.dispose();
	}
	
	/**
	 * @return ScaleBox
	 */
	public ScaleBox getValue() {
		return value;
	}
	/**
	 * @return TextWrapper
	 */
	public TextWrapper getDeviceName() {
		return deviceName;
	}

}

	