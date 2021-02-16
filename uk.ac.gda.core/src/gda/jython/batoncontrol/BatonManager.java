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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.IMetadataEntry;
import gda.data.metadata.Metadata;
import gda.data.metadata.StoredMetadataEntry;
import gda.jython.IJythonServerNotifer;
import gda.jython.InterfaceProvider;

/**
 * Component used by JythonServer to manage the list of clients registered to that server. If enabled, there is a baton which can be 'requested' by one of the
 * client which prevents the other clients from operating equipment. The baton can be passed from the holder to another client, or taken from a client by a
 * higher level user, or released by the holder. If no one holds the baton then any client can operate equipment.
 * <p>
 * 09 Oct 2015 {@link <a href="http://jira.diamond.ac.uk/browse/DATAACQTEAM-154">DATAACQTEAM-154</a>}
 * <p>
 * Changes made to stop accessing the fields of ClientInfo directly as this causes IllegalAccessExceptions under OSGI due to the split package problem. Instead
 * the (already present) mutators are now used
 */
public class BatonManager implements IBatonManager {

	private static final Logger logger = LoggerFactory.getLogger(BatonManager.class);

	private static final long LEASE_TIMEOUT = 60000; // 1 minute in milliseconds

	private AtomicInteger facadeIndex = new AtomicInteger();

	private String batonHolder = "";

	// holds <servername, access level>
	private final Map<String, ClientInfo> facadeNames = new ConcurrentHashMap<>();

	// holds <unique id, time lease renewed>. Those Clients who have recently interacted with the Jython Server.
	private final Map<String, Long> leaseHolders = new ConcurrentHashMap<>();

	private final boolean firstClientTakesBaton;

	private final boolean useBaton;

	private final boolean useRBAC;

	private boolean disableControlOverVisitMetadataEntry = false;

	public BatonManager() {

		useBaton = LocalProperties.isBatonManagementEnabled();

		if (!useBaton) {
			// if baton control not in use and this is the only client, then set this as the baton
			// holder (meaning in this case the only client. This is useful as it gives this client
			// certain privileges in this class which subsequent clients do not have e.g. to set
			// the visit ID).
			firstClientTakesBaton = true;
		} else {
			firstClientTakesBaton = LocalProperties.get("gda.accesscontrol.firstClientTakesBaton", "true").equals("true");
		}

		useRBAC = LocalProperties.isAccessControlEnabled();

		new LeaseRefresher().start();

		logger.info("Started baton manager. firstClientTakesBaton={}, useBaton={}, useRBAC={}", firstClientTakesBaton,
				useBaton, useRBAC);
	}

	@Override
	public int getAuthorisationLevelOf(String uniqueID) {

		if (!useRBAC) {
			return 0;
		}

		int authLevel = getClientInfo(uniqueID).getAuthorisationLevel();

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
		return getClientInfo(uniqueID).getAuthorisationLevel();
	}

	@Override
	public void addFacade(String uniqueID, ClientDetails info) {

		// must have a meaningful identifier
		if (uniqueID != null && uniqueID != "") {

			facadeNames.put(uniqueID, info.copy());

			// Decide if client should get the baton or not
			if (firstClientTakesBaton
					&& !isBatonHeld()
					&& !info.isServer()
					&& !info.isAutomatedUser()
					) {
				changeBatonHolder(uniqueID);
			}

			// skip this part if an object server
			if (!info.isServer()) {
				renewLease(uniqueID);
			}
			logger.info("Added new facade id='{}' client details='{}'", uniqueID, info);
			logger.info("Now have {} facades", facadeNames.size());
		} else {
			logger.warn("Tried to add an facade with an invalid identifier: {}", uniqueID);
		}
	}

	@Override
	public void switchUser(String uniqueFacadeName, String username, int accessLevel, String visitID) {
		// overwrite the entry in facadeNames
		ClientInfo info = getClientInfo(uniqueFacadeName);

		//only change if information is supplied
		if (username != null && !username.equals("")){
			info.setUserID(username);
			info.setAuthorisationLevel(accessLevel);
		}
		if (visitID != null && !visitID.equals("")){
			info.setVisitID(visitID);
		}

		facadeNames.put(uniqueFacadeName, info);

		// if the baton holder then ensure all information is refreshed
		if (amIBatonHolder(uniqueFacadeName,false)){
			changeBatonHolder(uniqueFacadeName);
		}
		logger.info("Switched user. uniqueFacadeName='{}' username='{}' accessLevel='{}' visitID='{}'",
				uniqueFacadeName, username, accessLevel, visitID);
	}

	@Override
	public synchronized void removeFacade(String uniqueID) {
		returnBaton(uniqueID);
		facadeNames.remove(uniqueID);
		leaseHolders.remove(uniqueID);
		logger.info("Removed facade '{}' now have {} facades", uniqueID, facadeNames.size());
	}

	@Override
	public int getNewFacadeIndex() {
		return facadeIndex.getAndIncrement();
	}

