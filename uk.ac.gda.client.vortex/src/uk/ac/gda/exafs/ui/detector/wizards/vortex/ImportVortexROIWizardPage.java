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

package uk.ac.gda.exafs.ui.detector.wizards.vortex;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.BeansFactory;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.beans.vortex.VortexROI;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.exafs.ui.detector.DetectorListComposite;
import uk.ac.gda.exafs.ui.detector.DetectorROIComposite;
import uk.ac.gda.exafs.ui.detector.vortex.NewVortexParametersUIHelper;
import uk.ac.gda.exafs.ui.detector.wizards.ImportROIWizardPage;
import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.richbeans.components.selector.GridListEditor;
import uk.ac.gda.richbeans.components.selector.ListEditor;
import uk.ac.gda.richbeans.components.selector.ListEditorUI;
import uk.ac.gda.richbeans.components.selector.VerticalListEditor;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class ImportVortexROIWizardPage extends ImportROIWizardPage {
	private static final Logger logger = LoggerFactory.getLogger(ImportVortexROIWizardPage.class);
	private int elementListSize;
	private List<VortexROI> currentBeans;
	private double maximum;
	protected DetectorComposite roisToImportComposite;
	private DetectorListComposite detectorListComposite;
	private VerticalListEditor importFileRegionList;
	private boolean validSource;
	private VortexParameters vortexParameters;
	
	public ImportVortexROIWizardPage(int elementListSize, List<? extends DetectorROI> currentBeans, double maximum) {
		this.elementListSize = elementListSize;
		this.currentBeans = (List<VortexROI>)currentBeans;
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
		} 
		else {
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
		} 
		else if (getBeansToAdd().size() >= NewVortexParametersUIHelper.INSTANCE.getMaximumRegions()) {
			setMessage("Maximum number of Regions reached. Please select Finish, or Delete existing items to copy more");
			addButton.setEnabled(false);
			addToAllButton.setEnabled(false);
		} 
		else {
			setMessage(null);
			addButton.setEnabled(true);
			addToAllButton.setEnabled(true);
		}
	}
	
	@Override
	protected void createSourceControls(Composite parent) {
		detectorListComposite = new DetectorListComposite(parent, DetectorElement.class, elementListSize, VortexROI.class, true);
		GridListEditor detectorListGridEditor = detectorListComposite.getDetectorList();
		NewVortexParametersUIHelper.INSTANCE.setDetectorListGridOrder(detectorListGridEditor);
		importFileRegionList = detectorListComposite.getDetectorElementComposite().getRegionList();
		importFileRegionList.setListEditorUI(new ListEditorUI() {
			
			@Override
			public void notifySelected(ListEditor listEditor) {
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

	public class DetectorComposite extends Composite{
		private VerticalListEditor regionList;

		public DetectorComposite(Composite parent, int style, double maximum) {
			super(parent, style);
			GridLayoutFactory.fillDefaults().applyTo(this);
			regionList = new VerticalListEditor(this, SWT.BORDER);
			regionList.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			regionList.setEditorClass(VortexROI.class);
			DetectorROIComposite detectorROIComposite = NewVortexParametersUIHelper.INSTANCE.getDetectorROICompositeFactory().createDetectorROIComposite(regionList, SWT.NONE);
			detectorROIComposite.getFieldWidgetsForDetectorElementsComposite().getRoiEnd().setMaximum(maximum);
			regionList.setEditorUI(detectorROIComposite);
			detectorROIComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			regionList.setTemplateName("ROI");
			regionList.setNameField("roiName");
			regionList.setListHeight(250);
			regionList.setMinItems(NewVortexParametersUIHelper.INSTANCE.getMinimumRegions());
			regionList.setMaxItems(NewVortexParametersUIHelper.INSTANCE.getMaximumRegions());
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
			BeanUI.switchState(element, roisToImportComposite, false);
			BeanUI.beanToUI(element, roisToImportComposite);
			BeanUI.switchState(element, roisToImportComposite, true);
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
				BeanUI.switchState(vortexParameters, detectorListComposite, false);
				BeanUI.beanToUI(vortexParameters, detectorListComposite);
				BeanUI.switchState(vortexParameters, detectorListComposite, true);
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
		List <VortexROI> regionToCopy;
		if(bean instanceof VortexROI){
			List<DetectorElement> detectors = vortexParameters.getDetectorList();
			regionToCopy = new ArrayList<VortexROI>(detectors.size());
			for(int i =0 ; i < detectors.size() ; i++){
				boolean regionFound = false;
				List <VortexROI>elementROIList = detectors.get(i).getRegionList();
				for ( VortexROI roi : elementROIList){
					if(roi.getRoiName().equals(((VortexROI)bean).getRoiName())){
						regionFound = true;
						regionToCopy.add(roi);
						break;
					}
				}
				if(!regionFound){
					logger.error("Unable to find the common region all elements, cannot copy");
					return;
				}
			}
			try {
			final List<?> elements = (List<?>) this.currentDetectorList.getValue();
			final List<?> regionClone = BeanUI.cloneBeans(regionToCopy);
			int index = -1;
				for (Object element : elements) {
					++index;
					if(index == currentDetectorList.getSelectedIndex())
						roisToImportComposite.getRegionList().addBean(regionClone.get(index), -1);
					else{
						Method addRegion = element.getClass().getMethod("addRegion", uk.ac.gda.beans.vortex.VortexROI.class);
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
		List<? extends DetectorROI> value = (List<? extends DetectorROI>)roisToImportComposite.getRegionList().getValue();
		return value;
	}

}