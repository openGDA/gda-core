/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class JsonHelper {
	private static final Logger logger = LoggerFactory.getLogger(JsonHelper.class);
	public static final String CLASS_TYPE = "classType";
	public static final String EDITOR_CLASS = "editorClass";

	private static Map<String, String> createMap(String jsonString) {
		Gson gson = new Gson();
		return gson.fromJson(jsonString, Map.class);
	}

	public static String toJson(Object object) {
		Gson gson = new Gson();
		return gson.toJson(object);

	}

	public static String getEditorClass(String jsonString) {
		return createMap(jsonString).get(EDITOR_CLASS);
	}

	public static <T> T createObject(String jsonString, Class<T> classType) {

		Map<String, String> values = createMap(jsonString);

		if (values.containsKey(CLASS_TYPE) && values.get(CLASS_TYPE).equals(classType.getCanonicalName())) {
			Gson gson = new Gson();
			return gson.fromJson(jsonString, classType);
		}
		return null;
	}


	// write value back to json file.
	public static void toJsonFile(Object object, String filepath) {
		Gson gson = new Gson();
		try (FileWriter writer = new FileWriter(filepath)) {
			gson.toJson(object, writer);
		}  catch (IOException e) {
			logger.error("Failed to write value back to json file.", e);
		}
	}
	public static <T> T readJson(String filePath, Class<T> classType){
		Gson gson = new Gson();
		try (FileReader reader = new FileReader(filePath)) {
			return gson.fromJson(reader, classType);
		} catch (IOException e) {
			logger.error("Failed to read json file.", e);
			return null;
		}
	}

	// read json from filePath.
	public static Map<String, Object> readJson(String filePath){
		return readJson(filePath, Map.class);
	}
}
