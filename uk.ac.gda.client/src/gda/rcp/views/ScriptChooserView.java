/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.rcp.views;

import java.io.File;
import java.util.function.Predicate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import gda.configuration.properties.LocalProperties;
import gda.jython.JythonServerFacade;

//Based on uk.ac.gda.beamline.i19.shared.views.CommissioningView commit 1ee5e754467119ffdef03e848d39913c60b4cbc7

public class ScriptChooserView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "gda.rcp.views.scriptchooserview";

	private FormToolkit toolkit;
	private ScrolledForm form;

	private String header = "";

	/**
	 * This is a callback that will allow us to create the viewer and initialise
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.setText(header);
		toolkit.decorateFormHeading(form.getForm());

		ColumnLayout layout = new ColumnLayout();
		layout.topMargin = 0;
		layout.bottomMargin = 5;
		layout.leftMargin = 10;
		layout.rightMargin = 10;
		layout.horizontalSpacing = 10;
		layout.verticalSpacing = 10;
		layout.maxNumColumns = 4;
		layout.minNumColumns = 1;
		form.getBody().setLayout(layout);

		createScriptSection();
	}

	private void createScriptSection() {
		Composite section = createSection("Scripts", "Shortcuts to commissioning scripts", 1);
		GridData gd = new GridData();

		String PROP_LOCAL_PROCEDURE = "gda.beamline.scripts.procedure.dir";
		String s = LocalProperties.get(PROP_LOCAL_PROCEDURE);

		Predicate<String> criteria = name -> (name.endsWith("py") && !name.startsWith("_"));
		File[] matchingFiles = new File(s).listFiles( (dir, name) -> criteria.test(name) );

		if (matchingFiles.length > 0) {
			Label label = toolkit.createLabel(section,
				s.substring(s.lastIndexOf("/", s.lastIndexOf("/") - 1) + 1));
			label.setToolTipText(s);
			label.setLayoutData(gd);

			for (File f : matchingFiles) {
				String scriptname = f.toString().substring(s.length() + 1, f.toString().length() - 3);
				Button runButton = toolkit.createButton(section, scriptname, SWT.PUSH);
				runButton.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						widgetDefaultSelected(e);
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						JythonServerFacade.getInstance().runScript(f);
					}
				});
			}
		}
	}

	private Composite createSection(String title, String description,
			int numColumns) {
		Section section = toolkit.createSection(form.getBody(), ExpandableComposite.TWISTIE
				| ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.EXPANDED);
		section.setText(title);
		section.setDescription(description);

		Composite client = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = numColumns;
		client.setLayout(layout);
		section.setClient(client);
		section.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(false);
			}
		});
		return client;
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void dispose() {
		toolkit.dispose();
		super.dispose();
	}

	public void setHeader(String header) {
		this.header = header;
	}
}
