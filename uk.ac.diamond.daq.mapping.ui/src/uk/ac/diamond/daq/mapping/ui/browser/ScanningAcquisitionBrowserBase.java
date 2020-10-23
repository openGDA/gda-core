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

package uk.ac.diamond.daq.mapping.ui.browser;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.api.acquisition.AcquisitionControllerException;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;

/**
 * Base implementation of {@link Browser} for {@link ScanningAcquisition}s, with the following features:
 * <ul>
 * <li>provides context menu for loading and deleting acquisitions</li>
 * <li>loads the selected acquisition on double-click</li>
 * </ul>
 */
public abstract class ScanningAcquisitionBrowserBase extends Browser<ScanningAcquisition> {

	private static final Logger logger = LoggerFactory.getLogger(ScanningAcquisitionBrowserBase.class);
	private final AcquisitionController<ScanningAcquisition> controller;

	public ScanningAcquisitionBrowserBase(AcquisitionConfigurationResourceType type, AcquisitionController<ScanningAcquisition> controller) {
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
			resources = getAcquisitionConfigurationResources(reload).stream()
					.map(AcquisitionConfigurationResource::getLocation)
					.map(ScanningAcquisitionBrowserBase.this::parseAcquisition)
					.filter(Objects::nonNull).collect(Collectors.toList());

			return resources.toArray(new AcquisitionConfigurationResource[0]);
		}

		@Override
		protected void save(URL configuration) {
			if (configurationIsRelevant(configuration)) {
				logger.debug("Adding resource at '{}'", configuration);

				// if overwriting, remove old version
				removeOldConfiguration(configuration);

				try {
					resources.add(controller.parseAcquisitionConfiguration(configuration));
					updateContents();
				} catch (AcquisitionControllerException e) {
					logger.error("Could not add new resource to browser list - '{}'", configuration, e);
				}
			}
		}

		private void removeOldConfiguration(URL configuration) {
			resources.stream()
				.filter(resource -> resource.getLocation().equals(configuration))
				.findFirst().ifPresent(resources::remove);
		}

		@Override
		protected void delete(URL configuration) {
			if (configurationIsRelevant(configuration)) {
				logger.debug("Removing resource at '{}'", configuration);
				try {
					resources.remove(controller.parseAcquisitionConfiguration(configuration));
					updateContents();
				} catch (AcquisitionControllerException e) {
					logger.error("Could not remove resource '{}'", configuration, e);
				}
			}
		}

		@SuppressWarnings("unchecked")
		private void updateContents() {
			updateContents(resources.toArray(new AcquisitionConfigurationResource[0]));
		}

		private boolean configurationIsRelevant(URL configuration) {
			return configuration != null &&
					configuration.getPath().endsWith(getType().getExtension());
		}
	}

	private AcquisitionConfigurationResource<ScanningAcquisition> parseAcquisition(URL resourceLocation) {
		try {
			return controller.parseAcquisitionConfiguration(resourceLocation);
		} catch (AcquisitionControllerException e) {
			logger.error("Could not parse resource at {}", resourceLocation, e);
			return null;
		}
	}

	@Override
	public IDoubleClickListener getDoubleClickListener() {
		return event -> new ControllerRunnable("load", this::load).run();
	}

	@SuppressWarnings("unchecked")
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
					.map(AcquisitionConfigurationResource::getLocation)
					.ifPresent(this::setParseAndSetSelected);
	}

	private void setParseAndSetSelected(URL url) {
		try {
			setSelected(controller.parseAcquisitionConfiguration(url));
		} catch (AcquisitionControllerException e) {
			logger.error("Cannot parse acquisition configuration at {}", url);
		}
	}

	public static final ScanningParameters getAcquisitionParameters(Object element) {
		return ScanningAcquisition.class.cast(AcquisitionConfigurationResource.class.cast(element).getResource())
				.getAcquisitionConfiguration().getAcquisitionParameters();
	}

	private void load() throws AcquisitionControllerException {
		controller.loadAcquisitionConfiguration(getSelected().getResource());
	}

	private void delete() throws AcquisitionControllerException {
		boolean confirmed = UIHelper.showConfirm("Are you sure you want to delete this acquisition?");

		if (confirmed) {
			controller.deleteAcquisitionConfiguration(getSelected().getLocation());
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
				String message = String.format("Error performing '%s' operation", name);
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
