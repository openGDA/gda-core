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

package uk.ac.gda.beans.validation;


/**
 * A class to hold errors.
 */
public class InvalidBeanMessage {

	private String   fileName;
	private String   folderName;
	private String[] messages;
    private String   primaryMessage;
    private String   label;
	/**
	 * 
	 * @param primaryMessage
	 * @param m
	 */
	public InvalidBeanMessage(final String primaryMessage, final String[] m) {
		this.primaryMessage = primaryMessage;
		this.messages = new String[m.length+1];
		messages[0] =  primaryMessage;
		for (int i = 0; i < m.length; i++) {
			messages[i+1] = m[i];
		}
	}
	/**
	 * 
	 * @param messages
	 */
	public InvalidBeanMessage(final String... messages) {
		this.messages = messages;
	}
	
	/**
	 * @return Returns the messages.
	 */
	public String[] getMessages() {
		return messages;
	}

	/**
	 * @param messages The messages to set.
	 */
	public void setMessages(String... messages) {
		this.messages = messages;
	}
	
	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder();
		for (int i = 0; i < messages.length; i++) {
			buf.append(messages[i]);
			buf.append("\n");
		}
		if (fileName!=null) {
			buf.append("In file: '"+folderName+"/"+fileName+"'.");
			buf.append("\n");
		}
		return buf.toString();
	}
	/**
	 * @return Returns the fileName.
	 */
	public String getFileName() {
		return fileName;
	}
	/**
	 * @param fileName The fileName to set.
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	/**
	 * @return Returns the folderName.
	 */
	public String getFolderName() {
		return folderName;
	}
	/**
	 * @param folderName The folderName to set.
	 */
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	/**
	 * @return Returns the primaryMessage.
	 */
	public String getPrimaryMessage() {
		return primaryMessage!=null?primaryMessage:messages[0];
	}
	/**
	 * @param primaryMessage The primaryMessage to set.
	 */
	public void setPrimaryMessage(String primaryMessage) {
		this.primaryMessage = primaryMessage;
	}
	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @param label The label to set.
	 */
	public void setLabel(String label) {
		this.label = label;
	}
}

