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

import org.dawnsci.common.richbeans.beans.IFieldWidget;
import org.dawnsci.common.richbeans.components.FieldComposite;
import org.dawnsci.common.richbeans.components.scalebox.ScaleBoxAndFixedExpression.ExpressionProvider;
import org.dawnsci.common.richbeans.components.wrappers.BooleanWrapper;
import org.dawnsci.common.richbeans.components.wrappers.ComboWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.util.Converter;
import gda.util.exafs.Element;
import swing2swt.layout.BorderLayout;
import uk.ac.gda.beans.exafs.QEXAFSParameters;
import uk.ac.gda.exafs.ui.composites.QEXAFSParametersComposite;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;

public final class QEXAFSParametersUIEditor extends ElementEdgeEditor {
	private QEXAFSParametersComposite beanComposite;
	private static Logger logger = LoggerFactory.getLogger(QEXAFSParametersUIEditor.class);

	/**
	 * @param path
	 * @param editingBean
	 */
	public QEXAFSParametersUIEditor(String path, final RichBeanMultiPageEditorPart containingEditor, final Object editingBean) {
		super(path, containingEditor.getMappingUrl(), containingEditor, editingBean);
	}

	@Override
	public String getRichEditorTabText() {
		return "QEXAFS Editor";
	}

	private ExpressionProvider getKProvider() {
		return new ExpressionProvider() {
			@Override
			public double getValue(double e) {
				Converter.setEdgeEnergy(getEdgeValue() / 1000.0);
				return Converter.convert(e, Converter.EV, Converter.PERANGSTROM);
			}
			@Override
			public IFieldWidget[] getPrecedents() {
				return null;
			}
		};
	}

	@SuppressWarnings("unused")
	@Override
	public void createPartControl(Composite comp) {
		ScrolledComposite scrolledComposite = new ScrolledComposite(comp, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite container = new Composite(scrolledComposite, SWT.NONE);
		container.setLayout(new BorderLayout(0, 0));
		scrolledComposite.setContent(container);

		Group grpQuickExafsParameters = new Group(container, SWT.NONE);
		grpQuickExafsParameters.setText("Quick EXAFS Parameters");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		grpQuickExafsParameters.setLayout(gridLayout);

		createElementEdgeArea(grpQuickExafsParameters);

		beanComposite = new QEXAFSParametersComposite(grpQuickExafsParameters,
				(QEXAFSParameters) editingBean, getKProvider());
		GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2);
		gridData.widthHint = 800;
		beanComposite.setLayoutData(gridData);
		beanComposite.setSize(800, 473);

		Button updateElementBtn = new Button(grpQuickExafsParameters, SWT.NONE);
		updateElementBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					getInitialEnergy().setValue(getInitialEnergyFromElement());
					getFinalEnergy().setValue(getFinalEnergyFromElement());
					getCoreHole().setValue(getCfromElement());
				} catch (Exception e1) {
					logger.error("Cannot update energies from element selection", e1);
				}
			}
		});
		updateElementBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		updateElementBtn.setText("Load defaults");
		updateElementBtn.setToolTipText("Set the initial and final energuies based on the selected element and edge");
		new Label(grpQuickExafsParameters, SWT.NONE);

		try {
			getCoreHole().setValue(getCfromElement());
		} catch (Exception e) {
			logger.error("Cannot get and set core hole", e);
		}

		beanComposite.getSpeed().on();
		beanComposite.getStepSize().on();
		beanComposite.getTime().on();
		beanComposite.getInitialEnergy().on();
		beanComposite.getFinalEnergy().on();
	}

	@Override
	public void setFocus() {
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

	public BooleanWrapper getBothWays() {
		return beanComposite.getBothWays();
	}

	protected double getInitialEnergyFromElement() throws Exception {
		Element ele = getElementUseBean();
		String edge = getEdgeUseBean();
		return ele.getInitialEnergy(edge);
	}

	protected double getFinalEnergyFromElement() throws Exception {
		Element ele = getElementUseBean();
		String edge = getEdgeUseBean();
		double fEnergy = ele.getFinalEnergy(edge);
		return fEnergy;
	}

	protected double getCfromElement() throws Exception {
		Element ele = getElementUseBean();
		String edge = getEdgeUseBean();
		return ele.getCoreHole(edge);
	}

	@Override
	protected void updateElement(ELEMENT_EVENT_TYPE type) {
		try {
			super.updateElement(type);
		} catch (Exception e) {
			logger.error("Cannot update element", e);
		}
	}

	@Override
	public void linkUI(final boolean isPageChange) {
		setPointsUpdate(false);
		try {
			setupElementAndEdge("QEXAFSParameters");
		} catch (Exception e) {
			logger.error("Could not update element list", e);
		}
		super.linkUI(isPageChange);
	}

	@Override
	public ComboWrapper getEdge() {
		return edge;
	}

	@Override
	protected void setPointsUpdate(boolean isUpdate) {
		updateValueAllowed = isUpdate;
		if (isUpdate) {
			updatePointsLabels();
			edge.on();
			element.on();
			beanComposite.getSpeed().on();
			beanComposite.getStepSize().on();
			beanComposite.getTime().on();
			beanComposite.getInitialEnergy().on();
			beanComposite.getFinalEnergy().on();
			beanComposite.getBothWays().on();
			getCoreHole().on();
			getEdgeEnergy().on();
		} else {
			edge.off();
			element.off();
			beanComposite.getInitialEnergy().off();
			beanComposite.getFinalEnergy().off();
			beanComposite.getBothWays().off();
			getCoreHole().off();
			getEdgeEnergy().off();
		}
	}
}
