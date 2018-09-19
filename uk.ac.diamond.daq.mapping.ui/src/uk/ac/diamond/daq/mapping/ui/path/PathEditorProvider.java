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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.LissajousModel;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.api.points.models.OneDStepModel;
import org.eclipse.scanning.api.points.models.PtychographyGridModel;
import org.eclipse.scanning.api.points.models.RandomOffsetGridModel;
import org.eclipse.scanning.api.points.models.RasterModel;
import org.eclipse.scanning.api.points.models.SinglePointModel;
import org.eclipse.scanning.api.points.models.SpiralModel;

/**
 * Class managing the custom editors for different path types.
 *
 * @author James Mudd
 */
public final class PathEditorProvider {

	private static final Map<Class<? extends IScanPathModel>, Class<? extends AbstractPathEditor>> pathToEditor;

	static {
		// Initialise the pathToEditor map
		pathToEditor = new HashMap<>();
		pathToEditor.put(GridModel.class, GridPathEditor.class);
		pathToEditor.put(RasterModel.class, RasterPathEditor.class);
		pathToEditor.put(PtychographyGridModel.class, PtychographyGridPathEditor.class);
		pathToEditor.put(SpiralModel.class, SpiralPathEditor.class);
		pathToEditor.put(LissajousModel.class, LissajousCurvePathEditor.class);
		pathToEditor.put(RandomOffsetGridModel.class, RandomOffsetGridPathEditor.class);
		pathToEditor.put(OneDStepModel.class, OneDStepModelPathEditor.class);
		pathToEditor.put(OneDEqualSpacingModel.class, OneDEqualSpacingPathEditor.class);
		pathToEditor.put(SinglePointModel.class, NoPathEditor.class);
	}

	private PathEditorProvider() {
		throw new IllegalStateException("Static access only");
	}

	/**
	 * This generates a composite for editing the supplied path.
	 *
	 * @param scanPath
	 *            The path to edit
	 * @param bundleContext
	 * 			  The IEclipseContext used in ContextInjectionFactory::make
	 * @return An editor for the requested path
	 */
	public static AbstractPathEditor createPathComposite(IScanPathModel scanPath, IEclipseContext bundleContext) {
		Class<? extends AbstractPathEditor> editorClass = pathToEditor.get(scanPath.getClass());
		AbstractPathEditor editor = ContextInjectionFactory.make(editorClass, bundleContext);
		editor.setModel(scanPath);
		return editor;
	}

}
