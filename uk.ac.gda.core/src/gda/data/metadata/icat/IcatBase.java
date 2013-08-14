/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.data.metadata.icat;

import gda.configuration.properties.LocalProperties;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;
import gda.data.metadata.VisitEntry;
import gda.device.DeviceException;
import gda.jython.JythonServerFacade;
import gda.jython.authoriser.AuthoriserProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Provides base functionality for classes implementing the Icat interface.
 */
public abstract class IcatBase implements Icat {
	private static final Logger logger = LoggerFactory.getLogger(IcatBase.class);

	protected String visitID = null;
	protected Date operatingDate = null;
	protected Metadata metadata;
	protected String instrumentName = null;
	protected String username;

	/**
	 * Has an ICAT been defined for use? Based solely on the java property @see {@link Icat#ICAT_TYPE_PROP}
	 */
	@Override
	public boolean icatInUse() {
		if (LocalProperties.get(ICAT_TYPE_PROP) != null) {
			return true;
		}
		return false;
	}

	@Override
	public void setMyVisit(String choiceOfVisit) {
		if (!choiceOfVisit.equals(visitID)) {
			if (!choiceOfVisit.equals("")) {
				logger.info("Visit ID used to filter information from Icat database now: " + choiceOfVisit);
				visitID = choiceOfVisit;
				if (JythonServerFacade.getInstance() != null) {
					JythonServerFacade.getInstance().changeVisitID(visitID);
				}
			}
		}
	}

	@Override
	public VisitEntry[] getMyValidVisits(String username) throws Exception {
		String results = "";
		results = getValue(null, username, getVisitIDAccessName());
		this.username = username;

		// Use a set here to prevent duplicate visits appearing
		Set<String> visits = new LinkedHashSet<String>();

		if (results != null && !results.isEmpty()) {
			final String[] bits = results.split(",");
			for (String bit : bits) {
				visits.add(bit.trim());
			}
		}

		// append to the list extra options if local staff
		if (AuthoriserProvider.getAuthoriser().isLocalStaff(username)) {

			// allow beamline staff to use the current visit ID
			String currentVisitID = getCurrentVisitId();
			addVisitIfNotNullOrEmpty(currentVisitID, visits);

			// allow beamline staff to choose the default ID listed in the metadata
			Metadata metadata = GDAMetadataProvider.getInstance();
			String defVisit = metadata.getMetadataValue("defVisit");
			addVisitIfNotNullOrEmpty(defVisit, visits);

			// allow beamline staff to use the default ID listed in Java properties
			String defProperty = LocalProperties.get("gda.defVisit").trim();
			addVisitIfNotNullOrEmpty(defProperty, visits);
		}

		// if nothing has been found so far and the local property is set, use the def visit
		if (visits.isEmpty() && LocalProperties.get("gda.icat.usersCanUseDefVisit", "false").equals("true")) {
			// allow beamline staff to use the default ID listed in Java properties
			String defProperty = LocalProperties.get("gda.defVisit");
			addVisitIfNotNullOrEmpty(defProperty, visits);
		}

		List<VisitEntry> visitEntries = new ArrayList<VisitEntry>();

		for (String visit : visits) {
			String titleValue = "";
			try {
				titleValue = getMyInformation(getExperimentTitleAccessName(), username, visit);
			} catch (Exception e) {
				// ignore and use empty string. If its a connection error then this would already have been logged.
			}

			if (titleValue != null) {
				titleValue = titleValue.trim();
			}

			VisitEntry entry = new VisitEntry(visit.trim(), titleValue);
			visitEntries.add(entry);
		}

		return visitEntries.toArray(new VisitEntry[] {});
	}
	
	private static void addVisitIfNotNullOrEmpty(String visit, Set<String> visits) {
		if (StringUtils.hasText(visit)) {
			visits.add(visit.trim());
		}
	}

	@Override
	public String getCurrentInformation(String accessName) throws Exception {
		String fedId = getCurrentFederalId();
		if (fedId == null || fedId.equals("")) {
			return null;
		}

		return getValue(getCurrentVisitId(), fedId, accessName);
	}

	@Override
	public String getMyInformation(String accessName) throws Exception {
		return getValue(visitID, username, accessName);
	}

	@Override
	public String getMyInformation(String accessName, String username, String visitID) throws Exception {
		return getValue(visitID, username, accessName);
	}

	@Override
	public void setOperatingDate(Date date) {
		operatingDate = date;
	}

	/**
	 * Inject the metadata object for testing. If not defined this gets the Metadata object from GDAMetadataProvider.
	 * 
	 * @param metadata
	 */
	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	/**
	 * Returns the access name which is used to find the visit ID
	 */
	protected abstract String getVisitIDAccessName();

	/**
	 * Returns the access name which is used to find the experiment title
	 */
	protected abstract String getExperimentTitleAccessName();

	/**
	 * Returns the SQL result for the given accessName string filtered by beamline, username and (optionally) visitID.
	 * 
	 * @param visitIDFilter
	 * @param userNameFilter
	 * @param accessName
	 * @return String
	 * @throws Exception
	 */
	protected abstract String getValue(String visitIDFilter, String userNameFilter, String accessName) throws Exception;

	protected Metadata getMetadata() {
		if (metadata == null) {
			metadata = GDAMetadataProvider.getInstance();
		}
		return metadata;
	}

	/**
	 * Inject the beamline (instrument) name for testing. If not defined this looks for the name in first metadata and
	 * then in java properties.
	 * 
	 * @param instrumentName
	 */
	public void setInstrumentName(String instrumentName) {
		this.instrumentName = instrumentName;
	}

	protected String getInstrumentName() {

		if (instrumentName != null) {
			return instrumentName;
		}

		instrumentName = LocalProperties.get("gda.instrument");
		if (!(instrumentName == null) && !instrumentName.isEmpty()) {
			return instrumentName;
		}

		return null;
	}

	/**
	 * @return the fedid of the user stored in metadata i.e. the one with the baton
	 * @throws DeviceException
	 */
	protected String getCurrentFederalId() throws DeviceException {
		if (getMetadata() != null) {
			return getMetadata().getMetadataValue("federalid");
		}
		return null;
	}

	/**
	 * @return the fedid of the user stored in metadata i.e. the one with the baton
	 * @throws DeviceException
	 */
	protected String getCurrentVisitId() throws DeviceException {
		if (getMetadata() != null) {
			return getMetadata().getMetadataValue("visit");
		}
		return null;
	}
}