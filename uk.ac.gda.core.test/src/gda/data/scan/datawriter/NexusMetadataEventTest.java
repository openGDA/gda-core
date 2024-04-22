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

package gda.data.scan.datawriter;

import static gda.configuration.properties.LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT;
import static gda.data.nexus.NexusMetadataExtractor.END_SCAN_METADATA_NODE_PATHS_FILE_NAME;
import static gda.data.nexus.NexusMetadataExtractor.START_SCAN_METADATA_NODE_PATHS_FILE_NAME;
import static gda.data.scan.datawriter.NexusScanDataWriter.PROPERTY_VALUE_DATA_FORMAT_NEXUS_SCAN;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.data.scan.nexus.NexusScanDataWriterTestSetup;
import gda.device.Detector;
import gda.device.Scannable;
import gda.device.detector.DummyDetector;
import gda.device.scannable.DummyScannable;
import gda.scan.ConcurrentScan;
import gda.util.Version;
import uk.ac.diamond.daq.api.messaging.Message;
import uk.ac.diamond.daq.api.messaging.MessagingService;
import uk.ac.diamond.daq.api.messaging.messages.ScanMessage;
import uk.ac.diamond.daq.api.messaging.messages.ScanMetadataMessage;
import uk.ac.diamond.daq.api.messaging.messages.ScanStatus;
import uk.ac.diamond.osgi.services.ServiceProvider;

class NexusMetadataEventTest {

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

	private static final int NUM_POINTS = 4;

	private Path outputDir;
	private Scannable scannable;
	private Detector detector;
	private static MessagingService messagingService;

	@BeforeAll
	static void setUpServices() {
		NexusScanDataWriterTestSetup.setUp();

		messagingService = mock(MessagingService.class);
		ServiceProvider.setService(MessagingService.class, messagingService);
	}

	@AfterAll
	static void tearDownServices() {
		NexusScanDataWriterTestSetup.tearDown();
	}

	@BeforeEach
	void setUp() {
		scannable = new DummyScannable("s1", 0.0);
		detector = new DummyDetector("det");
	}

	@Test
	void testWriterReadNexusFile() throws Exception {
		final String testDir = TestHelpers.setUpTest(WriteStatsDataGroupTest.class, "testWriteStatsDataGroup", true);
		LocalProperties.set(GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, PROPERTY_VALUE_DATA_FORMAT_NEXUS_SCAN);
		outputDir = Path.of(testDir, "Data");
		createPropertiesFiles(); // can't do in setUp as GDA_VAR isn't set

		final LocalDateTime timeBeforeScan = LocalDateTime.now();
		final Object[] scanArgs = { scannable, 0, NUM_POINTS - 1, 1, detector }; // TODO make scan take a few seconds
		final ConcurrentScan scan = new ConcurrentScan(scanArgs);
		scan.runScan();

		final Path filePath = outputDir.resolve("1.nxs");
		assertThat(Files.exists(filePath), is(true));

		final ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
		// ScanMessages for scanStart, scanEnd, each point, plus ScanMetadataMessage for scanStart and scanEnd
		verify(messagingService, times(NUM_POINTS + 4)).sendMessage(messageCaptor.capture());

		final LocalDateTime timeAfterScan = LocalDateTime.now();
		final List<Message> messages = messageCaptor.getAllValues();
		assertThat(((ScanMessage) messages.get(0)).getStatus(), is(ScanStatus.STARTED));
		assertThat(((ScanMessage) messages.get(1)).getStatus(), is(ScanStatus.UPDATED));
		assertThat(messages.get(2), instanceOf(ScanMetadataMessage.class));
		IntStream.iterate(3, i -> i + 1).limit(NUM_POINTS - 1)
			.forEach(i -> assertThat("i = " + i, ((ScanMessage) messages.get(i)).getStatus(), is(ScanStatus.UPDATED)));
		assertThat(((ScanMessage) messages.get(NUM_POINTS + 2)).getStatus(), is(ScanStatus.FINISHED));
		assertThat(messages.get(messages.size() - 1), instanceOf(ScanMetadataMessage.class));

		final ScanMetadataMessage startScanMetadataMessage = (ScanMetadataMessage) messages.get(2);
		assertThat(startScanMetadataMessage.getScanStatus(), is(ScanStatus.STARTED));
		assertThat(startScanMetadataMessage.getFilePath(), is(equalTo(filePath.toAbsolutePath().toString())));
		final Map<String, Object> startScanMetadata = startScanMetadataMessage.getScanMetadata();
		assertThat(startScanMetadata.keySet(), containsInAnyOrder(START_SCAN_METADATA_NODE_PATHS.stream()
				.filter(path -> !path.equals("/entry/doesnotexist")).toArray()));
		assertThat(startScanMetadata.get("/entry/experiment_identifier"), is(equalTo("cm0-0")));
		assertThat(startScanMetadata.get("/entry/instrument/name"), is(equalTo("base")));
		assertThat(startScanMetadata.get("/entry/program_name"), is(equalTo("GDA " + Version.getRelease())));
		assertThat(startScanMetadata.get("/entry/scan_command"), is(equalTo(
				String.format("scan %s 0 %d 1 %s", scannable.getName(), NUM_POINTS - 1, detector.getName()))));
		final String startTimeStr = (String) startScanMetadata.get("/entry/start_time");
		assertThat(startTimeStr, is(notNullValue()));
		final LocalDateTime scanStartTime = LocalDateTime.parse(startTimeStr, MILLISECOND_DATE_FORMAT);
		assertThat(scanStartTime, both(greaterThan(timeBeforeScan)).and(lessThan(timeAfterScan)));

		final ScanMetadataMessage endScanMetadataMessage = (ScanMetadataMessage) messages.get(messages.size() - 1);
		assertThat(endScanMetadataMessage.getScanStatus(), is(ScanStatus.FINISHED));
		assertThat(endScanMetadataMessage.getFilePath(), is(equalTo(filePath.toAbsolutePath().toString())));
		final Map<String, Object> endScanMetadata = endScanMetadataMessage.getScanMetadata();
		assertThat(endScanMetadata.keySet(), containsInAnyOrder(END_SCAN_METADATA_NODE_PATHS.stream()
				.filter(path -> !path.equals("/entry/doesnotexist")).toArray()));
		final String endTimeStr = (String) endScanMetadata.get("/entry/end_time");
		assertThat(endTimeStr, is(notNullValue()));
		final LocalDateTime scanEndTime = LocalDateTime.parse(endTimeStr, MILLISECOND_DATE_FORMAT);
		assertThat(scanEndTime, both(greaterThan(scanStartTime)).and(lessThan(timeAfterScan)));
		assertThat(Duration.ofMillis((Long) endScanMetadata.get("/entry/diamond_scan/duration")), is(equalTo(between(scanStartTime, scanEndTime))));
		assertThat((Long) endScanMetadata.get("/entry/diamond_scan/scan_dead_time"), is(greaterThanOrEqualTo(0l)));
	}

	private void createPropertiesFiles() throws IOException {
		final Path gdaVar = Path.of(LocalProperties.getVarDir());
		Files.createDirectories(gdaVar);

		Files.write(gdaVar.resolve(START_SCAN_METADATA_NODE_PATHS_FILE_NAME), START_SCAN_METADATA_NODE_PATHS);
		Files.write(gdaVar.resolve(END_SCAN_METADATA_NODE_PATHS_FILE_NAME) , END_SCAN_METADATA_NODE_PATHS);
	}

}
