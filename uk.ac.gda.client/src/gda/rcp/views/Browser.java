/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.ui.PlatformUI;

import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;
import uk.ac.gda.client.composites.AcquisitionsBrowserCompositeFactory;

/**
 * Defines the information necessary to create a {@link AcquisitionsBrowserCompositeFactory}
 *
 * @param <T>
 *            the type of objects displayed in the browser
 */
public abstract class Browser<T> {

	/**
	 * The location from where the class collects the objects, usually files, to display.
	 */
	private URL browserWorkingDir;
	/**
	 * The objects displayed in the browser
	 */
	private List<AcquisitionConfigurationResource<T>> acquisitionConfigurationResources;
	/**
	 * The type of objects displayed in the browser
	 */
	private final AcquisitionConfigurationResourceType type;
	/**
	 * Which object is actually selected in the browser
	 */
	private AcquisitionConfigurationResource<T> selected;

	public Browser(AcquisitionConfigurationResourceType type) {
		this.type = type;
	}

	/**
	 * Returns the browser objects
	 *
	 * @param reload
	 *            if {@code true} reloads the object from {@link #getBrowserWorkingDir()}
	 * @return the object displayed in the browser
	 */
	public List<AcquisitionConfigurationResource<T>> getAcquisitionConfigurationResources(boolean reload) {
		if (acquisitionConfigurationResources == null) {
			acquisitionConfigurationResources = new ArrayList<AcquisitionConfigurationResource<T>>();
		}

		if (acquisitionConfigurationResources == null || reload) {
			acquisitionConfigurationResources.clear();
			refreshResourcesList();
		}

		return acquisitionConfigurationResources;
	}

	/**
	 * The browser objects type
	 *
	 * @return the browser type configuration
	 */
	public AcquisitionConfigurationResourceType getType() {
		return type;
	}

	/**
	 * From where the browser loads its content
	 *
	 * @return a location
	 */
	public URL getBrowserWorkingDir() {
		if (browserWorkingDir == null) {
			try {
				browserWorkingDir = new URL("file", "localhost",
						PlatformUI.getWorkbench().getService(IFilePathService.class).getVisitConfigDir());
			} catch (MalformedURLException e) {

			}
		}
		return browserWorkingDir;
	}

	/**
	 * Extracts the last element from a {@link AcquisitionConfigurationResource#getLocation()}. For example for
	 *
	 * <pre>
	 * file://localhost/c$/WINDOWS/clock.avi
	 * </pre>
	 *
	 * returns
	 *
	 * <pre>
	 * clock.avi
	 * </pre>
	 *
	 * @param element
	 *            the resource from where extract the string
	 * @return the URL last segment in the URL path
	 */
	public static final String getURLLastPathSegment(AcquisitionConfigurationResource<?> element) {
		String name = "N/A";
		try {
			File file = new File(element.getLocation().toURI().getPath());
			name = file.getName();
		} catch (URISyntaxException e) {
		}
		return name;
	}

	/**
	 * Formats the objects in a suitable format for the TreeViewer contained in
	 * {@link AcquisitionsBrowserCompositeFactory}
	 *
	 * @return a tree content provider
	 */
	public abstract ITreeContentProvider getContentProvider();

	/**
	 * Adds a column to the the TreeViewer contained in {@link AcquisitionsBrowserCompositeFactory}
	 *
	 * @param builder
	 */
	public abstract void addColumns(TreeViewerBuilder<AcquisitionConfigurationResource<T>> builder);

	/**
	 * Generates the {@link TreeViewerBuilder} for the {@link AcquisitionsBrowserCompositeFactory}
	 *
	 * @return a builder
	 */
	public abstract TreeViewerBuilder<AcquisitionConfigurationResource<T>> getTreeViewBuilder();

	/**
	 * An action executed when a row in the browser is selected
	 *
	 * @return a selection listener
	 */
	public abstract ISelectionChangedListener getISelectionChangedListener(MenuManager contextMenu);

	/**
	 * An action executed when the user double click a row in the browser
	 *
	 * @return a listener
	 */
	public abstract IDoubleClickListener getDoubleClickListener();

	protected void setSelected(AcquisitionConfigurationResource<T> selected) {
		this.selected = selected;
	}

	protected AcquisitionConfigurationResource<T> getSelected() {
		return selected;
	}

	private void refreshResourcesList() {
		acquisitionConfigurationResources.clear();
		File[] resources = new File(getBrowserWorkingDir().getFile()).listFiles((dir, name) -> {
			return (name.endsWith("." + type.getExtension()));
		});
		Optional.ofNullable(resources).ifPresent(parseFiles);
	}

	Consumer<File[]> parseFiles = (resources) -> {
		Arrays.stream(resources).forEachOrdered(c -> {
			try {
				acquisitionConfigurationResources.add(new AcquisitionConfigurationResource(c.toURI().toURL(), null));
			} catch (MalformedURLException e) {
			}
		});
	};

}