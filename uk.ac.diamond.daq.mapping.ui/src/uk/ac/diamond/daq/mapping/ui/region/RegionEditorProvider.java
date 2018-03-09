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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.region.CentredRectangleMappingRegion;
import uk.ac.diamond.daq.mapping.region.CircularMappingRegion;
import uk.ac.diamond.daq.mapping.region.LineMappingRegion;
import uk.ac.diamond.daq.mapping.region.PointMappingRegion;
import uk.ac.diamond.daq.mapping.region.PolygonMappingRegion;
import uk.ac.diamond.daq.mapping.region.RectangularMappingRegion;

/**
 * Class managing the custom editors for different region types.
 *
 * @author James Mudd
 */
public final class RegionEditorProvider {

	private static final Map<Class<? extends IMappingScanRegionShape>, Class<? extends AbstractRegionEditor>> regionToEditor;

	static {
		// Initialise the regionToEditor map
		regionToEditor = new HashMap<>();
		regionToEditor.put(RectangularMappingRegion.class, RectangleRegionEditor.class);
		regionToEditor.put(CentredRectangleMappingRegion.class, CentredRectangleRegionEditor.class);
		regionToEditor.put(CircularMappingRegion.class, CircleRegionEditor.class);
		regionToEditor.put(LineMappingRegion.class, LineRegionEditor.class);
		regionToEditor.put(PolygonMappingRegion.class, PolygonRegionEditor.class);
		regionToEditor.put(PointMappingRegion.class, PointRegionEditor.class);
	}

	private RegionEditorProvider() {
		throw new IllegalStateException("Static access only");
	}

	/**
	 * This generates an editor for editing the supplied region
	 *
	 * @param mappingScanRegion
	 *            The region to edit
	 * @param bundleContext
	 * 			  The IEclipseContext used in ContextInjectionFactory::make
	 * @return An editor for editing the requested region
	 */
	public static AbstractRegionEditor createRegionEditor(IMappingScanRegionShape mappingScanRegion, IEclipseContext bundleContext) {
		Class<? extends AbstractRegionEditor> editorClass = regionToEditor.get(mappingScanRegion.getClass());
		AbstractRegionEditor editor = ContextInjectionFactory.make(editorClass, bundleContext);
		editor.setModel(mappingScanRegion);
		return editor;
	}

}
