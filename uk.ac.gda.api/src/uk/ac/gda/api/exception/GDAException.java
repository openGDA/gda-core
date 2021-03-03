/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Root class for all GDA exceptions
 *
 * @author Maurizio Nagni
 * @deprecated Use instead {@link uk.ac.gda.common.exception.GDAException}. To be removed on GDA 9.22
 */
@Deprecated
public class GDAException extends Exception {

	private static final Logger logger = LoggerFactory.getLogger(GDAException.class);

	public GDAException() {
	}

	public GDAException(String message) {
		super(message);
		logger.warn("Ths class is deprecated and will be remove on GDA 9.22");
	}

	public GDAException(Throwable cause) {
		super(cause);
		logger.warn("Ths class is deprecated and will be remove on GDA 9.22");
	}

	public GDAException(String message, Throwable cause) {
		super(message, cause);
		logger.warn("Ths class is deprecated and will be remove on GDA 9.22");
	}

	public GDAException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		logger.warn("Ths class is deprecated and will be remove on GDA 9.22");
	}
}
