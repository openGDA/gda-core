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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.expressions.IExpressionEngine;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.scan.PointEnd;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.api.device.models.ExpressionWatchdogModel;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.sequencer.ServiceHolder;
import org.eclipse.scanning.sequencer.expression.ServerExpressionService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitors an expression of scannables and if one of the values changes, reevaluates the expression.
 * <p>
 * Example XML configuration:
 *
 * <pre>
  {@literal <!-- We assume that beam is available if the ring current is above background level -->}
  {@literal <!-- and the port shutter is open -->}
  {@literal <bean id="beam_available_model" class="org.eclipse.scanning.api.device.models.ExpressionWatchdogModel">}
  {@literal     <property name="expression" value="ring_current >= 1.0 &amp;&amp; !portshutter.equalsIgnoreCase(&quot;Closed&quot;)"/>}
  {@literal     <property name="message" value="Beam has been lost"/>}
  {@literal </bean>}

  {@literal <bean id="beam_available_watchdog" class="org.eclipse.scanning.sequencer.watchdog.ExpressionWatchdog" init-method="activate">}
  {@literal     <property name="name" value="beam_available_watchdog" />}
  {@literal     <property name="model" ref="beam_available_model"/>}
  {@literal </bean>}
 * </pre>
 *
 * @author Matthew Gerring
 *
 */
public class ExpressionWatchdog extends AbstractWatchdog<ExpressionWatchdogModel> implements IPositionListener {

	private static final Logger logger = LoggerFactory.getLogger(ExpressionWatchdog.class);

	private IExpressionEngine engine;
	private IPosition lastCompletedPoint;
	private List<IScannable<?>> scannablesToRestore;
	private MapPosition mapPosition;
	private IPositioner positioner;

	private List<IScannable<?>> scannables;
	private static IExpressionService expressionService;

	public ExpressionWatchdog() {
		super();
	}

	public ExpressionWatchdog(ExpressionWatchdogModel model) {
		super();
		setModel(model);
	}

	@Override
	protected String getId() {
		return model.getExpression();
	}

	/**
	 * Called on a thread when the position changes.
	 */
	@Override
	public void positionChanged(PositionEvent evt) {
		checkPosition(evt.getPosition());
	}

	@Override
	public void positionPerformed(PositionEvent evt) {
		checkPosition(evt.getPosition());
	}

	/***
	 * Get the scannables to restore from the  ExpressionWatchdogModel if the current scannablesToRestore object is null.
	 * We do not want to overwrite the existing value for scannablesToRestore if it is already set.
	 *
	 * @return scannablesToRestore
	 */
	private List<IScannable<?>> getScannablesToRestore(){
		if(scannablesToRestore==null) {
			scannablesToRestore = new ArrayList<IScannable<?>>();
			// Iterate through the list of names of scannables in the ExpressionWatchdogModel
			for (String s : model.getScannablesToRestore()) {
				try {
					// If the scannable does not exist this will throw a ScanningException here
					IScannable<?> scannable = getScannable(s);
					scannablesToRestore.add(scannable);
				}catch(ScanningException e){
					// Log the error and then continue
					logger.error("Cannot get scannable "+s+":"+e.getMessage());
				}

			}
		}
		return scannablesToRestore;
	}

	private void checkPosition(IPosition pos) {
		try {
			if (engine == null) {
				return;
			}
			if (pos.getNames().size() != 1) {
				return;
			}
			final String name = pos.getNames().get(0);
			engine.addLoadedVariable(name, pos.get(name));
			checkExpression(true);
		} catch (Exception ne) {
			logger.error("Cannot process position " + pos, ne);
		}
	}

	private IPositioner getPositioner() throws ScanningException {
		if(positioner==null) {
			positioner = ServiceHolder.getRunnableDeviceService().createPositioner(ExpressionWatchdog.class.getName());
		}
		return positioner;
	}

	private boolean checkExpression(boolean requirePause) throws Exception {
		final Boolean ok = engine.evaluate();

		if (requirePause) {
			if (!ok) {
				/**
				 * Get the position of the energyScannable as soon as the scan is paused.
				 * Pause may be called again after it is already paused, but before scan has been resumed,
				 * In this instance we do not want to overwrite the position already saved as the motor may have
				 * drifted during the pause, so we want the earliest value possible.
				 * */
				if(mapPosition==null) {
					mapPosition = new MapPosition();
					for (IScannable<?> scannable : getScannablesToRestore()) {
						mapPosition.put(scannable.getName(), scannable.getPosition());
					}
				}
				controller.pause(getId(), model); // Will not pause if already paused.
			} else {
				if (lastCompletedPoint != null) {
					controller.seek(getId(), lastCompletedPoint.getStepIndex());
				}

				// Set the energy position back to what it was when pause was called
				if(mapPosition!=null) {
					getPositioner().setPosition(mapPosition);
					mapPosition=null;
				}
				controller.resume(getId()); // Will not resume unless paused by us
			}
		}
		return ok;
	}

	@ScanStart
	public void start(ScanBean bean, IPosition firstPosition) throws ScanningException {
		logger.debug("Expression Watchdog starting on {}", controller.getName());
		try {
			this.engine = getExpressionService().getExpressionEngine();

			engine.createExpression(model.getExpression()); // Parses expression, may send exception on syntax
			Collection<String> names = engine.getVariableNamesFromExpression();

			// get the scannables, checking that they all implement IPositionListenable
			// and add all their current positions to the expression engine
			this.scannables = new ArrayList<>(names.size());
			for (String name : names) {
				IScannable<?> scannable = getScannable(name);
				scannables.add(scannable);

				if (!(scannable instanceof IPositionListenable))
					throw new ScanningException(name + " is not a position listenable!");

				engine.addLoadedVariable(scannable.getName(), scannable.getPosition());
			}

			// Check the expression now to set the initial state of the watchdog
			checkExpression(true);

			// Add this watchdog as listeners to all the scannables
			for (IScannable<?> scannable : scannables) {
				((IPositionListenable) scannable).addPositionListener(this);
			}

			checkPosition(firstPosition);

			logger.debug("Expression Watchdog started on {}", controller.getName());
		} catch (ScanningException ne) {
			throw ne; // If there is something badly wrong a proper scanning exception will be prepared and thrown
		} catch (Exception ne) {
			logger.error("Cannot start watchdog!", ne);
		}
	}

	@PointEnd
	public void pointEnd(IPosition done) {
		this.lastCompletedPoint = done;
	}

	@ScanFinally
	public void stop() {
		logger.debug("Expression Watchdog stopping on {}", controller.getName());
		try {
			if (scannables != null) {
				for (IScannable<?> scannable : scannables) {
					((IPositionListenable)scannable).removePositionListener(this);
				}
				scannables.clear();
			}
			engine = null;

		} catch (Exception ne) {
			logger.error("Cannot stop watchdog!", ne);
		}
		logger.debug("Expression Watchdog stopped on {}", controller.getName());
	}


	private BundleContext bcontext;

	public IExpressionService getExpressionService() {
		if (expressionService==null) {
			ServiceReference<IExpressionService> ref = bcontext.getServiceReference(IExpressionService.class);
			if (ref!=null) expressionService = bcontext.getService(ref);
		}
		return expressionService;
	}

	public void setExpressionService(IExpressionService eservice) {
		ExpressionWatchdog.expressionService = eservice;
	}

	public static void setTestExpressionService(ServerExpressionService eservice) {
		expressionService = eservice;
	}

	public void start(ComponentContext context) {
		this.bcontext = context.getBundleContext();
	}

}
