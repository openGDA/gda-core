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

	public void addSelectionListener(ISelectionListener selectionListener);

	public void closeAllEditors(boolean b);

	public void closeEditor(IFile sampleFile);

	public void editSelectedElement();

	public ExperimentFolderEditor getActiveFolderEditor();

	public ExperimentRunEditor getActiveRunEditor();

	public IProject getCurrentProject();

	public RichBeanMultiPageEditorPart getEditor(IFile scanFile);

	public IFile getIFile(File scanFile);

	public IFile getIFile(IEditorInput input);

	public IFolder getIFolder(String folder);

	public File getProjectFolder();

	public Object getSelected();

	public IFile getSelectedFile();

	public IFolder getSelectedFolder();

	public IExperimentObjectManager getSelectedMultiScan();

	public IExperimentObject getSelectedScan();

	public Object getValueFromUIOrBean(String fieldName, IBeanController control, Class<? extends XMLRichBean>... classes) throws Exception;

	public ExperimentExperimentView getViewer();

	public void notifyFileNameChange(String origName, IFile nameFile) throws CoreException;

	public void notifyFileNameChange(String origName, IFolder to) throws CoreException;

	public void notifySelectionListeners();

	public boolean openDefaultEditors(final IExperimentObject ob, boolean checkCurrentPerspective);

	public IEditorPart openEditor(IFile vortexFile);

	public IEditorPart openEditor(IFile to, boolean b);

	public IEditorPart openEditor(IFile runFile, String id, boolean b);

	public IEditorPart openEditor(IFolder dir, String id, boolean b);

	public IEditorPart openEditorAndMoveToTheLeft(IFile newFile);

	public void refreshViewers();

	public void removeSelectionListener(ISelectionListener selectionListener);

	public void select(Object selected);

	public void setSelected(Object selected);
}
