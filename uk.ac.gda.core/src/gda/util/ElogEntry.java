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

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.concurrent.Async;

/** A builder and publisher for creating automated ELog entries */
public class ElogEntry {

	private static final Logger logger = LoggerFactory.getLogger(ElogEntry.class);
	public static final String GDA_ELOG_IMAGEURL_PROPERTY = "gda.elog.imageurl";
	public static final String GDA_ELOG_TARGETURL_PROPERTY = "gda.elog.targeturl";
	public static final String ENCODING = "UTF-8";

	/** String included in response of successful uploads */
	private static final String POST_SUCCESS_MARKER = "New Log Entry ID:";

	/** Log entries are added via type {@value #LOG_ENTRY_TYPE} */
	private static final String LOG_ENTRY_TYPE = "41";

	/** Marker to indicate start of user entry in final uploaded html */
	private static final String START_LINE = "\n\n<!-- ==== Start of Elog Entry Content ==== -->\n";
	/** Marker to indicate end of user entry in final uploaded html */
	private static final String END_LINE = "\n<!-- ==== End of Elog Entry Content ==== -->\n\n";

	private String targetPostURL;
	private String imagePostURL;

	private Collection<PostPart> parts;

	//parameters to be given as headers
	private String title;
	private String userID;
	private String visit;
	private String logID;
	private String groupID;

	/** Flag to prevent post being sent multiple times */
	private AtomicBoolean posted = new AtomicBoolean();

	/**
	 * Creates an ELog entry. Default ELog server is the development database.<br>
	 * The property {@value #GDA_ELOG_TARGETURL_PROPERTY} can be used to set to database to use for posts.<br>
	 * The property {@value #GDA_ELOG_IMAGEURL_PROPERTY} can be used to set to database to use for image uploads.
	 *
	 * @param title The ELog title
	 * @param content The ELog content
	 * @param userID The user ID e.g. epics or gda or abc12345
	 * @param visit The visit number
	 * @param logID The log book ID:<br>
	 *            Beam Lines: - BLB16, BLB23, BLI02, BLI03, BLI04, BLI06, BLI11,
	 *            BLI16, BLI18, BLI19, BLI22, BLI24, BLI15<br>
	 *            DAG = Data Acquisition<br>
	 *            EHC = Experimental Hall Coordinators,<br>
	 *            OM = Optics and Meteorology,<br>
	 *            OPR = Operations
	 * @param groupID
	 *            The group sending the ELog<br>
	 *            DA = Data Acquisition, EHC = Experimental Hall Coordinators, OM = Optics
	 *            and Meteorology, OPR = Operations CS = Control Systems<br>
	 *            GroupID Can also be a beam line,
	 * @param fileLocations The image file names with path to upload
	 * @throws ElogException
	 */
	public static String post(String title, String content, String userID, String visit, String logID, String groupID,
			String[] fileLocations) throws ElogException {
		ElogEntry entry = new ElogEntry(title, userID, visit, logID, groupID);
		entry.addText(content);
		entry.addImages(fileLocations);
		return entry.post();
	}

	/**
	 * Post ElogEntry in a background thread
	 * @see #post(String, String, String, String, String, String, String[])
	 */
	public static Future<String> postAsyn(final String title, final String content, final String userID, final String visit,
			final String logID, final String groupID, final String[] fileLocations) {
		return Async.submit(() -> post(title, content, userID, visit, logID, groupID, fileLocations),
				"ElogEntry: %s", title);
	}

	public ElogEntry(String title, String userID, String visit, String logID,
			String groupID) {
		this.parts = new ArrayList<>();
		this.title = title;
		this.userID = userID;
		this.visit = visit;
		this.logID = logID;
		this.groupID = groupID;
		this.targetPostURL  = LocalProperties.get(GDA_ELOG_TARGETURL_PROPERTY);
		this.imagePostURL = LocalProperties.get(GDA_ELOG_IMAGEURL_PROPERTY);
	}

