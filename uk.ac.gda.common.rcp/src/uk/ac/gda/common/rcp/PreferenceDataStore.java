/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.common.rcp;

import java.lang.reflect.Type;
import java.util.List;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PreferenceDataStore {
	private static final Logger logger = LoggerFactory.getLogger(PreferenceDataStore.class);
	
	final GsonBuilder gsonBuilder;
	private final Gson gson;
	private final Preferences prefs;

	public PreferenceDataStore(String name) {
		prefs = InstanceScope.INSTANCE.getNode(name);
		gsonBuilder = new GsonBuilder();
		gson = gsonBuilder.excludeFieldsWithoutExposeAnnotation().create();
	}

	public <T> T loadConfiguration(String key, Class<T> classType) {
		String gsonText = prefs.get(key, null);
		if (gsonText != null) {
			return gson.fromJson(gsonText, classType);
		}
		return null;
	}

	public <T> List<T> loadArrayConfiguration(String key, Class<T> classType) {
		String gsonText = prefs.get(key, null);
		if (gsonText != null) {
			Type listType = listToken(TypeToken.of(classType)).getType();
			return gson.fromJson(gsonText, listType);
		}
		return null;
	}
	
	static <K> TypeToken<List<K>> listToken(TypeToken<K> element) {
		return new TypeToken<List<K>>() {}.where(new TypeParameter<K>() {}, element);
	}

	public <T> void saveConfiguration(String key, T data) {
		prefs.put(key, gson.toJson(data));
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			logger.error("Unable to store preference", e);
		}
	}
	
	public void removeConfiguration(String key) {
		prefs.remove(key);
	}
}
