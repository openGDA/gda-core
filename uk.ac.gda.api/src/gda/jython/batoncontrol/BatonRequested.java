/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.jython.batoncontrol;

import java.io.Serializable;

/**
 * Message sent out when a request for the baton is made
 */
public class BatonRequested implements Serializable{
	
	/**
	 * @return Returns the requester.
	 */
	public ClientDetails getRequester() {
		return requester;
	}

	/**
	 * @param requester The requester to set.
	 */
	public void setRequester(ClientDetails requester) {
		this.requester = requester;
	}

	/**
	 * @param requester
	 */
	public BatonRequested(ClientDetails requester) {
		super();
		this.requester = requester;
	}

	private ClientDetails requester; 

}
