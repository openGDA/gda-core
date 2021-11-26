/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.gasrig.ui;

import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import gda.factory.Finder;
import uk.ac.diamond.daq.gasrig.api.IGasRig;
import uk.ac.diamond.daq.gasrig.api.models.CabinetModel;
import uk.ac.diamond.daq.gasrig.api.models.GasModel;
import uk.ac.diamond.daq.gasrig.api.models.GasRigModel;

public class GasRigControls {

	private IGasRig gasRig;
	Composite gasList;

	@PostConstruct
	public void postConstruct(Composite parent) {

		gasRig = Finder.findOptionalSingleton(IGasRig.class).orElseThrow(() -> new RuntimeException("No gas rig found in configuration"));

		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(parent);

		gasList = new Composite(parent, SWT.BORDER);
		GridDataFactory.fillDefaults()
			.grab(true, true)
			.align(SWT.FILL, SWT.FILL)
			.applyTo(gasList);
		gasList.setLayout(new RowLayout(SWT.VERTICAL));

		GasRigModel gasRigInfo = gasRig.getGasRigInfo();

		addGasGroup("Non-Cabinet Gases", gasRigInfo.getNonCabinetGases());
		gasRigInfo.getCabinets().stream().forEach(this::addCabinet);
	}

	private void addGasGroup(String gasGroupName, List<GasModel> gases) {
		if (!gases.isEmpty()) {
			Group gasGroup = new Group(gasList, SWT.SHADOW_IN);
			gasGroup.setText(gasGroupName);
			GridLayoutFactory.fillDefaults().numColumns(1).applyTo(gasGroup);
			gases.forEach(gas -> addGas(gasGroup, gas));
		}
	}

	private void addGas(Group group, GasModel gas) {
		Label label = new Label(group, SWT.LEFT);
		GridDataFactory.fillDefaults()
			.grab(true, false)
			.align(SWT.LEFT, SWT.CENTER)
			.hint(200, SWT.DEFAULT)
			.applyTo(label);
		FontDescriptor fontDescriptor = FontDescriptor.createFrom(label.getFont());
		fontDescriptor = fontDescriptor.setStyle(SWT.BOLD);
		fontDescriptor = fontDescriptor.setHeight(12);
		label.setFont(fontDescriptor.createFont(label.getDisplay()));
		label.setText(gas.getName());
	}

	private void addCabinet(CabinetModel cabinet) {
		addGasGroup(cabinet.getName(), cabinet.getGases());
	}
}
