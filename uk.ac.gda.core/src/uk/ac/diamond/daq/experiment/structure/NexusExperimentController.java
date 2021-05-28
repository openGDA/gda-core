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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.status.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentNodeExistsException;
import uk.ac.diamond.daq.experiment.api.structure.NodeFileCreationRequest;
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

	public final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	public final SimpleDateFormat daytimeFormat = new SimpleDateFormat("DDHHmmss");


	/** Represents all the acquisitions within the experiment as a tree data structure */
	private ExperimentTree tree;

	/** Backs up the state of the experiment every time it is modified */
	@Autowired
	private ExperimentTreeCache experimentTreeCache;

	/** Used to generate URLs in a consistent fashion */
	private URLFactory urlFactory = new URLFactory();

	/** Creates node NeXus files for the experiment and for multipart acquisitions */
	@Autowired
	private NodeFileRequesterService nodeFileRequesterService;

	@Autowired
	private AcquisitionFileContext acquisitionFileContext;

	private NexusExperimentController() {}

	@PostConstruct
	private void restoreState() {
		try {
			experimentTreeCache.restore().ifPresent(this::setTree);
		} catch (IOException e) {
			logger.error("Could not restore previous open experiment", e);
		}
	}

	@Override
	public URL startExperiment(String experimentName) throws ExperimentControllerException {
		if (isExperimentInProgress()) {
			throw new ExperimentControllerException("An experiment is already running");
		}

		setTree(new ExperimentTree.Builder()
					.withExperimentName(experimentName)
					.withActiveNode(createNode(experimentName, DEFAULT_EXPERIMENT_PREFIX))
					.build());
		return tree.getActiveNode().getLocation();
	}

	private void setTree(ExperimentTree tree) {
		this.tree = tree;
		cacheState();
	}

	@Override
	public String getExperimentName() {
		return isExperimentInProgress() ? tree.getExperimentName() : null;
	}

	@Override
	public void stopExperiment() throws ExperimentControllerException {
		ensureExperimentIsRunning();
		while (isMultipartAcquisitionInProgress()) {
			stopMultipartAcquisition();
		}
		closeNode(tree.getActiveNode());
		setTree(null);
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
		ExperimentNode acquisition = createNode(name, DEFAULT_ACQUISITION_PREFIX, tree.getActiveNode());
		tree.addChild(acquisition);
		if (multipart)
			tree.moveDown(acquisition.getId());
		cacheState();
		return acquisition.getLocation();
	}

	private boolean isMultipartAcquisitionInProgress() {
		return !tree.getActiveNode().isRoot();
	}

	@Override
	public void stopMultipartAcquisition() throws ExperimentControllerException {
		if (isMultipartAcquisitionInProgress()) {
			closeNode(tree.getActiveNode());
			tree.moveUp();
			cacheState();
		} else {
			throw new ExperimentControllerException("No multipart acquisition to stop");
		}
	}

	/**
	 * Creates the root node.
	 *
	 * @param name
	 * @param defaultName
	 * @return
	 * @throws ExperimentControllerException if the experiment cannot be created
	 * @throws ExperimentNodeExistsException if the experiment node already exists
	 */
	private ExperimentNode createNode(String name, String defaultName) throws ExperimentControllerException {
		try {
			URL url = getNodeUrl(getRootDir(), name, defaultName, dateFormat);
			if (Files.exists(Paths.get(url.toURI()).getParent(), LinkOption.NOFOLLOW_LINKS)) {
				throw new ExperimentNodeExistsException("Already exists an experiment with the same name");
			}
			return createNode(url, null);
		} catch (MalformedURLException e) {
			throw new ExperimentControllerException("Could not create experiment node", e);
		} catch (URISyntaxException e) {
			logger.error("Cannot transform URL to URI", e);
		}
		return null;
	}

	private ExperimentNode createNode(String name, String defaultName, ExperimentNode parent) throws ExperimentControllerException {
		try {
			URL url = getNodeUrl(urlFactory.getParent(parent.getLocation()), name, defaultName, daytimeFormat);
			return createNode(url, parent.getId());
		} catch (MalformedURLException e) {
			throw new ExperimentControllerException("Could not create experiment node", e);
		}
	}

	private URL getNodeUrl(URL root, String name, String defaultName, DateFormat format) throws MalformedURLException {
		return urlFactory.generateFormattedNameFile(root, name, defaultName, FILE_EXTENSION, format);
	}

	private ExperimentNode createNode(URL location, UUID parentId) {
		return new ExperimentNode(location, parentId);
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
		} catch (EventException | InterruptedException e) { // NOSONAR please, since we are rethrowing
			throw new ExperimentControllerException(e);
		}
		return job;
	}

	private NodeFileCreationRequest createNodeFileCreationRequestJob(ExperimentNode node) {
		NodeFileCreationRequest job = new NodeFileCreationRequest();
		job.setNodeLocation(node.getLocation());
		job.setChildren(node.getChildren().stream()
										.map(tree::getNode)
										.map(ExperimentNode::getLocation)
										.collect(Collectors.toSet()));
		return job;
	}

	private URL getRootDir() throws ExperimentControllerException {
		if (getAcquisitionFileContext() != null) {
			return getAcquisitionFileContext().getExperimentContext().getContextFile(ExperimentContextFile.EXPERIMENTS_DIRECTORY);
		}
		throw new ExperimentControllerException("GDAContext not available");
	}

	private void cacheState() {
		try {
			experimentTreeCache.store(tree);
		} catch (IOException e) {
			logger.error("Could not cache experiment state", e);
		}
	}

	private AcquisitionFileContext getAcquisitionFileContext() {
		return acquisitionFileContext;
	}
}
