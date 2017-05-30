/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos.ui.editors;

import static uk.ac.diamond.daq.devices.specs.phoibos.ui.SpecsUiConstants.OPEN_SEQUENCE;
import static uk.ac.diamond.daq.devices.specs.phoibos.ui.SpecsUiConstants.OPEN_SEQUENCE_EVENT;
import static uk.ac.diamond.daq.devices.specs.phoibos.ui.SpecsUiConstants.REGION_SELECTED_EVENT;
import static uk.ac.diamond.daq.devices.specs.phoibos.ui.SpecsUiConstants.SAVED_SEQUENCE_HASH;

import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosRegion;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequence;
import uk.ac.diamond.daq.devices.specs.phoibos.ui.SpecsUiConstants;

public class SpecsSequenceEditor {

	private static final Logger logger = LoggerFactory.getLogger(SpecsSequenceEditor.class);

	@Inject
	private MPart part;

	@Inject
	private IEventBroker eventBroker;

	@Inject
	private EPartService partService;

	private final Bundle bundle = FrameworkUtil.getBundle(this.getClass());

	private Text filePathText;
	private TableViewer sequenceTableViewer;
	private Map<String, Object> transientData;

	// static fields to hold the images
	private final Image checked = getImageDescriptor("icons/sequence_editor/checked.gif");
	private final Image unchecked = getImageDescriptor("icons/sequence_editor/unchecked.gif");

	// When sequence fire property change events cause the table to refresh
	private final PropertyChangeListener sequenceListener = evt -> {
		sequenceTableViewer.refresh();
		updateDirty();
	};

	private Image getImageDescriptor(String filepath) {
		URL url = FileLocator.find(bundle, new Path(filepath), null);
		return ImageDescriptor.createFromURL(url).createImage();
	}

