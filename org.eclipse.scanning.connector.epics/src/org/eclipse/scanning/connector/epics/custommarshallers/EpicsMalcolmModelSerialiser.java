/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package org.eclipse.scanning.connector.epics.custommarshallers;

import org.eclipse.scanning.malcolm.core.EpicsMalcolmModel;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.pv.FieldBuilder;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvmarshaller.marshaller.api.IPVStructureSerialiser;
import org.epics.pvmarshaller.marshaller.serialisers.Serialiser;

/**
 * A custom serialiser for {@link EpicsMalcolmModel}. This custom serialiser does not include
 * the detectors field in the resulting structure if it is null in the model.
 */
public class EpicsMalcolmModelSerialiser implements IPVStructureSerialiser<EpicsMalcolmModel> {

	private static final String FIELD_NAME_GENERATOR = "generator";
	private static final String FIELD_NAME_AXES_TO_MOVE = "axesToMove";
	private static final String FIELD_NAME_FILE_DIR = "fileDir";
	private static final String FIELD_NAME_FILE_TEMPLATE = "fileTemplate";
	private static final String FIELD_NAME_DETECTORS = "detectors";
	private static final String FIELD_NAME_BREAKPOINTS = "breakpoints";

	@Override
	public Structure buildStructure(Serialiser serialiser, EpicsMalcolmModel model) throws Exception {
		final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
		final FieldBuilder fieldBuilder = fieldCreate.createFieldBuilder();
		final Structure pointGeneratorStructure = serialiser.buildStructure(model.getGenerator());
		fieldBuilder.add(FIELD_NAME_GENERATOR, pointGeneratorStructure);
		fieldBuilder.addArray(FIELD_NAME_AXES_TO_MOVE, ScalarType.pvString);
		fieldBuilder.add(FIELD_NAME_FILE_DIR, ScalarType.pvString);
		fieldBuilder.add(FIELD_NAME_FILE_TEMPLATE, ScalarType.pvString);

		if (model.getDetectors() != null) {
			final Structure detectorsStructure = serialiser.buildStructure(model.getDetectors());
			fieldBuilder.add(FIELD_NAME_DETECTORS, detectorsStructure);
		}
		if (model.getBreakpoints() != null) {
			fieldBuilder.addArray(FIELD_NAME_BREAKPOINTS, ScalarType.pvInt);
		}

		return fieldBuilder.createStructure();
	}

	@Override
	public void populatePVStructure(Serialiser serialiser, EpicsMalcolmModel model, PVStructure pvStructure)
			throws Exception {
		serialiser.setFieldWithValue(pvStructure, FIELD_NAME_GENERATOR, model.getGenerator());
		final String[] axesToMove = model.getAxesToMove().toArray(new String[model.getAxesToMove().size()]);
		pvStructure.getSubField(PVStringArray.class, FIELD_NAME_AXES_TO_MOVE).put(0, axesToMove.length, axesToMove, 0);
		pvStructure.getStringField(FIELD_NAME_FILE_DIR).put(model.getFileDir());
		pvStructure.getStringField(FIELD_NAME_FILE_TEMPLATE).put(model.getFileTemplate());
		if (model.getDetectors() != null) {
			serialiser.setFieldWithValue(pvStructure, FIELD_NAME_DETECTORS, model.getDetectors());
		}
		if (model.getBreakpoints() != null) {
			final int[] breakpoints = model.getBreakpoints();
			pvStructure.getSubField(PVIntArray.class, FIELD_NAME_BREAKPOINTS).put(0, breakpoints.length, breakpoints, 0);
		}
	}

}
