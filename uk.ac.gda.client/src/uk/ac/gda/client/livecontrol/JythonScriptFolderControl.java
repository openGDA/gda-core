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

package uk.ac.gda.client.livecontrol;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Present a folder of scripts as a combo box and run button.
 * <ul>
 * <li>folder - every python script except ones starting with an _ are shown in the list, with underscores converted into spaces</li>
 * <li>group - (Optional if within a Group) the widget has no title/label, so use this to identify it in the UI</li>
 * <li>jobTitle - (Optional) Some descriptive text to show in the Eclipse status bar while the scripts run</li>
 * </ul>
 * <p> Example configuration:
 * <pre>
 * {@code
 * <bean class="uk.ac.gda.client.livecontrol.JythonScriptFolderControl">
 *   <property name="folder" value="/dls_sw/i15/scripts/procedures"/>
 * </bean>
 * }
 * </pre>
 */

public class JythonScriptFolderControl extends JythonScriptListControl {

	private static final Logger logger = LoggerFactory.getLogger(JythonScriptFolderControl.class);

	public void setFolder(String folder) {
		try {
			var scriptsMap = Files.list(Paths.get(folder))
					.map(Path::toFile)
					.filter(File::isFile)
					.filter(path -> path.getName().endsWith(".py"))
					.filter(path -> !path.getName().startsWith("_"))
					.collect(Collectors.toMap(
						file -> file.getName().replace("_", " ").replaceAll(".py$",""),
						File::getPath,
						(oldValue, newValue) -> oldValue, TreeMap::new));

			setScripts(scriptsMap);
		} catch (IOException e) {
			logger.error("Folder {} not found", folder, e);
			setScripts(Collections.emptyMap());
		}
	}
}
