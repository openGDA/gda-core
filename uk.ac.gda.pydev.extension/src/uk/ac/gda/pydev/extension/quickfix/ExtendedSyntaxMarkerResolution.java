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

package uk.ac.gda.pydev.extension.quickfix;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.jython.ExtendedCommand;

/**
 *  TODO create an action which corrects all extended syntax in the file. This just 
 *  corrects the last.
 */
public class ExtendedSyntaxMarkerResolution implements IMarkerResolutionGenerator2 {

    private static Logger logger = LoggerFactory.getLogger(ExtendedSyntaxMarkerResolution.class);
	
	@Override
	public IMarkerResolution[] getResolutions(final IMarker marker) {
		return new IMarkerResolution[] {new ReplaceLineResolution(marker.getAttribute(ExtendedCommand.EXTENDED_QUICK_FIX, (String)null))};
	}

	@Override
	public boolean hasResolutions(IMarker marker) {
		return marker.getAttribute(ExtendedCommand.EXTENDED_QUICK_FIX, (String)null)!=null;
	}
	
	private class ReplaceLineResolution implements IMarkerResolution {

		private String newLine;

		public ReplaceLineResolution(String newLine) {
			this.newLine = newLine;
		}

		@Override
		public String getLabel() {
			return "Replace line with '"+newLine+"'.";
		}

		@Override
		public void run(IMarker marker) {
		
			final IFile        file  = (IFile)marker.getResource();
			try {
				
				// TODO Check line and rep match?
				final String       rep   = marker.getAttribute(ExtendedCommand.EXTENDED_QUICK_FIX, null);
				if (rep == null)   return;
				
				
				final FileEditorInput input = new FileEditorInput(file);
				final IEditorReference[] er = EclipseUtils.getActivePage().findEditors(input, null, IWorkbenchPage.MATCH_INPUT);
				if (er!=null) for (int i = 0; i < er.length; i++) {
					final IEditorPart ed = er[i].getEditor(false);
					if (ed instanceof TextEditor) {
						final TextEditor te = (TextEditor)ed;
						final IDocument doc = te.getDocumentProvider().getDocument(te.getEditorInput());
						int start = (Integer) marker.getAttribute(IMarker.CHAR_START);
						int end = (Integer) marker.getAttribute(IMarker.CHAR_END);
						doc.replace(start, end-start, rep);
					}
				}
				
			} catch (Exception e) {
				logger.error("Cannot read file "+file, e);
			}
			
		}

	}

}
