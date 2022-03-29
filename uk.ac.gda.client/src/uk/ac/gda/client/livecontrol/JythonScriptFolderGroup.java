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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Present a folder of scripts as a series of buttons inside a LiveControlGroup.
 * <ul>
 * <li>folder - every python script except ones starting with an _ are shown with their own button, with underscores converted into spaces</li>
 * <li>group - (Optional if within a Group) the widget has no title/label, so use this to identify it in the UI</li>
 * </ul>
 * <p> Example configuration:
 * <pre>
 * {@code
 * <bean id="jythonScriptFolderGroup" class="uk.ac.gda.client.livecontrol.JythonScriptFolderGroup">
 *   <property name="group" value="Scripts Folder Group" />
 *   <property name="numColumns" value="2" />
 *   <property name="folder" value="/dls_sw/i15/scripts/procedures"/>
 * </bean>
 * }
 * </pre>
 */

public class JythonScriptFolderGroup extends LiveControlGroup {

	private static final Logger logger = LoggerFactory.getLogger(JythonScriptFolderGroup.class);

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

			// var doesn't work here: The method setControls(List<LiveControl>) in the type LiveControlGroup is not applicable
			//						  for the arguments (List<JythonCommandControl>)
			// even though JythonCommandControl is a CommandControl, which extends LiveControlBase, which implements LiveControl
			List<LiveControl> list = scriptsMap.entrySet().stream()
					.sorted(Map.Entry.comparingByKey())
					.map(e -> {
						JythonCommandControl command = new JythonCommandControl();
						command.setButtonText(e.getKey());
						command.setCommand("run('"+e.getValue()+"')");
						return command;
					})
					.collect(Collectors.toList());

			setControls(list);
		} catch (IOException e) {
			logger.error("Folder {} not found", folder, e);
			setControls(Collections.emptyList());
		}
	}
}
