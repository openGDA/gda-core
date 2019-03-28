/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.epics;

public interface EpicsConstants {
	public enum YesNo {
		No, Yes;
	}

	public enum DisableEnable {
		Disable, Enable;
	}

	/**
	 * Epics timestamps use the Epics Epoch which starts from 1st Jan 1990. As Unix / Java time runs from 1st Jan 1970
	 * an offset needs to be applied to convert Epics timestamps, in millisceonds, to the Java Epoch.
	 * <p>
	 * So add this number to Epics timestamps to create java.util.Date objects.
	 */
	public static final long EPICS_EPOCH_OFFSET = 631152000000L; // 00:00:00 1st Jan 1990 in Java Epoch
}
