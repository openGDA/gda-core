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
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A test class for Jackson to de/serialize using modules from
 * https://mvnrepository.com/artifact/com.fasterxml.jackson.datatype/jackson-datatype-jdk8
 */
public class ConverterTestClass {
	private final int foo;
	private final Double bar;
	private final String baz;
	private final Duration duration;

	@JsonCreator
	public ConverterTestClass(
			@JsonProperty("foo") int foo,
			@JsonProperty("bar") Double bar,
			@JsonProperty("baz") String baz,
			@JsonProperty("duration") Duration duration) {
		this.foo = foo;
		this.bar = bar;
		this.baz = baz;
		this.duration = duration;
	}

	public int getFoo() {
		return foo;
	}

	public Optional<Double> getBar() {
		return Optional.ofNullable(bar);
	}

	public Optional<String> getBaz() {
		return Optional.ofNullable(baz);
	}

	public Duration getDuration() {
		return duration;
	}

	@Override
	public int hashCode() {
		return Objects.hash(bar, baz, duration, foo);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConverterTestClass other = (ConverterTestClass) obj;
		return Objects.equals(bar, other.bar) && Objects.equals(baz, other.baz)
				&& Objects.equals(duration, other.duration) && foo == other.foo;
	}

	@Override
	public String toString() {
		return "ConverterTestClass [foo=" + foo + ", bar=" + bar + ", baz=" + baz + ", duration=" + duration + "]";
	}
}
