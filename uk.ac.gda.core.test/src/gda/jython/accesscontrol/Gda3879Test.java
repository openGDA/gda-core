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

package gda.jython.accesscontrol;

import junit.framework.TestCase;
import gda.configuration.properties.LocalProperties;
import gda.device.motor.DummyMotor;
import gda.device.scannable.ScannableMotor;
import gda.jython.accesscontrol.RbacUtils;

public class Gda3879Test extends TestCase {

	public void testGda3879() throws Exception {
		
		final double DELTA = 0.0001;
		
		LocalProperties.set(LocalProperties.GDA_ACCESS_CONTROL_ENABLED, "true");
		
		DummyMotor motor = new DummyMotor();
		motor.setName("motor");
		motor.setPosition(0.0);
		motor.setSpeed(10.0);
		
		ScannableMotor bragg = new ScannableMotor();
		bragg.setName("bragg");
		bragg.setMotor(motor);
		bragg.setScalingFactor(-1.0);
		
		ScannableMotor bragg2 = (ScannableMotor) RbacUtils.wrapFindableWithInterceptor(bragg);
		assertTrue(RbacUtils.objectIsCglibProxy(bragg2));
		
		motor.configure();
		bragg2.configure();
		
		assertEquals(0.0, (Double) bragg2.getPosition(), DELTA);
		assertEquals(0.0, motor.getPosition(), DELTA);
		
		bragg2.moveTo(1.0);
		
		assertEquals(1.0, (Double) bragg2.getPosition(), DELTA);
		assertEquals(-1.0, motor.getPosition(), DELTA);
		
		bragg2.moveTo(2.0);
		
		assertEquals(2.0, (Double) bragg2.getPosition(), DELTA);
		assertEquals(-2.0, motor.getPosition(), DELTA);
	}

}
