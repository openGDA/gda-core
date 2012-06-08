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


import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;

public class ExperimentRunModifier implements ICellModifier {

	private static final Logger logger = LoggerFactory.getLogger(ExperimentRunModifier.class);
	
	private ExperimentExperimentView controller;

	private boolean enabled;

	/**
	 * The editor can be disabled, useful
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public ExperimentRunModifier(ExperimentExperimentView controller) {
		this.controller = controller;
	}

	@Override
	public boolean canModify(Object element, String property) {
		// if (element instanceof IExperimentObject) {
		// if (((IExperimentObject)element).getExperimentStatus()==ExperimentStatus.RUNNING) return false;
		// }
	    return enabled;
	}


	@Override
	public Object getValue(Object element, String property) {
		if (element instanceof IFolder) {
			final IFolder folder = (IFolder)element;
            return folder.getName();

		} else if (element instanceof IExperimentObjectManager) {
			return ((IExperimentObjectManager)element).getName();
			
		} else if (element instanceof IExperimentObject) {
			return ((IExperimentObject)element).getRunName();
		}
		return null;
	}


	@Override
	public void modify(Object item, String property, Object value) {
		
		// Validation
		String text     = (String)value;
		// No spaces
		if (text!=null && text.indexOf(' ')>-1) {
			MessageDialog.openError(controller.getSite().getShell(), "Contains a space", "The name must not contain a space.");
			this.enabled = false;
			return;
		}

		final TreeItem treeItem = (TreeItem)item;
		Object         element  = treeItem.getData();
		try {
			if (element instanceof IFolder) {
			    if (value==null||"".equals(value)||!value.toString().matches("\\w+\\.?\\w*")) return;
				
			    final IFolder folder    = (IFolder)element;
			    final String origName   = folder.getName();
			    
			    final IFolder to        = ((IProject)folder.getParent()).getFolder(text);
			    if (to.exists()) return;
				folder.move(to.getFullPath(), true, null);
			    controller.refreshTree();		    
				controller.setSelected(to);
				
				ExperimentFactory.getExperimentEditorManager().notifyFileNameChange(origName, to);
				
			} else {
			    if (value==null||"".equals(value)||!value.toString().matches("\\w+")) return;
				if (element instanceof IExperimentObjectManager) {
					final IExperimentObjectManager man = (IExperimentObjectManager)element;
					man.setName(text);
					final ExperimentLabelProvider prov = controller.getLabelProvider();
					text = prov.getText(element);
					treeItem.setText(text);
					
				} else if (element instanceof IExperimentObject) {
					final IExperimentObject runOb = (IExperimentObject)element;
					runOb.setRunName(text);
					
					final ExperimentLabelProvider prov = controller.getLabelProvider();
					text = prov.getText(element);
					treeItem.setText(text);
					
					ExperimentFactory.getManager(runOb).write();
				}
			}
		
		} catch (Exception ne) {
			logger.error("Cannot modify "+element, ne);
		}
		this.enabled = false;

	}
}
