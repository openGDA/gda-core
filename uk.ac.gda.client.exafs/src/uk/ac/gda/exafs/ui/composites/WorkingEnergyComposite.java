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

package uk.ac.gda.exafs.ui.composites;

import gda.util.Element;

import org.eclipse.swt.SWT;
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

import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.FluorescenceParameters;
import uk.ac.gda.beans.exafs.QEXAFSParameters;
import uk.ac.gda.beans.exafs.TransmissionParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.beans.exafs.XesScanParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.exafs.ui.data.ScanObject;
import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.richbeans.components.FieldBeanComposite;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.selector.VerticalListEditor;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;

public class WorkingEnergyComposite extends FieldBeanComposite {

	protected VerticalListEditor diodeParameters;

	private final static Logger logger = LoggerFactory.getLogger(WorkingEnergyComposite.class);

	private DetectorParameters provider;

	public WorkingEnergyComposite(Composite parent, int style, DetectorParameters abean) {
		super(parent, style);
		provider = abean;
	}

	protected ScaleBox workingEnergy;
	private Button workingEnergyBtn;
	Button selectDefaultsBtn;
	private SelectionAdapter workingEnergyListener;
	SelectionAdapter selectDefaultsListener;
	private Group tabFolder;
	QEXAFSParameters qexafsBean;
	XasScanParameters xasBean;
	XanesScanParameters xanesBean;

	

