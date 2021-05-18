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

package gda.device.detector.addetector.collectionstrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import gda.configuration.properties.LocalProperties;
import gda.epics.connection.EpicsController;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;
import gda.util.functions.ThrowingFunction;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

/**
 * An EPICS Processing Variable (PV) decorator to configure any given PV parameter before data collection using scan and
 * restore this PV to its original value after scan.
 *
 *<p>
 * This can be used to configure a PV of different device that does not appear in the scan command.
 *<p>
 * It supports Spring <a href="https://docs.spring.io/spring/docs/4.2.x/spring-framework-reference/html/expressions.html">SPEL</a>
 * expression in bean definition.
 * In order to evaluate the expression lazily and dynamically, plain String text expression must be used in bean definition,
 * NOT the expression surrounded by #{..}.
 *<br/>
 * This decorator must be inside of {@link ConfigureAcquireTimeDecorator} in collection strategy bean definition
 * if the specified expression depends on acquire time (see example below). Otherwise it can be at any place in the decorator chain before {@link SoftwareStartStop}.
 *<p>
 * When {@link #isEnabled()} returns false, it does nothing but pass through to its decoratee.
 *<p>
 * It has built in support for dummy mode operation off beamline, which simply print message to Jython terminal.
 *<p>
 * Example usage:
 * <pre>
 * {@code
	<bean id="kbRasteringPeriod" class="gda.device.detector.addetector.collectionstrategy.ProcessingVariableDecorator">
		<property name="pvName" value="BL06I-EA-SGEN-01:PERIOD" />
   		<property name="expression" value="@medipix_adbase.getAcquireTime() gt 0.1 ? @medipix_adbase.getAcquireTime() : 0.1"/>
		<property name="enabled" value="true" />
		<property name="restorePvValue" value="true" />
		<property name="decoratee" ref="softstatrstop"/>
	</bean>
	}
 *</pre>
 */
public class ProcessingVariableDecorator extends AbstractADCollectionStrategyDecorator implements ApplicationContextAware {
	private static final Logger logger = LoggerFactory.getLogger(ProcessingVariableDecorator.class);
	/**
	 * EPICS Utility
	 */
	private static final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
	/**
	 * Map that stores the channel against the PV name
	 */
	private Map<String, Channel> channelMap = new HashMap<>();

	private String pvName;
	private String expression;
	private boolean enabled=false;
	private boolean restorePvValue = false;
	private double pvValueSaved=1.0;

	private ApplicationContext applicationContext;

	@Override
	protected void rawPrepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {

		if (isEnabled()) {
			ExpressionParser parser=new SpelExpressionParser();
			var context = new StandardEvaluationContext();
			context.setBeanResolver((ec, name) -> Finder.find(name) != null ? Finder.find(name) :  applicationContext.getBean(name));
			double newValue = parser.parseExpression(getExpression()).getValue(context, Double.class);

			if (LocalProperties.isDummyModeEnabled()) {
				print(String.format("set %s to %f", getPvName(), newValue));
			} else {
				setPvValue(newValue);
			}
		}
		getDecoratee().prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo); // must be called before detector initialise
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (getPvName() == null) {
			throw new IllegalArgumentException("'pvName' needs to be declared");
		}
		if (getExpression() == null) {
			throw new IllegalArgumentException("'expression' needs to be declared");
		}
		if (getDecoratee()==null) throw new IllegalStateException("'decoratee' is not set!");
		super.afterPropertiesSet();
	}

	// CollectionStrategyDecoratableInterface interface

	@Override
	public void saveState() throws Exception {
		getDecoratee().saveState();
		if (isEnabled()) {
			logger.trace("saveState() called, restorePvValue={}", restorePvValue);
			if (isRestorePvValue()) {
				if (LocalProperties.isDummyModeEnabled()) {
					print(String.format("get PV value %f from %s", pvValueSaved, getPvName()));
				} else {
					pvValueSaved=getPvValue();
				}
				logger.debug("Saved PV value now pvValueSaved={}", pvValueSaved);
			}
		}
	}

	@Override
	public void restoreState() throws Exception {
		if (isEnabled()) {
			logger.trace("restoreState() called, restorePvValue={}, pvValueSaved={}", restorePvValue, pvValueSaved);
			if (isRestorePvValue()) {
				if (LocalProperties.isDummyModeEnabled()) {
					print(String.format("set %s to %f", getPvName(), pvValueSaved));
				} else {
					setPvValue(pvValueSaved);
				}
				logger.debug("Restored state to pvValueSaved={}", pvValueSaved);
			}
		}
		getDecoratee().restoreState();
	}

	public double getPvValue() throws TimeoutException, CAException, InterruptedException {
		return EPICS_CONTROLLER.cagetDouble(getChannel(getPvName()).orElseThrow());
	}

	public void setPvValue(double value) throws CAException, InterruptedException {
		print(String.format("set %s to %f", getPvName(), value));
		EPICS_CONTROLLER.caput(getChannel(getPvName()).orElseThrow(), value);
	}

	/**
	 * Lazy initialize channels and store them in a map for retrieval later. Intentionally designed to cope with channel creation failure. This way it will not
	 * block data collection from other PVs.
	 *
	 * @param pv
	 * @return channel - Optional<Channel>
	 */
	private Optional<Channel> getChannel(String pv) {
		ThrowingFunction<String, Channel> f = EPICS_CONTROLLER::createChannel;
		Channel channel = null;
		try {
			channel = channelMap.computeIfAbsent(pv, f);
			logger.trace("Created channel for PV: {}", pv);
		} catch (RuntimeException e) {
			logger.error("Cannot create CA Channel for {}", pv, e);
		}
		return Optional.ofNullable(channel);
	}

	public String getPvName() {
		return pvName;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	public boolean isRestorePvValue() {
		return restorePvValue;
	}

	public void setRestorePvValue(boolean restorePvValue) {
		this.restorePvValue = restorePvValue;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}
	/**
	 * method to print message to the Jython Terminal console.
	 *
	 * @param msg
	 */
	private void print(String msg) {
		if (InterfaceProvider.getTerminalPrinter() != null) {
			InterfaceProvider.getTerminalPrinter().print(msg);
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
}
