/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

import java.util.Map;
import java.util.Objects;

import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import com.swtdesigner.SWTResourceManager;

import gda.jython.InterfaceProvider;

/**
 * This LiveControl implementation allows a script to be selected from a configured list of scripts through a combo box
 * and then run (non-blocking). The Spring configuration should specify:
 * <ul>
 * <li>group - the widget has no title/label, so use this to identify it in the UI</li>
 * <li>scripts as a map - keys should be user-friendly descriptions of the scripts!</li>
 * <li>jobTitle - (Optional) Some descriptive text to show in the Eclipse status bar while the scripts run</li>
 * </ul>
 * <p> Example configuration:
 * <pre>
 * {@literal		<bean id="endstation_configurations" class="uk.ac.gda.client.livecontrol.JythonScriptListControl">}
 * {@literal			<property name="jobTitle" value="Configuring endstation" />}
 * {@literal			<property name="group" value="Endstation configuration"/>}
 * {@literal			<property name="scripts">}
 * {@literal				<map>}
 * {@literal					<entry key="XrayEye in" value=".configs/in_xreye.py"/>}
 * {@literal					<entry key="XrayEye out" value=".configs/out_xreye.py"/>}
 * {@literal				</map>}
 * {@literal			</property>}
 * {@literal		</bean>}
 * </pre>
 */
public class JythonScriptListControl implements LiveControl {

	private Map<String, String> scripts;
	private String name;
	private String group;
	private String jobTitle;

	@Override
	public void createControl(Composite composite) {
		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		composite.setBackgroundMode(SWT.INHERIT_FORCE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(composite);

		Combo scriptsCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY | SWT.DROP_DOWN);
		scriptsCombo.setItems(scripts.keySet().toArray(new String[0]));

		Button runButton = new Button(composite, SWT.PUSH);
		runButton.setText("Run");
		runButton.setEnabled(false);

		scriptsCombo.addListener(SWT.Selection,
				event -> runButton.setEnabled(scriptsCombo.getSelectionIndex()>=0));

		runButton.addListener(SWT.Selection, event -> {
			int index = scriptsCombo.getSelectionIndex();
			String scriptName = scriptsCombo.getItem(index);
			runScript(scriptName);

			// let's reduce the probability of hitting the button,
			// then wondering whether we have indeed hit it.
			scriptsCombo.deselect(index);
			runButton.setEnabled(false);
		});
	}

	private void runScript(String name) {
		Job apply = Job.create(getJobTitle(name), monitor -> {
			// Using the blocking evaluateCommand so that this Job knows when the script has finished running
			InterfaceProvider.getCommandRunner().evaluateCommand("run '" + scripts.get(name) + "'");
			return Status.OK_STATUS;
		});

		apply.schedule();
	}

	@Override
	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public void setScripts(Map<String, String> configurations) {
		this.scripts = configurations;
	}

	private String getJobTitle(String name) {
		if (Objects.isNull(jobTitle)) {
			return "Running script: " + name;
		} else {
			return jobTitle + ": " + name;
		}
	}

	public void setJobTitle(String title) {
		jobTitle = title;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

}
