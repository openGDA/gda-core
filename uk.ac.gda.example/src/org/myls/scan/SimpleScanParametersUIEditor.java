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

package org.myls.scan;

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
public final class SimpleScanParametersUIEditor extends RichBeanEditorPart {

	private SimpleScanParametersComposite beanComposite;

	/**
	 * @param path
	 * @param mappingURL
	 * @param dirtyContainer
	 * @param editingBean
	 */
	public SimpleScanParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getRichEditorTabText() {
		return "SimpleScanParametersEditor";
	}

	/**
	 * 
	 */
	@Override
	public void createPartControl(Composite comp) {
		final ScrolledComposite scrolledComposite = new ScrolledComposite(comp, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		this.beanComposite = new SimpleScanParametersComposite(scrolledComposite, SWT.NONE);
		scrolledComposite.setContent(beanComposite);
		beanComposite.layout();
		scrolledComposite.setMinSize(beanComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	/**
	 * 
	 */
	@Override
	public void setFocus() {
		//TODO
	}

	public FieldComposite getEnd() {
		return beanComposite.getEnd();
	}

	public FieldComposite getName() {
		return beanComposite.getName();
	}

	public FieldComposite getSeconds() {
		return beanComposite.getSeconds();
	}

	public FieldComposite getStart() {
		return beanComposite.getStart();
	}

	
}
