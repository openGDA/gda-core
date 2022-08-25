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

package uk.ac.diamond.daq.autoprocessing.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.diamond.daq.autoprocessing.ui.AutoProcessingField.AutoProcEditorHint;

/**
 * Description of the configuration required for Autoprocessing
 *
 */
public class AutoProcessingConfig {

	private String name;
	private String description;
	private AutoProcessingField<?>[] fields;

	public AutoProcessingConfig(String name, String description, AutoProcessingField<?>[] fields) {

		this.name = name;
		this.description = description;
		this.fields = fields;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public AutoProcessingField<?>[] getFields() {
		return fields;
	}

	public Map<String, Object> fieldsToMap() {

		Map<String, Object> fieldMap = new HashMap<>();

		for (AutoProcessingField<?> f : fields) {
			fieldMap.put(f.getName(), f.getValue());
		}

		return fieldMap;
	}

	public static final String APPNAME = "app_name";
	public static final String APPDESCRIPTION = "app_description";
	public static final String APPCONFIG = "config";

	public static final String DEFAULT = "default";
	public static final String DESCRIPTION = "description";
	public static final String REQUIRED = "required";
	public static final String HINT = "hint";
	public static final String LABEL = "label";
	public static final String UNIT = "unit";
	public static final String OPTIONS = "options";

	public static final Map<String, AutoProcEditorHint> HINT_MAP = Map.of("file", AutoProcEditorHint.FILE, "xrf",
			AutoProcEditorHint.XRF);

	/**
	 * Method to parse a generic Map (deserialised from json) into a AutoProcessingConfig.
	 * <p>
	 * Example Json:<br>
	 *
	 * <pre>
	 * {"app_name": "test_app",
	 * "app_description": "Process using a configuration NeXus file",
	 * "config": {
	 *   "file": {"default": "",
	 *            "label": "Proc Config",
	 *            "description": "NeXus configuration file",
	 *            "required": true,
	 *            "hint": "file"},
	 *   "jupyter": {"default": "",
	 *              "description": "Path to jupyter notebook template to run after processing",
	 *              "label": "Post-Run Notebook",
	 *              "required": false,
	 *              "hint": "file"}
	 *              }
	 *}
	 * </pre>
	 *
	 * @param map
	 * @return config
	 */
	public static AutoProcessingConfig parseMap(Map<?, ?> map) {

		checkKey(map, APPNAME);
		checkKey(map, APPDESCRIPTION);
		checkKey(map, APPCONFIG);

		String name = map.get(APPNAME).toString();
		String description = map.get(APPDESCRIPTION).toString();
		Object conf = map.get(APPCONFIG);

		if (!(conf instanceof Map)) {
			throw new IllegalArgumentException(APPCONFIG + " must be a Map");
		}

		Map<?, ?> conf_map = (Map<?, ?>) conf;

		List<AutoProcessingField<?>> fields = new ArrayList<AutoProcessingField<?>>();

		for (Entry<?, ?> entry : conf_map.entrySet()) {

			if (!(entry.getValue() instanceof Map)) {
				throw new IllegalArgumentException("Each config entry must be a Map");
			}

			Map<?, ?> param_map = (Map<?, ?>) entry.getValue();

			fields.add(buildField(param_map, entry.getKey().toString()));

		}

		return new AutoProcessingConfig(name, description, fields.toArray(new AutoProcessingField[fields.size()]));

	}

	private static AutoProcessingField<?> buildField(Map<?, ?> param_map, String name) {

		checkKey(param_map, DEFAULT);
		checkKey(param_map, DESCRIPTION);
		checkKey(param_map, REQUIRED);

		Object val = param_map.get(DEFAULT);

		AutoProcessingField<?> out = new AutoProcessingField<>(name, val, param_map.get(DESCRIPTION).toString());

		if (param_map.containsKey(HINT) && HINT_MAP.containsKey(param_map.get(HINT))) {
			out.setEditorHint(HINT_MAP.get(param_map.get(HINT)));
		}

		if (param_map.containsKey(LABEL)) {
			out.setLabel(param_map.get(LABEL).toString());
		}

		if (param_map.containsKey(UNIT)) {
			out.setUnit(param_map.get(UNIT).toString());
		}

		if (param_map.containsKey(OPTIONS) && val instanceof String) {
			Object object = param_map.get(OPTIONS);
			if (object instanceof List) {
				List<?> ol = (List<?>) object;

				String[] options = ol.stream().map(Object::toString).toArray(String[]::new);
				out.setOptions(options);
			}
		}

		return out;
	}

	private static void checkKey(Map<?, ?> map, String key) {
		if (!map.containsKey(key)) {
			throw new IllegalArgumentException("Map must contain " + key);
		}
	}
}
