package uk.ac.diamond.daq.client.gui.energy;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.util.Arrays;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffect;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.SelectObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;

public class BeamEnergyControl {

	private static final String START_ICON_PATH = "/icons/start.png";
	private static final String STOP_ICON_PATH = "/icons/stop.png";

	private EnergyWorkflowController controller;

	private Button monoBeam;
	private Text monoDemand;
	private Button pinkBeam;
	private ComboViewer pinkDemand;

	private Button start;
	private Button stop;

	private Label updateMessage;

	private ProgressBar progress;

	private BeamEnergyBean bean;

	public BeamEnergyControl(EnergyWorkflowController controller) {
		this.controller = controller;
		bean = new BeamEnergyBean();
	}

	public void draw(Composite parent) {
		Composite base = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).applyTo(base);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(base);

		GridDataFactory layout = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);

		monoBeam = new Button(base, SWT.RADIO);
		monoBeam.setText("Monochromatic");
		layout.applyTo(monoBeam);

		monoDemand = new Text(base, SWT.BORDER);
		layout.applyTo(monoDemand);

		pinkBeam = new Button(base, SWT.RADIO);
		pinkBeam.setText("Polychromatic");
		layout.applyTo(pinkBeam);

		pinkDemand = new ComboViewer(base);
		layout.applyTo(pinkDemand.getControl());

		start = new Button(base, SWT.NONE);
		start.setText("Start");
		start.setImage(getIcon(START_ICON_PATH));
		layout.applyTo(start);

		stop = new Button(base, SWT.NONE);
		stop.setText("Stop");
		stop.setImage(getIcon(STOP_ICON_PATH));
		layout.applyTo(stop);

		progress = new ProgressBar(base, SWT.NONE);
		layout.copy().span(2, 1).applyTo(progress);

		updateMessage = new Label(base, SWT.READ_ONLY);
		updateMessage.setBackground(base.getBackground());
		updateMessage.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
		layout.copy().span(2, 1).applyTo(updateMessage);

		bind(base);
	}

	@SuppressWarnings("unchecked")
	private void bind(Composite base) {
		DataBindingContext context = new DataBindingContext();

		// beam selection
		SelectObservableValue<EnergyType> beamSelection = new SelectObservableValue<>();
		beamSelection.addOption(EnergyType.MONOCHROMATIC, WidgetProperties.selection().observe(monoBeam));
		beamSelection.addOption(EnergyType.POLYCHROMATIC, WidgetProperties.selection().observe(pinkBeam));
		IObservableValue<EnergyType> beamInBean = PojoProperties.value("type").observe(bean);

		context.bindValue(beamSelection, beamInBean);

		// mono energy
		IObservableValue<Double> monoEnergyUi = WidgetProperties.text(SWT.Modify).observe(monoDemand);
		IObservableValue<Double> monoEnergyModel = PojoProperties.value("monoEnergy").observe(bean);

		context.bindValue(monoEnergyUi, monoEnergyModel);

		// pink band
		pinkDemand.setContentProvider(ArrayContentProvider.getInstance());

		// FIXME hardcoded for now
		pinkDemand.setInput(Arrays.asList(
								new EnergyBand("low", 1.2, 2.9),
								new EnergyBand("mid", 2.5, 3.7),
								new EnergyBand("high", 3.1, 5.6)));

		pinkDemand.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				EnergyBand band = (EnergyBand) element;
				return band.getLabel() + ": " + band.getLow() + "-" + band.getHigh() + " keV";
			}
		});
		IViewerObservableValue bandInUi = ViewerProperties.singleSelection().observe(pinkDemand);
		IObservableValue<EnergyBand> bandInModel = PojoProperties.value("polyEnergy").observe(bean);
		context.bindValue(bandInUi, bandInModel);

		// disable mono/pink input
		ISideEffect.create(beamSelection::getValue, this::beamTypeChanged);

		// actions
		start.addSelectionListener(widgetSelectedAdapter(event -> start()));
		stop.addSelectionListener(widgetSelectedAdapter(event -> stop()));

		EnergyControllerListener updater = new Updater();
		controller.addListener(updater);
		base.addDisposeListener(event -> controller.removeListener(updater));

		setInitialState();
	}

	private void setInitialState() {

		switch (controller.getEnergySelectionType()) {
		case BOTH:
			break;

		case MONO:
			pinkBeam.setVisible(false);
			pinkDemand.getControl().setVisible(false);
			break;

		case PINK:
			monoBeam.setVisible(false);
			monoDemand.setVisible(false);
			break;

		default:
			break;
		}

		stop.setEnabled(false);

		progress.setSelection(0);
		updateMessage.setText("Ready");

	}

	private void beamTypeChanged(EnergyType newType) {
		boolean mono = newType == EnergyType.MONOCHROMATIC;
		monoDemand.setEnabled(mono);
		pinkDemand.getControl().setEnabled(!mono);
	}

	private void start() {
		controller.startWorkflow(bean);
	}

	private void stop() {
		controller.stopWorkflow(bean);
	}

	private Image getIcon(String path) {
		return new Image(Display.getCurrent(), getClass().getResourceAsStream(path));
	}

	private class Updater implements EnergyControllerListener {
		@Override
		public void operationStarted() {
			Display.getDefault().asyncExec(() -> {
				disable();
				setMessage("Running...");
			});
		}

		@Override
		public void operationFinished() {
			Display.getDefault().asyncExec(() -> {
				enable();
				setMessage("Complete");
				progress.setSelection(100);
			});
		}

		@Override
		public void operationFailed(String message) {
			Display.getDefault().asyncExec(() -> {
				enable();
				setMessage("Failed: " + message);
			});
		}

		@Override
		public void progressMade(String message, double percentage) {
			Display.getDefault().asyncExec(() -> {
				disable();
				setMessage(message);
				progress.setSelection((int) percentage);
			});
		}


		private void setMessage(String msg) {
			updateMessage.setText(msg);
		}

		private void enable() {
			setControlsEnabled(true);
			beamTypeChanged(bean.getType());
		}

		private void disable() {
			setControlsEnabled(false);
		}

		private void setControlsEnabled(boolean enabled) {
			monoBeam.setEnabled(enabled);
			monoDemand.setEnabled(enabled);
			pinkBeam.setEnabled(enabled);
			pinkDemand.getControl().setEnabled(enabled);
			start.setEnabled(enabled);
			stop.setEnabled(!enabled);
		}
	}

}
