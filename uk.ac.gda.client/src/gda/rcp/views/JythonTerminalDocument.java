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

package gda.rcp.views;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends the standard JFace {@link Document} class to add an {@code append}
 * method.
 */
public class JythonTerminalDocument extends Document {
	
	private static final Logger logger = LoggerFactory.getLogger(JythonTerminalDocument.class);
	
	/**
	 * Appends the given text to the end of this document.
	 */
	public void append(String text) {
		try {
			replace(getLength(), 0, text);
		} catch (BadLocationException e) {
			logger.error("Couldn't append text", e);
		}
	}

}
