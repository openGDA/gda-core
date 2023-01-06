/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.util;

import java.time.Duration;
import java.util.Optional;

/**
 * A test record for Jackson to de/serialize using modules from
 * https://mvnrepository.com/artifact/com.fasterxml.jackson.datatype/jackson-datatype-jdk8
 */
public record ConverterTestRecord (
		int foo,
		Optional<Double> bar,
		Optional<String> baz,
		Duration duration) {}
