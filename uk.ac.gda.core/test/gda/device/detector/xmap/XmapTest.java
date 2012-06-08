/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.detector.xmap;

import gda.factory.Finder;
import gda.factory.ObjectFactory;

import org.junit.BeforeClass;

/**
 *
 */
public class XmapTest {

	/**
	 * 
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
		ObjectFactory factory = new ObjectFactory();
		Finder finder = Finder.getInstance();
		finder.addFactory(factory);
		DummyXmapController xmapController = new DummyXmapController();
		xmapController.setName("xmap");
		factory.addFindable(xmapController);
	
	}

}
