/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package org.eclipse.scanning.test.scan.nexus;

import static java.time.Duration.between;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.MILLISECOND_DATE_FORMAT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.ac.diamond.daq.api.messaging.messages.INexusMetadataExtractor.END_SCAN_METADATA_NODE_PATHS_FILE_NAME;
import static uk.ac.diamond.daq.api.messaging.messages.INexusMetadataExtractor.START_SCAN_METADATA_NODE_PATHS_FILE_NAME;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.server.servlet.ScanProcess;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import gda.data.nexus.NexusMetadataExtractor;
import gda.util.Version;
import uk.ac.diamond.daq.api.messaging.Message;
import uk.ac.diamond.daq.api.messaging.MessagingService;
import uk.ac.diamond.daq.api.messaging.messages.INexusMetadataExtractor;
import uk.ac.diamond.daq.api.messaging.messages.ScanMessage;
import uk.ac.diamond.daq.api.messaging.messages.ScanMetadataMessage;
import uk.ac.diamond.daq.api.messaging.messages.ScanStatus;
import uk.ac.diamond.osgi.services.ServiceProvider;

class ScanMetadataMessageTest {

	private static final int NUM_POINTS = 5;

	private static final List<String> START_SCAN_METADATA_NODE_PATHS = List.of(
			"/entry/experiment_identifier",
			"/entry/instrument/name",
			"/entry/program_name",
			"/entry/scan_command",
			"/entry/start_time",
			"/entry/doesnotexist");

	private static final List<String> END_SCAN_METADATA_NODE_PATHS = List.of(
			"/entry/end_time",
			"/entry/doesnotexist",
			"/entry/diamond_scan/duration",
			"/entry/diamond_scan/scan_dead_time");

	private static final String PROPERTY_NAME_GDA_VAR = "gda.var";

	private static MessagingService messagingService;

	private MandelbrotDetector detector;

	@BeforeAll
	static void setUpServices() {
		ServiceTestHelper.setupServices();
		messagingService = mock(MessagingService.class);
		ServiceProvider.setService(MessagingService.class, messagingService);
		ServiceProvider.setService(INexusMetadataExtractor.class, new NexusMetadataExtractor());

		// ensure LocalProperties
		final IFilePathService filePathService = ServiceProvider.getService(IFilePathService.class);
		System.setProperty(PROPERTY_NAME_GDA_VAR, filePathService.getPersistenceDir());
	}

	@AfterAll
	static void tearDownServices() {
		ServiceProvider.reset();
		System.clearProperty(PROPERTY_NAME_GDA_VAR);
	}

	@BeforeEach
	void setUp() throws Exception {
		createPropertiesFiles();

		final MandelbrotModel model = new MandelbrotModel("p", "q");
		model.setName("mandelbrot");
		model.setExposureTime(0.0001);

		detector = (MandelbrotDetector) TestDetectorHelpers.createAndConfigureMandelbrotDetector(model);
		ServiceProvider.getService(IRunnableDeviceService.class).register(detector);
	}

