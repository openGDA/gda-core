/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.tool;

import static org.junit.Assert.assertNotNull;

import org.eclipse.swt.graphics.Image;
import org.junit.Ignore;
import org.junit.Test;

public class ClientSWTElementTest {

//	@Before
//	public void setUp() throws Exception {
//
//		ClientManager.setTestingMode(true);
//
//		GDAClientActivator.getDefault().getPreferenceStore().setValue(PreferenceConstants.MAX_SIZE_CACHED_DATA_POINTS,1000);
//	}

	/**
	 * Loads an image from the resources.
	 */
	@Test
	@Ignore //because it complains about eclipses object. To be done.
	public void getImageFromTest() {
		Image image = ClientSWTElements.getImage(getClass(), "/resources/test_icon.png");
		assertNotNull(image);
	}

}
