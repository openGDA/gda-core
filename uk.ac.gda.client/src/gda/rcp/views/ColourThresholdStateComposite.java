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

package gda.rcp.views;

import java.util.Map;
import java.util.Optional;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import gda.device.Scannable;

public class ColourThresholdStateComposite extends AbstractColourStateComposite {

	public ColourThresholdStateComposite(Composite parent, int style, String label, int canvasWidth, int canvasHeight,
			Scannable scannable, Map<String, Color> stateMap) {
		super(parent, style, label, canvasWidth, canvasHeight, scannable, stateMap);
	}

	@Override
	protected Color getMapValue(Object position) {
		return stateMap.get(getMapKey(position));
	}

	@Override
	protected String getMapKey(Object position) {
		Double key = Double.valueOf(position.toString());
		Optional<Map.Entry<String, Color>> optionalEntry = stateMap.entrySet().stream()
				.filter(entry -> Double.parseDouble(entry.getKey()) <= key)
				.max((entry1, entry2) -> Double.valueOf(entry1.getKey()).compareTo(Double.valueOf(entry2.getKey())));

		if (optionalEntry.isPresent()) {
			return optionalEntry.get().getKey();
		}
		return null;
	}

	@Override
	protected String getToolTip(Object position) {
		return position.toString();
	}

}
