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

package uk.ac.gda.client.scripting;

import static uk.ac.gda.client.scripting.PreferenceConstants.CHECK_SCRIPT_SYNTAX;
import static uk.ac.gda.client.scripting.PreferenceConstants.GDA_PYDEV_ADD_DEFAULT_JAVA_JARS;
import static uk.ac.gda.client.scripting.PreferenceConstants.GDA_PYDEV_ADD_GDA_LIBS_JARS;
import static uk.ac.gda.client.scripting.PreferenceConstants.SHOW_CONFIG_SCRIPTS;
import static uk.ac.gda.client.scripting.PreferenceConstants.SHOW_GDA_SCRIPTS;
import static uk.ac.gda.client.scripting.PreferenceConstants.SHOW_XML_CONFIG;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.GDAClientActivator;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	private static final Logger logger = LoggerFactory.getLogger(PreferenceInitializer.class);

	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = GDAClientActivator.getDefault().getPreferenceStore();
		store.setDefault(SHOW_CONFIG_SCRIPTS, false);
		store.setDefault(SHOW_GDA_SCRIPTS, false);
		store.setDefault(SHOW_XML_CONFIG, false);
		store.setDefault(CHECK_SCRIPT_SYNTAX, false);
		store.setDefault(GDA_PYDEV_ADD_GDA_LIBS_JARS, false);
		store.setDefault(GDA_PYDEV_ADD_DEFAULT_JAVA_JARS, false);

		store.addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				/**
				 * if a new folder is to be created/deleted or syntax checking is changed then rebuild the projects
				 */
				String prop = event.getProperty();
				if (prop.equals(SHOW_CONFIG_SCRIPTS)
						|| prop.equals(SHOW_GDA_SCRIPTS)) {

					logger.debug("Change script property: {} to {}", prop, event.getNewValue());
					Job job = new Job("Recreate set of Script projects") {

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								ScriptProjectCreator.createProjects(monitor);
								return Status.OK_STATUS;
							} catch (Exception e) {
								return new Status(IStatus.ERROR, GDAClientActivator.PLUGIN_ID, e.getMessage(), e);
							}
						}
					};
					job.setUser(true);
					job.schedule();
				}
				if (prop.equals(CHECK_SCRIPT_SYNTAX)) {

					logger.debug("Change script property: {} to {}", prop, event.getNewValue());
					Job job = new Job("Setup Jython interpreter and projects") {

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								ScriptProjectCreator.setupInterpreterAndProjects(monitor);
								return Status.OK_STATUS;
							} catch (Exception e) {
								return new Status(IStatus.ERROR, GDAClientActivator.PLUGIN_ID, e.getMessage(), e);
							}
						}
					};
					job.setUser(true);
					job.schedule();
				}
				if (prop.equals(SHOW_XML_CONFIG)) {

					logger.debug("Change xml property: {} to {}", prop, event.getNewValue());
					Job job = new Job("Create/Hide XML project") {

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								ScriptProjectCreator.handleShowXMLConfig(monitor);
								return Status.OK_STATUS;
							} catch (CoreException e) {
								return new Status(IStatus.ERROR, GDAClientActivator.PLUGIN_ID, e.getMessage(), e);
							}
						}
					};
					job.setUser(true);
					job.schedule();
				}
			}
		});
	}

}
