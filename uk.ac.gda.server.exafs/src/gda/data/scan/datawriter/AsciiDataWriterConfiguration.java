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

package gda.data.scan.datawriter;

import gda.factory.Findable;

import java.util.ArrayList;

/**
 * Bean which holds the configuration for an AsciiDataWriter
 */
public class AsciiDataWriterConfiguration implements Findable {

	private ArrayList<AsciiWriterExtenderConfig> columns = new ArrayList<AsciiWriterExtenderConfig>();

	private ArrayList<AsciiMetadataConfig> header = new ArrayList<AsciiMetadataConfig>();

	private ArrayList<AsciiMetadataConfig> footer = new ArrayList<AsciiMetadataConfig>();

	private String commentMarker = "#";

	private String controllerName = "";

	private String name = ""; // for Findable interface

	/**
	 * @return Returns the columns.
	 */
	public ArrayList<AsciiWriterExtenderConfig> getColumns() {
		return columns;
	}

	/**
	 * @param columns
	 *            The columns to set.
	 */
	public void setColumns(ArrayList<AsciiWriterExtenderConfig> columns) {
		this.columns = columns;
	}

	/**
	 * @return Returns the header.
	 */
	public ArrayList<AsciiMetadataConfig> getHeader() {
		return header;
	}

	/**
	 * @param header
	 *            The header to set.
	 */
	public void setHeader(ArrayList<AsciiMetadataConfig> header) {
		this.header = header;
	}

	/**
	 * @return Returns the footer.
	 */
	public ArrayList<AsciiMetadataConfig> getFooter() {
		return footer;
	}

	/**
	 * @param footer
	 *            The footer to set.
	 */
	public void setFooter(ArrayList<AsciiMetadataConfig> footer) {
		this.footer = footer;
	}

	/**
	 * @return Returns the commentMarker.
	 */
	public String getCommentMarker() {
		return commentMarker;
	}

	/**
	 * @param commentMarker
	 *            The commentMarker to set.
	 */
	public void setCommentMarker(String commentMarker) {
		this.commentMarker = commentMarker;
	}

	/**
	 * @return Returns the controllerName.
	 */
	public String getControllerName() {
		return controllerName;
	}

	/**
	 * @param controllerName
	 *            The controllerName to set.
	 */
	public void setControllerName(String controllerName) {
		this.controllerName = controllerName;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public AsciiDataWriterConfiguration clone(){
		AsciiDataWriterConfiguration copy = new AsciiDataWriterConfiguration();
		copy.name = name;
		copy.controllerName = controllerName;
		copy.commentMarker = commentMarker;
		copy.header = new ArrayList<AsciiMetadataConfig>(header);
		copy.footer = new ArrayList<AsciiMetadataConfig>(footer);
		copy.columns = new ArrayList<AsciiWriterExtenderConfig>(columns);
		return copy;
	}

}
