package uk.ac.diamond.daq.service.command.receiver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.diamond.daq.mapping.api.document.DocumentMapper;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.service.ScanningAcquisitionFileService;
import uk.ac.diamond.daq.service.ServiceUtils;
import uk.ac.diamond.daq.service.command.strategy.OutputStrategy;
import uk.ac.diamond.osgi.services.ServiceProvider;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;
import uk.ac.gda.common.entity.Document;
import uk.ac.gda.common.entity.filter.DocumentFilter;
import uk.ac.gda.common.entity.filter.DocumentFilterBuilder;
import uk.ac.gda.common.exception.GDAServiceException;

public class FilesCollectionCommandReceiverTest {

	private CollectionCommandReceiver<Document> receiver;

	@Mock
	private static IFilePathService filePathService;

	@Mock
	private ScanningAcquisitionFileService fileService;

	@Mock
	private OutputStream outputStream;

	@Rule
	public MockitoRule initialiseMocks = MockitoJUnit.rule();

	@Rule
	public TemporaryFolder visitConfigDir = new TemporaryFolder();

	@Captor
	private ArgumentCaptor<String> documentCaptor;

	private InMemoryOutput output = new InMemoryOutput();

	private ObjectMapper mapper = new ObjectMapper();

	@Before
	public void injectServices() throws Exception {
		filePathService = mock(IFilePathService.class);
		when(filePathService.getVisitConfigDir()).thenReturn(visitConfigDir.getRoot().getAbsolutePath());
		ServiceProvider.setService(IFilePathService.class, filePathService);
		receiver = new FilesCollectionCommandReceiver<Document>(Document.class, outputStream);

		ReflectionTestUtils.setField(receiver, "documentMapper", mock(DocumentMapper.class, CALLS_REAL_METHODS));
		ReflectionTestUtils.setField(receiver, "serviceUtils", new ServiceUtils());
		ReflectionTestUtils.setField(receiver, "fileService", fileService);
	}

	@After
	public void tearDownServices() {
		ServiceProvider.reset();
	}

	/** When no documents are found, we return an empty list */
	@Test
	public void queryNoDocuments() throws Exception {
		receiver.query(mock(DocumentFilter.class), output);
		assertTrue(output.documents.isEmpty());
	}

	/** retrieve all documents found */
	@Test
	public void unfilteredQuery() throws Exception {
		var json = mapper.writeValueAsString(new ScanningAcquisition());
		createDiffractionFiles(2, json);
		createTomographyFiles(1, json);

		var filter = mock(DocumentFilter.class);
		receiver.query(filter, output);

		assertThat(output.documents.size(), is(3));
	}

	/** we return documents matching extension given in filter */
	@Test
	public void filteredQuery() throws Exception {
		var json = mapper.writeValueAsString(new ScanningAcquisition());
		// create 3 files total...
		createDiffractionFiles(2, json);
		createTomographyFiles(1, json);

		var filter = new DocumentFilterBuilder().setFileExtension("map").build();
		receiver.query(filter, output);

		// ...retrieve 2
		assertThat(output.documents.size(), is(2));
	}

	/** Simple UUID matching */
	@Test
	public void getDocument() throws Exception {
		var scan = new ScanningAcquisition();
		var uuid = UUID.randomUUID();
		scan.setUuid(uuid);
		createDiffractionFiles(1, mapper.writeValueAsString(scan));
		var other = new ScanningAcquisition();
		other.setUuid(UUID.randomUUID());
		createDiffractionFiles(2, mapper.writeValueAsString(other));

		receiver.getDocument(uuid, output);

		assertThat(output.documents.size(), is(1));
		assertThat(output.documents.get(0), is(equalTo(scan)));
	}

	/** Returns null */
	@Test
	public void getDocumentNotFound() throws Exception {
		receiver.getDocument(UUID.randomUUID(), output);
		assertNull(output.documents);
	}

