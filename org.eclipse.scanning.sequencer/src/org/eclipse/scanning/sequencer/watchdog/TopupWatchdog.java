/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.sequencer.watchdog;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.scan.PointEnd;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.api.device.models.TopupWatchdogModel;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.status.WatchdogStatusRecord.WatchdogState;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * <pre>
 * This watchdog may be started to run with a scan.
 *
 * It will attempt to pause a scan when topup is about
 * to happen and restart it after topup has finished.
 *
 * Once made a watch dog is active if the activate method
 * is called. The deactivate method may be called to stop
 * a given watchdog watching scans.
 *
 * https://en.wikipedia.org/wiki/Watchdog_timer
 *
 * NOTE This class will usually be created in spring
 *
 * Note there are 3 PVs to describe the topup state. This implementation assumes
 * that the scannable referred to by the countdown property of the model
 * wraps the PV SR-CS-FILL-01:COUNTDOWN.
 *
 * A typical top-up event takes around 20s and in low-alpha mode this can be longer, maybe around a minute.
 * Will Rogers and Nick Battam are the ones who know and maintain this page.
 * If you are missing information on this page, please ask them to explain and possibly add information to the page.
 * The gruesome details of the top-up application are here: http://confluence.diamond.ac.uk/x/QpRTAQ

SR-CS-FILL-01:COUNTDOWN: this is a float-valued counter that runs to zero
at the start of TopUp and remains there until the fill is complete when
it resets to time before next TopUp fill,
</pre>

<h3> Example XML configuration</h3>

<pre>
	{@literal <!--  Watchdog Example -->}
	{@literal <bean id="topupModel" class="org.eclipse.scanning.api.device.models.TopupWatchdogModel">}
	{@literal     <property name="countdownName" value="topup" />}
	{@literal     <property name="cooloff" value="4000" />}
	{@literal     <property name="warmup" value="5000" />}

	{@literal     <!-- Optional, recommended but not compulsory a scannable linked to SR-CS-FILL-01:TOPUPMODE, checks the mode is right -->}
	{@literal     <property name="modeName" value="mode" />}

	{@literal     <!-- Optional, do not usually need to set -->}
	{@literal     <property name="period" value="600000"/>}
	{@literal     <property name="topupTime" value="15000"/>}
	{@literal     <!-- End optional, do not usually need to set -->}
	{@literal </bean>}

	{@literal <bean id="topupWatchdog" class="org.eclipse.scanning.sequencer.watchdog.TopupWatchdog" init-method="activate">}
	{@literal     <property name="model" ref="topupModel"/>}
	{@literal </bean>}
</pre>

<h3>Calculation of scannable parts of topup  </h3>
	<pre>

	|<-w->|
	|.
	|  .
	|    .
	|      .
	|        .
	|          .
	|            .
	|              .|<-   c  ->|
	|                .
	|                  .
	|                    .
	|                      .
	|                        . |<-Tf->|
	|                          ........    t
	|
	|__________________________________(time)

	|<-              p              ->|

	w  - warmup
	c  - cooloff
	t  - time until next topup next occurs
	Tf - Topup fill time (variable but max 15s in normal mode)
	p  - Period of cycle, usually 10mins or so.

	In order for scanning to run, all of the following conditions must be satisfied:

	1. Mode is normal (8)
	2. t > c
	3. t < (p-Tf)-w

	</pre>

<h3>Topup Mode</h3>

The "Topup Mode" PV is SR-CS-FILL-01:TOPUPMODE.

This is an enum PV that has three possible values<ul>
<li>Normal,</li>
<li>Low Alpha,</li>
<li>Low Alpha THz</li>
</ul>

The Topup watchdog should only be used when we are in 'Normal' mode.

 * @author Matthew Gerring
 *
 */
public class TopupWatchdog extends AbstractWatchdog<TopupWatchdogModel> implements IPositionListener {

	private static Logger logger = LoggerFactory.getLogger(TopupWatchdog.class);

	private static final String NORMAL_TOPUP_MODE_VALUE = "Normal";

	private static final String[] DECAY_MODE_STATES = {"Off", "Failed", "No process"};

