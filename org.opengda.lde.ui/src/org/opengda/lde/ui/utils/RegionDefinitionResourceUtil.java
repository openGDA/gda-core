package org.opengda.lde.ui.utils;

import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;
import gda.device.DeviceException;

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
import org.opengda.lde.model.ldeexperiment.ExperimentDefinition;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsFactory;
import org.opengda.lde.model.ldeexperiment.Sample;
import org.opengda.lde.model.ldeexperiment.SampleList;
import org.opengda.lde.ui.Activator;
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
			// A hacky impl here as this class need to run on both server and client. GDA PathConstructor provide different methods for client and server which cannot be distinguished here.
			String defaultFolder = PathConstructor.createFromDefaultProperty();
			String currentVisitFolder=defaultFolder;
			if (LocalProperties.get(LocalProperties.RCP_APP_VISIT) != null) {
				currentVisitFolder = defaultFolder.replace(LocalProperties.get(LocalProperties.GDA_DEF_VISIT),LocalProperties.get(LocalProperties.RCP_APP_VISIT));
			}
			if (currentVisitFolder != null && currentVisitFolder.isEmpty()) {
				dir = System.getProperty("user.home") + File.separator;
			} else {
				if (!currentVisitFolder.endsWith(File.separator)) {
					currentVisitFolder += File.separator;
				}
				dir = currentVisitFolder + "xml";
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
		if (!seqFile.exists()) {
			seqFile.createNewFile();
			createSequence();
		}
		URI fileURI = URI.createFileURI(fileName);
		return resourceSet.getResource(fileURI, true);
	}
	
	private ResourceSet getResourceSet() throws Exception {
		EditingDomain sequenceEditingDomain = SampleGroupEditingDomain.INSTANCE.getEditingDomain();
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
	public List<Sample> getSamples() throws Exception {

		SampleList samples = getSampelList();
		if (samples != null) {
			return samples.getSamples();
		}
		return Collections.emptyList();
	}

	public List<Sample> getRegions(String filename) throws Exception {
		setFileName(filename);
		SampleList samples = getSampelList();
		if (samples != null) {
			return samples.getSamples();
		}
		return Collections.emptyList();
	}

	public SampleList createSequence() throws Exception {
		final Resource newResource = getResourceSet().createResource(
				URI.createFileURI(fileName));
		final ExperimentDefinition root = LDEExperimentsFactory.eINSTANCE
				.createExperimentDefinition();

		SampleList seq = LDEExperimentsFactory.eINSTANCE.createSampleList();
		Sample region=LDEExperimentsFactory.eINSTANCE.createSample();
		seq.getSamples().add(region);
		root.setSamplelist(seq);
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

		return getSampelList();
	}

	public SampleList getSampelList() throws Exception {
		Resource res = getResource();
		if (res != null) {
			List<EObject> contents = res.getContents();
			EObject eobj = contents.get(0);
			if (eobj instanceof ExperimentDefinition) {
				ExperimentDefinition root = (ExperimentDefinition) eobj;
				return root.getSamplelist();
			}
		}
		return null;
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



	public SampleList getSequence(Resource res) throws Exception {
		if (res != null) {
			List<EObject> contents = res.getContents();
			EObject eobj = contents.get(0);
			if (eobj instanceof ExperimentDefinition) {
				ExperimentDefinition root = (ExperimentDefinition) eobj;
				return root.getSamplelist();
			}
		}
		return null;
	}

	public List<Sample> getRegions(SampleList samplelist) throws Exception {
		if (samplelist != null) {
			return samplelist.getSamples();
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
