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

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;


/*
 * Implementation of EnumValueMapper<T> that is based of a supplied Map<String, T>
 */
public class MapBasedEnumValueMapper<T> implements EnumValueMapper<T>, InitializingBean{

	Map<String, T> map;

	private BiMap<String,T> externalToInternalMap;
	private BiMap<T, String> internalToExternalMap;
	private String[] externalValues;
	
	
	
	public void setMap(Map<String, T> map) {
		this.map = map;
	}

	@Override
	public String getExternalValue(T internalValue) throws IllegalArgumentException {
		String externalValue = internalToExternalMap.get(internalValue);
		if( externalValue == null)
			throw new IllegalArgumentException("Unable to find external value for " + internalValue);
		return externalValue;
	}

	@Override
	public T getInternalValue(String externalValue) throws IllegalArgumentException {
		T internalValue = externalToInternalMap.get(externalValue);
		if( internalValue == null)
			throw new IllegalArgumentException("Unable to find external value for " + externalValue);
		return internalValue;
	}

	@Override
	public Boolean isExternalValueValid(String externalValueToCheck) {
		return externalToInternalMap.containsKey(externalValueToCheck);
	}

	@Override
	public String[] getExternalValues() {
		return externalValues;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if( map == null || map.isEmpty()){
			throw new Exception("map is not set or empty");
		}
		externalToInternalMap = HashBiMap.create(map);
		internalToExternalMap = this.externalToInternalMap.inverse();
		externalValues = externalToInternalMap.keySet().toArray(new String[]{});
	}

}
