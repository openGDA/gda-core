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

package gda.simplescan;

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

/**
 *
 */
public final class SimpleScanUIEditor extends RichBeanEditorPart {

	SimpleScan bean;
	
	private SimpleScanComposite beanComposite;

	/**
	 * @param path
	 * @param mappingURL
	 * @param dirtyContainer
	 * @param editingBean
	 */
	public SimpleScanUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
		bean = (SimpleScan) editingBean;
	}

	@Override
	public String getRichEditorTabText() {
		return "SimpleScan";
	}

	@Override
	public void createPartControl(Composite comp) {
		final ScrolledComposite scrolledComposite = new ScrolledComposite(comp, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		this.beanComposite = new SimpleScanComposite(scrolledComposite, SWT.NONE, bean, this);
		scrolledComposite.setContent(beanComposite);
		beanComposite.layout();
		scrolledComposite.setMinSize(beanComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	@Override
	public void setFocus() {
	}

	public FieldComposite getFromPos() {
		return beanComposite.getFromPos();
	}

	public FieldComposite getToPos() {
		return beanComposite.getToPos();
	}

	public FieldComposite getStepSize() {
		return beanComposite.getStepSize();
	}

	public FieldComposite getAcqTime() {
		return beanComposite.getAcqTime();
	}
	
	public FieldComposite getScannables() {
		return beanComposite.getScannables();
	}
	
	public FieldComposite getDetectors() {
		return beanComposite.getDetectors();
	}
	
	public void setDirty(boolean dirty)
	{
		this.dirtyContainer.setDirty(dirty);
	}
	
	public FieldComposite getScannableName(){
		return beanComposite.getScannableName();
	}
}
