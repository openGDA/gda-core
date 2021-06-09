/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.pes.api;

import java.util.HashMap;
import java.util.Map;

public enum AcquisitionMode {
	FIXED("Fixed"),
	SWEPT("Swept"),
	DITHER("Dither");

	public final String label;

	private AcquisitionMode(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}

	private static final Map<String, AcquisitionMode> LABELS = new HashMap<>();

	static {
        for (AcquisitionMode e: values()) {
        	LABELS.put(e.label, e);
        }
    }

	public static AcquisitionMode valueOfLabel(String label) {
		return LABELS.get(label);
    }
}
