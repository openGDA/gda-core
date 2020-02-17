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

package gda.util;

public class CrystalParameters {
	private static final double Si_a = 5.4310205; // lattice parameter, in Angstroms, of silicon (CODATA 2018)

	private static final double Ge_a = 5.658; // lattice parameter, in Angstroms, of germanium (Handbook Series on Semiconductor
	// Parameters, v1&2, eds M. Levinstein, S. Rumyantsev and M. Shur, World Scientific, London, 1996, 1999)

	public enum CrystalMaterial {
		SILICON(Si_a), GERMANIUM(Ge_a);

		private final double a;

		// the lattice parameters in Angstroms
		CrystalMaterial(double a) {
			this.a = a;
		}

		/**
		 * @return lattice parameter of cubic crystal in Angstroms
		 */
		public double getA() {
			return a;
		}
	}

	public enum CrystalSpacing {
		Si_111("111", CrystalMaterial.SILICON, 1, 1, 1),
		Si_311("311", CrystalMaterial.SILICON, 3, 1, 1);

		private final String label;
		private final double val;

		/**
		 * @param label
		 * @param m material
		 * @param h Miller indices of plane
		 * @param k
		 * @param l
		 */
		private CrystalSpacing(String label, CrystalMaterial m, int h, int k, int l) {
			this.label = label;
			this.val = m.getA() / Math.sqrt(h*h + k*k + l*l);
		}
		public String getLabel() {
			return label;
		}

		/**
		 * @return plane spacing in Angstroms
		 */
		public double getCrystalD() {
			return val;
		}
	}
}
