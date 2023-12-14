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

import static java.util.Collections.nCopies;
import static java.util.stream.Collectors.joining;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.swtdesigner.SWTResourceManager;

import gda.jython.InterfaceProvider;
import uk.ac.gda.client.viewer.ThreeStateDisplay;

/**
 * This LiveControl implementation allows a script to be selected from a configured list of scripts through a combo box
 * and then run (non-blocking). The Spring configuration should specify:
 * <ul>
 * <li>scripts as a map - keys should be user-friendly descriptions of the scripts!</li>
 * <li>group - (Optional if within a Group) the widget has no title/label, so use this to identify it in the UI</li>
 * <li>jobTitle - (Optional) Some descriptive text to show in the Eclipse status bar while the scripts run</li>
 * <li>displayName - (Optional) Text to add as a label in the grid </li>
 * <li>horizontalLayout - (Optional) The default layout you would like it to have. If False, it has a vertical layout </li>
 * </ul>
 * <p>
 * Example configuration:
 *
 * <pre>{@code
 * <bean id="endstation_configurations" class="uk.ac.gda.client.livecontrol.JythonScriptListControl">
 *   <property name="displayName" value="Photon Energy " />
 *   <property name="jobTitle" value="Configuring endstation" />
 *   <property name="group" value="Endstation configuration"/>
 *   <property name="horizontalLayout" value="false"/>
 *   <property name="scripts">
 *     <map>
 *       <entry key="XrayEye in" value=".configs/in_xreye.py"/>
 *       <entry key="XrayEye out" value=".configs/out_xreye.py"/>
 *     </map>
 *   </property>
 * </bean>
 * }
 * </pre>
 */
public class JythonScriptListControl extends LiveControlBase {

	private Map<String, String> scripts;
	private String jobTitle;
	private Combo scriptsCombo;
	private ThreeStateDisplay colourState;
	private String displayName = null;
	private boolean horizontalLayout = true;

	@Override
	public void createControl(Composite parent) {

		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		parent.setBackgroundMode(SWT.INHERIT_FORCE);

		Composite gridComposite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = 1;
		gridLayout.verticalSpacing = 1;
		gridComposite.setLayout(gridLayout);

		GridData buttonLayout = new GridData(SWT.FILL, SWT.FILL, true, true);
		buttonLayout.horizontalAlignment = GridData.FILL;
		buttonLayout.horizontalSpan = 1;

		GridData scriptComboLayout = new GridData(SWT.FILL, SWT.FILL, true, false);
		scriptComboLayout.horizontalAlignment = GridData.FILL;
		scriptComboLayout.horizontalSpan = 1;

		GridData stateDisplayLayout = new GridData(SWT.FILL, SWT.FILL, true, true);
		stateDisplayLayout.horizontalSpan = 1;
		stateDisplayLayout.horizontalAlignment = SWT.CENTER;
		stateDisplayLayout.minimumHeight = 28;

		Button runButton;

		if (displayName != null ) {
			CLabel displayNameLabel = new CLabel(gridComposite, SWT.CENTER);
			displayNameLabel.setText(displayName);
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
			gridData.horizontalAlignment = GridData.FILL;
			gridData.horizontalSpan = 1;
			displayNameLabel.setLayoutData(gridData);
		}

		if (horizontalLayout) {
			gridLayout.numColumns = displayName != null ? 4 : 3;

			scriptsCombo = new Combo(gridComposite, SWT.BORDER | SWT.READ_ONLY | SWT.DROP_DOWN);
			runButton    = new Button(gridComposite, SWT.PUSH | SWT.CENTER);
		}
		else {
			//We change the order if vertical layout so that the drop down doesn't go over button and user accidently pressing Run
			runButton    = new Button(gridComposite, SWT.PUSH | SWT.CENTER);
			scriptsCombo = new Combo(gridComposite, SWT.BORDER | SWT.READ_ONLY | SWT.DROP_DOWN);
		}
		scriptsCombo.setItems(scripts.keySet().toArray(new String[0]));
		scriptsCombo.setLayoutData(scriptComboLayout);

		runButton.setText("Run");
		runButton.setEnabled(false);
		runButton.addListener(SWT.Selection, event -> {
			int index = scriptsCombo.getSelectionIndex();
			String scriptName = scriptsCombo.getItem(index);
			runScript(scriptName);
			runButton.setEnabled(false);
		});
		runButton.setLayoutData(buttonLayout);

		colourState = new ThreeStateDisplay(gridComposite, "Ready", null, "Busy");
		Composite stateDisplay = colourState.getDisplay();
		stateDisplay.setLayoutData(stateDisplayLayout);

		scriptsCombo.addListener(SWT.Selection,
			event -> runButton.setEnabled(scriptsCombo.getSelectionIndex() >= 0)
		);
	}

	private void runScript(String name) {
		String title = getJobTitle(name);
		Job apply = Job.create(title, monitor -> {
			InterfaceProvider.getTerminalPrinter().print(addAsciiBorder(title));
			Display.getDefault().asyncExec(this::scriptStartedGUIActions);

			// Using the blocking evaluateCommand so that this Job knows when the script has finished running
			String result = InterfaceProvider.getCommandRunner().evaluateCommand("run '" + scripts.get(name) + "'");

			String script = scripts.get(name);

			if (result == null) {
				if (new File(script).isFile()) {
					InterfaceProvider.getTerminalPrinter().print("Error running script " + script + ". Check the log for details.");
				}
				else {
					InterfaceProvider.getTerminalPrinter().print("Error: Script " + script + " doesn't exist.");
				}
			}

			InterfaceProvider.getTerminalPrinter().print(addAsciiBorder("Completed '" + name + "'"));
			Display.getDefault().asyncExec(this::scriptFinishedGUIActions);
			return Status.OK_STATUS;
		});

		apply.schedule();
	}

	public void toggleLayoutControl() {
		if (horizontalLayout) {
			horizontalLayout = false;
		} else {
			horizontalLayout = true;
		}
	}

	private void scriptStartedGUIActions() {
		colourState.setRed();
		scriptsCombo.setEnabled(false);
	}

	private void scriptFinishedGUIActions() {
		colourState.setGreen();
		scriptsCombo.setEnabled(true);
	}

	private static final String BORDER_SYMBOL = "*";
	private static final char NEW_LINE = '\n';
	private static final char SPACE = ' ';

	private String addAsciiBorder(String message) {

		final String horizontal = nCopies(message.length() + 4, BORDER_SYMBOL).stream().collect(joining());
		return new StringBuilder().append(NEW_LINE)
			.append(horizontal)
			.append(NEW_LINE)
			.append(BORDER_SYMBOL)
			.append(SPACE)
			.append(message)
			.append(SPACE)
			.append(BORDER_SYMBOL)
			.append(NEW_LINE)
			.append(horizontal)
			.append(NEW_LINE)
			.toString();
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

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setHorizontalLayout(boolean horizontalLayout) {
		this.horizontalLayout = horizontalLayout;
	}

	public boolean getHorizontalLayout() {
		return horizontalLayout;
	}
}