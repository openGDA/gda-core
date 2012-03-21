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

package uk.ac.gda.exafs.ui.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import uk.ac.gda.client.experimentdefinition.ui.handlers.AbstractExperimentCommandHandler;
import uk.ac.gda.client.experimentdefinition.ui.handlers.XMLCommandHandler;
import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.exafs.ui.detector.DetectorEditor;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;

/**
 *
 */
public class RevertDetectorParametersToTemplateAction extends AbstractExperimentCommandHandler {

	/**
	 * 
	 */
	public final static String ID = "uk.ac.gda.exafs.ui.actions.RevertDetectorParametersToTemplateAction";
	
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
	
        final IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (!(part instanceof RichBeanMultiPageEditorPart)) return Boolean.FALSE;
        
        final boolean ok = MessageDialog.openQuestion(part.getSite().getShell(), "Revert Detector", 
        		           "Are you sure you want to lose your edits and revert to the server detector configuration?");
        if (!ok) return Boolean.FALSE;
        
        final IEditorPart       page   = ((RichBeanMultiPageEditorPart)part).getRichBeanEditor();
        if (!(page instanceof DetectorEditor)) return Boolean.FALSE;

        final DetectorEditor    ed     = (DetectorEditor)page;
        final XMLCommandHandler copier = ed.getXMLCommandHandler();
        
        final IFile file = EclipseUtils.getIFile(ed.getEditorInput());
        if (file==null) {
        	MessageDialog.openError(ed.getSite().getShell(), "Cannot revert '"+ed.getPartName()+"'",
        			                "The file cannot be reverted as it is an external file to the project.");
        	return Boolean.FALSE;
        }
        
        // Copy
        copier.doCopy(file);
        
        // Refresh editor
        ((RichBeanMultiPageEditorPart)part).setInput(new FileEditorInput(file));
        
        return Boolean.TRUE;
	}

}
