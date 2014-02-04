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

package gda.util;

import gda.configuration.properties.LocalProperties;
import gda.data.metadata.GDAMetadataProvider;
import gda.device.DeviceException;

import java.io.File;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to send ELog entries consisting of text and images to an ELog server.
 */
public class ElogEntry {
	static final String defaultURL = "http://rdb.pri.diamond.ac.uk/devl/php/elog/cs_logentryext_bl.php";
	/**
	 * logger that can be used for logging messages to go into eLog - needs to be selected in the logger config
	 */
	public static final Logger elogger = LoggerFactory.getLogger(ElogEntry.class);
	
	/**
	 * Creates an ELog entry. Default ELog server is "http://rdb.pri.diamond.ac.uk/devl/php/elog/cs_logentryext_bl.php"
	 * which is the development database. "http://rdb.pri.diamond.ac.uk/php/elog/cs_logentryext_bl.php" is the
	 * production database. The java.properties file contains the property "gda.elog.targeturl" which can be set to be
	 * either the development or production databases.
	 * 
	 * @param title
	 *            The ELog title
	 * @param content
	 *            The ELog content
	 * @param userID
	 *            The user ID e.g. epics or gda or abc12345
	 * @param visit
	 *            The visit number
	 * @param logID
	 *            The type of log book, The log book ID: Beam Lines: - BLB16, BLB23, BLI02, BLI03, BLI04, BLI06, BLI11,
	 *            BLI16, BLI18, BLI19, BLI22, BLI24, BLI15, DAG = Data Acquisition, EHC = Experimental Hall
	 *            Coordinators, OM = Optics and Meteorology, OPR = Operations, E
	 * @param groupID
	 *            The group sending the ELog, DA = Data Acquisition, EHC = Experimental Hall Coordinators, OM = Optics
	 *            and Meteorology, OPR = Operations CS = Control Systems, GroupID Can also be a beam line,
	 * @param fileLocations
	 *            The image file names with path to upload
	 * @throws ELogEntryException
	 */
	public static void post(String title, String content, String userID, String visit, String logID, String groupID,
			String[] fileLocations) throws ELogEntryException {
		String targetURL = defaultURL;
		try {
			String entryType = "41";// entry type is always a log (41)
			String titleForPost = visit == null ? title : "Visit: " + visit + " - " + title;
			
			MultipartEntityBuilder request = MultipartEntityBuilder.create()
					.addTextBody("txtTITLE", titleForPost)
					.addTextBody("txtCONTENT", content)
					.addTextBody("txtLOGBOOKID", logID)
					.addTextBody("txtGROUPID", groupID)
					.addTextBody("txtENTRYTYPEID", entryType)
					.addTextBody("txtUSERID", userID);
			
			if(fileLocations != null){
				for (int i = 1; i < fileLocations.length + 1; i++) {
					File targetFile = new File(fileLocations[i - 1]);
					request = request.addBinaryBody("userfile" + i,
							targetFile,
							ContentType.create("image/png"),
							targetFile.getName());
				}
			}
			
			HttpEntity entity = request.build();
			targetURL  = LocalProperties.get("gda.elog.targeturl", defaultURL);
			HttpPost httpPost = new HttpPost(targetURL);
			httpPost.setEntity(entity);
			CloseableHttpClient httpClient = HttpClients.createDefault();
			CloseableHttpResponse response = httpClient.execute(httpPost);
			
			try {
				String responseString = EntityUtils.toString(response.getEntity());
				System.out.println(responseString);
				if (!responseString.contains("New Log Entry ID")) {
					throw new ELogEntryException("Upload failed, status=" + response.getStatusLine().getStatusCode()
						+ " response="+responseString
						+ " targetURL = " + targetURL
						+ " titleForPost = " + titleForPost
						+ " logID = " + logID
						+ " groupID = " + groupID
						+ " entryType = " + entryType
						+ " userID = " + userID);
				}
			} finally {
				response.close();
				httpClient.close();
			}
		} catch (ELogEntryException e) {
			throw e;
		} catch (Exception e) {
			throw new ELogEntryException("Error in ELogger.  Database:" + targetURL, e);
		}
	}
	
	/**
	 * Async version of post @see ElogEntry.post
	 * 
	 * @param title
	 * @param content
	 * @param userID
	 * @param visit
	 * @param logID
	 * @param groupID
	 * @param fileLocations
	 */
	public static void postAsyn(final String title, final String content, final String userID, final String visit,
			final String logID, final String groupID, final String[] fileLocations) {

		Thread t = uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
			@Override
			public void run() {
				try {
					ElogEntry.post(title, content, userID, visit, logID, groupID, fileLocations);
				} catch (Exception e) {
					Logger logger = LoggerFactory.getLogger(ElogEntry.class);
					logger.error(e.getMessage(), e);
				}
			}
		}, "ElogEntry: "+title);

		t.start();
	}
	
	/**
	 * Create a logger event of the class gda.util.ElogEntry which can be used
	 * as a filter in logback configuration
	 * The resultant message is of the form visit + "%%" + title + "%%" + content
	 * which is understood by the associated ELogAppender class.
	 * @param title
	 * @param content
	 */ 
	public static void postViaLogger(String title, String content) {
		String visit;
		try {
			visit = GDAMetadataProvider.getInstance().getMetadataValue("visit");
		} catch (DeviceException e) {
			visit = "unknown";
		}
		elogger.info(visit + "%%" + title + "%%" + content);
	}
}