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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.ServiceHolder;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IRequestHandler;
import org.eclipse.scanning.api.event.status.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.experiment.api.structure.NodeInsertionRequest;


/**
 * Builds a node NeXus file consisting of a single NXentry
 * composed of links to the individual NeXus files
 */
public class NodeFileCreator implements IRequestHandler<NodeInsertionRequest> {
	
	private static final Logger logger = LoggerFactory.getLogger(NodeFileCreator.class);
	
	private final NodeInsertionRequest request;
	private final IPublisher<NodeInsertionRequest> publisher;
	
	/** 
	 * Constructor arguments are here to fulfil the {@link IRequestHandler} contract,
	 * but are not used
	 *  
	 * @param request
	 * @param publisher
	 */
	NodeFileCreator(NodeInsertionRequest request, IPublisher<NodeInsertionRequest> publisher) {
		this.request = request;
		this.publisher = publisher;
	}

	@Override
	public NodeInsertionRequest process(NodeInsertionRequest request) throws EventException {
		handleRequest(request.getNodeLocation(), request.getChildren());
		return request;
	}

	private void handleRequest(URL file, Set<URL> children) {
		
		if (children.isEmpty()) {
			getBean().setMessage("Nothing to link; won't do anything");
			getBean().setStatus(Status.COMPLETE);
			return;
		}
		
		getBean().setStatus(Status.RUNNING);
		
		try (var nexus = ServiceHolder.getNexusFileFactory().newNexusFile(file.getPath(), true)) {
			getParentDirectory(file.getPath()).mkdirs();
			nexus.openToWrite(true);
			
			for (var child : children) {
				nexus.linkExternal(getRelativeURI(file, child), getDestinationNodePath(child), true);
			}
			
			nexus.flush();
			getBean().setStatus(Status.COMPLETE);
		} catch (NexusException | URISyntaxException e) {
			String error = "Could not create node file '"  + file.getPath() + "'";
			logger.error(error, e);
			getBean().setStatus(Status.FAILED);
			getBean().setMessage(error);
		}
	}
	
	private File getParentDirectory(String filepath) {
		return Paths.get(filepath).getParent().toFile();
	}
	
	/**
	 * Returns a URI describing the relative path of {@code child} to {@code parent},
	 * and the internal HDF path {@code /entry/}.
	 */
	private URI getRelativeURI(URL parent, URL child) throws URISyntaxException {
		return new URI(getRelativePath(parent.getPath(), child.getPath()) + "#/entry/");
	}
	
	private String getRelativePath(String parentFilePath, String childFilePath) {
		var parentDirectory = Path.of(parentFilePath).getParent();
		var child = Path.of(childFilePath);
		return parentDirectory.relativize(child).toString();
	}

	private String getDestinationNodePath(URL location) {
		return "/entry/" + NexusUtils.getName(location.getPath());
	}

	@Override
	public NodeInsertionRequest getBean() {
		return request;
	}

	@Override
	public IPublisher<NodeInsertionRequest> getPublisher() {
		return publisher;
	}

}
