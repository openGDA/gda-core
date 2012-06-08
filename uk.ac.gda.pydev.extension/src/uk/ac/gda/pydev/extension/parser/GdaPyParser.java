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

package uk.ac.gda.pydev.extension.parser;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.python.pydev.core.IPyEdit;
import org.python.pydev.core.log.Log;
import org.python.pydev.parser.ErrorDescription;
import org.python.pydev.parser.PyParser;
import org.python.pydev.parser.jython.ParseException;
import org.python.pydev.parser.jython.Token;
import org.python.pydev.parser.jython.TokenMgrError;

public class GdaPyParser extends PyParser {

	public GdaPyParser(IPyEdit editorView) {
		super(editorView);
	}
	
    /**
     * Adds the error markers for some error that was found in the parsing process.
     * 
     * @param error the error find while parsing the document
     * @param resource the resource that should have the error added
     * @param doc the document with the resource contents
     * @return the error description (or null)
     * 
     * @throws BadLocationException
     * @throws CoreException
     */
    public static ErrorDescription createParserErrorMarkers(Throwable error, IAdaptable resource, IDocument doc)
            throws BadLocationException, CoreException {
        ErrorDescription errDesc;
        if(resource == null){
            return null;
        }
        IResource fileAdapter = (IResource) resource.getAdapter(IResource.class);
        if(fileAdapter == null){
            return null;
        }
    
        errDesc = createErrorDesc(error, doc);
        
        createErrorMarkers(errDesc, fileAdapter);
        return errDesc;
    }

	private static void createErrorMarkers(ErrorDescription errDesc, IResource fileAdapter) throws CoreException {
		Map<String, Object> map = new HashMap<String, Object>();
        
        map.put(IMarker.MESSAGE, errDesc.message);
        map.put(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
        map.put(IMarker.LINE_NUMBER, errDesc.errorLine);
        map.put(IMarker.CHAR_START, errDesc.errorStart);
        map.put(IMarker.CHAR_END, errDesc.errorEnd);
        map.put(IMarker.TRANSIENT, true);
        MarkerUtilities.createMarker(fileAdapter, map, IMarker.PROBLEM);
	}

    /**
     * Creates the error description for a given error in the parse.
     */
    public static ErrorDescription createErrorDesc(Throwable error, IDocument doc) throws BadLocationException {
        int errorStart = -1;
        int errorEnd = -1;
        int errorLine = -1;
        String message = null;
        if (error instanceof ParseException) {
            ParseException parseErr = (ParseException) error;
            
            // Figure out where the error is in the document, and create a
            // marker for it
            if(parseErr.currentToken == null){
                IRegion endLine = doc.getLineInformationOfOffset(doc.getLength());
                errorStart = endLine.getOffset();
                errorEnd = endLine.getOffset() + endLine.getLength();
    
            }else{
                Token errorToken = parseErr.currentToken.next != null ? parseErr.currentToken.next : parseErr.currentToken;
                IRegion startLine = doc.getLineInformation(getDocPosFromAstPos(errorToken.beginLine));
                IRegion endLine;
                if (errorToken.endLine == 0){
                    endLine = startLine;
                }else{
                    endLine = doc.getLineInformation(getDocPosFromAstPos(errorToken.endLine));
                }
                errorStart = startLine.getOffset() + getDocPosFromAstPos(errorToken.beginColumn);
                errorEnd = endLine.getOffset() + errorToken.endColumn;
            }
            message = parseErr.getMessage();
    
        } else if(error instanceof TokenMgrError){
            TokenMgrError tokenErr = (TokenMgrError) error;
            IRegion startLine = doc.getLineInformation(tokenErr.errorLine - 1);
            errorStart = startLine.getOffset();
            errorEnd = startLine.getOffset() + tokenErr.errorColumn;
            message = tokenErr.getMessage();
        } else{
            Log.log("Error, expecting ParseException or TokenMgrError. Received: "+error);
            return new ErrorDescription(null, 0, 0, 0);
        }
        errorLine = doc.getLineOfOffset(errorStart); 
    
        // map.put(IMarker.LOCATION, "Whassup?"); this is the location field
        // in task manager
        if (message != null) { // prettyprint
            message = message.replaceAll("\\r\\n", " ");
            message = message.replaceAll("\\r", " ");
            message = message.replaceAll("\\n", " ");
        }
        return new ErrorDescription(message, errorLine, errorStart, errorEnd);
    }

    /**
     * The ast position starts at 1 and the document starts at 0 (but it could be that we had nothing valid
     * and received an invalid position, so, we must treat that).
     */
    private static int getDocPosFromAstPos(int astPos) {
        if(astPos > 0){
            astPos--;
        }
        return astPos;
    }


}
