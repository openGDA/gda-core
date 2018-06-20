/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.richbeans.widgets.shuffle.ShuffleConfiguration;
import org.eclipse.richbeans.widgets.shuffle.ShuffleViewer;
import org.eclipse.scanning.api.INameable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;

/**
 * Lets user choose a subset from their configured devices.
 */
public class ChooseDevicesDialog<M extends INameable> extends Dialog {

	private String title = "Choose from available devices";
	private List<IScanModelWrapper<M>> originalList;
	private List<IScanModelWrapper<M>> selectedList;
	private ShuffleConfiguration<String> data;
	private Map<String, IScanModelWrapper<M>> labelMap = new HashMap<>();

	/**
	 * @param parentShell
	 * @param availableDevices - all devices configured in Spring
	 * @param selectedDevices - previously selected devices; can be null
	 */
	protected ChooseDevicesDialog(Shell parentShell, List<IScanModelWrapper<M>> availableDevices, List<IScanModelWrapper<M>> selectedDevices) {
		super(parentShell);
		setShellStyle(SWT.RESIZE | SWT.APPLICATION_MODAL);
		originalList = availableDevices;
		selectedList = selectedDevices == null ? new ArrayList<>() : selectedDevices;
		originalList.forEach(model -> labelMap.put(model.getName(), model));
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
	}

	@Override
	public Control createDialogArea(Composite parent) {
		parent.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		parent.setBackgroundMode(SWT.INHERIT_FORCE);

		data = new ShuffleConfiguration<>();
		data.setFromLabel("Available");
		data.setToLabel("Selected");

		final List<String> selectedDevices = selectedList.stream()
				.map(IScanModelWrapper<M>::getName)
				.collect(Collectors.toList());

		final List<String> availableDevices = labelMap.keySet().stream()
				.filter(model -> !selectedDevices.contains(model))
				.collect(Collectors.toList());

		final ShuffleViewer<String> viewer = new ShuffleViewer<>(data);
		final Control shuffleComposite = viewer.createPartControl(parent);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(shuffleComposite);

		data.setFromList(availableDevices);
		data.setToList(selectedDevices);

		return shuffleComposite;
	}

	@Override
	public void okPressed() {
		selectedList = new ArrayList<>();
		data.getToList().forEach(device -> selectedList.add(labelMap.get(device)));

		// To avoid unexpected behaviour, do not include in scan any device that isn't selected here
		data.getFromList().forEach(device -> labelMap.get(device).setIncludeInScan(false));
		super.okPressed();
	}

	public List<IScanModelWrapper<M>> getSelectedDevices() {
		return selectedList;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
