/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.sample.plate.management.ui.models;

import java.util.List;

public class ParamCollection {

	private String analyser = null;

	private List<PresetParam> presetParams;

	private List<SetParam> setParams;

	private List<CollectedParam> collectedParams;

	public ParamCollection(List<PresetParam> presetParams, List<SetParam> setParams, List<CollectedParam> collectedParams) {
		this(presetParams, setParams, collectedParams, null);
	}

	public ParamCollection(List<PresetParam> presetParams, List<SetParam> setParams, List<CollectedParam> collectedParams, String analyser) {
		this.presetParams = presetParams;
		this.setParams = setParams;
		this.collectedParams = collectedParams;
		this.analyser = analyser;
	}

	public String getPresetParams() {
		String str = "";
		for (PresetParam presetParam: presetParams) {
			str += "pos ";
			str += presetParam.getParam();
			str += "\n";
		}
		return str;
	}

	public String getSetParams() {
		String str = "";
		for (SetParam setParam: setParams) {
			str += setParam.getParam();
			str += " ";
		}
		return str;
	}

	public String getCollectedParams() {
		String str = "";
		for (CollectedParam collectedParam: collectedParams) {
			str += collectedParam.getParam();
			str += " ";
		}
		return str;
	}

	public String getAnalyser() {
		return analyser;
	}
}
