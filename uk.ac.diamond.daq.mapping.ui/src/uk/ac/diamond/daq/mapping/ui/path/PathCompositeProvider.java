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

package uk.ac.diamond.daq.mapping.ui.path;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.richbeans.api.generator.IGuiGeneratorService;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.RasterModel;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.ui.Activator;

/**
 * Class managing the custom composites for different path types.
 *
 * @author James Mudd
 */
public final class PathCompositeProvider {

	private static final Logger logger = LoggerFactory.getLogger(PathCompositeProvider.class);
	private static final Map<Class<? extends IScanPathModel>, Class<? extends Composite>> pathToComposite;
	private static final IGuiGeneratorService guiGenerator = Activator.getService(IGuiGeneratorService.class);

	static {
		// Initialise the regionToComposite map
		pathToComposite = new HashMap<>();
		pathToComposite.put(RasterModel.class, RasterPathComposite.class);
	}

	private PathCompositeProvider() {
		// Prevent instances
	}

	/**
	 * This generates a composite for editing the supplied path. It first checks if a custom composite is registered for
	 * that path type. If it is that will be supplied. If not it will fall back to auto-generation
	 *
	 * @param parent
	 *            The composite to fill
	 * @param scanPath
	 *            The region to edit
	 * @return A composite for editing the requested region
	 */
	public static Composite createPathComposite(Composite parent, IScanPathModel scanPath) {
		// Find out which composite edits this path type
		Class<? extends Composite> compositeClass = pathToComposite.get(scanPath.getClass());
		if (compositeClass != null) {
			// If we have a custom composite for this region type make one and return it
			try {
				final Constructor<? extends Composite> constructor = compositeClass.getConstructor(
						Composite.class, scanPath.getClass());
				return constructor.newInstance(parent, scanPath);
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				logger.error("Failed to create custom region composite, falling back to auto-generation", e);
			}
		}
		// No custom composite, or building one failed, use auto-generation
		return guiGenerator.generateGui(scanPath, parent);
	}

}
