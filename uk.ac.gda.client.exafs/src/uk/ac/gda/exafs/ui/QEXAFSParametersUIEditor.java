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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import swing2swt.layout.BorderLayout;
import uk.ac.gda.beans.exafs.QEXAFSParameters;
import uk.ac.gda.exafs.ui.composites.QEXAFSParametersComposite;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

/**
 *
 */
public final class QEXAFSParametersUIEditor extends RichBeanEditorPart {

	private QEXAFSParametersComposite beanComposite;

	/**
	 * @param path
	 * @param mappingURL
	 * @param dirtyContainer
	 * @param editingBean
	 */
	public QEXAFSParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getRichEditorTabText() {
		return "QEXAFS Editor";
	}

	/**
	 * 
	 */
	@Override
	public void createPartControl(Composite comp) {

		final ScrolledComposite scrolledComposite = new ScrolledComposite(comp, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		final Composite container = new Composite(scrolledComposite, SWT.NONE);
		container.setLayout(new BorderLayout(0, 0));
		scrolledComposite.setContent(container);

		Group grpQuickExafsParameters = new Group(container, SWT.NONE);
		grpQuickExafsParameters.setText("Quick EXAFS Parameters");
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		grpQuickExafsParameters.setLayout(gridLayout);

		this.beanComposite = new QEXAFSParametersComposite(grpQuickExafsParameters, SWT.NONE, (QEXAFSParameters)editingBean);
		GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gridData.widthHint = 800;
		beanComposite.setLayoutData(gridData);
		beanComposite.setSize(800, 473);

	}

	/**
	 * 
	 */
	@Override
	public void setFocus() {
		// TODO
	}

	public FieldComposite getInitialEnergy() {
		return beanComposite.getInitialEnergy();
	}

	public FieldComposite getFinalEnergy() {
		return beanComposite.getFinalEnergy();
	}

	public FieldComposite getSpeed() {
		return beanComposite.getSpeed();
	}

	public FieldComposite getStepSize() {
		return beanComposite.getStepSize();
	}
	
	public FieldComposite getTime() {
		return beanComposite.getTime();
	}

	public FieldComposite getShouldValidate() {
		return beanComposite.getShouldValidate();
	}
}
