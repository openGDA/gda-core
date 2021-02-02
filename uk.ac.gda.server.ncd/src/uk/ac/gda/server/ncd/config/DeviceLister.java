/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.config;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.jython.IJythonNamespace;
import gda.jython.InterfaceProvider;

public class DeviceLister {
	private static final String HTML_FORMAT = "<b>%s</b> %s; %s; (%d)<br>";
	private static final String TEXT_FORMAT = "%s %s; %s; (%d)";

	public static String generateDeviceListHTML() {
		return generateDeviceList(HTML_FORMAT);
	}

	public static String generateDeviceList() {
		return generateDeviceList(TEXT_FORMAT);
	}

	private static String formatScannable(String format, Scannable s) {
		int level;
		try {
			level = s.getProtectionLevel();
		} catch (DeviceException e) {
			level = -1;
		}
		return String.format(format,
				s.getName(),
				stream(s.getInputNames()).collect(joining(",")),
				stream(s.getExtraNames()).collect(joining(",")),
				level);
	}

	public static String generateDeviceList(String format) {
		IJythonNamespace namespace = InterfaceProvider.getJythonNamespace();
		return namespace
				.getAllNamesForType(Scannable.class)
				.stream()
				.sorted()
				.map(namespace::getFromJythonNamespace)
				.map(Scannable.class::cast) // safe based on getAllNamesForType
				.map(s -> formatScannable(format, s))
				.collect(joining("\n"));
	}
}