	/**
	 * Post eLog Entry
	 * @return The id of the post (if successful)
	 */
	public String post() throws ElogException {
		if (targetPostURL == null) {
			throw new IllegalStateException("No elog url found. Set property " + GDA_ELOG_TARGETURL_PROPERTY);
		}
		if (posted.getAndSet(true)) {
			throw new ElogException("Post has already been uploaded");
		}

		String titleForPost = visit == null ? title : "Visit: " + visit + " - " + title;

		String content = buildContent(false);

		MultipartEntityBuilder request = MultipartEntityBuilder.create()
			.addTextBody("txtTITLE", titleForPost)
			.addTextBody("txtCONTENT", content)
			.addTextBody("txtLOGBOOKID", logID)
			.addTextBody("txtGROUPID", groupID)
			.addTextBody("txtENTRYTYPEID", LOG_ENTRY_TYPE)
			.addTextBody("txtUSERID", userID);

		HttpEntity entity = request.build();

		try {
			String response = Request.Post(targetPostURL)
					.body(entity)
					.execute()
					.returnContent()
					.asString();
			if (!response.startsWith(POST_SUCCESS_MARKER)) {
				return response.substring(POST_SUCCESS_MARKER.length());
			} else {
				throw new ElogException("Unexpected response: " + response);
			}
		} catch (IOException e1) {
			throw new ElogException("Could not post to elog", e1);
		}
	}

	/** Write post out to file as html - mainly useful for testing */
	public void postToFile(String file) {
		try (PrintWriter writer = new PrintWriter(file, ENCODING)) {
			writer.println(buildContent(true));
		} catch (FileNotFoundException e) {
			logger.error("Could not find file ({}) to write to", file, e);
		} catch (UnsupportedEncodingException e) {
			logger.error("Could not find {} charset", ENCODING);
		}
	}

	/**
	 * Async version of post @see ElogEntry.post
	 */
	public Future<String> postAsync() {
		// needs cast to distinguish from submit(Runnable, ...)
		return Async.submit((Callable<String>)this::post, "ElogEntry: %s", title);
	}

	private String buildContent(boolean offline) {
		return parts.stream().map(p -> {
			try {
				return offline ? p.getOfflineHTML() : p.getHTML();
			} catch (ElogException e) {
				logger.error("Could not extract html from {}", p);
				return "";
			}}).collect(Collectors.joining("", START_LINE, END_LINE));
	}

	/**
	 * Add html string to eLog <br>
	 * html will be added as is with no characters escaped
	 *
	 * @param html html string to add
	 */
	public ElogEntry addHtml(String html) {
		this.parts.add(new HtmlPart(html));
		return this;
	}

	/**
	 * Add multiple html strings to eLog <br>
	 * html will be added as is with no characters escaped
	 *
	 * @param html Array of html strings to add
	 */
	public ElogEntry addHtml(String[] html) {
		Arrays.stream(html).forEach(this::addHtml);
		return this;
	}

	/**
	 * Add paragraph of text to the eLog Entry.<br>
	 * html characters will be escaped
	 *
	 * @param text String to add
	 */
	public ElogEntry addText(String text) {
		this.parts.add(new TextPart(text));
		return this;
	}

	/**
	 * Add multiple lines of text to the eLog Entry.
	 * <br>Each will be formatted as a separate paragraph
	 *
	 * @param texts Array of strings to add
	 */
	public ElogEntry addText(String[] texts) {
		Arrays.stream(texts).forEach(this::addText);
		return this;
	}

	/**
	 * Add single image to the eLog Entry
	 *
	 * @param fileURI The location of image to add
	 * @throws ElogException if file does not exist
	 */
	public ElogEntry addImage(String fileURI) throws ElogException {
		addImage(fileURI, null);
		return this;
	}

	/**
	 * Add single image to the eLog Entry with caption
	 *
	 * @param fileURI The list of file locations of images to add
	 * @param caption A caption to be displayed below the image
	 * @throws ElogException if file does not exist
	 */
	public ElogEntry addImage(String fileURI, String caption) throws ElogException {
		File img = new File(fileURI);
		if (img.exists()) {
			this.parts.add(new ImagePart(fileURI, caption));
		} else {
			throw new ElogException("File \"" + fileURI + "\" does not exist");
		}
		return this;
	}