	protected void createEdgeEnergy(Composite comp) {

		Label workingELbl = new Label(comp, SWT.NONE);
		workingELbl.setText("Calculate ion chamber gas filling for energy:");

		Composite workingEComp = new Composite(comp, SWT.NONE);
		GridLayout glWorkingE = new GridLayout(3, false);
		glWorkingE.marginHeight = 0;
		glWorkingE.marginWidth = 0;
		workingEComp.setLayout(glWorkingE);
		GridData gdWorkingE = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gdWorkingE.widthHint = 490;
		workingEComp.setLayoutData(gdWorkingE);

		workingEnergy = new ScaleBox(workingEComp, SWT.NONE);
		workingEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		workingEnergy.setMaximum(20000.0);
		workingEnergy.setUnit("eV");
		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd.widthHint = 120;
		workingEnergy.setLayoutData(gd);

		this.workingEnergyBtn = new Button(workingEComp, SWT.NONE);
		workingEnergyBtn.setText("Get Energy From Scan");
		workingEnergyBtn.setToolTipText("Click to take edge energy from currently open scan parameters.");
		this.workingEnergyListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setWorkingFromEdge();
			}
		};
		workingEnergyBtn.addSelectionListener(workingEnergyListener);

		this.selectDefaultsBtn = new Button(workingEComp, SWT.NONE);
		selectDefaultsBtn.setText("Set Default Gas Mixtures");
		selectDefaultsBtn.setToolTipText("Click to set ion chamber gas types to defaults.");
		

		IExperimentObject selectedScan = ExperimentFactory.getExperimentEditorManager().getSelectedScan();

		// no point making any connections if no scan has been selected
		if (selectedScan == null) {
			return;
		}

		if (selectedScan instanceof ScanObject) {
			final ScanObject runOb = (ScanObject) selectedScan;
			String location = runOb.getScanFile().getLocation().toString();
			try {
				if (runOb.isQexafs() && provider != null) {
					try {
						qexafsBean = QEXAFSParameters.createFromXML(location);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					workingEnergy.setMaximum(qexafsBean.getFinalEnergy());
					workingEnergy.setMinimum(qexafsBean.getInitialEnergy());
					double init = qexafsBean.getInitialEnergy();
					double fin = qexafsBean.getFinalEnergy();
					String energy = String.valueOf(init + ((fin - init) / 2.0));
					workingEnergy.setValue(energy);
					if (runOb.getDetectorParameters().getExperimentType().equals("Transmission"))
						provider.getTransmissionParameters().setWorkingEnergy(Double.parseDouble(energy));
					else
						provider.getFluorescenceParameters().setWorkingEnergy(Double.parseDouble(energy));
				} else if (runOb.isXes() && provider != null) {
					workingEnergy.setMaximum(runOb.getFinalEnergy(), "FinalEnergy", XesScanParameters.class);
					workingEnergy.setMinimum(runOb.getInitialEnergy(), "InitialEnergy", XesScanParameters.class);
				}

				else if (runOb.isXas()) {
					xasBean = XasScanParameters.createFromXML(location);
					workingEnergy.setMaximum(xasBean.getFinalEnergy());
					workingEnergy.setMinimum(xasBean.getInitialEnergy());

					String element = xasBean.getElement();
					String edge = xasBean.getEdge();
					Element ele = Element.getElement(element);
					workingEnergy.setValue(ele.getEdgeEnergy(edge));
					String expType = runOb.getDetectorParameters().getExperimentType();
					if (expType.equals("Transmission") && provider != null) {
						TransmissionParameters tp = provider.getTransmissionParameters();
						double edgeEnergy = ele.getEdgeEnergy(edge);
						tp.setWorkingEnergy(edgeEnergy);
					} else if (provider != null) {
						FluorescenceParameters fp = provider.getFluorescenceParameters();
						double edgeEnergy = ele.getEdgeEnergy(edge);
						fp.setWorkingEnergy(edgeEnergy);
					}
				}

				else if (runOb.isXanes()) {
					try {
						xanesBean = XanesScanParameters.createFromXML(location);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					workingEnergy.setMaximum(xanesBean.getFinalEnergy());
					workingEnergy.setMinimum(xanesBean.getRegions().get(0).getEnergy());

					String element = xanesBean.getElement();
					String edge = xanesBean.getEdge();
					Element ele = Element.getElement(element);
					workingEnergy.setValue(ele.getEdgeEnergy(edge));
					if (runOb.getDetectorParameters().getExperimentType().equals("Transmission") && provider != null)
						provider.getTransmissionParameters().setWorkingEnergy(ele.getEdgeEnergy(edge));
					else if (provider != null)
						provider.getFluorescenceParameters().setWorkingEnergy(ele.getEdgeEnergy(edge));

				}

				else if (!runOb.isMicroFocus() && provider != null) {
					workingEnergy.setMaximum(runOb.getFinalEnergy(), "FinalEnergy", XasScanParameters.class);
					workingEnergy.setMinimum(runOb.getInitialEnergy(), "InitialEnergy", XasScanParameters.class);
				} else if (!runOb.isMicroFocus() && provider != null) {
					workingEnergy.setMaximum(runOb.getFinalEnergy(), "FinalEnergy", XanesScanParameters.class);
					workingEnergy.setMinimum(runOb.getInitialEnergy(), "InitialEnergy", XanesScanParameters.class);
				}

			} catch (Exception ne) {
				// If you get this exception, please file details in GDA-3404
				logger.error("Cannot connect bounds for working energy (see GDA-3404)", ne);
			}
		}

		try {
			BeanUI.addBeanFieldValueListener(XasScanParameters.class, "EdgeEnergy", new ValueAdapter(
					"EdgeEnergyListener") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					if(!workingEnergy.isDisposed())
					workingEnergy.setValue(e.getValue());
				}
			});
			BeanUI.addBeanFieldValueListener(XanesScanParameters.class, "EdgeEnergy", new ValueAdapter(
					"EdgeEnergyListener") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					if(!workingEnergy.isDisposed())
					workingEnergy.setValue(e.getValue());
				}
			});
			BeanUI.addBeanFieldValueListener(QEXAFSParameters.class, "EdgeEnergy", new ValueAdapter(
					"EdgeEnergyListener") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					if(!workingEnergy.isDisposed())
					workingEnergy.setValue(e.getValue());
				}
			});

		} catch (Exception ne) {
			logger.error("Cannot add EdgeEnergy listeners.", ne);
		}
	}

	protected Group getTabFolder() {
		if (tabFolder == null) {
			tabFolder = new Group(this, SWT.NONE);
			tabFolder.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));
			final GridLayout gridLayout_1 = new GridLayout();
			gridLayout_1.numColumns = 1;
			tabFolder.setLayout(gridLayout_1);
		}
		return tabFolder;
	}

	protected void setWorkingFromEdge() {
		try {
			final ScanObject ob = (ScanObject) ExperimentFactory.getExperimentEditorManager().getSelectedScan();

			if (ob != null) {
				final String element, edge;
				final Object params = ob.getScanParameters();
				if (params instanceof XanesScanParameters) {
					element = ((XanesScanParameters) params).getElement();
					edge = ((XanesScanParameters) params).getEdge();
					final Element ele = Element.getElement(element);
					workingEnergy.setMinimum(((XanesScanParameters) params).getRegions().get(0).getEnergy());
					workingEnergy.setMaximum(ele.getFinalEnergy(edge));
					if (Math.floor(ele.getEdgeEnergy(edge)) == ele.getEdgeEnergy(edge))
						workingEnergy.setValue((int) ele.getEdgeEnergy(edge));
					else
						workingEnergy.setValue(ele.getEdgeEnergy(edge));
				} else if (params instanceof XasScanParameters) {
					element = ((XasScanParameters) params).getElement();
					edge = ((XasScanParameters) params).getEdge();
					final Element ele = Element.getElement(element);
					workingEnergy.setMinimum(ele.getInitialEnergy(edge));
					workingEnergy.setMaximum(ele.getFinalEnergy(edge));
					if (Math.floor(ele.getEdgeEnergy(edge)) == ele.getEdgeEnergy(edge))
						workingEnergy.setValue((int) ele.getEdgeEnergy(edge));
					else
						workingEnergy.setValue(ele.getEdgeEnergy(edge));
				} else if (params instanceof XesScanParameters) {
					XesScanParameters xesparams = (XesScanParameters)params;
					element = "";
					double init = xesparams.getXesInitialEnergy();
					double fin = xesparams.getXesFinalEnergy();
					String energy = String.valueOf(init + ((fin - init) / 2.0));
					workingEnergy.setMinimum(init);
					workingEnergy.setMaximum(fin);
					workingEnergy.setValue(energy);
				} else {
					element = "";
					double init = ((QEXAFSParameters) params).getInitialEnergy();
					double fin = ((QEXAFSParameters) params).getFinalEnergy();
					double energy = init + ((fin - init) / 2.0);
					workingEnergy.setMinimum(init);
					workingEnergy.setMaximum(fin);
					if (Math.floor(energy) == energy)
						workingEnergy.setValue((int) energy);
					else
						workingEnergy.setValue(energy);
				}
			}
		} catch (Exception ne) {
			logger.error("Cannot get edge energy for element.", ne);
		}
	}

	public ScaleBox getWorkingEnergy() {
		return workingEnergy;
	}

	public VerticalListEditor getDiodeParameters() {
		return diodeParameters;
	}

	@Override
	public void dispose() {
		if (workingEnergyBtn != null && !workingEnergyBtn.isDisposed()) {
			workingEnergyBtn.removeSelectionListener(workingEnergyListener);
		}
		super.dispose();
	}
}
