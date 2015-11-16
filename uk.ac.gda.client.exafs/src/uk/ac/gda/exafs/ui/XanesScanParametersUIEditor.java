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

import gda.configuration.properties.LocalProperties;
import gda.jython.JythonServerFacade;
import gda.util.exafs.Element;

import java.util.List;

import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import swing2swt.layout.BorderLayout;
import uk.ac.gda.beans.exafs.Region;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.exafs.ui.composites.RegionComposite;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;

/**
 * An editor part designed to be a page in a multipage editor. This part can be entirely auto-generated and extends
 * RichBeanEditorPart which provides the link between the editor and the bean.
 */
public class XanesScanParametersUIEditor extends ElementEdgeEditor {

	private static final Logger logger = LoggerFactory.getLogger(XanesScanParametersUIEditor.class);

	private ScaleBox finalEnergy;
	private RegionComposite regionsEditor;
	private double edgeVal = 0;
	private Button updateTable;
	private ELEMENT_EVENT_TYPE type;
	/**
	 * @param path
	 * @param containingEditor
	 * @param xanesScanParameters
	 */
	public XanesScanParametersUIEditor(final String path, final RichBeanMultiPageEditorPart containingEditor,
			final Object xanesScanParameters) {
		super(path, containingEditor.getMappingUrl(), containingEditor, xanesScanParameters);
		editingBean = xanesScanParameters;
	}

	/**
	 * Create contents of the editor part
	 *
	 * @param parent
	 */
	@SuppressWarnings("unused")
	@Override
	public void createPartControl(Composite parent) {

		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		container = new Composite(scrolledComposite, SWT.NONE);
		container.setLayout(new BorderLayout(0, 0));
		scrolledComposite.setContent(container);

		Group exafsScanParametersGroup = new Group(container, SWT.NONE);
		exafsScanParametersGroup.setLayoutData(BorderLayout.NORTH);
		exafsScanParametersGroup.setText("XANES/ANGLE Parameters");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		exafsScanParametersGroup.setLayout(gridLayout);

		createElementEdgeArea(exafsScanParametersGroup);

		new Label(exafsScanParametersGroup, SWT.NONE);
		new Label(exafsScanParametersGroup, SWT.NONE);

		updateTable = new Button(exafsScanParametersGroup, SWT.NONE);
		updateTable.setText("             Get Defaults            ");

		regionsEditor = new RegionComposite(exafsScanParametersGroup, SWT.NONE, getSite(), (XanesScanParameters) editingBean, this);
		regionsEditor.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, false, false));

		updateTable.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for(int i=0;i<((XanesScanParameters) editingBean).getRegions().size();i++){
					if(i>4)
						((XanesScanParameters) editingBean).getRegions().remove(i);
				}
				Region obj = null;
				try {
					List<uk.ac.gda.beans.exafs.Region> regions = regionsEditor.getBeanRegions();
					double coreHole = getSelectedElement(type).getCoreHole(getEdgeUseBean());

					if (getEdgeValue()!=edgeVal) {
						edgeVal = getEdgeValue();
						for (int i = 0; i < regions.size(); i++) {
							obj = regions.get(i);
							if (i == 0) {
								obj.setEnergy(edgeVal - 100 * coreHole);
								obj.setStep(5 * coreHole);
							} else if (i == 1) {
								obj.setEnergy(edgeVal - 20 * coreHole);
								obj.setStep(coreHole);
							} else if (i == 2) {
								obj.setEnergy(edgeVal - 10 * coreHole);
								obj.setStep(coreHole / 5);
							} else if (i == 3) {
								obj.setEnergy(edgeVal + 10 * coreHole);
								obj.setStep(coreHole);
							} else if (i == 4) {
								obj.setEnergy(edgeVal + 20 * coreHole);
								obj.setStep(2 * coreHole);
							}
						}
						getFinalEnergy().setValue(getFinalEnergyFromElement());
						((XanesScanParameters) editingBean).setRegions(regions);
						regionsEditor.updateTable();
					}

				} catch (Exception e1) {
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		new Label(exafsScanParametersGroup, SWT.NONE);

		Composite bottom = new Composite(exafsScanParametersGroup, SWT.NONE);
		bottom.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(SWT.NONE, SWT.CENTER, true, false);
		gd.widthHint = 195;
		bottom.setLayoutData(gd);

		Label stopEnergyLabel = new Label(bottom, SWT.NONE);
		stopEnergyLabel.setText("Final Energy");

		finalEnergy = new ScaleBox(bottom, SWT.NONE);

		if (LocalProperties.get("gda.beamline.name").equals("b18")) {
			String dcmCrystal = JythonServerFacade.getInstance().evaluateCommand("dcm_crystal()");

			if (dcmCrystal.equals("Si(111)")) {
				finalEnergy.setMinimum(2050.0);
				finalEnergy.setMaximum(26000.0);
			} else if (dcmCrystal.equals("Si(311)")) {
				finalEnergy.setMinimum(4000.0);
				finalEnergy.setMaximum(40000.0);
			}
		}
		else
			finalEnergy.setMaximum(120000.0);

		finalEnergy.setUnit("eV");
		finalEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		expandContainer = container;
		createEstimationComposite(container);
		scrolledComposite.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	@Override
	public void linkUI(final boolean isPageChange) {
		try {
			setPointsUpdate(false);
			try {
				setupElementAndEdge("XanesScanParameters");
			} catch (Exception e) {
				e.printStackTrace();
			}
			super.linkUI(isPageChange);
			try {
				updateElement(ELEMENT_EVENT_TYPE.INIT); // Must be after super.linkUI()
				updatePlottedPoints();
			} catch (Exception e) {
				logger.error("Cannot update Xanes points graph!", e);
			}
		} finally {
			if (type != ELEMENT_EVENT_TYPE.INIT)
				setPointsUpdate(true);
		}
	}

	protected double getFinalEnergyFromElement() throws Exception {
		Element ele = getElementUseBean();
		String edge = getEdgeUseBean();
		return ele.getFinalEnergy(edge);
	}

	@Override
	protected void updateElement(ELEMENT_EVENT_TYPE type) {
		try {
			this.type = type;
			if (type != null)
				super.updateElement(type);
		} catch (Exception e) {
		}
	}

	@Override
	protected String getRichEditorTabText() {
		return "XANES Scan";
	}

	public ScaleBox getFinalEnergy() {
		return finalEnergy;
	}

	public void setDirty(boolean dirty) {
		this.dirtyContainer.setDirty(dirty);
	}
}
