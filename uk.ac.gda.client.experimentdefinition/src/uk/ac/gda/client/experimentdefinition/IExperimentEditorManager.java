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

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.richbeans.api.binding.IBeanController;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;

import uk.ac.gda.client.experimentdefinition.components.ExperimentExperimentView;
import uk.ac.gda.client.experimentdefinition.components.ExperimentFolderEditor;
import uk.ac.gda.client.experimentdefinition.components.ExperimentRunEditor;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;
import uk.ac.gda.util.beans.xml.XMLRichBean;

/**
 * Interface for objects controlling editors which display an experiment as defined by an IExperimentObject.
 */
public interface IExperimentEditorManager {

	void addSelectionListener(ISelectionListener selectionListener);

	void closeAllEditors(boolean b);

	void closeEditor(IFile sampleFile);

	void editSelectedElement();

	ExperimentFolderEditor getActiveFolderEditor();

	ExperimentRunEditor getActiveRunEditor();

	IProject getCurrentProject();

	RichBeanMultiPageEditorPart getEditor(IFile scanFile);

	IFile getIFile(File scanFile);

	IFile getIFile(IEditorInput input);

	IFolder getIFolder(String folder);

	File getProjectFolder();

	Object getSelected();

	IFile getSelectedFile();

	IFolder getSelectedFolder();

	IExperimentObjectManager getSelectedMultiScan();

	IExperimentObject getSelectedScan();

	Object getValueFromUIOrBean(String fieldName, IBeanController control, Class<? extends XMLRichBean>... classes) throws Exception;

	ExperimentExperimentView getViewer();

	void notifyFileNameChange(String origName, IFile nameFile) throws CoreException;

	void notifyFileNameChange(String origName, IFolder to) throws CoreException;

	void notifySelectionListeners();

	boolean openDefaultEditors(final IExperimentObject ob, boolean checkCurrentPerspective);

	IEditorPart openEditor(IFile vortexFile);

	IEditorPart openEditor(IFile to, boolean b);

	IEditorPart openEditor(IFile runFile, String id, boolean b);

	IEditorPart openEditor(IFolder dir, String id, boolean b);

	IEditorPart openEditorAndMoveToTheLeft(IFile newFile);

	void refreshViewers();

	void removeSelectionListener(ISelectionListener selectionListener);

	void select(Object selected);

	void setSelected(Object selected);
}
