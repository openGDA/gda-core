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
import gda.device.Timer;
import gda.device.XmapDetector;
import gda.factory.Finder;

import java.io.File;
import java.net.URL;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentBeanManager;
import uk.ac.gda.client.experimentdefinition.ui.handlers.XMLCommandHandler;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.composites.FluorescenceComposite;
import uk.ac.gda.exafs.ui.detector.Counts;
import uk.ac.gda.exafs.ui.detector.DetectorEditor;
import uk.ac.gda.exafs.ui.detector.DetectorElementComposite;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.selector.BeanSelectionEvent;
import uk.ac.gda.richbeans.components.selector.BeanSelectionListener;
import uk.ac.gda.richbeans.components.selector.GridListEditor;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.editors.DirtyContainer;

public class VortexParametersUIEditor extends DetectorEditor {
	public Label acquireFileLabel;
	protected VortexParameters vortexParameters;
	private ComboWrapper countType;
	private VortexAcquire vortexAcquire;
	private XmapDetector xmapDetector;
	private VortexElements vortexElements;
	
	public VortexParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean, "vortexConfig");
		vortexParameters = (VortexParameters) editingBean;
	}

	@Override
	protected String getRichEditorTabText() {
		return "Vortex";
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		Composite left = sashPlotFormComposite.getLeft();
		String detectorName = vortexParameters.getDetectorName();
		xmapDetector = (XmapDetector) Finder.getInstance().find(detectorName);
		String tfgName = vortexParameters.getTfgName();
		Timer tfg = (Timer) Finder.getInstance().find(tfgName);
		vortexAcquire = new VortexAcquire(sashPlotFormComposite, xmapDetector, tfg, getSite().getShell().getDisplay(), plot, new Counts(), dirtyContainer);
		vortexAcquire.createAcquire(parent, left);
		sashPlotFormComposite.setWeights(new int[] { 35, 74 });
		vortexElements = new VortexElements(left, this.getSite().getShell(), dirtyContainer, sashPlotFormComposite, vortexParameters, counts);
		if (!ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.DETECTOR_OUTPUT_IN_OUTPUT_PARAMETERS))
			vortexElements.addOutputPreferences(left);
		vortexElements.createROI(left);
		vortexElements.configureUI(vortexAcquire.getMcaData(), getCurrentSelectedElementIndex());
		
		vortexAcquire.addAcquireListener(getCurrentSelectedElementIndex(), getDetectorList(), getDetectorElementComposite());
		vortexAcquire.addLoadListener(vortexParameters, getDetectorList(), getDetectorElementComposite());
		getDetectorList().addBeanSelectionListener(new BeanSelectionListener() {
			@Override
			public void selectionChanged(BeanSelectionEvent evt) {
				int[][][] mcaData = vortexAcquire.getMcaData();
				plot.plot(evt.getSelectionIndex(),mcaData, false, null);
				DetectorElementComposite detectorElementComposite = getDetectorElementComposite();
				detectorElementComposite.setTotalElementCounts(counts.getTotalElementCounts(evt.getSelectionIndex(), mcaData));
				detectorElementComposite.setTotalCounts(counts.getTotalCounts(mcaData));
				vortexElements.configureUI(vortexAcquire.getMcaData(), evt.getSelectionIndex());
			}
		});
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

	public ScaleBox getCollectionTime() {
		return vortexAcquire.getAcquireTime();
	}

	public ComboWrapper getCountType() {
		return countType;
	}

	public BooleanWrapper getSaveRawSpectrum() {
		return vortexElements.getSaveRawSpectrum();
	}

	@Override
	protected String getDataXMLName() {
		String varDir = LocalProperties.get(LocalProperties.GDA_VAR_DIR);
		return varDir + "/vortex_editor_data.xml";
	}

	@Override
	public void dispose() {
		if (countType != null)
			countType.dispose();
		vortexElements.getSaveRawSpectrum().dispose();
		acquireFileLabel.dispose();
		super.dispose();
	}
	
	public XMLCommandHandler getXMLCommandHandler() {
		return ExperimentBeanManager.INSTANCE.getXmlCommandHandler(VortexParameters.class);
	}
	
	public DetectorElementComposite getDetectorElementComposite() {
		return vortexElements.getDetectorListComposite().getDetectorElementComposite();
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
	
	public GridListEditor getDetectorList() {
		return vortexElements.getDetectorListComposite().getDetectorList();
	}
	
	protected int getCurrentSelectedElementIndex() {
		return vortexElements.getDetectorListComposite().getDetectorList().getSelectedIndex();
	}
	
}