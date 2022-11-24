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

package uk.ac.diamond.daq.client.gui.camera.absorption;

import static uk.ac.gda.ui.tool.ClientMessages.ABSORPTION;
import static uk.ac.gda.ui.tool.ClientMessages.BRIGHT;
import static uk.ac.gda.ui.tool.ClientMessages.DARK;

import java.util.stream.IntStream;

import org.eclipse.richbeans.widgets.menu.MenuAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.swtdesigner.SWTResourceManager;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.liveview.CameraPlotter;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;

/**
 * Allows to compare the intensities of two regions from a camera stream.
 *
 * <p>
 * Creates a context {@link MenuAction} named <i>Absorption</i> which contains other two other menu {@link MenuAction}:
 * <i>Bright</i> and <i>Dark</i> For each section, the user can select, then draw, different shapes: <i>Box</i>,
 * <i>Point</i> and <i>Circle</i>
 * </p>
 *
 * <p>
 * Creates also a two rows {@link Table}, one per region implemented as {@link ROIStatisticRow} Each row contains four
 * columns:
 * <ul>
 * <li>A check box to lock the calculated intensity ratio</li>
 * <li>The region name</li>
 * <li>The region intensity</li>
 * <li>The ratio with the other region, if existing</li>
 * </ul>
 * </p>
 *
 * @author Maurizio Nagni
 * @see ROIStatisticRow
 * @see AbsorptionElement
 *
 */
public class AbsorptionComposite implements CompositeFactory {

	private final CameraPlotter cameraPlotter;

	public AbsorptionComposite(CameraPlotter cameraPlotter) {
		this.cameraPlotter = cameraPlotter;
	}

	@Override
	public Composite createComposite(final Composite parent, int style) {
		// Creates a table
		Table table = new Table(parent, SWT.VIRTUAL | SWT.BORDER);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 200;
		table.setLayoutData(data);
		createTableColumns(table);

		// Creates rows for the table
		ROIStatisticRow bright = new ROIStatisticRow(table, ClientMessages.BRIGHT);
		ROIStatisticRow dark = new ROIStatisticRow(table, ClientMessages.DARK);

		// Creates the context menu for absorption regions
		MenuAction absorption = new MenuAction(ClientMessagesUtility.getMessage(ABSORPTION));
		absorption.setId(ClientMessagesUtility.getMessage(ABSORPTION));
		// Creates the bright region action and associates it the table row consumer
		absorption.add(new AbsorptionElement(BRIGHT, SWTResourceManager.getColor(SWT.COLOR_YELLOW), cameraPlotter,
				bright.getProcessEvent()));
		// Creates the dark region action and associates it the table row consumer
		absorption.add(new AbsorptionElement(DARK, SWTResourceManager.getColor(SWT.COLOR_RED), cameraPlotter,
				dark.getProcessEvent()));
		// append the menuAction to the existing context menu
		cameraPlotter.getPlottingSystem().getPlotActionSystem().addPopupAction(absorption);

		table.addDisposeListener(e -> dispose());

		return table;
	}

	private void createTableColumns(Table table) {
		ClientMessages[] headers = { ClientMessages.LOCK_VALUE, ClientMessages.SELECTION, ClientMessages.INTENSITY,
				ClientMessages.RATIO };
		IntStream.range(0, headers.length).forEach(c -> {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setWidth(120);
			column.setText(ClientMessagesUtility.getMessage(headers[c]));
		});
	}

	private void dispose() {
		cameraPlotter.getPlottingSystem().getPlotActionSystem().remove(ClientMessagesUtility.getMessage(ABSORPTION));
	}
}
