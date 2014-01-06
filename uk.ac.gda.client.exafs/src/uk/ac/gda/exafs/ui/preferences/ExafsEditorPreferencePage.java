/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import uk.ac.gda.exafs.ExafsActivator;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By subclassing
 * <samp>FieldEditorPreferencePage</samp>, we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that belongs to the main
 * plug-in class. That way, preferences can be accessed directly via the preference store.
 */

public class ExafsEditorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * 
	 */
	public static final String ID = "uk.ac.gda.exafs.preferences.exafsEditorPreferencePage";

	/**
	 * 
	 */
	public ExafsEditorPreferencePage() {
		super(GRID);
		setPreferenceStore(ExafsActivator.getDefault().getPreferenceStore());
		setDescription("Preferences for the editors involved in defining the scan parameters, sample parameters, detector parameters and output parameters.\n");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to manipulate various
	 * types of preferences. Each field editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {

		final Group xas = new Group(getFieldEditorParent(), SWT.NONE);
		xas.setText("XAS");
		xas.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label linkLabel = new Label(xas, SWT.NONE);
		linkLabel.setText(" Fields to recalculate when element changes:");
		linkLabel.setToolTipText("When ticked the value will also be set when the editor is opened.");
		createBoolean(ExafsPreferenceConstants.INITIAL_ENERGY_ELEMENT_LINK, "&Initial Energy.", xas);
		createBoolean(ExafsPreferenceConstants.FINAL_ENERGY_ELEMENT_LINK, "&Final Energy.", xas);
		createBoolean(ExafsPreferenceConstants.A_ELEMENT_LINK, "&A value (via Gaf1).", xas);
		createBoolean(ExafsPreferenceConstants.B_ELEMENT_LINK, "&B value (via Gaf2).", xas);
		createBoolean(ExafsPreferenceConstants.C_ELEMENT_LINK, "&C value (via Gaf3).", xas);
		createBoolean(ExafsPreferenceConstants.C_MIRRORS_B_LINK, "&C value mirrors B about the edge value.", xas);
		createBoolean(ExafsPreferenceConstants.EXAFS_GRAPH_EDITABLE, "&Edit edges by dragging lines on graph", xas);
		createBoolean(ExafsPreferenceConstants.DETECTOR_OVERLAY_ENABLED, "&Edit detector ROIs by dragging lines on graph", xas);
		createDisabledBoolean(ExafsPreferenceConstants.EXAFS_FINAL_ANGSTROM, "&Final Energy in Angstrom", xas);
	}

	private void createBoolean(final String id, final String label, final Composite parent) {
		final BooleanFieldEditor ed = new BooleanFieldEditor(id, label, SWT.NONE, parent);
		ed.setPreferenceStore(ExafsActivator.getDefault().getPreferenceStore());
		addField(ed);
	}
	private void createDisabledBoolean(final String id, final String label, final Composite parent) {
		final BooleanFieldEditor ed = new BooleanFieldEditor(id, label, SWT.NONE, parent);
		ed.setPreferenceStore(ExafsActivator.getDefault().getPreferenceStore());
		addField(ed);
		ed.setEnabled(false, parent);
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	public void checkState() {
		super.checkState();
		// Can do additional validation here.
	}
}