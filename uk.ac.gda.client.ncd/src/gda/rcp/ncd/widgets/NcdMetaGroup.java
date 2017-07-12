/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.rcp.ncd.widgets;

import java.text.DecimalFormat;

import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import uk.ac.diamond.daq.msgbus.MsgBus;
import uk.ac.diamond.daq.scm.api.events.NcdMetaType;
import uk.ac.diamond.daq.scm.api.events.NcdMsg;
import uk.ac.diamond.scisoft.analysis.io.NexusDiffractionCalibrationReader;


public class NcdMetaGroup extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(NcdMetaGroup.class);

	final String type;
	private Label distance;
	private Label center_x;
	private Label center_y;

	public NcdMetaGroup(Composite parent, String type) {
		super(parent, SWT.NONE);
		this.type = type;
		GridDataFactory gdf = GridDataFactory.fillDefaults();

		setLayout(new GridLayout());
		setLayoutData(gdf.grab(true, true).create());

		Group group = new Group(this, SWT.NONE);
		group.setLayout(new GridLayout(2, true));
		group.setText(type + " Calibration");
		group.setLayoutData(gdf.align(SWT.FILL, SWT.FILL).create());


		Label distLabel = new Label(group, SWT.NONE);
		distLabel.setLayoutData(gdf.create());
		distLabel.setText("Distance");

		distance = new Label(group, SWT.READ_ONLY|SWT.RIGHT);
		distance.setLayoutData(gdf.create());
		distance.setText("n/a");

		Label xLabel = new Label(group, SWT.NONE);
		xLabel.setLayoutData(gdf.create());
		xLabel.setText("Centre x");

		center_x = new Label(group, SWT.READ_ONLY|SWT.RIGHT);
		center_x.setLayoutData(gdf.create());
		center_x.setText("n/a");

		Label yLabel = new Label(group, SWT.NONE);
		yLabel.setLayoutData(gdf.create());
		yLabel.setText("Centre y");

		center_y = new Label(group, SWT.READ_ONLY|SWT.RIGHT);
		center_y.setLayoutData(gdf.create());
		center_y.setText("n/a");

		addDisposeListener(e -> dispose());
		MsgBus.subscribe(this);
	}

	@Subscribe
	public void update(NcdMsg.StatusUpdate upd) {
		if (upd.getDetectorType() == null || !upd.getDetectorType().equals(type)) {
			return;
		}
		if (upd.getMetaType() != NcdMetaType.CALIBRATION) {
			return;
		}
		if (upd.getFilepath() != null && !upd.getFilepath().isEmpty()) {
			updateDetail(upd.getFilepath());
		} else {
			Display.getDefault().asyncExec(this::clearGui);
		}
	}

	private void updateDetail(String filepath) {
		logger.info("{} - Loading calibration from {}", type, filepath);
		try {
			IDiffractionMetadata dm = NexusDiffractionCalibrationReader.getDiffractionMetadataFromNexus(filepath, null);
			final double dist = dm.getDetector2DProperties().getBeamCentreDistance();
			double[] bc = dm.getDetector2DProperties().getBeamCentreCoords();
			final double x = bc[0];
			final double y = bc[1];

			Display.getDefault().asyncExec(() -> updateGui(dist, x, y));
		} catch (Exception e) {
			logger .error("Couldn't load calibration detail", e);
			Display.getDefault().asyncExec(this::clearGui);
		}
	}

	private void updateGui(double dist, double x, double y) {
		if (!isDisposed()) {
			DecimalFormat form = new DecimalFormat(",###.00");
			center_x.setText(String.format("%8s", form.format(x)));
			distance.setText(String.format("%8s", form.format(dist)));
			center_y.setText(String.format("%8s", form.format(y)));
		}
	}

	private void clearGui() {
		if (!isDisposed()) {
			distance.setText("n/a");
			center_x.setText("n/a");
			center_y.setText("n/a");
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		MsgBus.unsubscribe(this);
	}
}
