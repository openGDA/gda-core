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

package gda.device.detector.xspress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class Xspress1RelinearizeTest {

	@Test
	public void testXspress1Relinearize() {
		Xspress1System xspress = Mockito.mock(Xspress1System.class);
		double deadtime =  2.5454E-7;
		double collectionTime;
		{
			long windowed = 86;
			long total = 46946;
			long resets = 6454;

			collectionTime = 1.0;			
			Mockito.when(xspress.relinearize(total, resets, windowed, deadtime, collectionTime)).thenReturn(87);
			collectionTime = 3.0;			
			Mockito.when(xspress.relinearize(total, resets, windowed, deadtime, collectionTime)).thenReturn(86);
			collectionTime = 5.0;			
			Mockito.when(xspress.relinearize(total, resets, windowed, deadtime, collectionTime)).thenReturn(86);
		}
		{
			long windowed = 8382;
			long total = 15941;
			long resets = 18953;

			collectionTime = 1.0;			
			Mockito.when(xspress.relinearize(total, resets, windowed, deadtime, collectionTime)).thenReturn(8432);
			collectionTime = 3.0;			
			Mockito.when(xspress.relinearize(total, resets, windowed, deadtime, collectionTime)).thenReturn(8398);
			collectionTime = 5.0;			
			Mockito.when(xspress.relinearize(total, resets, windowed, deadtime, collectionTime)).thenReturn(8391);
		}
	}
}
