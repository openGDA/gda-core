/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package gda.exafs.scan;

import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;

public interface XasScanPointCreator {

	/**
	 * @return an array of the energy and time values this scan will step through
	 * @throws Exception
	 */
	double[][] getEnergies() throws Exception;

	/**
	 * @return an array of the time values this scan will step through
	 * @throws Exception
	 */
	Double[] getTimes() throws Exception;


	public static XasScanPointCreator build(IScanParameters params) throws Exception {
		if (params instanceof XasScanParameters xasParams) {
			return new ExafsScanPointCreator(xasParams);
		} else if (params instanceof XanesScanParameters xanesParams) {
			return new XanesScanPointCreator(xanesParams);
		}
		throw new IllegalArgumentException("Cannot make scan point creator for parameters of type "+params.getClass().getCanonicalName());
	}

}
