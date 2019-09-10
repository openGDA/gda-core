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

package org.eclipse.scanning.device.ui.util;

import static org.eclipse.scanning.api.event.EventConstants.ACQUIRE_REQUEST_TOPIC;
import static org.eclipse.scanning.api.event.EventConstants.ACQUIRE_RESPONSE_TOPIC;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.dawnsci.analysis.dataset.slicer.SliceInformation;
import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.scan.AcquireRequest;
import org.eclipse.scanning.api.ui.CommandConstants;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Functions used in mapping experiment setup
 */
public class ScanningUiUtils {

	// *******************************************************************************
	// TODO: refactor this class: rename this class AcquireUtils and move other out?

	private static final String DEFAULT_ENTRY_PATH = "/entry/";
	private static final String DEFAULT_DATASET_NAME = "data"; // NXdetector.data field

	private static final int ACQUIRE_TIMEOUT_LATENCY_SECONDS = 5;


	// Prevent instantiation
	private ScanningUiUtils() {
	}

	private static IRequester<AcquireRequest> acquireRequester = null;

	// Create a requestor to which acquire requests can be posted to ActiveMQ
	private static IRequester<AcquireRequest> getAcquireRequestor() throws Exception {
		if (acquireRequester == null) {
			final IEventService eventService = Activator.getDefault().getService(IEventService.class);
			final URI uri = new URI(CommandConstants.getScanningBrokerUri());
			acquireRequester = eventService.createRequestor(uri, ACQUIRE_REQUEST_TOPIC, ACQUIRE_RESPONSE_TOPIC);
			acquireRequester.setTimeout(5, TimeUnit.SECONDS);
		}

		return acquireRequester;
	}

	public static AcquireRequest acquireData(final IDetectorModel detectorModel) throws Exception {
		final AcquireRequest request = new AcquireRequest();
		request.setDetectorName(detectorModel.getName());
		request.setDetectorModel(detectorModel);

		IRequester<AcquireRequest> requester = getAcquireRequestor();
		requester.setTimeout(detectorModel.getTimeout() + ACQUIRE_TIMEOUT_LATENCY_SECONDS, TimeUnit.SECONDS);

		return getAcquireRequestor().post(request);
	}

	// TODO: refactor the methods below?
	public static String getDatasetPath(final String detectorName) {
		return DEFAULT_ENTRY_PATH + detectorName + "/" + DEFAULT_DATASET_NAME;
	}

	public static SourceInformation getSourceInformation(final String detectorName, final IDataset dataset) {
		return new SourceInformation("/", DEFAULT_ENTRY_PATH + detectorName + "/" + DEFAULT_DATASET_NAME, dataset);
	}

	public static SliceInformation getDatasetSlice(final IDataset dataset) {
		final int[] dataDimensions = dataset.getRank() == 1 ? new int[] { 0 } : new int[] { 0, 1 };
		return new SliceInformation(new SliceND(dataset.getShape()),
				new SliceND(dataset.getShape()),
				new SliceND(dataset.getShape()),
				dataDimensions, 1, 0);
	}

	public static Control createDataPlotControl(final Composite parent, final IPlottingSystem<Composite> plottingSystem, final String title) {
		final Composite plotAndToolbarComposite = new Composite(parent, SWT.NULL);
		GridLayoutFactory.fillDefaults().applyTo(plotAndToolbarComposite);

		plottingSystem.createPlotPart(plotAndToolbarComposite, title, null, PlotType.IMAGE, null);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(plottingSystem.getPlotComposite());
		return plotAndToolbarComposite;
	}

	public static Label createErrorLabel(final Composite parent, final String message, final Exception e) {
		final Label label = new Label(parent, SWT.NONE);
		label.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		label.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
		label.setText(message + ": " + e.getMessage());
		return label;
	}
}
