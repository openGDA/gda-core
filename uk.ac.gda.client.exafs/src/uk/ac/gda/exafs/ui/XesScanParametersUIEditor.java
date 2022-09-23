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
import org.eclipse.richbeans.widgets.FieldComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;

import gda.exafs.scan.ScanObject;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.XesScanParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentEditorManager;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.FauxRichBeansEditor;

public final class XesScanParametersUIEditor extends FauxRichBeansEditor<XesScanParameters> {

	private XesScanParametersComposite beanComposite;

	public XesScanParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, (XesScanParameters) editingBean);
	}

	@Override
	public String getRichEditorTabText() {
		return "XesScanParameters";
	}

	@Override
	public void createPartControl(Composite comp) {
		ScrolledComposite scrolledComposite = new ScrolledComposite(comp, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		beanComposite = new XesScanParametersComposite(scrolledComposite, SWT.NONE);
		beanComposite.addIObserver((source,arg) -> {
			beanChanged();
		});
		scrolledComposite.setContent(beanComposite);
		scrolledComposite.setMinSize(beanComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	@Override
	public void linkUI(final boolean isPageChange) {
		// update GUI from bean when it has been changed.
		super.linkUI(isPageChange);
		beanComposite.setBean(getBean());
		beanComposite.setEditingInput(getEditorInput());
		beanComposite.setupUiFromBean();
		beanComposite.linkUI();
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
			if (currentDetParams != null)
				if (!currentDetParams.getExperimentType().equalsIgnoreCase("xes"))
					MessageDialog.openInformation(getSite().getShell(), "Options inconsistent", "XES option has not been selected in the detector parameters!");
		} catch (Exception e) {
			// any problems, simply ignore as this will be a problem in the XafsEditorManager which will be picked up elsewhere
		}
		super.doSave(monitor);
	}

	public FieldComposite getScanFileName() {
		return beanComposite.getScanFileName();
	}
}
