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
import java.util.Set;

import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.NexusBuilderFactory;
import org.eclipse.dawnsci.nexus.builder.NexusFileBuilder;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.status.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.experiment.api.Activator;
import uk.ac.diamond.daq.experiment.api.structure.IndexFileCreationRequest;


/**
 * Builds an index NeXus file for the experiment, consisting of links to the individual NeXus files
 * for each acquisition performed dURLng the experiment.
 */
public class IndexFileCreator extends ExperimentStructureJobHandler<IndexFileCreationRequest> {
	
	private static final Logger logger = LoggerFactory.getLogger(IndexFileCreator.class);
	private NexusBuilderFactory factory;

	@Override
	public IndexFileCreationRequest process(IndexFileCreationRequest request) throws EventException {
		createIndexFile(request.getExperimentLocation(), request.getExperimentName(), request.getAcquisitions());
		return request;
	}

	private void createIndexFile(URL root, String name, Set<URL> nxsUrls) {
		String indexFilePath = root.getFile() + name + ".nxs";
		NexusFileBuilder nxsBuilder = getNexusBuilderFactory().newNexusFileBuilder(indexFilePath);
		NXobject nxsRoot = nxsBuilder.getNXroot();
		
		try {
			nxsUrls.forEach(acquisition ->
				nxsRoot.addExternalLink(getAcquisitionName(acquisition), acquisition.getPath(), "/entry/"));
			
			nxsBuilder.createFile(true);
			
			getBean().setIndexFileLocation(new URL(indexFilePath));
			
		} catch (NexusException | MalformedURLException e) {
			String error = "Could not create index file for experiment '"  + name + "'";
			logger.error(error, e);
			getBean().setStatus(Status.FAILED);
			getBean().setMessage(error);
		}
	}

	private String getAcquisitionName(URL acquisitionUrl) {
		return NexusUtils.getName(acquisitionUrl.getPath());
	}

	private NexusBuilderFactory getNexusBuilderFactory() {
		if (factory == null) {
			factory = Activator.getService(NexusBuilderFactory.class);
		}
		return factory;
	}

}
