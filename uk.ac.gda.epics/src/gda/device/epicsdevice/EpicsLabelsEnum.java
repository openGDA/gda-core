/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.epicsdevice;

import java.io.Serializable;
import gov.aps.jca.dbr.*;

/**
 * EpicsLabelsEnum Class
 */
public class EpicsLabelsEnum extends EpicsSTS implements Serializable, LABELS {
	protected String[] strings;

	EpicsLabelsEnum(DBR_LABELS_Enum lblsEnum) {
		super(lblsEnum);
		strings = lblsEnum.getLabels();
	}

	@Override
	public String[] getLabels() {
		return strings;
	}

	@Override
	public void setLabels(String[] strs) {
		strings = strs;
	}

	/**
	 * @see gda.device.epicsdevice.EpicsSTS#toString()
	 */
	@Override
	public String toString() {
		String labels = "";
		for (int i = 0; i < strings.length; i++) {
			labels += "[" + strings[i] + "]";
		}
		return super.toString() + "\n" + "Labels  = " + labels;
	}
}
