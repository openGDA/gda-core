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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.status.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;
import uk.ac.diamond.daq.experiment.api.structure.NodeFileCreationRequest;
import uk.ac.gda.core.tool.spring.AcquisitionFileContext;

/**
 * Controls the Experiment workflow.
 *
 * <p>
 * The file structure of an experiment is
 * <a href="https://confluence.diamond.ac.uk/display/DIAD/File+System#experiments">described in Confluence</a>
 * </p>
 * <p>
 * This class uses Spring to load from a property file the following properties
 * <ul>
 * <li><i>experiment.structure.job.request.topic</i>
 * <ul>
 * <li>Used to configure the request topic of the internal {@code IRequester}.</li>
 * <li>Default <i>uk.ac.diamond.daq.experiment.structure.job.request.topic</i></li>
 * </ul>
 * </li>
 * <li><i>experiment.structure.job.response.topic</i>
 * <ul>
 * <li>Used to configure the response topic of the internal {@code IRequester}.</li>
 * <li>Default <i>uk.ac.diamond.daq.experiment.structure.job.response.topic</i></li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 */
@Component
public class NexusExperimentController implements ExperimentController {

	public static final String DEFAULT_EXPERIMENT_PREFIX = "UntitledExperiment";
	public static final String DEFAULT_ACQUISITION_PREFIX = "UntitledAcquisition";

	private static final String FILE_EXTENSION = "nxs";

	private TreeNavigator tree;

	private URLFactory urlFactory = new URLFactory();

	private final NodeFileRequesterService nodeFileRequesterService;

	@Autowired
	private AcquisitionFileContext acquisitionFileContext;

	/**
	 * Default constructor for Spring
	 */
	@Autowired
	public NexusExperimentController(NodeFileRequesterService nodeFileRequesterService) {
		this.nodeFileRequesterService = nodeFileRequesterService;
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
		if (multipart)
			tree.moveDown(acquisition);
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

	private ExperimentNode createNode(String name, String defaultName, ExperimentNode parent)
			throws ExperimentControllerException {
		try {
			URL root = parent == null ? getRootDir() : urlFactory.getParent(parent.getFileLocation());
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

	private NodeFileCreationRequest closeNode(ExperimentNode node) throws ExperimentControllerException {
		NodeFileCreationRequest job = null;
		// no need to create a node file if the node is childless
		if (!node.hasChildren())
			return job;

		job = createNodeFileCreationRequestJob(node);

		try {
			job = nodeFileRequesterService.getNodeFileCreationRequestResponse(job);
			if (job.getStatus() == Status.FAILED) {
				throw new ExperimentControllerException(job.getMessage());
			}
		} catch (EventException | InterruptedException e) {
			throw new ExperimentControllerException(e);
		}
		return job;
	}

	private NodeFileCreationRequest createNodeFileCreationRequestJob(ExperimentNode node) {
		NodeFileCreationRequest job = new NodeFileCreationRequest();
		job.setNodeLocation(node.getFileLocation());
		job.setChildren(node.getChildren().stream().map(ExperimentNode::getFileLocation).collect(Collectors.toSet()));
		return job;
	}

	private URL getRootDir() throws ExperimentControllerException {
		if (getAcquisitionFileContext() != null) {
			return getAcquisitionFileContext().getContextFile(AcquisitionFileContext.ContextFile.ACQUISITION_EXPERIMENT_DIRECTORY);
		}
		throw new ExperimentControllerException("GDAContext not available");
	}

	private AcquisitionFileContext getAcquisitionFileContext() {
		return acquisitionFileContext;
	}
}
