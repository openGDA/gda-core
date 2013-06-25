package org.opengda.detector.electronanalyser.scan;

import gda.scan.ScanPositionProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.DocumentRoot;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence;
import org.opengda.detector.electronanalyser.utils.RegionDefinitionResourceUtil;
import org.opengda.detector.electronanalyser.utils.SequenceEditingDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegionPositionProvider implements ScanPositionProvider {
	List<Region> points = new ArrayList<Region>();
	// RegionDefinitionResourceUtil regionResourceutil = new RegionDefinitionResourceUtil();
	private static final Logger logger = LoggerFactory.getLogger(RegionPositionProvider.class);

	public RegionPositionProvider(String filename) {

		logger.debug("Sequence file changed to {}{}", FilenameUtils.getFullPath(filename), FilenameUtils.getName(filename));
		try {
			// List<Region> regions = regionResourceutil.getRegions(filename);
			Resource resource = getResource(filename);
			resource.unload();
			resource.load(Collections.emptyMap());

			Sequence sequence = getSequence(resource);
			List<Region> regions = getRegions(sequence);

			for (Region region : regions) {
				if (region.isEnabled()) { // only add selected/enabled region to the list of points to collect
					this.points.add(region);
				}
			}
		} catch (Exception e) {
			logger.error("Cannot get region list from file.", e);
		}
	}
	public RegionPositionProvider(Region region) {
		this.points.clear();
		this.points.add(region);
	}

	@Override
	public Object get(int index) {
		return points.get(index);
	}

	@Override
	public int size() {
		return points.size();
	}

	public Resource getResource(String fileName) throws Exception {
		ResourceSet resourceSet = getResourceSet();
		File seqFile = new File(fileName);
		if (seqFile.exists()) {
			URI fileURI = URI.createFileURI(fileName);
			return resourceSet.getResource(fileURI, true);
		}
		return null;
	}

	private ResourceSet getResourceSet() throws Exception {
		EditingDomain sequenceEditingDomain = SequenceEditingDomain.INSTANCE.getEditingDomain();
		ResourceSet resourceSet = sequenceEditingDomain.getResourceSet();

		return resourceSet;
	}

	public Sequence getSequence(Resource res) throws Exception {
		if (res != null) {
			List<EObject> contents = res.getContents();
			EObject eobj = contents.get(0);
			if (eobj instanceof DocumentRoot) {
				DocumentRoot root = (DocumentRoot) eobj;
				return root.getSequence();
			}
		}
		return null;
	}

	public List<Region> getRegions(Sequence sequence) throws Exception {
		if (sequence != null) {
			return sequence.getRegion();
		}
		return Collections.emptyList();
	}
}
