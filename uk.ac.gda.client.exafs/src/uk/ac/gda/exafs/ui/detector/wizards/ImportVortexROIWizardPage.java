/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.detector.wizards;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.reflection.BeansFactory;
import org.eclipse.richbeans.widgets.selector.GridListEditor;
import org.eclipse.richbeans.widgets.selector.ListEditor;
import org.eclipse.richbeans.widgets.selector.ListEditorUI;
import org.eclipse.richbeans.widgets.selector.VerticalListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.exafs.ui.detector.DetectorListComposite;
import uk.ac.gda.exafs.ui.detector.DetectorROIComposite;
import uk.ac.gda.exafs.ui.detector.IDetectorROICompositeFactory;
import uk.ac.gda.exafs.ui.detector.vortex.VortexParametersUIHelper;
import uk.ac.gda.util.beans.xml.XMLHelpers;

@Deprecated
public class ImportVortexROIWizardPage extends ImportROIWizardPage {
	private static final Logger logger = LoggerFactory.getLogger(ImportVortexROIWizardPage.class);

	private int elementListSize;
	private List<DetectorROI> currentBeans;
	private double maximum;
	protected DetectorComposite roisToImportComposite;
	private DetectorListComposite detectorListComposite;
	private VerticalListEditor importFileRegionList;
	private boolean validSource;

	private VortexParameters vortexParameters;


	// Region list stores a list of ROIs, potentially unsafe conversion, if it fails
	// there will be runtime class cast exceptions
	@SuppressWarnings("unchecked")
	public ImportVortexROIWizardPage(int elementListSize, List<? extends DetectorROI> currentBeans, double maximum) {
		this.elementListSize = elementListSize;
		this.currentBeans = (List<DetectorROI>)currentBeans;
		this.maximum = maximum;
	}


	@Override
	protected void updateEnables() {
		if (currentSourceValid()) {
			setErrorMessage(null);
			detectorListComposite.setEnabled(true);
			setEnables(detectorListComposite.getDetectorElementComposite(), false);
			setEnables(detectorListComposite.getDetectorList(), true);
			importFileRegionList.setEnabled(true);
			Composite composite = (Composite)importFileRegionList.getEditorUI();
			setEnables(composite, false);
		} else {
			setErrorMessage("Please select a valid Vortex Parameters file for this beamline.");
			setEnables(detectorListComposite, false);
		}
		updateAddButtonEnables();

	}

	private void updateAddButtonEnables() {
		if (!currentSourceValid()) {
			// error set by invalid source
			addButton.setEnabled(false);
			addToAllButton.setEnabled(false);
		} else if (getBeansToAdd().size() >= VortexParametersUIHelper.INSTANCE.getMaximumRegions()) {
			setMessage("Maximum number of Regions reached. Please select Finish, or Delete existing items to copy more");
			addButton.setEnabled(false);
			addToAllButton.setEnabled(false);
		} else {
			setMessage(null);
			addButton.setEnabled(true);
			addToAllButton.setEnabled(true);
		}
	}


	@Override
	protected void createSourceControls(Composite parent) {

		IDetectorROICompositeFactory factory = VortexParametersUIHelper.INSTANCE.getDetectorROICompositeFactory();
		detectorListComposite = new DetectorListComposite(parent,
				DetectorElement.class, elementListSize, DetectorROI.class, factory,false);
		GridListEditor detectorListGridEditor = detectorListComposite.getDetectorList();
		VortexParametersUIHelper.INSTANCE.setDetectorListGridOrder(detectorListGridEditor);

		detectorListComposite.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				scrolledComp.setMinSize(mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		});

		importFileRegionList = detectorListComposite.getDetectorElementComposite().getRegionList();
		importFileRegionList.setListEditorUI(new ListEditorUI() {

			@Override
			public void notifySelected(ListEditor listEditor) {
				//nothing todo
			}

			@Override
			public boolean isReorderAllowed(ListEditor listEditor) {
				return false;
			}

			@Override
			public boolean isDeleteAllowed(ListEditor listEditor) {
				return false;
			}

			@Override
			public boolean isAddAllowed(ListEditor listEditor) {
				return false;
			}
		});

