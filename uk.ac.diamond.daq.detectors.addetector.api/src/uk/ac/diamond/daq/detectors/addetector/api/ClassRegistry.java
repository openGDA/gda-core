/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.detectors.addetector.api;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.persistence.IClassRegistry;

public class ClassRegistry implements IClassRegistry {

	private static Map<String, Class<?>> registry;
	static {
		registry = new HashMap<>();
		registry.put(AreaDetectorRunnableDeviceModel.class.getSimpleName(), AreaDetectorRunnableDeviceModel.class);
		registry.put(AreaDetectorWritingFilesRunnableDeviceModel.class.getSimpleName(), AreaDetectorWritingFilesRunnableDeviceModel.class);
		registry.put(DarkImageAreaDetectorWritingFilesRunnableDeviceModel.class.getSimpleName(), DarkImageAreaDetectorWritingFilesRunnableDeviceModel.class);
		registry.put(ZebraModel.class.getSimpleName(), ZebraModel.class);
	}

	@Override
	public Map<String, Class<?>> getIdToClassMap() {
		return registry;
	}

}
