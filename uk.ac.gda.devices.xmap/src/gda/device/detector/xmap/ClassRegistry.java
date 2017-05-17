/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.device.detector.xmap;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.persistence.IClassRegistry;

import gda.device.detector.xmap.api.XmapRunnableDeviceModel;

public class ClassRegistry implements IClassRegistry {

	private static Map<String, Class<?>> registry;
	static {
		registry = new HashMap<>();
		registry.put(XmapRunnableDeviceModel.class.getSimpleName(), XmapRunnableDeviceModel.class);
	}

	@Override
	public Map<String, Class<?>> getIdToClassMap() {
		return registry;
	}

}
