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
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;

import uk.ac.gda.client.experimentdefinition.components.ExperimentObjectListener;

/**
 * An interface for objects controlling all the IExperimentObjects in the project.
 */
public interface IExperimentObjectManager extends Serializable{

	public IExperimentObject addExperiment(IExperimentObject obj);

	public void addExperimentObjectListener(ExperimentObjectListener l);

	/**
	 * Creates a new IExperimentObject which references the same xml files and uses the same name.
	 * 
	 * @param original
	 * @return IExperimentObject
	 */
	public IExperimentObject cloneExperiment(IExperimentObject original);

	/**
	 * Creates a new IExperimentObject, with new xml files which are identical to the supplied original except the names
	 * of the xml files and the name of the experiment has been incremented.
	 * 
	 * @param original
	 * @return IExperimentObject
	 * @throws CoreException
	 */
	public IExperimentObject createCopyOfExperiment(IExperimentObject original) throws CoreException;

	public IExperimentObject createNewExperiment(String scanName);

	public void fireExperimentObjectListeners();

	public IFolder getContainingFolder();

	public String getErrorMessage();

	public List<IExperimentObject> getExperimentList();

	public Class<IExperimentObject> getExperimentObjectType();

	public IFile getFile();

	public String getName();

	public Collection<IFile> getReferencedFiles();

	public String getUniqueName(String name);

	public void insertExperimentAfter(IExperimentObject ob, IExperimentObject copy) throws Exception;

	public IExperimentObject insertNewExperimentAfter(IExperimentObject ob);

	public boolean isEmpty();

	public boolean isFileNameUsed(String fileName);

	public void load(IFile file);

	public void notifyExperimentObjectListeners(ExperimentObjectEvent evt);

	public void removeExperiment(IExperimentObject scanToBeMoved) throws Exception;

	public void removeExperimentObjectListener(ExperimentObjectListener l);

	public void setFile(IFile nameFile);

	public void setName(String text) throws Exception;

	public void write();

	/**
	 * Returns an array of the bean types in each experiment, in the order that they should be stored to disk and in the
	 * order that they will be presented to the user (in both the Experiment perspective and the ExperimentRunEditor).
	 * <p>
	 * These strings match the values returned from the getBeanType method in IExperimentBeanDescription
	 * 
	 * @return - String[]
	 */
	public String[] getOrderedColumnBeanTypes();

}