	@Override
	public boolean amIBatonHolder(String myJSFIdentifier) {
		return useBaton ? amIBatonHolder(myJSFIdentifier, true) : true;
	}

	@Override
	public void assignBaton(String myJSFIdentifier, int indexOfReciever, int indexOfPasser) {
		ClientDetails batonHolderDetails = InterfaceProvider.getBatonStateProvider().getBatonHolder();
		// Assign baton if there is no baton holder, or if baton passer still holds baton
		if (batonHolderDetails == null || indexOfPasser == batonHolderDetails.getIndex()) {
			String idOfNewHolder = idFromIndex(indexOfReciever);
			if (idOfNewHolder != null) {
				changeBatonHolder(idOfNewHolder);
				renewLease(idOfNewHolder);
			}
			renewLease(myJSFIdentifier);
		} else {
			int batonHolderIndex = InterfaceProvider.getBatonStateProvider().getBatonHolder().getIndex();
			logger.warn("Client {} doesn't currently hold the baton, so can't pass the baton to client {}. " +
					"Client {} is the baton holder", indexOfPasser, indexOfReciever, batonHolderIndex);
		}
	}

	private boolean canTheseShareTheBaton(String myJSFIdentifier, String theirJSFIdentifier) {

		if (!LocalProperties.canShareBaton()){
			return false;
		}

		if (myJSFIdentifier.equals(theirJSFIdentifier))
			return true;
		if (myJSFIdentifier.isEmpty() || theirJSFIdentifier.isEmpty())
			return false;
		if (getClientInfo(theirJSFIdentifier) == null)
			return false;

		String myFedID = getClientInfo(myJSFIdentifier).getUserID();
		String theirFedID = getClientInfo(theirJSFIdentifier).getUserID();
		String myVisitID = getClientInfo(myJSFIdentifier).getVisitID();
		String theirVisitID = getClientInfo(theirJSFIdentifier).getVisitID();

		if (myFedID == null && theirFedID == null)
			return true;
		if (myFedID == null || theirFedID == null)
			return false;

		return myFedID.equals(theirFedID) && myVisitID.equals(theirVisitID);
	}

	@Override
	public ClientDetails getClientInformation(String myJSFIdentifier) {
		boolean hasBaton = amIBatonHolder(myJSFIdentifier, false);
		ClientInfo info = getClientInfo(myJSFIdentifier);
		return new ClientDetails(info, hasBaton);
	}

	/*
	 * Returns static information and does not say if this client holds the baton.
	 */
	private ClientInfo getClientInfo(String myJSFIdentifier) {
		return facadeNames.get(myJSFIdentifier);
	}

	@Override
	public ClientDetails[] getOtherClientInformation(String myJSFIdentifier) {
		renewLease(myJSFIdentifier);
		return facadeNames.entrySet().stream()
				.filter(e -> !e.getKey().equals(myJSFIdentifier)) // Ignore me
				.filter(e -> !e.getValue().isServer()) // Ignore server JSF
				.filter(e -> leaseHolders.containsKey(e.getKey())) // Ignore dead clients
				.map(e -> new ClientDetails(e.getValue(), amIBatonHolder(e.getKey())))
				.toArray(ClientDetails[]::new);
	}

	@Override
	public synchronized boolean requestBaton(String uniqueIdentifier) {
		logger.info("Baton requested by: {}", uniqueIdentifier);

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
			ClientInfo currentHolder = getClientInfo(this.batonHolder);
			ClientInfo other = getClientInfo(uniqueIdentifier);

			// if requester has higher auth than current baton holder then take
			if (other.getAuthorisationLevel() > currentHolder.getAuthorisationLevel()
					|| (other.getUserID().equals(currentHolder.getUserID()) && other.getAuthorisationLevel() == currentHolder.getAuthorisationLevel())) {
				changeBatonHolder(uniqueIdentifier);
				return true;
			}
			// else sent out request message and see if the request is granted
			InterfaceProvider.getJythonServerNotifer().notifyServer(this,
					new BatonRequested(new ClientDetails(other, false)));
		}
		// Automated client will reject any request to pass the baton to human user until it has
		// finished the current task. The human user requesting the baton gets a pop-up dialog
		// saying they will get the baton when the automated client finishes its task. Don't log
		// baton request denied if baton holder is automated client as this will confuse the user
		if (getClientInfo(this.batonHolder).isAutomatedUser()) {
			logger.info("Baton request from '{}' received by automated client", uniqueIdentifier);
		} else {
			logger.warn("Baton request from '{}' was denied", uniqueIdentifier);
		}

