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

import static org.eclipse.scanning.api.event.EventConstants.ACQUIRE_REQUEST_TOPIC;
import static org.eclipse.scanning.api.event.EventConstants.ACQUIRE_RESPONSE_TOPIC;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.dawnsci.analysis.dataset.slicer.SliceInformation;
import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.scan.AcquireRequest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.ui.MappingUIConstants;

/**
 * Functions used in mapping experiment setup
 */
class MappingExperimentUtils {

	private static final String DEFAULT_ENTRY_PATH = "/entry/";
	private static final String DEFAULT_DATASET_NAME = "data"; // NXdetector.data field

	// Prevent instantiation
	private MappingExperimentUtils() {
	}

	// Create a requestor to which acquire requests can be posted to ActiveMQ
	static IRequester<AcquireRequest> getAcquireRequestor(final IEclipseContext context) throws Exception {
		final IEventService eventService = context.get(IEventService.class);
		final URI uri = new URI(LocalProperties.getActiveMQBrokerURI());
		final IRequester<AcquireRequest> acquireRequestor = eventService.createRequestor(uri, ACQUIRE_REQUEST_TOPIC, ACQUIRE_RESPONSE_TOPIC);
		acquireRequestor.setTimeout(15, TimeUnit.SECONDS);
		return acquireRequestor;
	}

	static AcquireRequest acquireData(final IDetectorModel detectorModel, final IRequester<AcquireRequest> acquireRequestor) throws Exception {
		final AcquireRequest request = new AcquireRequest();
		request.setDetectorName(detectorModel.getName());
		request.setDetectorModel(detectorModel);
		return acquireRequestor.post(request);
	}

	static Image getImage(final String imagePath) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(MappingUIConstants.PLUGIN_ID, imagePath).createImage();
	}

	static String getDatasetPath(final String detectorName) {
		return DEFAULT_ENTRY_PATH + detectorName + "/" + DEFAULT_DATASET_NAME;
	}

	static SourceInformation getSourceInformation(final String detectorName, final IDataset dataset) {
		return new SourceInformation("/", DEFAULT_ENTRY_PATH + detectorName + "/" + DEFAULT_DATASET_NAME, dataset);
	}

	static SliceInformation getDatasetSlice(final IDataset dataset) {
		final int[] dataDimensions = dataset.getRank() == 1 ? new int[] { 0 } : new int[] { 0, 1 };
		return new SliceInformation(new SliceND(dataset.getShape()),
				new SliceND(dataset.getShape()),
				new SliceND(dataset.getShape()),
				dataDimensions, 1, 1);
	}

	static Control createDataPlotControl(final Composite parent, final IPlottingSystem<Composite> plottingSystem, final String title) {
		final Composite plotAndToolbarComposite = new Composite(parent, SWT.NULL);
		GridLayoutFactory.fillDefaults().applyTo(plotAndToolbarComposite);

		plottingSystem.createPlotPart(plotAndToolbarComposite, title, null, PlotType.IMAGE, null);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(plottingSystem.getPlotComposite());
		return plotAndToolbarComposite;
	}

	static Label createErrorLabel(final Composite parent, final String message, final Exception e) {
		final Label label = new Label(parent, SWT.NONE);
		label.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		label.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
		label.setText(message + ": " + e.getMessage());
		return label;
	}
}
