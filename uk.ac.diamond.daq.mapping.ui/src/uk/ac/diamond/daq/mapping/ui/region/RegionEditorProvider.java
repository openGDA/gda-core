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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.region.CentredRectangleMappingRegion;
import uk.ac.diamond.daq.mapping.region.CircularMappingRegion;
import uk.ac.diamond.daq.mapping.region.LineMappingRegion;
import uk.ac.diamond.daq.mapping.region.PointMappingRegion;
import uk.ac.diamond.daq.mapping.region.PolygonMappingRegion;
import uk.ac.diamond.daq.mapping.region.RectangularMappingRegion;
import uk.ac.diamond.daq.mapping.ui.experiment.AbstractRegionPathModelEditor;

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
	 * @param mappingScanRegion the region to edit
	 * @param regionUnits a map specifying the initial units to display for each axis, updated when the user selects new units for an axis. This is used
	 * 		for display purposes only, the actual units come from the scannable with the given axis name. May be <code>null</code> or empty.
	 * @param bundleContext the IEclipseContext used in ContextInjectionFactory::make
	 * @return An editor for editing the requested region
	 */
	public static AbstractRegionPathModelEditor<IMappingScanRegionShape> createRegionEditor(IMappingScanRegionShape mappingScanRegion, Map<String, String> regionUnits, IEclipseContext bundleContext) {
		Class<? extends AbstractRegionEditor> editorClass = regionToEditor.get(mappingScanRegion.getClass());
		final AbstractRegionPathModelEditor<IMappingScanRegionShape> editor;
		if (editorClass == null) {
			editor = new UnknownRegionEditor(mappingScanRegion);
		} else {
			editor = ContextInjectionFactory.make(editorClass, bundleContext);
		}
		editor.setModel(mappingScanRegion);
		editor.setAxisUnits(regionUnits);
		return editor;
	}

	/** Replacement editor for when no editor is registered against the given region type */
	private static class UnknownRegionEditor extends AbstractRegionEditor {

		private final String unknownRegionClass;

		public UnknownRegionEditor(IMappingScanRegionShape unrecognisedRegion) {
			this.unknownRegionClass = unrecognisedRegion.getClass().getCanonicalName();
		}

		@Override
		public Composite createEditorPart(Composite parent) {
			var composite = super.createEditorPart(parent);
			new Label(composite, SWT.NONE).setText("Unregistered region type!\n" + unknownRegionClass);
			return composite;
		}
	}

}
