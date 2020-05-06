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

import static uk.ac.diamond.daq.experiment.api.remote.EventProperties.EXPERIMENT_STRUCTURE_JOB_REQUEST_TOPIC;
import static uk.ac.diamond.daq.experiment.api.remote.EventProperties.EXPERIMENT_STRUCTURE_JOB_RESPONSE_TOPIC;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.springframework.stereotype.Component;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.experiment.api.Activator;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;
import uk.ac.diamond.daq.experiment.api.structure.NodeFileCreationRequest;


@Component
public class NexusExperimentController implements ExperimentController {

	/**
	 * A GDA property to define the experiment root folder.
	 * The property value may be either absolute URL
	 *
	 * <blockquote>
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
	 * </blockquote>
	 *
	 * All the missing directories are created.
	 */
	public static final String EXPERIMENT_CONTROLLER_ROOT = "experiment.root.directory";

	public static final String DEFAULT_EXPERIMENT_PREFIX = "UntitledExperiment";
	public static final String DEFAULT_ACQUISITION_PREFIX = "UntitledAcquisition";

	private static final String FILE_EXTENSION = "nxs";

	private TreeNavigator tree;

	private URLFactory urlFactory = new URLFactory();

	private IFilePathService filePathService;
	private IRequester<NodeFileCreationRequest> nodeFileRequester;


	/**
	 * Default constructor for Spring
	 */
	public NexusExperimentController() {
		// Nothing else to set up
	}

	/**
	 * Package-private constructor for tests
	 */
	NexusExperimentController(IFilePathService filePathService, IRequester<NodeFileCreationRequest> requester) {
		this.filePathService = filePathService;
		this.nodeFileRequester = requester;
	}

	@Override
	public URL startExperiment(String experimentName) throws ExperimentControllerException {
		if (isExperimentInProgress()) {
			throw new ExperimentControllerException("An experiment is already running");
		}

		tree = new TreeNavigator(createNode(experimentName, DEFAULT_EXPERIMENT_PREFIX, null));
		return tree.getCurrentNode().getFileLocation();
	}

	@Override
	public void stopExperiment() throws ExperimentControllerException {
		ensureExperimentIsRunning();
		while (isMultipartAcquisitionInProgress()) {
			stopMultipartAcquisition();
		}
		closeNode(tree.getCurrentNode());
		tree = null;
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
		return prepareAcquisition(acquisitionName, true);
	}

	/**
	 * multipart is {@code true} if this is the start of a multipart acquisition, else {@code false}
	 */
	private URL prepareAcquisition(String name, boolean multipart) throws ExperimentControllerException {
		ensureExperimentIsRunning();
		ExperimentNode acquisition = createNode(name, DEFAULT_ACQUISITION_PREFIX, tree.getCurrentNode());
		tree.getCurrentNode().addChild(acquisition);
		if (multipart) tree.moveDown(acquisition);
		return acquisition.getFileLocation();
	}

	private boolean isMultipartAcquisitionInProgress() {
		return !tree.getCurrentNode().isRoot();
	}

	@Override
	public void stopMultipartAcquisition() throws ExperimentControllerException {
		if (isMultipartAcquisitionInProgress()) {
			closeNode(tree.getCurrentNode());
			tree.moveUp();
		} else {
			throw new ExperimentControllerException("No multipart acquisition to stop");
		}
	}

	private ExperimentNode createNode(String name, String defaultName, ExperimentNode parent) throws ExperimentControllerException {
		try {
			URL root = parent == null ? getDataDirectory() : urlFactory.getParent(parent.getFileLocation());
			URL file = urlFactory.generateUniqueFile(root, name, defaultName, FILE_EXTENSION);
			return new ExperimentNode(file, parent);
		} catch (MalformedURLException e) {
			throw new ExperimentControllerException("Could not create experiment node", e);
		}
	}

	private void ensureExperimentIsRunning() throws ExperimentControllerException {
		if (!isExperimentInProgress()) {
			throw new ExperimentControllerException("Experiment is not running");
		}
	}

	private URL getDataDirectory() throws MalformedURLException {
		URL root = urlFactory.generateUrl(getFilePathService().getVisitDir());
		if (LocalProperties.contains(EXPERIMENT_CONTROLLER_ROOT)) {
			String rootDir = LocalProperties.get(EXPERIMENT_CONTROLLER_ROOT);
			if (rootDir.startsWith("/")) {
				// property describes an absolute path
				root = urlFactory.generateUrl(rootDir);
			} else {
				// property describes a relative path
				root = urlFactory.generateUrl(root, rootDir);
			}
		}
		return root;
	}

	private void closeNode(ExperimentNode node) throws ExperimentControllerException {

		// no need to create a node file if the node is childless
		if (!node.hasChildren()) return;

		NodeFileCreationRequest job = new NodeFileCreationRequest();
		job.setNodeLocation(node.getFileLocation());
		job.setChildren(node.getChildren().stream()
				.map(ExperimentNode::getFileLocation)
				.collect(Collectors.toSet()));

		try {
			NodeFileCreationRequest response = getNodeFileRequester().post(job);
			if (response.getStatus() == Status.FAILED) {
				throw new ExperimentControllerException(response.getMessage());
			}
		} catch (EventException | InterruptedException e) {
			throw new ExperimentControllerException(e);
		}
	}

	private final IFilePathService getFilePathService() {
		if (filePathService == null) {
			filePathService = Activator.getService(IFilePathService.class);
		}
		return filePathService;
	}

	private IRequester<NodeFileCreationRequest> getNodeFileRequester() throws EventException {
		if (nodeFileRequester == null) {
			try {
				URI activemqURL = new URI(LocalProperties.getActiveMQBrokerURI());
				IEventService eventService = Activator.getService(IEventService.class);
				nodeFileRequester = eventService.createRequestor(activemqURL,
														 LocalProperties.get(EXPERIMENT_STRUCTURE_JOB_REQUEST_TOPIC),
														 LocalProperties.get(EXPERIMENT_STRUCTURE_JOB_RESPONSE_TOPIC));
				nodeFileRequester.setTimeout(5, TimeUnit.SECONDS);
			} catch (URISyntaxException e) {
				throw new EventException("Cannot create submitter", e);
			}
		}
		return nodeFileRequester;
	}

}
