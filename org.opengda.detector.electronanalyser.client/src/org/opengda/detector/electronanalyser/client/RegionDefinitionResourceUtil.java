package org.opengda.detector.electronanalyser.client;

import gda.data.PathConstructor;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.DocumentRoot;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionFactory;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum;
import org.opengda.detector.electronanalyser.model.regiondefinition.util.RegiondefinitionResourceFactoryImpl;

public class RegionDefinitionResourceUtil {
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
		String filename;
		String defaultFolder = PathConstructor.createFromDefaultProperty();
		if (defaultFolder != null && defaultFolder.isEmpty()) {
			filename = System.getProperty("user.home") + File.pathSeparator
					+ defaultSequenceFilename;
		} else {
			filename = defaultFolder + File.pathSeparator
					+ defaultSequenceFilename;
		}
		return filename;
	}

	private String fileName;

	public String getFileName() {
		return fileName;
	}
	/**
	 * enable file name to be set in Spring object configuration.
	 * @param fileName
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	/**
	 * return the resource. The filename for this resource can be set in Spring object configuration property. 
	 * If not set in Spring configuration, it is built using GDA property 
	 * {@link gda.configuration.properties.LocalProperties.GDA_DATAWRITER_DIR} or {@code gda.data.scan.datawriter.datadir} 
	 * and the default sequnece file name {@code user.seq}. If this propery is not set this sequence file will be created at {@code user.home}
	 * @return
	 * @throws Exception
	 */
	public Resource getResource() throws Exception {
		ResourceSet resourceSet = getResourceSet();
		if (fileName == null) {
			fileName = getDefaultSequenceFilename();
		}
		File seqFile = new File(fileName);
		if (seqFile.exists()) {
			URI fileURI = URI.createFileURI(fileName);
			return resourceSet.getResource(fileURI, true);
		}
		return null;
	}
	/**
	 * return the list of regions contained in a sequence or an empty list.
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
//		EditingDomain editingDomain = getEditingDomain();
//		final CommandStack commandStack = editingDomain.getCommandStack();

//		commandStack.execute(new RecordingCommand(
//				(TransactionalEditingDomain) editingDomain) {
//
//			@Override
//			protected void doExecute() {
//				newResource.getContents().add(root);
//			}
//		});
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

	private ResourceSet getResourceSet() throws Exception {
		EditingDomain sequenceEditingDomain = ElectronAnalyserClientPlugin
				.getDefault().getSequenceEditingDomain();
		// Create a resource set to hold the resources.
		ResourceSet resourceSet = sequenceEditingDomain.getResourceSet();
		// Register the appropriate resource factory to handle all file
		// extensions.
		resourceSet
				.getResourceFactoryRegistry()
				.getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION,
						new RegiondefinitionResourceFactoryImpl());
		// Register the package to ensure it is available during loading.
		resourceSet.getPackageRegistry().put(RegiondefinitionPackage.eNS_URI,
				RegiondefinitionPackage.eINSTANCE);
		return resourceSet;
	}

	public void save(Resource res) throws IOException {
		res.save(null);
	}

	public EditingDomain getEditingDomain() throws Exception {
		return ElectronAnalyserClientPlugin.getDefault()
				.getSequenceEditingDomain();
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
