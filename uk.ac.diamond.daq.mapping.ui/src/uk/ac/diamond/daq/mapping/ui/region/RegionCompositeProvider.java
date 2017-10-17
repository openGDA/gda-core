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

package uk.ac.diamond.daq.mapping.ui.region;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.richbeans.api.generator.IGuiGeneratorService;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.region.CentredRectangleMappingRegion;
import uk.ac.diamond.daq.mapping.region.CircularMappingRegion;
import uk.ac.diamond.daq.mapping.region.LineMappingRegion;
import uk.ac.diamond.daq.mapping.region.RectangularMappingRegion;
import uk.ac.diamond.daq.mapping.ui.Activator;

/**
 * Class managing the custom composites for different region types.
 *
 * @author James Mudd
 */
public final class RegionCompositeProvider {

	private static final Logger logger = LoggerFactory.getLogger(RegionCompositeProvider.class);
	private static final Map<Class<? extends IMappingScanRegionShape>, Class<? extends Composite>> regionToComposite;
	private static final IGuiGeneratorService guiGenerator = Activator.getService(IGuiGeneratorService.class);

	static {
		// Initialise the regionToComposite map
		regionToComposite = new HashMap<>();
		regionToComposite.put(RectangularMappingRegion.class, RectangleRegionComposite.class);
		regionToComposite.put(CentredRectangleMappingRegion.class, CentredRectangleRegionComposite.class);
		regionToComposite.put(CircularMappingRegion.class, CircleRegionComposite.class);
		regionToComposite.put(LineMappingRegion.class, LineRegionComposite.class);
	}

	private RegionCompositeProvider() {
		// Prevent instances
	}

	/**
	 * This generates a composite for editing the supplied region. It first checks if a custom composite is registered
	 * for that region type. If it is that will be supplied. If not it will fall back to auto-generation
	 *
	 * @param parent
	 *            The composite to fill
	 * @param mappingScanRegion
	 *            The region to edit
	 * @return A composite for editing the requested region
	 */
	public static Composite createRegionComposite(Composite parent, IMappingScanRegionShape mappingScanRegion) {
		// Find out which composite edits this region type
		Class<? extends Composite> compositeClass = regionToComposite.get(mappingScanRegion.getClass());
		if (compositeClass != null) {
			// If we have a custom composite for this region type make one and return it
			try {
				final Constructor<? extends Composite> constructor = compositeClass.getConstructor(
						Composite.class, mappingScanRegion.getClass());
				return constructor.newInstance(parent, mappingScanRegion);
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				logger.error("Failed to create custom region composite, falling back to auto-generation", e);
			}
		}
		// No custom composite, or building one failed, use auto-generation
		return guiGenerator.generateGui(mappingScanRegion, parent);
	}

}
