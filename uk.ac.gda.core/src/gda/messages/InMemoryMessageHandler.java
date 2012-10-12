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

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import gda.jython.UserMessage;
import gda.util.BoundedLinkedList;

/**
 * {@link MessageHandler} that stores messages in memory. There is a limit to the number of messages stored per
 * visit (default 10).
 */
public class InMemoryMessageHandler implements MessageHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(InMemoryMessageHandler.class);
	
	private int maxMessagesPerVisit = 10;
	
	public void setMaxMessagesPerVisit(int maxMessagesPerVisit) {
		this.maxMessagesPerVisit = maxMessagesPerVisit;
	}
	
	private CacheLoader<String, List<UserMessage>> loader = new CacheLoader<String, List<UserMessage>>() {
		@Override
		public List<UserMessage> load(String visit) throws Exception {
			final List<UserMessage> visitMessages = new BoundedLinkedList<UserMessage>(maxMessagesPerVisit);
			return visitMessages;
		}
	};
	
	private LoadingCache<String, List<UserMessage>> messages = CacheBuilder.newBuilder().build(loader);
	
	@Override
	public void saveMessage(String visit, UserMessage message) {
		try {
			final List<UserMessage> visitMessages = messages.get(visit);
			visitMessages.add(message);
		} catch (ExecutionException e) {
			logger.error("Unable to save message for visit " + visit, e);
		}
	}
	
	@Override
	public List<UserMessage> getMessageHistory(String visit) {
		try {
			final List<UserMessage> visitMessages = messages.get(visit);
			return visitMessages;
		} catch (ExecutionException e) {
			logger.error("Unable to get message history for visit " + visit, e);
			return null;
		}
	}

}
