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

import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import gda.configuration.properties.LocalProperties;
import gda.factory.Finder;
import gda.jython.JythonServerFacade;
import gda.util.exafs.Element;
import swing2swt.layout.BorderLayout;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.exafs.ui.composites.RegionComposite;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;

/**
 * An editor part designed to be a page in a multipage editor. This part can be entirely auto-generated and extends
 * RichBeanEditorPart which provides the link between the editor and the bean.
 *
 * SWTBot test: uk.ac.gda.exafs.ui.XanesScanParametersUIEditorTest
 */
public class XanesScanParametersUIEditor extends ElementEdgeEditor {

	private static final Logger logger = LoggerFactory.getLogger(XanesScanParametersUIEditor.class);

	private ScaleBox finalEnergy;
	private RegionComposite regionsEditor;
	private final DefaultXanesRegions regionsProvider;
	private double edgeVal = 0;
	private Button updateTable;
	private ELEMENT_EVENT_TYPE type;
	private double maxAllowedFinalEnergy = 30000; // Maximum allowed final energy; the value in the 'finalEnergy' box will be constrained to be <= to this value.
	private boolean showElementEdge = true;

	/**
	 * @param path
	 * @param containingEditor
	 * @param xanesScanParameters
	 */
	public XanesScanParametersUIEditor(final String path, final RichBeanMultiPageEditorPart containingEditor,
			final Object xanesScanParameters) {
		super(path, containingEditor.getMappingUrl(), containingEditor, xanesScanParameters);
		editingBean = xanesScanParameters;

		// Set flag to hide element and edge controls if the values are set to null in the bean.
		if (editingBean instanceof XanesScanParameters) {
			XanesScanParameters param = (XanesScanParameters) editingBean;
			if (StringUtils.isEmpty(param.getElement()) && StringUtils.isEmpty(param.getEdge()) ) {
				showElementEdge = false;
			}
		}

		regionsProvider = Finder.findOptionalSingleton(DefaultXanesRegions.class)
							.orElse(new StandardXanesRegionsProvider());

		logger.debug("Loaded {} as default XANES regions provider", regionsEditor.getClass().getName());
	}

	private void createDefaultsButton(Composite parent) {
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);

		updateTable = new Button(parent, SWT.NONE);
		updateTable.setText("             Get Defaults            ");

		updateTable.addListener(SWT.Selection, selectionEvent -> {
			try {
				((XanesScanParameters) editingBean).setRegions(regionsProvider.getDefaultRegions(getElementUseBean(), getEdgeUseBean()));
				getFinalEnergy().setValue(regionsProvider.getFinalEnergy(getElementUseBean(), getEdgeUseBean()));
				regionsEditor.updateTable();
			} catch (Exception e) {
				logger.error("Error setting default parameters", e);
			}
		});
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
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		exafsScanParametersGroup.setLayout(gridLayout);

		if (showElementEdge) {
			exafsScanParametersGroup.setText("XANES/ANGLE Parameters");
			createElementEdgeArea(exafsScanParametersGroup);
			createDefaultsButton(exafsScanParametersGroup);
		} else {
			exafsScanParametersGroup.setText("Energy Region Parameters");
		}

		regionsEditor = new RegionComposite(exafsScanParametersGroup, SWT.NONE, getSite(), (XanesScanParameters) editingBean, this);
		regionsEditor.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, false, false));

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
		else {
			finalEnergy.setMaximum(maxAllowedFinalEnergy);
		}
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
			if (showElementEdge) {
				try {
					setupElementAndEdge("XanesScanParameters");
				} catch (Exception e) {
					e.printStackTrace();
				}
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
		// If necessary.adjust the value so that is is less than the maximum allowed
		Double val = (Double) finalEnergy.getValue();
		if (val != null && val > finalEnergy.getMaximum()) {
			logger.info("Final energy {} exceeds maximum value. Setting to max allowed value ({})", val, finalEnergy.getMaximum());
			finalEnergy.off();
			finalEnergy.setValue( Math.min(finalEnergy.getMaximum(), val));
			finalEnergy.on();

		}
		return finalEnergy;
	}

	public void setDirty(boolean dirty) {
		this.dirtyContainer.setDirty(dirty);
	}
}
