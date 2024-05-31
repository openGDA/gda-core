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

package uk.ac.gda.ui.tool.processing;

import static uk.ac.gda.ui.tool.ClientSWTElements.composite;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.scanning.api.event.scan.ProcessingRequest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import gda.autoprocessing.AutoProcessingBean;
import gda.configuration.properties.LocalProperties;
import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.autoprocessing.ui.AutoProcessingListViewer;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.gda.api.acquisition.configuration.AcquisitionConfiguration;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.Reloadable;
import uk.ac.gda.ui.tool.controller.AcquisitionController;
import uk.ac.gda.ui.tool.spring.ClientSpringContext;

/**
 * Displays in a tabular way a set of {@link ProcessingRequest}s
 *
 * <p>
 * This composite centralises, per perspective, the configuration of the processing requests.
 * The user may add one row at time and only if the last added row has been completed or deleted.
 * Once completed each row is able to update the actual acquisition in the controller.
 * </p>
 *
 * @author Maurizio Nagni
 *
 */
public class ProcessingRequestComposite implements CompositeFactory, Reloadable {


	private AutoProcessingListViewer processingListViewer;


	@Override
	public Composite createComposite(final Composite parent, int style) {

		var composite = composite(parent, 1);
		Button addConfigButton = new Button(composite, SWT.NONE);
		addConfigButton.setText("Add Config...");

		List<AutoProcessingBean> cwList = new ArrayList<>();
		processingListViewer = new AutoProcessingListViewer(composite);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(processingListViewer);

		String host = LocalProperties.get("gda.autoprocessing.server.host", "http://localhost");
		int port = LocalProperties.getInt("gda.autoprocessing.server.port", 8695);

		URI uri;
		try {
			uri = new URI(host + ":" + port);
		} catch (URISyntaxException e) {
			return composite;
		}
		processingListViewer.setUri(uri);

		addConfigButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				processingListViewer.addNewItem();
			}
		});

		processingListViewer.setInput(cwList);

		processingListViewer.addListener(this::updateAcquisitionFromControls);

		return composite;
	}

	private void updateAcquisitionFromControls() {
		getScanningAcquisition().ifPresent(acquisition ->
			acquisition.getAcquisitionConfiguration()
			.setProcessingRequest(processingListViewer.getProcessingList()));
	}

	private void updateControlsFromAcquisition() {
		List<AutoProcessingBean> processes = getScanningAcquisition()
			.map(ScanningAcquisition::getAcquisitionConfiguration)
			.map(AcquisitionConfiguration::getProcessingRequest)
			.orElse(new ArrayList<>()); // list must be modifiable

		processingListViewer.setInput(processes);
	}

	@Override
	public void reload() {
		if (processingListViewer.isDisposed()) return;
		updateControlsFromAcquisition();
	}

	private Optional<ScanningAcquisition> getScanningAcquisition() {
		return SpringApplicationContextFacade.getBean(ClientSpringContext.class).getAcquisitionController()
				.map(AcquisitionController::getAcquisition);
	}
}
