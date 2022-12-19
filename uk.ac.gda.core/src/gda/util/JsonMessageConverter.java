/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.diamond.daq.classloading.GDAClassLoaderService;

/**
 * Utility class to convert JMS text and bytes messages to/from JSON
 */
public class JsonMessageConverter {
	/** cached object mapper to un/marshall JSON */
	private final ObjectMapper mapper;

	public JsonMessageConverter() {
		mapper = new ObjectMapper();
		var classLoader = GDAClassLoaderService
				.getClassLoaderService()
				.getClassLoaderForLibraryWithGlobalResourceLoading(
						ObjectMapper.class,
						Collections.emptySet());
		mapper.registerModules(ObjectMapper.findModules(classLoader));
	}

	/**
	 * Convert an object to a JSON string inside a {@link TextMessage}
	 * @param object The message body, will be serialized to JSON
	 * @param session The JMS session for the {@link TextMessage}
	 * @return a new JMS {@link Message}
	 * @throws JMSException If there is a problem with the session
	 * @throws JsonMessageConversionException if there is a problem serializing the body
	 */
	public Message toMessage(Object object, Session session) throws JMSException, JsonMessageConversionException {
		try {
			return session.createTextMessage(mapper.writeValueAsString(object));
		} catch (JsonProcessingException e) {
			throw new JsonMessageConversionException(e);
		}
	}

	/**
	 * Extract the (assumed) JSON body of a JMS {@link Message} and deserialize it to a Java object
	 * @param <T> The expected type of the body
	 * @param message The JMS {@link Message} to decode
	 * @param targetType The expected type of the body
	 * @return The deserialized message body
	 * @throws JMSException if there a problem extracting the body from the message
	 * @throws JsonMessageConversionException if there is a problem deserializing the body
	 */
	public <T> T fromMessage(Message message, Class<T> targetType) throws JMSException, JsonMessageConversionException {
		try {
			return deserializeMessage(message, targetType);
		} catch (JsonProcessingException e) {
			throw new JsonMessageConversionException(e);
		}
	}

	private <T> T deserializeMessage(Message message, Class<T> targetType) throws JMSException, JsonProcessingException {
		if (message instanceof TextMessage textMessage)
			return deserializeTextMessage(textMessage, targetType);
		else if (message instanceof BytesMessage bytesMessage)
			return deserializeBytesMessage(bytesMessage, targetType);
		else
			throw new JMSException("Could not deserialize " + message + " to type " + targetType);
	}

	private <T> T deserializeTextMessage(TextMessage message, Class<T> targetType) throws JMSException, JsonProcessingException {
		final var body = message.getText();
		return mapper.readValue(body, targetType);
	}

	private <T> T deserializeBytesMessage(BytesMessage message, Class<T> targetType) throws JMSException, JsonProcessingException {
		// Store message content in a buffer and convert to to UTF-8.
		// We need to do it this way rather than use BytesMessage::readUTF because
		// that method uses a modified version of UTF-8 which is unfriendly to messages
		// sent from non-Java applications.
		// See docs: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/io/DataInput.html
		final var buffer = new byte[(int) message.getBodyLength()];
		message.readBytes(buffer);
		final var body = new String(buffer, StandardCharsets.UTF_8);
		return mapper.readValue(body, targetType);
	}
}