	private String countdownUnit;
	private volatile IPosition lastCompletedPoint;

	private volatile boolean busy = false;
	private volatile boolean rewind = false;

	private boolean decayMode = false;

	private WatchdogState state;

	public TopupWatchdog() {
		super();
	}

	public TopupWatchdog(TopupWatchdogModel model) {
		super();
		setModel(model);
	}

	@Override
	public String getId() {
		return getClass().getName();
	}

	/**
	 * Called on a thread when the position changes.
	 * The countdown is likely to report at 10Hz. TODO Check if this is ok during a scan and does not
	 * use too much CPU.
	 */
	@Override
	public void positionChanged(PositionEvent evt) {
		try {
			checkPosition(evt.getDevice().getName(), evt.getPosition());
		} catch (Exception ne) {
			logger.error("Cannot process position {}", evt.getPosition(), ne);
		}
	}


	/**
	 * Checks the position during the scan and at startup.
	 * @param pos
	 */
	protected void checkPosition(String deviceName, IPosition pos) throws Exception {
		if (deviceName.equals(model.getCountdownName())) {
			final long time = getValueMs(pos, model.getCountdownName(), countdownUnit);
			processCountdownPosition(time);
		} else if (deviceName.equals(model.getStateName())) {
			checkTopupState();
			processTopupState();
		}
	}

	/**
	 * If beam is on decay mode and the scan is paused by a top up event, resume the scan.
	 * Scan will not resume if it was paused by the user.
	 */
	private void processTopupState() throws Exception {
		if (onDecayMode()) {
			if (rewind && lastCompletedPoint!=null) {
				// We paused when topup was already ongoing: rewind first
				rewindToLastCompletedPoint();
			}
			state = WatchdogState.RESUMING;
			controller.resume(getId());
		}
	}

	/**
	 * This method may be called at around 10Hz. In order to reduce
	 * CPU, we could disable events at the start of topup but this may not detect
	 * beam dump so might not be desirable. If the beam is dumped, pos also
	 * goes to 0 so the devices will be paused. In this case rewind must be called
	 * because
	 * @param t in ms or 0 if topup is happening, or -1 is no beam.
	 * @throws ScanningException
	 */
	private void processCountdownPosition(long t) throws Exception {

		// It's 10Hz, we can ignore events if we are doing something.
		// We ignore events while processing an event.
		// Events are frequent and blocking is bad.
		if (busy) {
			logger.info("Event '{}'@{} has been ignored.", t, model.getCountdownName());
			return;
		}

		// It's 10Hz don't write much in here other than
		// simple tests or FPE's
		try {
			busy = true;
			if (!isPositionValid(t)) {
				rewind = t<0; // We did not detect it before losing beam
				// Only pause if beam is not on decay mode
				if (!onDecayMode()) {
					state = WatchdogState.PAUSING;
					controller.pause(getId(), getModel());
				}
			} else {
				// We are a valid place in the topup: we can resume the scan
				if (rewind && lastCompletedPoint!=null) {
					// We paused when topup was already ongoing: rewind first
					rewindToLastCompletedPoint();
				}
				state = WatchdogState.RESUMING;
				controller.resume(getId());
			}
		} finally {
			busy = false;
		}
	}

	private void rewindToLastCompletedPoint() {
		try {
			controller.seek(getId(), lastCompletedPoint.getStepIndex()); // Probably only does something useful for malcolm
		} catch (Exception e) {
			logger.error("Error seeking; step index = {}", lastCompletedPoint.getStepIndex(), e);
		} finally {
			rewind = false;
		}
	}

	private boolean isPositionValid(long time) {
		long warmup  = model.getWarmup();
		long cooldown  = model.getCooloff();
		long period  = model.getPeriod(); // TODO: should use period scannable for this
		long topupTime = model.getTopupTime();

		return time > cooldown && time < (period - topupTime) - warmup;
	}

