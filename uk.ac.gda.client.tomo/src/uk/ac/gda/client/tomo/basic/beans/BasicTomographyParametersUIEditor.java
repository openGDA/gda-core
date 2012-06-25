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

package uk.ac.gda.client.tomo.basic.beans;

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
public final class BasicTomographyParametersUIEditor extends RichBeanEditorPart {

	private BasicTomographyParametersComposite beanComposite;

	/**
	 * @param path
	 * @param mappingURL
	 * @param dirtyContainer
	 * @param editingBean
	 */
	public BasicTomographyParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer,
			Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
		// TODO Auto-generated constructor stub
	}


	@Override
	public String getRichEditorTabText() {
		return "BasicTomographyParameters";
	}


	@Override
	public void createPartControl(Composite comp) {
		final ScrolledComposite scrolledComposite = new ScrolledComposite(comp, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		this.beanComposite = new BasicTomographyParametersComposite(scrolledComposite, SWT.NONE);
		scrolledComposite.setContent(beanComposite);
		beanComposite.layout();
		scrolledComposite.setMinSize(beanComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}


	@Override
	public void setFocus() {
		//TODO
	}

	public FieldComposite getCamera() {
		return beanComposite.getCamera();
	}

	public FieldComposite getTheta() {
		return beanComposite.getTheta();
	}

	public FieldComposite getFlatFieldTranslation() {
		return beanComposite.getFlatFieldTranslation();
	}

	public FieldComposite getCameraROIStartX() {
		return beanComposite.getCameraROIStartX();
	}

	public FieldComposite getCameraROIStartY() {
		return beanComposite.getCameraROIStartY();
	}

	public FieldComposite getCameraROISizeX() {
		return beanComposite.getCameraROISizeX();
	}

	public FieldComposite getCameraROISizeY() {
		return beanComposite.getCameraROISizeY();
	}

	public FieldComposite getCameraBinX() {
		return beanComposite.getCameraBinX();
	}

	public FieldComposite getCameraBinY() {
		return beanComposite.getCameraBinY();
	}

	public FieldComposite getCameraExposureTime() {
		return beanComposite.getCameraExposureTime();
	}

	public FieldComposite getScanStartAngle() {
		return beanComposite.getScanStartAngle();
	}

	public FieldComposite getScanEndAngle() {
		return beanComposite.getScanEndAngle();
	}

	public FieldComposite getScanNumberOfPointsPerSegment() {
		return beanComposite.getScanNumberOfPointsPerSegment();
	}

	public FieldComposite getScanNumberOfSegments() {
		return beanComposite.getScanNumberOfSegments();
	}

	public FieldComposite getDarkNumberOfImages() {
		return beanComposite.getDarkNumberOfImages();
	}

	public FieldComposite getFlatNumberOfImages() {
		return beanComposite.getFlatNumberOfImages();
	}

	public FieldComposite getReconNumberOfChunks() {
		return beanComposite.getReconNumberOfChunks();
	}

	public FieldComposite getReconJobName() {
		return beanComposite.getReconJobName();
	}

}
