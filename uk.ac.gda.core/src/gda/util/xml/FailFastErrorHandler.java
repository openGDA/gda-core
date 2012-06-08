/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.util.xml;

import org.slf4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * An {@link ErrorHandler} that logs to a {@link Logger} and aborts the parsing
 * when an error or fatal error occurs.
 */
public class FailFastErrorHandler implements ErrorHandler {

	private Logger logger;
	
	/**
	 * Creates a fail-fast error handler that logs to the specified logger.
	 * 
	 * @param logger a {@link Logger}
	 */
	public FailFastErrorHandler(Logger logger) {
		this.logger = logger;
	}
	
	@Override
	public void warning(SAXParseException exception) throws SAXException {
		logger.warn("Warning while parsing XML (line " + exception.getLineNumber() + " column " + exception.getColumnNumber() + ")");
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
		logger.error("Error while parsing XML (line " + exception.getLineNumber() + " column " + exception.getColumnNumber() + ")");
		throw exception;
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		logger.error("Fatal error while parsing XML (line " + exception.getLineNumber() + " column " + exception.getColumnNumber() + ")");
		throw exception;
	}

}
