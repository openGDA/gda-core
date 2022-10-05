/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package org.eclipse.scanning.api.malcolm.connector;

import java.lang.reflect.Method;
import java.util.Map;

import org.eclipse.scanning.api.malcolm.message.Type;

/**
 * A class representing a malcolm method and its meta information. An instance of this class
 * is returned as the result of a {@link Type#GET} with the name of a {@link MalcolmMethod}
 * as the endpoint. It can be considered to be somewhat like a {@link Method}.
 * <p>
 * Note that this an instance of this class is returned corresponding to both the returned
 * structure "malcolm:core/Method:1.1" structure and the "malcolm:core/MethodMeta:1.1" as
 * we're not interested in the other contents of the Method object, namely the 'took' and
 * 'returned' objects. Additionally, at present we only include the 'defaults' object
 * of the MethodMeta structure (if present), ignoring the 'takes' and 'returns' objects as
 * we currently have no use case for those.
 */
public class MalcolmMethodMeta {

	private final MalcolmMethod method;

	private Map<String, Object> defaults;

	public MalcolmMethodMeta(MalcolmMethod method) {
		this.method = method;
	}

	/**
	 * @return the {@link MalcolmMethod} that this meta object describes
	 */
	public MalcolmMethod getMethod() {
		return method;
	}

	/**
	 * @return the default arguments for this method
	 */
	public Map<String, Object> getDefaults() {
		return defaults;
	}

	public void setDefaults(Map<String, Object> defaults) {
		this.defaults = defaults;
	}

}
