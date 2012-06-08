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

import java.util.Date;

import gda.data.metadata.VisitEntry;

/**
 * Interface for access to a user office database (ICAT) which maps experiment visits to users. It may also contain
 * metadata about experiments such as experimental title or sample safety information.
 * <p>
 * The accessName strings in the getInformation methods are implementation specific as the variety of information is
 * implementation specific. These methods are expected to be accessed by MetadataEntries used within the Metadata system.
 */
public interface Icat {

	/**
	 * Name of the java property which defines the class of the ICAT. If this is set to null then it will be assumed that no
	 * ICAT is in use.
	 */
	public static final String ICAT_TYPE_PROP = "gda.data.metadata.icat.type";
	
	/**
	 * Name of the java property which defines the Icat database url (file or database)
	 */
	public static final String URL_PROP = "gda.data.metadata.icat.url";

	/**
	 * Name of the java property which defines the shift tolerance (in minutes) to use when deciding if an experiment
	 * is current
	 */
	public static final String SHIFT_TOL_PROP = "gda.data.metadata.icat.shift_tolerance";

	/**
	 * @return true if the Icat database can be connected to.
	 */
	public boolean icatInUse();

	/**
	 * Returns all the visit IDs for this user on this beamline at this point in time (tolerance in the
	 * IcatConnectionDetails).
	 * 
	 * @param username
	 * @return array of valid visit IDs
	 * @throws Exception
	 */
	public VisitEntry[] getMyValidVisits(String username) throws Exception;

	/**
	 * Sets the visit under which this user shall collect data.
	 * 
	 * @param visitID
	 */
	public void setMyVisit(String visitID);

	/**
	 * Queries the database filtering using the stored username, beamline and visit.
	 * 
	 * @param accessName
	 * @return String or null if no information found
	 * @throws Exception
	 */
	public String getMyInformation(String accessName) throws Exception;

	/**
	 * Queries the database filtering using the given username, beamline and visit.
	 * 
	 * @param accessName
	 * @param username
	 * @param visitID
	 * @return String or null if no information found
	 * @throws Exception
	 */
	public String getMyInformation(String accessName, String username, String visitID) throws Exception;

	/**
	 * Queries the database filtering using the current username and visit held in metadata.
	 * 
	 * @param accessName
	 * @return String or null if no information found
	 * @throws Exception
	 */
	public String getCurrentInformation(String accessName) throws Exception;

	/**
	 * Inject the current time for testing. This will be used when filtering for valid visits on the given beamline.
	 * <p>
	 * Set to null for the current date to tbe used.
	 * 
	 * @param date
	 */
	public void setOperatingDate(Date date);

}
