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
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
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
import gda.device.Scannable;
import gda.jython.JythonServerFacade;
import uk.ac.gda.ui.viewer.MotorPositionViewer;

// Based on uk.ac.gda.beamline.i19.shared.views.CommissioningView commit 1ee5e754467119ffdef03e848d39913c60b4cbc7

public class CommissioningView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "gda.rcp.views.CommissioningView";
	private static final String FORMAT_ELEMENT_KEY = "format";

	private static final int NUM_COLUMNS = 2;
	private static final int DECIMAL_PLACES_DEFAULT = 2;

	private FormToolkit toolkit;
	private ScrolledForm form;

	private List<Map<String, Object>> sections;
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

		for (Map<String, Object> section : sections) {
			createEPICSSections(section);
		}

		createScriptSection();
	}

	private void createEPICSSections(Map<String, Object> sectionDefinition) {
		Composite sectionClient = createSection(sectionDefinition.get("title")
				.toString(), sectionDefinition.get("description").toString(), NUM_COLUMNS);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> elements = (List<Map<String, Object>>) sectionDefinition.get("elements");

		for (Map<String, Object> element : elements) {
			int dp = extractDecimalPlacesFromFormat(element.get(FORMAT_ELEMENT_KEY));
			Boolean readonly = element.get("readonly") != null;

			addEPICSElement(sectionClient, element.get("name").toString(),
					(Scannable) element.get("scannable"), dp,
					readonly);
		}
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

	private void addEPICSElement(Composite parent, String description,
			Scannable element, int decimalPlaces, boolean readonly) {
		GridData gd;
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;

		MotorPositionViewer motPos = new MotorPositionViewer(parent, element,
				description, false, gd);
		motPos.setEnabled(!readonly);
		motPos.setDecimalPlaces(decimalPlaces);
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

	public void setSections(List<Map<String, Object>> sections) {
		this.sections = sections;
	}

	private static int extractDecimalPlacesFromFormat(Object formatElement) {
		if(null==formatElement) return DECIMAL_PLACES_DEFAULT;
		return new DecimalFormat(formatElement.toString()).getMinimumFractionDigits();
	}
}
