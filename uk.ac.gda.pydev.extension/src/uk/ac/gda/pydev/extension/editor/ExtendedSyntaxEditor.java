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

package uk.ac.gda.pydev.extension.editor;

import java.util.List;

import gda.jython.JythonServerFacade;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.python.pydev.core.parser.IParserObserver;
import org.python.pydev.editor.PyEdit;
import org.python.pydev.editor.model.IModelListener;
import org.python.pydev.parser.ErrorDescription;
import org.python.pydev.parser.jython.SimpleNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.jython.ExtendedJythonMarkers;
import uk.ac.gda.jython.ExtendedJythonSyntax;
import uk.ac.gda.pydev.extension.Activator;
import uk.ac.gda.pydev.ui.preferences.PreferenceConstants;

/**
 *
 */
public class ExtendedSyntaxEditor extends PyEdit implements IParserObserver, IModelListener {
   
	private static final Logger logger = LoggerFactory.getLogger(ExtendedSyntaxEditor.class);
	
	/**
     * The last parsing error description we got.
     */
    static public final String ID = "uk.ac.gda.pydev.extension.editor.PythonEditor";
	
    protected ErrorDescription currentErrorDescription;

    public ExtendedSyntaxEditor() {
    	addModelListener(this);
    }
    
	@Override
    public void parserError(Throwable error, IAdaptable resource, IDocument doc) {
		
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		if (!store.getBoolean(PreferenceConstants.CHECK_SCRIPT_SYNTAX))
			return;
		
		// Disable as quick fix does not work (as in: isn't offered from GUI)
		// FIXME at the moment almost nothing else in this plugin works as it was designed
		// needs to be rewritten for the latest pydev
		if (true)
			return;
		
		super.parserError(error,resource,doc);
		
		if (currentErrorDescription!=null) {
			final String[] lines = doc.get().split("\n");
			if (lines.length-1<currentErrorDescription.errorLine) return;
			if (currentErrorDescription.errorLine<0) return;
			
			final String   line  = lines[currentErrorDescription.errorLine];
			final List<String> alii = JythonServerFacade.getInstance().getAliasedCommands();
			if (ExtendedJythonSyntax.isCommand(line, alii)) {
				
				try {
					IResource fileAdapter = (IResource) resource.getAdapter(IResource.class);
					if (fileAdapter!=null) {
						final IMarker[] markers = fileAdapter.findMarkers(IMarker.PROBLEM, false, IResource.DEPTH_ONE);
						if (markers.length==1) {
							final IMarker marker = markers[0];
							ExtendedJythonMarkers.fixMarker(marker, line, alii, doc.get());
						}
					}
				} catch (Exception ne) {
					logger.error("Cannot adjust markers for extended syntax", ne);
				}
			}
		}
	}
	
	@Override
	public void errorChanged(ErrorDescription error) {
		currentErrorDescription  = error;
	}

	@Override
	public void modelChanged(SimpleNode arg0) {
		
	}
	
	@Override
	public void dispose() {
		super.dispose();
		removeModelListener(this);
	}
}
