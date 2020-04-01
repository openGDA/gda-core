package uk.ac.diamond.daq.experiment.structure;

import static uk.ac.diamond.daq.experiment.api.remote.EventProperties.EXPERIMENT_STRUCTURE_JOB_REQUEST_TOPIC;
import static uk.ac.diamond.daq.experiment.api.remote.EventProperties.EXPERIMENT_STRUCTURE_JOB_RESPONSE_TOPIC;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.springframework.stereotype.Controller;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.experiment.api.Activator;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;
import uk.ac.diamond.daq.experiment.api.structure.IndexFileCreationRequest;

@Controller("experimentController")
public class ExperimentControllerImpl implements ExperimentController {

	/**
	 * A GDA property to defines the experiment root folder. The property value may be either absolute URL <blockquote>
	 *
	 * <pre>
	 *     file://aPath/subFolder
	 * </pre>
	 *
	 * </blockquote>
	 *
	 * or relative to {@link IFilePathService#getVisitDir()}
	 *
	 * <blockquote>
	 *
	 * <pre>
	 * aPath/experiment
	 * </pre>
	 *
	 * </blockquote> All the missing directories are created.
	 */
	public static final String EXPERIMENT_CONTROLLER_ROOT = "experiment.root";

	public static final String DEFAULT_EXPERIMENT_PREFIX = "UntitledExperiment";
	public static final String DEFAULT_ACQUISITION_PREFIX = "UntitledAcquisition";

	private static final Pattern INVALID_CHARACTERS_PATTERN = Pattern.compile("[^a-zA-Z0-9\\.\\-\\_]");

	private final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy_mm_dd_HH_mm_ss");

	private ExperimentResource experiment;

	private IFilePathService filePathService;
	private IRequester<IndexFileCreationRequest> experimentStructureJobRequester;

	public ExperimentControllerImpl() {
		// No-arg constructor for Spring
	}

	/**
	 * Constructor for tests
	 */
	ExperimentControllerImpl(IFilePathService filePathService, IRequester<IndexFileCreationRequest> submitter) {
		super();
		this.filePathService = filePathService;
		this.experimentStructureJobRequester = submitter;
	}

	@Override
	public URL startExperiment(String experimentName) throws ExperimentControllerException {
		if (isStarted()) {
			throw new ExperimentControllerException("Experiment already running");
		}
		String validatedName = validateName(experimentName, DEFAULT_EXPERIMENT_PREFIX);
		experiment = new ExperimentResource(validatedName, createLocation(validatedName, getRootFolder()));
		return experiment.getExperimentURL();
	}

	@Override
	public URL createAcquisitionLocation(String acquisitionName) throws ExperimentControllerException {
		if (!isStarted()) {
			throw new ExperimentControllerException("Start Experiment first.");
		}
		String validatedName = validateName(acquisitionName, DEFAULT_ACQUISITION_PREFIX);
		URL acquisitionUrl = createLocation(validatedName, experiment.getExperimentURL());
		experiment.addAcquisition(acquisitionUrl);
		return acquisitionUrl;
	}

	@Override
	public void stopExperiment() throws ExperimentControllerException {
		if (!isStarted()) {
			throw new ExperimentControllerException("Start Experiment first.");
		}
		try {
			createIndexFile();
		} catch (EventException | InterruptedException e) {
			throw new ExperimentControllerException("Could not create index file", e);
		}
		experiment = null;
	}

	@Override
	public boolean isStarted() {
		return experiment != null;
	}

	private URL getRootFolder() throws ExperimentControllerException {
		URL root = createURL("file", null, formatAsDirectory(getFilePathService().getVisitDir()));
		if (LocalProperties.contains(EXPERIMENT_CONTROLLER_ROOT)) {
			String rootDir = LocalProperties.get(EXPERIMENT_CONTROLLER_ROOT);
			if (rootDir.startsWith("/")) {
				// EXPERIMENT_CONTROLLER_ROOT is an absolute path
				root = createURL("file", null, formatAsDirectory(rootDir));
			} else {
				// EXPERIMENT_CONTROLLER_ROOT is a relative path
				root = createURL(root, formatAsDirectory(rootDir));
			}
		}
		return root;
	}


