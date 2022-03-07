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

package uk.ac.gda.ui.tool.browser;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.Browser;
import gda.rcp.views.TreeViewerBuilder;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceSaveEvent;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.AcquisitionControllerException;
import uk.ac.gda.client.exception.GDAClientRestException;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.controller.AcquisitionController;
import uk.ac.gda.ui.tool.rest.ConfigurationsRestServiceClient;

/**
 * Base implementation of {@link Browser} for {@link ScanningAcquisition}s, with the following features:
 * <ul>
 * <li>provides context menu for loading and deleting acquisitions</li>
 * <li>loads the selected acquisition on double-click</li>
 * </ul>
 */
public abstract class ScanningAcquisitionBrowserBase extends Browser<ScanningAcquisition> {

	private ConfigurationsRestServiceClient configurationService;

	private static final Logger logger = LoggerFactory.getLogger(ScanningAcquisitionBrowserBase.class);
	private final AcquisitionController<ScanningAcquisition> controller;

	protected ScanningAcquisitionBrowserBase(AcquisitionConfigurationResourceType type, AcquisitionController<ScanningAcquisition> controller) {
		super(type);
		this.controller = controller;
	}

	@Override
	public ITreeContentProvider getContentProvider() {
		return new AcquisitionConfigurationResourceContentProvider();
	}

	@Override
	public TreeViewerBuilder<AcquisitionConfigurationResource<ScanningAcquisition>> getTreeViewBuilder() {
		return new ScanningAcquisitionTreeBuilder();
	}

	class ScanningAcquisitionTreeBuilder extends TreeViewerBuilder<AcquisitionConfigurationResource<ScanningAcquisition>> {

		private List<AcquisitionConfigurationResource<ScanningAcquisition>> resources;

		@SuppressWarnings("unchecked")
		@Override
		public AcquisitionConfigurationResource<ScanningAcquisition>[] getInputElements(boolean reload) {
			resources = getAcquisitionConfigurationResources(reload);
			return resources.toArray(new AcquisitionConfigurationResource[0]);
		}

		@Override
		protected void save(AcquisitionConfigurationResourceSaveEvent event) {
			if (!getType().equals(event.getType()))
				return;

			logger.debug("Adding resource at '{}'", event.getUuid());
			Optional.ofNullable(event.getUuid())
				.ifPresent(this::removeOldConfiguration);

			try {
				resources.add(controller.createAcquisitionConfigurationResource(event.getUuid()));
				updateContents();
			} catch (AcquisitionControllerException e) {
				logger.error("Could not add new resource to browser list - '{}'", event.getUuid(), e);
			}
		}

		private void removeOldConfiguration(UUID configuration) {
			resources.stream()
				.filter(resource -> resource.getResource().getUuid().equals(configuration))
				.findFirst()
				.ifPresent(resources::remove);
		}

		@Override
		protected void delete(UUID configuration) {
			logger.debug("Removing resource at '{}'", configuration);
			resources.removeIf(r -> r.getResource().getUuid().equals(configuration));
			updateContents();
		}

		@SuppressWarnings("unchecked")
		private void updateContents() {
			updateContents(resources.toArray(new AcquisitionConfigurationResource[0]));
		}
	}

	@Override
	public IDoubleClickListener getDoubleClickListener() {
		return event -> new ControllerRunnable("load", this::load).run();
	}

	@Override
	public ISelectionChangedListener getISelectionChangedListener(MenuManager contextMenu) {
		if (contextMenu != null) {
			contextMenu.addMenuListener(manager -> {
				manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
				manager.add(new ControllerAction(ClientMessages.LOAD, this::load));
				manager.add(new ControllerAction(ClientMessages.DELETE, this::delete));
			});
		}
		return event ->
			Optional.ofNullable(event.getStructuredSelection().getFirstElement())
					.map(AcquisitionConfigurationResource.class::cast)
					.ifPresent(this::setAcquisitionConfigurationResource);
	}

	private void setAcquisitionConfigurationResource(AcquisitionConfigurationResource<ScanningAcquisition> resource) {
		try {
			loadFromFile(resource.getLocation());
			setSelected(resource);
		} catch (AcquisitionControllerException e) {
			logger.error("Cannot parse acquisition configuration at {}", resource);
		}
	}

	public static final ScanningParameters getAcquisitionParameters(Object element) {
		return Optional.ofNullable(ScanningAcquisition.class.cast(AcquisitionConfigurationResource.class.cast(element).getResource()))
			.map(ScanningAcquisition::getAcquisitionConfiguration)
			.map(ScanningConfiguration::getAcquisitionParameters)
			.orElse(null);
	}

	private void load() throws AcquisitionControllerException {
		loadFromFile(getSelected().getLocation());
	}

	private void loadFromFile(URL url) throws AcquisitionControllerException {
		try {
			var acquisition = (ScanningAcquisition) getConfigurationService().getDocument(url);
			controller.loadAcquisitionConfiguration(acquisition);
		} catch (GDAClientRestException e) {
			throw new AcquisitionControllerException("Error loading acquisition from file '" + url + "'", e);
		}

	}

	private ConfigurationsRestServiceClient getConfigurationService() {
		if (configurationService == null) {
			configurationService = SpringApplicationContextFacade.getBean(ConfigurationsRestServiceClient.class);
		}
		return configurationService;
	}

	private void delete() throws AcquisitionControllerException {
		boolean confirmed = UIHelper.showConfirm("Are you sure you want to delete this acquisition?");

		if (confirmed) {
			controller.deleteAcquisitionConfiguration(getSelected().getResource().getUuid());
		}
	}

	/** Generic signature of controller methods */
	private interface ControllerOperation {
		void perform() throws AcquisitionControllerException;
	}

	/** Exception-handling runnable for {@link ControllerOperation} */
	private class ControllerRunnable implements Runnable {
		private final ControllerOperation operation;
		private final String name;

		public ControllerRunnable(String name, ControllerOperation operation) {
			this.operation = operation;
			this.name = name;
		}

		@Override
		public void run() {
			try {
				operation.perform();
			} catch (AcquisitionControllerException e) {
				var message = String.format("Error performing '%s' operation", name);
				UIHelper.showError(message, e, logger);
			}
		}
	}

	/** Action which delegates to a {@link ControllerRunnable} */
	private class ControllerAction extends Action {

		private final ControllerRunnable runnable;

		public ControllerAction(ClientMessages message, ControllerOperation operation) {
			this(ClientMessagesUtility.getMessage(message), operation);
		}

		private ControllerAction(String name, ControllerOperation operation) {
			super(name);
			this.runnable = new ControllerRunnable(name, operation);
		}

		@Override
		public void run() {
			runnable.run();
		}
	}

	private class AcquisitionConfigurationResourceContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			return inputElement instanceof AcquisitionConfigurationResource[] ? (AcquisitionConfigurationResource[]) inputElement : null;
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return false;
		}
	}

}