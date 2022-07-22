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

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import gda.configuration.properties.LocalProperties;

public class AutoProcessingExampleView extends ViewPart {

	private AutoProcessingConfigComposite configComposite;

	@Override
	public void createPartControl(Composite parent) {

		IMarshallerService service = Activator.getService(IMarshallerService.class);
		parent.setLayout(new GridLayout());

		String host = LocalProperties.get("gda.autoprocessing.server.host", "http://localhost");
		int port = LocalProperties.getInt("gda.autoprocessing.server.port", 5000);

		URI uri;
		try {
			uri = new URI(host + ":" + port);
		} catch (URISyntaxException e) {
			return;
		}

		configComposite = new AutoProcessingConfigComposite(parent, service, uri);
		configComposite
				.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).create());

		Button button = new Button(parent, SWT.None);
		button.setText("Update");
		button.setLayoutData(GridDataFactory.fillDefaults().grab(false, false).align(SWT.LEFT, SWT.CENTER).create());

		Composite lc = new Composite(parent, SWT.NONE);
		lc.setLayout(new FillLayout());

		final Label label = new Label(parent, SWT.NONE);
		label.setText("Press update to get config");
		label.setLayoutData(GridDataFactory.fillDefaults().grab(false, false).align(SWT.LEFT, SWT.CENTER).create());

		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String[] conf = configComposite.getConfiguration();
				try {
					label.setText(conf[1]);
					label.getParent().layout();
				} catch (Exception e1) {
					// do nothing
				}
			}
		});
	}

	@Override
	public void setFocus() {
		configComposite.setFocus();

	}

}
