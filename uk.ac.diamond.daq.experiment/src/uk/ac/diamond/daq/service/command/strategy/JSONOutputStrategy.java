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
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.diamond.daq.mapping.api.document.DocumentMapper;
import uk.ac.gda.common.exception.GDAServiceException;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;

/**
 * An {@link OutputStrategy} which converts the input to a JSON document as output
 * 
 * @author Maurizio Nagni
 *
 * @param <T>
 */
public class JSONOutputStrategy<T> implements OutputStrategy<T> {

	@Override
	public byte[] write(T document) throws GDAServiceException {
		try {
			return getObjectMapper().writeValueAsBytes(document);
		} catch (JsonProcessingException e) {
			throw new GDAServiceException("Cannot convert document");
		}
	}

	@Override
	public byte[] write(TypeReference<List<T>> typeReference, List<T> ds) throws GDAServiceException {		
		try {
			return getObjectMapper().writerFor(typeReference).writeValueAsBytes(ds);
		} catch (JsonProcessingException e) {
			throw new GDAServiceException("Cannot convert document");
		}
	}

	private ObjectMapper getObjectMapper() throws GDAServiceException {
		DocumentMapper om = Optional.ofNullable(SpringApplicationContextFacade.getBean(DocumentMapper.class))
				.orElseThrow(() -> new GDAServiceException("DocumentMapper not available"));		
		return om.getJacksonObjectMapper();
	}
}