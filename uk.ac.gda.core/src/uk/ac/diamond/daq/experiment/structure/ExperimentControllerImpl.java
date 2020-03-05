package uk.ac.diamond.daq.experiment.structure;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.scanning.api.scan.IFilePathService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.experiment.api.Activator;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;

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
	private static final String DEFAULT_EXPERIMENT_PREFIX = "UntitledExperiment";
	private static final String DEFAULT_ACQUISITION_PREFIX = "UntitledAcquisition";

	private static final Logger logger = LoggerFactory.getLogger(ExperimentControllerImpl.class);

	private URL controllerRootFolder;
	private Optional<URL> experimentFolder = Optional.empty();
	private Optional<URL> lastAcquisitionFolder = Optional.empty();

	private IFilePathService filePathService;

	public ExperimentControllerImpl() {
		super();
	}

	public ExperimentControllerImpl(IFilePathService filePathService) {
		super();
		this.filePathService = filePathService;
	}

	@Override
	public URL startExperiment(String experimentName) throws ExperimentControllerException {
		if (isStarted()) {
			throw new ExperimentControllerException("Experiment already running");
		}
		String validPath = validate(experimentName, DEFAULT_EXPERIMENT_PREFIX);
		URL experimentUrl = createExperiment(validPath);
		logger.info("created experiment url: {}", experimentUrl);
		experimentFolder = Optional.of(experimentUrl);
		return experimentUrl;
	}

	@Override
	public URL createAcquisitionLocation(String acquisitionName) throws ExperimentControllerException {
		if (!isStarted()) {
			throw new ExperimentControllerException("Start Experiment first.");
		}
		String validPath = validate(acquisitionName, DEFAULT_ACQUISITION_PREFIX);
		URL acquisitionUrl = createAcquisition(validPath);
		logger.info("created acquisition url: {}", acquisitionUrl);
		lastAcquisitionFolder = Optional.of(acquisitionUrl);
		return acquisitionUrl;
	}

	@Override
	public void stopExperiment() throws ExperimentControllerException {
		if (!isStarted()) {
			throw new ExperimentControllerException("Start Experiment first.");
		}
		experimentFolder = Optional.empty();
		lastAcquisitionFolder = Optional.empty();
		wrapUpExperiment();
	}

	@Override
	public boolean isStarted() {
		return experimentFolder.isPresent();
	}

	@Override
	public URL getControllerRootLocation() throws ExperimentControllerException {
		if (controllerRootFolder == null) {
			controllerRootFolder = getRootFolder();
		}
		return controllerRootFolder;
	}

	@Override
	public URL getExperimentLocation() {
		return experimentFolder.orElse(null);
	}

	@Override
	public URL getLastAcquisitionLocation() {
		return lastAcquisitionFolder.orElse(null);
	}

	private static final Pattern INVALID_CHARACTERS_PATTERN = Pattern.compile("[^a-zA-Z0-9\\.\\-\\_]");

	private String validate(String value, String defaultValue) throws ExperimentControllerException {
		value = (value == null ? defaultValue : value) + getTimestamp();
		String alphaNumericOnly = INVALID_CHARACTERS_PATTERN.matcher(value).replaceAll(" ");
		return Arrays.stream(alphaNumericOnly.split(" "))
			.map(String::trim)
			.filter(word -> !word.isEmpty())
			.map(word -> {
				String initial = word.substring(0, 1);
				return word.replaceFirst(initial, initial.toUpperCase());
			}).reduce((a, b) -> a + b).orElseThrow(ExperimentControllerException::new);
	}

	private String getTimestamp() {
		return Instant.now().toString();
	}

	private URL createExperiment(String spec) throws ExperimentControllerException {
		return createFolder(getRootFolder(), formatAsDirectory(spec));
	}

	private URL createAcquisition(String spec) throws ExperimentControllerException {
		return createFolder(getExperimentLocation(), formatAsDirectory(spec));
	}

	private URL createFolder(URL base, String spec) throws ExperimentControllerException {
		try {
			URL newUrl = new URL(base, spec);
			if (new File(newUrl.toURI()).mkdirs()) {
				return newUrl;
			}
			throw new ExperimentControllerException("Cannot create folder " + newUrl.getPath());
		} catch (MalformedURLException e) {
			throw new ExperimentControllerException("Malformed path", e);
		} catch (URISyntaxException e) {
			throw new ExperimentControllerException("Path syntax error", e);
		}
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

	private URL createURL(String protocol, String host, String path) throws ExperimentControllerException {
		try {
			return new URL(protocol, host, path);
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

	private final IFilePathService getFilePathService() {
		if (filePathService == null) {
			filePathService = Activator.getService(IFilePathService.class);
		}
		return filePathService;
	}

	private void wrapUpExperiment() {
		// TODO
	}

	private String formatAsDirectory(String dir) {
		String ret = dir;
		if (!dir.endsWith("/")) {
			ret = dir + "/";
		}
		return ret;
	}

	@Override
	public String getDefaultExperimentName() {
		return DEFAULT_EXPERIMENT_PREFIX;
	}

	@Override
	public String getDefaultAcquisitionName() {
		return DEFAULT_ACQUISITION_PREFIX;
	}
}
