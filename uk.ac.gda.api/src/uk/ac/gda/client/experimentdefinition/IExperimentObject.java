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
	 * Should only be called on a freshly instantiated object. This copies xml files from the templates folder into the correct folder for this ExperimentObject
	 * using the names of the files which this object has been given.
	 */
	void createFilesFromTemplates(IXMLCommandHandler xmlCH);

	/**
	 * Views may not work correctly if this is not implemented
	 *
	 * @see Object#equals(Object)
	 */
	@Override
	boolean equals(Object obj);

	/**
	 * Views may not work correctly if this is not implemented
	 *
	 * @see Object#hashCode()
	 */
	@Override
	int hashCode();

	/**
	 * Prediction in milliseconds of the duration of this experiment
	 *
	 * @return long
	 * @throws Exception
	 */
	long estimateTime() throws Exception;

	/**
	 * FIXME should probably be renamed.
	 *
	 * @return validates and returns error if any problems
	 */
	String getErrorMessage();

	IFile getFile(String type);

	String getFileName(String type);

	List<IFile> getFiles();

	Map<String, IFile> getFilesWithTypes();

	String getId();

	Integer getNumberRepetitions();

	/**
	 * @return the directory where data files should go to, if defined within the experiment's beans, else returns null
	 */
	String getOutputPath();

	List<XMLRichBean> getParameters() throws Exception;

	IFolder getFolder();

	void setFolder(IFolder containingFolder);

	String getRunName();

	String getMultiScanName();

	void setMultiScanName(String multiScanName);

	boolean isFileUsed(IFile xmlFile);

	void renameFile(String name, String name2);

	void setFileName(String exafsBeanType, String name);

	void setFiles(Map<String, IFile> targetFiles);

	void setNumberRepetitions(Integer i);

	void setRunName(String text);

	/**
	 * When the ExperimentObjectManager persists all of its scans to file, each object must return a space-separated
	 * list of the xml files it represents.
	 *
	 * @return String
	 */
	String toPersistenceString();

	/**
	 * The command to run the script, calling the relevant xml files.
	 *
	 * @return the Jython command to run the experiment defined by this object
	 * @throws Exception
	 */
	String getCommandString() throws Exception;

	/**
	 * @return the string to be displayed in the Command Queue View
	 */
	String getCommandSummaryString(boolean hasBeenStarted);

	/**
	 * When an experiment object is used in the Command Queue, it may be edited by the command string being written to a
	 * temp file.
	 * <p>
	 * This method should be called to update the command string and command summary string if the file is edited.
	 *
	 * @param fileName
	 * @throws Exception
	 */
	void parseEditorFile(String fileName) throws Exception;
}
