/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.composites.detectors.internal;

import org.dawnsci.common.richbeans.components.FieldBeanComposite;
import org.dawnsci.common.richbeans.components.FieldComposite;
import org.dawnsci.common.richbeans.components.selector.BeanSelectionEvent;
import org.dawnsci.common.richbeans.components.selector.BeanSelectionListener;
import org.dawnsci.common.richbeans.components.selector.GridListEditor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.swtdesigner.SWTResourceManager;

import uk.ac.gda.beans.exafs.IDetectorElement;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.exafs.ui.detector.vortex.VortexParametersUIEditor;

public class FluoDetectorRegionsComposite extends FieldBeanComposite {

	private GridListEditor detectorChannelList;
	private FluoDetectorRegionListComposite regionListComposite;
	private FluorescenceDetectorCompositeController controller;

	public FluoDetectorRegionsComposite(Composite composite, FluorescenceDetectorCompositeController controller) {
		super(composite, SWT.NONE);
		this.setLayout(new FillLayout());

		this.controller = controller;

		Group regionsGroup = new Group(this, SWT.NONE);
		regionsGroup.setText("Regions");
		GridLayoutFactory.fillDefaults().applyTo(regionsGroup);

		createApplyToAllPanel(regionsGroup);

		createSeparator(regionsGroup);

		createElementSelectionTable(regionsGroup);

		createSeparator(regionsGroup);

		createRegionsList(regionsGroup);

		detectorChannelList.setGridWidth(200);
		detectorChannelList.setEnabled(true);
		detectorChannelList.setAdditionalLabelProvider(new ColumnLabelProvider() {
			private final Color lightGray = SWTResourceManager.getColor(SWT.COLOR_GRAY);

			@Override
			public Color getForeground(Object element) {
				if (element instanceof IDetectorElement) {
					IDetectorElement detectorElement = (IDetectorElement) element;
					if (detectorElement.isExcluded())
						return lightGray;
				}
				return null;
			}

			@Override
			public String getText(Object element) {
				return null;
			}
		});

		createBindings();
	}

	private void createSeparator(Group regionsGroup) {
		Label sep = new Label(regionsGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	}

	private void createRegionsList(Group regionsGroup) {
		regionListComposite = new FluoDetectorRegionListComposite(regionsGroup, SWT.BORDER, controller);
		GridDataFactory.fillDefaults().applyTo(regionListComposite);
		detectorChannelList.setEditorUI(regionListComposite);
	}

	private void createElementSelectionTable(Group regionsGroup) {
		// TODO add import button

		int channelListSize = controller.getDetector().getNumberOfChannels();

		double channelListSizeSquareRoot = Math.sqrt(channelListSize);
		// Squared table of Detector Elements
		if (Double.compare(channelListSizeSquareRoot, (int) channelListSizeSquareRoot) == 0) {
			this.detectorChannelList = new GridListEditor(regionsGroup, SWT.NONE, (int) channelListSizeSquareRoot,
					(int) channelListSizeSquareRoot);
			// Table with two rows in the case of even number of detectors
		} else if ((channelListSize % 2) == 0) {
			this.detectorChannelList = new GridListEditor(regionsGroup, SWT.NONE, channelListSize / 2, 2);
		} else {
			throw new NullPointerException("Grid with the list of detectors cannot be created");
		}

		GridDataFactory.fillDefaults().applyTo(detectorChannelList);
		detectorChannelList.setEditorClass(DetectorElement.class); // TODO do we need this??

	}

	public int getSelectedDetectorChannel() {
		return detectorChannelList.getSelectedIndex();
	}

	private void createBindings() {

		regionListComposite.addBeanSelectionListener(new BeanSelectionListener() {
			private int lastSelectedElementIndex = 0;

			@Override
			public void selectionChanged(BeanSelectionEvent evt) {

				if (getSelectedDetectorChannel() == lastSelectedElementIndex) {
					int selectedRegionIndex = evt.getSelectionIndex();
					regionListComposite.getRegionList().setSelectedIndex(selectedRegionIndex);
				}

				lastSelectedElementIndex = getSelectedDetectorChannel();
				// updateROIAfterElementCompositeChange();
			}
		});

		// changes in the selection of the element ListEditor
		detectorChannelList.addBeanSelectionListener(new BeanSelectionListener() {
			@Override
			public void selectionChanged(BeanSelectionEvent evt) {
				controller.replot();
				regionListComposite.getRegionList().setSelectedIndex(0);
				// TODO and change the rois visible in the regionList
				// plot(evt.getSelectionIndex(), false);
				// if (bean instanceof XspressParameters) {
				// XspressParameters xspress = (XspressParameters) bean;
				// getDetectorElementComposite().getRegionList().setSelectedIndex(xspress.getSelectedRegionNumber());
				// } else if (bean instanceof VortexParameters) {
				// VortexParameters vortex = (VortexParameters) bean;
				// getDetectorElementComposite().getRegionList().setSelectedIndex(vortex.getSelectedRegionNumber());
				// }
			}
		});

		// // TODO where should these two be?
		//
		// // change the behaviour of the plot based on the preference
		// ExafsActivator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
		// @Override
		// public void propertyChange(PropertyChangeEvent event) {
		// if (event.getProperty().compareTo(ExafsPreferenceConstants.DETECTOR_OVERLAY_ENABLED) == 0) {
		// setRegionEditableFromPreference();
		// }
		// }
		// });
		//
		// // setup the default dragging behaviour
		// setRegionEditableFromPreference();
	}

	private void createApplyToAllPanel(Group regionsGroup) {
		final Composite applyToAllPanel = new Composite(regionsGroup, SWT.NONE);
		applyToAllPanel.setLayout(new GridLayout(2, false));

		final Label applyToAllLabel = new Label(applyToAllPanel, SWT.NONE);
		applyToAllLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		applyToAllLabel.setText("Apply To All Elements ");

		final Button applyToAllButton = new Button(applyToAllPanel, SWT.NONE);
		final GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gridData.widthHint = 60;
		gridData.minimumWidth = 60;
		applyToAllButton.setLayoutData(gridData);
		applyToAllButton.setImage(SWTResourceManager.getImage(VortexParametersUIEditor.class, "/icons/camera_go.png"));
		applyToAllButton.setToolTipText("Apply current detector regions of interest to all other detector elements.");
		final SelectionAdapter applyToAllListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO applyToAll(true);
			}
		};
		applyToAllButton.addSelectionListener(applyToAllListener);
	}

	public FieldComposite getDetectorList() {
		return detectorChannelList;
	}
}
