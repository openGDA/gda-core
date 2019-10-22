/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.rcp.ncd.views;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.measure.quantity.Duration;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.data.PathConstructor;
import gda.factory.Finder;
import gda.observable.IObserver;
import gda.rcp.ncd.ExptDataModel;
import uk.ac.gda.client.NumberAndUnitsComposite;
import uk.ac.gda.client.NumberUnitsWidgetProperty;
import uk.ac.gda.server.ncd.timing.HardwareTimerException;
import uk.ac.gda.server.ncd.timing.TimerController;
import uk.ac.gda.server.ncd.timing.data.SimpleTimerConfiguration;

public class SimpleTimerView extends ViewPart {
	private static final Logger logger = LoggerFactory.getLogger(SimpleTimerView.class);

	private static final String MILLISECONDS = "Milliseconds";
	private static final String SECONDS = "Seconds";
	private static final String MINUTES = "Minutes";
	private static final String HOURS = "Hours";

	private static final double MILLISECONDS_TO_SECONDS = 1.0 / 1000;
	private static final double MINUTES_TO_SECONDS = 60;
	private static final double HOURS_TO_SECONDS = 3600;

	private SimpleTimerConfiguration simpleTimerConfiguration = new SimpleTimerConfiguration ();

	private DataBindingContext dbc = new DataBindingContext();
	private TimerController timerController;
	private TimerControllerListener listener;
	private ChangeListener changeListener = new ChangeListener();
	private Composite parent;

	private Text numberOfFramesText;
	private NumberAndUnitsComposite<Duration> exposureTime;
	private Button delayCheckBox;
	private NumberAndUnitsComposite<Duration> delayTime;
	private Label totalCollectionTimeLabel;

	private Button saveButton;
	private Button configureButton;

	private class ChangeListener implements IChangeListener {
		@Override
		public void handleChange(ChangeEvent event) {
			update();
		}

		private void update () {
			double value = simpleTimerConfiguration.getExposure();
			if (simpleTimerConfiguration.isDelay()) {
				value += simpleTimerConfiguration.getDelayTime();
			}
			value *= simpleTimerConfiguration.getNumberOfFrames();
			double displayTime;
			String time;
			if (value / HOURS_TO_SECONDS > 1) {
				displayTime = value / HOURS_TO_SECONDS;
				time = HOURS;
			} else if (value / MINUTES_TO_SECONDS > 1) {
				displayTime = value / MINUTES_TO_SECONDS;
				time = MINUTES;
			} else if (value > 1) {
				displayTime = value;
				time = SECONDS;
			} else {
				displayTime = value / MILLISECONDS_TO_SECONDS;
				time = MILLISECONDS;
			}

			totalCollectionTimeLabel.setText(String.format("%.0f %s", displayTime, time));
		}
	}

	private class TimerControllerListener implements IObserver {
		@Override
		public void update(Object source, Object arg) {
			if (arg instanceof SimpleTimerConfiguration) {
				SimpleTimerConfiguration.copy((SimpleTimerConfiguration)arg, simpleTimerConfiguration);
				Display.getDefault().asyncExec(() -> dbc.updateTargets());
			}
		}
	}

	public SimpleTimerView() {
		setTitleToolTip("Configure shutter capture");
		setPartName("Simple Time Frame");

		listener = new TimerControllerListener();

		timerController = Finder.getInstance().findSingleton(TimerController.class);
		timerController.addIObserver(listener);
		SimpleTimerConfiguration.copy(timerController.getLastUsedConfiguration(), simpleTimerConfiguration);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void createPartControl(Composite parent) {
		logger.debug ("Started creating control");

		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(parent);

		/* Build Forms ******************************************************************************/
		Label spacer;

		Composite configurationPanel = createShutterConfigurationComposite (parent);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).hint(350, -1).applyTo(configurationPanel);
		spacer = new Label(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(spacer);
		Composite buttonPanel = createButtonPanel (parent);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.BEGINNING).applyTo(buttonPanel);
		spacer = new Label(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).applyTo(spacer);
		spacer = new Label(parent, SWT.NONE);
		GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).span(2, 1).applyTo(spacer);

		/* Data Bindings ****************************************************************************/
		IObservableValue numberOfFramesTarget = WidgetProperties.text(SWT.Modify).observe(numberOfFramesText);
		numberOfFramesTarget.addValueChangeListener(e -> {
			try {
				Integer.parseInt(e.getObservableValue().getValue().toString());
				numberOfFramesText.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
				enableButtons(true);
			} catch (NumberFormatException ex) {
				numberOfFramesText.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
				enableButtons(false);
			}
		});
		IObservableValue numberOfFramesModel = BeanProperties.value("numberOfFrames").observe(simpleTimerConfiguration);
		numberOfFramesModel.addChangeListener(changeListener);
		dbc.bindValue(numberOfFramesTarget, numberOfFramesModel);

		IObservableValue exposureTimeTarget = new NumberUnitsWidgetProperty<Duration>().observe(exposureTime);
		IObservableValue exposureTimeModel = PojoProperties.value("exposure").observe(simpleTimerConfiguration);
		exposureTimeModel.addChangeListener(changeListener);
		dbc.bindValue(exposureTimeTarget, exposureTimeModel);

		IObservableValue delayTarget = WidgetProperties.selection().observe(delayCheckBox);
		IObservableValue delayModel = BeanProperties.value("delay").observe(simpleTimerConfiguration);
		delayModel.addChangeListener(changeListener);
		delayModel.addChangeListener(e -> {
			if (simpleTimerConfiguration.isDelay()) {
				delayTime.setEnabled(true);
			} else {
				delayTime.setEnabled(false);
			}
		});
		dbc.bindValue(delayTarget, delayModel);

