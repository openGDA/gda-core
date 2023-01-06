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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class JsonMessageConverterTest {
	private static final ConverterTestClass TEST_BODY_CLASS = new ConverterTestClass(
			1, 2.5, null, Duration.ofSeconds(3));
	private static final ConverterTestRecord TEST_BODY_RECORD = new ConverterTestRecord(
			1, Optional.of(2.5), Optional.empty(), Duration.ofSeconds(3));
	private static final String TEST_BODY_AS_JSON = "{\"foo\":1,\"bar\":2.5,\"baz\":null,\"duration\":3.000000000}";
	private static final String TEST_BODY_AS_BROKEN_JSON = "{\"int:1,\"double\":2.5,\"baz\":null,\"duration\":3.000000000}";

	@ParameterizedTest
	@MethodSource("testBodies")
	public void serializesWithSession(Object body) throws JMSException, JsonMessageConversionException {
		var session = mockSession();
		var converter = new JsonMessageConverter();
		var message = converter.toMessage(body, session);

		assertEquals(TEST_BODY_AS_JSON, ((TextMessage) message).getText());
	}


	@ParameterizedTest
	@MethodSource("correctMessages")
	public void deserializesClassWhenCorrect(Message message) throws JMSException, JsonMessageConversionException {
		var converter = new JsonMessageConverter();
		var deserialized = converter.fromMessage(message, ConverterTestClass.class);
		assertEquals(TEST_BODY_CLASS, deserialized);
	}

	@ParameterizedTest
	@MethodSource("correctMessages")
	public void deserializesRecordWhenCorrect(Message message) throws JMSException, JsonMessageConversionException {
		var converter = new JsonMessageConverter();
		var deserialized = converter.fromMessage(message, ConverterTestRecord.class);
		assertEquals(TEST_BODY_RECORD, deserialized);
	}

	private static Stream<Message> correctMessages() throws JMSException, IOException {
		return Stream.of(
				mockTextMessage(TEST_BODY_AS_JSON),
				mockTextBytesMessage(TEST_BODY_AS_JSON, StandardCharsets.UTF_8)
				);
	}

	@Test
	public void deserializationBreaksWithInvalidJson() throws JMSException {
		var message = mockTextBytesMessage(TEST_BODY_AS_BROKEN_JSON, StandardCharsets.UTF_8);
		var converter = new JsonMessageConverter();
		assertThrows(
				JsonMessageConversionException.class,
				() -> converter.fromMessage(message, ConverterTestClass.class));
	}

	private static Stream<Object> testBodies() {
		return Stream.of(TEST_BODY_CLASS, TEST_BODY_RECORD);
	}

	private Session mockSession() throws JMSException {
		var session = mock(Session.class);
		when(session.createTextMessage(anyString()))
			.thenAnswer(i -> mockTextMessage((String) i.getArguments()[0]));
		return session;
	}

	private static Message mockTextMessage(String body) throws JMSException {
		var message = mock(TextMessage.class);
		when(message.getText()).thenReturn(body);
		return message;
	}

	private static Message mockTextBytesMessage(String body, Charset charset) throws JMSException {
		var asBytes = body.getBytes(charset);
		return mockBytesMessaage(asBytes);
	}

	private static Message mockBytesMessaage(byte[] body) throws JMSException {
		var message = mock(BytesMessage.class);
		doAnswer(i -> {
			var buffer = (byte[]) i.getArguments()[0];
			System.arraycopy(body, 0, buffer, 0, body.length);
			return null;
		}).when(message).readBytes(any());
		when(message.getBodyLength()).thenReturn((long) body.length);
		return message;
	}
}
