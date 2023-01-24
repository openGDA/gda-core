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

package gda.util;


/**
 * Enum of the different Ionchamber gas fill types
 */
public enum GasType {
	NITROGEN("N", 1),
	ARGON("Ar", 2),
	KRYPTON("Kr", 3);

	private String name; /** Name of the element */
	private int index; /** index of item in gastype combo box */

	private GasType(String str, int type) {
		this.name = str;
		this.index = type;
	}
	public int getIndex() {
		return index;
	}
	public String getName() {
		return name;
	}

	/**	Return gas fill type for I0 ion chamber for a given energy :
	 * <li> If energy < 7 keV default to use N<sub>2</sub> gas
	 * <li> If 7 <= energy < 26 keV default to use Ar
	 * <li> If energy >= 26keV default to use Kr
	 *
	 * @param energy
	 * @return GasType
	*/
	public static GasType getI0GasType(double energy) {
		if (energy < 7000) {
			return GasType.NITROGEN;
		}
		if (energy >= 7000 && energy < 26000) {
			return GasType.ARGON;
		}
		return GasType.KRYPTON;
	}

	/**Return gas fill type for It/Iref ion chamber for a given energy :
	 * <li> energy < 16keV use Ar
	 * <li> energy >= 16keV use Kr
	 *
	 * @param energy
	 * @return GasType
	 */
	public static GasType getItIrefGasType(double energy) {
		if (energy < 16000) {
			return GasType.ARGON;
		}
		return GasType.KRYPTON;
	}
}
