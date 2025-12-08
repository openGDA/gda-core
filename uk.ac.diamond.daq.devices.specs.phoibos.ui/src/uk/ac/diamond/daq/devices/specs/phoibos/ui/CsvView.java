/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos.ui;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Widget;

public class CsvView {
	public static final String ID = "uk.ac.diamond.daq.devices.specs.phoibos.ui.CsvView";
	private static final String DEFAULT_FILEPATH = "/dls_sw/b07/scripts/Beamline/Element_list(NEXAFS).csv";
	private String filePath;

	private TableViewer viewer;

	private final UISynchronize uiSync;


	@Inject
	public CsvView(@Optional String filePath, UISynchronize uiSync) {
		this.uiSync = uiSync;
		this.filePath = filePath != null? filePath:DEFAULT_FILEPATH;
	}

	@PostConstruct
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		Composite buttonArea = new Composite(parent, SWT.NONE);
		buttonArea.setLayout(new RowLayout());
		Button reloadButton = new Button(buttonArea, SWT.PUSH);
		reloadButton.setText("Reload From File");
		reloadButton.addListener(SWT.Selection, e -> {
			updateCsv();
		});

		viewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.BORDER);
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setContentProvider(ArrayContentProvider.getInstance());

		var table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		updateCsv();
	}

	private void updateCsv() {
		uiSync.asyncExec(() -> {
			try {
				List<String[]> rows = readCsv(filePath);
				displayCsv(rows);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private List<String[]> readCsv(String path) throws IOException {
		List<String[]> rows = new ArrayList<>();
		for (String line : Files.readAllLines(Paths.get(path))) {
			rows.add(line.split(","));
		}
		return rows;
	}

	private void displayCsv(List<String[]> rows) {
		var table = viewer.getTable();
		table.removeAll();
		Arrays.stream(table.getColumns()).forEach(Widget::dispose);

		if (rows.isEmpty()) return;

		// Create columns based on header
		String[] header = rows.get(0);
		for (String colName : header) {
			var column = new TableColumn(table, SWT.NONE);
			column.setText(colName);
			column.setWidth(75);
		}

		// Populate rows
		viewer.setInput(rows.subList(1, rows.size()));
		viewer.setLabelProvider(new CsvLabelProvider());
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
