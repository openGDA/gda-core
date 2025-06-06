/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.gasrig.ui;

import java.text.DecimalFormat;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.gasrig.api.GasRigException;
import uk.ac.diamond.daq.gasrig.api.GasRigSequenceUpdate;
import uk.ac.diamond.daq.gasrig.api.IGasMix;
import uk.ac.diamond.daq.gasrig.api.IGasRig;
import uk.ac.diamond.daq.gasrig.ui.viewmodels.CabinetViewModel;
import uk.ac.diamond.daq.gasrig.ui.viewmodels.GasFlowViewModel;
import uk.ac.diamond.daq.gasrig.ui.viewmodels.GasMixViewModel;
import uk.ac.diamond.daq.gasrig.ui.viewmodels.GasRigViewModel;
import uk.ac.diamond.daq.gasrig.ui.viewmodels.GasViewModel;
import uk.ac.gda.client.livecontrol.ScannableValueControl;

public class GasRigControls implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(GasRigControls.class);

	private static final String TWO_DECIMAL_PLACES = "#0.00";
	private static final String TWO_DECIMAL_PLACES_PERCENT = "#0.00' %'";
	private static final String TWO_DECIMAL_PLACES_MBAR = "#0.00 'mbar'";

	private static final String NORMALISED_FLOW_TOOLTIP = "= Mass Max Flow * √Molar Mass / Pressure";
	private static final String MASS_FLOW_TOOLTIP = "= Max Total Weighted Flow * (Pressure / Total Pressure) * (1 / √Molar Mass)";
	private static final String MAXIMUM_TOTAL_WEIGHTED_FLOW_TOOLTIP = "= MIN(Total Weighted Flow Limit, Lowest Normalised Flow Rate * Total Pressure";

	private int columns_per_gas = 4;
	private static final int COLUMNS_PER_LINE = 4;

	private GasRigViewModel gasRigViewModel;
	private IGasRig gasRig;

	private int numberOfGasListColumns;
	private int numberOfGasMixes;

	private Composite mainComposite;
	private Composite gasList;
	private Composite debugPanel;
	private Composite lowerPanel;
	private Composite upperPanel;

	private Text sequenceName;
	private Text sequenceStatus;
	private ProgressBar sequenceProgress;

	private Button endstationLine1Button;
	private Button endstationLine2Button;
	private Button exhaustLine1Button;
	private Button exhaustLine2Button;

	private Button fillLine1Button;
	private Button fillLine2Button;
	private Button emptyLine1Button;
	private Button emptyLine2Button;

	private boolean minimalGuiMode;


	private DataBindingContext bindingContext = new DataBindingContext();

	@PostConstruct
	public void postConstruct(Composite parent) {

		try {
			gasRig = Finder.findOptionalSingleton(IGasRig.class).orElseThrow(() -> new GasRigException("No gas rig found in configuration"));
			gasRigViewModel = new GasRigViewModel(gasRig);
		} catch (GasRigException exception) {
			showError(exception.getMessage());
			return;
		}

		mainComposite = parent;
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(mainComposite);

		upperPanel = new Composite(mainComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(upperPanel);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(upperPanel);

		gasList = new Composite(upperPanel, SWT.BORDER);
		GridDataFactory.fillDefaults()
			.grab(true, true)
			.align(SWT.FILL, SWT.FILL)
			.applyTo(gasList);

		numberOfGasMixes = gasRigViewModel.getNumberOfMixes();
		numberOfGasListColumns = columns_per_gas + (COLUMNS_PER_LINE * numberOfGasMixes);
		GridLayoutFactory.fillDefaults().numColumns(numberOfGasListColumns).applyTo(gasList);

		addHeadingsToGasList();
		addGasesToGasList();
		addTotalRowToGasList();
		addDebugSection();
		lowerPanel = new Composite(mainComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(4).applyTo(lowerPanel);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.BEGINNING, SWT.FILL).applyTo(lowerPanel);
		addGasRigControls();
		addSequenceProgress();
		addEndstationEnvironmentMonitors();
		addPerspectiveCustomizationPanel();

		gasRig.addIObserver(this);
	}

	private void addSequenceProgress() {

		Composite seqProgressPanel = new Composite(lowerPanel, SWT.BORDER);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(seqProgressPanel);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.BEGINNING, SWT.FILL).applyTo(seqProgressPanel);

		addLabel(seqProgressPanel, "Sequence Monitoring", span(2), true);

		addLabel(seqProgressPanel, "Current/Last Sequence", span(1), false);
		sequenceName = addUneditableUnboundTextBox(seqProgressPanel, span(1), "");

		addLabel(seqProgressPanel, "Sequence Status", span(1), false);
		sequenceStatus = addUneditableUnboundTextBox(seqProgressPanel, span(1), "");

		addLabel(seqProgressPanel, "Sequence Progress", span(1), false);
		sequenceProgress = new ProgressBar(seqProgressPanel, SWT.HORIZONTAL);
		sequenceProgress.setMaximum(100);
	}

	private void addPerspectiveCustomizationPanel() {
		Composite customizationComposite = new Composite(lowerPanel, SWT.BORDER);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(customizationComposite);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(customizationComposite);
		addLabel(customizationComposite, "Perspective", span(1), true);
		Button viewModeButton = new Button(customizationComposite, SWT.TOGGLE);
		viewModeButton.setText("Min");
		viewModeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (viewModeButton.getSelection()) {
					for(Control control : gasList.getChildren()) {
						control.dispose();
					}
					columns_per_gas = 2;
					numberOfGasListColumns = columns_per_gas + (COLUMNS_PER_LINE * numberOfGasMixes);
					GridLayoutFactory.fillDefaults().numColumns(numberOfGasListColumns).applyTo(gasList);
					debugPanel.dispose();
					minimalGuiMode = true;
					addHeadingsToGasList();
					addGasesToGasList();
					addTotalRowToGasList();
					gasList.requestLayout();
					viewModeButton.setText("Max");
				} else {
					for(Control control : gasList.getChildren()) {
						control.dispose();
					}
					columns_per_gas = 4;
					numberOfGasListColumns = columns_per_gas + (COLUMNS_PER_LINE * numberOfGasMixes);
					GridLayoutFactory.fillDefaults().numColumns(numberOfGasListColumns).applyTo(gasList);
					addDebugSection();
					minimalGuiMode = false;
					addHeadingsToGasList();
					addGasesToGasList();
					addTotalRowToGasList();
					gasList.requestLayout();
					viewModeButton.setText("Min");
				}
			}
		});
	}

	private void addHeadingsToGasList() {
		addLabel(gasList, "", span(columns_per_gas), false);
		IntStream.rangeClosed(1, numberOfGasMixes).forEach(line -> addLabel(gasList, "Line " + line, span(COLUMNS_PER_LINE), true, 14));

		addLabel(gasList, "Gases", span(1), true, 14);
		addLabel(gasList, "Current Mass Flow", span(1), false);
		if(!minimalGuiMode) {
			addLabel(gasList, "Max Mass Flow", span(1), false);
			addLabel(gasList, "Molar Mass", span(1), false);
		}

		for (int i = 0; i < numberOfGasMixes; i++ ) {
			addLabel(gasList, "Pressure (mbar)", span(1), false);
			addLabel(gasList, "Composition %", span(1), false);
			addLabel(gasList, "Normalised Flow", span(1), false);
			addLabel(gasList, "Mass Flow", span(1), false);
		}
	}

	private void addGasesToGasList() {
		gasRigViewModel.getNonCabinetGases().forEach(this::addGasToGasList);
		gasRigViewModel.getCabinets().forEach(this::addCabinetToGasList);
	}

	private void addCabinetToGasList(CabinetViewModel cabinet) {
		if (!cabinet.getGases().isEmpty()) {
			addLabel(gasList, cabinet.getName(), span(numberOfGasListColumns), true, 14);
			cabinet.getGases().forEach(this::addGasToGasList);
		}
	}

	private void addGasToGasList(GasViewModel gas) {

		addLabel(gasList, gas.getName(), hint(75), false, 14);
		if(!gasRig.isRemoveLiveControls()) {
			addMassFlowLiveControlToGasList(gas);
		} else {
			addLabel(gasList, "", span(1), true, 14);
		}
		if(!minimalGuiMode) {
			addUneditableUnboundTextBox(gasList, hint(75), String.valueOf(gas.getMaxMassFlow()));
			addUneditableUnboundTextBox(gasList, hint(75), String.valueOf(gas.getMolarMass()));
		}
		addMixControlsToGasList(gas);
	}

	private void addMassFlowLiveControlToGasList(GasViewModel gas) {
		ScannableValueControl control = new ScannableValueControl();
		control.setScannableName(gas.getMassFlowScannableName());
		control.setDisplayName("");
		control.setReadOnly(true);
		control.setTextWidth(80);
		control.createControl(gasList);
	}

	private void addLiveControl(Composite parent, String name) {
		ScannableValueControl control = new ScannableValueControl();
		control.setScannableName(name);
		control.setDisplayName("");
		control.setReadOnly(true);
		control.setTextWidth(100);
		control.createControl(parent);
	}

	private void addMixControlsToGasList(GasViewModel gas) {

		try {
			for (GasMixViewModel gasMix : gasRigViewModel.getGasMixes()) {
				GasFlowViewModel gasFlow;

				gasFlow = gasMix.getGasFlowViewModelByGasId(gas.getId());

				addTwoWayBoundSpinner(gasList, GasFlowViewModel.class, gasFlow, GasFlowViewModel.PRESSURE, 99.99, span(1));
				addOneWayBoundDecimalTextBox(gasList, GasFlowViewModel.class, gasFlow, GasFlowViewModel.PRESSURE_PERCENTAGE, TWO_DECIMAL_PLACES_PERCENT, hint(75), false);
				addOneWayBoundDecimalTextBox(gasList, GasFlowViewModel.class, gasFlow, GasFlowViewModel.NORMALISED_FLOW_RATE, TWO_DECIMAL_PLACES, hint(75), false, NORMALISED_FLOW_TOOLTIP);
				addOneWayBoundDecimalTextBox(gasList, GasFlowViewModel.class, gasFlow, GasFlowViewModel.MASS_FLOW, TWO_DECIMAL_PLACES, hint(75), false, MASS_FLOW_TOOLTIP);
			}
		} catch (GasRigException exception) {
			String error = "Unable to find gas flow for gas id " + gas.getId() + ". Gas rig is misconfigured. Please notify GDA support.";
			logger.error(error, exception);
			showError(error);
		}
	}

	private void addTotalRowToGasList() {
		addLabel(gasList, "Totals", span(columns_per_gas), true, 14);

		for (GasMixViewModel gasMix : gasRigViewModel.getGasMixes()) {
			addOneWayBoundDecimalTextBox(gasList, GasMixViewModel.class, gasMix, GasMixViewModel.TOTAL_PRESSURE, TWO_DECIMAL_PLACES_MBAR, spanAndHint(COLUMNS_PER_LINE - 1, 75), true);
			addOneWayBoundDecimalTextBox(gasList, GasMixViewModel.class, gasMix, GasMixViewModel.TOTAL_MASS_FLOW, TWO_DECIMAL_PLACES, spanAndHint(1, 75), true);
		}
	}

	private void addGasRigControls() {
		Composite gasRigControlPanel = new Composite(lowerPanel, SWT.BORDER);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(gasRigControlPanel);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.BEGINNING, SWT.FILL).applyTo(gasRigControlPanel);

		Composite fillAndEmptyPart =  new Composite(gasRigControlPanel, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(fillAndEmptyPart);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(fillAndEmptyPart);

		addLabel(fillAndEmptyPart, "", spanAndHint(1, 50), true, 14);
		addLabel(fillAndEmptyPart, "Fill", spanAndHint(1, 40), true, 14);
		addLabel(fillAndEmptyPart, "Empty", spanAndHint(1, 60), true, 14);

		for (GasMixViewModel gasMix : gasRigViewModel.getGasMixes()) {
			addFillOrEmptyLineComposite(fillAndEmptyPart, "Line "+ gasMix.getLineNumber(), gasMix.getLineNumber());
		}

		addLabel(fillAndEmptyPart, "", span(1), true, 14);
		Button updateButton2 = new Button(fillAndEmptyPart, SWT.PUSH);
		updateButton2.setText("UPDATE 2");
		updateButton2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				updateButterflyValvePressureOrPosition();
				fillOrEmptyLines();
			}
		});
		span(2).applyTo(updateButton2);

		Composite endstationAndExhaustPart =  new Composite(gasRigControlPanel, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(endstationAndExhaustPart);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(endstationAndExhaustPart);

		addLabel(endstationAndExhaustPart, "", span(1), true, 14);
		addLabel(endstationAndExhaustPart, "ES", span(1), true, 14);
		addLabel(endstationAndExhaustPart, "Exhaust", span(1), true, 14);

		for (GasMixViewModel gasMix : gasRigViewModel.getGasMixes()) {
			addEndstationOrExhaustCompositeForLine(endstationAndExhaustPart, gasMix.getLineNumber(), span(3));
		}

		Button updateButton1 = new Button(endstationAndExhaustPart, SWT.PUSH);
		updateButton1.setText("UPDATE 1");
		updateButton1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				updateButterflyValvePressureOrPosition();
				linesToEndstationOrExhaust();
			}
		});
		span(3).applyTo(updateButton1);

		Composite restOfButtonsPart =  new Composite(gasRigControlPanel, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(restOfButtonsPart);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(restOfButtonsPart);

		final String EVACUATE_ENDSTATION = "EVACUATE ENDSTATION";
		final String INITIALISE = "INITIALISE";
		final String ZERO_ALL_FLOWS = "ZERO ALL FLOWS";
		final String STOP_SEQUENCE = "STOP SEQUENCE";

		String[] sequenceLabels = new String[] {INITIALISE, EVACUATE_ENDSTATION, ZERO_ALL_FLOWS, STOP_SEQUENCE};
		for (String label : sequenceLabels) {
			Button seqButton = new Button(restOfButtonsPart, SWT.PUSH);
			seqButton.setText(label);
			seqButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					if(label.equals(INITIALISE)) {
						try {
							gasRig.initialise();
						} catch(DeviceException e) {
							logger.debug(e.getMessage());
						}

					} else if(label.equals(EVACUATE_ENDSTATION)) {
						try {
							gasRig.evacuateEndStation();
						} catch(GasRigException e) {
							logger.debug(e.getMessage());
						}
					} else if(label.equals(ZERO_ALL_FLOWS)) {
						try {
							gasRig.setAllGasFlowsToZero(1);
						} catch(DeviceException e) {
							logger.debug("Problem zeroing gas flows- {}", e.getMessage());
						}
					} else if(label.equals(STOP_SEQUENCE)) {
						try {
							gasRig.stopCurrentSequence();
						} catch(DeviceException e) {
							logger.debug("Problem stopping sequence- {}", e.getMessage());
						}
					}
				}
			});
		}
	}

	private void linesToEndstationOrExhaust() {
		if(endstationLine1Button.getSelection() && endstationLine2Button.getSelection()) {
			Async.execute(()-> {
				try {
					gasRig.admitLinesToEndstation();
				} catch (GasRigException e) {
					unselectButtons(endstationLine1Button, endstationLine2Button);
					showError(e.getMessage());
				}
			});
		} else if(exhaustLine1Button.getSelection() && exhaustLine2Button.getSelection()) {
			Async.execute(()-> {
				try {
					gasRig.admitLinesToExhaust();
				} catch (GasRigException e) {
					unselectButtons(exhaustLine1Button, exhaustLine2Button);
					showError(e.getMessage());
				}
			});
		} else if(endstationLine1Button.getSelection()) {
			Async.execute(()-> {
				try {
					gasRig.admitLineToEndStation(1);
				} catch (GasRigException e) {
					unselectButtons(endstationLine1Button, exhaustLine2Button);
					showError(e.getMessage());
				}
			});
		} else if(endstationLine2Button.getSelection()) {
			Async.execute(()-> {
				try {
					gasRig.admitLineToEndStation(2);
				} catch (GasRigException e) {
					unselectButtons(endstationLine2Button, exhaustLine1Button);
					showError(e.getMessage());
				}
			});
		}
	}

	private void fillOrEmptyLines() {
		IGasMix requestedGasMix1 = gasRigViewModel.getGasMixes().get(0).getGasMix();
		IGasMix requestedGasMix2 = gasRigViewModel.getGasMixes().get(1).getGasMix();

		if(fillLine1Button.getSelection() && fillLine2Button.getSelection()) {
			Async.execute(()-> {
				try {
					gasRig.configureGasMixForLine(requestedGasMix1, 1);
					gasRig.configureGasMixForLine(requestedGasMix2, 2);
					gasRig.settleUnusedGases(requestedGasMix1, requestedGasMix2);
				} catch (GasRigException | DeviceException e) {
					showError(e.getMessage());
				}
			});
		} else if(fillLine1Button.getSelection() && emptyLine2Button.getSelection()) {
			Async.execute(()-> {
				try {
					gasRig.configureGasMixForLine(requestedGasMix1, 1);
					gasRig.evacuateLine(2);
				} catch (GasRigException | DeviceException e) {
					logger.debug("Probably a timeout exception, so silently log - {}", e.getMessage());
				}
			});
		} else if(fillLine2Button.getSelection() && emptyLine1Button.getSelection()) {
			Async.execute(()-> {
				try {
					gasRig.configureGasMixForLine(requestedGasMix2, 2);
					gasRig.evacuateLine(1);
				} catch (GasRigException | DeviceException e) {
					logger.debug("Probably a timeout exception, so silently log - {}", e.getMessage());
				}
			});
		} else if(emptyLine1Button.getSelection() && emptyLine2Button.getSelection()) {
			Async.execute(()-> {
				try {
					gasRig.evacuateLines();
				} catch (DeviceException | GasRigException e) {
					showError(e.getMessage());
				}
			});
		}
	}

	private void addEndstationEnvironmentMonitors() {
		Composite endstationEnvironmentMonitoringPanel = new Composite(lowerPanel, SWT.BORDER);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(endstationEnvironmentMonitoringPanel);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(endstationEnvironmentMonitoringPanel);
		addLabel(endstationEnvironmentMonitoringPanel, "Actual pressure CAP3", span(1), true, 14);
		addLiveControl(endstationEnvironmentMonitoringPanel, "cap03c");
		addLabel(endstationEnvironmentMonitoringPanel, "Actual pressure CAP4", span(1), true, 14);
		addLiveControl(endstationEnvironmentMonitoringPanel, "cap04c");
		addLabel(endstationEnvironmentMonitoringPanel, "V92 position", span(1), true, 14);
		addLiveControl(endstationEnvironmentMonitoringPanel, "gr_butterfly_valve_position");
		addLabel(endstationEnvironmentMonitoringPanel, "Sample temperature", span(1), true, 14);
		addLiveControl(endstationEnvironmentMonitoringPanel, "gr_sample_temp");
	}

	private void addDebugSection() {

		debugPanel = new Composite(upperPanel, SWT.BORDER);

		GridDataFactory.fillDefaults()
			.grab(true, true)
			.align(SWT.FILL, SWT.FILL)
			.applyTo(debugPanel);

		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(debugPanel);


		addLabel(debugPanel, "Debug", span(2), true, 14);

		for (GasMixViewModel gasMix : gasRigViewModel.getGasMixes()) {
			addLabel(debugPanel, "Line " + gasMix.getLineNumber(), span(2), true);

			addLabel(debugPanel, "Lowest Normalised Flow Rate", span(1), false);
			addOneWayBoundDecimalTextBox(debugPanel, GasMixViewModel.class, gasMix, GasMixViewModel.LOWEST_NORMALISED_FLOW_RATE, TWO_DECIMAL_PLACES, spanAndHint(1, 75), false);

			addLabel(debugPanel, "Lowest Normalised Flow Rate * Total Pressure", span(1), false);
			addOneWayBoundDecimalTextBox(debugPanel, GasMixViewModel.class, gasMix, GasMixViewModel.LOWEST_WEIGHTED_FLOW, TWO_DECIMAL_PLACES, spanAndHint(1, 75), false);

			addLabel(debugPanel, "Total Weighted Flow Limit", span(1), false);
			addTwoWayBoundSpinner(debugPanel, GasMixViewModel.class, gasMix, GasMixViewModel.TOTAL_WEIGHTED_FLOW_LIMIT, 1000, span(1));

			addLabel(debugPanel, "Maximum Total Weighted Flow", span(1), false);
			addOneWayBoundDecimalTextBox(debugPanel, GasMixViewModel.class, gasMix, GasMixViewModel.MAXIMUM_TOTAL_WEIGHTED_FLOW, TWO_DECIMAL_PLACES, spanAndHint(1, 75), false, MAXIMUM_TOTAL_WEIGHTED_FLOW_TOOLTIP);
		}
	}

	@Override
	public void update(Object source, Object arg) {
		logger.info("Received update" + arg);

		if (arg instanceof GasRigSequenceUpdate) {
			var sequenceUpdate = (GasRigSequenceUpdate)arg;
			String name = sequenceUpdate.getName();
			String status = sequenceUpdate.getStatus();
			Display.getDefault().asyncExec(() -> {
				if (name.equals("Initialise") && status.equals("Not running")) {
					handleRadioButtons(exhaustLine1Button, endstationLine1Button);
					handleRadioButtons(exhaustLine2Button, endstationLine2Button);
				}
				sequenceName.setText(name);
				sequenceStatus.setText(status);
				sequenceProgress.setSelection((int)sequenceUpdate.getPercentComplete());
			});
		}
	}

	private Label addLabel(Composite parent, String labelText, GridDataFactory layout, boolean bold, int fontSize) {
		Label label = addLabel(parent, labelText, layout, bold);
		setLabelFontSize(label, fontSize);

		return label;
	}

	private Label addLabel(Composite parent, String labelText, GridDataFactory layout, boolean bold) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(labelText);

		if (bold) {
			setControlBold(label);
		}

		layout.applyTo(label);

		return label;
	}

	private void addFillOrEmptyLineComposite(Composite parent, String labelName, int lineNumber) {
		Composite lineComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(lineComposite);
		addLabel(lineComposite, labelName, span(1), true, 14);
		Button fillButton = new Button(lineComposite, SWT.RADIO);
		spanAndHint(1,50).applyTo(fillButton);
		if (lineNumber == 1) {
			fillLine1Button = fillButton;
		} else if (lineNumber == 2) {
			fillLine2Button = fillButton;
		}

		Button emptyButton = new Button(lineComposite, SWT.RADIO);
		spanAndHint(1,50).applyTo(emptyButton);
		if (lineNumber == 1) {
			emptyLine1Button = emptyButton;
		} else if (lineNumber == 2) {
			emptyLine2Button = emptyButton;
		}
		span(3).applyTo(lineComposite);
	}

	private Composite addEndstationOrExhaustCompositeForLine(Composite parent, int lineNumber, GridDataFactory layout) {
		Composite lineComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(lineComposite);
		addLabel(lineComposite, "", span(1), true, 14);
		Button esButton = new Button(lineComposite, SWT.RADIO);
		spanAndHint(1,50).applyTo(esButton);
		if (lineNumber == 1) {
			endstationLine1Button = esButton;
		} else if (lineNumber == 2) {
			endstationLine2Button = esButton;
		}

		Button exhaustButton = new Button(lineComposite, SWT.RADIO);
		spanAndHint(1,50).applyTo(exhaustButton);
		if (lineNumber == 1) {
			exhaustLine1Button = exhaustButton;
		} else if (lineNumber == 2) {
			exhaustLine2Button = exhaustButton;
		}

		layout.applyTo(lineComposite);
		return lineComposite;
	}

	private GridDataFactory spanAndHint(int span, int hint) {
		return span(span).hint(hint, SWT.DEFAULT);
	}

	private GridDataFactory span(int span) {
		return GridDataFactory.fillDefaults()
				.span(span, 1)
				.grab(false, false)
				.align(SWT.LEFT, SWT.CENTER);
	}

	private GridDataFactory hint(int hint) {
		return GridDataFactory.fillDefaults()
				.hint(hint, SWT.DEFAULT)
				.grab(false, false)
				.align(SWT.LEFT, SWT.CENTER);
	}

	private void setControlBold(Control control) {
		FontDescriptor fontDescriptor = FontDescriptor.createFrom(control.getFont());
		fontDescriptor = fontDescriptor.setStyle(SWT.BOLD);
		control.setFont(fontDescriptor.createFont(control.getDisplay()));
	}

	private void setLabelFontSize(Label label, int fontSize) {
		FontDescriptor fontDescriptor = FontDescriptor.createFrom(label.getFont());
		fontDescriptor = fontDescriptor.setHeight(fontSize);
		label.setFont(fontDescriptor.createFont(label.getDisplay()));
	}

	private Text addUneditableUnboundTextBox(Composite parent, GridDataFactory layout, String text) {
		Text textBox = new Text(parent, SWT.BORDER);

		layout.applyTo(textBox);

		textBox.setEditable(false);
		textBox.setText(text);

		return textBox;
	}

	private <T> Text addOneWayBoundDecimalTextBox(Composite parent, Class<T> modelClass, T model, String propertyname, String decimalFormatString, GridDataFactory layout, boolean bold) {

		Text textBox = new Text(parent, SWT.BORDER);

		layout.applyTo(textBox);
		textBox.setEditable(false);

		if (bold) {
			setControlBold(textBox);
		}

		var modelObservable = BeanProperties.value(modelClass, propertyname, Double.class).observe(model);
		var uiObservable = WidgetProperties.text(SWT.Modify).observe(textBox);

		var uiToModelStrategy = new UpdateValueStrategy<String, Double>(false, UpdateValueStrategy.POLICY_NEVER);

		var percentFormatter = new DecimalFormat(decimalFormatString);
		var modelToUiStrategy = new UpdateValueStrategy<Double, String>(false, UpdateValueStrategy.POLICY_UPDATE)
				.setConverter(IConverter.create(Double.class, String.class, percentFormatter::format));

		bindingContext.bindValue(uiObservable, modelObservable, uiToModelStrategy, modelToUiStrategy);

		return textBox;
	}

	private <T> void addOneWayBoundDecimalTextBox(Composite parent, Class<T> modelClass, T model, String propertyname, String decimalFormatString, GridDataFactory layout, boolean bold, String toolTip) {

		var textBox = addOneWayBoundDecimalTextBox(parent, modelClass, model, propertyname, decimalFormatString, layout, bold);
		textBox.setToolTipText(toolTip);
	}

	private <T> void addTwoWayBoundSpinner(Composite parent, Class<T> modelClass, T model, String propertyname, double maxValue, GridDataFactory layout) {

		var spinner = new Spinner(parent, SWT.BORDER);
		spinner.setMinimum(0);
		spinner.setDigits(2);
		spinner.setMaximum((int) (maxValue * 100));
		spinner.setIncrement(1);
		spinner.setPageIncrement(100);
		spinner.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		layout.applyTo(spinner);

		var modelObservable = BeanProperties.value(modelClass, propertyname, Double.class).observe(model);
		var uiObservable = WidgetProperties.spinnerSelection().observe(spinner);

		var uiToModelStrategy = new UpdateValueStrategy<Integer, Double>(false, UpdateValueStrategy.POLICY_UPDATE)
				.setConverter(IConverter.create(Integer.class, Double.class, x -> (double)x / 100));

		var modelToUiStrategy = new UpdateValueStrategy<Double, Integer>(false, UpdateValueStrategy.POLICY_UPDATE)
				.setConverter(IConverter.create(Double.class, Integer.class, x -> (int)Math.round(x * 100)));

		bindingContext.bindValue(uiObservable, modelObservable, uiToModelStrategy, modelToUiStrategy);
	}

	private void showError(String message) {
		Display.getDefault().asyncExec(()-> {
			MessageBox messageBox = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("Error");
			messageBox.setMessage(message);
			messageBox.open();
		});
	}

	/**
	 * Updates valve position or pressure based on the selected gas rig controls
	 */
	private void updateButterflyValvePressureOrPosition() {
		try {
			if(shouldSetButterflyValveToSummedPressure()) {
				double totalPressureOnBothLines = 0;
				for (GasMixViewModel gasMix : gasRigViewModel.getGasMixes()) {
					totalPressureOnBothLines += gasMix.getTotalPressure();
				}
				gasRig.setButterflyValvePressure(totalPressureOnBothLines);
			} else if(shouldSetButterflyValveToLine1Pressure()) {
				double gasMix1Pressure = gasRigViewModel.getGasMixes().get(0).getGasMix().getTotalPressure();
				gasRig.setButterflyValvePressure(gasMix1Pressure);
			} else if(shouldSetButterflyValveToLine2Pressure()) {
				double gasMix2Pressure = gasRigViewModel.getGasMixes().get(1).getGasMix().getTotalPressure();
				gasRig.setButterflyValvePressure(gasMix2Pressure);
			} else if(shouldSetButterflyValveToHundredPercent()) {
				gasRig.setButterflyValvePosition(100);
			}
		} catch (DeviceException e) {
			showError(e.getMessage());
		}
	}

	private boolean shouldSetButterflyValveToHundredPercent() {
		return (endstationLine1Button.getSelection() && emptyLine1Button.getSelection() && exhaustLine2Button.getSelection() ||
				endstationLine2Button.getSelection() && emptyLine2Button.getSelection() && exhaustLine1Button.getSelection() ||
				endstationLine1Button.getSelection() && emptyLine1Button.getSelection() && endstationLine2Button.getSelection() && emptyLine2Button.getSelection());
	}

	private boolean shouldSetButterflyValveToLine1Pressure() {
		return endstationLine1Button.getSelection() && fillLine1Button.getSelection() && exhaustLine2Button.getSelection() ||
				endstationLine1Button.getSelection() && fillLine1Button.getSelection() && endstationLine2Button.getSelection() && emptyLine2Button.getSelection();
	}

	private boolean shouldSetButterflyValveToLine2Pressure() {
		return endstationLine2Button.getSelection() && fillLine2Button.getSelection() && exhaustLine1Button.getSelection() ||
				endstationLine2Button.getSelection() && fillLine2Button.getSelection() && endstationLine1Button.getSelection() && emptyLine1Button.getSelection();
	}

	private boolean shouldSetButterflyValveToSummedPressure() {
		return endstationLine1Button.getSelection() && fillLine1Button.getSelection()
				&& endstationLine2Button.getSelection() && fillLine2Button.getSelection();
	}

	private void unselectButtons(Button a, Button b) {
		a.setSelection(false);
		b.setSelection(false);
	}

	private void handleRadioButtons(Button a, Button b) {
		a.setSelection(true);
		b.setSelection(false);
	}

}
