/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.scannable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.python.core.Py;
import org.python.core.PySequenceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.scannablegroup.ScannableGroup;

/**
 * Holds basic information about a scannable at the time of construction.
 * Useful for passing in serialized form to the Dashboard view
 */
public class ScannableSnapshot implements Serializable {
	private static final long serialVersionUID = 3064120878687072679L;
	public final String name;
	public final String[] outputFormat;
	public final String[] extraNames;
	public final String[] inputNames;
	public final String[] units;
	public final Object lastPosition;
	public final boolean busy;

	private static final Logger logger = LoggerFactory.getLogger(ScannableSnapshot.class);

	public ScannableSnapshot(Scannable scn) {
		this.name = scn.getName();
		this.outputFormat = scn.getOutputFormat();
		this.extraNames = scn.getExtraNames();
		this.inputNames = scn.getInputNames();
		if (scn instanceof ScannableMotionUnits) {
			this.units = new String[this.outputFormat.length];
			Arrays.fill(this.units, "");
			this.units[0] = ((ScannableMotionUnits) scn).getUserUnits();
		} else if (scn instanceof ScannableGroup) {
			this.units = getUnitsOfGroup((ScannableGroup) scn).toArray(new String[0]);
		} else {
			this.units = new String[this.outputFormat.length];
			Arrays.fill(this.units, "");
		}
		Object position = null;
		try {
			position = scn.getPosition();
		} catch (Exception e) {
			// still want a valid "snapshot" of a scannable even if we can't get a position
		}
		if (position instanceof PySequenceList) {
			Object p = ((PySequenceList) position).__tojava__(Object[].class);
			if (p == Py.NoConversion) {
				logger.error(String.format("Could not convert %s position to Object[]", name));
				this.lastPosition = null;
			} else {
				this.lastPosition = p;
			}
		} else {
			this.lastPosition = position;
		}
		boolean busy = false;
		try {
			busy = scn.isBusy();
		} catch (Exception e) {
			// usually caused by a motor being in fault
		}
		this.busy = busy;
	}

	private static List<String> getUnitsOfGroup(ScannableGroup grp) {
		List<String> units = new ArrayList<String>();
		for (Scannable scn : grp.getGroupMembersAsArray()) {
			if (scn instanceof ScannableMotionUnits) {
				units.add(((ScannableMotionUnits) scn).getUserUnits());
				for (int i = 1; i < scn.getInputNames().length + scn.getExtraNames().length; i++) {
					// getUserUnits() only provides the unit for the first input name
					// we have to add blank units for the other input and extra names
					units.add("");
				}
			} else if (scn instanceof ScannableGroup) {
				units.addAll(getUnitsOfGroup((ScannableGroup)scn));
			} else {
				for (int i = 0; i < scn.getInputNames().length + scn.getExtraNames().length; i++) {
					units.add("");
				}
			}
		}
		return units;
	}
}
