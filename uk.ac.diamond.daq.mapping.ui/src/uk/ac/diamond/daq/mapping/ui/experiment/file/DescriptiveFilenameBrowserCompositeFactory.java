/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment.file;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.mapping.ui.experiment.saver.ScanSaver;
import uk.ac.gda.ui.tool.ClientSWTElements;

/**
 * Composite for use with {@link DescriptiveFilenameFactory}. Will display filenames encoded using this in 3 columns
 * showing the name, shape and shape/path parameters in a sortable searchable tree view. Filenames without the
 * descriptor section will just show in the name column. Double clicking an entry loads the file in question.
 *
 * @since GDA 9.13
 */
public class DescriptiveFilenameBrowserCompositeFactory implements CompositeFactory {
	private static final int NAME_WIDTH = 250;
	private static final int SHAPE_WIDTH = 70;
	private static final int DETAIL_WIDTH = 200;

	private ScanSaver scanSaver;
	private SavedScanMetaData selectedScan;

	public DescriptiveFilenameBrowserCompositeFactory(ScanSaver scanSaver) {
		super();
		this.scanSaver = scanSaver;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		Composite container = ClientSWTElements.createComposite(parent, style);

		TreeViewerBuilder<SavedScanMetaData> builder = new TreeViewerBuilder<SavedScanMetaData>() {

			@Override
			public SavedScanMetaData[] getInputElements() {
				return scanSaver.getObservableList().stream().toArray(SavedScanMetaData[]::new);
			}
		};
		builder.addColumn("Name", NAME_WIDTH, new SavedScanNameLabelProvider());
		builder.addColumn("Shape", SHAPE_WIDTH, new SavedScanShapeLabelProvider());
		builder.addColumn("Detail", DETAIL_WIDTH, new SavedScanDetailsLabelProvider());
		builder.addContentProvider(new SavedScansContentProvider());
		builder.addDoubleClickListener(getDoubleClickListener(scanSaver));
		builder.addObservableList(scanSaver.getObservableList());

		MenuManager contextMenu = new MenuManager("#ViewerMenu"); //$NON-NLS-1$
		builder.addMenuManager(contextMenu);
		builder.addSelectionListener(getISelectionChangedListener(contextMenu));
		builder.build(container);

		Button saveButton = new Button(parent, SWT.PUSH);
		saveButton.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(
				"uk.ac.diamond.daq.mapping.ui", "icons/save.png").createImage());
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				scanSaver.save();
			}
		});

		return container;
	}

	private SavedScanMetaData getSelectedScan() {
		return selectedScan;
	}

	private void setSelectedScan(SavedScanMetaData selectedScan) {
		this.selectedScan = selectedScan;
	}

	/**
	 * On selecting a row, displays a ContexMenu
	 *
	 * @param contextMenu
	 * @return
	 */
	private ISelectionChangedListener getISelectionChangedListener(MenuManager contextMenu) {
		if (contextMenu != null) {
			contextMenu.addMenuListener(manager -> {
				manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
				manager.add(new LoadScan(getSelectedScan()));
				manager.add(new DeleteScan(getSelectedScan()));
			});
		}
		return event -> {
			setSelectedScan((SavedScanMetaData) event.getStructuredSelection().getFirstElement());
		};
	}

	/**
	 * Loads a row
	 *
	 * @param scanSaver
	 * @return
	 */
	private IDoubleClickListener getDoubleClickListener(ScanSaver scanSaver) {
		return event -> {
			SavedScanMetaData scan = (SavedScanMetaData) ((TreeSelection) event.getSelection()).getFirstElement();
			scanSaver.load(scan);
		};
	}

	class LoadScan extends Action {

		private final SavedScanMetaData selectedScan;

		public LoadScan(SavedScanMetaData selectedScan) {
			super("Load Scan");
			this.selectedScan = selectedScan;
		}

		@Override
		public void run() {
			scanSaver.load(selectedScan);
		}
	}

	class DeleteScan extends Action {

		private final SavedScanMetaData selectedScan;

		public DeleteScan(SavedScanMetaData selectedScan) {
			super("Deleted Scan");
			this.selectedScan = selectedScan;
		}

		@Override
		public void run() {
			scanSaver.delete(selectedScan);
		}
	}
}