	/** A UUID is inserted internally when null */
	@Test
	public void insertDocumentNoId() throws Exception {
		var document = new ScanningAcquisition();
		var name = "my_scan";
		document.setName(name);

		receiver.insertDocument(document, output);

		verify(fileService).saveTextDocument(documentCaptor.capture(), eq(name),
				eq(AcquisitionConfigurationResourceType.DEFAULT), eq(false));

		// we do this because no guarantees on json format
		var writtenDocument = mapper.readValue(documentCaptor.getValue(), ScanningAcquisition.class);
		assertThat(writtenDocument, is(equalTo(document)));

		assertNotNull(document.getUuid());
	}

	/**
	 * A document with a preexisting UUID
	 * signals an overwrite operation
	 */
	@Test
	public void insertDocumentWithId() throws Exception {
		var document = new ScanningAcquisition();
		document.setUuid(UUID.randomUUID());
		var file = createTomographyFiles(1, mapper.writeValueAsString(document)).get(0);
		assertTrue(file.exists());

		var name = "Ludgar";
		document.setName(name);

		receiver.insertDocument(document, output);

		assertThat(output.documents.size(), is(1));
		assertThat(output.documents.get(0).getName(), is(equalTo(name)));

		// our file has been deleted (the real service would then overwrite it)
		assertFalse(file.exists());
	}

	/**
	 * We assume we are overwriting when given a document with UUID,
	 * but should still succeed in saving if no previous document
	 * with a matching UUID is found.
	 */
	@Test
	public void insertDocumentWithIdNoPreexistingDocument() throws Exception {
		var document = new ScanningAcquisition();
		document.setUuid(UUID.randomUUID());
		receiver.insertDocument(document, output);

		assertThat(output.documents.size(), is(1));
		assertThat(output.documents.get(0), is(equalTo(document)));
	}

	@Test
	public void insertDocumentFailureWrappedAsGDAServiceException() throws Exception {
		when(fileService.saveTextDocument(any(String.class), any(String.class), any(AcquisitionConfigurationResourceType.class), any(boolean.class)))
			.thenThrow(new IOException("Couldn't save :)"));

		assertThrows(GDAServiceException.class, () -> receiver.insertDocument(mock(Document.class), output));
	}

	/** Simple UUID matching */
	@Test
	public void deleteDocument() throws Exception {
		var document = new ScanningAcquisition();
		var uuid = UUID.randomUUID();
		document.setUuid(uuid);
		var file = createDiffractionFiles(1, mapper.writeValueAsString(document)).get(0);
		assertTrue(file.exists());

		// create other files for mad confusion
		var other = new ScanningAcquisition();
		other.setUuid(UUID.randomUUID());
		createDiffractionFiles(3, mapper.writeValueAsString(other));

		receiver.deleteDocument(uuid, output);
		// our manually created file has been deleted...
		assertFalse(file.exists());

		// ...and output strategy invoked with deleted document
		assertThat(output.documents.get(0), is(equalTo(document)));
	}

	/** Output strategy never invoked when UUID not found */
	@Test
	public void deleteWithNonExistingId() throws Exception {
		receiver.deleteDocument(UUID.randomUUID(), output);
		assertNull(output.documents);
	}

	private List<File> createDiffractionFiles(int numberOfFiles, String fileContent) throws Exception {
		return createScanFiles(numberOfFiles, visitConfigDir, "map", fileContent);
	}

	private List<File> createTomographyFiles(int numberOfFiles, String fileContent) throws Exception {
		return createScanFiles(numberOfFiles, visitConfigDir, "tomo", fileContent);
	}

	private List<File> createScanFiles(int number, TemporaryFolder folder, String extension, String fileContent) throws Exception {
		List<File> files = new ArrayList<>();
		for (var fileNumber = 0; fileNumber < number; fileNumber++) {
			var file = folder.newFile(Instant.now() + "." + extension);
			FileUtils.writeStringToFile(file, fileContent, Charset.defaultCharset());
			files.add(file);
		}
		return files;
	}

	/** Writes output to a list */
	private class InMemoryOutput implements OutputStrategy<Document> {

		List<Document> documents;

		@Override
		public byte[] write(TypeReference<List<Document>> typeReference, List<Document> documents)
				throws GDAServiceException {
			this.documents = documents;
			return null;
		}

		@Override
		public byte[] write(Document document) throws GDAServiceException {
			if (document != null) {
				documents = List.of(document);
			}
			return null;
		}
	}

}
