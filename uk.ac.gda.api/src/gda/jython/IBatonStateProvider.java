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

package gda.jython;

import gda.jython.batoncontrol.ClientDetails;
import gda.observable.IObserver;


/**
 * 
 */
public interface IBatonStateProvider{

	/**
	 * @return boolean
	 * @see gda.jython.Jython#isBatonHeld()
	 */
	public boolean isBatonHeld();
	
	/**
	 * @param anObserver
	 */
	void addBatonChangedObserver(IObserver anObserver);

	/**
	 * @param anObserver
	 */
	void deleteBatonChangedObserver(IObserver anObserver);
	
	/**
	 * @return boolean
	 * @see gda.jython.Jython#amIBatonHolder(String)
	 */
	public boolean amIBatonHolder();

	/**
	 * Returns the baton holder from the list of known clients connected, even if the baton holder is this client.
	 * 
	 * @return ClientDetails
	 */	
	public ClientDetails getBatonHolder();
	
	/**
	 * @see gda.jython.Jython#requestBaton(String)
	 */
	public void returnBaton();

	/**
	 * @return boolean
	 * @see gda.jython.Jython#requestBaton(String)
	 */
	public boolean requestBaton();
	
	/**
	 * @param index
	 * @see gda.jython.Jython#assignBaton(String,int)
	 */
	public void assignBaton(int index);

	/**
	 * @return ClientDetails[]
	 * @see gda.jython.Jython#getOtherClientInformation(String)
	 */
	public ClientDetails[] getOtherClientInformation();
	
	/**
	 * Returns the current ClientDetails for this client. What is returned will change depending on the baton status and
	 * if the user has been switched using the switchUser method.
	 * <p>
	 * The authorisationLevel returned takes into account any alternate user, but not whether the baton is held. For
	 * this, call getAuthorisationLevel(). 
	 * 
	 * @return ClientDetails
	 */
	public ClientDetails getMyDetails();
	
	/**
	 * @param dataSource
	 * @param data
	 */
	public void update(Object dataSource, Object data);
	
	/**
	 * Reverts to the original user this client was initially logged in as.
	 * 
	 * @see gda.jython.Jython#switchUser(String,String,String)
	 */
	public void revertToOriginalUser();

	/**
	 * Switches the visit this client will collect data as when it holds the baton.
	 * 
	 * @param visitID
	 */	
	public void changeVisitID(String visitID); 	
	
	/**
	 * Broadcast a message to other users on this beamline. Such messages will be displayed in a special viewer.
	 * @param message
	 */	
	void sendMessage(String message);
	
	/**
	 * @param username
	 * @param password
	 * @return true if switch successful
	 * @see gda.jython.Jython#switchUser(String,String,String)
	 */

	public boolean switchUser(String username, String password);
}
