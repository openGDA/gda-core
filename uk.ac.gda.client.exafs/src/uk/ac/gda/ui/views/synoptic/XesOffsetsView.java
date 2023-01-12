/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.views.synoptic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.IScannableMotor;
import gda.device.scannable.scannablegroup.IScannableGroup;
import gda.exafs.xes.IXesOffsets;
import gda.factory.Finder;
import uk.ac.gda.client.livecontrol.LiveControlBase;

public class XesOffsetsView extends LiveControlBase {

	private static final Logger logger = LoggerFactory.getLogger(XesOffsetsView.class);

	private String offsetObjectName = "";

	private IScannableGroup spectrometerGroup;
	private List<IScannableMotor> spectrometerMotors;
	private Map<IScannableMotor, Text> textBoxes;
	private String numberFormat = "%.5g";

	private String xesEnergyScannableName;

	public void setOffsetObjectName(String offsetObjectName) {
		this.offsetObjectName = offsetObjectName;
	}

	private void setupScannables() {
		IXesOffsets offsets = Finder.find(offsetObjectName);
		if (offsets == null) {
			logger.warn("Could not create XesOffsetsView - no XesOffsets object called {} was found", offsetObjectName);
			return;
		}

		logger.debug("Setting spectrometer scannables from {} object", offsets.getName());

		// Name of underlying XESEnergyScannable used by XesOffset object
		xesEnergyScannableName = offsets.getXesEnergyScannableName();

		// get the spectrometer group and make list of spectrometer scannables
		spectrometerGroup = Finder.find(offsets.getSpectrometerGroupName());
		spectrometerMotors = new ArrayList<>();
		Map<String, IScannableMotor> motorObjectMap = Finder.getFindablesOfType(IScannableMotor.class);
		for(String name : spectrometerGroup.getGroupMemberNames()) {
			if (motorObjectMap.containsKey(name)) {
				spectrometerMotors.add(motorObjectMap.get(name));
			} else {
				logger.debug("Not adding {} to XesOffsetsView - it is not an IScannableMotor", name);
			}
		}
	}

	@Override
	public void createControl(Composite parent) {
		setupScannables();
		if (spectrometerGroup == null) {
			MessageDialog.openWarning(parent.getShell(), "Cannot open Offsets view", "Cannot open offsets view - required XesOffset object not found on server.");
			return;
		}

		setupGui(parent);
		// Update the GUI when the offsets change (event fired by XesOffset object)
		spectrometerGroup.addIObserver( (source,arg) -> {
			if (arg.toString().equals(IXesOffsets.OFFSET_UPDATE_EVENT)) {
				updateGui();
			}
		});
	}

	private Label addLabel(Composite parent, String labelText) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(labelText);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		return label;
	}

	private Text addTextBox(Composite parent, int width) {
		Text textbox = new Text(parent, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = width;
		textbox.setLayoutData(gridData);
		textbox.setEditable(false);
		return textbox;
	}

	private void setupGui(Composite parent) {
		final int offsetTextboxWidth = 100;

		Group comp = new Group(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		comp.setText("Motor offsets for "+xesEnergyScannableName);
		textBoxes = new HashMap<>();
		for(IScannableMotor scn : spectrometerMotors) {
			addLabel(comp, scn.getName());
			Text textBox = addTextBox(comp, offsetTextboxWidth);
			textBoxes.put(scn, textBox);
		}
		updateOffsetTextboxes();

		// Set the parent background to match these widgets
		parent.getParent().setBackground(comp.getBackground());

	}

	private synchronized void updateGui() {
		Display.getDefault().asyncExec(this::updateOffsetTextboxes);
	}

	private Double getOffset(IScannableMotor scnMotor) {
		return scnMotor.getOffset() == null ? 0.0 : scnMotor.getOffset()[0];
	}

	/**
	 * Update the text boxes with the current offset values from the motors.
	 */
	private void updateOffsetTextboxes() {
		logger.debug("Updating offset text boxes");
		textBoxes.entrySet()
		.stream()
		.forEach( entry -> {
			Double offset = getOffset(entry.getKey());
			Text textbox = entry.getValue();
			textbox.setText(String.format(numberFormat, offset));
		});
	}

}
