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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;
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

import gda.configuration.properties.LocalProperties;
import gda.data.metadata.GDAMetadataProvider;
import gda.device.DeviceException;
import uk.ac.gda.util.ThreadManager;

public class ElogEntry {

	private static final String IMG_UPLOAD_URL = "http://rdb.pri.diamond.ac.uk/devl/php/elog/cs_logonlyimageupload_ext_bl.php";
	private static final String POST_UPLOAD_URL = "http://rdb.pri.diamond.ac.uk/devl/php/elog/cs_logentryext_bl.php";

	private static final Logger elogger = LoggerFactory.getLogger(ElogEntry.class);

	private String targetPostURL;
	private String imagePostURL;

	//the html output to be posted - Stops post being buried in unknown html
	private String startLine = "\n\n<!-- ==== Start of Elog Entry Content ==== -->\n";
	private ArrayList<PostPart> parts;
	private String endLine = "\n<!-- ==== End of Elog Entry Content ==== -->\n\n";

	//parameters to be given as headers
	private String title;
	private String userID;
	private String visit;
	private String logID;
	private String groupID;

	public ElogEntry(String title, String userID, String visit, String logID,
			String groupID) {
		this.parts = new ArrayList<ElogEntry.PostPart>();
		this.title = title;
		this.userID = userID;
		this.visit = visit;
		this.logID = logID;
		this.groupID = groupID;
		this.targetPostURL  = LocalProperties.get("gda.elog.targeturl", POST_UPLOAD_URL);
		this.imagePostURL = LocalProperties.get("gda.elog.imageurl", IMG_UPLOAD_URL);

	}

	/**
	 * Post eLog Entry
	 */
	public void post() throws ELogEntryException {

		String entryType = "41";// entry type is always a log (41)

		String titleForPost = visit == null ? title : "Visit: " + visit + " - " + title;

		String content = buildContent();

		MultipartEntityBuilder request = MultipartEntityBuilder.create()
			.addTextBody("txtTITLE", titleForPost)
			.addTextBody("txtCONTENT", content)
			.addTextBody("txtLOGBOOKID", logID)
			.addTextBody("txtGROUPID", groupID)
			.addTextBody("txtENTRYTYPEID", entryType)
			.addTextBody("txtUSERID", userID);

		HttpEntity entity = request.build();
		HttpPost httpPost = new HttpPost(targetPostURL);
		httpPost.setEntity(entity);

		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;

		try {
			httpClient = HttpClients.createDefault();
			response = httpClient.execute(httpPost);
			String responseString = EntityUtils.toString(response.getEntity());
			System.out.println(responseString);
			if (!responseString.contains("New Log Entry ID")) {
				throw new ELogEntryException("Upload failed, status=" + response.getStatusLine().getStatusCode()
					+ " response="+responseString
					+ " targetURL = " + targetPostURL
					+ " titleForPost = " + titleForPost
					+ " logID = " + logID
					+ " groupID = " + groupID
					+ " entryType = " + entryType
					+ " userID = " + userID);
			}
		} catch (ELogEntryException e) {
			throw e;
		} catch (Exception e) {
			throw new ELogEntryException("Error in ELogger.  Database:" + targetPostURL, e);
		} finally {
			try {
				if (httpClient != null) httpClient.close();
				if (response != null) response.close();
			} catch (IOException e) {
				elogger.error("Could not close connections", e);
			}
		}

	}

