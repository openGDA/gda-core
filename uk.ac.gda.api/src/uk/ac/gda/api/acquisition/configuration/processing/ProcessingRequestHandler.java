/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.api.acquisition.configuration.processing;

import org.eclipse.scanning.api.event.scan.ScanRequest;

/**
 * Defines the method that any class handling a subclass of {@link ProcessingRequestPair} should implement.
 *
 * @author Maurizio Nagni
 */
public interface ProcessingRequestHandler {
	/**
	 * Parses a {@link ProcessingRequestPair}
	 *
	 * @param requestingPair the request to process
	 * @param scanRequest the scanRequest to which the {@code requestingPair} is referred
	 * @return {@code true} if the request has been handled normally so the handlers chain may be interrupted, {@code false} otherwise.
	 */
	boolean handle(ProcessingRequestPair<?> requestingPair, ScanRequest scanRequest);
}
