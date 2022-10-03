/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.connector.MalcolmMethod;
import org.eclipse.scanning.api.malcolm.connector.MalcolmMethodMeta;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvmarshaller.marshaller.api.IPVStructureDeserialiser;
import org.epics.pvmarshaller.marshaller.deserialisers.Deserialiser;

/**
 * Deserialises a Malcolm method PV structure ({@code malcolm:core/Method:1.1} to a {@link MalcolmMethod} object.
 *
 * Below is an example of the PV structure for a malcolm method.
 * <pre>
    malcolm:core/Method:1.1 configure
        malcolm:core/MethodLog:1.0 took
            string[] present ["generator", "fileDir", "fileTemplate"]
        malcolm:core/MethodLog:1.0 returned
            string[] present []
        malcolm:core/MethodMeta:1.1 meta
            string description Validate the params then configure the device ready for run().\n\n        Try to prepare the device as much as possible so that run() is quick to\n        start, this may involve potentially long running activities like moving\n        motors.\n\n        Normally it will return in Armed state. If the user aborts then it will\n        return in Aborted state. If something goes wrong it will return in Fault\n        state. If the user disables then it will return in Disabled state.\n
            string[] tags []
            boolean writeable true
            string label Configure
            malcolm:core/MapMeta:1.0 takes
                structure elements
                    malcolm:core/PointGeneratorMeta:1.0 generator
                    malcolm:core/StringMeta:1.0 fileDir
                    malcolm:core/StringArrayMeta:1.0 axesToMove
                    malcolm:core/TableMeta:1.0 detectors
                    malcolm:core/StringMeta:1.0 fileTemplate
                string[] required ["generator", "fileDir"]
            structure defaults
                malcolm:core/Table:1.0 detectors
                    string[] name ["DET", "DIFF", "PANDA-01", "PANDA-02"]
                    string[] mri ["BL45P-ML-DET-01", "BL45P-ML-DIFF-01", "BL45P-ML-PANDA-01", "BL45P-ML-PANDA-02"]
                    double[] exposure [0,0,0,0]
                    int[] framesPerStep [1,1,1,1]
                string fileTemplate %s.h5
            malcolm:core/MapMeta:1.0 returns
                string[] required []
	</pre>
 */
public class MalcolmMethodDeserialiser implements IPVStructureDeserialiser {

	private static final String FIELD_NAME_META = "meta";
	private static final String FIELD_NAME_DEFAULTS = "defaults";

	@Override
	public Object fromPVStructure(Deserialiser deserialiser, PVStructure pvStructure) throws Exception {
		final MalcolmMethod method = getMalcolmMethod(pvStructure);

		final PVStructure metaStructure = pvStructure.getStructureField(FIELD_NAME_META);
		final PVStructure defaultsStructure = metaStructure.getStructureField(FIELD_NAME_DEFAULTS);
		// Note a malcolm method also has

		final MalcolmMethodMeta methodMeta = new MalcolmMethodMeta(method);
		methodMeta.setDefaults(getDefaults(deserialiser, defaultsStructure));

		return methodMeta;
	}

	private Map<String, Object> getDefaults(Deserialiser deserialiser, PVStructure defaultsMetaStructure) throws Exception {
		if (defaultsMetaStructure == null) return Collections.emptyMap();

		final Map<String, Object> defaults = new HashMap<>();
		for (PVField pvField : defaultsMetaStructure.getPVFields()) {
			final String fieldName = pvField.getFieldName();
			final Object fieldValue = deserialiser.getObjectFromField(defaultsMetaStructure, fieldName);
			defaults.put(fieldName, fieldValue);
		}

		return defaults;
	}

	private MalcolmMethod getMalcolmMethod(PVStructure methodStructure) throws MalcolmDeviceException {
		final String methodName = methodStructure.getFieldName();
		try {
			return MalcolmMethod.fromString(methodName);
		} catch (IllegalArgumentException e) {
			throw new MalcolmDeviceException("Unknown malcolm method name: " + methodName);
		}
	}

}
