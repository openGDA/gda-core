package org.opengda.detector.electronanalyser.scan;

import java.io.File;
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
import org.opengda.detector.electronanalyser.utils.SequenceEditingDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.scan.ScanPositionProvider;

public class RegionPositionProvider implements ScanPositionProvider {

	private final List<Region> points;

	private static final Logger logger = LoggerFactory.getLogger(RegionPositionProvider.class);

	public RegionPositionProvider(String filename) {
		logger.debug("Sequence file changed to {}{}", FilenameUtils.getFullPath(filename), FilenameUtils.getName(filename));
		points = calculatePoints(filename);
	}

	private List<Region> calculatePoints(String filename) {
		try {
			Resource resource = getResource(filename);
			resource.unload();
			resource.load(Collections.emptyMap());

			Sequence sequence = getSequence(resource);
			List<Region> regions = getRegions(sequence);

			// only add selected/enabled region to the list of points to collect
			return regions.stream().filter(Region::isEnabled).toList();
		} catch (Exception e) {
			logger.error("Cannot get region list from file.", e);
			return Collections.emptyList();
		}
	}

	public RegionPositionProvider(Region region) {
		points = List.of(region);
	}

	@Override
	public Object get(int index) {
		return points.get(index);
	}

	@Override
	public int size() {
		return points.size();
	}

	public Resource getResource(String fileName) {
		ResourceSet resourceSet = getResourceSet();
		File seqFile = new File(fileName);
		if (seqFile.exists()) {
			URI fileURI = URI.createFileURI(fileName);
			return resourceSet.getResource(fileURI, true);
		}
		return null;
	}

	private ResourceSet getResourceSet() {
		EditingDomain sequenceEditingDomain = SequenceEditingDomain.INSTANCE.getEditingDomain();
		return sequenceEditingDomain.getResourceSet();
	}

	public Sequence getSequence(Resource res) {
		if (res != null) {
			List<EObject> contents = res.getContents();
			EObject eobj = contents.get(0);
			if (eobj instanceof DocumentRoot root) {
				return root.getSequence();
			}
		}
		return null;
	}

	public List<Region> getRegions(Sequence sequence) {
		if (sequence != null) {
			return sequence.getRegion();
		}
		return Collections.emptyList();
	}

	@Override
	public String toString() {
		return "RegionPositionProvider [points=" + points + "]";
	}

}
