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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;

		 /**
		 * An appender for Logback to send ELog messages.
		 * This Appender can be set up in LogServer.xml where:
		 *
		 * 		<Visit>12ab34</Visit> is the visit and can also be left blank
		 * 		<LogID>OPR</LogID> is the log ID
		 * 		<GroupID>DA</GroupID> is the group ID
		 * 		<UserID>gda</UserID> is the user ID, please ensure this is lower case for it to work in the production database
		 *
		 * 		<layout class="ch.qos.logback.classic.PatternLayout">
		 * 		<pattern>%-5level [%logger] %rEx - %m%n</pattern>
		 * 		</layout>
		 *
		 * The layout is used to format the messages that are sent to ELog, where pattern defines the format codes.
		 * Please see the manual at http://logback.qos.ch/manual/index.html for more information.
		 *
		 */
		public class ElogAppender extends AppenderBase<ILoggingEvent> {
			String
			visit="",
			logID="",
			groupID="",
			userID="";

			static final Logger logger = LoggerFactory.getLogger("gda.util.ElogAppender");
			@Override
			protected void append(ILoggingEvent eventObject) {
//				if(!eventObject.getCallerData()[0].getClassName().equals("gda.util.ElogEntry")){
//					return;
//				}
				String [] parts = eventObject.getMessage().split("%%");
				String visitID=visit;
				String title="";
				String content="";
				if( parts.length == 3){
					visitID=parts[0];
					title=parts[1];
					content=parts[2];
				} else {
					content = layout == null ? eventObject.getMessage() : layout.doLayout(eventObject);
					title = content.substring(0, content.indexOf(" -"));
				}
				try {
					ElogEntry.post(title, content,userID, visitID, logID, groupID, null);
				} catch (Exception e) {
					logger.warn("Error posting elog entry", e);
				}
			}

			/**
			 * @param visit
			 * Sets the visit. The visit is defined in the configuration file.
			 */
			public void setVisit(String visit) {
				this.visit = visit;
			}

			/**
			 * @return
			 * Returns the visit.
			 */
			public String getVisit() {
				 return visit;
			}

			/**
			 * @param logID
			 * Sets the log ID. The log ID is defined in the configuration file.
			 */
			public void setLogID(String logID) {
				this.logID = logID;
			}

			/**
			 * @return
			 * Returns the log ID.
			 */
			public String getLogID() {
				 return logID;
			}

			/**
			 * @param groupID
			 * Sets the group ID. The group ID is defined in the configuration file.
			 */
			public void setGroupID(String groupID) {
				this.groupID = groupID;
			}

			/**
			 * @return
			 * Returns the group ID
			 */
			public String getGroupID() {
				 return groupID;
			}

			/**
			 * @param userID
			 * Sets the user ID. The user ID is defined in the configuration file.
			 */
			public void setUserID(String userID) {
				this.userID = userID;
			}

			/**
			 * @return
			 * Returns the user ID.
			 */
			public String getUserID() {
				 return userID;
			}

			private Layout<ILoggingEvent> layout;

			public void setLayout(Layout<ILoggingEvent> layout) {
				this.layout = layout;
			}
}
