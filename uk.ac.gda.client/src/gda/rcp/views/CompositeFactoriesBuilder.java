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

package gda.rcp.views;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Helps to assembles {@link CompositeFactory}
 * @author Maurizio Nagni
 */
public class CompositeFactoriesBuilder<T extends CompositeFactory> {

	private List<T> composites = new ArrayList<>();

	public CompositeFactoriesBuilder<T> add(T tabFactory) {
		composites.add(tabFactory);
		return this;
	}

	public final T[] build() {
		if (composites.isEmpty()) {
			return (T[]) composites.toArray();
		}
		return composites.toArray((T[]) Array.newInstance(composites.get(0).getClass(), 0));
	}
}
