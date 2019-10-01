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

package uk.ac.diamond.daq.persistence.manager;

import java.util.HashSet;
import java.util.Set;

import uk.ac.diamond.daq.persistence.implementation.service.VisitService;
import uk.ac.diamond.daq.persistence.implementation.service.VisitServiceListener;

public class TestVisitService implements VisitService {

	private String currentVisitId;
    private Set<VisitServiceListener> listeners = new HashSet<>();

	public TestVisitService(String currentVisitId) {
		this.currentVisitId = currentVisitId;
	}

	@Override
	public void addListener(VisitServiceListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(VisitServiceListener listener) {
		listeners.remove(listener);
	}

	@Override
	public String getCurrentVisitId() {
		return currentVisitId;
	}

	@Override
	public void setCurrentVisitId(String currentVisitId) {
		this.currentVisitId = currentVisitId;
	}
}