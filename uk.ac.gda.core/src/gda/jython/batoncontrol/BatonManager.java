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

import gda.configuration.properties.LocalProperties;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.IMetadataEntry;
import gda.data.metadata.Metadata;
import gda.data.metadata.StoredMetadataEntry;
import gda.device.DeviceException;
import gda.jython.InterfaceProvider;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component used by JythonServer to manage the list of clients registered to that server. If enabled, there is a baton
 * which can be 'requested' by one of the client which prevents the other clients from operating equipment. The baton
 * can be passed from the holder to another client, or taken from a client by a higher level user, or released by the
 * holder. If no one holds the baton then any client can operate equipment.
 */
public class BatonManager implements IBatonManager {

	private static final Logger logger = LoggerFactory.getLogger(BatonManager.class);

	private final long LEASETIMEOUT = 60000; // 1 minute in milliseconds

	private int facadeIndex = 0;

	private String batonHolder = "";

	// holds <servername, access level>
	private volatile Hashtable<String, ClientInfo> facadeNames = new Hashtable<String, ClientInfo>(20, 0.25F);

	// holds <unique id, time lease renewed>. Those Clients who have recently interacted with the Jython Server.
	private volatile Hashtable<String, Long> leaseHolders = new Hashtable<String, Long>();

	private boolean firstClientTakesBaton = false;

	private boolean useBaton = false;

	private boolean useRBAC = false;
	
	private boolean disableControlOverVisitMetadataEntry = false;

	public BatonManager() {

		firstClientTakesBaton = LocalProperties.get("gda.accesscontrol.firstClientTakesBaton", "true").equals("true");

		useBaton = LocalProperties.isBatonManagementEnabled();

		useRBAC = LocalProperties.isAccessControlEnabled();

		new leaseRefresher().start();
	}

	@Override
	public int getAuthorisationLevelOf(String uniqueID) {

		if (!useRBAC) {
			return 0;
		}

		int authLevel = facadeNames.get(uniqueID).authorisationLevel;

		// always skip if its a server
		if (authLevel == Integer.MAX_VALUE) {
			return authLevel;
		}

		renewLease(uniqueID);

		// if we are not using batons
		if (!useBaton) {
			return authLevel;
		}

		// if am the baton holder
		if (amIBatonHolder(uniqueID, false)) {
			return authLevel;
		}

		// else do not have control!
		return 0;
	}

	@Override
	public int getAuthorisationLevelOf(int index) {
		if (!useRBAC) {
			return 0;
		}
		String uniqueID = idFromIndex(index);
		return facadeNames.get(uniqueID).authorisationLevel;
	}

	@Override
	public void addFacade(String uniqueID, ClientDetails info) {

		// must have a meaningful identifier
		if (uniqueID != null && uniqueID != "") {

			facadeNames.put(uniqueID, info.copy());

			// if baton control not in use and this is the only client, the set this as the baton holder (meaning in 
			// this case the only client. This is useful as it gives this client certain privileges in this class
			// which subsequent clients do not have e.g. to set the visit ID).
			if (!useBaton && leaseHolders.size() == 0 && !info.userID.equals("")) {
				changeBatonHolder(uniqueID);
			}
			// if baton in use and firstClientTakesBaton flag set and this is the first client
			else if (firstClientTakesBaton && useBaton && leaseHolders.size() == 0 && !info.userID.equals("")) {
				changeBatonHolder(uniqueID);
			}

			// skip this part if an object server
			if (info.authorisationLevel != Integer.MAX_VALUE) {
				renewLease(uniqueID);
				notifyServerOfBatonChange();
			}
		}
	}

	@Override
	public void switchUser(String uniqueFacadeName, String username, int accessLevel, String visitID) {
		// overwrite the entry in facadeNames
		ClientInfo info = facadeNames.get(uniqueFacadeName);
		
		boolean changeMade = false;
		//only change if information is supplied
		if (username != null && !username.equals("")){
			info.userID = username;
			info.authorisationLevel = accessLevel;
			changeMade = true;
		}
		if (visitID != null && !visitID.equals("")){
			info.visitID = visitID;
			changeMade = true;
		}
		
		facadeNames.put(uniqueFacadeName, info);
		
		// if the baton holder then ensure all information is refreshed
		if (amIBatonHolder(uniqueFacadeName,false)){
			changeBatonHolder(uniqueFacadeName);
		} else if (changeMade){
			//do a refresh anyway if any changes had been made
			notifyServerOfBatonChange();
		}
	}

	@Override
	public void removeFacade(String uniqueID) {
		returnBaton(uniqueID);
		facadeNames.remove(uniqueID);
		leaseHolders.remove(uniqueID);
		notifyServerOfBatonChange();
	}

	@Override
	public int getNewFacadeIndex() {
		int returnValue = facadeIndex;
		facadeIndex++;
		return returnValue;
	}

	@Override
	public boolean amIBatonHolder(String myJSFIdentifier) {
		return useBaton ? amIBatonHolder(myJSFIdentifier, true) : true;
	}

