package org.opengda.lde.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.opengda.lde.Activator;
import org.opengda.lde.model.ldeexperiment.Cell;
import org.opengda.lde.model.ldeexperiment.Experiment;
import org.opengda.lde.model.ldeexperiment.ExperimentDefinition;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsFactory;
import org.opengda.lde.model.ldeexperiment.Sample;
import org.opengda.lde.model.ldeexperiment.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;
import gda.device.DeviceException;

public class LDEResourceUtil {
	private final Logger logger = LoggerFactory.getLogger(LDEResourceUtil.class);
	String defaultFilename = "newsamples.lde";

	/**
	 * returns the default filename.
	 *
	 * @return
	 */
	public String getDefaultFilename() {
		return defaultFilename;
	}
	/**
	 * return the default file directory.
	 *
	 * This directory must have permission for GDA client to write to.
	 *
	 * At DLS beamlines it is always the 'xml' directory under the directory defined by
	 * the java property {@code gda.data.scan.datawriter.datadir}.
	 *
	 * In other case, such as on developer's PC, or if this property is not set, it will use system
	 * {@code use.home} property to store the file.
	 *
	 *
	 * @return
	 */
	public String getDefaultDirectory() {
		String dir = null;
		String metadataValue = null;
		Metadata metadata = GDAMetadataProvider.getInstance();
		try {
			//must test if 'subdirectory' is set as client can only write to the 'xml' directory under the visit root folder
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
			// restore the original value back for other processing
			metadata.setMetadataValue("subdirectory", metadataValue);
		} catch (DeviceException e) {
			e.printStackTrace();
		}
		return dir;
	}

	private String fileName;

	public String getFileName() {
		return fileName;
	}

	/**
	 * enable file name to be set in Spring object configuration. If the file name dosn't contain full path,
	 * the default XML data directory defined in java property <code>gda.data.scan.datawriter.datadir</code> is used.
	 *
	 * @param fileName
	 */
	public void setFileName(String fileName) {
		if (!fileName.startsWith(File.separator)) {
			fileName=getDefaultDirectory()+File.separator+fileName;
		}
		try {
			if(getResource()!=null) {
				//must unload current resource when change resource filename.
				getResource().unload();
			}
		} catch (Exception e) {
			logger.debug("Unable to unload resource");
		}
		this.fileName = fileName;
	}