	@PostConstruct
	void createView(Composite parent) {
		transientData = part.getTransientData();

		GridLayoutFactory.swtDefaults().numColumns(1).spacing(5, 0).applyTo(parent);
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		// Make the status bar composite which shows the open sequence
		Composite filePathComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(filePathComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).spacing(5, 0).applyTo(filePathComposite);
		Label label = new Label(filePathComposite, SWT.NONE);
		label.setText("Open sequence: ");
		filePathText = new Text(filePathComposite, SWT.READ_ONLY);
		filePathText.setText("None");
		filePathComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(filePathText);

		sequenceTableViewer = new TableViewer(parent,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
				.applyTo(sequenceTableViewer.getControl());

		final Table table = sequenceTableViewer.getTable();

		createColumns(sequenceTableViewer);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		sequenceTableViewer.setContentProvider(new ArrayContentProvider());
		sequenceTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				final ISelection selection = event.getSelection();
				logger.trace("Region selected: {}", selection);
				final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				final SpecsPhoibosRegion region = (SpecsPhoibosRegion) structuredSelection.getFirstElement();
				transientData.put(SpecsUiConstants.SELECTED_REGION, region);
				// Send the event the region editor will display the region
				eventBroker.post(REGION_SELECTED_EVENT, region);
				// Force canExecute methods to be reevaluated
				eventBroker.send(UIEvents.REQUEST_ENABLEMENT_UPDATE_TOPIC, UIEvents.ALL_ELEMENT_ID);
			}
		});

		// On double click on region open the region editor and display it
		sequenceTableViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				logger.trace("Region double clicked: {}", selection);

				// Open the region editor and give focus
				partService.showPart("uk.ac.diamond.daq.devices.specs.phoibos.ui.part.regioneditor",
						PartState.ACTIVATE);

				// Send the event to show the region.
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				SpecsPhoibosRegion reg = (SpecsPhoibosRegion) structuredSelection.getFirstElement();
				// Send the event the region editor will display the region
				eventBroker.post(REGION_SELECTED_EVENT, reg);
			}
		});

	}

	private void setSequence(SpecsPhoibosSequence sequence, String path) {

		// Update the file path label
		if (path == null) { // null path indicates unsaved sequence
			filePathText.setText("Unsaved sequence");
		} else {
			filePathText.setText(path);
		}

		transientData.put(OPEN_SEQUENCE, sequence);
		transientData.put(SAVED_SEQUENCE_HASH, sequence.hashCode());

		sequenceTableViewer.setInput(sequence.getRegions());

		// Add the sequence listener
		sequence.addPropertyChangeListener(sequenceListener);

		// Force canExecute methods to be reevaluated
		eventBroker.send(UIEvents.REQUEST_ENABLEMENT_UPDATE_TOPIC, UIEvents.ALL_ELEMENT_ID);
	}

	private void createColumns(final TableViewer tableViewer) {
		// Enabled
		TableViewerColumn enabledCol = createTableViewerColumn(tableViewer, "Enabled", 18);
		enabledCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return null;
			}

			@Override
			public Image getImage(Object element) {
				if (((SpecsPhoibosRegion) element).isEnabled()) {
					return checked;
				} else {
					return unchecked;
				}
			}

		});
		enabledCol.setEditingSupport(new EnabledEditingSupport(tableViewer));

		// Region Name
		TableViewerColumn nameCol = createTableViewerColumn(tableViewer, "Name", 150);
		nameCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((SpecsPhoibosRegion) element).getName();
			}
		});

		// Acquisition Mode
		TableViewerColumn acquisitionModeCol = createTableViewerColumn(tableViewer, "Acquisition Mode", 180);
		acquisitionModeCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((SpecsPhoibosRegion) element).getAcquisitionMode();
			}
		});

		// Lens Mode
		TableViewerColumn lensModeCol = createTableViewerColumn(tableViewer, "Lens Mode", 180);
		lensModeCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((SpecsPhoibosRegion) element).getLensMode();
			}
		});

		// Pass Energy
		TableViewerColumn passEnergyCol = createTableViewerColumn(tableViewer, "Pass Energy (eV)", 50);
		passEnergyCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return String.valueOf(((SpecsPhoibosRegion) element).getPassEnergy());
			}
		});

		// Energy Mode
		TableViewerColumn energyModeCol = createTableViewerColumn(tableViewer, "Energy Mode", 65);
		energyModeCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (((SpecsPhoibosRegion) element).isBindingEnergy()) {
					return "Binding";
				}
				return "Kinetic";
			}
		});

		// Start Energy
		TableViewerColumn startEnergyCol = createTableViewerColumn(tableViewer, "Start Energy (eV)", 75);
		startEnergyCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return String.valueOf(((SpecsPhoibosRegion) element).getStartEnergy());
			}
		});

		// End Energy
		TableViewerColumn endEnergyCol = createTableViewerColumn(tableViewer, "End Energy (eV)", 75);
		endEnergyCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return String.valueOf(((SpecsPhoibosRegion) element).getEndEnergy());
			}
		});

		// Step Energy
		TableViewerColumn stepEnergyCol = createTableViewerColumn(tableViewer, "Step Energy (eV)", 75);
		stepEnergyCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return String.valueOf(((SpecsPhoibosRegion) element).getStepEnergy());
			}
		});

		// Exposure Time
		TableViewerColumn exposureTimeCol = createTableViewerColumn(tableViewer, "Exposure Time (sec)", 50);
		exposureTimeCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return String.valueOf(((SpecsPhoibosRegion) element).getExposureTime());
			}
		});

		// Iterations
		TableViewerColumn iterationsCol = createTableViewerColumn(tableViewer, "Iterations", 50);
		iterationsCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return String.valueOf(((SpecsPhoibosRegion) element).getIterations());
			}
		});

		// PSU Mode
		TableViewerColumn psuModeCol = createTableViewerColumn(tableViewer, "PSU Mode", 80);
		psuModeCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((SpecsPhoibosRegion) element).getPsuMode();
			}
		});

	}

	private TableViewerColumn createTableViewerColumn(final TableViewer viewer, final String title, final int width) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(width);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	@Focus
	public void onFocus() {
		sequenceTableViewer.getTable().setFocus();
	}

	/**
	 * Sets if the open sequence differs from the saved one. And therefore if the part is dirty and can be saved.
	 *
	 * It works by comparing the hash of the current sequence with the hash of the saved sequence.
	 */
	private void updateDirty() {
		int sequenceHash = (transientData.get(OPEN_SEQUENCE)).hashCode();
		int savedFileSequenceHash = (int) transientData.get(SAVED_SEQUENCE_HASH);
		part.setDirty(sequenceHash != savedFileSequenceHash);
	}

	@Optional
	@Inject
	private void openSequence(@UIEventTopic(OPEN_SEQUENCE_EVENT) SpecsPhoibosSequence sequence) {

		String path = part.getPersistedState().get(SpecsUiConstants.OPEN_SEQUENCE_FILE_PATH);
		setSequence(sequence, path);
	}

	private class EnabledEditingSupport extends EditingSupport {

		private final TableViewer viewer;

		public EnabledEditingSupport(TableViewer viewer) {
			super(viewer);
			this.viewer = viewer;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new CheckboxCellEditor(null, SWT.CHECK | SWT.READ_ONLY);

		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			return ((SpecsPhoibosRegion) element).isEnabled();
		}

		@Override
		protected void setValue(Object element, Object value) {
			((SpecsPhoibosRegion) element).setEnabled((boolean) value);
			viewer.update(element, null);
		}
	}
}
