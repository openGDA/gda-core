/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.preference;

import java.text.DecimalFormat;

import org.dawb.common.ui.widgets.LabelFieldEditor;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import uk.ac.diamond.scisoft.analysis.deprecated.rcp.Activator;


public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private StringFieldEditor formatFieldEditor;
	/**
	 * @wbp.parser.constructor
	 */
	public PreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Preferences for viewing data sets available in nexus and ascii data.");
	}
	
	@SuppressWarnings("unused")
	@Override
	protected void createFieldEditors() {
		
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor("uk.ac.diamond.scisoft.analysis.data.set.filter");
				
		if (config!=null && config.length>0) {
			final Label sep = new Label(getFieldEditorParent(), SWT.NONE);
			sep.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 1, 5));
			BooleanFieldEditor showAll = new BooleanFieldEditor(DeprecatedPreferenceConstants.IGNORE_DATASET_FILTERS,"Show all possible data sets",getFieldEditorParent());
	      	addField(showAll);
	
			final StringBuilder buf = new StringBuilder("Current data set filters:\n");
			
			for (IConfigurationElement e : config) {
				buf.append("\t-    ");
				final String pattern     = e.getAttribute("regularExpression");
				buf.append(pattern);
				buf.append("\n");
			}
			buf.append("\nData set filters reduce the content available to plot and\ncompare for simplicity but can be turned off.\nAll data is shown in the nexus tree, filters are not applied.");
			final Label label = new Label(getFieldEditorParent(), SWT.WRAP);
			label.setText(buf.toString());
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		
		BooleanFieldEditor showXY = new BooleanFieldEditor(DeprecatedPreferenceConstants.SHOW_XY_COLUMN,"Show XY column.",getFieldEditorParent());
      	addField(showXY);

		BooleanFieldEditor showSize = new BooleanFieldEditor(DeprecatedPreferenceConstants.SHOW_DATA_SIZE,"Show size column.",getFieldEditorParent());
      	addField(showSize);
      	
		BooleanFieldEditor showDims = new BooleanFieldEditor(DeprecatedPreferenceConstants.SHOW_DIMS, "Show dimensions column.",getFieldEditorParent());
      	addField(showDims);
     	
		BooleanFieldEditor showShape = new BooleanFieldEditor(DeprecatedPreferenceConstants.SHOW_SHAPE, "Show shape column.",getFieldEditorParent());
      	addField(showShape);
 
      	new LabelFieldEditor("\nEditors with a 'Data' tab, show the data of the current plot.\nThis option sets the number format for the table and the csv file, if the data is exported.", getFieldEditorParent());

		formatFieldEditor = new StringFieldEditor(DeprecatedPreferenceConstants.DATA_FORMAT, "Number format:", getFieldEditorParent());
		addField(formatFieldEditor);
		
		new LabelFieldEditor("Examples: #0.0000, 0.###E0, ##0.#####E0, 00.###E0", getFieldEditorParent());
		
		new LabelFieldEditor("\n", getFieldEditorParent());

		IntegerFieldEditor playSpeed = new IntegerFieldEditor(DeprecatedPreferenceConstants.PLAY_SPEED,"Speed of slice play for n-Dimensional data sets (ms):",getFieldEditorParent()) {
			@Override
			protected boolean checkState() {
				if (!super.checkState()) return false;
				if (getIntValue()<10||getIntValue()>10000) return false;
				return true;
			}
		};
		addField(playSpeed);
		
	}

	@Override
	public void init(IWorkbench workbench) {
		
		
	}
	
    /**
     * Adjust the layout of the field editors so that
     * they are properly aligned.
     */
    @Override
	protected void adjustGridLayout() {
        super.adjustGridLayout();
        ((GridLayout) getFieldEditorParent().getLayout()).numColumns = 1;
    }


	
	@Override
	protected void checkState() {
		super.checkState();
		
		try {
			DecimalFormat format = new DecimalFormat(formatFieldEditor.getStringValue());
			format.format(100.001);
		} catch (IllegalArgumentException ne) {
			setErrorMessage("The format '"+formatFieldEditor.getStringValue()+"' is not valid.");
			setValid(false);
			return;
		}
		
		setErrorMessage(null);
		setValid(true);
		
	}

}
