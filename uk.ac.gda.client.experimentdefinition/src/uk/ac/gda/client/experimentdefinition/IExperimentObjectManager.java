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

	IExperimentObject addExperiment(IExperimentObject obj);

	void addExperimentObjectListener(ExperimentObjectListener l);

	/**
	 * Creates a new IExperimentObject which references the same xml files and uses the same name.
	 *
	 * @param original
	 * @return IExperimentObject
	 */
	IExperimentObject cloneExperiment(IExperimentObject original);

	/**
	 * Creates a new IExperimentObject, with new xml files which are identical to the supplied original except the names
	 * of the xml files and the name of the experiment has been incremented.
	 *
	 * @param original
	 * @return IExperimentObject
	 * @throws CoreException
	 */
	IExperimentObject createCopyOfExperiment(IExperimentObject original) throws CoreException;

	IExperimentObject createNewExperiment(String scanName);

	void fireExperimentObjectListeners();

	IFolder getContainingFolder();

	String getErrorMessage();

	List<IExperimentObject> getExperimentList();

	Class<IExperimentObject> getExperimentObjectType();

	IFile getFile();

	String getName();

	Collection<IFile> getReferencedFiles();

	String getUniqueName(String name);

	void insertExperimentAfter(IExperimentObject ob, IExperimentObject copy) throws Exception;

	IExperimentObject insertNewExperimentAfter(IExperimentObject ob);

	boolean isEmpty();

	boolean isFileNameUsed(String fileName);

	void load(IFile file);

	void notifyExperimentObjectListeners(ExperimentObjectEvent evt);

	void removeExperiment(IExperimentObject scanToBeMoved) throws Exception;

	void removeExperimentObjectListener(ExperimentObjectListener l);

	void setFile(IFile nameFile);

	void setName(String text) throws Exception;

	void write();

	/**
	 * Returns an array of the bean types in each experiment, in the order that they should be stored to disk and in the
	 * order that they will be presented to the user (in both the Experiment perspective and the ExperimentRunEditor).
	 * <p>
	 * These strings match the values returned from the getBeanType method in IExperimentBeanDescription
	 * <p>
	 * This can be customised by setting ExafsPreferenceConstants.SCAN_TAB_ORDER to the required order (e.g.
	 * "Scan, Sample, Detector, Output") in plugin_customization.ini.
	 * @return - String[]
	 */
	String[] getOrderedColumnBeanTypes();

	/**
	 * Returns the index of the tab to be selected when opening new scan settings in Experiment perspective.
	 * <p>
	 * This can be customised by setting ExafsPreferenceConstants.SELECTED_SCAN_TAB to one of
	 * Scan, Detector, Sample, Output in plugin_customization.ini
	 * @return int
	 */
	int getDefaultSelectedColumnIndex();

	/**
	 * Whether true if the same type of settings tab is selected when switching between different scans in
	 * the 'Experiment explorer' (e.g. if the 'detector' tab is selected, the detector tab will also be selected
	 * in the next scan that is opened)
	 * <p>
	 * This can be customised by setting ExafsPreferenceConstants.AUTO_SELECT_TAB to true or false
	 * in plugin_customization.ini.
	 * @return true if using auto selection, false otherwise
	 */
	default boolean isAutoSelectColumn() {
		return false;
	}
}
