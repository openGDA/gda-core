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

package uk.ac.diamond.daq.autoprocessing.ui;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import gda.autoprocessing.AutoProcessingBean;
import gda.configuration.properties.LocalProperties;

public class AutoProcessingExampleView extends ViewPart {

	private AutoProcessingListViewer listViewer;

	@Override
	public void createPartControl(Composite parent) {

		List<AutoProcessingBean> cwList = new ArrayList<>();

		parent.setLayout(new GridLayout(2, false));

		Button addConfigButton = new Button(parent, SWT.NONE);
		addConfigButton.setText("Add Config...");

		listViewer = new AutoProcessingListViewer(parent);
		listViewer.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(2, 1).align(SWT.FILL, SWT.FILL).create());
		String host = LocalProperties.get("gda.autoprocessing.server.host", "http://localhost");
		int port = LocalProperties.getInt("gda.autoprocessing.server.port", 8695);

		URI uri;
		try {
			uri = new URI(host + ":" + port);
		} catch (URISyntaxException e) {
			return;
		}
		listViewer.setUri(uri);

		addConfigButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				listViewer.addNewItem();
			}
		});

		listViewer.setInput(cwList);
	}

	@Override
	public void setFocus() {
		listViewer.setFocus();
	}

}
