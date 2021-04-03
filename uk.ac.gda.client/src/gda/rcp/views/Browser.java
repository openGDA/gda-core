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

import static uk.ac.gda.api.acquisition.AcquisitionPredicates.isAcquisitionInstance;
import static uk.ac.gda.api.acquisition.AcquisitionPredicates.isDiffractionType;
import static uk.ac.gda.api.acquisition.AcquisitionPredicates.isTomographyType;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.api.acquisition.Acquisition;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;
import uk.ac.gda.client.composites.AcquisitionsBrowserCompositeFactory;
import uk.ac.gda.client.exception.GDAClientRestException;
import uk.ac.gda.common.entity.Document;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.rest.ConfigurationsRestServiceClient;

/**
 * Defines the information necessary to create a {@link AcquisitionsBrowserCompositeFactory}
 *
 * @param <T>
 *            the type of objects displayed in the browser
 */
public abstract class Browser<T extends Document> {

	private static final Logger logger = LoggerFactory.getLogger(Browser.class);

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

	protected Browser(AcquisitionConfigurationResourceType type) {
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
			acquisitionConfigurationResources = new ArrayList<>();
		}

		if (reload) {
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
				logger.error("Cannot retrieve browser working directory", e);
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
			logger.error("Cannot retrieve tURLLastPathSegment", e);
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
		try {
			List<T> documents = getConfigurationsRestServiceClient().getDocuments();
			Optional.ofNullable(documents).ifPresent(parseDocument);
		} catch (GDAClientRestException e) {
			logger.error("Cannot get documents from service", e);
		}
	}

	private Consumer<List<T>> parseDocument = documents ->
			documents.stream()
			.filter(getTypeFilter())
			.map(u -> new AcquisitionConfigurationResource<T>(getDocumentURL(u).get(), (T) u))
			.forEach(c -> acquisitionConfigurationResources.add(c));

    private Predicate<? super Document> getTypeFilter() {
    	return isAcquisitionInstance().and(getTomoPredicateNew().or(getMapPredicateNew()));
    }

    private Predicate<? super Document> getTomoPredicateNew() {
    	return document -> AcquisitionConfigurationResourceType.TOMO.equals(getType()) &&
    			isTomographyType().test(Acquisition.class.cast(document));
    }

    private Predicate<? super Object> getMapPredicateNew() {
    	return document -> AcquisitionConfigurationResourceType.MAP.equals(getType()) &&
    			isDiffractionType().test(Acquisition.class.cast(document));
    }

	private Optional<URL> getDocumentURL(Document document) {
		try {
			return Optional.ofNullable(getConfigurationsRestServiceClient().getDocumentURL(document.getUuid()));
		} catch (GDAClientRestException e) {
			logger.error("Cannot add resource", e);
			return Optional.empty();
		}
	}

	private ConfigurationsRestServiceClient getConfigurationsRestServiceClient() {
		return SpringApplicationContextFacade.getBean(ConfigurationsRestServiceClient.class);
	}
}