		IObservableValue delayTimeTarget = new NumberUnitsWidgetProperty<Duration>().observe(delayTime);
		IObservableValue delayTimeModel = PojoProperties.value("delayTime").observe(simpleTimerConfiguration);
		delayTimeModel.addChangeListener(changeListener);
		dbc.bindValue(delayTimeTarget, delayTimeModel);

		changeListener.update();

		logger.debug ("Finished creating controls");
	}

	private Composite createShutterConfigurationComposite (Composite parent) {
		this.parent = parent;

		Label label;
		Set<Unit<Duration>> units = new HashSet<>();
		units.add(SI.MILLI(SI.SECOND));
		units.add(SI.SECOND);
		units.add(NonSI.MINUTE);
		units.add(NonSI.HOUR);

		Composite panel = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).applyTo(panel);

		/* Number of Frames Row ***********************************************************************/
		label = new Label(panel, SWT.NONE);
		label.setText("Number Of Frames");
		GridDataFactory.swtDefaults().applyTo(label);

		numberOfFramesText = new Text(panel, SWT.BORDER | SWT.RIGHT);
		numberOfFramesText.setText("No Value");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(numberOfFramesText);

		/* Data Collection Time Row *******************************************************************/
		label = new Label(panel, SWT.NONE);
		label.setText("Exposure Time");
		GridDataFactory.swtDefaults().applyTo(label);

		exposureTime = new NumberAndUnitsComposite<>(panel, SWT.None, SI.SECOND, units);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(exposureTime);

		/* Delay **************************************************************************************/
		label = new Label(panel, SWT.NONE);
		label.setText("Set Delay");
		GridDataFactory.swtDefaults().applyTo(label);

		delayCheckBox = new Button(panel, SWT.CHECK);
		GridDataFactory.swtDefaults().applyTo(delayCheckBox);

		/* Delay Time Row *****************************************************************************/
		label = new Label(panel, SWT.NONE);
		label.setText("Delay Time");
		GridDataFactory.swtDefaults().applyTo(label);

		delayTime = new NumberAndUnitsComposite<>(panel, SWT.None, SI.SECOND, units);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(delayTime);

		/* Total Exposure Row *************************************************************************/
		label = new Label(panel, SWT.NONE);
		label.setText("Approx. Collection Time:");
		totalCollectionTimeLabel = new Label(panel, SWT.None);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(totalCollectionTimeLabel);

		/* *******************************************************************************************/

		return panel;
	}

	private Composite createButtonPanel (Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);

		RowLayoutFactory.swtDefaults().wrap(false).pack(false).applyTo(panel);

		Button loadButton = new Button(panel, SWT.PUSH);
		loadButton.setText("Load");
		loadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadConfiguration();
			}
		});
		saveButton = new Button(panel, SWT.PUSH);
		saveButton.setText("Save");
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				saveConfiguration ();
			}
		});
		configureButton = new Button(panel, SWT.PUSH);
		configureButton.setText("Configure");
		configureButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				configure();
			}
		});

		return panel;
	}

	private void enableButtons (boolean enabled) {
		saveButton.setEnabled(enabled);
		configureButton.setEnabled(enabled);
	}

	private void configure () {
		dbc.updateModels();
		try {
			if (!timerController.configureTimer(simpleTimerConfiguration)) {
				MessageDialog.openError(parent.getShell(), "Timer Configuration", "Unable to configure timer please see logs");
			}
		} catch (HardwareTimerException e) {
			MessageDialog.openError(parent.getShell(), "Timer Configuration", e.getMessage());
		}
	}

	private void saveConfiguration () {
		FileDialog fileDialog = new FileDialog(this.getViewSite().getShell(), SWT.SAVE);
		fileDialog.setFilterPath(PathConstructor.getClientVisitSubdirectory("xml"));
		fileDialog.setFilterExtensions(new String[] {timerController.getConfigurationFileExtension()});

		String selectedFileName = fileDialog.open();
		if (selectedFileName != null) {
			try {
				File selectedFile = new File (selectedFileName);
				timerController.saveTimer(selectedFile, simpleTimerConfiguration);
				ExptDataModel.getInstance().setFileName(selectedFile.getName());
			} catch (IOException e) {
				MessageBox errorMessage = new MessageBox(this.getViewSite().getShell(), SWT.ICON_ERROR | SWT.CLOSE);
				errorMessage.setText("Save Error");
				errorMessage.setMessage(e.getMessage());
				errorMessage.open();
			}
		}
	}

	private void loadConfiguration () {
		FileDialog fileDialog = new FileDialog(this.getViewSite().getShell(), SWT.OPEN);
		fileDialog.setFilterPath(PathConstructor.getClientVisitSubdirectory("xml"));
		fileDialog.setFilterExtensions(new String[] {timerController.getConfigurationFileExtension()});

		String selectedFilename = fileDialog.open();
		if (selectedFilename != null) {
			try {
				File selectedFile = new File (selectedFilename);
				SimpleTimerConfiguration.copy(timerController.loadTimer(selectedFile), simpleTimerConfiguration);
				ExptDataModel.getInstance().setFileName(selectedFile.getName());
				dbc.updateTargets();
			} catch (IOException e) {
				MessageBox errorMessage = new MessageBox(this.getViewSite().getShell(), SWT.ICON_ERROR | SWT.CLOSE);
				errorMessage.setText("Save Error");
				errorMessage.setMessage(e.getMessage());
				errorMessage.open();
			}
		}
	}

	@Override
	public void setFocus() {
		//Not used
	}
}
