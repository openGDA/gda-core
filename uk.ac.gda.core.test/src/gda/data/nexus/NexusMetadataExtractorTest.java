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

package gda.data.nexus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mock.Strictness;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.ac.diamond.daq.api.messaging.messages.INexusMetadataExtractor;
import uk.ac.diamond.daq.api.messaging.messages.ScanMetadataMessage;
import uk.ac.diamond.daq.api.messaging.messages.ScanStatus;
import uk.ac.diamond.osgi.services.ServiceProvider;

@ExtendWith(MockitoExtension.class)
class NexusMetadataExtractorTest {

	private static final int SCAN_NUMBER = 123;
	private static final Path FILE_PATH = Path.of(SCAN_NUMBER + ".nxs");

	private static final Map<ScanStatus, Map<String, Object>> SCAN_METADATA;

	private INexusMetadataExtractor metadataExtractor;

	static {
		SCAN_METADATA = new EnumMap<>(ScanStatus.class);

		final Map<String, Object> startScanMetadata = new LinkedHashMap<>(Map.of(
				"/entry/experiment_identifier", "cm0-0",
				"/entry/instrument/name", "ixx",
				"/entry/program_name", "GDA 9.35",
				"/entry/scan_command", "scan s1 0 1 5 det",
				"/entry/start_time", "2024-05-02T11:03:16.091"));
		startScanMetadata.put("/entry/doesnotexist", null); // Map.of doesn't allow null values
		SCAN_METADATA.put(ScanStatus.STARTED, startScanMetadata);

		final Map<String, Object> endScanMetadata = new LinkedHashMap<>(Map.of(
				"/entry/end_time", "2024-05-02T11:03:19.091",
				"/entry/diamond_scan/duration", 3000L,
				"/entry/diamond_scan/scan_dead_time", 1500L));
		endScanMetadata.put("/entry/doesnotexist", null);
		SCAN_METADATA.put(ScanStatus.FINISHED, endScanMetadata);
	}

	@Mock
	private INexusFileFactory mockNexusFileFactory;

	@Mock(strictness = Strictness.LENIENT)
	private IFilePathService mockFilePathService;

	@BeforeEach
	void setUp() throws Exception {
		ServiceProvider.setService(INexusFileFactory.class, mockNexusFileFactory);
		metadataExtractor = new NexusMetadataExtractor();

		when(mockFilePathService.getPersistenceDir()).thenReturn("var");
		ServiceProvider.setService(IFilePathService.class, mockFilePathService);
	}

	@AfterEach
	void tearDown() {
		ServiceProvider.reset();
	}

	@ParameterizedTest(name = "scanStatus = {0}")
	@EnumSource(ScanStatus.class)
	void testCreateScanMetadataMessage(ScanStatus scanStatus) throws Exception {
		if (scanStatus != ScanStatus.STARTED && scanStatus != ScanStatus.FINISHED) {
			assertThrows(IllegalArgumentException.class,
					() -> metadataExtractor.createScanMetadataMessage(scanStatus, FILE_PATH.toString()));
			return;
		}

		setUpMockNexusFile(scanStatus);

		final Map<String, Object> metadata =  SCAN_METADATA.get(scanStatus);
		final List<String> metadataNodePaths = new ArrayList<>(metadata.keySet());

		ScanMetadataMessage message = null;
		final Path metadataPathsFilePath = NexusMetadataExtractor.getPropertiesFilePath(scanStatus);
		try (MockedStatic<Files> mockFiles = mockStatic(Files.class)) {
			mockFiles.when(() -> Files.exists(metadataPathsFilePath)).thenReturn(true);
			mockFiles.when(() -> Files.readAllLines(metadataPathsFilePath)).thenReturn(metadataNodePaths);

			message = metadataExtractor.createScanMetadataMessage(scanStatus, FILE_PATH.toString());
		}

		assertThat(message, is(notNullValue()));
		assertThat(message.getScanStatus(), is(scanStatus));
		assertThat(message.getFilePath(), is(equalTo(FILE_PATH.toString())));

		final Map<String, Object> scanMetadata = message.getScanMetadata();
		assertThat(scanMetadata, is(notNullValue()));
		assertThat(scanMetadata.keySet(), containsInAnyOrder(metadata.keySet().stream()
				.filter(path -> !path.equals("/entry/doesnotexist")).toArray()));

		Map<String, Object> expectedScanMetadata = new HashMap<>(metadata);
		expectedScanMetadata.remove("/entry/doesnotexist");
		assertThat(scanMetadata, is(equalTo(scanMetadata)));
	}

	private void setUpMockNexusFile(ScanStatus scanStatus) throws Exception {
		NexusFile mockNexusFile = mock(NexusFile.class);
		when(mockNexusFileFactory.newNexusFile(FILE_PATH.toString())).thenReturn(mockNexusFile);

		final Map<String, Object> metadata = SCAN_METADATA.get(scanStatus);
		for (Map.Entry<String, Object> metadataEntry : metadata.entrySet()) {
			final DataNode dataNode = NexusNodeFactory.createDataNode();
			dataNode.setDataset(DatasetFactory.createFromObject(metadataEntry.getValue()));
			when(mockNexusFile.getNode(metadataEntry.getKey())).thenReturn(dataNode);
		}
	}

}