	@Override
	public void assignBaton(String myJSFIdentifier, int indexOfReciever) {
		if (this.batonHolder.equals(myJSFIdentifier)) {
			String idOfNewHolder = idFromIndex(indexOfReciever);
			if (idOfNewHolder != null) {
				changeBatonHolder(idOfNewHolder);
				renewLease(idOfNewHolder);
			}
			renewLease(myJSFIdentifier);
		}

	}

	@Override
	public ClientDetails getClientInformation(String myJSFIdentifier) {
		boolean hasBaton = amIBatonHolder(myJSFIdentifier, false);
		ClientInfo info = facadeNames.get(myJSFIdentifier);
		return new ClientDetails(info, hasBaton);
	}

	@Override
	public ClientDetails[] getOtherClientInformation(String myJSFIdentifier) {
		renewLease(myJSFIdentifier);
		ClientDetails[] array = new ClientDetails[0];

		// loop through facades and find matching index
		for (String uniqueID : facadeNames.keySet()) {
			boolean hasBaton = amIBatonHolder(uniqueID, false);
			ClientInfo info = facadeNames.get(uniqueID);
			ClientDetails details = new ClientDetails(info, hasBaton);

			// add other clients whose lease has not run out
			// (so ignore Object Servers and Clients who have probably died and not de-registered)
			if (!uniqueID.equals(myJSFIdentifier) && !details.userID.equals("")
					&& leaseHolders.containsKey(idFromIndex(details.index))) {
				array = (ClientDetails[]) ArrayUtils.add(array, details);
			}
		}
		return array;
	}

	@Override
	public synchronized boolean requestBaton(String uniqueIdentifier) {

		// if am already baton holder
		if (this.batonHolder.equals(uniqueIdentifier)) {
			return true;
		}

		// if no baton holder
		if (this.batonHolder.equals("") && isJSFRegistered(uniqueIdentifier)) {
			changeBatonHolder(uniqueIdentifier);
			return true;
		}

		// if there is a baton holder
		if (isJSFRegistered(uniqueIdentifier)) {
			ClientInfo currentHolder = facadeNames.get(this.batonHolder);
			ClientInfo other = facadeNames.get(uniqueIdentifier);

			// if requester has higher auth than current baton holder then take
			if (other.authorisationLevel > currentHolder.authorisationLevel
					|| (other.userID.equals(currentHolder.userID) && other.authorisationLevel == currentHolder.authorisationLevel)) {
				changeBatonHolder(uniqueIdentifier);
				return true;
			}
			// else sent out request message and see if the request is granted
			InterfaceProvider.getJythonServerNotifer().notifyServer(this,
					new BatonRequested(new ClientDetails(other, false)));
		}

		// if get here then cannot take baton
		return false;
	}

	private void changeBatonHolder(String uniqueIdentifier) {
		this.batonHolder = uniqueIdentifier;

		if (!uniqueIdentifier.equals("")) {
			//log any change
			if (!uniqueIdentifier.equals(batonHolder)){
				logger.info("Baton now held by " + facadeNames.get(uniqueIdentifier).userID);
			}
			changeUserIDDefinedMetadata(uniqueIdentifier);
		}

		notifyServerOfBatonChange();
	}

	private void changeUserIDDefinedMetadata(String uniqueIdentifier) {
		// refresh the pieces of metadata which holds the current visit id and username
		try {
			Metadata metadata = GDAMetadataProvider.getInstance();
			if (metadata != null) {

				String currentUser = facadeNames.get(uniqueIdentifier).getUserID();
				String visitID = facadeNames.get(uniqueIdentifier).getVisitID();

				// first change the metadata values for current user
				if (metadataContainsKey(metadata,"userid")){
					metadata.setMetadataValue("userid", currentUser);
				} else {
					StoredMetadataEntry userid = new StoredMetadataEntry();
					userid.setName("userid");
					userid.setValue(currentUser);
					userid.setDefEntryName("");
					metadata.addMetadataEntry(userid);
				}
				if (metadataContainsKey(metadata,"federalid")){
					metadata.setMetadataValue("federalid", currentUser);
				} else {
					StoredMetadataEntry federalid = new StoredMetadataEntry();
					federalid.setName("federalid");
					federalid.setValue(currentUser);
					federalid.setDefEntryName("");
					metadata.addMetadataEntry(federalid);
				}
				// then ensure that the information fetched from the icat database is refreshed for the new user
				if (isDisableControlOverVisitMetadataEntry()) {
					logger.info("Ingoring client request to change visit to: '" + visitID + "'");
				} else {
					if (metadataContainsKey(metadata, "visit")) {
						metadata.setMetadataValue("visit", visitID);
					} else {
						StoredMetadataEntry visitid = new StoredMetadataEntry();
						visitid.setName("visit");
						visitid.setValue(visitID);
						visitid.setDefEntryName("");
						metadata.addMetadataEntry(visitid);
					}
				}
			}
		} catch (Exception e) {
			logger
					.warn("Exception while BatonManager changing the username stored in metadata. This could cause problems with data collection. Error was: "
							+ e.getMessage());
		}
	}
	
