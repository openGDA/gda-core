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
	 * Determines the authorisation level of the given client. If another client holds the baton then the level is 0.
	 * 
	 * @param uniqueID
	 * @return the authorisation level
	 */
	public int getAuthorisationLevelOf(String uniqueID);

	/**
	 * This does not take the baton into account, this is simple what is in the authorisation database for the account
	 * the given Client is running as.
	 * 
	 * @param index
	 *            - the public index number of a client
	 * @return int the authorisation level of the given client
	 */
	public int getAuthorisationLevelOf(int index);

	/**
	 * Register a new facade to this class
	 * 
	 * @param uniqueID
	 * @param info
	 */
	public void addFacade(String uniqueID, ClientDetails info);

	/**
	 * Switches the user of the given facade.
	 * 
	 * @param uniqueFacadeName
	 * @param username
	 * @param accessLevel
	 * @param visitID
	 */
	public void switchUser(String uniqueFacadeName, String username, int accessLevel, String visitID);

	/**
	 * Remove a facade registered in this class
	 * 
	 * @param uniqueID
	 */
	public void removeFacade(String uniqueID);

	public int getNewFacadeIndex();

	/**
	 * @param myJSFIdentifier
	 * @return boolean
	 * @see gda.jython.Jython#amIBatonHolder(java.lang.String)
	 */
	public boolean amIBatonHolder(String myJSFIdentifier);

	/**
	 * @param myJSFIdentifier
	 * @param indexOfReciever
	 * @see gda.jython.Jython#assignBaton(String, int)
	 */
	public void assignBaton(String myJSFIdentifier, int indexOfReciever);

	/**
	 * @param myJSFIdentifier
	 * @return ClientDetails
	 */
	public ClientDetails getClientInformation(String myJSFIdentifier);

	/**
	 * @param myJSFIdentifier
	 * @return ClientDetails[]
	 * @see gda.jython.Jython#getOtherClientInformation(String)
	 */
	public ClientDetails[] getOtherClientInformation(String myJSFIdentifier);

	/**
	 * @param uniqueIdentifier
	 * @return boolean
	 * @see gda.jython.Jython#requestBaton(String)
	 */
	public boolean requestBaton(String uniqueIdentifier);

	/**
	 * @param uniqueIdentifier
	 * @see gda.jython.Jython#returnBaton(String)
	 */
	public void returnBaton(String uniqueIdentifier);

	/**
	 * @param myJSFIdentifier
	 * @return true if the given id string matches one registered by this class
	 */
	public boolean isJSFRegistered(String myJSFIdentifier);

	/**
	 * @return boolean
	 * @see gda.jython.Jython#isBatonHeld()
	 */
	public boolean isBatonHeld();

	public boolean isDisableControlOverVisitMetadataEntry();

	public void setDisableControlOverVisitMetadataEntry(boolean disableControlOverVisitMetadataEntry);
	
	/**
	 * Returns details of all connected clients.
	 */
	public List<ClientDetailsAndLeaseState> getAllClients();

}