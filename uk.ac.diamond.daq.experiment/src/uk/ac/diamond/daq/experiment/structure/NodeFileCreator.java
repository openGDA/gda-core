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

import java.net.URL;
import java.util.Set;

import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.NexusBuilderFactory;
import org.eclipse.dawnsci.nexus.builder.NexusFileBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusBuilderFile;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IRequestHandler;
import org.eclipse.scanning.api.event.status.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.experiment.api.Activator;
import uk.ac.diamond.daq.experiment.api.structure.NodeFileCreationRequest;


/**
 * Builds a node NeXus file consisting of a single NXentry
 * composed of links to the individual NeXus files
 */
public class NodeFileCreator implements IRequestHandler<NodeFileCreationRequest> {
	
	private static final Logger logger = LoggerFactory.getLogger(NodeFileCreator.class);
	private NexusBuilderFactory factory;
	
	private final NodeFileCreationRequest request;
	private final IPublisher<NodeFileCreationRequest> publisher;
	
	/** 
	 * Constructor arguments are here to fulfil the {@link IRequestHandler} contract,
	 * but are not used
	 *  
	 * @param request
	 * @param publisher
	 */
	NodeFileCreator(NodeFileCreationRequest request, IPublisher<NodeFileCreationRequest> publisher) {
		this.request = request;
		this.publisher = publisher;
	}

	@Override
	public NodeFileCreationRequest process(NodeFileCreationRequest request) throws EventException {
		createIndexFile(request.getNodeLocation(), request.getChildren());
		return request;
	}

	private void createIndexFile(URL file, Set<URL> children) {
		
		if (children.isEmpty()) {
			getBean().setMessage("Nothing to link; won't do anything");
			return;
		}
		
		getBean().setStatus(Status.RUNNING);
		NexusFileBuilder nxsBuilder = getNexusBuilderFactory().newNexusFileBuilder(file.getPath());
		
		try {
			final NXobject entry = nxsBuilder.newEntry().getNXentry();
			
			children.forEach(child -> 
				entry.addExternalLink(getLeafName(child), child.getPath(), "/entry/"));
			
			NexusBuilderFile nodeFile = nxsBuilder.createFile(true);
			nodeFile.close();
			getBean().setStatus(Status.COMPLETE);
			
		} catch (NexusException e) {
			String error = "Could not create node file '"  + file.getPath() + "'";
			logger.error(error, e);
			getBean().setStatus(Status.FAILED);
			getBean().setMessage(error);
		}
	}

	private String getLeafName(URL location) {
		return NexusUtils.getName(location.getPath());
	}

	private NexusBuilderFactory getNexusBuilderFactory() {
		if (factory == null) {
			factory = Activator.getService(NexusBuilderFactory.class);
		}
		return factory;
	}

	@Override
	public NodeFileCreationRequest getBean() {
		return request;
	}

	@Override
	public IPublisher<NodeFileCreationRequest> getPublisher() {
		return publisher;
	}

}
