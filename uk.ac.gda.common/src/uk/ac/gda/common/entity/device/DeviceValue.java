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

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import uk.ac.gda.common.entity.DocumentBase;

/**
 * A {@link DocumentBase} subclass used to get or set device properties.
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
 * As a device may be associated to different service, and different services may have different properties,
 * this class represents the value (primitive or array) of a property of a specific service.
 * <p>
 *
 * @author Maurizio Nagni
 */
@JsonTypeName("deviceValue")
@JsonDeserialize(builder = DeviceValue.Builder.class)
public class DeviceValue extends DocumentBase {

	/**
	 * The service associated with the device defines by {@link #getName()}
	 */
	private final String serviceName;
	/**
	 * The property provided by the service
	 */
	private final String property;

	/**
	 * The property value
	 */
	private final Object value;

	/**
	 * A constructor used only by the inner builder class
	 * @param serviceName
	 * @param property
	 * @param documentBase
	 */
	private DeviceValue(String serviceName, String property, Object value, DocumentBase documentBase) {
		super(documentBase);
		this.serviceName = serviceName;
		this.property = property;
		this.value = value;
	}

	public String getProperty() {
		return property;
	}

	public String getServiceName() {
		return serviceName;
	}

	public Object getValue() {
		return value;
	}

	@JsonPOJOBuilder
    public static class Builder extends DocumentBase.Builder {
    	private String property;
    	private String serviceName;
    	private Object value;

    	public final Builder withProperty(String property) {
	        this.property = property;
	        return this;
	    }

    	public final Builder withServiceName(String serviceName) {
	        this.serviceName = serviceName;
	        return this;
	    }

    	public final Builder withValue(Object value) {
	        this.value = value;
	        return this;
	    }

		@Override
		public DeviceValue build() {
			return new DeviceValue(serviceName, property, value, super.build());
		}
    }
}