	public void postFile(String file) throws ELogEntryException {
//			if (new File(fileURI).exists()) {
//				System.out.println("fileExists");
//				return;
//			}
		PrintWriter writer;
		try {
			writer = new PrintWriter(file, "UTF-8");
			writer.println(buildContent());
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Async version of post @see ElogEntry.post
	 *
	 */
	public void postAsync() {
		Thread t = ThreadManager.getThread(new Runnable() {
			@Override
			public void run() {
				try {
					post();
				} catch (Exception e) {
					elogger.error("Error posting ElogEntry", e);
				}
			}
		}, "ElogEntry: "+title);

		t.start();
	}

	private String buildContent() throws ELogEntryException {
		StringBuffer content = new StringBuffer(startLine);
		for (PostPart part : parts) {
			content.append(part.getHTML());
		}
		content.append(endLine);
		return content.toString();
	}

	/**
	 * Add html string to eLog <br>
	 * html will be added as is with no characters escaped
	 *
	 * @param html html string to add
	 */
	public void addHtml(String html) {
		this.parts.add(new htmlPart(html));
	}

	/**
	 * Add multiple html strings to eLog <br>
	 * html will be added as is with no characters escaped
	 *
	 * @param html Array of html strings to add
	 */
	public void addHtml(String[] html) {
		for (String string : html) {
			addHtml(string);
		}
	}

	/**
	 * Add paragraph of text to the eLog Entry.<br>
	 * html characters will be escaped
	 *
	 * @param text String to add
	 */
	public void addText(String text) {
		this.parts.add(new textPart(text));
	}

	/**
	 * Add multiple lines of text to the eLog Entry.
	 * <br>Each will be formatted as a separate paragraph
	 *
	 * @param texts Array of strings to add
	 */
	public void addText(String[] texts) {
		for (String text : texts) {
			addText(text);
		}
	}

	/**
	 * Add single image to the eLog Entry
	 *
	 * @param fileURI The location of image to add
	 * @throws ELogEntryException if file does not exist
	 */
	public void addImage(String fileURI) throws ELogEntryException {
		addImage(fileURI, null);
	}

	/**
	 * Add single image to the eLog Entry with caption
	 *
	 * @param fileURI The list of file locations of images to add
	 * @param caption A caption to be displayed below the image
	 * @throws ELogEntryException if file does not exist
	 */
	public void addImage(String fileURI, String caption) throws ELogEntryException {
		File img = new File(fileURI);
		if (img.exists()) {
			this.parts.add(new imagePart(fileURI, caption));
		} else {
			throw new ELogEntryException("File \"" + fileURI + "\" does not exist");
		}
	}

	/**
	 * Add multiple images to the eLog Entry
	 *
	 * @param fileURIs The list of file locations of images to add
	 * @throws ELogEntryException if file does not exist
	 */
	public void addImage(String[] fileURIs) throws ELogEntryException {
		for (String file : fileURIs) {
			addImage(file, null);
		}
	}


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
		String targetURL = POST_UPLOAD_URL;
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
			targetURL  = LocalProperties.get("gda.elog.targeturl", POST_UPLOAD_URL);
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
					elogger.error("Error posting ElogEntry", e);
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

	public void setTitle(String title) {
		this.title = title;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public void setVisit(String visit) {
		this.visit = visit;
	}

	public void setLogID(String logID) {
		this.logID = logID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	private interface PostPart {
		String getHTML() throws ELogEntryException;
	}

	private class textPart implements PostPart {
		private String text;
		public textPart(String text) {
			this.text = text;
		}
		@Override
		public String getHTML() {
			return "<p>" + StringEscapeUtils.escapeHtml(text).replace("\n", "<br>\n\t") + "</p>";
		}
	}

	private class htmlPart implements PostPart {
		private String html;
		public htmlPart(String html) {
			this.html = html;
		}
		@Override
		public String getHTML() throws ELogEntryException {
			return "\n<!-- html submitted as is - not rendered by ElogEntry -->\n" + html + "<br>\n";
		}

	}

	private class imagePart implements PostPart {
		private final String fileURI;
		private final String caption;
		private String imageURL = null;

		public imagePart(String fileURI, String caption) {
			this.fileURI = fileURI;
			this.caption = caption;
		}

		@Override
		public String getHTML() throws ELogEntryException {
			if (imageURL == null) {
				imageURL = uploadImage();
			}
			return imageHtml();
		}

		private String imageHtml() {
			if (imageURL != null) {
				String captionText = caption == null || caption.isEmpty()
						? ""
						: "\n<span style=\"max-width:600px; margin: 0 auto; text-align:inherited; word-wrap:break-word;\">" + StringEscapeUtils.escapeHtml(caption) + "</span>\n";
				String style = "display:inline-block;";
				String imageHtml = "\n" +
						"<img alt =\"" + imageURL + "\" src=\"" + imageURL + "\"/><br>" +
						captionText;
				return "\n<div style=\"" + style + "\">" + imageHtml + "</div><br><br>\n";
			}
			return null;
		}

//		private String uploadImage() {
//			return fileURI;
//		}

		//upload image to eLog and get attachment URL
		private String uploadImage() throws ELogEntryException {
			MultipartEntityBuilder request = MultipartEntityBuilder.create()
					.addTextBody("txtUSERID", userID)
					.addBinaryBody("userfile1", new File(fileURI));

			HttpEntity entity = request.build();
			HttpPost httpPost = new HttpPost(imagePostURL);
			httpPost.setEntity(entity);
			CloseableHttpClient httpClient = null;
			CloseableHttpResponse response = null;
			try {
				httpClient = HttpClients.createDefault();
				response = httpClient.execute(httpPost);
				String responseString = EntityUtils.toString(response.getEntity());
				int index = responseString.indexOf("FULL URL:");
				if (index > 0) {
					return responseString.substring(index + 9);
				}
				throw new ELogEntryException("Image upload failed: " + fileURI + " not found");
			} catch (ELogEntryException e) {
				throw e;
			} catch (Exception e) {
				throw new ELogEntryException("Image upload failed", e);
			}
			finally {
				try {
					if (httpClient != null) httpClient.close();
					if (response != null) response.close();
				} catch (IOException e) {
					elogger.error("Could not close connections", e);
				}
			}
		}

	}
}
