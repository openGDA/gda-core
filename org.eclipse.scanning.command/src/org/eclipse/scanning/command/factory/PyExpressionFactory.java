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
package org.eclipse.scanning.command.factory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.device.models.IReflectedModel;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.OneAxisPointRepeatedModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsRandomOffsetModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;
import org.eclipse.scanning.command.PyExpressionNotImplementedException;

/**
 *
 * This is a factory for expressing various Java objects in mscan command syntax.
 * @deprecated for removal in 9.21. See DAQ-3292
 * To add a new point generation model to mscan:
 * 1. Add a new expresser for your model here.
 * 2. Edit the test PyExpresserTest to have a test for expressing your model.
 * 3. Edit mapping_scan_commands.py to have a Jython method which matches your expresser string.
 * 4. Edit the test ScanRequestCreationTest to run a test with your model
 *
 * @author Matthew Gerring
 *
 */
//
@Deprecated
public class PyExpressionFactory {

	private static Map<Class<?>, PyModelExpresser<?>> expressers;
	static {
		Map<Class<?>, PyModelExpresser<?>> exp = new LinkedHashMap<>();

		exp.put(AxialStepModel.class, new AxialStepModelExpresser());
		exp.put(AxialMultiStepModel.class, new AxialMultiStepModelExpresser());
		exp.put(TwoAxisGridPointsModel.class, new TwoAxisGridPointsModelExpresser());
		exp.put(TwoAxisGridStepModel.class, new TwoAxisGridStepModelExpresser());
		exp.put(AxialArrayModel.class,new AxialArrayModelExpresser());
		exp.put(OneAxisPointRepeatedModel.class, new OneAxisPointRepeatedModelExpresser());
		exp.put(TwoAxisGridPointsRandomOffsetModel.class,
				new TwoAxisGridPointsRandomOffsetModelExpresser());
		exp.put(TwoAxisSpiralModel.class, new TwoAxisSpiralModelExpresser());
		exp.put(TwoAxisLissajousModel.class, new TwoAxisLissajousModelExpresser());

		exp.put(Collection.class,      new ROICollectionExpresser());
		exp.put(List.class,            new ROICollectionExpresser());
		exp.put(CircularROI.class,     new CircularROIExpresser());
		exp.put(RectangularROI.class,  new RectangularROIExpresser());
		exp.put(PolygonalROI.class,    new PolygonalROIExpresser());

		exp.put(ScanRequest.class,     new ScanRequestExpresser());

		exp.put(IReflectedModel.class, new ReflectedModelExpressor());

		expressers = exp;
	}

	public <T> String pyExpress(T model, boolean verbose) throws Exception {
		final PyModelExpresser<T> expresser = getExpresser(model);
		return expresser.pyExpress(model, verbose);
	}


	private <T> PyModelExpresser<T> getExpresser(T model) throws PyExpressionNotImplementedException {
		final Class<?> modelClass = model.getClass();
		PyModelExpresser<T> expresser = getExpresser(modelClass);
		if (expresser == null) {
			throw new PyExpressionNotImplementedException("The model '" + modelClass + "' does not have a python expresser!");
		}
		expresser.setFactory(this);
		return expresser;
	}

	@SuppressWarnings("unchecked")
	private <T> PyModelExpresser<T> getExpresser(Class<?> modelClass) {
		if (expressers.containsKey(modelClass)) {
			return (PyModelExpresser<T>) expressers.get(modelClass);
		}

		// Recurse over the superclasses/interfaces of the model class
		Class<?> class1 = modelClass;
		while (class1 != null && !class1.equals(Object.class)) {
			try {
				for (Class<?> class2 : class1.getInterfaces()) {
					final PyModelExpresser<T> expresser = getExpresser(class2);
					if (expresser != null) {
						return expresser;
					}
				}
			} finally {
				try {
					class1 = class1.getSuperclass();
				} catch (Exception ne) {
					// nothing to do
				}
			}
		}
		return null;
	}

	public <T> String pyExpress(T model, Collection<IROI> rois, boolean verbose) throws Exception {
		final PyModelExpresser<T> expresser = getExpresser(model);
		return expresser.pyExpress(model, rois, verbose);
	}
}
