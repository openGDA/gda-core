/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.messages;

import gda.jython.UserMessage;
import gda.jython.UserMessagesIO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * {@link MessageHandler} that saves messages to a file for each visit.
 */
public class FileMessageHandler implements MessageHandler, InitializingBean {
	
	private static final Logger logger = LoggerFactory.getLogger(FileMessageHandler.class);
	
	private File userMessagesDirectory;
	
	public void setUserMessagesDirectory(File userMessagesDirectory) {
		this.userMessagesDirectory = userMessagesDirectory;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		userMessagesDirectory.mkdirs();
	}
	
	private CacheLoader<String, List<UserMessage>> loader = new CacheLoader<String, List<UserMessage>>() {
		@Override
		public List<UserMessage> load(String visit) throws Exception {
			
			// Start with an empty list of messages for this visit
			List<UserMessage> visitMessages = new ArrayList<UserMessage>();
			
			// If there's a message file for this visit, load the messages from it
			final File msgFile = getUserMessagesFile(visit);
			if (msgFile.exists()) {
				visitMessages = loadUserMessagesFromFile(msgFile);
			}
			
			return visitMessages;
		}
	};
	
	private LoadingCache<String, List<UserMessage>> messages = CacheBuilder.newBuilder()
		.expireAfterAccess(5, TimeUnit.MINUTES)
		.build(loader);
	
	@Override
	public void saveMessage(String visit, UserMessage message) {
		try {
			List<UserMessage> visitMessages = messages.get(visit);
			visitMessages.add(message);
			File msgFile = getUserMessagesFile(visit);
			saveUserMessagesToFile(visitMessages, msgFile);
		} catch (Exception e) {
			logger.error("Unable to save message for visit " + visit, e);
		}
	}
	
	private List<UserMessage> loadUserMessagesFromFile(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		List<UserMessage> messages = UserMessagesIO.readMessages(fis);
		fis.close();
		return messages;
	}
	
	private void saveUserMessagesToFile(List<UserMessage> messages, File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		UserMessagesIO.writeMessages(messages, fos);
		fos.close();
	}
	
	private File getUserMessagesFile(String visit) {
		final String visitFile = String.format("%s.log", visit);
		final File msgFile = new File(userMessagesDirectory, visitFile);
		return msgFile;
	}
	
	@Override
	public List<UserMessage> getMessageHistory(String visit) {
		try {
			return messages.get(visit);
		} catch (ExecutionException e) {
			logger.error("Unable to load messages for visit " + visit, e);
			return null;
		}
	}

}