		detectorListComposite.getDetectorElementComposite().setEndMaximum((int)maximum);
		detectorListComposite.getDetectorElementComposite().setWindowsEditable(false);
		GridUtils.setVisibleAndLayout(importFileRegionList, true);
	}


	public class DetectorComposite extends Composite
	{
		private VerticalListEditor regionList;

		public DetectorComposite(Composite parent, int style, double maximum) {
			super(parent, style);
			GridLayoutFactory.fillDefaults().applyTo(this);
			regionList = new VerticalListEditor(this, SWT.BORDER);
			regionList.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			regionList.setEditorClass(DetectorROI.class);

			final DetectorROIComposite detectorROIComposite = VortexParametersUIHelper.INSTANCE.getDetectorROICompositeFactory().createDetectorROIComposite(regionList, SWT.NONE);
			detectorROIComposite.getFieldWidgetsForDetectorElementsComposite().getRoiEnd().setMaximum(maximum);
			regionList.setEditorUI(detectorROIComposite);

			detectorROIComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			regionList.setTemplateName("ROI");
			regionList.setNameField("roiName");
			regionList.setListHeight(250);
			regionList.setMinItems(VortexParametersUIHelper.INSTANCE.getMinimumRegions());
			regionList.setMaxItems(VortexParametersUIHelper.INSTANCE.getMaximumRegions());
			regionList.addValueListener(new ValueAdapter("Vortex Region List Listener") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					updateAddButtonEnables();
				}
			});
			regionList.setListEditorUI(new ListEditorUI() {

				@Override
				public void notifySelected(ListEditor listEditor) {
			   		//nothing to do
				}

				@Override
				public boolean isReorderAllowed(ListEditor listEditor) {
					return true;
				}

				@Override
				public boolean isDeleteAllowed(ListEditor listEditor) {
					return true;
				}

				@Override
				public boolean isAddAllowed(ListEditor listEditor) {
					// add is performed by using the >>> button
					return false;
				}
			});

		}


		public VerticalListEditor getRegionList() {
			return regionList;
		}


	}

	@Override
	protected void createDestinationControls(Composite parent) {
		roisToImportComposite = new DetectorComposite(parent, SWT.NONE, maximum);
		GridDataFactory.swtDefaults().applyTo(roisToImportComposite);

		// create a temporary DetectorElement as a container for the beans
		DetectorElement element = new DetectorElement();
		element.setRegionList(currentBeans);

		try {
			beanToUI(roisToImportComposite, element);
		} catch (Exception e1) {
			// this is very unexpected as the currentBeans was just cloned from the current editor
			logger.error("Unexpected exception creating destination contents", e1);
		}
	}

	@Override
	protected void newSourceSelected(IPath path) {
		validSource = false;
		try {
			vortexParameters = (VortexParameters)XMLHelpers.readBean(path.toFile(), VortexParameters.class);
			if (vortexParameters.getDetectorList().size() == elementListSize) {
				beanToUI(detectorListComposite, vortexParameters);
				validSource = true;
			}
		} catch (Exception e1) {
			logger.error("Error ",e1);
		}
	}

	@Override
	protected boolean currentSourceValid() {
		return validSource;
	}

	@Override
	protected void performAdd() {
		Object bean = detectorListComposite.getDetectorElementComposite().getRegionList().getBean();
		try {
			Object clonedBean = BeansFactory.deepClone(bean);
			roisToImportComposite.getRegionList().addBean(clonedBean, -1);
		} catch (Exception e1) {
			logger.error("Failed to clone bean. Check bean type is fully cloneable", e1);
		}
	}
	@Override
	protected void performAddAll() {
		Object bean = detectorListComposite.getDetectorElementComposite().getRegionList().getBean();
		List <DetectorROI> regionToCopy;
		if(bean instanceof DetectorROI)
		{
			List<DetectorElement> detectors = vortexParameters.getDetectorList();
			regionToCopy = new ArrayList<DetectorROI>(detectors.size());
			for(int i =0 ; i < detectors.size() ; i++){
				boolean regionFound = false;
				List <DetectorROI>elementROIList = detectors.get(i).getRegionList();
				for ( DetectorROI roi : elementROIList){
					if(roi.getRoiName().equals(((DetectorROI)bean).getRoiName()))
					{
						regionFound = true;
						regionToCopy.add(roi);
						break;
					}
				}
				if(!regionFound)
				{
					logger.error("Unable to find the common region all elements, cannot copy");
					return;
				}
			}
//			System.out.println("the beans found are " + this.currentDetectorList.getValue());
			try {
			final List<?> elements = (List<?>) this.currentDetectorList.getValue();
				final List<?> regionClone = BeansFactory.cloneBeans(regionToCopy);
			int index = -1;

				for (Object element : elements) {
					++index;

					if(index == currentDetectorList.getSelectedIndex())
					{
						roisToImportComposite.getRegionList().addBean(regionClone.get(index), -1);
					}
					else
					{
						final Method addRegion = element.getClass().getMethod("addRegion", DetectorROI.class);
						addRegion.invoke(element, regionClone.get(index));
					}

				}
			} catch (Exception e1) {
				logger.error("Error apply current detector regions to all detectors.", e1);
			}

		}

	}

	@Override
	public List<? extends DetectorROI> getBeansToAdd() {
		// the region list is a wrapper for a List of DetectorROIs, therefore safe SuppressWarning
		@SuppressWarnings("unchecked")
		List<? extends DetectorROI> value = (List<? extends DetectorROI>)roisToImportComposite.getRegionList().getValue();
		return value;
	}


}
