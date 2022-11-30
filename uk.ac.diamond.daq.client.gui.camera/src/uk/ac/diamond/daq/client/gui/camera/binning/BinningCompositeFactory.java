/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.camera.binning;

import static uk.ac.gda.ui.tool.ClientMessages.EMPTY_MESSAGE;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientButton;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.jface.layout.GridLayoutFactory;
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

import gda.device.DeviceException;
import gda.observable.IObserver;
import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.client.gui.camera.event.ChangeActiveCameraEvent;
import uk.ac.gda.api.camera.BinningFormat;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraControllerEvent;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
import uk.ac.gda.ui.tool.ClientBindingElements;

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
 * {@link CameraHelper#getDefaultCameraConfigurationProperties()}
 * </p>
 *
 * @author Maurizio Nagni
 */
public class BinningCompositeFactory implements CompositeFactory {
	private static final Logger logger = LoggerFactory.getLogger(BinningCompositeFactory.class);

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

	/**
	 * The {@code cameraConfiguration} binning configuration
	 */
	private Label readOut;
	private Map<Binning, Button> radios = new EnumMap<>(Binning.class);

	private CameraConfigurationProperties camera;
	private ICameraConfiguration iCameraConfiguration;

	public BinningCompositeFactory(CameraConfigurationProperties camera) {
		this.camera = camera;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		iCameraConfiguration = CameraHelper.createICameraConfiguration(camera);

		var composite = createClientCompositeWithGridLayout(parent, SWT.NONE, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(composite);

		createElements(composite);
		radios.values().forEach(this::bindRadio);
		iCameraConfiguration.getCameraControl()
			.ifPresent(cc -> {
				initialiseElements(cc);
				ClientBindingElements.addDisposableObserver(composite, cc, cameraControlObserver);
			});
		return composite;
	}

	private void createElements(Composite parent) {
		var group = new Composite(parent, SWT.NONE);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(group);
		GridLayoutFactory.swtDefaults().numColumns(Binning.values().length + 2).applyTo(group);

		new Label(group, SWT.NONE).setText("Binning");

		Arrays.stream(Binning.values()).forEach(binning -> createRadioButton(group, binning));

		readOut = new Label(group, SWT.NONE);
		readOut.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
		createClientGridDataFactory().indent(10, SWT.DEFAULT).applyTo(readOut);
	}

	private void initialiseElements(CameraControl cameraControl) {
		try {
			BinningFormat bf = cameraControl.getBinningPixels();
			updateGUI(bf);
		} catch (DeviceException e) {
			logger.warn("Error reading detector binning {}", e.getMessage());
		}
	}

	private void updateGUI(BinningFormat bf) {
		readOut.setText(String.format("ReadOut(x,y): %s,%s", bf.getX(), bf.getY()));
		// The actual widget does not handle asymmetric binning
		if (bf.getX() != bf.getY()) {
			UIHelper.showWarning("The camera has asymmetrical binning.", "X/Y binning are differents");
			radios.values().stream().forEach(radio -> {
				radio.setEnabled(false);
				radio.setSelection(false);
			});
			return;
		}

		Arrays.stream(Binning.values())
		.filter(b -> b.getPixelSize() == bf.getX())
		.findFirst()
		.ifPresent(configureRadios);

	}

	/**
	 * Configures the radios with the passed binning value
	 */
	private Consumer<Binning> configureRadios = binning -> radios.values().stream().forEach(configureRadio(binning));

	private Consumer<Button> configureRadio(Binning binning) {
		// the widget is editable only if by-configuraiton
		return radio -> {
			radio.setSelection(radio.getData().equals(binning));
			radio.setEnabled(camera.isPixelBinningEditable());
		};
	}

	private final IObserver cameraControlObserver = (source, arg) -> {
		if (arg instanceof CameraControllerEvent event) {
			Display.getDefault().asyncExec(() -> updateGUI(event.getBinningFormat()));
		}
	};

	private void createRadioButton(Composite parent, Binning binning) {
		var button = createClientButton(parent, SWT.RADIO, EMPTY_MESSAGE, EMPTY_MESSAGE);
		createClientGridDataFactory().indent(5, SWT.DEFAULT).applyTo(button);
		// sets the button data (the shape it refers to)
		button.setData(binning);
		button.setSelection(false);
		button.setText(Integer.toString(binning.getPixelSize()));
		radios.put(binning, button);
	}

	private void bindRadio(Button button) {
		button.addSelectionListener(SelectionListener.widgetSelectedAdapter(widgetSelected));
	}

	private Consumer<SelectionEvent> widgetSelected = event -> iCameraConfiguration.getCameraControl()
			.ifPresent(cameraControl -> {
				try {
					var binning = Binning.class.cast(event.widget.getData());
					BinningFormat bf = new BinningFormat(binning.getPixelSize(), binning.getPixelSize());
					cameraControl.setBinningPixels(bf);
				} catch(DeviceException e) {
					UIHelper.showError("Cannot update the camera binning", e, logger);
				}
			});
}
