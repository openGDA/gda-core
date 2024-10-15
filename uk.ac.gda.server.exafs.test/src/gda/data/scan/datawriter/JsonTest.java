/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package gda.data.scan.datawriter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import gda.util.JsonHelper;
import uk.ac.gda.beans.exafs.IonchamberOptimisationParams;

public class JsonTest {

	@Test
	public void testJson() {
		IonchamberOptimisationParams params = new IonchamberOptimisationParams();
		params.setAutoControl(true);
		params.setEnergies(new double[] {100,120,140});

		String jsonString = JsonHelper.toJson(params);
		System.out.print("Json string : "+jsonString);

		IonchamberOptimisationParams paramsFromJson = JsonHelper.createObject(jsonString, IonchamberOptimisationParams.class);

		assertEquals(params, paramsFromJson);
	}
}
