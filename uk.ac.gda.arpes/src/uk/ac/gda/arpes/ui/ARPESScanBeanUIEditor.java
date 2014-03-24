/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.arpes.ui;

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.arpes.beans.ARPESScanBean;
import uk.ac.gda.richbeans.beans.IFieldWidget;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public final class ARPESScanBeanUIEditor extends RichBeanEditorPart {

	private ARPESScanBeanComposite beanComposite;
	private ARPESScanBeanEditor asbe;

	public ARPESScanBeanUIEditor(String path, URL mappingURL, ARPESScanBeanEditor dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
		asbe = dirtyContainer;
	}

	@Override
	public String getRichEditorTabText() {
		return "ARPES Scan Editor";
	}

	@Override
	public void createPartControl(Composite comp) {
		final ScrolledComposite scrolledComposite = new ScrolledComposite(comp, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		this.beanComposite = new ARPESScanBeanComposite(scrolledComposite, SWT.NONE, this);
		scrolledComposite.setContent(beanComposite);
		beanComposite.layout();
		scrolledComposite.setMinSize(beanComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	@Override
	public void linkUI(boolean isPageChange) {
		super.linkUI(isPageChange);
		this.beanComposite.beanUpdated();
	}
	
	@Override
	public void setFocus() {
	}

	public FieldComposite getLensMode() {
		return beanComposite.getLensMode();
	}

	public FieldComposite getPassEnergy() {
		return beanComposite.getPassEnergy();
	}

	public FieldComposite getConfigureOnly() {
		return beanComposite.getConfigureOnly();
	}

	public FieldComposite getStartEnergy() {
		return beanComposite.getStartEnergy();
	}

	public FieldComposite getEndEnergy() {
		return beanComposite.getEndEnergy();
	}

	public FieldComposite getStepEnergy() {
		return beanComposite.getStepEnergy();
	}

	public FieldComposite getTimePerStep() {
		return beanComposite.getTimePerStep();
	}

	public FieldComposite getIterations() {
		return beanComposite.getIterations();
	}

	public IFieldWidget getSweptMode() {
		return beanComposite.getSweptMode();
	}

	public void replaceBean(ARPESScanBean read) {
		asbe.replaceBean(read);
	}
}