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

package uk.ac.gda.client.microfocus.ui.editors;

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.beans.microfocus.MicroFocusScanParameters;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public final class MicroFocusScanParametersUIEditor extends RichBeanEditorPart {

	private MicroFocusScanParametersComposite beanComposite;
	private MicroFocusScanParameters scanParameters;

	
	/**
	 * @param path
	 * @param mappingURL
	 * @param dirtyContainer
	 * @param editingBean
	 */
	public MicroFocusScanParametersUIEditor(String path, URL mappingURL,
			DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
		scanParameters = (MicroFocusScanParameters)editingBean;
	}

	/**
	 * @return Editor name
	 */
	@Override
	public String getRichEditorTabText() {
		return "MicroFocusScanParameters";
	}

	/**
	 * 
	 */
	@Override
	public void createPartControl(Composite comp) {
		final ScrolledComposite scrolledComposite = new ScrolledComposite(comp,
				SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		this.beanComposite = new MicroFocusScanParametersComposite(
				scrolledComposite, scanParameters, SWT.NONE);
		((GridData) beanComposite.getXScannableName().getLayoutData()).widthHint = 323;
		((GridData) beanComposite.getCollectionTime().getLayoutData()).widthHint = 215;
		scrolledComposite.setContent(beanComposite);
		beanComposite.layout();
		scrolledComposite.setMinSize(beanComposite.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));
	}

	/**
	 * 
	 */
	@Override
	public void setFocus() {
	}
	
	@Override
	public void linkUI(boolean isPageChange){
		super.linkUI(isPageChange);
		beanComposite.updateScanInfo();
	}

	public FieldComposite getXScannableName() {
		return beanComposite.getXScannableName();
	}

	public FieldComposite getYScannableName() {
		return beanComposite.getYScannableName();
	}
	public FieldComposite getZScannableName() {
		return beanComposite.getZScannableName();
	}
	public FieldComposite getEnergyScannableName() {
		return beanComposite.getEnergyScannableName();
	}

	public FieldComposite getCollectionTime() {
		return beanComposite.getCollectionTime();
	}

	public FieldComposite getXStart() {
		return beanComposite.getXStart();
	}

	public FieldComposite getYStart() {
		return beanComposite.getYStart();
	}

	public FieldComposite getXEnd() {
		return beanComposite.getXEnd();
	}

	public FieldComposite getYEnd() {
		return beanComposite.getYEnd();
	}

	public FieldComposite getXStepSize() {
		return beanComposite.getXStepSize();
	}

	public FieldComposite getYStepSize() {
		return beanComposite.getYStepSize();
	}
	public FieldComposite getZValue() {
		return beanComposite.getZValue();
	}

	public FieldComposite getEnergy() {
		return beanComposite.getEnergy();
	}
	
	public FieldComposite getRaster() {
		return beanComposite.getRaster();
	}
	public FieldComposite getRowTime() {
		return beanComposite.getRowTime();
	}
	}
