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

package uk.ac.gda.pydev.ui.preferences;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import uk.ac.gda.pydev.ScriptProjectCreator;
import uk.ac.gda.pydev.extension.Activator;

public class PreferenceInitializer extends AbstractPreferenceInitializer {
//	private static final Logger logger = LoggerFactory.getLogger(PreferenceInitializer.class);


	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.CLOSE_RICH_BEAN_EDITORS, true);
		store.setDefault(PreferenceConstants.SHOW_CONFIG_SCRIPTS,     false);
		store.setDefault(PreferenceConstants.SHOW_GDA_SCRIPTS,        false);
		store.setDefault(PreferenceConstants.SHOW_XML_CONFIG,         false);
		store.setDefault(PreferenceConstants.CHECK_SCRIPT_SYNTAX, false);
		store.setDefault(PreferenceConstants.GDA_PYDEV_ADD_GDA_LIBS_JARS, false);
		store.setDefault(PreferenceConstants.GDA_PYDEV_ADD_DEFAULT_JAVA_JARS, false);

		
		store.addPropertyChangeListener(new IPropertyChangeListener() {		
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				/**
				 * if a new folder is to be created/deleted or syntax checking is changed 
				 * then rebuild the projects
				 */
				String prop = event.getProperty();
				if( prop.equals(PreferenceConstants.SHOW_CONFIG_SCRIPTS) ||
						prop.equals(PreferenceConstants.SHOW_GDA_SCRIPTS) ||
						prop.equals(PreferenceConstants.CHECK_SCRIPT_SYNTAX ))
				{
					Job job = new Job("Create/Hide Script projects"){

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								ScriptProjectCreator.createProjects(monitor);
								return Status.OK_STATUS;
							} catch (CoreException e) {
								return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(),e );
							}
						}
					};
					job.setUser(true);
					job.schedule();
				}
				if( prop.equals(PreferenceConstants.SHOW_XML_CONFIG)){
					Job job = new Job("Create/Hide XML project"){

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								ScriptProjectCreator.handleShowXMLConfig(monitor);
								return Status.OK_STATUS;
							} catch (CoreException e) {
								return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(),e );
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
