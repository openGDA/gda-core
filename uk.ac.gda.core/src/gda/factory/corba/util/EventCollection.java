/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.factory.corba.util;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;

public class EventCollection extends ArrayList<Object> {

	private static final int ELEMENT_STRING_LIMIT = 20;

	public Object lastElement() {
		return get(size() -1);
	}

	public Object setLastElement(Object element) {
		return set(size() -1, element);
	}

	@Override
	public String toString() {
		// Only log first few items
		final boolean truncated = size() > ELEMENT_STRING_LIMIT;
		final String limitWarning = truncated ? String.format(" (first %d shown)", ELEMENT_STRING_LIMIT) : "";
		final String baseString = String.format("EventCollection(size=%d%s)[", size(), limitWarning);
		final String tailString = truncated ? ", ...]" : "]";
		return stream()
				.limit(ELEMENT_STRING_LIMIT)
				.map(Object::toString)
				.collect(joining(", ", baseString, tailString));
	}
}
