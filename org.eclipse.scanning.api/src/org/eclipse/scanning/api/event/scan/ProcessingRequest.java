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

package org.eclipse.scanning.api.event.scan;

import java.util.Collection;
import java.util.Map;

public class ProcessingRequest {

	Map<String, Collection<Object>> request;

	public ProcessingRequest() {

	}

	public Map<String, Collection<Object>> getRequest() {
		return request;
	}

	public void setRequest(Map<String, Collection<Object>> request) {
		this.request = request;
	}

}
