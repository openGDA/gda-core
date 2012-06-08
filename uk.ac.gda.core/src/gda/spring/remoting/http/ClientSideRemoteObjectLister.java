/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.spring.remoting.http;

import gda.spring.remoting.RemoteObjectLister;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A {@link RemoteObjectLister} that will retrieve a list of objects from the
 * specified URL.
 */
public class ClientSideRemoteObjectLister implements RemoteObjectLister {

	private String url;

	/**
	 * Creates a client-side remote object lister that will get the list of
	 * objects from the specified URL.
	 * 
	 * @param url the URL from which to retrieve the object list
	 */
	public ClientSideRemoteObjectLister(String url) {
		this.url = url;
	}
	
	@Override
	public Map<String, String> getAvailableObjects() {
		try {
			Map<String, String> availableObects = new LinkedHashMap<String, String>();

			URL theUrl = new URL(url);
			Object content = theUrl.getContent();
			if (!(content instanceof InputStream)) {
				throw new Exception("Server did not return list of objects in expected format");
			}
			
			InputStream inputStream = (InputStream) content;
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				String bits[] = line.split("\\|");
				String objectName = bits[0];
				String objectInterface = bits[1];
				availableObects.put(objectName, objectInterface);
			}
			inputStream.close();
			
			return availableObects;
			
		} catch (Exception e) {
			throw new RuntimeException("Could not retrieve list of available objects", e);
		}
	}

}
