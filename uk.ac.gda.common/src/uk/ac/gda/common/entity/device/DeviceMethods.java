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

package uk.ac.gda.common.entity.device;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import uk.ac.gda.common.entity.DocumentBase;

/**
 * A {@link DocumentBase} subclass used to describe a device available methods.
 *
 * <p>
 * In the frame of this class, the parent {@link DocumentBase} identifies
 * <ul>
 * <li>
 *  a device - {@link DocumentBase#getName()}
 * </li>
 * <li>
 *  a request or a response message - {@link DocumentBase#getUuid()}.
 * </li>
 * </ul>
 * <p>
 *
 * @author Maurizio Nagni
 *
 * @see DeviceValue
 */
@JsonTypeName("deviceMethods")
@JsonDeserialize(builder = DeviceMethods.Builder.class)
public class DeviceMethods extends DocumentBase {

	/**
	 * The property value
	 */
	private final Map<String, List<String>> methods;

	/**
	 * A constructor used only by the inner builder class
	 * @param serviceName
	 * @param property
	 * @param documentBase
	 */
	private DeviceMethods(Map<String, List<String>> methods, DocumentBase documentBase) {
		super(documentBase);
		this.methods = methods;
	}

	public Map<String, List<String>> getMethods() {
		return methods;
	}

	@JsonPOJOBuilder
    public static class Builder extends DocumentBase.Builder {
		private Map<String, List<String>> methods;

    	public final Builder withMethods( Map<String, List<String>> methods) {
	        this.methods = methods;
	        return this;
	    }

    	@Override
		public DeviceMethods build() {
			return new DeviceMethods(methods, super.build());
		}
    }
}
