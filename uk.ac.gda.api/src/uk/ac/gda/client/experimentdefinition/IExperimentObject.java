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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

import uk.ac.gda.util.beans.xml.XMLRichBean;

/**
 * Interface for an object describing an experiment using a collection of IRichBeans
 */
public interface IExperimentObject extends Serializable{

	/**
	 * Should only be called on a freshly instantiated object. This copies xml files from the templates folder into the
	 * correct folder for this ExperimentObject using the names of the files which this object has been gievn.
	 */

	public void createFilesFromTemplates();

	public void createFilesFromTemplates(IXMLCommandHandler xmlCH);

	/**
	 * Views may not work correctly if this is not implemented
	 *
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj);

	/**
	 * Views may not work correctly if this is not implemented
	 *
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode();

	/**
	 * Prediction in milliseconds of the duration of this experiment
	 *
	 * @return long
	 * @throws Exception
	 */
	public long estimateTime() throws Exception;

	/**
	 * FIXME should probably be renamed.
	 *
	 * @return validates and returns error if any problems
	 */
	public String getErrorMessage();

	public IFile getFile(String type);

	public String getFileName(String type);

	public List<IFile> getFiles();

	public Map<String, IFile> getFilesWithTypes();

	public String getId();

	public Integer getNumberRepetitions();

	/**
	 * @return the directory where data files should go to, if defined within the experiment's beans, else returns null
	 */
	public String getOutputPath();

	public List<XMLRichBean> getParameters() throws Exception;

	public IFolder getFolder();

	public void setFolder(IFolder containingFolder);

	public String getRunName();

	public String getMultiScanName();

	public void setMultiScanName(String multiScanName);

	public boolean isFileUsed(IFile xmlFile);

	public void renameFile(String name, String name2);

	public void setFileName(String exafsBeanType, String name);

	public void setFiles(Map<String, IFile> targetFiles);

	public void setNumberRepetitions(Integer i);

	public void setRunName(String text);

	/**
	 * When the ExperimentObjectManager persists all of its scans to file, each object must return a space-separated
	 * list of the xml files it represents.
	 *
	 * @return String
	 */
	public String toPersistenceString();

	/**
	 * The command to run the script, calling the relevant xml files.
	 *
	 * @return the Jython command to run the experiment defined by this object
	 * @throws Exception
	 */
	public String getCommandString() throws Exception;

	/**
	 * @return the string to be displayed in the Command Queue View
	 */
	public String getCommandSummaryString(boolean hasBeenStarted);

	/**
	 * When an experiment object is used in the Command Queue, it may be edited by the command string being written to a
	 * temp file.
	 * <p>
	 * This method should be called to update the command string and command summary string if the file is edited.
	 *
	 * @param fileName
	 * @throws Exception
	 */
	public void parseEditorFile(String fileName) throws Exception;
}
