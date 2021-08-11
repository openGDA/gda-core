/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.client.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import uk.ac.gda.client.properties.stage.position.ScannableKeys;
import uk.ac.gda.ui.tool.spring.ClientSpringProperties;

/**
 * Converts a pair of String:String to {@code ScannableKeys}
 *
 * @see ClientSpringProperties
 *
 * @author Maurizio Nagni
 */
@Component
public class ScannableKeysConverter implements Converter<String, ScannableKeys> {

	private static final Logger logger = LoggerFactory.getLogger(ScannableKeysConverter.class);

	@Override
	public ScannableKeys convert(String source) {
		String[] keys = source.split(":");
		var result = new ScannableKeys();
		if(keys.length == 2) {
			result.setGroupId(keys[0]);
			result.setScannableId(keys[1]);
		} else {
			logger.warn("Cannot convert scannableKeys: {}", source);
		}
		return result;
	}
}
