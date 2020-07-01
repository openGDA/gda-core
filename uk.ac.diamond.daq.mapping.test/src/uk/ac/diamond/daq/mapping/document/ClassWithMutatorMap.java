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

import java.util.EnumMap;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import gda.mscan.element.Mutator;
import uk.ac.diamond.daq.mapping.api.document.deserializer.MutatorDeserializer;

public class ClassWithMutatorMap {
	@JsonDeserialize(keyUsing = MutatorDeserializer.class)
	private Map<Mutator, String> mutators = new EnumMap<>(Mutator.class);

	public void put(Mutator key, String value) {
		mutators.put(key, value);
	}

	public Map<Mutator, String> getMutators() {
		return mutators;
	}



}