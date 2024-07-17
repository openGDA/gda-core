package uk.ac.gda.beamline.synoptics.composites;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.configuration.properties.LocalProperties;
import gda.epics.connection.EpicsController;
import gda.rcp.views.CompositeFactory;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import uk.ac.diamond.daq.concurrent.Async;

public class LaserScannerStatusCompositeFactory implements CompositeFactory {

	private String label;
	private String pvName;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		return new LaserScannerStatusComposite(parent, style, parent.getDisplay(), label, getPvName());
	}

	public String getPvName() {
		return pvName;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

}

class LaserScannerStatusComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(LaserScannerStatusComposite.class);

	private static final Color LASER_SCANNER_CLEAR_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
	private static final Color LASER_SCANNER_TRIGGERED_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
	private static final String LASER_SCANNER_CLEAR_TOOL_TIP = "Laser Scanner Clear";
	private static final String LASER_SCANNER_TRIGGERED_TOOL_TIP = "Laser Scanner Triggered";

	private Display mdisplay;
	private Color currentColor;
	private Canvas canvas;

	private ScheduledFuture<?> colorChange;

	private final EpicsController epics_controller = EpicsController.getInstance();
	private Channel pvch;

	public LaserScannerStatusComposite(Composite parent, int style, final Display display, String label, String pv) {
		super(parent, style);

		GridDataFactory.fillDefaults().applyTo(this);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(this);

		Group grp = new Group(this, style);
		GridDataFactory.fillDefaults().applyTo(grp);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(grp);
		grp.setText(label);
		grp.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		this.mdisplay = display;
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(this);
		GridDataFactory.fillDefaults().applyTo(this);

		currentColor = LASER_SCANNER_CLEAR_COLOR;

		initialisation(pv);

		canvas = new Canvas(grp, SWT.NONE);
		GridData gridData = new GridData(GridData.VERTICAL_ALIGN_FILL);
		gridData.widthHint = 40;
		gridData.heightHint = 40;
		canvas.setLayoutData(gridData);
		canvas.addPaintListener(e -> {
			GC gc = e.gc;
			gc.setAntialias(SWT.ON);
			gc.setBackground(currentColor);
			gc.setLineWidth(1);
			Rectangle clientArea = canvas.getClientArea();
			final int margin = 4;
			final Point topLeft = new Point(margin, margin);
			final Point size = new Point(clientArea.width - margin * 2, clientArea.height - margin * 2);
			gc.fillOval(topLeft.x, topLeft.y, size.x, size.y);
			gc.drawOval(topLeft.x, topLeft.y, size.x, size.y);
		});
	}

	private void initialisation(String pv) {
		if (LocalProperties.isDummyModeEnabled()) {
			logger.debug("Dummy mode uses ");
			colorChange = Async.scheduleAtFixedRate(() ->
				mdisplay.asyncExec(() -> {
					currentColor = currentColor == LASER_SCANNER_CLEAR_COLOR ? LASER_SCANNER_TRIGGERED_COLOR : LASER_SCANNER_CLEAR_COLOR;
					canvas.setToolTipText(currentColor == LASER_SCANNER_CLEAR_COLOR ? LASER_SCANNER_CLEAR_TOOL_TIP : LASER_SCANNER_TRIGGERED_TOOL_TIP);
					canvas.redraw();
					canvas.update();
				})
			, 5, 5, TimeUnit.SECONDS);
		} else {
			try {
				pvch = epics_controller.createChannel(pv);
				colorChange = updateColorThread(pv);
			} catch (CAException | TimeoutException e) {
				logger.error("Failed to create channel for {}", pv, e);
			}
		}
	}

	private ScheduledFuture<?> updateColorThread(String pv) {
		return Async.scheduleAtFixedRate(() ->
			mdisplay.asyncExec(() -> {
				try {
					short value = epics_controller.cagetEnum(pvch);
					if (value == 0 || value == 1) {
						currentColor = LASER_SCANNER_CLEAR_COLOR;
					} else if (value == 2 || value == 3) {
						currentColor = LASER_SCANNER_TRIGGERED_COLOR;
					}
					canvas.setToolTipText(currentColor == LASER_SCANNER_CLEAR_COLOR ? LASER_SCANNER_CLEAR_TOOL_TIP : LASER_SCANNER_TRIGGERED_TOOL_TIP);
					canvas.redraw();
					canvas.update();
				} catch (TimeoutException | CAException e) {
					logger.error("failed to get value from {}",pv, e);
				} catch (InterruptedException e) {
					logger.error("Interrupted while getting value from {}",pv, e);
					Thread.currentThread().interrupt();
				}
			})
		, 5, 10, TimeUnit.SECONDS);
	}

	@Override
	public void dispose() {
		if (colorChange != null && !colorChange.isCancelled()) {
			colorChange.cancel(true);
		}
		if (pvch != null) {
			try {
				pvch.destroy();
			} catch (IllegalStateException | CAException e) {
				logger.error("Failed to destroy channel access to {}", pvch.getName(), e);
			}
		}
		super.dispose();
	}
}
