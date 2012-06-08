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

package gda.device;

import gda.factory.Findable;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;

/** 
 * Spring configurable Findable to hold a map of objects which are themselves not findable.
 */
public class FindableObjectHolder implements InitializingBean, Findable{
	Object object;

	String name;

	@Override
	public void setName(String name) {
		this.name =name;
		
	}

	@Override
	public String getName() {
		return name;
	}

	Map<String, Object> map;

	public void setMap(Map<String, Object> map) {
		this.map = map;
	}
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if( object == map)
			throw new IllegalArgumentException("map is null");
		
	}


	public Object get(Object key) {
		return map.get(key);
	}

	public Set<String> keySet() {
		return map.keySet();
	}

}
