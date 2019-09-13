/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

import static gda.configuration.properties.LocalProperties.GDA_CONFIG;

import java.nio.file.Paths;
import java.util.function.Supplier;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.api.IScriptFiles;
import uk.ac.diamond.daq.mapping.ui.Activator;

/**
 * Simple dialog for choosing scripts to run before and/or after a scan.
 * After {@link #okPressed()}, the full paths can be retrieved with {@link #getBeforeScanScript()}
 * and {@link #getAfterScanScript()}. Changes are discarded if the 'Cancel' button is pressed.
 */
public class ScriptsSelectionDialog extends Dialog {

	private static final String DIALOG_TITLE = "Select scripts";
	private static final String BROWSE_ICON_PATH = "icons/folder-import.png";
	private static final String CLEAR_ICON_PATH = "icons/cross.png";
	private static final String SCRIPTS_SUBDIRECTORY = "scripts";
	private static final String[] FILTER_NAMES = new String[] {"Python scripts", "All files"};
	private static final String[] FILTER_EXTENSIONS = new String[] {"*.py", "*.*"};

	private String beforeScript;
	private String afterScript;
	private boolean alwaysRunAfterScript;

	private Supplier<String> beforeScriptSupplier;
	private Supplier<String> afterScriptSupplier;
	private Supplier<Boolean> alwaysRunAfterScriptSupplier;

	public ScriptsSelectionDialog(Shell parentShell, IScriptFiles scripts) {
		super(parentShell);
		this.beforeScript = scripts.getBeforeScanScript();
		this.afterScript = scripts.getAfterScanScript();
		this.alwaysRunAfterScript = scripts.isAlwaysRunAfterScript();
	}

	/**
	 * @return full path of script to run before scan, or empty string if not set
	 */
	public String getBeforeScanScript() {
		return beforeScript != null ? beforeScript : "";
	}

	/**
	 * @return full path of script to run after scan, or empty string if not set
	 */
	public String getAfterScanScript() {
		return afterScript != null ? afterScript : "";
	}

	/**
	 * @return <code>true</code> if "after scan" script should always be run (even in case of error/cancellation),
	 *         <code>false</code> if it should be run only if scan completes successfully
	 */
	public boolean getAlwaysRunAfterScript() {
		return alwaysRunAfterScript;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(composite);

		beforeScriptSupplier = createControlsRow(composite, "Before", getBeforeScanScript());
		afterScriptSupplier  = createControlsRow(composite, "After", getAfterScanScript());
		alwaysRunAfterScriptSupplier = createAlwaysRunRow(composite, alwaysRunAfterScript);

		return composite;
	}

	/**
	 * Creates label, text box, browse button, and clear button.
	 *
	 * @param composite on which to create widgets
	 * @param whenToRun 'Before' or 'After' for labels/captions
	 * @param currentScript currently selected script to populate text box
	 * @return supplier of text in the Text box
	 */
	private Supplier<String> createControlsRow(Composite composite, String whenToRun, String currentScript) {
		new Label(composite, SWT.NONE).setText(whenToRun + " scan");
		final Text textbox = new Text(composite, SWT.BORDER);
		textbox.setText(currentScript);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(textbox);
		final Button browse = new Button(composite, SWT.PUSH);
		browse.setImage(Activator.getImage(BROWSE_ICON_PATH));
		browse.setToolTipText("Browse...");
		browse.addListener(SWT.Selection, e -> textbox.setText(browseForScript(whenToRun.toLowerCase())));
		final Button clear = new Button(composite, SWT.PUSH);
		clear.setImage(Activator.getImage(CLEAR_ICON_PATH));
		clear.setToolTipText("Clear");
		clear.addListener(SWT.Selection, e -> textbox.setText(""));
		return textbox::getText;
	}

	private String browseForScript(String whenToRun) {
		FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
		fileDialog.setText("Select script to run " + whenToRun + " the scan");
		fileDialog.setFilterNames(FILTER_NAMES);
		fileDialog.setFilterExtensions(FILTER_EXTENSIONS);
		String visitDirectory = Paths.get(LocalProperties.get(GDA_CONFIG), SCRIPTS_SUBDIRECTORY).toString();
		fileDialog.setFilterPath(visitDirectory);
		String scriptPath = fileDialog.open();
		return scriptPath != null ? scriptPath : "";
	}

	private Supplier<Boolean> createAlwaysRunRow(Composite parent, boolean alwaysRunAfterScript) {
		final Label spacer = new Label(parent, SWT.NULL);
		spacer.setVisible(false);
		final Button alwaysRunCheckbox = new Button(parent, SWT.CHECK);
		GridDataFactory.swtDefaults().applyTo(alwaysRunCheckbox);
		alwaysRunCheckbox.setText("Always run script after scan");
		alwaysRunCheckbox.setSelection(alwaysRunAfterScript);
		return alwaysRunCheckbox::getSelection;
	}

	@Override
	protected void okPressed() {
		beforeScript = beforeScriptSupplier.get();
		afterScript = afterScriptSupplier.get();
		alwaysRunAfterScript = alwaysRunAfterScriptSupplier.get();
		super.okPressed();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(DIALOG_TITLE);
	}

	@Override
	protected Point getInitialSize() {
		// prevent dialog from being too narrow
		// when no scripts are specified on construction
		int minimumHeight = super.getInitialSize().y;
		return new Point(500, minimumHeight);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
}
