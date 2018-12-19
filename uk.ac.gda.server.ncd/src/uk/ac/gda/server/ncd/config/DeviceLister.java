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

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.jython.InterfaceProvider;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

public class DeviceLister {

	
	public static String generateDeviceListHTML() throws DeviceException {
		return generateDeviceList("", "", "<p>", "</p>", " <b>", "</b> ", "", "", ",", "; ", "", "", ",", "; ", " (", ")");
	}

	public static String generateDeviceList() throws DeviceException {
		return generateDeviceList("", "", "", "\n", "", ":", "", "", ",", ";", "", "", ",", ";", "", "");
	}

	public static String generateDeviceList(String bodyStart, String bodyEnd, String lineStart, String lineEnd, String nameStart, String nameEnd, String inputStart, String inputEnd, String inputInterSep, String inputFinalSep, String extraStart, String extraEnd, String extraIntersep, String extraFinalSep, String permissionStart, String permissionEnd) throws DeviceException {

		SortedMap<String, Object> map = new TreeMap<String, Object>(InterfaceProvider.getJythonNamespace().getAllFromJythonNamespace());
		StringBuilder sb = new StringBuilder(bodyStart);
		for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext();) {
			String name = iterator.next();
			if (!(map.get(name) instanceof Scannable)) {
				continue;
			}
			Scannable scannable = (Scannable) map.get(name);
			sb.append(lineStart);
			
			sb.append(nameStart);
			sb.append(name);
			sb.append(nameEnd);
			
			boolean first = true;
			for (String in : scannable.getInputNames()) {
				if (!first) sb.append(inputInterSep);
				sb.append(inputStart);
				sb.append(in);
				sb.append(inputEnd);
				first = false;
			}
			
			first = true;
			sb.append(inputFinalSep);
			for (String en : scannable.getExtraNames()) {
				if (!first) sb.append(extraIntersep);
				sb.append(extraStart);
				sb.append(en);
				sb.append(extraEnd);
				first = false;
			}
			sb.append(extraFinalSep);

			sb.append(permissionStart);
			sb.append(scannable.getProtectionLevel());
			sb.append(permissionEnd);
			sb.append(lineEnd);
		}
		sb.append(bodyEnd);
		return sb.toString();
	}
}