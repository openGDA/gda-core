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
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.api.acquisition.AcquisitionControllerException;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;
import uk.ac.gda.client.composites.AcquisitionsBrowserCompositeFactory;
import uk.ac.gda.tomography.base.TomographyParameterAcquisition;
import uk.ac.gda.tomography.base.TomographyParameters;
import uk.ac.gda.tomography.ui.controller.TomographyPerspectiveController;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Generates a {@link Browser} for the tomography configuration files, suitable for an {@link AcquisitionsBrowserCompositeFactory}
 *
 * @author Maurizio Nagni
 */
public class TomoBrowser extends Browser<TomographyParameterAcquisition> {

	private static final int NAME_WIDTH = 250;
	private static final int TYPE_WIDTH = 70;
	private static final int RANGE_WIDTH = 200;

	public TomoBrowser() {
		super(AcquisitionConfigurationResourceType.TOMO);
	}

	@Override
	public TreeViewerBuilder<AcquisitionConfigurationResource<TomographyParameterAcquisition>> getTreeViewBuilder() {
		return new TreeViewerBuilder<AcquisitionConfigurationResource<TomographyParameterAcquisition>>() {
			@Override
			public AcquisitionConfigurationResource<TomographyParameterAcquisition>[] getInputElements(boolean reload) {
				return getAcquisitionConfigurationResources(reload).stream().map(resource -> {
					try {
						return getTomographyAcquisitionController().parseAcquisitionConfiguration(resource.getLocation());
					} catch (AcquisitionControllerException e) {
						return null;
					}
				}).filter(Objects::nonNull). collect(Collectors.toList()).toArray(new AcquisitionConfigurationResource[0]);
			}
		};
	}

	@Override
	public void addColumns(TreeViewerBuilder<AcquisitionConfigurationResource<TomographyParameterAcquisition>> builder) {
		builder.addColumn("Name", NAME_WIDTH, new NameProvider());
		builder.addColumn("Range", RANGE_WIDTH, new RangeProvider());
		builder.addColumn("Type", TYPE_WIDTH, new ScanTypeProvider());
	}

	@Override
	public ITreeContentProvider getContentProvider() {
		return new ContentProvider();
	}

	@Override
	public IDoubleClickListener getDoubleClickListener() {
		return event -> {
			AcquisitionConfigurationResource<TomographyParameterAcquisition> resource = (AcquisitionConfigurationResource<TomographyParameterAcquisition>) ((TreeSelection) event
					.getSelection()).getFirstElement();
			try {
				getTomographyAcquisitionController().loadAcquisitionConfiguration(resource.getLocation());
			} catch (AcquisitionControllerException e) {

			}
		};
	}

	@Override
	public ISelectionChangedListener getISelectionChangedListener(MenuManager contextMenu) {
		if (contextMenu != null) {
			contextMenu.addMenuListener(manager -> {
				manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
				manager.add(new LoadAcquisitionConfigurationResource(this.getSelected(), getTomographyAcquisitionController()));
				manager.add(new DeletedAcquisitionConfigurationResource(this.getSelected(), getTomographyAcquisitionController()));
			});
		}
		return event -> {
			this.setSelected((AcquisitionConfigurationResource<TomographyParameterAcquisition>) event.getStructuredSelection().getFirstElement());
		};
	}

	static final TomographyParameters getTomographyParameters(Object element) {
		return TomographyParameterAcquisition.class.cast(AcquisitionConfigurationResource.class.cast(element).getResource()).getAcquisitionConfiguration()
				.getAcquisitionParameters();
	}

	private AcquisitionController<TomographyParameterAcquisition> getTomographyAcquisitionController() {
		return SpringApplicationContextProxy.getBean(TomographyPerspectiveController.class).getTomographyAcquisitionController();
	}
}
