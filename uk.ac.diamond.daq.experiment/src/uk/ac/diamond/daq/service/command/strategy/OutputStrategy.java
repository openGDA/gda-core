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
package uk.ac.diamond.daq.service.command.strategy;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import uk.ac.gda.common.exception.GDAServiceException;

/**
 * Converts documents accordingly to the implementing strategy class
 *
 * @author Maurizio Nagni
 *
 * @param <T> a compliant document
 */
public interface OutputStrategy<T> {
	
	 /**
	 * Converts a list of documents to a byte array.
	 * 
	 * @param documents a list of objects to format
	 * @return the formatted output
	 * @throws GDAServiceException
	 */
	byte[] write(TypeReference<List<T>> typeReference, final List<T> documents) throws GDAServiceException;
	
	byte[] write(final T documents) throws GDAServiceException;

}