	/**
	 * return the resource. The filename for this resource can be set in Spring object configuration property.
	 * If not set in Spring configuration, it is built using GDA property {@code gda.data.scan.datawriter.datadir}
	 * and the default file name {@code newsamples.lde}. If this property is not set this sequence file will
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

		File ldeFile = new File(fileName);
		if (!ldeFile.exists()) {
			ldeFile.createNewFile();
			createExperiments();
		}
		URI fileURI = URI.createFileURI(fileName);
		return resourceSet.getResource(fileURI, true);
	}

	private ResourceSet getResourceSet() {
		EditingDomain editingDomain = SampleGroupEditingDomain.INSTANCE.getEditingDomain();
		// Create a resource set to hold the resources.
		ResourceSet resourceSet = editingDomain.getResourceSet();
		return resourceSet;
	}

	public List<Experiment> getExperiments(String filename) throws Exception {
		setFileName(filename);
		return getExperiments();
	}

	public List<Experiment> createExperiments() throws Exception {
		if (fileName == null || fileName.isEmpty()) {
			fileName=getDefaultDirectory()+File.separator+getDefaultFilename();
		}

		final Resource newResource = getResourceSet().createResource(URI.createFileURI(fileName));
		final ExperimentDefinition root = LDEExperimentsFactory.eINSTANCE.createExperimentDefinition();

		Experiment experiment = LDEExperimentsFactory.eINSTANCE.createExperiment();
		Stage stage = LDEExperimentsFactory.eINSTANCE.createStage();
		experiment.getStage().add(stage);
		Cell cell = LDEExperimentsFactory.eINSTANCE.createCell();
		stage.getCell().add(cell);
		Sample sample=LDEExperimentsFactory.eINSTANCE.createSample();
		cell.getSample().add(sample);
		root.getExperiment().add(experiment);
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

		return getExperiments();
	}

	public List<Experiment> getExperiments() throws Exception {
		Resource res = getResource();
		if (res != null) {
			List<EObject> contents = res.getContents();
			EObject eobj = contents.get(0);
			if (eobj instanceof ExperimentDefinition) {
				ExperimentDefinition root = (ExperimentDefinition) eobj;
				return root.getExperiment();
			}
		}
		return Collections.emptyList();
	}

	public Resource getResource(String fileName) {
		ResourceSet resourceSet = getResourceSet();
		File ldeFile = new File(fileName);
		if (ldeFile.exists()) {
			URI fileURI = URI.createFileURI(fileName);
			return resourceSet.getResource(fileURI, true);
		}
		return null;
	}

	public List<Experiment> getExperiments(Resource res) {
		if (res != null) {
			List<EObject> contents = res.getContents();
			EObject eobj = contents.get(0);
			if (eobj instanceof ExperimentDefinition) {
				ExperimentDefinition root = (ExperimentDefinition) eobj;
				return root.getExperiment();
			}
		}
		return Collections.emptyList();
	}
	public List<Stage> getStages(Experiment experiment) {
		if (experiment != null) {
			return experiment.getStage();
		}
		return Collections.emptyList();
	}
	public List<Cell> getCells(Stage stage) {
		if (stage != null) {
			return stage.getCell();
		}
		return Collections.emptyList();
	}
	public List<Sample> getSamples(Cell cell) {
		if (cell != null) {
			return cell.getSample();
		}
		return Collections.emptyList();
	}


	public void save(Resource res) throws IOException {
		res.save(null);
	}

	public void saveAs(Resource resource, String filename) {
		try {
			Resource createResource = getResourceSet().createResource(URI.createFileURI(filename));
			createResource.getContents().add(EcoreUtil.copy(resource.getContents().get(0)));
			createResource.save(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public EditingDomain getEditingDomain() {
		return Activator.getDefault().getSampleGroupEditingDomain();
	}

	public Map<String, Sample> getSamples(String filename) throws Exception {
		Map<String, Sample> samples=new HashMap<>();
		List<Experiment> experiments = getExperiments(filename);
		for (Experiment experiment : experiments) {
			for (Stage stage :experiment.getStage()) {
				for (Cell cell : stage.getCell()) {
					for (Sample sample : cell.getSample()) {
						samples.put(sample.getSampleID(), sample);
					}
				}
			}
		}
		return Collections.unmodifiableMap(samples);
	}

	public Map<String, Stage> getStages(String filename) throws Exception {
		Map<String, Stage> stages=new HashMap<>();
		List<Experiment> experiments = getExperiments(filename);
		for (Experiment experiment : experiments) {
			for (Stage stage : experiment.getStage()) {
				stages.put(stage.getStageID(), stage);
			}
		}
		return Collections.unmodifiableMap(stages);
	}

	public Map<String, Cell> getCells(String filename) throws Exception {
		Map<String, Cell> cells=new HashMap<>();
		List<Experiment> experiments = getExperiments(filename);
		for (Experiment experiment : experiments) {
			for (Stage stage : experiment.getStage()) {
				for (Cell cell : stage.getCell()) {
					cells.put(cell.getCellID(), cell);
				}
			}
		}
		return Collections.unmodifiableMap(cells);
	}

	public List<Sample>  getSamples() throws Exception {
		List<Sample> samples=new ArrayList<>();
		for (Sample sample : getSamples(getFileName()).values()) {
			samples.add(sample);
		}
		return samples;
	}
	public List<Stage> getStages() throws Exception {
		List<Stage> stages=new ArrayList<>();
		for (Stage stage : getStages(getFileName()).values()) {
			stages.add(stage);
		}
		return stages;
	}
	public List<Cell> getCells() throws Exception {
		List<Cell> cells=new ArrayList<>();
		for (Cell cell : getCells(getFileName()).values()) {
			cells.add(cell);
		}
		return cells;
	}
}