	private String validateName(String value, String defaultValue) {
		value = value == null ? defaultValue : value;
		String alphaNumericOnly = INVALID_CHARACTERS_PATTERN.matcher(value).replaceAll(" ");
		return Arrays.stream(alphaNumericOnly.split(" "))
			.map(String::trim)
			.filter(word -> !word.isEmpty())
			.map(this::capitalise)
			.collect(Collectors.joining());
	}

	private String capitalise(String word) {
		String initial = word.substring(0, 1);
		return word.replaceFirst(initial, initial.toUpperCase());
	}

	private URL createURL(String scheme, String host, String path) throws ExperimentControllerException {
		try {
			return new URL(scheme, host, path);
		} catch (MalformedURLException e) {
			throw new ExperimentControllerException("Path syntax error", e);
		}
	}

	private URL createURL(URL base, String path) throws ExperimentControllerException {
		try {
			return new URL(base, path);
		} catch (MalformedURLException e) {
			throw new ExperimentControllerException("Path syntax error", e);
		}
	}

	private URL createLocation(String name, URL root) throws ExperimentControllerException {
		try {
			URL newURL = new URL(root, name + timestamp() + "/");
			if (new File(newURL.toURI()).mkdirs()) {
				return newURL;
			}
			throw new ExperimentControllerException("Cannot create location '" + newURL.getFile() + "'");
		} catch (URISyntaxException | MalformedURLException e) {
			throw new ExperimentControllerException("Error creating URL", e);
		}
	}

	private String timestamp() {
		return timestampFormat.format(new Date());
	}

	private final IFilePathService getFilePathService() {
		if (filePathService == null) {
			filePathService = Activator.getService(IFilePathService.class);
		}
		return filePathService;
	}

	private void createIndexFile() throws EventException, InterruptedException, ExperimentControllerException {
		IndexFileCreationRequest job = new IndexFileCreationRequest();
		job.setExperimentName(experiment.getExperimentName());
		job.setExperimentLocation(experiment.getExperimentURL());
		job.setAcquisitions(getNeXusLocations());
		IndexFileCreationRequest response = getExperimentStructureJobRequester().post(job);
		if (response.getStatus() == Status.FAILED) {
			throw new ExperimentControllerException(response.getMessage());
		}
	}

	/**
	 * Returns the URLs of NeXus files created as part of this resource.
	 * Traverses all acquisitions locations registered with {@link #addAcquisition(URL)}
	 * looking for those containing exactly one NeXus file.
	 */
	private Set<URL> getNeXusLocations() {
		return experiment.getAcquisitions().stream()
			.map(URL::getFile)
			.map(File::new)
			.map(dir -> dir.listFiles(this::nexusFilter))
			.filter(nxsFiles -> nxsFiles.length == 1)
			.map(nxsFiles -> nxsFiles[0])
			.map(File::toURI)
			.map(this::uriToUrl)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}

	/**
	 * Stream-safe URI to URL conversion
	 * Returns null if fails; remember to filter!
	 */
	private URL uriToUrl(URI uri) {
		try {
			return uri.toURL();
		} catch (MalformedURLException e) {
			return null;
		}
	}

	private boolean nexusFilter(@SuppressWarnings("unused") File directory, String filename) {
		if (filename.lastIndexOf('.') > 0) {
			int lastIndex = filename.lastIndexOf('.');
			return filename.substring(lastIndex).equals(".nxs");
        }
        return false;
	}

	private IRequester<IndexFileCreationRequest> getExperimentStructureJobRequester() throws EventException {
		if (experimentStructureJobRequester == null) {
			try {
				URI activemqURL = new URI(LocalProperties.getActiveMQBrokerURI());
				IEventService eventService = Activator.getService(IEventService.class);
				experimentStructureJobRequester = eventService.createRequestor(activemqURL,
														 LocalProperties.get(EXPERIMENT_STRUCTURE_JOB_REQUEST_TOPIC),
														 LocalProperties.get(EXPERIMENT_STRUCTURE_JOB_RESPONSE_TOPIC));
			} catch (URISyntaxException e) {
				throw new EventException("Cannot create submitter", e);
			}
		}
		return experimentStructureJobRequester;
	}

	private String formatAsDirectory(String dir) {
		String ret = dir;
		if (!dir.endsWith("/")) {
			ret = dir + "/";
		}
		return ret;
	}

}
