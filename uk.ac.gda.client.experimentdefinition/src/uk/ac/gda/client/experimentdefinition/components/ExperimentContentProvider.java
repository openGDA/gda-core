/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.client.experimentdefinition.components;



import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;
import uk.ac.gda.common.rcp.util.ISortingUtils;

public class ExperimentContentProvider implements ITreeContentProvider {


	private String            templateName;
	private ExperimentObjectListener runObjectListener;
	
	public ExperimentContentProvider(final ExperimentObjectListener l) {
		setTemplateName("Configuration");
		this.runObjectListener = l;
	}
	
	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement==null) return null;
		
		if (parentElement instanceof IFile) {
		    throw new RuntimeException("Found IFile where not expected "+parentElement);
		    
		} else if (parentElement instanceof IFolder) {
			
			final IFolder folder = (IFolder)parentElement;
			if (folder.getName().indexOf(' ')>-1) return null;
			if (folder.getResourceAttributes().isReadOnly()) return null;
		    
			try {
				final List<IFile> files = ISortingUtils.getSortedFileList(folder, ".scan");
				if (files==null||files.size()<1) return null;
				
				final IExperimentObjectManager [] mans = new IExperimentObjectManager[files.size()];
				for (int i = 0; i < files.size(); i++) {
					try {
						mans[i] =   ExperimentFactory.getManager(files.get(i));
						mans[i].addExperimentObjectListener(runObjectListener);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return mans;
			} catch (Exception ne) {
				ne.printStackTrace();
				return null;
			}
			
		} else if (parentElement instanceof IProject) {
		   
			try {
				List<IFolder> files = ISortingUtils.getSortedFolderList((IProject)parentElement);
				return files.toArray();
			} catch (CoreException e) {
			    e.printStackTrace();
				return null;
			}

		} else if (parentElement instanceof IExperimentObjectManager) {
			final IExperimentObjectManager manager = (IExperimentObjectManager)parentElement;
			return manager.getExperimentList().toArray(new IExperimentObject[0]);
			
		} else if (parentElement instanceof IExperimentObject) {
			// TODO Show files in tree?
			return null;
		}
		
		return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element==null)                        return null;
		if (ExperimentFactory.getExperimentEditorManager().getProjectFolder().equals(element)) return null;
		
		if (element instanceof IExperimentObject) {
			return ExperimentFactory.getManager((IExperimentObject) element);
		}
		if (element instanceof IExperimentObjectManager) {
			return ((IExperimentObjectManager)element).getContainingFolder();
		}
		if (element instanceof IResource) {
			return ((IResource)element).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IContainer)        return true;
		if (element instanceof IExperimentObjectManager) return true;
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Object[]) {
			return (Object[]) inputElement;
		}
		return getChildren(inputElement);
	}
	
	public Object getRoot() {
		return ExperimentFactory.getExperimentEditorManager().getCurrentProject();
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}


}

	