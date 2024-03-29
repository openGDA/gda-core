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
package org.eclipse.scanning.connector.epics.custommarshallers;

import org.eclipse.scanning.api.points.models.AbstractTwoAxisGridModel.Orientation;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvmarshaller.marshaller.api.IPVStructureDeserialiser;
import org.epics.pvmarshaller.marshaller.deserialisers.Deserialiser;

/**
 * Custom deserialiser for Grid model.
 * TODO - make this non 'test' and finalise custom serialisation strategy for models
 * TODO: DAQ-2324 This class is almost certainly not used. It should be deleted.
 * @author Matt Taylor
 *
 */
public class TwoAxisGridPointsModelDeserialiser implements IPVStructureDeserialiser {

	@Override
	public Object fromPVStructure(Deserialiser deserialiser, PVStructure pvStructure) throws Exception {
		TwoAxisGridPointsModel gridModel = new TwoAxisGridPointsModel();
		gridModel.setName(pvStructure.getSubField(PVString.class, "name").get());
		gridModel.setxAxisName(pvStructure.getSubField(PVString.class, "xAxisName").get());
		gridModel.setyAxisName(pvStructure.getSubField(PVString.class, "yAxisName").get());
		gridModel.setxAxisPoints(pvStructure.getSubField(PVInt.class, "xAxisPoints").get());
		gridModel.setyAxisPoints(pvStructure.getSubField(PVInt.class, "yAxisPoints").get());
		gridModel.setAlternating(pvStructure.getBooleanField("alternating").get());
		gridModel.setContinuous(pvStructure.getBooleanField("continuous").get());
		if (pvStructure.getBooleanField("verticalOrientation").get()) {
			gridModel.setOrientation(Orientation.VERTICAL);
		}
		return gridModel;
	}
}
