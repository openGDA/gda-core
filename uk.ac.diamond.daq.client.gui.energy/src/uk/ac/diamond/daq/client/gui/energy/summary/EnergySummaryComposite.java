/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.energy.summary;

import static uk.ac.gda.ui.tool.ClientMessages.BEAM_TYPE;
import static uk.ac.gda.ui.tool.ClientMessages.ENERGY_KEV;
import static uk.ac.gda.ui.tool.ClientMessages.SHUTTER;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientButton;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGroup;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.energy.BeamEnergyDialogBuilder;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.images.ClientImages;

/**
 * Displays a summary of the active beam and allows to open the more general {@link BeamEnergyDialogBuilder}
 *
 * @author Maurizio Nagni
 *
 */
public class EnergySummaryComposite implements CompositeFactory {

	private Button energyButton;
	private Label energy;
	private Label energyValue;
	private Button shutter;
	private Label shutterLabel;
	private Label shutterValue;

	private EnergySummaryRow row;

	public EnergySummaryComposite() {
	}

	@Override
	public Composite createComposite(final Composite parent, int style) {
		Group container = createClientGroup(parent, style, 1, ClientMessages.SOURCE);
		ClientSWTElements.createClientGridDataFactory().align(SWT.FILL, SWT.FILL).applyTo(container);

		energyButton = createClientButton(container, style, ClientMessages.CONFIGURE, ClientMessages.ENERGY_KEV,
				ClientImages.BEAM_16);
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.BOTTOM).indent(5, SWT.DEFAULT).applyTo(energyButton);

		// Creates a table
		Table table = new Table(container, SWT.BORDER);
		ClientSWTElements.createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		createTableColumns(table);
		bindElements(container);
		return container;
	}

	private void createTableColumns(Table table) {
		ClientMessages[] headers = { BEAM_TYPE, ENERGY_KEV, SHUTTER };
		Set<TableColumn> columns = new HashSet<>();
		IntStream.range(0, headers.length).forEach(c -> {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(ClientMessagesUtility.getMessage(headers[c]));
			columns.add(column);
		});

		// Creates rows for the table
		row = new EnergySummaryRow(table);
		table.setSize(table.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		columns.stream().forEach(TableColumn::pack);
	}

	private void bindElements(Composite parent) {
		Listener energyButtonListener = e -> {
			BeamEnergyDialogBuilder builder = new BeamEnergyDialogBuilder();
			builder.addImagingController();
			builder.addDiffractionController();
			builder.build(parent.getShell()).open();
			row.updateColumns();
		};
		energyButton.addListener(SWT.Selection, energyButtonListener);
	}
}
