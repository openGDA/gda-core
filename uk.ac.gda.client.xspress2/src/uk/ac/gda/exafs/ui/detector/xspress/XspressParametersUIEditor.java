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

package uk.ac.gda.exafs.ui.detector.xspress;

import gda.configuration.properties.LocalProperties;
import gda.device.detector.xspress.XspressDetector;
import gda.factory.Finder;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentBeanManager;
import uk.ac.gda.client.experimentdefinition.ui.handlers.XMLCommandHandler;
import uk.ac.gda.exafs.ui.composites.FluorescenceComposite;
import uk.ac.gda.exafs.ui.detectorviews.DetectorEditor;
import uk.ac.gda.exafs.ui.detectorviews.DetectorElementComposite;
import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.richbeans.components.selector.GridListEditor;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.editors.DirtyContainer;

public class XspressParametersUIEditor extends DetectorEditor {
	private XspressParameters xspressParameters;
	private String xmlPath;
	private Xspress xspress;
	
	public XspressParametersUIEditor(String xmlPath, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(xmlPath, mappingURL, dirtyContainer, editingBean, "xspressConfig");
		this.xmlPath = xmlPath;
		this.xspressParameters = (XspressParameters) editingBean;
	}

	@Override
	public void createPartControl(Composite parent) {
		XspressDetector xspressDetector = Finder.getInstance().find(xspressParameters.getDetectorName());
		xspress = new Xspress(xmlPath, this.getSite(), parent, xspressDetector, xspressParameters.getDetectorList(), xspressParameters);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);
	}

	@Override
	public void notifyFileSaved(File file) {
		FluorescenceComposite fluorescenceComposite = (FluorescenceComposite) BeanUI.getBeanField("fluorescenceParameters", DetectorParameters.class);
		if (fluorescenceComposite == null || fluorescenceComposite.isDisposed())
			return;
		fluorescenceComposite.getDetectorType().setValue("Germanium");
		fluorescenceComposite.getConfigFileName().setValue(file.getAbsolutePath());
	}
	
	public XMLCommandHandler getXMLCommandHandler() {
		return ExperimentBeanManager.INSTANCE.getXmlCommandHandler(XspressParameters.class);
	}
	
	@Override
	protected String getRichEditorTabText() {
		return "Xspress";
	}

	@Override
	public void setFocus() {
	}
	
	public ComboWrapper getResGrade() {
		return xspress.getResolutionGrade().getResolutionGradeCombo();
	}

	public BooleanWrapper getEditIndividualElements() {
		return xspress.getXspressElements().getShowIndividualElements();
	}

	public ComboWrapper getReadoutMode() {
		return xspress.getReadoutMode().getReadoutMode();
	}
	
	public BooleanWrapper getOnlyShowFF() {
		return xspress.getXspressPreferences().getOnlyShowFF();
	}
	
	public BooleanWrapper getShowDTRawValues() {
		return xspress.getXspressPreferences().getShowDTRawValues();
	}
	
	public BooleanWrapper getSaveRawSpectrum() {
		return xspress.getXspressPreferences().getSaveRawSpectrum();
	}
	
	public ComboWrapper getRegionType() {
		return xspress.getRegionType().getRegionType();
	}
	
	protected String getDetectorName() {
		return xspressParameters.getDetectorName();
	}

	public DetectorElementComposite getDetectorElementComposite() {
		return xspress.getXspressElements().getDetectorListComposite().getDetectorElementComposite();
	}
	
	protected int getCurrentSelectedElementIndex() {
		return xspress.getXspressElements().getDetectorListComposite().getDetectorList().getSelectedIndex();
	}
	
	public GridListEditor getDetectorList() {
		return xspress.getXspressElements().getDetectorListComposite().getDetectorList();
	}
	
}
