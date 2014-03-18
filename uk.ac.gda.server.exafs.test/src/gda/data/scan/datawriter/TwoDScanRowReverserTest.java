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

package gda.data.scan.datawriter;

import static org.junit.Assert.assertEquals;
import gda.scan.ScanDataPoint;

import org.junit.Test;

public class TwoDScanRowReverserTest {

	
	@Test
	public void NineByFourTest () {
		TwoDScanRowReverser indexer = new TwoDScanRowReverser();
		indexer.setNoOfColumns(9);
		indexer.setNoOfRows(4);
		indexer.setReverseOdd(true);
		indexer.setReverseEven(false);
		
		ScanDataPoint sdp = new ScanDataPoint();
		sdp.setCurrentPointNumber(0);
		indexer.indexData(sdp);
		assertEquals(0,sdp.getCurrentPointNumber());
		
		sdp = new ScanDataPoint();
		sdp.setCurrentPointNumber(6);
		indexer.indexData(sdp);
		assertEquals(6,sdp.getCurrentPointNumber());

		sdp = new ScanDataPoint();
		sdp.setCurrentPointNumber(11);
		indexer.indexData(sdp);
		assertEquals(15,sdp.getCurrentPointNumber());

		sdp = new ScanDataPoint();
		sdp.setCurrentPointNumber(16);
		indexer.indexData(sdp);
		assertEquals(10,sdp.getCurrentPointNumber());

		sdp = new ScanDataPoint();
		sdp.setCurrentPointNumber(20);
		indexer.indexData(sdp);
		assertEquals(20,sdp.getCurrentPointNumber());

		sdp = new ScanDataPoint();
		sdp.setCurrentPointNumber(24);
		assertEquals(24,sdp.getCurrentPointNumber());

	}
}
