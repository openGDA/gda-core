package org.opengda.detector.electronanalyser.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.opengda.detector.electronanalyser.Activator;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.DocumentRoot;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionFactory;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;

public class RegionDefinitionResourceUtil {
	private final Logger logger = LoggerFactory
			.getLogger(RegionDefinitionResourceUtil.class);
	private double xRaySourceEnergyLimit = 2100.0;
	private boolean sourceSelectable;
	private String fileName;

	public String getFileName() {
		if (fileName == null) {
			fileName = createExampleSequenceFileIfRequired();
		}
		return fileName;
	}

	private String createExampleSequenceFileIfRequired() {

		// Find the target location for the example .seq file
		final String tgtDataRootPath = PathConstructor.createFromProperty("gda.ses.electronanalyser.seq.dir");
		final String exampleFileName = LocalProperties.get("gda.ses.electronanalyser.seq.filename");
		final String combinedFileName = tgtDataRootPath + File.separator + exampleFileName;
		final File targetFile = new File(tgtDataRootPath, exampleFileName);
		logger.debug("Initial .seq file target '{}'", targetFile.getAbsolutePath());

		// Find the full path to user.seq in the config
		String configDir = LocalProperties.getConfigDir();
		File exampleFile = new File(configDir, exampleFileName);
		logger.debug("Initial .seq file source '{}'", exampleFile.getAbsolutePath());

		// Example file doesn't exist so copy it
		if (!targetFile.exists()) {
			try {
				Files.createDirectories(targetFile.toPath().getParent());
				logger.info("Created directory '{}'", targetFile.toPath().getParent());
				Files.copy(exampleFile.toPath(), targetFile.toPath());
				logger.info("Copied sample analyser config file from '{}' to '{}'", exampleFile, targetFile);
			} catch (IOException e) {
				logger.error("Failed copying sample analyser config file from '{}' to '{}'", exampleFile, targetFile, e);
			}
		}
		else {
			logger.debug("Inital .seq file already present");
		}
		return combinedFileName;
	}

	/**
	 * enable file name to be set in Spring object configuration.
	 *
	 * @param fileName
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * return the resource. The filename for this resource can be set in Spring
	 * object configuration property. If not set in Spring configuration, it is
	 * built using GDA property
	 * {@link gda.configuration.properties.LocalProperties.GDA_DATAWRITER_DIR}
	 * or {@code gda.data.scan.datawriter.datadir} and the default sequnece file
	 * name {@code user.seq}. If this propery is not set this sequence file will
	 * be created at {@code user.home}
	 *
	 * @return
	 * @throws Exception
	 */
	public Resource getResource() throws Exception {
		ResourceSet resourceSet = getResourceSet();
		if (fileName == null) {
			fileName = getFileName();
		}
		URI fileURI = URI.createFileURI(fileName);
		return resourceSet.getResource(fileURI, true);
	}

	private ResourceSet getResourceSet() throws Exception {
		EditingDomain sequenceEditingDomain = SequenceEditingDomain.INSTANCE.getEditingDomain();
		// Create a resource set to hold the resources.
		ResourceSet resourceSet = sequenceEditingDomain.getResourceSet();

		return resourceSet;
	}

	/**
	 * return the list of regions contained in a sequence or an empty list.
	 *
	 * @return
	 * @throws Exception
	 */
	public List<Region> getRegions() throws Exception {

		Sequence sequence = getSequence();
		if (sequence != null) {
			return sequence.getRegion();
		}
		return Collections.emptyList();
	}

	public List<Region> getRegions(String filename) throws Exception {
		setFileName(filename);
		Sequence sequence = getSequence();
		if (sequence != null) {
			return sequence.getRegion();
		}
		return Collections.emptyList();
	}

	public Sequence createSequence() throws Exception {
		final Resource newResource = getResourceSet().createResource(
				URI.createFileURI(fileName));
		final DocumentRoot root = RegiondefinitionFactory.eINSTANCE
				.createDocumentRoot();

		Spectrum spectrum = RegiondefinitionFactory.eINSTANCE.createSpectrum();
		Sequence seq = RegiondefinitionFactory.eINSTANCE.createSequence();
		Region region=RegiondefinitionFactory.eINSTANCE.createRegion();
		seq.setSpectrum(spectrum);
		seq.getRegion().add(region);
		root.setSequence(seq);
		newResource.getContents().add(root);

		try {
			newResource.save(null);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return seq;
	}

	public Sequence getSequence() throws Exception {
		Resource res = getResource();
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

	public Spectrum getSpectrum() throws Exception {
		return getSequence().getSpectrum();
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

	public void save(Resource res) throws IOException {
		res.save(null);
	}

	public void saveAs(Resource resource, String filename) {
		try {
			Resource createResource = getResourceSet().createResource(
					URI.createFileURI(filename));
			createResource.getContents().add(
					EcoreUtil.copy(resource.getContents().get(0)));
			createResource.save(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public EditingDomain getEditingDomain() throws Exception {
		return Activator.getDefault().getSequenceEditingDomain();
	}

	public boolean isSourceSelectable() {
		return sourceSelectable;
	}

	public void setSourceSelectable(boolean sourceSelectable) {
		this.sourceSelectable = sourceSelectable;
	}

	public double getXRaySourceEnergyLimit() {
		return xRaySourceEnergyLimit;
	}

	public void setXRaySourceEnergyLimit(double xRaySourceEnergyLimit) {
		this.xRaySourceEnergyLimit = xRaySourceEnergyLimit;
	}
}
