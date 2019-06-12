/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.test.event;

import static org.eclipse.scanning.api.event.EventConstants.ACQUIRE_REQUEST_TOPIC;
import static org.eclipse.scanning.api.event.EventConstants.ACQUIRE_RESPONSE_TOPIC;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertAxes;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertSignal;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertTarget;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.PositionIterator;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.scan.AcquireRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.server.servlet.AcquireServlet;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AcquireRequestTest extends BrokerTest {

	private IRequester<AcquireRequest> requester;
	private AcquireServlet acquireServlet;

	@Before
	public void createServices() throws Exception {
		ServiceTestHelper.setupServices();
		ServiceTestHelper.registerTestDevices();

		this.acquireServlet = new AcquireServlet();
		acquireServlet.setBroker(uri.toString());
		acquireServlet.connect();

		requester = ServiceTestHelper.getEventService().createRequestor(uri, ACQUIRE_REQUEST_TOPIC, ACQUIRE_RESPONSE_TOPIC);
		requester.setTimeout(10, TimeUnit.SECONDS);
	}

	@After
	public void stop() throws Exception {
		requester.disconnect();
		acquireServlet.disconnect();
	}

	@Test
	public void testAcquire() throws Exception {
		AcquireRequest request = createRequest();
		AcquireRequest response = requester.post(request);
		assertThat(response, is(notNullValue()));
		assertThat(response.getStatus(), is(Status.COMPLETE));
		assertThat(response.getMessage(), is(nullValue()));

		checkNexusFile(response);
	}

	private AcquireRequest createRequest() throws IOException {
		final AcquireRequest request = new AcquireRequest();

		final File file = File.createTempFile("acquire_servlet_test", ".nxs");
		System.err.println("Writing to file " + file);
		file.deleteOnExit();
		request.setFilePath(file.getAbsolutePath());

		final MandelbrotModel mandyModel = new MandelbrotModel();
		mandyModel.setName("mandelbrot");
		mandyModel.setRealAxisName("xNex");
		mandyModel.setImaginaryAxisName("yNex");
		mandyModel.setExposureTime(0.01);

		request.setDetectorName(mandyModel.getName());
		request.setDetectorModel(mandyModel);

		return request;
	}


	private void checkNexusFile(AcquireRequest request) throws Exception {
		String filePath = request.getFilePath();

		INexusFileFactory fileFactory = org.eclipse.dawnsci.nexus.ServiceHolder.getNexusFileFactory();
		NXroot rootNode = null;
		try (NexusFile nf =  fileFactory.newNexusFile(filePath)) {
			nf.openToRead();
			TreeFile nexusTree = NexusUtils.loadNexusTree(nf);
			rootNode = (NXroot) nexusTree.getGroupNode();
		}

		NXentry entry = rootNode.getEntry();
		NXinstrument instrument = entry.getInstrument();

		LinkedHashMap<String, List<String>> signalFieldAxes = new LinkedHashMap<>();
		// axis for additional dimensions of a datafield, e.g. image
		// note all dataset have additional first dimension of size 1. This is added by the
		// the jython static generator due to the limitiations of area detector
		signalFieldAxes.put(NXdetector.NX_DATA, Arrays.asList(".", "real", "imaginary"));
		signalFieldAxes.put("spectrum", Arrays.asList(".", "spectrum_axis"));
		signalFieldAxes.put("value", Arrays.asList("."));

		String detectorName = request.getDetectorName();
		NXdetector detector = instrument.getDetector(detectorName);
		// map of detector data field to name of nxData group where that field is the @signal field
		Map<String, String> expectedDataGroupNames =
				signalFieldAxes.keySet().stream().collect(Collectors.toMap(Function.identity(),
				x -> detectorName + (x.equals(NXdetector.NX_DATA) ? "" : "_" + x)));

		// validate the main NXdata generated by the NexusDataBuilder
		Map<String, NXdata> nxDataGroups = entry.getChildren(NXdata.class);
		assertEquals(signalFieldAxes.size(), nxDataGroups.size());
		assertTrue(nxDataGroups.keySet().containsAll(
				expectedDataGroupNames.values()));
		for (String nxDataGroupName : nxDataGroups.keySet()) {
			NXdata nxData = entry.getData(nxDataGroupName);

			String sourceFieldName = nxDataGroupName.equals(detectorName) ? NXdetector.NX_DATA :
				nxDataGroupName.substring(nxDataGroupName.indexOf('_') + 1);
			assertSignal(nxData, sourceFieldName);
			// check the nxData's signal field is a link to the appropriate source data node of the detector
			DataNode dataNode = detector.getDataNode(sourceFieldName);
			IDataset dataset = dataNode.getDataset().getSlice();
			assertSame(dataNode, nxData.getDataNode(sourceFieldName));
			assertTarget(nxData, sourceFieldName, rootNode, "/entry/instrument/" + detectorName
					+ "/" + sourceFieldName);

			// check that the other primary data fields of the detector haven't been added to this NXdata
			for (String primaryDataFieldName : signalFieldAxes.keySet()) {
				if (!primaryDataFieldName.equals(sourceFieldName)) {
					assertNull(nxData.getDataNode(primaryDataFieldName));
				}
			}

			int[] shape = dataset.getShape();

			// Make sure none of the numbers are NaNs. The detector
			// is expected to fill this scan with non-nulls.
			final PositionIterator it = new PositionIterator(shape);
			while (it.hasNext()) {
				int[] next = it.getPos();
				assertFalse(Double.isNaN(dataset.getDouble(next)));
			}

			// Check axes
			List<String> expectedAxesNames = signalFieldAxes.get(sourceFieldName);
			assertAxes(nxData, expectedAxesNames.toArray(new String[expectedAxesNames.size()]));
		}
	}

}
