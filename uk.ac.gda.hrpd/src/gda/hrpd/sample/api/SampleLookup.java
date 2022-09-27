/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.hrpd.sample.api;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface SampleLookup extends Iterable<SampleMetadata> {
	default Collection<SampleMetadata> getSamples() {
		return stream().collect(toList());
	}
	default Stream<SampleMetadata> stream() {
		return StreamSupport.stream(spliterator(), false);
	}
}
