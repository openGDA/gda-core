/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.structure;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import uk.ac.diamond.daq.experiment.api.EventConstants;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentEvent;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentEvent.Transition;
import uk.ac.diamond.daq.experiment.api.structure.NodeInsertionRequest;
import uk.ac.diamond.osgi.services.ServiceProvider;
import uk.ac.gda.core.tool.URLFactory;
import uk.ac.gda.core.tool.spring.AcquisitionFileContext;
import uk.ac.gda.core.tool.spring.ExperimentContextFile;

/**
 * {@link ExperimentController} implementation for use with NeXus-based acquisitions.
 * <p>
 * The controller generates URLs for measurements and sub-measurements acquired during
 * the experiment. At the end of an experiment, a single overarching NeXus files is
 * created which links to every acquisition file generated.
 * <p>
 * The life-cycle of this controller is managed by Spring. Clients may therefore obtain it via
 * {@code SpringApplicationContextFacade.getBean(ExperimentController.class)}.
 *
 * @see NodeFileRequesterService
 */
@Component
public class NexusExperimentController implements ExperimentController {

	public static final String DEFAULT_EXPERIMENT_PREFIX = "UntitledExperiment";
	public static final String DEFAULT_ACQUISITION_PREFIX = "UntitledAcquisition";

	private static final Logger logger = LoggerFactory.getLogger(NexusExperimentController.class);

	private static final String FILE_EXTENSION = "nxs";

	/** Represents all the acquisitions within the experiment as a tree data structure */
	private ExperimentTree tree;

	/** Used to generate URLs in a consistent fashion */
	private URLFactory urlFactory = new URLFactory();

	private SafeUniqueNameGenerator nameGenerator = new SafeUniqueNameGenerator();

	/** Creates node NeXus files for the experiment and for multipart acquisitions */
	@Autowired
	private NodeFileRequesterService nodeFileRequesterService;

	@Autowired
	private AcquisitionFileContext acquisitionFileContext;

	private IPublisher<ExperimentEvent> publisher;

	private ExternalFileSubscriber processingFilesLinker = new ExternalFileSubscriber(this::registerExternalFile);

	@Override
	public URL startExperiment(String experimentName) throws ExperimentControllerException {
		if (isExperimentInProgress()) {
			throw new ExperimentControllerException("An experiment is already running");
		}

		setTree(new ExperimentTree.Builder()
					.withExperimentName(experimentName)
					.withActiveNode(createNode(experimentName, null, false))
					.build());

		var location = tree.getActiveNode().getLocation();

		publish(new ExperimentEvent(experimentName, location, Transition.STARTED));

		logger.debug("Experiment '{}' started: {}", experimentName, location);

		processingFilesLinker.attachListener();

		return location;
	}

	private void publish(ExperimentEvent event) throws ExperimentControllerException {
		if (publisher == null) {
			connectPublisher();
		}
		try {
			publisher.broadcast(event);
		} catch (EventException e) {
			throw new ExperimentControllerException("Error broadcasting event", e);
		}

	}

	private void connectPublisher() throws ExperimentControllerException {
		try {
			URI activeMqUri = new URI(LocalProperties.getActiveMQBrokerURI());
			publisher = ServiceProvider.getService(IEventService.class)
					.createPublisher(activeMqUri, EventConstants.EXPERIMENT_CONTROLLER_TOPIC);
		} catch (URISyntaxException e) {
			throw new ExperimentControllerException(e);
		}
	}

	private void setTree(ExperimentTree tree) {
		this.tree = tree;
	}

	@Override
	public String getExperimentName() {
		return isExperimentInProgress() ? tree.getExperimentName() : null;
	}

	@Override
	public void stopExperiment() throws ExperimentControllerException {
		ensureExperimentIsRunning();
		var experimentName = getExperimentName();
		while (isMultipartAcquisitionInProgress()) {
			stopMultipartAcquisition();
		}
		setTree(null);
		publish(new ExperimentEvent(experimentName, null, Transition.STOPPED));

		processingFilesLinker.dettachListener();

		logger.debug("Experiment '{}' stopped", experimentName);
	}

