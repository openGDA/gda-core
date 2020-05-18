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

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.scan.PointEnd;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.api.device.models.TopupWatchdogModel;
import org.eclipse.scanning.api.event.scan.ScanBean;
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
    {@literal 	  <property name="modeName" value="mode" />}

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

	private String countdownUnit;
	private volatile IPosition lastCompletedPoint;

	private volatile boolean busy = false;
	private volatile boolean rewind = false;

	public TopupWatchdog() {
		super();
	}

	public TopupWatchdog(TopupWatchdogModel model) {
		super();
		setModel(model);
	}

	@Override
	protected String getId() {
		return getClass().getName();
	}

	/**
	 * Called on a thread when the position changes.
	 * The countdown is likely to report at 10Hz. TODO Check if this is ok during a scan and does not
	 * use too much CPU.
	 */
	@Override
	public void positionChanged(PositionEvent evt) {
		checkPosition(evt.getPosition());
	}

	/**
	 * Checks the position during the scan and at startup.
	 * @param pos
	 */
	protected void checkPosition(IPosition pos) {
		try {
			// Topup is currently 10Hz which is the rate that the scannable should call positionChanged(...) at.
			final long time = getValueMs(pos, model.getCountdownName(), countdownUnit);
			processPosition(time);
		} catch (Exception ne) {
			logger.error("Cannot process position {}", pos, ne);
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
	private void processPosition(long t) throws Exception {

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
				rewind = t<0; // We did not detect it before loosing beam
				controller.pause(getId(), getModel());
			} else { // We are a valid place in the topup, see if we can resume
				// the warmup period has ended, we can resume the scan
				if (rewind && lastCompletedPoint!=null) {
					controller.seek(getId(), lastCompletedPoint.getStepIndex()); // Probably only does something useful for malcolm
					rewind = false;
				}
				controller.resume(getId());
			}
		} finally {
			busy = false;
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
			// Get the topup, the unit and add a listener
			IScannable<?> topup = getScannable(model.getCountdownName());
			if (countdownUnit==null) this.countdownUnit = topup.getUnit();
			if (!(topup instanceof IPositionListenable)) {
				throw new ScanningException(model.getCountdownName()+" is not a position listenable!");
			}
			((IPositionListenable)topup).addPositionListener(this);

			long time = getValueMs(((Number)topup.getPosition()).doubleValue(), countdownUnit);
			processPosition(time); // Pauses the starting scan if topup already running.

			logger.debug("Watchdog started on {}", controller.getName());
		} catch (Exception ne) {
			logger.error("Cannot start watchdog!", ne);
		}
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

	@PointEnd
	public void pointEnd(IPosition done) {
		this.lastCompletedPoint = done;
	}

	@ScanFinally
	public void stop() {
		logger.debug("Watchdog stopping on {}", controller.getName());
		try {
			final IScannable<?> topup = getScannable(model.getCountdownName());
			((IPositionListenable) topup).removePositionListener(this);

			logger.info("Watchdog stopped on {}", controller.getName());
		} catch (ScanningException ne) {
			logger.error("Cannot stop watchdog!", ne);
		}
	}

	public String getCountdownUnit() {
		return countdownUnit;
	}

	public void setCountdownUnit(String countdownUnit) {
		this.countdownUnit = countdownUnit;
	}

}
