/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.malcolm.attributes;

import java.util.Arrays;

/**
 *
 * Encapsulates a number array attribute as read from a malcolm device
 *
 * @author Matt Taylor
 *
 */
public class NumberArrayAttribute extends MalcolmAttribute<Number[]> {
	public static final String NUMBERARRAY_ID = "malcolm:core/NumberArrayMeta:";

	private String dtype;
	private Number value[];

	public String getDtype() {
		return dtype;
	}

	public void setDtype(String dtype) {
		this.dtype = dtype;
	}

	@Override
	public Number[] getValue() {
		return value;
	}

	public void setValue(Number[] value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "NumberArrayAttribute [value=" + Arrays.toString(value) + ", getName()=" + getName() + "]";
	}

}
