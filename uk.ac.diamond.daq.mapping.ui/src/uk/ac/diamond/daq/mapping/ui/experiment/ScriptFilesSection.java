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

import static uk.ac.diamond.daq.mapping.ui.MappingImageConstants.IMG_PENCIL;
import static uk.ac.gda.ui.tool.ClientMessages.SCRIPT_FILES_SELECT_TP;
import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;

import java.nio.file.Paths;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.daq.mapping.api.IScriptFiles;

/**
 * A section to configure the script files to run before and/or after a scan.
 */
public class ScriptFilesSection extends AbstractHideableMappingSection {

	private Text summaryText;

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);

		content = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(3).equalWidth(false).applyTo(content);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(content);
		new Label(content, SWT.NONE).setText("Script Files");

		Composite scriptsSummaryComposite = new Composite(content, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(scriptsSummaryComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(scriptsSummaryComposite);
		summaryText = new Text(scriptsSummaryComposite, SWT.MULTI | SWT.READ_ONLY);
		GridDataFactory.swtDefaults().grab(true, false).applyTo(summaryText);
		summaryText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));

		var editScriptsButton = new Button(content, SWT.PUSH);
		editScriptsButton.setImage(getImage(IMG_PENCIL));

		editScriptsButton.setToolTipText(getMessage(SCRIPT_FILES_SELECT_TP));
		GridDataFactory.swtDefaults().align(SWT.TRAIL, SWT.CENTER).applyTo(editScriptsButton);

		editScriptsButton.addListener(SWT.Selection, event -> openDialog());

		updateSummaryText();
	}

	private void openDialog() {
		IScriptFiles scripts = getBean().getScriptFiles();
		ScriptsSelectionDialog dialog = new ScriptsSelectionDialog(getShell(), scripts);
		if (dialog.open() == Window.OK) {
			scripts.setBeforeScanScript(dialog.getBeforeScanScript());
			scripts.setAfterScanScript(dialog.getAfterScanScript());
			scripts.setAlwaysRunAfterScript(dialog.getAlwaysRunAfterScript());
			updateSummaryText();
		}
	}

	private void updateSummaryText() {
		IScriptFiles scripts = getBean().getScriptFiles();
		StringBuilder summary = new StringBuilder();
		boolean beforeSet = Objects.nonNull(scripts.getBeforeScanScript()) && !scripts.getBeforeScanScript().isEmpty();
		boolean afterSet = Objects.nonNull(scripts.getAfterScanScript()) && !scripts.getAfterScanScript().isEmpty();

		if (beforeSet) {
			summary.append("before scan: " + getScriptName(scripts.getBeforeScanScript()));
			if (afterSet) summary.append("; ");
		}
		if (afterSet) {
			summary.append("after scan: " + getScriptName(scripts.getAfterScanScript()));
			if (scripts.isAlwaysRunAfterScript()) {
				summary.append("(*)");
			}
		}

		summaryText.setText(summary.toString());
		summaryText.setToolTipText(summary.toString());
		summaryText.getParent().layout(true, true);
	}

	private String getScriptName(String path) {
		return Paths.get(path).getFileName().toString();
	}

	@Override
	public void updateControls() {
		updateSummaryText();
	}
}
