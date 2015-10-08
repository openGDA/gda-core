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

package uk.ac.gda.exafs.ui.dialogs;

import gda.util.Element;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.api.reflection.IBeanController;
import org.eclipse.richbeans.api.reflection.IBeanService;
import org.eclipse.richbeans.api.widget.IFieldWidget;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.wrappers.ComboWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.ElementPosition;
import uk.ac.gda.beans.exafs.FluorescenceParameters;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.IonChamberParameters;
import uk.ac.gda.beans.exafs.TransmissionParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.beans.exafs.i20.I20SampleParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.data.ScanObject;
import uk.ac.gda.exafs.util.CancelledException;
import uk.ac.gda.exafs.util.GainBean;
import uk.ac.gda.exafs.util.GainCalculation;
import uk.ac.gda.exafs.util.IntensityException;
import uk.ac.gda.exafs.util.SmallIntensityException;

import com.swtdesigner.SWTResourceManager;

public class GainWizardPage extends WizardPage {
	private static Logger logger = LoggerFactory.getLogger(GainWizardPage.class);
	private ScaleBox referenceEdgeEnergy;
	private ScaleBox sampleEdgeEnergy;
	private ScaleBox finalEnergy;
	private ScaleBox tolerance;
	private Object referenceEdgeEnergyValue=0d;
	private Object sampleEdgeEnergyValue=0d;
	private Object finalEnergyValue=0d;
	private Button calculateButton;
	private String i0_gain,it_gain,iref_gain;
	private Text resultsLabel;
	private IBeanController control;

	public GainWizardPage(IBeanController control) {
		super("Gain Calculation");
		setTitle("Gain Calculation");
		setDescription("Configures gain on amplifiers for the ion chambers.");
		this.control = control;
	}

