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
package org.eclipse.scanning.device.ui.device;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.scanning.device.ui.ServiceHolder;

public class ControlTreeUtils {

	private ControlTreeUtils() {}

	public static final <T> T clone(T tree) throws Exception {
		IMarshallerService mservice = ServiceHolder.getMarshallerService();
		String      json = mservice.marshal(tree);
		@SuppressWarnings("unchecked")
		T clone = mservice.unmarshal(json, (Class<T>)tree.getClass());
		return clone;
	}

}
