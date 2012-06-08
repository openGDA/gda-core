/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.client.experimentdefinition.ui.wizards;

import gda.configuration.properties.LocalProperties;

import java.io.File;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

public class ImportExperimentWizardPage extends WizardPage {

	private IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	private IWorkspace workspace = ResourcesPlugin.getWorkspace();
	private String rootPath;
	private List years;
	private List visits;
	private List experiments;
	
	public IWorkspace getWorkspace() {
		return workspace;
	}

	public String getRootPath() {
		return rootPath;
	}

	public IWorkspaceRoot getRoot() {
		return root;
	}

	public List getYears() {
		return years;
	}

	public List getVisits() {
		return visits;
	}

	public List getExperiments() {
		return experiments;
	}

	protected ImportExperimentWizardPage() {
		super("Import Experiment");
		setDescription("Import Experiment");
	}

	@Override
	public void createControl(Composite parent) {

		rootPath = LocalProperties.get(LocalProperties.GDA_DATA);
		File yearDir = new File(rootPath);

		String[] yearsList = yearDir.list();

		Composite chooseVisitArea = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(chooseVisitArea);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(chooseVisitArea);

		years = new List(chooseVisitArea, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		visits = new List(chooseVisitArea, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		experiments = new List(chooseVisitArea, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);

		visits.setBounds(0, 0, 100, 500);

		for (int i = 0; i < yearsList.length; i++) {
			years.add(yearsList[i]);
		}

		for (int i = 0; i < 10 - yearsList.length; i++) {
			years.add("                                ");
		}

		for (int i = 0; i < 10; i++) {
			visits.add("                                 ");
			experiments.add("                                  ");
		}

		years.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				String yearPath = rootPath + "/" + years.getSelection()[0];
				File visitDir = new File(yearPath);
				String[] visitList = visitDir.list();
				visits.removeAll();
				experiments.removeAll();
				for (int i = 0; i < visitList.length; i++) {
					visits.add(visitList[i]);
				}
			}
		});

		visits.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {

				String visitPath = rootPath + "/" + years.getSelection()[0] + "/" + visits.getSelection()[0] + "/xml";
				File experimentDir = new File(visitPath);
				String[] experimentList = experimentDir.list();
				experiments.removeAll();
				for (int i = 0; i < experimentList.length; i++) {
					if (!experimentList[i].startsWith("."))
						experiments.add(experimentList[i]);
				}
			}
		});
		
		experiments.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				setPageComplete(true);
			}
		});

		setPageComplete(false);
		setErrorMessage(null);
		setMessage(null);
		setControl(parent);
	}
}
