/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.client.experimentdefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.util.beans.xml.XMLHelpers;
import uk.ac.gda.util.beans.xml.XMLRichBean;

/**
 * Represents a single scan.
 * <p>
 * Concrete subclasses are required to provide correct constructors when new scans are created in a variety of
 * situations.
 * <p>
 * To work properly with the Experiment perspective, extending classes MUST implement toString(), hashCode() and
 * equals(). The toString method should return a String listing the xml files defining the experiment object, separated
 * by spaces and in the same order that the file types are listed in the extension points.
 */
public abstract class ExperimentObject implements IExperimentObject {

	private static final Logger logger = LoggerFactory.getLogger(ExperimentObject.class);

	public static String createCommandSummary(String folderName,String multiScanName, String scanName, int numberRepetitions, boolean hasBeenStarted){
		String summary = folderName + "-" + multiScanName + "-" + scanName;
		if (numberRepetitions > 1 && !hasBeenStarted) {
			summary += " [" + numberRepetitions + " repeats]";
		}
		return summary;
	}

	protected String id;
	protected boolean isError = false;
	protected int numberRepetitions = 1;
	protected transient IFolder containingFolder;
	protected String folderName;
	protected String multiScanName; // name of IExperimentObjectManager
	protected HashMap<String,String> typeToFileMap = new HashMap<String,String>();
	protected String runName;

	@Override
	public String getErrorMessage() {
		final StringBuilder buf = new StringBuilder();
		HashMap<String,String> typeToFiles = getTypeToFileMap();

		for (Object type : typeToFiles.keySet()) {
			String fileName = typeToFiles.get(type);

			if (!getFolder().getFile(fileName).exists()) {
				buf.append((String) type + " File '");
				buf.append(fileName);
				buf.append("' does not exist.\n");
			}

		}
		return buf.toString();

	}

	@Override
	public IFile getFile(String type) {
		HashMap<String,String> map = getTypeToFileMap();
		String filename = map.get(type);
		if (filename != null) {
			return getFolder().getFile(filename);
		}
		return null;
	}

	@Override
	public String getFileName(String type) {
		return getTypeToFileMap().get(type);
	}

	@Override
	public List<IFile> getFiles() {
		HashMap<String,String> typeToFiles = getTypeToFileMap();

		List<IFile> files = new ArrayList<IFile>(typeToFiles.size());
		for (Object fileName : typeToFiles.values()) {
			files.add(getFolder().getFile((String) fileName));
		}
		return files;
	}

	@Override
	public Map<String, IFile> getFilesWithTypes() {
		HashMap<String,String> typeToFiles = getTypeToFileMap();
		Map<String, IFile> targetFiles = new HashMap<String, IFile>(typeToFiles.size());
		for (Object fileType : typeToFiles.keySet()) {
			IFile file = getFolder().getFile(typeToFiles.get(fileType));
			targetFiles.put((String) fileType, file);
		}
		return targetFiles;
	}

	@Override
	public IFolder getFolder() {
		return containingFolder;
	}

	@Override
	public void setFolder(IFolder containingFolder) {
		this.containingFolder = containingFolder;
		this.folderName = containingFolder.getName();
	}

	/**
	 * The id should be unique for each instance and even cloned ExperimentObjects should have different ids
	 *
	 * @return Returns the id of the run.
	 */
	@Override
	public String getId() {
		if (id == null || id.isEmpty()) {
			id = createUniqueId();
		}
		return id;
	}

	private String createUniqueId() {
		StringBuffer randomString = new StringBuffer();
		Random random = new Random();

		// Generate a random string
		for (int i = 0; i < 15; i++) {
			// 26 uppercase letters + 9 digits + 26 lowercase letters
			int digit = random.nextInt(51);

			char alphaNum;
			if (digit < 26) {
				alphaNum = (char) (digit + 'A');
			} else if (digit < 35) {
				alphaNum = (char) (digit - 26 + '0');
			} else {
				alphaNum = (char) (digit - 35 + 'a');
			}

			randomString.append(alphaNum);
		}
		return randomString.toString();

	}

	@Override
	public Integer getNumberRepetitions() {
		return numberRepetitions;
	}

	@Override
	public List<XMLRichBean> getParameters() throws Exception {

		List<IFile> files = getFiles();
		List<XMLRichBean> params = new ArrayList<XMLRichBean>(files.size());

		for (IFile file : files) {
			try {
				params.add(XMLHelpers.getBean(file.getLocation().toFile()));
			} catch (Exception e) {
				logger.warn("File not found: " + file);
				params.add(null);
			}
		}

		return params;
	}

	@Override
	public String getMultiScanName() {
		return multiScanName;
	}

	@Override
	public void setMultiScanName(String multiScanName) {
		this.multiScanName = multiScanName;
	}

	@Override
	public String getRunName() {
		return runName != null ? runName : "";
	}

	protected HashMap<String,String> getTypeToFileMap() {
		return typeToFileMap;
	}

	@Override
	public boolean isFileUsed(IFile xmlFile) {
		List<IFile> files = getFiles();
		return files.contains(xmlFile);
	}

//	protected void notifyListeners(String propertyName) {
//		final ExperimentObjectEvent evt = new ExperimentObjectEvent(this);
//		evt.setPropertyName(propertyName);
//		evt.setError(isError);
//
//		if (getRunFileManager() != null)
//			this.getRunFileManager().notifyExperimentObjectListeners(evt);
//	}

	@Override
	public void renameFile(String from, String to) {
		HashMap<String, String> typeToFiles = getTypeToFileMap();

		for (String key : typeToFiles.keySet()) {
			String value = typeToFiles.get(key);
			if (value.equals(from)) {
				typeToFiles.put(key, to);
			}
		}
	}

	@Override
	public void setFileName(String exafsBeanType, String name) {
		HashMap<String,String> map = getTypeToFileMap();
		map.put(exafsBeanType, name);
	}

	@Override
	public void setFiles(Map<String, IFile> targetFiles) {
		HashMap<String,String> typeToFiles = getTypeToFileMap();
		for (String beanType : targetFiles.keySet()) {
			IFile file = targetFiles.get(beanType);
			typeToFiles.put(beanType, file.getName());
		}
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void setNumberRepetitions(Integer numRuns) {
		this.numberRepetitions = numRuns;
//		notifyListeners("NumberRepetitions");
	}

	@Override
	public void setRunName(String runName) {
		if (runName.indexOf(' ') > -1)
			throw new RuntimeException("Run name cannot contain a space.");
		this.runName = runName;
//		notifyListeners("RunName");
	}

	protected String getDuplicatedFile(final String fileName) throws CoreException {
		final IFile file = getFolder().getFile(fileName);
		final IFile nf = EclipseUtils.getUniqueFile(file, "xml");
		file.copy(nf.getFullPath(), true, null);
		return nf.getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + numberRepetitions;
		result = prime * result + ((runName == null) ? 0 : runName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExperimentObject other = (ExperimentObject) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		if (numberRepetitions != other.numberRepetitions)
			return false;
		if (runName == null) {
			if (other.runName != null)
				return false;
		} else if (!runName.equals(other.runName))
			return false;
		return true;
	}

	@Override
	public String getCommandSummaryString(boolean hasBeenStarted) {
		return createCommandSummary(folderName, getMultiScanName(),
				getRunName(), getNumberRepetitions(),hasBeenStarted);
	}

}
