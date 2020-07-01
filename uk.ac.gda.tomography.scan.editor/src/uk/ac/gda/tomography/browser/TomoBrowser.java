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

package uk.ac.gda.tomography.browser;

import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IWorkbenchActionConstants;

import gda.rcp.views.Browser;
import gda.rcp.views.TreeViewerBuilder;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.api.acquisition.AcquisitionControllerException;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;
import uk.ac.gda.client.composites.AcquisitionsBrowserCompositeFactory;

/**
 * Generates a {@link Browser} for the tomography configuration files, suitable for an {@link AcquisitionsBrowserCompositeFactory}
 *
 * @author Maurizio Nagni
 */
public class TomoBrowser extends Browser<ScanningAcquisition> {

	private static final int NAME_WIDTH = 250;
	private static final int TYPE_WIDTH = 70;
	private static final int RANGE_WIDTH = 200;

	private final AcquisitionController<ScanningAcquisition> controller;

	public TomoBrowser(AcquisitionController<ScanningAcquisition> controller) {
		super(AcquisitionConfigurationResourceType.TOMO);
		this.controller = controller;
	}

	@Override
	public TreeViewerBuilder<AcquisitionConfigurationResource<ScanningAcquisition>> getTreeViewBuilder() {
		return new TreeViewerBuilder<AcquisitionConfigurationResource<ScanningAcquisition>>() {
			@Override
			public AcquisitionConfigurationResource<ScanningAcquisition>[] getInputElements(boolean reload) {
				return getAcquisitionConfigurationResources(reload).stream().map(resource -> {
					try {
						return getController().parseAcquisitionConfiguration(resource.getLocation());
					} catch (AcquisitionControllerException e) {
						return null;
					}
				}).filter(Objects::nonNull). collect(Collectors.toList()).toArray(new AcquisitionConfigurationResource[0]);
			}
		};
	}

	@Override
	public void addColumns(TreeViewerBuilder<AcquisitionConfigurationResource<ScanningAcquisition>> builder) {
		builder.addColumn("Name", NAME_WIDTH, new NameProvider());
		builder.addColumn("Range", RANGE_WIDTH, new RangeProvider());
		builder.addColumn("Type", TYPE_WIDTH, new MutatorsProvider());
	}

	@Override
	public ITreeContentProvider getContentProvider() {
		return new ContentProvider();
	}

	@Override
	public IDoubleClickListener getDoubleClickListener() {
		return event -> {
			AcquisitionConfigurationResource<ScanningAcquisition> resource = (AcquisitionConfigurationResource<ScanningAcquisition>) ((TreeSelection) event
					.getSelection()).getFirstElement();
			try {
				getController().loadAcquisitionConfiguration(resource.getLocation());
			} catch (AcquisitionControllerException e) {

			}
		};
	}

	@Override
	public ISelectionChangedListener getISelectionChangedListener(MenuManager contextMenu) {
		if (contextMenu != null) {
			contextMenu.addMenuListener(manager -> {
				manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
				manager.add(new LoadAcquisitionConfigurationResource(this.getSelected(), getController()));
				manager.add(new DeletedAcquisitionConfigurationResource(this.getSelected(), getController()));
			});
		}
		return event -> {
			this.setSelected((AcquisitionConfigurationResource<ScanningAcquisition>) event.getStructuredSelection().getFirstElement());
		};
	}

	static final ScanningParameters getAcquisitionParameters(Object element) {
		return ScanningAcquisition.class.cast(AcquisitionConfigurationResource.class.cast(element).getResource()).getAcquisitionConfiguration()
				.getAcquisitionParameters();
	}

	private AcquisitionController<ScanningAcquisition> getController() {
		return controller;
	}
}
