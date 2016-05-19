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

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.api.widget.ActiveMode;
import org.eclipse.richbeans.widgets.wrappers.ComboWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import gda.configuration.properties.LocalProperties;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.FluorescenceParameters;
import uk.ac.gda.beans.exafs.SoftXRaysParameters;
import uk.ac.gda.beans.exafs.TransmissionParameters;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.composites.FluorescenceComposite;
import uk.ac.gda.exafs.ui.composites.SoftXRaysComposite;
import uk.ac.gda.exafs.ui.composites.TransmissionComposite;
import uk.ac.gda.exafs.ui.data.ScanObjectManager;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public class DetectorParametersUIEditor extends RichBeanEditorPart {
	private ComboWrapper experimentType;
	private ScrolledComposite scrolledComposite;
	private Composite composite;
	private TransmissionComposite transmissionComposite;
	private FluorescenceComposite fluorescenceComposite;
	private FluorescenceComposite xesParameters;
	private SoftXRaysComposite softXRaysParameters;
	private StackLayout stackLayout;
	private Composite stackComponent;

	/**
	 * @param path
	 * @param mappingURL
	 * @param dirtyContainer
	 * @param editingBean
	 */
	public DetectorParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
	}

	@Override
	protected String getRichEditorTabText() {
		return "Detector";
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setData(this);
		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		composite = new Composite(scrolledComposite, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		composite.setLayout(gridLayout);
		composite.setLocation(0, 0);
		Label experimentTypeLabel = new Label(composite, SWT.NONE);
		experimentTypeLabel.setText("Experiment Type");
		experimentTypeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		experimentType = new ComboWrapper(composite, SWT.READ_ONLY);
		String[] items;
		if (ScanObjectManager.isXESOnlyMode())
			items = new String[] {"XES"};
		else if (ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.DETECTORS_SOFT_XRAY))
			items = new String[] {"Transmission", "Fluorescence", "Soft X-Rays"};
		else if (ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.DETECTORS_FLUO_ONLY))
			items = new String[] {"Fluorescence"};
		else
			items = new String[] {"Transmission", "Fluorescence"};
		experimentType.setItems(items);
		GridData gd_experimentType = new GridData(SWT.FILL, SWT.CENTER, false, false);
		experimentType.setLayoutData(gd_experimentType);
		Composite composite_1 = new Composite(composite, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		composite_1.setLayout(new GridLayout());
		Label blankLabel = new Label(composite, SWT.NONE);
		blankLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		stackComponent = new Composite(composite, SWT.NONE);
		stackLayout = new StackLayout();
		stackComponent.setLayout(stackLayout);
		GridData gd_stackComponent = new GridData(SWT.FILL, SWT.FILL, true, true);
		stackComponent.setLayoutData(gd_stackComponent);
		transmissionComposite = new TransmissionComposite(stackComponent, SWT.NONE, (DetectorParameters) editingBean, controller);
		transmissionComposite.setActiveMode(ActiveMode.ACTIVE_ONLY);
		transmissionComposite.setEditorClass(TransmissionParameters.class);
		if (LocalProperties.get("gda.factory.factoryName").equals("b16server") ||  LocalProperties.get("gda.factory.factoryName").equals("b16"))
			fluorescenceComposite = new FluorescenceComposite(stackComponent, SWT.NONE, true, false, false, false, (DetectorParameters) editingBean, controller);
		else if (LocalProperties.get("gda.factory.factoryName").equals("b18"))
			fluorescenceComposite = new FluorescenceComposite(stackComponent, SWT.NONE, true, true, true, false, (DetectorParameters) editingBean, controller);
		else if (LocalProperties.get("gda.factory.factoryName").equalsIgnoreCase("i18"))
			fluorescenceComposite = new FluorescenceComposite(stackComponent, SWT.NONE, false, true, true, false, (DetectorParameters) editingBean, controller);
		else
			fluorescenceComposite = new FluorescenceComposite(stackComponent, SWT.NONE, true, true, false, false, (DetectorParameters) editingBean, controller);
		fluorescenceComposite.setActiveMode(ActiveMode.ACTIVE_ONLY);
		fluorescenceComposite.setEditorClass(FluorescenceParameters.class);
		if (LocalProperties.get("gda.factory.factoryName").equals("b18")) {
			softXRaysParameters = new SoftXRaysComposite(stackComponent, SWT.NONE, controller);
			softXRaysParameters.setActiveMode(ActiveMode.ACTIVE_ONLY);
			softXRaysParameters.setEditorClass(SoftXRaysParameters.class);
		}
		if (LocalProperties.get("gda.factory.factoryName").equals("i20")) {
			xesParameters = new FluorescenceComposite(stackComponent, SWT.NONE, true, false, false, true, (DetectorParameters) editingBean, controller);
			xesParameters.setActiveMode(ActiveMode.ACTIVE_ONLY);
			xesParameters.setEditorClass(FluorescenceParameters.class);
		}
		stackLayout.topControl = transmissionComposite;
		scrolledComposite.setContent(composite);
		scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		if (!ExafsActivator.getDefault().getPreferenceStore().getDefaultBoolean(ExafsPreferenceConstants.HIDE_WORKING_ENERGY)) {
			transmissionComposite.getIonChamberComposite().calculatePressure();
			fluorescenceComposite.getIonChamberComposite().calculatePressure();
		}
	}

	@Override
	public void linkUI(final boolean isPageChange) {
		experimentType.addValueListener(new ValueAdapter("experimentTypeListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateExperimentType();
			}
		});
		super.linkUI(isPageChange);
		initExperimentType();
		if (fluorescenceComposite != null) {
			if (path != null)
				fluorescenceComposite.setCurrentFolder((new File(path)).getParentFile());
			fluorescenceComposite.updateFileName();
		}
		if (softXRaysParameters != null) {
			if (path != null)
				softXRaysParameters.setCurrentFolder((new File(path)).getParentFile());
			softXRaysParameters.updateFileName();
		}
		if (xesParameters != null) {
			if (path != null)
				xesParameters.setCurrentFolder((new File(path)).getParentFile());
			xesParameters.updateFileName();
		}
	}

	private void initExperimentType() {
		DetectorParameters bean = (DetectorParameters) editingBean;
		int index = -1;
		String expType = bean.getExperimentType();
		final List<String> items = Arrays.asList(experimentType.getItems());
		if (expType == null) {
			// as getExperimentType is optional, choose
			// the first sub parameters that is non-null and choose that one
			if (bean.getTransmissionParameters() != null)
				expType = "Transmission";
			else if (bean.getFluorescenceParameters() != null)
				expType = "Fluorescence";
			else if (bean.getSoftXRaysParameters() != null)
				expType = "Soft X-Rays";
			else if (bean.getXesParameters() != null)
				expType = "XES";
		}
		index = items.indexOf(expType);
		if (index < 0)
			index = 0;
		experimentType.select(index);
		updateExperimentType();
	}

	private void updateExperimentType() {
		try {
			int index = experimentType.getSelectionIndex();
			String[] items = experimentType.getItems();
			if (index < 0 || index >= items.length) {
				index = 0;
				experimentType.select(index);
			}
			String selection = items[index];
			final DetectorParameters bean = (DetectorParameters)editingBean;
			Control control = null;
			Object  val     = null;
			if ("Transmission".equals(selection)) {
				control = transmissionComposite;
				val = getTransmissionParameters().getValue();
				if (val == null)
					val = bean.getTransmissionParameters();
				if (val == null)
					val = new TransmissionParameters();
				if (bean.getTransmissionParameters() == null)
					bean.setTransmissionParameters((TransmissionParameters) val);
				if (getTransmissionParameters().getValue() == null)
					getTransmissionParameters().setEditingBean(val);
				transmissionComposite.setExperimentType("Transmission");
			}
			else if ("Fluorescence".equals(selection)) {
				control = fluorescenceComposite;
				val = getFluorescenceParameters().getValue();
				if (val == null)
					val = bean.getFluorescenceParameters();
				if (val == null)
					val = new FluorescenceParameters();
				if (bean.getFluorescenceParameters() == null)
					bean.setFluorescenceParameters((FluorescenceParameters) val);
				if (getFluorescenceParameters().getValue() == null)
					getFluorescenceParameters().setEditingBean(val);
				fluorescenceComposite.setExperimentType("Fluorescence");
			}
			else if ("Soft X-Rays".equals(selection)) {
				control = softXRaysParameters;
				val = getSoftXRaysParameters().getValue();
				if (val == null)
					val = bean.getSoftXRaysParameters();
				if (val == null)
					val = new SoftXRaysParameters();
				if (bean.getSoftXRaysParameters() == null)
					bean.setSoftXRaysParameters((SoftXRaysParameters) val);
				if (getSoftXRaysParameters().getValue() == null)
					getSoftXRaysParameters().setEditingBean(val);
			}
			else if ("XES".equals(selection)) {
				control = xesParameters;
				val = getXesParameters().getValue();
				if (val == null)
					val = bean.getXesParameters();
				if (val == null)
					val = new FluorescenceParameters();
				if (bean.getXesParameters() == null)
					bean.setXesParameters((FluorescenceParameters) val);
				if (getXesParameters().getValue() == null)
					getXesParameters().setEditingBean(val);
			}
			stackLayout.topControl = control;
			stackComponent.layout();
			composite.layout();
			scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		} catch (Throwable ne) {
			ne.printStackTrace();
		}
 	}

	@Override
	public void setFocus() {
	}

	/**
	 * @return TransmissionComposite
	 */
	public TransmissionComposite getTransmissionParameters() {
		return transmissionComposite;
	}

	/**
	 * @return FluorComposite
	 */
	public FluorescenceComposite getFluorescenceParameters() {
		return fluorescenceComposite;
	}

	/**
	 * @return SoftXRaysComposite
	 */
	public SoftXRaysComposite getSoftXRaysParameters() {
		return softXRaysParameters;
	}

	/**
	 * @return SoftXRaysComposite
	 */
	public FluorescenceComposite getXesParameters() {
		return xesParameters;
	}

	/**
	 * @return ComboWrapper
	 */
	public ComboWrapper getExperimentType() {
		return experimentType;
	}

}
