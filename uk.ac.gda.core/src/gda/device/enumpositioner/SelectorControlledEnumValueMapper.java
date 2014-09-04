/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.enumpositioner;

import gda.device.DeviceException;
import gda.device.Scannable;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;


/**
 * Class to provide a EnumValueMapper selected by the value of a Scannable whose getPosition returns a String
 */
public class SelectorControlledEnumValueMapper<T> implements EnumValueMapper<T>, InitializingBean{
	private static final Logger logger = LoggerFactory.getLogger(SelectorControlledEnumValueMapper.class);
	private Map<String, Map<String,T>> mappings;
	private Scannable selector;
	
	EnumValueMapper<T> mapper;
	

	public Map<String, Map<String, T>> getMappers() {
		return mappings;
	}

	public void setMappers(Map<String, Map<String, T>> mappers) {
		this.mappings = mappers;
	}

	public Scannable getSelector() {
		return selector;
	}

	public void setSelector(Scannable selector) {
		this.selector = selector;
	}

	@Override
	public String getExternalValue(T internalValue) throws IllegalArgumentException {
		try {
			return getMapper().getExternalValue(internalValue);
		} catch( IllegalArgumentException e){
			throw e;
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to get external value",e);
		}
	}

	private EnumValueMapper<T> getMapper() throws Exception {
		if(mapper ==null){
			
			Map<String, T> map = mappings.get(selector.getPosition());
			MapBasedEnumValueMapper<T> enumValueMapper = new MapBasedEnumValueMapper<T>();
			enumValueMapper.setMap(map);
			enumValueMapper.afterPropertiesSet();
			mapper = enumValueMapper;
			
		}
		return mapper;
	}

	@Override
	public T getInternalValue(String externalValue) throws IllegalArgumentException {
		try {
			return getMapper().getInternalValue(externalValue);
		} catch( IllegalArgumentException e){
			throw e;
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to get internal value",e);
		}
	}

	@Override
	public Boolean isExternalValueValid(String externalValueToCheck) {
		try {
			return getMapper().isExternalValueValid(externalValueToCheck);
		} catch( IllegalArgumentException e){
			throw e;
		} catch (Exception e) {
			logger.error("Exception in isExternalValueValid", e);
		}
		return false;
	}

	@Override
	public String[] getExternalValues() throws DeviceException {
		try {
			return getMapper().getExternalValues();
		} catch( IllegalArgumentException e){
			throw e;
		} catch (Exception e) {
			throw new DeviceException("Error in getExternalValues",e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if( mappings == null || mappings.isEmpty()){
			throw new Exception("mappers is not set or empty");
		}
		for(  Map<String, T> m: mappings.values()){
			if( m ==null || m.isEmpty()){
				throw new Exception("one of the maps is not set or empty");
			}
		}
		if(selector == null){
			throw new Exception("selector is null");
		}
	}

}
