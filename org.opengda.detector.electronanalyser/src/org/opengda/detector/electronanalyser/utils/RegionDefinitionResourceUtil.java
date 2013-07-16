package org.opengda.detector.electronanalyser.utils;

import gda.data.PathConstructor;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;
import gda.device.DeviceException;
import gda.jython.InterfaceProvider;

import java.io.File;
import java.io.IOException;
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

public class RegionDefinitionResourceUtil {
	private final Logger logger = LoggerFactory
			.getLogger(RegionDefinitionResourceUtil.class);
	String defaultSequenceFilename = "user.seq";
	private double xRaySourceEnergyLimit = 2100.0;
	private boolean sourceSelectable;

	/**
	 * returns the default sequence filename. The returned value depends on java
	 * property {@code gda.data.scan.datawriter.datadir}. If this property is
	 * set, it will use the path specified by this property to store the
	 * sequence file. If this property is not set, it will use {@code use.home}
	 * property to store the sequence file.
	 * 
	 * @return
	 */
	public String getDefaultSequenceFilename() {
		return defaultSequenceFilename;
	}
	/**
	 * return the default sequence file directory. 
	 * 
	 * This directory must have permission for GDA client to write to.
	 * At DLS it is always the 'xml' directory under the directory defined by
	 * the java property {@code gda.data.scan.datawriter.datadir}. 
	 * If this property is set, it will use the path specified by this property 
	 * to store the sequence file. If this property is not set, it will use 
	 * {@code use.home} property to store the sequence file.
	 * 
	 * 
	 * @return
	 */
	public String getDefaultSequenceDirectory() {
		String dir = null;
		String metadataValue = null;
		Metadata metadata = GDAMetadataProvider.getInstance();
		try {
			//must test if 'subdirectory' is set as client can only write to the 
			//'xml' directory under the visit root folder
			metadataValue = metadata.getMetadataValue("subdirectory");
			if (metadataValue != null && !metadataValue.isEmpty()) {
				metadata.setMetadataValue("subdirectory", "");
			}
			String defaultFolder = PathConstructor.createFromDefaultProperty();
			if (defaultFolder != null && defaultFolder.isEmpty()) {
				dir = System.getProperty("user.home") + File.separator;
			} else {
				if (!defaultFolder.endsWith(File.separator)) {
					defaultFolder+=File.separator;
				}
				dir = defaultFolder + "xml";
			}
			// set the original value back for other processing
			metadata.setMetadataValue("subdirectory", metadataValue);
		} catch (DeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dir;
	}
	
	private String fileName;

	public String getFileName() {
		// Resource resource=null;
		// try {
		// resource = getResource();
		// } catch (Exception e) {
		// logger.error("Cannot get resource.", e);
		// }
		// if (resource!=null) {
		// fileName=resource.getURI().toString();
		// }
		return fileName;
	}

	/**
	 * enable file name to be set in Spring object configuration.
	 * 
	 * @param fileName
	 */
	public void setFileName(String fileName) {
		if (!fileName.startsWith(File.separator)) {
			fileName=getDefaultSequenceDirectory()+File.separator+fileName;
		}
		try {
			if(getResource()!=null) {
				getResource().unload();
			}
		} catch (Exception e) {
			logger.debug("Unable to unload resource");
		}
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
		File seqFile = new File(fileName);
		if (seqFile.exists()) {
			URI fileURI = URI.createFileURI(fileName);
			return resourceSet.getResource(fileURI, true);
		}
		return null;
	}
	
	private ResourceSet getResourceSet() throws Exception {
		EditingDomain sequenceEditingDomain = SequenceEditingDomain.INSTANCE.getEditingDomain();
		//the following line only works in RCP/OSGi, not on server
		//Activator.getDefault().getSequenceEditingDomain();
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
		seq.setSpectrum(spectrum);
		root.setSequence(seq);
		newResource.getContents().add(root);

		// use Transaction
		// EditingDomain editingDomain = getEditingDomain();
		// final CommandStack commandStack = editingDomain.getCommandStack();

		// commandStack.execute(new RecordingCommand(
		// (TransactionalEditingDomain) editingDomain) {
		//
		// @Override
		// protected void doExecute() {
		// newResource.getContents().add(root);
		// }
		// });
		try {
			newResource.save(null);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return getSequence();
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
