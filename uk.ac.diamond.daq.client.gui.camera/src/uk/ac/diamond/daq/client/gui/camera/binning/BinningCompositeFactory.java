package uk.ac.diamond.daq.client.gui.camera.binning;

import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGroup;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import gda.device.DeviceException;
import gda.observable.IObserver;
import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.event.ChangeActiveCameraEvent;
import uk.ac.gda.api.camera.BinningFormat;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraControllerEvent;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.ui.tool.ClientBindingElements;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientResourceManager;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * A {@link Group} to edit a {@code EpicsCameraControl} {@code ADBase.BinX} and
 * {@code ADBase.BinY}.
 * <p>
 * Even if the camera control allow for independent and separated X/Y binning
 * integer values, there are no major cases that require such freedom.
 * Consequently this widget exposes just three binning values (1,2,4) which are
 * applied to both X and Y.
 * </p>
 * <p>
 * This widget dynamically change the {@code cameraControl} it is attached
 * listening to {@link ChangeActiveCameraEvent} events.
 * </p>
 * 
 * <p>
 * At the start the component points at the camera defined by
 * {@link CameraHelper#getDefaultCameraProperties()}
 * </p>
 * 
 * <p>
 * <b>NOTE:</b> To works correctly this widget requires that the
 * {@code useAcquireTimeMonitor} property in the {@link EpicsCameraControl} bean
 * is set to {@code true} (usually in the configuration file).
 * </p>
 * 
 * @author Maurizio Nagni
 */
public class BinningCompositeFactory implements CompositeFactory {

	/**
	 * Predefined binning
	 */
	private enum Binning {
		ONE(1), TWO(2), FOUR(4);

		private final int pixelSize;

		Binning(int pixelSize) {
			this.pixelSize = pixelSize;
		}

		public int getPixelSize() {
			return pixelSize;
		}
	}

	private Map<Binning, Button> radios = new EnumMap<>(Binning.class);
	/**
	 * The {@code cameraConfiguration} binning configuration
	 */
	private Label readOut;

	private int cameraIndex;

	private static final Logger logger = LoggerFactory.getLogger(BinningCompositeFactory.class);

	@Override
	public Composite createComposite(Composite parent, int style) {
		cameraIndex = CameraHelper.getDefaultCameraProperties().getIndex();
		
		Composite composite = createClientCompositeWithGridLayout(parent, style, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(composite);

		createElements(composite);
		radios.forEach(this::bindRadio);
		CameraHelper.getCameraControl(cameraIndex).ifPresent(cc -> {
			initialiseElements(cc);
			ClientBindingElements.addDisposableObserver(composite, cc, cameraControlObserver);
		});
		return composite;
	}

	private void createElements(Composite parent) {
		Group group = createClientGroup(parent, SWT.NONE, 1, ClientMessages.BINNING);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(group);

		Arrays.stream(Binning.values()).forEach(binning -> createRadioButton(group, binning));
		readOut = ClientSWTElements.createClientLabel(group, SWT.LEFT, ClientMessages.EMPTY_MESSAGE, Optional
				.ofNullable(FontDescriptor.createFrom(ClientResourceManager.getInstance().getTextDefaultFont())));
		createClientGridDataFactory().indent(5, SWT.DEFAULT).applyTo(readOut);
		
		try {
			SpringApplicationContextProxy.addDisposableApplicationListener(group,
					getChangeActiveCameraListener(group, this::initialiseElements));
		} catch (GDAClientException e) {
			UIHelper.showError(ClientMessages.CANNOT_LISTEN_CAMERA_PUBLISHER, e, logger);
		}
	}

	private void initialiseElements(CameraControl cameraControl) {
		try {
			BinningFormat bf = cameraControl.getBinningPixels();
			updateGUI(bf);
		} catch (DeviceException e) {
			UIHelper.showError("Error reading detector binning", e, logger);
		}
	}

	private void updateGUI(BinningFormat bf) {
		updateReadOut(bf);
		// The actual widget does not handle asymmetric binning
		if (bf.getX() != bf.getY()) {
			UIHelper.showWarning("The camera has asymmetrical binning.", "X/Y binning are differents");
			radios.values().stream().forEach(radio -> {
				radio.setEnabled(false);
				radio.setSelection(false);
			});
			return;
		}

		Predicate<Binning> filterBinning = b -> Objects.equals(b.getPixelSize(), bf.getX());
		Arrays.stream(Binning.values()).filter(filterBinning).findFirst().ifPresent(configureRadios);
	}

	/**
	 * Configures the radios with the passed binning value
	 */
	private Consumer<Binning> configureRadios = binning -> radios.values().stream().forEach(configureRadio(binning));

	private Consumer<Button> configureRadio(Binning binning) {
		// the widget is editable only if by-configuraiton
		boolean editable = CameraHelper.getCameraProperties(cameraIndex).isPixelBinningEditable();
		return radio -> {
			radio.setSelection(radio.getData().equals(binning));
			radio.setEnabled(editable);
		};
	}

	private void updateReadOut(BinningFormat bf) {
		readOut.setText(String.format("ReadOut(x,y): %s,%s", bf.getX(), bf.getY()));
	}

	private void updateModelToGUI(CameraControllerEvent e) {
		updateGUI(e.getBinningFormat());
	}

	private ApplicationListener<ChangeActiveCameraEvent> getChangeActiveCameraListener(Composite parent,
			Consumer<? super CameraControl> ccConsumer) {
		return new ApplicationListener<ChangeActiveCameraEvent>() {
			@Override
			public void onApplicationEvent(ChangeActiveCameraEvent event) {
				// if the event arrives from a component with a different common parent, rejects
				// the event
				if (!event.haveSameParent(parent)) {
					return;
				}
				cameraIndex = event.getActiveCamera().getIndex();
				Display.getDefault().asyncExec(() -> CameraHelper.getCameraControl(cameraIndex).ifPresent(ccConsumer));
			}
		};
	}

	private final IObserver cameraControlObserver = (source, arg) -> {
		if (CameraControllerEvent.class.isInstance(arg)) {
			Display.getDefault().asyncExec(() -> updateModelToGUI(CameraControllerEvent.class.cast(arg)));
		}
	};

	private void createRadioButton(Composite parent, Binning binning) {
		Button button = ClientSWTElements.createButton(parent, SWT.RADIO, Integer.toString(binning.pixelSize), "",
				Optional.empty(), Optional.empty());
		// sets the button data (the shape it refers to)
		button.setData(binning);
		button.setSelection(false);
		radios.put(binning, button);
	}

	private void bindRadio(Binning binning, Button button) {
		button.addSelectionListener(SelectionListener.widgetSelectedAdapter(widgetSelected));
	}

	private Consumer<SelectionEvent> widgetSelected = event -> CameraHelper.getCameraControl(cameraIndex)
			.ifPresent(c -> {
				try {
					Binning binning = Binning.class.cast(event.widget.getData());
					c.setBinningPixels(new BinningFormat(binning.getPixelSize(), binning.getPixelSize()));
				} catch (DeviceException e) {
					UIHelper.showError("Cannot update acquisition time", e, logger);
				}
			});
}
