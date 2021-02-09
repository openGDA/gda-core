package uk.ac.diamond.daq.client.gui.camera.beam;

import static uk.ac.gda.ui.tool.ClientMessages.AXES;
import static uk.ac.gda.ui.tool.ClientMessages.BEAM_MOTORS;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.ClickEvent;
import org.eclipse.dawnsci.plotting.api.axis.IClickListener;
import org.eclipse.richbeans.widgets.menu.MenuAction;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.composites.FinderHelper;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.spring.MotorUtils;

/**
 * Attach the camera stream plotting the beam mapping information calculated in
 * an earlier beam to camera mapping.
 * <p>
 * If active, the class provides two main features:
 * <ul>
 * <li>pressing the <i>b</i> key and clicking on the plotting area, moves the
 * beam according to the available transformation</li>
 * <li>on the context menu, allows the user to adds a supplementary axes
 * displaying the beam motor positions.</li>
 * </ul>
 * </p>
 *
 *
 * @author Maurizio Nagni
 *
 */
public final class BeamMappingSupport {

	private static final Logger logger = LoggerFactory.getLogger(BeamMappingSupport.class);

	private final ICameraConfiguration iConfiguration;
	private final IPlottingSystem<Composite> plottingSystem;

	/**
	 * Listen to click events on the configured plotting system. When the key
	 * <i>b</i> is pressed the mouse click will move the beam to the specified
	 * position.
	 */
	private final IClickListener clickListener = new IClickListener() {

		@Override
		public void doubleClickPerformed(final ClickEvent event) {
			// Does nothing
		}

		@Override
		public void clickPerformed(final ClickEvent event) {
			if (event.getCharacter() == 'b' && Objects.nonNull(iConfiguration.getBeamCameraMap())) {
				// Do not need to wait
				Async.execute(() -> moveMotors(event));
			}
		}

		private void moveMotors(ClickEvent event) {
			iConfiguration.getBeamCameraMap().getDriver().forEach(s -> {
				Optional<RealVector> moveTo;
				if (s.equals(event.getxAxis().getTitle())) {
					moveTo = Optional.of(new ArrayRealVector(new double[] { event.getxValue(), event.getyValue() }, false));
				} else {
					moveTo = iConfiguration.getBeamCameraMapping().pixelToBeam(iConfiguration,
									event.getxValue(), event.getyValue());
				}
				moveTo.ifPresent(this::doMove);
			});

		}

		/**
		 * Drives asynchronously the motors so to move the beam simultaneously in both directions
		 * @param moveTo
		 */
		private void doMove(RealVector moveTo) {
			IntStream.rangeClosed(0, 1).forEach(i -> {
				FinderHelper.getIScannableMotor(iConfiguration.getBeamCameraMap().getDriver().get(i))
					.ifPresent(m -> getMotorUtils() .moveMotorAsynchronously(m, moveTo.getEntry(i)));
			});
		}

		private MotorUtils getMotorUtils() {
			return SpringApplicationContextFacade.getBean(MotorUtils.class);
		}
	};

	/**
	 * Associates a camera configuration with a plotting system. If the camera has a
	 * valid mapping, the constructor attach a new element to the plotting system
	 * camera configuration.
	 *
	 * @param iConfiguration
	 * @param plottingSystem
	 */
	public BeamMappingSupport(ICameraConfiguration iConfiguration, IPlottingSystem<Composite> plottingSystem) {
		this.iConfiguration = iConfiguration;
		this.plottingSystem = plottingSystem;

		Optional<RealVector> topLeft = iConfiguration.getBeamCameraMapping().pixelToBeam(iConfiguration, 0, 0);
		Optional<RealVector> bottomRight= Optional.empty();
		int[] pos = iConfiguration.getCameraControl().map(this::getFrameSize).orElseGet(() -> new int[] { 0, 0 });
		double x = pos[0];
		double y = pos[1];
		bottomRight = iConfiguration.getBeamCameraMapping().pixelToBeam(iConfiguration, x, y);

		// Creates the context menu for the calibrated axes
		MenuAction alternativeAxes = new MenuAction(ClientMessagesUtility.getMessage(AXES));
		// Creates the action to display the motor calibrated axes
		if (topLeft.isPresent() && bottomRight.isPresent()) {
			alternativeAxes.add(new AxesAction(BEAM_MOTORS, () -> plottingSystem, topLeft.get(), bottomRight.get(),
					iConfiguration.getBeamCameraMap().getDriver().get(0), iConfiguration.getBeamCameraMap().getDriver().get(1)));
			plottingSystem.getPlotActionSystem().addPopupAction(alternativeAxes);
		}
	}

	private int[] getFrameSize(CameraControl cc) {
		try {
			return cc.getFrameSize();
		} catch (DeviceException e) {
			return new int[] { 0, 0 };
		}
	}

	/**
	 * Draws the mapping boundaries and allows to the key+click feature on the
	 * plotting system specified in the constructor
	 *
	 * @param activate {@code true} to activate the listener, {@code false} to
	 *                 remove.
	 */
	public void activateListener(boolean activate) {
		if (activate) {
			drawBoundaries();
		} else {
			plottingSystem.removeClickListener(clickListener);
		}
	}

	private void drawBoundaries() {
		try {
			DrawCameraMappingArea.drawBeamBoundaries(plottingSystem, iConfiguration);
			plottingSystem.addClickListener(clickListener);
		} catch (GDAClientException e) {
			UIHelper.showError("Cannot calibrate camera to motors", e, logger);
		}
	}
}
