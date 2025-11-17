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

package gda.jython.batoncontrol;

import java.util.List;

public interface IBatonManager {

	/**
	 * Determines the current authorisation level of the given client.
	 * If this server has RBAC enabled and another client holds the baton
	 * then the effective level is 0.
	 *
	 * @param uniqueID The UUID of the client facade
	 * @return the effective authorisation level
	 */
	int effectiveAuthorisationLevelOf(String uniqueID);

	/**
	 * Register a new facade to this class
	 *
	 * @param uniqueID
	 * @param info
	 */
	void addFacade(String uniqueID, ClientDetails info);

	/**
	 * Switches the user of the given facade.
	 *
	 * @param uniqueFacadeName
	 * @param username
	 * @param accessLevel
	 * @param visitID
	 */
	void switchUser(String uniqueFacadeName, String username, int accessLevel, String visitID);

	/**
	 * Remove a facade registered in this class
	 *
	 * @param uniqueID
	 */
	void removeFacade(String uniqueID);

	int getNewFacadeIndex();

	/**
	 * @param myJSFIdentifier
	 * @return boolean
	 * @see gda.jython.Jython#amIBatonHolder(java.lang.String)
	 */
	boolean amIBatonHolder(String myJSFIdentifier);

	/**
	 * @param myJSFIdentifier
	 * @param indexOfReciever
	 * @param indexOfPasser
	 * @see gda.jython.Jython#assignBaton(String, int, int)
	 */
	void assignBaton(String myJSFIdentifier, int indexOfReciever, int indexOfPasser);

	/**
	 * @param myJSFIdentifier
	 * @return ClientDetails
	 */
	ClientDetails getClientInformation(String myJSFIdentifier);

	/**
	 * @param myJSFIdentifier
	 * @return ClientDetails[]
	 * @see gda.jython.Jython#getOtherClientInformation(String)
	 */
	ClientDetails[] getOtherClientInformation(String myJSFIdentifier);

	/**
	 * @param uniqueIdentifier
	 * @return boolean
	 * @see gda.jython.Jython#requestBaton(String)
	 */
	boolean requestBaton(String uniqueIdentifier);

	/**
	 * @param uniqueIdentifier
	 * @see gda.jython.Jython#returnBaton(String)
	 */
	void returnBaton(String uniqueIdentifier);

	/**
	 * @param myJSFIdentifier
	 * @return true if the given id string matches one registered by this class
	 */
	boolean isJSFRegistered(String myJSFIdentifier);

	/**
	 * @return boolean
	 * @see gda.jython.Jython#isBatonHeld()
	 */
	boolean isBatonHeld();

	boolean isDisableControlOverVisitMetadataEntry();

	void setDisableControlOverVisitMetadataEntry(boolean disableControlOverVisitMetadataEntry);

	/**
	 * Returns details of all connected clients.
	 */
	List<ClientDetailsAndLeaseState> getAllClients();
}