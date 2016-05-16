/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.eventbus;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import uk.ac.gda.eventbus.GDAEventBus.DefaultDeadEventHandler;

public class GDAEventBusExample {

	private static final Logger logger = LoggerFactory.getLogger(GDAEventBusExample.class);

	@SuppressWarnings("unused")
	private static void example() {
		GDAEventBus bus = new GDAEventBus(); // identifier "default"

		// Simple handlers might be anonymous sub types of Object
		bus.register(new Object() {
			@Subscribe
			public void openingGambit(String s) {
				logger.info("read " + (s.trim().split("\\s").length == 1 ? "single " : "multi-") + "word String: \"{}\"", s);
			}
		});

		// Subscribers to Object will receive any (and all!) events
		class AnyEventHandler {
			@Subscribe
			public void anyAndAll(Object o) {
				logger.debug("saw {}: {}", o.getClass().getName(), o.toString());
			}
		}

		// Handlers can subscribe to multiple event types/subtypes
		class NumericEventHandler<T> { // parameterised for demonstration purposes
			@Subscribe
			public void lessThanZero(Integer i) {
				logger.info("determined Integer to be " + (i < 0 ? "negative" : "positive") + ": {}", i);
			}
			@Subscribe
			public void lessThanZero(Double d) {
				logger.info("determined Double to be " + (d < 0 ? "negative" : "positive") + ": {}", d);
			}
			@Subscribe
			public void whichClass(T n) { // https://github.com/google/guava/issues/1549 ?
				logger.debug("passed object of type " + n.getClass().getName() + ": {}", n);
			}
		}

		// Instantiate super type handlers as needed for debugging
		final AnyEventHandler anyEventHandler = new AnyEventHandler();
		bus.register(anyEventHandler);
		// but keep a reference for when you are done
		bus.unregister(anyEventHandler);

		// DefaultDeadEventHandler receives events without subscribers
		bus.register(new DefaultDeadEventHandler(logger));

		// Having left generic handlers until it was unavoidable...
		bus.register(new NumericEventHandler<Number>());

		// Let's test
		bus.post(1);
		bus.post(-2.0);
		bus.post("Three little birds");
	}

	public static void main(String[] args) throws Exception {
//		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.apache.activemq.transport")).setLevel(Level.INFO);
//		((ch.qos.logback.classic.Logger) logger).setLevel(Level.INFO);
//		example();
		testActiveMq();
	}

	private static void testActiveMq() throws InterruptedException, JMSException {

		// create
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
		GDAEventBus eventBus1 = new GDAEventBus("eventBus1", connectionFactory);
		GDAEventBus eventBus2 = new GDAEventBus("eventBus2", connectionFactory);
//		eventBus2 = eventBus1; // can be the same!

		// ready
		Object handler = new Object() {
			@Subscribe
			public void print(Object event) {
				logger.debug("received event: {}", event);
				System.out.println(event);
			}
		};

		// set
		eventBus1.register(handler);
		eventBus2.register(handler);

		// go
		eventBus1.post("String from eventBus1");
		eventBus2.publish("String from eventBus2");
		eventBus1.publish(1);
		eventBus2.post(2);

		// wait expectantly
		Thread.sleep(3000);

		eventBus1.cleanUp();
		eventBus2.cleanUp();
	}

}