	private boolean metadataContainsKey(Metadata metadata, String key){
		try {
			ArrayList<IMetadataEntry> entries  = metadata.getMetadataEntries();
			
			for(IMetadataEntry entry : entries){
				if (entry.getName().equals(key)){
					return true;
				}
			}
			return false;
		} catch (DeviceException e) {
			return false;
		}
	}
	
	@Override
	public void returnBaton(String uniqueIdentifier) {
		if (this.batonHolder.equals(uniqueIdentifier) && isJSFRegistered(uniqueIdentifier)) {
			changeBatonHolder("");
			renewLease(uniqueIdentifier);
		}
	}

	@Override
	public boolean isJSFRegistered(String myJSFIdentifier) {
		for (String uniqueID : facadeNames.keySet()) {
			if (myJSFIdentifier.equals(uniqueID) && !myJSFIdentifier.equals("")) {
				renewLease(myJSFIdentifier);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isBatonHeld() {
		return !this.batonHolder.equals("");
	}

	private boolean amIBatonHolder(String myJSFIdentifier, boolean refresh) {
		if (useBaton) {
		if (refresh) {
			renewLease(myJSFIdentifier);
		}
		return this.batonHolder.equals(myJSFIdentifier);
	}
		return true;
	}

	private String idFromIndex(int index) {
		// loop through facades and find matching index
		for (String uniqueID : facadeNames.keySet()) {
			ClientInfo details = facadeNames.get(uniqueID);
			if (details.index == index) {
				return uniqueID;
			}
		}
		return null;
	}

	private void notifyServerOfBatonChange() {
		// during object server startup, this may come back null
		if (InterfaceProvider.getJythonServerNotifer() != null) {
			InterfaceProvider.getJythonServerNotifer().notifyServer(this, new BatonChanged());
		}
	}

	private void notifyServerOfBatonLeaseRenewRequest() {
		// during object server startup, this may come back null
		if (InterfaceProvider.getJythonServerNotifer() != null) {
			InterfaceProvider.getJythonServerNotifer().notifyServer(this, new BatonLeaseRenewRequest());
		}
	}
	
	private synchronized void renewLease(String myJSFIdentifier) {
		// update the start time of this lease, but only for clients
		if (facadeNames.containsKey(myJSFIdentifier) && !facadeNames.get(myJSFIdentifier).userID.equals("")) {
			leaseHolders.put(myJSFIdentifier, new GregorianCalendar().getTimeInMillis());
		}
	}

	@Override
	public boolean isDisableControlOverVisitMetadataEntry() {
		return disableControlOverVisitMetadataEntry;
	}

	@Override
	public void setDisableControlOverVisitMetadataEntry(boolean disableControlOverVisitMetadataEntry) {
		this.disableControlOverVisitMetadataEntry = disableControlOverVisitMetadataEntry;
	}

	/**
	 * Loops continually, informing GUIs to get in touch else lose their lease. Baton holders whose lose their lease
	 * lose the baton.
	 */
	private class leaseRefresher extends Thread {
		
		public leaseRefresher(){
			super("BatonManagerLeaseRefresher");
		}

		@Override
		public void run() {

			while (useBaton) {
				try {
					// clean up the list
					removeTimeoutLeases();
					Thread.sleep(LEASETIMEOUT/2-2000);
					// send out a notification to all clients, forcing them to update their UI and so updating their
					// leases
					notifyServerOfBatonLeaseRenewRequest();
					Thread.sleep(2000);
				} catch (Exception e) {
					logger.error("Error sending lease renew request",e);
					//this can happen because the JythonServer has not yet been added to the finder.
					// ignore and carry on
					//TODO pass IJythonServerNotifer as construction argument
				}
			}
		}

		/**
		 * Remove from the list of leased clients those which have not communicated for some time.
		 */
		private synchronized void removeTimeoutLeases() {
			// refresh all other leases
			Long now = new GregorianCalendar().getTimeInMillis();

			String[] clientIDs = leaseHolders.keySet().toArray(new String[0]);

			for (int i = 0; i < clientIDs.length; i++) {
				Long leaseStart = leaseHolders.get(clientIDs[i]);
				if (now - leaseStart > LEASETIMEOUT) {
					leaseHolders.remove(clientIDs[i]);
					if (amIBatonHolder(clientIDs[i], false)) {
						logger.warn("Baton holder timeout, so baton released after " + ((now - leaseStart) / 1000)
								+ "s");
						changeBatonHolder("");
					}
				}
			}
		}
	}
	
	@Override
	public List<ClientDetails> getAllClients() {
		final List<ClientDetails> clients = new ArrayList<ClientDetails>();
		for (Map.Entry<String, ClientInfo> entry : facadeNames.entrySet()) {
			final String uniqueId = entry.getKey();
			final ClientInfo info = entry.getValue();
			if (!info.isServer()) {
				final boolean hasBaton = amIBatonHolder(uniqueId, false);
				final ClientDetails details = new ClientDetails(info, hasBaton);
				clients.add(details);
			}
		}
		return clients;
	}
}
