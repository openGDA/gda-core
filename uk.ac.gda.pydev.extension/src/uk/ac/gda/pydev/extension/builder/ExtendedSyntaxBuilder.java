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

package uk.ac.gda.pydev.extension.builder;

import gda.jython.InterfaceProvider;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;
import org.python.pydev.builder.PyDevBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.common.rcp.util.IFileUtils;
import uk.ac.gda.jython.ExtendedJythonMarkers;
import uk.ac.gda.jython.ExtendedJythonSyntax;
import uk.ac.gda.pydev.extension.Activator;
import uk.ac.gda.pydev.ui.preferences.PreferenceConstants;

/**
 * Class intercepts the builds and removes markers marking the extended Jython syntax.
 */
public class ExtendedSyntaxBuilder extends PyDevBuilder {

	private final static Logger logger = LoggerFactory.getLogger(ExtendedSyntaxBuilder.class);
	/**
	 * 
	 */
	public static final String ID = "uk.ac.gda.pydev.extension.ExtendedJythonSyntaxBuilder";

	private List<String> alii;

	private IProject[] projects;
	private CoreException exception;

	@SuppressWarnings("rawtypes")
	@Override
	protected IProject[] build(final int kind, final Map args, final IProgressMonitor monitor) throws CoreException {

		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		if (!store.getBoolean(PreferenceConstants.CHECK_SCRIPT_SYNTAX))
			return null;


		if (monitor.isCanceled())
			return null;

		this.alii = InterfaceProvider.getAliasedCommandProvider().getAliasedCommands();

		if (monitor.isCanceled())
			return null;

		projects = null;
		exception = null;

		if (!PlatformUI.isWorkbenchRunning())
			return null;
		if (PlatformUI.getWorkbench().getDisplay().isDisposed())
			return null;

		// Attempt to reduce thread access errors in PyDevBuilder
		// What can happen is that this build method can be started on
		// any thread but pydev assumes that is not the case. It then
		// can accesss UI items depending on what editors are open. This
		// can result in an obscure problem which means the caching fails
		// to save when the client exits.
		// So the UI works the first time it is opened after being built
		// into the client. But next time that the workspace is opened
		// the UI is invalid.
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					projects = superBuild(kind, args, monitor);
				} catch (CoreException e) {
					exception = e;
				}
			}
		});

		if (exception != null)
			throw exception;
		if (monitor.isCanceled())
			return null;

		correctExtendedSyntaxMarkers(getProject());
		return projects;
	}

	protected IProject[] superBuild(final int kind, @SuppressWarnings("rawtypes") final Map args, final IProgressMonitor monitor)
			throws CoreException {
		return super.build(kind, args, monitor);
	}

	private void correctExtendedSyntaxMarkers(IProject project) {
		try {
			IMarker[] markers = project.findMarkers(null, true, IResource.DEPTH_INFINITE);
			for (int i = 0; i < markers.length; i++) {
				checkMarker(markers[i]);
			}
		} catch (Exception e) {
			logger.error("Cannot correct markers", e);
		}

	}

	private void checkMarker(IMarker marker) throws Exception {

		if (marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING) != IMarker.SEVERITY_ERROR)
			return;

		final String message = marker.getAttribute(IMarker.MESSAGE, null);
		if (message == null)
			return;

		final IResource res = marker.getResource();
		if (!(res instanceof IFile))
			return;

		if (res.getMarker(marker.getId()) == null)
			return;

		if (message.contains("Undefined variable:")) {

			final int num = marker.getAttribute(IMarker.LINE_NUMBER, -1);
			if (num < 0)
				return;

			final IFile file = (IFile) res;
			int start = marker.getAttribute(IMarker.CHAR_START, -1);
			int end = marker.getAttribute(IMarker.CHAR_END, -1);
			if (start < 0 || end < 0)
				return;

			final String line = getLine(file, num);
			if (ExtendedJythonSyntax.isCommand(line, alii)) {
				ExtendedJythonMarkers.fixMarker(marker, line, alii, get(file).toString());
			}
		}
	}

	/**
	 * Assumes small files.
	 * 
	 * @param file
	 * @param line
	 * @return String
	 * @throws Exception
	 */
	private String getLine(IFile file, int line) throws Exception {
		// NOTE: Caching not required because pydev only returns the first error in the file currently.
		final List<String> buf = IFileUtils.parseFile(file);
		return buf.get(line);
	}

	/**
	 * Assumes small files. NOTE reads entire file into StringBuilder. Works with windows or unix files.
	 * 
	 * @param file
	 * @return StringBuilder
	 * @throws Exception
	 */
	private StringBuilder get(final IFile file) throws Exception {
		return IFileUtils.readFile(file);
	}
}
