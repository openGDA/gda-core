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
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsRandomOffsetModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLineStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.eclipse.scanning.api.points.models.TwoAxisPointSingleModel;
import org.eclipse.scanning.api.points.models.TwoAxisPtychographyModel;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;

/**
 * Class managing the custom editors for different path types.
 *
 * @author James Mudd
 */
public final class PathEditorProvider {

	private static final Map<Class<? extends IScanPathModel>, Class<? extends AbstractPathEditor<?>>> pathToEditor;

	static {
		// Initialise the pathToEditor map
		pathToEditor = new HashMap<>();
		pathToEditor.put(TwoAxisGridPointsModel.class, GridPointsPathEditor.class);
		pathToEditor.put(TwoAxisGridStepModel.class, GridStepPathEditor.class);
		pathToEditor.put(TwoAxisPtychographyModel.class, PtychographyGridPathEditor.class);
		pathToEditor.put(TwoAxisSpiralModel.class, SpiralPathEditor.class);
		pathToEditor.put(TwoAxisLissajousModel.class, LissajousCurvePathEditor.class);
		pathToEditor.put(TwoAxisGridPointsRandomOffsetModel.class, GridPointsRandomOffsetPathEditor.class);
		pathToEditor.put(TwoAxisLineStepModel.class, LineStepPathEditor.class);
		pathToEditor.put(TwoAxisLinePointsModel.class, LinePointsPathEditor.class);
		pathToEditor.put(TwoAxisPointSingleModel.class, SinglePointPathEditor.class);
	}

	private PathEditorProvider() {
		throw new IllegalStateException("Static access only");
	}

	/**
	 * This generates a composite for editing the supplied path.
	 *
	 * @param pathModel the path to edit
	 * @param bundleContext the IEclipseContext used in ContextInjectionFactory::make
	 * @param <M> the model class
	 * @param <E> the editor class
	 * @return An editor for the requested path
	 */
	public static <M extends IScanPathModel, E extends AbstractPathEditor<M>> E createPathComposite(M pathModel, IEclipseContext bundleContext) {
		@SuppressWarnings("unchecked")
		final Class<E> editorClass = (Class<E>) pathToEditor.get(pathModel.getClass());
		final E editor = ContextInjectionFactory.make(editorClass, bundleContext);
		editor.setModel(pathModel);
		return editor;
	}

}