	@ScanStart
	public void start(ScanBean bean) throws Exception {
		logger.debug("Watchdog starting on {}", controller.getName());
		checkTopupMode(); // check the topup mode is as expected, throws exception if not
		try {
			// if topup state is configured, set the current decay mode and add listener to the scannable
			if (model.getStateName() != null) {
				checkTopupState();
				addPositionListener(model.getStateName());
			}

			IScannable<?> topup = addPositionListener(model.getCountdownName());
			if (countdownUnit==null) this.countdownUnit = topup.getUnit();

			long time = getValueMs(((Number)topup.getPosition()).doubleValue(), countdownUnit);
			processCountdownPosition(time); // Pauses the starting scan if topup already running.

			logger.debug("Watchdog started on {}", controller.getName());
		} catch (Exception ne) {
			logger.error("Cannot start watchdog!", ne);
		}
	}

	private IScannable<?> addPositionListener(String scannableName) throws ScanningException {
		IScannable<?> scannable = getScannable(scannableName);
		if (!(scannable instanceof IPositionListenable)) {
			throw new ScanningException(model.getCountdownName()+" is not a position listenable!");
		}
		((IPositionListenable)scannable).addPositionListener(this);
		return scannable;
	}

	private void removePositionListener(String scannableName) throws ScanningException {
		final IScannable<?> scannable = getScannable(scannableName);
		((IPositionListenable) scannable).removePositionListener(this);
	}

	private void checkTopupMode() throws ScanningException {
		// A scannable may optionally be defined to check that the mode of the machine
		// fits with this watch dog. If it does not then there will be a nice exception
		// to the user and the scan will fail. This watch dog should not be operational
		// unless the mode is 'Normal'
		if (model.getModeName() != null) {
			final IScannable<?> mode = getScannable(model.getModeName());
			final String smode = String.valueOf(mode.getPosition());
			if (!NORMAL_TOPUP_MODE_VALUE.equals(smode)) {
				throw new ScanningException("The machine is in low alpha or another mode where "+getClass().getSimpleName()+" cannot be used!");
			}
		}
	}

	/**
	 * Decay mode is on if the topup state pv value equals to 'Off', 'Failed' or 'No process'
	 */
	private void checkTopupState() throws ScanningException {
		String topupState = String.valueOf(getScannable(model.getStateName()).getPosition());
		decayMode = Arrays.stream(DECAY_MODE_STATES).anyMatch(topupState::equals);
	}

	@PointEnd
	public void pointEnd(IPosition done) {
		this.lastCompletedPoint = done;
	}

	@ScanFinally
	public void stop() {
		logger.debug("Watchdog stopping on {}", controller.getName());
		try {
			if (model.getStateName() != null) {
				removePositionListener(model.getStateName());
			}
			removePositionListener(model.getCountdownName());
			logger.info("Watchdog stopped on {}", controller.getName());
		} catch (ScanningException ne) {
			logger.error("Cannot stop watchdog!", ne);
		}
	}

	protected long getValueMs(IPosition ipos, String name, String unit) {
		final double pos = ipos.getDouble(name);
		return getValueMs(pos, unit);
	}

	protected long getValueMs(double pos, String unit) {
		final TimeUnit timeUnit = getTimeUnit(unit);
		return switch (timeUnit) {
			case MILLISECONDS -> Math.round(pos);
			case SECONDS -> Math.round(pos * 1000);
			case MINUTES -> Math.round(pos * 1000 * 60);
			default -> throw new RuntimeException("Unexpected unit" + timeUnit);
		};
	}

	private static final TimeUnit getTimeUnit(String unit) {
		return switch (unit.toLowerCase()) {
			case "ms", "milliseconds" -> TimeUnit.MILLISECONDS;
			case "s", "seconds" -> TimeUnit.SECONDS;
			case "m", "min" -> TimeUnit.MINUTES;
			default -> TimeUnit.SECONDS;
		};
	}

	public String getCountdownUnit() {
		return countdownUnit;
	}

	public void setCountdownUnit(String countdownUnit) {
		this.countdownUnit = countdownUnit;
	}

	private boolean onDecayMode() {
		return decayMode;
	}

	@Override
	public boolean isPausing() {
 		if (state != null) {
			return isActive() && state.equals(WatchdogState.PAUSING);
		} else {
			return false;
		}
	}


}
