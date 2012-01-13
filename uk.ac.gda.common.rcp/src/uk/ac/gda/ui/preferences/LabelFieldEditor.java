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

package uk.ac.gda.ui.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


/**
 * A "fake" field editor to allow a label to be added in a simple preferences page
 */
public class LabelFieldEditor extends FieldEditor {

	private static int counter = 0; 

	private Label label;
	
	/**
	 * Create the Label
     * @param labelText The text to appear in the label
     * @param parent The parent composite (normally getFieldEditorParent())
     */
    public LabelFieldEditor(String labelText, Composite parent) {
        init("LabelFieldEditor" + counter, labelText);
        counter++;
        createControl(parent);
    }

    @Override
	protected void adjustForNumColumns(int numColumns) {
        GridData gd = (GridData) label.getLayoutData();
        gd.horizontalSpan = numColumns;
     }

    @Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
    	// the label is actually created by the superclass, we just need
    	// to modify it here so that we can set numColumns later
        label = getLabelControl(parent);
        label.setLayoutData(new GridData());
    }

    @Override
	protected void doLoad() {
    }

    @Override
	protected void doLoadDefault() {
    }

    @Override
	protected void doStore() {
    }

    @Override
	public int getNumberOfControls() {
        return 1;
    }
}