/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package uk.ac.gda.core.tool.spring;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * Test the {@link SpringApplicationContextFacade} without a Spring context
 */
public class NoSpringSpringApplicationContextFacadeTest {

	/**
	 * With no configurableApplicationContext should return {@code false}
	 */
	@Test
	public void testNullConfigurableApplicationContext() {
		Assert.assertFalse(SpringApplicationContextFacade.addApplicationListener(dummyListener));
	}

	private ApplicationListener<ApplicationEvent> dummyListener = new ApplicationListener<ApplicationEvent>() {
		@Override
		public void onApplicationEvent(ApplicationEvent event) {

		}
	};

}
