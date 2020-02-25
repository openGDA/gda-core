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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IWorkbenchActionConstants;

import gda.rcp.views.Browser;
import gda.rcp.views.TreeViewerBuilder;
import uk.ac.diamond.daq.mapping.ui.experiment.file.SavedScanMetaData;
import uk.ac.diamond.daq.mapping.ui.experiment.saver.ScanSaver;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;
import uk.ac.gda.client.composites.AcquisitionsBrowserCompositeFactory;

/**
 * Generates a {@link Browser} for the tomography configuration files, suitable for an {@link AcquisitionsBrowserCompositeFactory}
 *
 * @author Maurizio Nagni
 */
public class MapBrowser extends Browser<SavedScanMetaData> {

	private static final int NAME_WIDTH = 250;
	private static final int SHAPE_WIDTH = 70;
	private static final int DETAIL_WIDTH = 200;

	private final ScanSaver scanSaver;

	public MapBrowser(ScanSaver scanSaver) {
		super(AcquisitionConfigurationResourceType.MAP);
		this.scanSaver = scanSaver;
	}

	@Override
	public TreeViewerBuilder<AcquisitionConfigurationResource<SavedScanMetaData>> getTreeViewBuilder() {
		return new TreeViewerBuilder<AcquisitionConfigurationResource<SavedScanMetaData>>() {
			@Override
			public AcquisitionConfigurationResource<SavedScanMetaData>[] getInputElements(boolean reload) {
				final List<AcquisitionConfigurationResource<SavedScanMetaData>> ret = new ArrayList<>();

				getAcquisitionConfigurationResources(reload).stream().forEachOrdered(acq -> {
					ret.add(new AcquisitionConfigurationResource<SavedScanMetaData>(acq.getLocation(),
							new SavedScanMetaData(getURLLastPathSegment(acq))));
				});
				return ret.toArray(new AcquisitionConfigurationResource[0]);
			}
		};
	}

	private String getURLLastPath(Object element) {
		return Browser.getURLLastPathSegment((AcquisitionConfigurationResource<SavedScanMetaData>) element);
	}

	@Override
	public void addColumns(TreeViewerBuilder<AcquisitionConfigurationResource<SavedScanMetaData>> builder) {
		builder.addColumn("Name", NAME_WIDTH, new NameProvider());
		builder.addColumn("Shape", SHAPE_WIDTH, new ShapeProvider());
		builder.addColumn("Detail", DETAIL_WIDTH, new DetailsProvider());
	}

	@Override
	public ITreeContentProvider getContentProvider() {
		return new ContentProvider();
	}

	@Override
	public IDoubleClickListener getDoubleClickListener() {
		return event -> {
			SavedScanMetaData scan = extractSavedScanMetaData(((TreeSelection) event.getSelection()).getFirstElement());
			scanSaver.load(scan);
		};
	}

	@Override
	public ISelectionChangedListener getISelectionChangedListener(MenuManager contextMenu) {
		if (contextMenu != null) {
			contextMenu.addMenuListener(manager -> {
				manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
				manager.add(new LoadAcquisitionConfigurationResource(this.getSelected().getResource(), scanSaver));
				manager.add(new DeleteAcquisitionConfigurationResource(this.getSelected().getResource(), scanSaver));
			});
		}
		return event -> {
			this.setSelected((AcquisitionConfigurationResource<SavedScanMetaData>) event.getStructuredSelection()
					.getFirstElement());
		};
	}

	static SavedScanMetaData extractSavedScanMetaData(Object element) {
		return ((AcquisitionConfigurationResource<SavedScanMetaData>) element).getResource();
	}
}
