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

package uk.ac.gda.exafs.ui.views.amplifier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.currentamplifier.StanfordAmplifier;
import gda.factory.Finder;

public class StanfordAmplifierComposite{

	private static final Logger logger = LoggerFactory.getLogger(StanfordAmplifierComposite.class);

	private Combo sensitivityValueCombo;
	private Combo offsetValueCombo;
	private Combo offsetUnitCombo;
	private Combo sensitivityUnitCombo;
	private Button on;
	private Button off;

	private final StanfordAmplifier stanfordScannable;

	public StanfordAmplifierComposite(Composite parent, @SuppressWarnings("unused") int style, String name, String scannable) {

		stanfordScannable = (StanfordAmplifier)Finder.getInstance().find(scannable);

		Group group = new Group(parent, SWT.NONE);
		group.setText(name);
		group.setLayout(new GridLayout(5, false));
		group.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));

		Label lblSensitivity = new Label(group, SWT.NONE);
		lblSensitivity.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSensitivity.setText("Sensitivity");

		sensitivityValueCombo = new Combo(group, SWT.NONE);
		sensitivityValueCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					stanfordScannable.setSensitivity(sensitivityValueCombo.getSelectionIndex());
				} catch (DeviceException e1) {
					logger.error("Problem setting sensitivity", e1);
				}
			}
		});

		sensitivityValueCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		sensitivityValueCombo.setItems(stanfordScannable.getAllowedPositions());
		((GridData)sensitivityValueCombo.getLayoutData()).minimumWidth = 75;

		sensitivityUnitCombo = new Combo(group, SWT.NONE);
		sensitivityUnitCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					stanfordScannable.setSensitivityUnit(sensitivityUnitCombo.getSelectionIndex());
				} catch (DeviceException e1) {
					logger.error("Problem setting units", e1);
				}
			}
		});
		sensitivityUnitCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		sensitivityUnitCombo.setItems(stanfordScannable.getGainUnits());
		((GridData)sensitivityUnitCombo.getLayoutData()).minimumWidth = 75;

		Button update = new Button(group, SWT.NONE);
		update.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		update.setText("Update Values");
		update.setToolTipText("Update settings to match current hardware status");

		Label lblInputOffsetCurrent = new Label(group, SWT.NONE);
		lblInputOffsetCurrent.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblInputOffsetCurrent.setText("Input Offset");

		offsetValueCombo = new Combo(group, SWT.NONE);
		offsetValueCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					stanfordScannable.setOffset(offsetValueCombo.getSelectionIndex());
				} catch (DeviceException e1) {
					logger.error("Problem setting offset", e1);
				}
			}
		});
		offsetValueCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		offsetValueCombo.setItems(stanfordScannable.getAllowedPositions());

		offsetUnitCombo = new Combo(group, SWT.NONE);
		offsetUnitCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					stanfordScannable.setOffsetUnit(sensitivityUnitCombo.getSelectionIndex());
				} catch (DeviceException e1) {
					logger.error("Problem setting offset unit", e1);
				}
			}
		});
		offsetUnitCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		offsetUnitCombo.setItems(stanfordScannable.getOffsetUnits());

		on = new Button(group, SWT.NONE);
		on.setSelection(true);
		on.setText("On");

		off = new Button(group, SWT.NONE);
		off.setText("Off");

		on.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					switchCurrentOn(true);
				} catch(DeviceException e1) {
					logger.error("Problem switching on current ", e1);
				}
			}
		});

		off.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					switchCurrentOn(false);
				} catch (DeviceException e1) {
					logger.error("Problem switching off current ", e1);
				}
			}
		});

		update.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateFields();
			}
		});

		updateFields();
	}

	/**
	 * Turn ion current on/off and also update gui on/off button enabled status.
	 * @param switchOn
	 * @throws DeviceException
	 */
	private void switchCurrentOn(boolean switchOn) throws DeviceException {
		stanfordScannable.setOffsetCurrentOn(switchOn);
		// Update gui on/off buttons
		boolean offsetOn = stanfordScannable.isOffsetCurrentOn();
		on.setEnabled(!offsetOn);
		off.setEnabled(offsetOn);
	}

	private void updateFields(){
		try {
			sensitivityValueCombo.select(stanfordScannable.getSensitivity());
			offsetValueCombo.select(stanfordScannable.getOffset());
			offsetUnitCombo.select(stanfordScannable.getOffsetUnit());
			sensitivityUnitCombo.select(stanfordScannable.getSensitivityUnit());
			switchCurrentOn( stanfordScannable.isOffsetCurrentOn() );
		} catch (DeviceException e) {
			logger.error("Problem updating gui from amplifier hardware settings", e);
		}
	}
}
