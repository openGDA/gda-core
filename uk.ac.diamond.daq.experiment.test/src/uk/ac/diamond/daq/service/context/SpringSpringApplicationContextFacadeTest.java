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

package uk.ac.diamond.daq.service.context;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;

/**
 * Test the {@link SpringApplicationContextFacade}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AcquisitionFileContextTestConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class SpringSpringApplicationContextFacadeTest {

	/**
	 * Closes the running Spring context, started by {@link SpringJUnit4ClassRunner}
	 * to verify {@link SpringApplicationContextFacade} cleans the context properly.
	 */
	@Test
	public void SpringApplicationContextFacadePreDestroyTest() {
		SpringApplicationContextFacade.getBean(SpringApplicationContextFacade.class).closeSpringFramework();
		// Uses this call as fails on a clean context
		Assert.assertFalse(SpringApplicationContextFacade.addApplicationListener(dummyListener));
	}

	private ApplicationListener<ApplicationEvent> dummyListener = new ApplicationListener<ApplicationEvent>() {
		@Override
		public void onApplicationEvent(ApplicationEvent event) {

		}
	};

}