	@Override
	public boolean isExperimentInProgress() {
		return tree != null;
	}

	@Override
	public URL prepareAcquisition(String acquisitionName) throws ExperimentControllerException {
		return prepareAcquisition(acquisitionName, false);
	}

	@Override
	public URL startMultipartAcquisition(String acquisitionName) throws ExperimentControllerException {
		logger.debug("Preparing multipart acquisition '{}'", acquisitionName);
		return prepareAcquisition(acquisitionName, true);
	}

	/**
	 * multipart is {@code true} if this is the start of a multipart acquisition, else {@code false}
	 */
	private URL prepareAcquisition(String name, boolean multipart) throws ExperimentControllerException {
		ensureExperimentIsRunning();

		var parent = tree.getActiveNode();
		var acquisition = createNode(name, parent, multipart);

		tree.addChild(acquisition);

		if (multipart) {
			tree.moveDown(acquisition.getId());
		}

		addLinkToNodeFile(parent.getLocation(), acquisition.getName(), acquisition.getLocation());

		logger.debug("Acquisition '{}' prepared: {}", acquisition.getName(), acquisition.getLocation());

		return acquisition.getLocation();
	}

	private boolean isMultipartAcquisitionInProgress() {
		return !tree.getActiveNode().isRoot();
	}

	@Override
	public void stopMultipartAcquisition() throws ExperimentControllerException {
		if (isMultipartAcquisitionInProgress()) {
			tree.moveUp();
			logger.debug("Multipart acquisition stopped");
		} else {
			throw new ExperimentControllerException("No multipart acquisition to stop");
		}
	}

	/**
	 * @param name Will be formatted and made unique
	 * @param parent {@code null} for root node
	 * @param multipart {@code true} if node will have children
	 * @return
	 * @throws ExperimentControllerException
	 */
	private ExperimentNode createNode(String name, ExperimentNode parent, boolean multipart) throws ExperimentControllerException {
		try {
			if (parent == null) {
				return createInternalNode(getRootDir(), name, DEFAULT_EXPERIMENT_PREFIX, null);
			} else if (multipart) {
				var root = urlFactory.getParent(parent.getLocation());
				return createInternalNode(root, name, DEFAULT_ACQUISITION_PREFIX, parent.getId());
			} else {
				return createLeafNode(name, DEFAULT_ACQUISITION_PREFIX, parent.getId());
			}
		} catch (Exception e) {
			throw new ExperimentControllerException("Error creating experiment node", e);
		}
	}

	private ExperimentNode createInternalNode(URL root, String name, String defaultName, UUID parentId) throws Exception {
		var safeUniqueName = nameGenerator.safeUniqueName(name, defaultName);
		URL location = urlFactory.generateFileUrl(root, safeUniqueName, FILE_EXTENSION);
		return new ExperimentNode(safeUniqueName, location, parentId);
	}

	private ExperimentNode createLeafNode(String name, String defaultName, UUID parentId) throws Exception {
			URL url = urlFactory.generateUrl(ServiceProvider.getService(IFilePathService.class).getNextPath(null));
			String safeUniqueName = nameGenerator.safeUniqueName(name, defaultName, ServiceProvider.getService(IFilePathService.class).getScanNumber());
			return new ExperimentNode(safeUniqueName, url, parentId);
	}

	private void ensureExperimentIsRunning() throws ExperimentControllerException {
		if (!isExperimentInProgress()) {
			throw new ExperimentControllerException("Experiment is not running");
		}
	}

	private void addLinkToNodeFile(URL parent, String childName, URL childLocation) throws ExperimentControllerException {
		var job = createNodeFileCreationRequestJob(parent, childName, childLocation);
		processRequest(job);
	}