	@Test
	void testScanMetadataMessages() throws Exception {
		final ScanBean scanBean = new ScanBean();
		final ScanRequest scanRequest = new ScanRequest();
		scanRequest.setDetectors(Map.of(detector.getName(), detector.getModel()));
		scanBean.setScanRequest(scanRequest);
		scanRequest.setCompoundModel(new CompoundModel(new AxialStepModel("xNex", 0, NUM_POINTS - 1, 1)));

		final LocalDateTime timeBeforeScan = LocalDateTime.now();
		final ScanProcess scanProcess = new ScanProcess(scanBean, null, true);
		scanProcess.execute(); // run in same thread

		final String filePath = scanBean.getFilePath();
		assertThat(Files.exists(Path.of(filePath)), is(true));

		final ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
		verify(messagingService, times(NUM_POINTS + 4)).sendMessage(messageCaptor.capture());

		final LocalDateTime timeAfterScan = LocalDateTime.now();
		final List<Message> messages = messageCaptor.getAllValues();
		assertThat(((ScanMessage) messages.get(0)).getStatus(), is(ScanStatus.STARTED));
		assertThat(messages.get(1), instanceOf(ScanMetadataMessage.class));
		IntStream.iterate(2, i -> i + 1).limit(NUM_POINTS)
			.forEach(i -> assertThat("i = " + i, ((ScanMessage) messages.get(i)).getStatus(), is(ScanStatus.UPDATED)));
		assertThat(((ScanMessage) messages.get(NUM_POINTS + 2)).getStatus(), is(ScanStatus.FINISHED));
		assertThat(messages.get(messages.size() - 1), instanceOf(ScanMetadataMessage.class));

		final ScanMetadataMessage startScanMetadataMessage = (ScanMetadataMessage) messages.get(1);
		assertThat(startScanMetadataMessage.getScanStatus(), is(ScanStatus.STARTED));
		assertThat(startScanMetadataMessage.getFilePath(), is(equalTo(filePath)));
		final Map<String, Object> startScanMetadata = startScanMetadataMessage.getScanMetadata();
		assertThat(startScanMetadata.keySet(), containsInAnyOrder(START_SCAN_METADATA_NODE_PATHS.stream()
				.filter(path -> !path.equals("/entry/doesnotexist") && !path.equals("/entry/scan_command")).toArray()));
		assertThat(startScanMetadata.get("/entry/experiment_identifier"), is(equalTo("test-mock")));
		assertThat(startScanMetadata.get("/entry/instrument/name"), is(equalTo("base")));
		assertThat(startScanMetadata.get("/entry/program_name"), is(equalTo("GDA " + Version.getRelease())));
		final String startTimeStr = (String) startScanMetadata.get("/entry/start_time");
		assertThat(startTimeStr, is(notNullValue()));
		final LocalDateTime scanStartTime = LocalDateTime.parse(startTimeStr, MILLISECOND_DATE_FORMAT);
		assertThat(scanStartTime, both(greaterThan(timeBeforeScan)).and(lessThan(timeAfterScan)));

		final ScanMetadataMessage endScanMetadataMessage = (ScanMetadataMessage) messages.get(messages.size() - 1);
		assertThat(endScanMetadataMessage.getScanStatus(), is(ScanStatus.FINISHED));
		assertThat(endScanMetadataMessage.getFilePath(), is(equalTo(filePath)));
		final Map<String, Object> endScanMetadata = endScanMetadataMessage.getScanMetadata();
		assertThat(endScanMetadata.keySet(), containsInAnyOrder(END_SCAN_METADATA_NODE_PATHS.stream()
				.filter(path -> !path.equals("/entry/doesnotexist")).toArray()));
		final String endTimeStr = (String) endScanMetadata.get("/entry/end_time");
		assertThat(endTimeStr, is(notNullValue()));
		final LocalDateTime scanEndTime = LocalDateTime.parse(endTimeStr, MILLISECOND_DATE_FORMAT);
		assertThat(scanEndTime, both(greaterThan(scanStartTime)).and(lessThan(timeAfterScan)));
		assertThat(Duration.ofMillis((Long) endScanMetadata.get("/entry/diamond_scan/duration")),
				is(equalTo(between(scanStartTime, scanEndTime))));
		assertThat((Long) endScanMetadata.get("/entry/diamond_scan/scan_dead_time"), is(greaterThanOrEqualTo(0l)));
	}

	private void createPropertiesFiles() throws IOException {
		final IFilePathService filePathService = ServiceProvider.getService(IFilePathService.class);
		final Path varDir = Path.of(filePathService.getPersistenceDir());

		Files.write(varDir.resolve(START_SCAN_METADATA_NODE_PATHS_FILE_NAME), START_SCAN_METADATA_NODE_PATHS);
		Files.write(varDir.resolve(END_SCAN_METADATA_NODE_PATHS_FILE_NAME), END_SCAN_METADATA_NODE_PATHS);
	}

}
