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

package uk.ac.diamond.daq.mapping.ui.position.summary;

import static uk.ac.gda.ui.tool.ClientMessages.ACTIONS;
import static uk.ac.gda.ui.tool.ClientMessages.POSITIONS;
import static uk.ac.gda.ui.tool.ClientMessages.STATE;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.Position;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;

/**
 * Displays in a tabular way a set of {@link Position}s
 *
 * @author Maurizio Nagni
 *
 */
public class PositionSummaryComposite implements CompositeFactory {

	private Table table;

	private final List<PositionSummaryRow> rows = new ArrayList<>();

	public PositionSummaryComposite() {
	}

	@Override
	public Composite createComposite(final Composite parent, int style) {
		// Creates a table
		table = new Table(parent, SWT.VIRTUAL | SWT.BORDER);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		createClientGridDataFactory().applyTo(table);
		createTableColumns(table);

		// Creates rows for the table
		createTableRow();
		return table;
	}

	private void createTableRow() {
		rows.add(new PositionSummaryRow(table, Position.START));
		rows.add(new PositionSummaryRow(table, Position.OUT_OF_BEAM));
	}

	private void createTableColumns(Table table) {
		ClientMessages[] headers = { POSITIONS, STATE, ACTIONS };
		IntStream.range(0, headers.length).forEach(c -> {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setWidth(100);
			column.setText(ClientMessagesUtility.getMessage(headers[c]));
		});
	}
}
