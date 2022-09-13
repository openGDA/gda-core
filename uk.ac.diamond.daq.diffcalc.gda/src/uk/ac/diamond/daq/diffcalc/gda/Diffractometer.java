/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.diffcalc.gda;

import java.util.HashMap;
import java.util.Map;

import gda.configuration.properties.LocalProperties;
import gda.device.scannable.scannablegroup.ScannableGroup;
import uk.ac.diamond.daq.diffcalc.ApiClient;
import uk.ac.diamond.daq.diffcalc.api.HklApi;

public class Diffractometer extends ScannableGroup {

	private AngleTransform angleTransform;

	private ApiClient client = (new ApiClient()).setBasePath(LocalProperties.get("gda.diffcalc.url"));
	private HklApi hklApi = new HklApi(client);

	public String simulateMoveTo(Object position) {
		Map<ReferenceGeometry, Double> wat = angleTransform.getReferenceGeometry(getPositionAsMap(position));

		//hklApi.millerIndicesFromLabPositionHklNamePositionHklGetWithHttpInfo(DEFAULT_INPUT_NAME, null, null, null, null, null, null, null, DEFAULT_PROTECTION_LEVEL_PROPERTY)
		return "";
	}

	public Map<String, Double> getPositionAsMap(Object position) {
		Object[] positionList = (Object[]) position;
		Map<String, Double> beamlinePosition = new HashMap<>();

		for (int i=0; i < positionList.length; i++) {
			beamlinePosition.put(getInputNames()[i], (Double) positionList[i]);
		}

		return beamlinePosition;
	}
}