	/**
	 * Add multiple images to the eLog Entry
	 *
	 * @param fileURIs The list of file locations of images to add
	 * @throws ElogException if file does not exist
	 */
	public ElogEntry addImages(String[] fileURIs) throws ElogException {
		if (fileURIs != null) {
			for (String file : fileURIs) {
				addImage(file, null);
			}
		}
		return this;
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

	/**
	 * A representation of a section of an entry
	 */
	@FunctionalInterface
	private interface PostPart {
		/** Convert this part into its HTML representation */
		String getHTML() throws ElogException;
		default String getOfflineHTML() throws ElogException {return getHTML();}
	}

	/**
	 * Part representing plain text. Text is escaped so that it appears as entered in the final
	 * HTML form.
	 *
	 * @see StringEscapeUtils#escapeHtml(String)
	 */
	private class TextPart implements PostPart {
		private static final String TEMPLATE = "<p>%s</p>\n";
		private static final String NEWLINE = "\n";
		private static final String HTML_NEWLINE = "<br>\n\t";

		private String text;

		public TextPart(String text) {
			this.text = text;
		}
		@Override
		public String getHTML() {
			return String.format(TEMPLATE, escapeHtml(text).replace(NEWLINE, HTML_NEWLINE));
		}
	}

	/**
	 * Part representing raw HTML. Text is uploaded as submitted with nothing being escaped
	 */
	private class HtmlPart implements PostPart {
		private static final String TEMPLATE = "\n<!-- html submitted as is - not rendered by ElogEntry -->\n%s<br>\n";

		private String html;

		public HtmlPart(String html) {
			this.html = html;
		}

		@Override
		public String getHTML() {
			return String.format(TEMPLATE, html);
		}
	}

	/**
	 * Part to represent an image. The given image is lazily uploaded when the HTML is generated.
	 */
	private class ImagePart implements PostPart {
		/** Marker string included in response to indicate start of uploaded URL */
		private static final String IMAGE_UPLOAD_MARKER = "FULL URL:";
		/** Template for the caption part of the HTML. Should be formatted with the HTML-escaped caption */
		private static final String IMAGE_CAPTION_TEMPLATE =
				"\n<span style=\"" +
						"max-width:600px; " +
						"margin: 0 auto; " +
						"text-align:inherited; " +
						"word-wrap:break-word;" +
				"\">%s</span>\n";
		/** Template for the whole HTML. Should be formatted with the image URL and the caption part */
		private static final String TEMPLATE =
				"\n" +
				"<div style=\"display:inline-block;\">\n"+
					"<img alt=\"%1$s\" src=\"%1$s\"/><br>" + // HTML for image
					"%2$s" + // HTML for caption
				"</div>" +
				"<br><br>\n";
		private final String fileURI;
		private final String caption;
		private String imageURL = null;

		public ImagePart(String fileURI, String caption) {
			this.fileURI = fileURI;
			this.caption = caption;
		}

		@Override
		public String getHTML() throws ElogException {
			if (imageURL == null) {
				imageURL = extractAttachmentUrl(uploadImage());
			}
			return imageHtml();
		}

		@Override
		public String getOfflineHTML() throws ElogException {
			if (imageURL == null) {
				imageURL = fileURI;
			}
			return imageHtml();
		}

		/**
		 * Format image URI into an HTML <code>&lt;div&gt;</code> containing the image and caption
		 */
		private String imageHtml() {
			if (imageURL != null) {
				String captionText = caption == null || caption.isEmpty()
						? ""
						: String.format(IMAGE_CAPTION_TEMPLATE, escapeHtml(caption));
				return String.format(TEMPLATE, imageURL, captionText);
			}
			return null;
		}

		/** Upload image to eLog and return response */
		private String uploadImage() throws ElogException {
			if (imagePostURL == null) {
				throw new ElogException("This server is not configured to upload images to elog. See property "
						+ GDA_ELOG_IMAGEURL_PROPERTY);
			}
			MultipartEntityBuilder request = MultipartEntityBuilder.create()
					.addTextBody("txtUSERID", userID)
					.addBinaryBody("userfile1", new File(fileURI));

			HttpEntity entity = request.build();

			try {
				return Request.Post(imagePostURL).body(entity).execute().returnContent().asString();
			} catch (IOException e) {
				throw new ElogException("Image upload failed", e);
			}
		}

		/**
		 * Extract attachment URL from upload response
		 * @param response Response from {@link #uploadImage()}
		 * @throws ElogException if response does not include URL
		 */
		private String extractAttachmentUrl(String response) throws ElogException {
			int index = response.indexOf(IMAGE_UPLOAD_MARKER);
			if (index < 0) {
				throw new ElogException("Image upload failed: " + fileURI + " not found");
			}
			return response.substring(index + IMAGE_UPLOAD_MARKER.length());
		}
	}

	public static class ElogException extends Exception {
		public ElogException(String msg) {
			super(msg);
		}

		public ElogException(String msg, Throwable e) {
			super(msg, e);
		}
	}
}
