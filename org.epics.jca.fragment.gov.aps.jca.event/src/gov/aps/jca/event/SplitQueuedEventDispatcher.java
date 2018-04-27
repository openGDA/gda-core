package gov.aps.jca.event;

import gov.aps.jca.configuration.Configurable;
import gov.aps.jca.configuration.Configuration;
import gov.aps.jca.configuration.ConfigurationException;

import java.util.ArrayList;
import java.util.List;

/**
 * This EventDispatcher uses an internal thread to dispatch events and overrides older (obsolete) monitor values.
 */
@SuppressWarnings("rawtypes")
public class SplitQueuedEventDispatcher extends AbstractEventDispatcher implements Runnable, Configurable {

	private List<LatestMonitorOnlyQueuedEventDispatcher> dispatchers;

	private final LatestMonitorOnlyQueuedEventDispatcher otherDispatcher, monitorDispatcher, connectionDispatcher,
			putDispatcher;

	/**
	 * Constructor
	 */
	public SplitQueuedEventDispatcher() {
		dispatchers = new ArrayList<LatestMonitorOnlyQueuedEventDispatcher>();
		dispatchers.add(otherDispatcher = new LatestMonitorOnlyQueuedEventDispatcher());
		dispatchers.add(monitorDispatcher = new LatestMonitorOnlyQueuedEventDispatcher());
		dispatchers.add(connectionDispatcher = new LatestMonitorOnlyQueuedEventDispatcher());
		dispatchers.add(putDispatcher = new LatestMonitorOnlyQueuedEventDispatcher());
	}

	/**
	 * Process events in the queue as they are added. The queue must be synchronized for thread safety but we must be
	 * careful not to block for too long or else new events get blocked from being added to the queue causing long
	 * delays. The design is to process events in batches. This allows for the most efficient use of computer cycles
	 * since much of the time the queue will not be blocked. This method was modified by tap on 6/17/2004 to allow both
	 * efficient event processing and thread safety. Later optimized by msekoranja.
	 */
	@Override
	public void run() {
		for (LatestMonitorOnlyQueuedEventDispatcher dispatcher : dispatchers)
			dispatcher.run();
	}

	@Override
	public void dispose() {
		for (LatestMonitorOnlyQueuedEventDispatcher dispatcher : dispatchers)
			dispatcher.dispose();
	}

	/**
	 * @see gov.aps.jca.configuration.Configurable#configure(gov.aps.jca.configuration.Configuration)
	 */
	@Override
	public void configure(Configuration conf) throws ConfigurationException {
		for (LatestMonitorOnlyQueuedEventDispatcher dispatcher : dispatchers)
			dispatcher.configure(conf);
	}

	/**
	 * @return priority
	 */
	public int getPriority() {
		return otherDispatcher.getPriority();
	}

	/**
	 * @param priority
	 */
	public void setPriority(int priority) {
		for (LatestMonitorOnlyQueuedEventDispatcher dispatcher : dispatchers)
			dispatcher.setPriority(priority);
	}

	/**
	 * @see gov.aps.jca.event.EventDispatcher#dispatch(gov.aps.jca.event.ContextMessageEvent, java.util.List)
	 */
	@Override
	public void dispatch(ContextMessageEvent ev, List listeners) {
		otherDispatcher.dispatch(ev, listeners);
	}

	/**
	 * @see gov.aps.jca.event.EventDispatcher#dispatch(gov.aps.jca.event.ContextExceptionEvent, java.util.List)
	 */
	@Override
	public void dispatch(ContextExceptionEvent ev, List listeners) {
		otherDispatcher.dispatch(ev, listeners);
	}

	/**
	 * @see gov.aps.jca.event.EventDispatcher#dispatch(gov.aps.jca.event.ConnectionEvent, java.util.List)
	 */
	@Override
	public void dispatch(ConnectionEvent ev, List listeners) {
		connectionDispatcher.dispatch(ev, listeners);
	}

	/**
	 * @see gov.aps.jca.event.EventDispatcher#dispatch(gov.aps.jca.event.AccessRightsEvent, java.util.List)
	 */
	@Override
	public void dispatch(AccessRightsEvent ev, List listeners) {
		otherDispatcher.dispatch(ev, listeners);
	}

	/**
	 * @see gov.aps.jca.event.EventDispatcher#dispatch(gov.aps.jca.event.MonitorEvent, java.util.List)
	 */
	@Override
	public void dispatch(MonitorEvent ev, List listeners) {
		monitorDispatcher.dispatch(ev, listeners);
	}

	/**
	 * @see gov.aps.jca.event.EventDispatcher#dispatch(gov.aps.jca.event.GetEvent, java.util.List)
	 */
	@Override
	public void dispatch(GetEvent ev, List listeners) {
		otherDispatcher.dispatch(ev, listeners);
	}

	@Override
	public void dispatch(PutEvent ev, List listeners) {
		putDispatcher.dispatch(ev, listeners);
	}

	/**
	 * @see gov.aps.jca.event.AbstractEventDispatcher#dispatch(gov.aps.jca.event.ContextMessageEvent,
	 *      gov.aps.jca.event.ContextMessageListener)
	 */
	@Override
	public void dispatch(ContextMessageEvent ev, ContextMessageListener listener) {
		otherDispatcher.dispatch(ev, listener);
	}

	/**
	 * @see gov.aps.jca.event.AbstractEventDispatcher#dispatch(gov.aps.jca.event.ContextExceptionEvent,
	 *      gov.aps.jca.event.ContextExceptionListener)
	 */
	@Override
	public void dispatch(ContextExceptionEvent ev, ContextExceptionListener listener) {
		otherDispatcher.dispatch(ev, listener);
	}

	/**
	 * @see gov.aps.jca.event.AbstractEventDispatcher#dispatch(gov.aps.jca.event.ConnectionEvent,
	 *      gov.aps.jca.event.ConnectionListener)
	 */
	@Override
	public void dispatch(ConnectionEvent ev, ConnectionListener listener) {
		connectionDispatcher.dispatch(ev, listener);
	}

	/**
	 * @see gov.aps.jca.event.AbstractEventDispatcher#dispatch(gov.aps.jca.event.AccessRightsEvent,
	 *      gov.aps.jca.event.AccessRightsListener)
	 */
	@Override
	public void dispatch(AccessRightsEvent ev, AccessRightsListener listener) {
		otherDispatcher.dispatch(ev, listener);
	}

	/**
	 * @see gov.aps.jca.event.AbstractEventDispatcher#dispatch(gov.aps.jca.event.MonitorEvent,
	 *      gov.aps.jca.event.MonitorListener)
	 */
	@Override
	public void dispatch(MonitorEvent ev, MonitorListener listener) {
		monitorDispatcher.dispatch(ev, listener);
	}

	/**
	 * @see gov.aps.jca.event.AbstractEventDispatcher#dispatch(gov.aps.jca.event.GetEvent,
	 *      gov.aps.jca.event.GetListener)
	 */
	@Override
	public void dispatch(GetEvent ev, GetListener listener) {
		otherDispatcher.dispatch(ev, listener);
	}

	/**
	 * @see gov.aps.jca.event.AbstractEventDispatcher#dispatch(gov.aps.jca.event.PutEvent,
	 *      gov.aps.jca.event.PutListener)
	 */
	@Override
	public void dispatch(PutEvent ev, PutListener listener) {
		putDispatcher.dispatch(ev, listener);
	}

}