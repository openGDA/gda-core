/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.richbeans.api.generator.IGuiGeneratorService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A section to configure the script files to run before and/or after a scan.
 */
public class ScriptFilesSection extends AbstractMappingSection {

	@Override
	public boolean shouldShow() {
		// script files section only shown if bean is non null. Create an empty script files bean
		// in your spring configuration to allow script files to be set
		return getMappingBean().getScriptFiles() != null;
	}

	@Override
	public void createControls(Composite parent) {
		final Composite scriptsComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).applyTo(scriptsComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(scriptsComposite);

		final Label scriptsLabel = new Label(scriptsComposite, SWT.NONE);
		scriptsLabel.setText("Script Files");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(scriptsLabel);

		final Button editScriptsButton = new Button(scriptsComposite, SWT.PUSH);
		editScriptsButton.setImage(MappingExperimentUtils.getImage("icons/pencil.png"));
		editScriptsButton.setToolTipText("Select Script Files");
		GridDataFactory.swtDefaults().align(SWT.TRAIL, SWT.CENTER).applyTo(editScriptsButton);

		final IGuiGeneratorService guiGenerator = getService(IGuiGeneratorService.class);
		editScriptsButton.addListener(SWT.Selection, event ->
			guiGenerator.openDialog(getMappingBean().getScriptFiles(), parent.getShell(), "Select Script Files")
		);
	}

}
