/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.tool;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Maurizio Nagni
 */
public class ClientMessagesUtility {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ClientMessagesUtility.class);

	private static final Map<Locale, ResourceBundle> resourceBundles;
	public static final String MESSAGE_BUNDLE = "MessageBundle";
	private static final String MESSAGE_UNDEFINED = "Message undefined";

	static {
		resourceBundles = Collections.synchronizedMap(new HashMap<Locale, ResourceBundle>());
	}

	private ClientMessagesUtility() {
		super();
	}

	public static final String getMessage(ClientMessages message) {
		return getMessage(null, message);
	}

	/**
	 * Return a ClientMessage constant however the value is passed as string.
	 * This feature may be useful when the configuration is done externally to the code (XML, JSON, properties).
	 * Protects against {@link ClientMessages#valueOf(String)} exceptions
	 * @param messageKey the required {@code ClientMessage} constant to search
	 * @return the value of an existing {@code ClientMessages} constant if exists, or
	 */
	public static final ClientMessages getClientMessageByString(String messageKey) {
		try {
			return ClientMessages.valueOf(messageKey);
		} catch (NullPointerException | IllegalArgumentException e) {
			return ClientMessages.MISSING_MESSAGE;
		}
	}

	public static final String getMessage(Locale locale, ClientMessages message) {
		if (Objects.isNull(message)) {
			return MESSAGE_UNDEFINED;
		}
		Locale tmpLocale = Objects.nonNull(locale) ? locale : Locale.getDefault();
		try {
			return getResourceBundle(tmpLocale).getString(message.name());
		} catch (MissingResourceException e) {
			return message.name();
		}
	}

	private static ResourceBundle getResourceBundle(Locale locale) {
		if (resourceBundles.containsKey(locale)) {
			return resourceBundles.get(locale);
		}

		try {
			ResourceBundle rb = ResourceBundle.getBundle(MESSAGE_BUNDLE, locale);
			resourceBundles.put(locale, rb);
		} catch (MissingResourceException e) {
			logger.warn("Missing ResourceBundle for Locale {}", locale);
			if (locale.equals(Locale.getDefault())) {
				throw e;
			}
			return getResourceBundle(Locale.getDefault());
		}
		return resourceBundles.get(locale);
	}

}
