/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.detector.vortex;

import gda.configuration.properties.LocalProperties;

import java.io.File;
import java.net.URL;

import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentBeanManager;
import uk.ac.gda.client.experimentdefinition.ui.handlers.XMLCommandHandler;
import uk.ac.gda.exafs.ui.composites.FluorescenceComposite;
import uk.ac.gda.exafs.ui.detector.DetectorEditor;
import uk.ac.gda.exafs.ui.detector.DetectorElementComposite;
import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.selector.GridListEditor;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.editors.DirtyContainer;

public class VortexParametersUIEditor extends DetectorEditor {
	private VortexParameters vortexParameters;
	private Vortex vortex;
	
	public VortexParametersUIEditor(String xmlPath, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(xmlPath, mappingURL, dirtyContainer, editingBean, "vortexConfig");
		vortexParameters = (VortexParameters) editingBean;
	}

	@Override
	protected String getRichEditorTabText() {
		return "Vortex";
	}

	@Override
	public void createPartControl(Composite parent) {
		this.vortex = new Vortex(path, this.getSite(), parent, vortexParameters, dirtyContainer);
	}

	@Override
	public void linkUI(final boolean isPageChange) {
		super.linkUI(isPageChange);
		getDetectorElementComposite().setIndividualElements(true);
	}

	@Override
	public void notifyFileSaved(File file) {
		@SuppressWarnings("unchecked")
		final FluorescenceComposite comp = (FluorescenceComposite) BeanUI.getBeanField("fluorescenceParameters", DetectorParameters.class);
		if (comp == null || comp.isDisposed())
			return;
		comp.getDetectorType().setValue("Silicon");
		comp.getConfigFileName().setValue(file.getAbsolutePath());
	}

	@Override
	protected String getDataXMLName() {
		String varDir = LocalProperties.get(LocalProperties.GDA_VAR_DIR);
		return varDir + "/vortex_editor_data.xml";
	}

	@Override
	public void dispose() {
		vortex.getVortexElements().getSaveRawSpectrum().dispose();
		vortex.getAcquireFileLabel().dispose();
		super.dispose();
	}

	@Override
	public void setFocus() {
	}
	
	public ScaleBox getCollectionTime() {
		return vortex.getVortexAcquire().getAcquireTime();
	}
	
	public BooleanWrapper getSaveRawSpectrum() {
		return vortex.getVortexElements().getSaveRawSpectrum();
	}
	
	public XMLCommandHandler getXMLCommandHandler() {
		return ExperimentBeanManager.INSTANCE.getXmlCommandHandler(VortexParameters.class);
	}
	
	public DetectorElementComposite getDetectorElementComposite() {
		return vortex.getVortexElements().getDetectorListComposite().getDetectorElementComposite();
	}
	
	public GridListEditor getDetectorList() {
		return vortex.getVortexElements().getDetectorListComposite().getDetectorList();
	}
	
	protected int getCurrentSelectedElementIndex() {
		return vortex.getVortexElements().getDetectorListComposite().getDetectorList().getSelectedIndex();
	}
	
}