/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui;


import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentEditorManager;
import uk.ac.gda.exafs.ui.data.ScanObject;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.components.wrappers.RadioWrapper;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public final class XesScanParametersUIEditor extends RichBeanEditorPart {

	private XesScanParametersComposite beanComposite;

	public XesScanParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
	}

	@Override
	public String getRichEditorTabText() {
		return "XesScanParameters";
	}

	@Override
	public void createPartControl(Composite comp) {

		final ScrolledComposite scrolledComposite = new ScrolledComposite(comp, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		this.beanComposite = new XesScanParametersComposite(scrolledComposite, SWT.NONE);
		scrolledComposite.setContent(beanComposite);

		scrolledComposite.setMinSize(beanComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

	}

	@Override
	public void linkUI(final boolean isPageChange) {

		super.linkUI(isPageChange);
		beanComposite.linkUI();

		beanComposite.setEditingInput(getEditorInput());
	}

	@Override
	public void setFocus() {
		beanComposite.setFocus();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// before saving, check if the current detector parameters has xes selected, to ensure the correct ion chambers
		try {
			IExperimentEditorManager man = ExperimentFactory.getExperimentEditorManager();
			IDetectorParameters currentDetParams = ((ScanObject)man.getSelectedScan()).getDetectorParameters();

			if (currentDetParams != null) {
				if (!currentDetParams.getExperimentType().equalsIgnoreCase("xes")) {
					// popup to warn that wrong detector params
					MessageDialog.openInformation(getSite().getShell(), "Options inconsistent", "XES option has not been selected in the detector parameters!");
				}
			}
		} catch (Exception e) {
			// any problems, simply ignore as this will be a problem in the XafsEditorManager which will be picked up elsewhere
		}

		super.doSave(monitor);
	}

	public FieldComposite getScanType() {
		return beanComposite.getScanType();
	}

	public FieldComposite getXesIntegrationTime() {
		return beanComposite.getXesIntegrationTime();
	}

	public FieldComposite getScanFileName() {
		return beanComposite.getScanFileName();
	}

	public FieldComposite getElement() {
		return beanComposite.getElement();
	}

	public FieldComposite getEdge() {
		return beanComposite.getEdge();
	}

	public FieldComposite getXesInitialEnergy() {
		return beanComposite.getXesInitialEnergy();
	}

	public FieldComposite getXesFinalEnergy() {
		return beanComposite.getXesFinalEnergy();
	}

	public FieldComposite getXesStepSize() {
		return beanComposite.getXesStepSize();
	}

	public FieldComposite getMonoInitialEnergy() {
		return beanComposite.getMonoInitialEnergy();
	}

	public FieldComposite getMonoFinalEnergy() {
		return beanComposite.getMonoFinalEnergy();
	}

	public FieldComposite getMonoStepSize() {
		return beanComposite.getMonoStepSize();
	}

	public FieldComposite getXesEnergy() {
		return beanComposite.getXesEnergy();
	}

	public FieldComposite getMonoEnergy() {
		return beanComposite.getMonoEnergy();
	}

	public FieldComposite getAdditionalCrystal0() {
		return beanComposite.getAdditionalCrystal0();
	}

	public FieldComposite getAdditionalCrystal1() {
		return beanComposite.getAdditionalCrystal1();
	}

	public FieldComposite getAdditionalCrystal2() {
		return beanComposite.getAdditionalCrystal2();
	}

	public FieldComposite getAdditionalCrystal3() {
		return beanComposite.getAdditionalCrystal3();
	}

	public RadioWrapper getLoopChoice() {
		return beanComposite.getLoopChoice();
	}

}
