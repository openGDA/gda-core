/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.detector.addetector.triggering;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class AbstractCollectionStrategyDecoratorTest {

	private SimpleAcquire mockSimpleAcquire;
	private UnsynchronisedExternalShutterDecorator unsynchronisedExternalShutterDecorator;
	private MerlinColourModeDecorator merlinColourModeDecorator;

	@Before
	public void setUp() {
		mockSimpleAcquire = mock(SimpleAcquire.class);
		merlinColourModeDecorator = new MerlinColourModeDecorator();
		unsynchronisedExternalShutterDecorator = new UnsynchronisedExternalShutterDecorator();
	}

	@Test
	public void expectEmptyListWithNoIncompatibleDecorators() throws Exception {
		merlinColourModeDecorator.setDecoratee(mockSimpleAcquire);
		merlinColourModeDecorator.afterPropertiesSet();
		List<UnsynchronisedExternalShutterNXCollectionStrategy> unsynchronisedExternalShutterNXCollectionStrategies =
				merlinColourModeDecorator.getDecorateesOfType(UnsynchronisedExternalShutterNXCollectionStrategy.class);

		assertEquals(0, unsynchronisedExternalShutterNXCollectionStrategies.size());
	}

	@Test
	public void expectCorrectSizeListOnCompatibleInnerDecorator() throws Exception {
		unsynchronisedExternalShutterDecorator.setDecoratee(mockSimpleAcquire);
		unsynchronisedExternalShutterDecorator.afterPropertiesSet();
		merlinColourModeDecorator.setDecoratee(unsynchronisedExternalShutterDecorator);
		merlinColourModeDecorator.afterPropertiesSet();
		List<UnsynchronisedExternalShutterNXCollectionStrategy> unsynchronisedExternalShutterNXCollectionStrategies =
				merlinColourModeDecorator.getDecorateesOfType(UnsynchronisedExternalShutterNXCollectionStrategy.class);

		assertEquals(1, unsynchronisedExternalShutterNXCollectionStrategies.size());
	}

	@Test
	public void expectCorrectSizeListOnCompatibleOuterDecorator() throws Exception {
		merlinColourModeDecorator.setDecoratee(mockSimpleAcquire);
		merlinColourModeDecorator.afterPropertiesSet();
		unsynchronisedExternalShutterDecorator.setDecoratee(merlinColourModeDecorator);
		unsynchronisedExternalShutterDecorator.afterPropertiesSet();
		List<UnsynchronisedExternalShutterNXCollectionStrategy> unsynchronisedExternalShutterNXCollectionStrategies =
				unsynchronisedExternalShutterDecorator.getDecorateesOfType(UnsynchronisedExternalShutterNXCollectionStrategy.class);

		assertEquals(1, unsynchronisedExternalShutterNXCollectionStrategies.size());
	}
}
