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

package uk.ac.gda.bimorph.ui;

import java.net.URL;

import org.eclipse.richbeans.widgets.FieldComposite;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
import org.eclipse.richbeans.widgets.wrappers.ComboWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public final class BimorphParametersUIEditor extends RichBeanEditorPart {
	private BimorphParametersComposite beanComposite;

	public BimorphParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
	}

	@Override
	public String getRichEditorTabText() {
		return "BimorphParametersEditor";
	}

	@Override
	public void createPartControl(Composite comp) {
		final ScrolledComposite scrolledComposite = new ScrolledComposite(comp, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		this.beanComposite = new BimorphParametersComposite(scrolledComposite, SWT.NONE);
		scrolledComposite.setContent(beanComposite);
		beanComposite.layout();
		scrolledComposite.setMinSize(beanComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	@Override
	public void setFocus() {
	}
	
	@Override
	public void linkUI(final boolean isPageChange) {
		super.linkUI(isPageChange);
		boolean calcEllipse = this.beanComposite.getCalculateErrorFile().getValue();
		this.beanComposite.setEllipseEnabled(calcEllipse);
	}
	
	public FieldComposite getMirrorScannableName() {
		return beanComposite.getBimorphScannableName();
	}

	public ComboWrapper getMirrorType() {
		return beanComposite.getMirrorType();
	}

	public FieldComposite getVoltageIncrement() {
		return beanComposite.getVoltageIncrement();
	}

	public FieldComposite getSlitSizeScannable() {
		return beanComposite.getSlitSizeScannable();
	}

	public FieldComposite getSlitPosScannable() {
		return beanComposite.getSlitPosScannable();
	}

	public FieldComposite getSlitSize() {
		return beanComposite.getSlitSize();
	}

	public FieldComposite getSlitStart() {
		return beanComposite.getSlitStart();
	}

	public FieldComposite getSlitEnd() {
		return beanComposite.getSlitEnd();
	}

	public FieldComposite getSlitStep() {
		return beanComposite.getSlitStep();
	}

	public FieldComposite getOtherSlitSizeScannable() {
		return beanComposite.getOtherSlitSizeScannable();
	}

	public FieldComposite getOtherSlitPosScannable() {
		return beanComposite.getOtherSlitPosScannable();
	}

	public FieldComposite getOtherSlitPosValue() {
		return beanComposite.getOtherSlitPos();
	}

	public FieldComposite getOtherSlitSize() {
		return beanComposite.getOtherSlitSize();
	}

	public FieldComposite getDetectorName() {
		return beanComposite.getDetectorName();
	}

	public FieldComposite getExposureTime() {
		return beanComposite.getExposureTime();
	}

	public FieldComposite getSettleTime() {
		return beanComposite.getSettleTime();
	}

	public FieldComposite getScanNumberInputs() {
		return beanComposite.getScanNumberInputs();
	}

	public FieldComposite getErrorFile() {
		return beanComposite.getErrorFile();
	}

	public FieldComposite getBeamOffset() {
		return beanComposite.getBeamOffset();
	}

	public FieldComposite getBimorphScannableName() {
		return beanComposite.getBimorphScannableName();
	}

	public FieldComposite getBimorphVoltages() {
		return beanComposite.getBimorphVoltages();
	}
	
	public ScaleBox getPixelSize() {
		return beanComposite.getPixelSize();
	}
	
	public ScaleBox getPresentSourceMirrorDistance() {
		return beanComposite.getPresentSourceMirrorDistance();
	}

	public ScaleBox getPresentMirrorFocusDistance() {
		return beanComposite.getPresentMirrorFocusDistance();
	}

	public ScaleBox getPresentAngleOfIncidence() {
		return beanComposite.getPresentAngleOfIncidence();
	}
	
	public ScaleBox getNewSourceMirrorDistance() {
		return beanComposite.getNewSourceMirrorDistance();
	}
	
	public ScaleBox getNewMirrorFocusDistance() {
		return beanComposite.getNewMirrorFocusDistance();
	}

	public ScaleBox getNewAngleOfIncidence() {
		return beanComposite.getNewAngleOfIncidence();
	}

	public ComboWrapper getISign() {
		return beanComposite.getISign();
	}

	public ScaleBox getDetectorDistance() {
		return beanComposite.getDetectorDistance();
	}

	public ScaleBox getFocusSize() {
		return beanComposite.getFocusSize();
	}
	
	public BooleanWrapper getAutoOffset() {
		return beanComposite.getAutoOffset();
	}
	
	public BooleanWrapper getAutoDist() {
		return beanComposite.getAutoDist();
	}
	
	public BooleanWrapper getCalculateErrorFile() {
		return beanComposite.getCalculateErrorFile();
	}

	public FieldComposite getBimorphGroups() {
		return beanComposite.getBimorphGroups();
	}
	
	public BooleanWrapper getBtnGroupElectrodesTogether() {
		return beanComposite.getBtnGroupElectrodesTogether();
	}
	
	public ScaleBox getPresentDetDist() {
		return beanComposite.getPresentDetDist();
	}

	public ScaleBox getSlitScanDetDist() {
		return beanComposite.getSlitScanDetDist();
	}
	
	public ScaleBox getMinSlitPos() {
		return beanComposite.getMinSlitPos();
	}

	public ScaleBox getMaxSlitPos() {
		return beanComposite.getMaxSlitPos();
	}
	
	public ComboWrapper getInv() {
		return beanComposite.getInv();
	}
	
	public ComboWrapper getMethod() {
		return beanComposite.getMethod();
	}
}