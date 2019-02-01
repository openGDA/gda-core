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

package uk.ac.gda.remoting.server;

import java.io.Serializable;

/**
 * An immutable POJO object for passing information about exported RMI objects from the server to the client
 *
 * @author James Mudd
 * @since GDA 9.12
 */
public class RmiObjectInfo implements Serializable {

	/** The name of the remote object */
	private final String name;
	/** The URL the RMI object is avaliable at */
	private final String url;
	/** The fully qualified class name of the service interface */
	private final String serviceInterface;
	/** If the object support events */
	private final boolean events;

	public RmiObjectInfo(String name, String url, String serviceInterface, boolean events) {
		super();
		this.name = name;
		this.url = url;
		this.serviceInterface = serviceInterface;
		this.events = events;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public String getServiceInterface() {
		return serviceInterface;
	}

	public boolean isEvents() {
		return events;
	}

}