	private NodeInsertionRequest createNodeFileCreationRequestJob(URL parent, String childName, URL childLocation) {
		NodeInsertionRequest job = new NodeInsertionRequest();
		job.setNodeLocation(parent);
		job.setChildren(Map.of(childName, childLocation));
		return job;
	}

	private NodeInsertionRequest processRequest(NodeInsertionRequest request) throws ExperimentControllerException {
		try {
			request = nodeFileRequesterService.getNodeFileCreationRequestResponse(request);
			if (request.getStatus() == Status.FAILED) {
				throw new ExperimentControllerException(request.getMessage());
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ExperimentControllerException(e);
		} catch (EventException e) {
			throw new ExperimentControllerException(e);
		}
		return request;
	}

	private URL getRootDir() throws ExperimentControllerException {
		if (acquisitionFileContext != null) {
			return acquisitionFileContext.getExperimentContext().getContextFile(ExperimentContextFile.EXPERIMENTS_DIRECTORY);
		}
		throw new ExperimentControllerException("GDAContext not available");
	}

	private void registerExternalFile(String path) {
		if (path.endsWith(".nxs") || path.endsWith(".hdf5")) {

			logger.info("Registering external file {} to Experiment '{}'", path, getExperimentName());

			try {
				var pathSegments = path.split("/");
				var name = pathSegments[pathSegments.length - 1];
				addLinkToNodeFile(getExperimentNode().getLocation(), name, new File(path).toURI().toURL());
			} catch (ExperimentControllerException | MalformedURLException e) {
				logger.error("Error registering file {} to Experiment '{}'", path, getExperimentName());
			}
		}
	}

	private ExperimentNode getExperimentNode() {
		var node = tree.getActiveNode();
		while (node.getParentId() != null) {
			node = tree.getNode(node.getParentId());
		}
		return node;
	}

	/**
	 * Safe means replacing any non-alphanumeric characters with a safe character.
	 * Unique just means appending a unique* integer to a name to prevent file overwriting.
	 *
	 * <p>
	 * *{@link #safeUniqueName(String, String)} uses an internal {@link NumTracker};
	 * {@link #safeUniqueName(String, String, int)} assumes the caller can guarantee of uniqueness of the {@code int}
	 */
	private static class SafeUniqueNameGenerator {
		private static final Pattern INVALID_CHARACTERS_PATTERN = Pattern.compile("[^a-zA-Z0-9\\.\\-\\_]");

		/**
		 * Lazily initialised; call via {@link #getNumTracker()}
		 */
		private NumTracker internalNumTracker;

		private NumTracker getNumTracker() throws IOException {
			if (internalNumTracker == null) {
				var persistenceDir = ServiceProvider.getService(IFilePathService.class).getPersistenceDir();
				internalNumTracker = new NumTracker(NexusExperimentController.class.getName() + "_numtracker", persistenceDir);
			}
			return internalNumTracker;
		}

		/**
		 * Internal logic to guarantee uniqueness
		 */
		public String safeUniqueName(String rawName, String defaultName) throws IOException {
			return uniqueName(safeName(rawName, defaultName), getNumTracker().incrementNumber());
		}

		/**
		 * Caller responsible for providing unique {@code int}
		 */
		public String safeUniqueName(String rawName, String defaultName, int suffix) {
			return uniqueName(safeName(rawName, defaultName), suffix);
		}

		private String safeName(String rawName, String defaultName) {
			String value = StringUtils.isNotBlank(rawName) ? rawName : defaultName;
			String alphaNumericOnly = INVALID_CHARACTERS_PATTERN.matcher(value).replaceAll(" ");
			return Arrays.stream(alphaNumericOnly.split(" "))
				.map(String::trim)
				.filter(word -> !word.isEmpty())
				.map(this::capitalise)
				.collect(Collectors.joining());
		}

		private String uniqueName(String name, int suffix) {
			return String.format("%s-%d", name, suffix);
		}

		private String capitalise(String word) {
			String initial = word.substring(0, 1);
			return word.replaceFirst(initial, initial.toUpperCase());
		}
	}

}
