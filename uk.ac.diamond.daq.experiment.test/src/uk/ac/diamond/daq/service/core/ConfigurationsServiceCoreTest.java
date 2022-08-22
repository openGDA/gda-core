package uk.ac.diamond.daq.service.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import uk.ac.diamond.daq.service.CommonDocumentService;
import uk.ac.diamond.daq.service.command.receiver.CollectionCommandReceiver;
import uk.ac.diamond.daq.service.command.receiver.FilesCollectionCommandReceiver;
import uk.ac.diamond.daq.service.command.strategy.OutputStrategy;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;
import uk.ac.gda.common.entity.Document;
import uk.ac.gda.common.entity.filter.DocumentFilter;
import uk.ac.gda.common.exception.GDAServiceException;

/**
 * Mostly tests that API calls correct methods on {@link CommonDocumentService}.
 */
@SuppressWarnings("unchecked")
public class ConfigurationsServiceCoreTest {

	private ConfigurationsServiceCore core;

	@Mock
	private CommonDocumentService documentService;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Rule
	public MockitoRule initialiseMocks = MockitoJUnit.rule();

	@Captor
	private ArgumentCaptor<DocumentFilter> filterCaptor;

	private CollectionCommandReceiver<Document> receiver;

	private Exception exception = new GDAServiceException("No.");

	@Before
	public void injectDocumentService() throws Exception {
		core = new ConfigurationsServiceCore();
		ReflectionTestUtils.setField(core, "documentService", documentService);

		// instantiated with every test because holds reference to mocks instantiated with every test
		receiver = new FilesCollectionCommandReceiver<>(Document.class, response.getOutputStream());
	}

	@Test
	public void selectDocument() throws GDAServiceException {
		var uuid = UUID.randomUUID();
		core.selectDocument(uuid, request, response);
		verify(documentService).selectDocument(eq(receiver), eq(uuid), any(OutputStrategy.class));
	}

	@Test
	public void selectDocumentFails() throws GDAServiceException {
		var uuid = UUID.randomUUID();
		doThrow(exception).when(documentService).selectDocument(eq(receiver), eq(uuid), any(OutputStrategy.class));
		checkException(() -> core.selectDocument(uuid, request, response));
	}

	@Test
	public void selectDocuments() throws GDAServiceException {
		core.selectDocuments(request, response);
		verify(documentService).selectDocuments(eq(receiver), any(DocumentFilter.class), any(OutputStrategy.class));
	}

	@Test
	public void selectDocumentsFails() throws GDAServiceException {
		doThrow(exception).when(documentService).selectDocuments(eq(receiver), any(DocumentFilter.class), any(OutputStrategy.class));
		checkException(() -> core.selectDocuments(request, response));
	}

	/** Tests the creation of the a useful {@link DocumentFilter} */
	@Test
	public void selectFilteredDocuments() throws GDAServiceException {
		/* let's request all saved tomography definitions */
		var filter = "tomo";
		when(request.getParameterMap()).thenReturn(Map.of("configurationType", new String[] {filter}));

		core.selectDocuments(request, response);

		verify(documentService).selectDocuments(eq(receiver), filterCaptor.capture(), any(OutputStrategy.class));

		var documentFilter = filterCaptor.getValue();
		assertThat(documentFilter.getFileExtension(), is(equalTo(filter)));
	}

	@Test
	public void insertDocument() throws GDAServiceException, IOException {
		var document = mock(Document.class);
		var type = AcquisitionConfigurationResourceType.PLAN;

		// redefine receiver to include type
		receiver = new FilesCollectionCommandReceiver<>(Document.class, response.getOutputStream(), type);

		core.insertDocument(document, type, request, response);

		verify(documentService).insertDocument(eq(receiver), eq(document), any(OutputStrategy.class));
	}

	@Test
	public void insertDocumentUnknownFailure() throws GDAServiceException {
		makeInsertDocumentFail(exception);
		checkException(() -> core.insertDocument(mock(Document.class), AcquisitionConfigurationResourceType.DEFAULT, request, response));
	}

	@Test
	public void inserDocumentConflictFailure() throws GDAServiceException {
		makeInsertDocumentFail(new GDAServiceException(new FileAlreadyExistsException(null)));
		checkException(() -> core.insertDocument(mock(Document.class), AcquisitionConfigurationResourceType.DEFAULT, request, response),
				HttpStatus.CONFLICT);
	}

	private void makeInsertDocumentFail(Exception exceptionToThrow) throws GDAServiceException {
		doThrow(exceptionToThrow).when(documentService).insertDocument(any(CollectionCommandReceiver.class), any(Document.class), any(OutputStrategy.class));
	}

	@Test
	public void deleteDocument() throws GDAServiceException {
		var uuid = UUID.randomUUID();
		core.deleteDocument(uuid, request, response);
		verify(documentService).deleteDocument(eq(receiver), eq(uuid), any(OutputStrategy.class));
	}

	@Test
	public void deleteDocumentFails() throws GDAServiceException {
		var uuid = UUID.randomUUID();
		doThrow(exception).when(documentService).deleteDocument(eq(receiver), eq(uuid), any(OutputStrategy.class));
		checkException(() -> core.deleteDocument(uuid, request, response));
	}

	private void checkException(ThrowingRunnable call) {
		checkException(call, HttpStatus.PRECONDITION_FAILED);
	}

	private void checkException(ThrowingRunnable call, HttpStatus status) {
		var e = assertThrows(ResponseStatusException.class, call);
		assertThat(e.getStatus(), is(status));
	}

}