		// if get here then cannot take baton
		return false;
	}

	private void changeBatonHolder(String uniqueIdentifier) {
		this.batonHolder = uniqueIdentifier;

		if (!uniqueIdentifier.equals("")) {
			changeUserIDDefinedMetadata(uniqueIdentifier);
		}
		notifyServerOfBatonChange();
	}

	private void changeUserIDDefinedMetadata(String uniqueIdentifier) {
		// refresh the pieces of metadata which holds the current visit id and username
		try {
			Metadata metadata = GDAMetadataProvider.getInstance();
			logger.trace("changeUserIDDefinedMetadata({}) called with metadata={}", uniqueIdentifier, metadata);
			if (metadata != null) {

				String currentUser = getClientInfo(uniqueIdentifier).getUserID();
				String visitID = getClientInfo(uniqueIdentifier).getVisitID();

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
					logger.info("Ignoring client request to change visit to: '{}'", visitID);
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
			logger.trace("changeUserIDDefinedMetadata({}) completed with metadata={}", uniqueIdentifier, metadata);
		} catch (Exception e) {
			logger.warn("Exception while changing the username stored in metadata. This could cause problems with data collection", e);
		}
	}

	private boolean metadataContainsKey(Metadata metadata, String key){
		List<IMetadataEntry> entries  = metadata.getMetadataEntries();

		for(IMetadataEntry entry : entries){
			if (entry.getName().equals(key)){
				return true;
			}
		}
		return false;
	}

	@Override
	public void returnBaton(String uniqueIdentifier) {
		if (this.batonHolder.equals(uniqueIdentifier) && isJSFRegistered(uniqueIdentifier)) {
			changeBatonHolder("");
			renewLease(uniqueIdentifier);
			notifyServerOfBatonChange();
			logger.info("Baton returned by: {}", uniqueIdentifier);
		} else {
			logger.warn("Baton was not returned by: {}", uniqueIdentifier);
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
			return this.batonHolder.equals(myJSFIdentifier) || canTheseShareTheBaton(myJSFIdentifier,this.batonHolder);
		}
		return true;
	}

	private String idFromIndex(int index) {
		// loop through facades and find matching index
		for (String uniqueID : facadeNames.keySet()) {
			ClientInfo details = getClientInfo(uniqueID);
			if (details.getIndex() == index) {
				return uniqueID;
			}
		}
		return null;
	}

	private void notifyServerOfBatonChange() {
		// during object server startup, this may come back null
		final IJythonServerNotifer jythonServerNotifer = InterfaceProvider.getJythonServerNotifer();
		if (logger.isDebugEnabled()) {
			logger.debug("notifyServerOfBatonChange() called when jythonServerNotifer={}, called from {} (abridged)",
					jythonServerNotifer,
					Arrays.stream(Thread.currentThread().getStackTrace()).skip(2).limit(10).collect(Collectors.toList()));
		}
		if (jythonServerNotifer != null) {
			jythonServerNotifer.notifyServer(this, new BatonChanged());
		}
	}

	private synchronized void renewLease(String myJSFIdentifier) {
		// update the start time of this lease, but only for clients
		if (facadeNames.containsKey(myJSFIdentifier) && !getClientInfo(myJSFIdentifier).getUserID().equals("")) {
			leaseHolders.put(myJSFIdentifier, new GregorianCalendar().getTimeInMillis());
		}
		logger.trace("Lease renewed by: {}", myJSFIdentifier);
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
	private class LeaseRefresher extends Thread {

		public LeaseRefresher(){
			super("BatonManagerLeaseRefresher");
			setDaemon(true);
		}

		@Override
		public void run() {

			while (useBaton) {
				try {
					// clean up the list
					removeTimeoutLeases();
					Thread.sleep(LEASE_TIMEOUT/2-2000);
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
		private void removeTimeoutLeases() {

			synchronized (BatonManager.this) {

				// refresh all other leases
				Long now = new GregorianCalendar().getTimeInMillis();

				String[] clientIDs = leaseHolders.keySet().toArray(new String[0]);

				for (int i = 0; i < clientIDs.length; i++) {
					Long leaseStart = leaseHolders.get(clientIDs[i]);
					if (now - leaseStart > LEASE_TIMEOUT) {
						leaseHolders.remove(clientIDs[i]);
						if (amIBatonHolder(clientIDs[i], false)) {
							logger.warn("Baton holder timeout, so baton released after {} secs",
									(now - leaseStart) / 1000);
							changeBatonHolder("");
						}
					}
				}
			}
		}

		private void notifyServerOfBatonLeaseRenewRequest() {
			// during object server startup, this may come back null
			if (InterfaceProvider.getJythonServerNotifer() != null) {
				InterfaceProvider.getJythonServerNotifer().notifyServer(this, new BatonLeaseRenewRequest());
			}
		}
	}

	@Override
	public List<ClientDetailsAndLeaseState> getAllClients() {
		final List<ClientDetailsAndLeaseState> clients = new ArrayList<>();
		for (Map.Entry<String, ClientInfo> entry : facadeNames.entrySet()) {
			final String uniqueId = entry.getKey();
			final ClientInfo info = entry.getValue();
			if (!info.isServer()) {
				final boolean hasBaton = amIBatonHolder(uniqueId, false);
				final ClientDetails details = new ClientDetails(info, hasBaton);
				final boolean hasLease = leaseHolders.containsKey(uniqueId);
				final ClientDetailsAndLeaseState detailsAndLease = new ClientDetailsAndLeaseState(details, hasLease);
				clients.add(detailsAndLease);
			}
		}
		return clients;
	}
}
