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

package uk.ac.gda.tomography.scan.editor.view;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Maurizio Nagni
 */
public class TomographyMessagesUtility {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(TomographyMessagesUtility.class);

	private static final Map<Locale, ResourceBundle> resourceBundles;
	public static final String MESSAGE_BUNDLE = "MessageBundle";

	static {
		resourceBundles = Collections.synchronizedMap(new HashMap<Locale, ResourceBundle>());
	}

	private TomographyMessagesUtility() {
		super();
	}

	public static final String getMessage(Locale locale, TomographyMessages message) {
		try {
			return getResourceBundle(locale).getString(message.name());
		} catch (MissingResourceException e) {
			return message.name();
		}

	}

	public static final String getMessage(TomographyMessages message) {
		return getMessage(Locale.getDefault(), message);
	}

	private static ResourceBundle getResourceBundle(Locale locale) {
		if (!resourceBundles.containsKey(locale)) {
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
		}
		return resourceBundles.get(locale);
	}

}
