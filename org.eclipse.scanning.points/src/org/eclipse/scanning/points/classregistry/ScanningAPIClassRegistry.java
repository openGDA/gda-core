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
package org.eclipse.scanning.points.classregistry;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.persistence.IClassRegistry;
import org.eclipse.scanning.api.device.models.ClusterProcessingModel;
import org.eclipse.scanning.api.device.models.GenericModel;
import org.eclipse.scanning.api.device.models.ProcessingModel;
import org.eclipse.scanning.api.event.scan.AcquireRequest;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.event.scan.DeviceValueMultiPosition;
import org.eclipse.scanning.api.event.scan.PositionerRequest;
import org.eclipse.scanning.api.event.scan.ProcessingRequest;
import org.eclipse.scanning.api.event.scan.SampleData;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.points.StaticPosition;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.AxialCollatedStepModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLineStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisPtychographyModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsRandomOffsetModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.api.points.models.OneAxisPointRepeatedModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.points.models.TwoAxisPointSingleModel;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.scan.AxisConfiguration;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.event.Location;
import org.eclipse.scanning.api.scan.models.ScanMetadata;
import org.eclipse.scanning.api.scan.ui.ControlEnumNode;
import org.eclipse.scanning.api.scan.ui.ControlFileNode;
import org.eclipse.scanning.api.scan.ui.ControlGroup;
import org.eclipse.scanning.api.scan.ui.ControlNode;
import org.eclipse.scanning.api.scan.ui.ControlTree;
import org.eclipse.scanning.api.scan.ui.MonitorScanUIElement;
import org.eclipse.scanning.api.script.ScriptLanguage;
import org.eclipse.scanning.api.script.ScriptRequest;

/**
 *
 * The registry is here because it makes dependencies on DAWNSCI
 * in order to link in beans to the marshaller.
 *
 * @author Martin Gaughran
 * @author Matthew Gerring
 *
 */
public class ScanningAPIClassRegistry implements IClassRegistry {

	private static final Map<String, Class<?>> idToClassMap;
	static {
		Map<String, Class<?>> tmp = new HashMap<String, Class<?>>();

		// event.scan
		registerClass(tmp, DeviceRequest.class);
		registerClass(tmp, PositionerRequest.class);
		registerClass(tmp, AcquireRequest.class);
		registerClass(tmp, ScanBean.class);
		registerClass(tmp, ScanEvent.class);
		registerClass(tmp, SampleData.class);
		registerClass(tmp, ScanRequest.class);
		registerClass(tmp, ScanMetadata.class);
		registerClass(tmp, DeviceValueMultiPosition.class);

		// points
		registerClass(tmp, StaticPosition.class);
		registerClass(tmp, MapPosition.class);
		registerClass(tmp, Point.class);
		registerClass(tmp, Scalar.class);

		// points.models
		registerClass(tmp, AxialArrayModel.class);
		registerClass(tmp, BoundingBox.class);
		registerClass(tmp, BoundingLine.class);
		registerClass(tmp, AxialCollatedStepModel.class);
		registerClass(tmp, CompoundModel.class);
		registerClass(tmp, StaticModel.class);
		registerClass(tmp, TwoAxisGridPointsModel.class);
		registerClass(tmp, TwoAxisLissajousModel.class);
		registerClass(tmp, TwoAxisLinePointsModel.class);
		registerClass(tmp, TwoAxisLineStepModel.class);
		registerClass(tmp, TwoAxisGridPointsRandomOffsetModel.class);
		registerClass(tmp, TwoAxisGridStepModel.class);
		registerClass(tmp, ScanRegion.class);
		registerClass(tmp, TwoAxisPointSingleModel.class);
		registerClass(tmp, TwoAxisSpiralModel.class);
		registerClass(tmp, OneAxisPointRepeatedModel.class);
		registerClass(tmp, AxialStepModel.class);
		registerClass(tmp, AxialMultiStepModel.class);
		registerClass(tmp, GenericModel.class);
		registerClass(tmp, TwoAxisPtychographyModel.class);

		// scan.ui
		registerClass(tmp, ControlEnumNode.class);
		registerClass(tmp, ControlFileNode.class);
		registerClass(tmp, ControlGroup.class);
		registerClass(tmp, ControlNode.class);
		registerClass(tmp, ControlTree.class);
		registerClass(tmp, MonitorScanUIElement.class);
		registerClass(tmp, AxisConfiguration.class);

		// api.scan
		registerClass(tmp, PositionEvent.class);

		// scan.event
		registerClass(tmp, Location.class);

		// device.models
		registerClass(tmp, ProcessingModel.class);
		registerClass(tmp, ClusterProcessingModel.class);

		// script
		registerClass(tmp, ScriptLanguage.class);
		registerClass(tmp, ScriptRequest.class);

		//processing
		registerClass(tmp, ProcessingRequest.class);

		idToClassMap = tmp;
	}

	public ScanningAPIClassRegistry() {

	}
	public ScanningAPIClassRegistry(Class<?>... extras) {
		if (extras!=null) {
			for (Class<?> class1 : extras) {
				registerClass(idToClassMap, class1);
			}
		}
	}

	private static void registerClass(Map<String, Class<?>> map, Class<?> clazz) {
		map.put(clazz.getSimpleName(), clazz);
	}

	@Override
	public Map<String, Class<?>> getIdToClassMap() {
		return idToClassMap;
	}
}