	@Override
	public void createControl(Composite parent) {
		final Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout());
		final Label theCalculationLooksLabel = new Label(container, SWT.WRAP);
		GridData gd_theCalculationLooksLabel = new GridData(SWT.FILL, SWT.FILL, true, false);
		theCalculationLooksLabel.setLayoutData(gd_theCalculationLooksLabel);
		Composite main = new Composite(container, SWT.BORDER);
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		main.setLayout(new GridLayout());
		Composite top = new Composite(main, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		top.setLayout(gridLayout);
		top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Label calculationExtentLabel = new Label(top, SWT.NONE);
		calculationExtentLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		calculationExtentLabel.setText("Calculation Extent");
		ComboWrapper comboWrapper = new ComboWrapper(top, SWT.READ_ONLY);
		comboWrapper.setItems(new String[] {"All ion chambers"});
		comboWrapper.select(0);
		comboWrapper.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		ExpandableComposite advancedExpandableComposite = new ExpandableComposite(main, SWT.NONE);
		advancedExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		advancedExpandableComposite.setText("Advanced");
		Composite advanced = new Composite(advancedExpandableComposite, SWT.NONE);
		advanced.setLayout(gridLayout);
		advanced.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Link finalEnergyLabel = new Link(advanced, SWT.NONE);
		finalEnergyLabel.setToolTipText("Click to take energy from scan parameters. This also happens automatically when the gain form is opened.");
		finalEnergyLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		finalEnergyLabel.setText("<a>E1</a>");
		finalEnergyLabel.setToolTipText("Final energy");
		finalEnergyLabel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getFinalEnergyValue();
			}
		});
		finalEnergy = new ScaleBox(advanced, SWT.NONE);
		finalEnergy.setMaximum(120000.0);
		finalEnergy.setUnit("eV");
		GridData gd_finalEnergy = new GridData(SWT.FILL, SWT.CENTER, true, false);
		finalEnergy.setLayoutData(gd_finalEnergy);
		finalEnergy.setValue(finalEnergyValue);
		Link sampleEdgeEnergyLabel = new Link(advanced, SWT.NONE);
		sampleEdgeEnergyLabel.setToolTipText("Click to take energy from scan parameters.  This also happens automatically when the gain form is opened.");
		GridData gd_sampleEdgeEnergyLabel = new GridData(SWT.FILL, SWT.CENTER, false, false);
		sampleEdgeEnergyLabel.setLayoutData(gd_sampleEdgeEnergyLabel);
		sampleEdgeEnergyLabel.setText("<a>E3</a>");
		sampleEdgeEnergyLabel.setToolTipText("This is 20ev below the sample edge energy.");
		sampleEdgeEnergyLabel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getSampleEdgeValue();
			}
		});
		sampleEdgeEnergy = new ScaleBox(advanced, SWT.NONE);
		GridData gd_sampleEdgeEnergy = new GridData(SWT.FILL, SWT.CENTER, true, false);
		sampleEdgeEnergy.setLayoutData(gd_sampleEdgeEnergy);
		sampleEdgeEnergy.setUnit("eV");
		sampleEdgeEnergy.setMaximum(finalEnergy);
		sampleEdgeEnergy.setValue(sampleEdgeEnergyValue);
		Link referenceEdgeEnergyLabel = new Link(advanced, SWT.NONE);
		referenceEdgeEnergyLabel.setToolTipText("Click to take energy from sample parameters. This also happens automatically when the gain form is opened.");
		GridData gd_referenceEdgeEnergyLabel = new GridData(SWT.FILL, SWT.CENTER, true, false);
		referenceEdgeEnergyLabel.setLayoutData(gd_referenceEdgeEnergyLabel);
		referenceEdgeEnergyLabel.setText("<a>E3</a>");
		referenceEdgeEnergyLabel.setToolTipText("This is 20ev below the reference edge energy.");
		referenceEdgeEnergyLabel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getReferenceEdgeSample();
			}
		});
		referenceEdgeEnergy = new ScaleBox(advanced, SWT.NONE);
		GridData gd_referenceEdgeEnergy = new GridData(SWT.FILL, SWT.CENTER, true, false);
		referenceEdgeEnergy.setLayoutData(gd_referenceEdgeEnergy);
		referenceEdgeEnergy.setUnit("eV");
		referenceEdgeEnergy.setMaximum(finalEnergy);
		referenceEdgeEnergy.setValue(referenceEdgeEnergyValue);
		Label toleranceLabel = new Label(advanced, SWT.NONE);
		toleranceLabel.setToolTipText("This is how close to the maximum intensity that the algorithm should find the gain setting for.");
		toleranceLabel.setText("Tolerance");
		tolerance = new ScaleBox(advanced, SWT.NONE);
		tolerance.setNumericValue(90);
		tolerance.setUnit("%");
		tolerance.setMinimum(1.0);
		tolerance.setMaximum(100.0);
		tolerance.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		tolerance.addValueListener(new ValueAdapter("Label updater") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				setCalculationLabelText(theCalculationLooksLabel);
			}
		});
		tolerance.on();
		ExpandableComposite detailsExpandableComposite = new ExpandableComposite(main, SWT.NONE);
		detailsExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		detailsExpandableComposite.setText("Details");
		detailsExpandableComposite.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				resultsLabel.setVisible(e.getState());
				container.layout();
			}
		});
		resultsLabel = new Text(main, SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		GridData gd_resultsLabel = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_resultsLabel.minimumHeight = 200;
		gd_resultsLabel.heightHint    = 200;
		resultsLabel.setLayoutData(gd_resultsLabel);
		resultsLabel.setText("");
		resultsLabel.setVisible(false);
		resultsLabel.setEditable(false);
		calculateButton = new Button(container, SWT.NONE);
		calculateButton.setImage(SWTResourceManager.getImage(GainWizardPage.class, "/icons/calculator_edit.png"));
		GridData gd_calculateButton = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		calculateButton.setLayoutData(gd_calculateButton);
		calculateButton.setText("Calculate");
		calculateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				calculate();
			}
		});
		advancedExpandableComposite.setClient(advanced);
		advancedExpandableComposite.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				container.layout();
			}
		});
		setCalculationLabelText(theCalculationLooksLabel);
		setControl(container);
		setPageComplete(i0_gain!=null&&it_gain!=null&&iref_gain!=null);
	}

	@SuppressWarnings({ "unchecked"})
	protected void calculate() {
		try {
			// Setup data
			final GainBean bean = new GainBean() {
				@Override
				public void updateMessage(String text) {
					if (resultsLabel!=null) {
						resultsLabel.append(text+"\n");
						resultsLabel.getParent().redraw();
					}
				}
			};
			resultsLabel.setText("");

			IBeanService service = ExafsActivator.getService(IBeanService.class);
			IBeanController mapper = service.createController(this, bean);
			mapper.uiToBean();
			bean.setCollectionTime(1000L);
			bean.setTolerance(tolerance.getNumericValue());
			bean.setLogger(logger);

			// Name of scannable
			ScanObject ob = (ScanObject) ExperimentFactory.getExperimentEditorManager().getSelectedScan();
			IScanParameters scanParams = ob.getScanParameters();
			String name       = scanParams.getScannableName();
			bean.setScannableName(name);

			// Name of amplifiers
			final List<IonChamberParameters> ionChambers;
			String type = (String) control.getBeanField("experimentType", DetectorParameters.class).getValue();
			if (type.equalsIgnoreCase("Transmission"))
				ionChambers = ((TransmissionParameters) control.getBeanField("transmissionParameters", DetectorParameters.class).getValue())
						.getIonChamberParameters();
			else if (type.equalsIgnoreCase("fluorescence"))
				ionChambers = ((FluorescenceParameters) control.getBeanField("fluorescenceParameters", DetectorParameters.class).getValue())
						.getIonChamberParameters();
			else
				throw new Exception("Cannot deal with experimentType = '"+type+"'");

			setPageComplete(true);
			getWizard().getContainer().run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Calculate Gain", 100);
					bean.setMonitor(monitor);
					try {
						monitor.worked(10);
						try {
							bean.setIonChamber(ionChambers.get(0));
							i0_gain  = GainCalculation.getSuggestedGain(bean);
						} catch (SmallIntensityException ne) {
							i0_gain = ne.getSuggestedGain();
							showMessage(ne);
						}
						monitor.worked(5);
						try {
							bean.setIonChamber(ionChambers.get(1));
							it_gain  = GainCalculation.getSuggestedGain(bean);
						} catch (SmallIntensityException ne) {
							it_gain = ne.getSuggestedGain();
							showMessage(ne);
						}
						monitor.worked(5);
						try {
							bean.setIonChamber(ionChambers.get(2));
							iref_gain  = GainCalculation.getSuggestedGain(bean);
						} catch (SmallIntensityException ne) {
							iref_gain = ne.getSuggestedGain();
							showMessage(ne);
						}
						monitor.worked(5);
					} catch (final CancelledException e) {
						return;
					} catch (final IntensityException e) {
						showMessage(e);
						return;
					} catch (Exception ne) {
						logger.error("Cannot calculate gain automatically", ne);
					} finally {
						monitor.done();
					}
					getShell().getDisplay().asyncExec(new Runnable()  {
						@Override
						public void run() {
							setPageComplete(i0_gain!=null&&it_gain!=null&&iref_gain!=null);
						}
					});
				}
			});
		} catch (Exception e) {
			logger.error("Cannot calculate gain.", e);
		}

	}

	protected void showMessage(final IntensityException e) {
		getShell().getDisplay().asyncExec(new Runnable()  {
			@Override
			public void run() {
				MessageDialog dialog = new MessageDialog(getShell(), "Gain calculation not completed.", null, // accept
				e.getMessage(), e.getType(), new String[] { IDialogConstants.OK_LABEL }, 0); // ok
				dialog.open();
			}
		});
	}

	private void setCalculationLabelText(Label label) {
		double val = tolerance.getNumericValue();
		if (Double.isNaN(val)) {
			label.setText("Error, the tolerance is blank but should be between 1 and 100");
			calculateButton.setEnabled(false);
		}
		else {
			label.setText("The calculation looks at the intensity for each ion chamber based on the scan parameters and attempts to adjust gain until intensity is ~"+val+"% of the detector maximum.");
			calculateButton.setEnabled(true);
		}
	}

	@SuppressWarnings("unchecked")
	protected void getReferenceEdgeSample() {
		try {
			int pos = (Integer) ExperimentFactory.getExperimentEditorManager().getValueFromUIOrBean("sampleWheelPosition", control, I20SampleParameters.class);
			List<ElementPosition> elePos = (List<ElementPosition>) ExperimentFactory.getExperimentEditorManager().getValueFromUIOrBean("elementPositions",
					control, I20SampleParameters.class);
			for (ElementPosition elementPosition : elePos) {
				if (elementPosition.getWheelPosition()==pos) {
					Element refElement = Element.getElement(elementPosition.getPrincipleElement());
					if (refElement!=null) {
						this.referenceEdgeEnergyValue = (refElement.getEdgeEnergy("K")) - 20d;
						if (referenceEdgeEnergy!=null)
							this.referenceEdgeEnergy.setValue(referenceEdgeEnergyValue);
					}
				}
			}
		} catch (Exception ne) {
			logger.error("Cannot get reference edge energy", ne);
		}
	}

	@SuppressWarnings("unchecked")
	protected void getFinalEnergyValue() {
		try {
			finalEnergyValue = ExperimentFactory.getExperimentEditorManager().getValueFromUIOrBean("finalEnergy", control, XasScanParameters.class,
					XanesScanParameters.class);
			if (finalEnergy!=null) this.finalEnergy.setValue(finalEnergyValue);
		} catch (Exception ne) {
			logger.error("Cannot get final energy", ne);
		}
	}

	protected void getSampleEdgeValue() {
		try {
			@SuppressWarnings("unchecked")
			IFieldWidget ui = control.getBeanField("edgeEnergy", XasScanParameters.class, XanesScanParameters.class);
			if (ui!=null) {
				sampleEdgeEnergyValue = ui.getValue();
			} else {
				try {
					final ScanObject ob = (ScanObject) ExperimentFactory.getExperimentEditorManager().getSelectedScan();
					if (ob != null) {
						String element, edge;
						Object params = ob.getScanParameters();
						if (params instanceof XanesScanParameters) {
							element = ((XanesScanParameters)params).getElement();
							edge    = ((XanesScanParameters)params).getEdge();
						}
						else {
							element = ((XasScanParameters)params).getElement();
							edge    = ((XasScanParameters)params).getEdge();
						}
						Element ele = Element.getElement(element);
						sampleEdgeEnergyValue = new Double(ele.getEdgeEnergy(edge)) - 20d;
					}
				} catch (Exception ne) {
					logger.error("Cannot get edge energy for element.", ne);
				}
			}
			if (sampleEdgeEnergy!=null)
				this.sampleEdgeEnergy.setValue(sampleEdgeEnergyValue);
		} catch (Exception ne) {
			logger.error("Cannot get sample edge energy", ne);
		}
	}

	/**
	 * @return Returns the referenceEdgeEnergy.
	 */
	public ScaleBox getReferenceEdgeEnergy() {
		return referenceEdgeEnergy;
	}

	/**
	 * @return Returns the sampleEdgeEnergy.
	 */
	public ScaleBox getSampleEdgeEnergy() {
		return sampleEdgeEnergy;
	}

	/**
	 * @return Returns the finalEnergy.
	 */
	public ScaleBox getFinalEnergy() {
		return finalEnergy;
	}

	/**
	 * @return Returns the tolerance.
	 */
	public ScaleBox getTolerance() {
		return tolerance;
	}

	/**
	 * @return Returns the i0_gain.
	 */
	public String getI0_gain() {
		return i0_gain;
	}

	/**
	 * @return Returns the it_gain.
	 */
	public String getIt_gain() {
		return it_gain;
	}

	/**
	 * @return Returns the iref_gain.
	 */
	public String getIref_gain() {
		return iref_gain;
	}

}