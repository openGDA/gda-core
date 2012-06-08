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

package uk.ac.gda.client.viewer;

import java.util.List;

public class SwtHitTestCalculator {

	private final SWT2DOverlayProvider provider;
	private final int x;
	private final int y;
	private List<Integer> cache;

	public SwtHitTestCalculator(SWT2DOverlayProvider provider, int x, int y) {
		this.provider = provider;
		this.x = x;
		this.y = y;
	}

	public int getPrimitiveID() {
		if (getPrimitiveIDs().size() > 0)
			return getPrimitiveIDs().get(getPrimitiveIDs().size() - 1);
		return -1;
	}

	public List<Integer> getPrimitiveIDs() {
		// possibly thread issue, but both calls should return
		// same value so no real need to synchronize
		if (cache == null) {
			cache = provider.hitTest(x, y);
		}
		return cache;
	}
}
