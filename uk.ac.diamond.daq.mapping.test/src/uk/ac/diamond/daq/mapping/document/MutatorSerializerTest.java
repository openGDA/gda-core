/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.document;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import gda.mscan.element.Mutator;
import uk.ac.diamond.daq.mapping.api.document.deserializer.MutatorDeserializer;
import uk.ac.gda.common.exception.GDAException;

/**
 * Tests for the {@link MutatorDeserializer}
 *
 * @author Maurizio Nagni
 */
public class MutatorSerializerTest extends DocumentTestBase {


	@Before
	public void before() {
	}

	@Test
	public void serialiseMutatorTest() throws GDAException {
		ClassWithMutatorMap testClass = new ClassWithMutatorMap();
		testClass.put(Mutator.ALTERNATING, "One");
		testClass.put(Mutator.CONTINUOUS, "Two");
		String document = serialiseDocument(testClass);
		assertThat(document, containsString("\"ALTERNATING\" : \"One\""));
		assertThat(document, containsString("\"CONTINUOUS\" : \"Two\""));
	}

	@Test
	public void deserialiseMutatorTest() throws GDAException {
		ClassWithMutatorMap testClass = deserialiseDocument("test/resources/MutatorTest.json",
				ClassWithMutatorMap.class);
		Assert.assertEquals("One", testClass.getMutators().get(Mutator.ALTERNATING));
		Assert.assertEquals("Two", testClass.getMutators().get(Mutator.CONTINUOUS));
	}
}
