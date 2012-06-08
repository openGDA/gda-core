package gov.aps.jca.event;

import gov.aps.jca.JCALibrary;
import gov.aps.jca.configuration.Configurable;
import gov.aps.jca.configuration.Configuration;
import gov.aps.jca.configuration.ConfigurationException;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This EventDispatcher uses an internal thread to dispatch events and overrides older (obsolete) monitor values.
 */
@SuppressWarnings("unchecked")
public class LatestMonitorOnlyQueuedEventDispatcher extends AbstractEventDispatcher implements Runnable, Configurable {

	static protected int _count = 0;

	protected volatile boolean _killed = false;

	protected int _priority = Thread.NORM_PRIORITY;

	protected Thread _dispatcherThread;

	protected ArrayList<Event> _queue;

	protected int _queueLimit = 100;

	protected Map<Object,SynchronizedLimitedInt> _sourcesEventCount;

	protected int _limit = 5;

	protected Map<Object, Event> _overrideMap;

	/**
	 * Constructor
	 */
	@SuppressWarnings("rawtypes")
	public LatestMonitorOnlyQueuedEventDispatcher() {
		_dispatcherThread = uk.ac.gda.util.ThreadManager.getThread(this, "LatestMonitorOnlyQueuedEventDispatcher-" + (_count++));
		_dispatcherThread.setDaemon(true);
		setPriority(JCALibrary.getInstance().getPropertyAsInt(
				LatestMonitorOnlyQueuedEventDispatcher.class.getName() + ".priority", _priority));
		_queue = new ArrayList();
		_queueLimit = JCALibrary.getInstance().getPropertyAsInt(
				LatestMonitorOnlyQueuedEventDispatcher.class.getName() + ".queue_limit", _queueLimit);
		if (_queueLimit < 10)
			_queueLimit = 10;

		_sourcesEventCount = new HashMap();
		_limit = JCALibrary.getInstance().getPropertyAsInt(
				LatestMonitorOnlyQueuedEventDispatcher.class.getName() + ".channel_queue_limit", _limit);
		if (_limit < 3)
			_limit = 3;

		_overrideMap = new HashMap();

		_dispatcherThread.start();
	}

	abstract class Event {
		CAEvent _ev;

		final EventListener _listener;

		final Object[] _listeners;

		final Object _overrideId;

		Event(CAEvent ev, Object[] listeners) {
			this(ev, listeners, null);
		}

		Event(CAEvent ev, Object[] listeners, Object overrideId) {
			_ev = ev;
			_listener = null;
			_listeners = listeners;
			_overrideId = overrideId;
		}

		Event(CAEvent ev, EventListener listener) {
			this(ev, listener, null);
		}

		Event(CAEvent ev, EventListener listener, Object overrideId) {
			_ev = ev;
			_listener = listener;
			_listeners = null;
			_overrideId = overrideId;
		}

		/**
		 * 
		 */
		abstract public void dispatch();
	}

	protected void queueEvent(Event ev) {
		// logger.debug(ev + ">>>>>>>>");
		// increment counter, will block if limit will be reached
		// avoid deadlock allowing recursive queue-ing
		boolean doNotBlock = (Thread.currentThread() == _dispatcherThread);
		// logger.debug(ev + " before getSyncCounter(ev)");
		SynchronizedLimitedInt syncCounter = getSyncCounter(ev);
		if (_killed) {
			// logger.debug(ev + "<<<<<<");
			return;
		}
		// logger.debug(ev + "before syncCounter.increment(doNotBlock)");
		syncCounter.increment(doNotBlock);

		// logger.debug(ev + "before synchronized (_queue)");
		synchronized (_queue) {
			// logger.debug(ev + "after synchronized (_queue) - queue size = "
			// + _queue.size());
			if (!doNotBlock && _queue.size() >= _queueLimit) {
				try {
					// logger.debug(ev + "going into wait state");
					_queue.wait();
					// logger.debug(ev + "leaving wait state");
				} catch (InterruptedException e) {
				}
			}

			// logger.debug(ev + "before _queue.add(ev)");
			_queue.add(ev);
			// notify event arrival
			// logger.debug(ev + "before _queue.notifyAll()");
			_queue.notifyAll();
		}
		// logger.debug(ev + "<<<<<<");
	}

	private SynchronizedLimitedInt getSyncCounter(Event ev) {
		Object source = ev._ev.getSource();

		SynchronizedLimitedInt sli = null;
		synchronized (_sourcesEventCount) {
			sli = _sourcesEventCount.get(source);
			if (sli == null) {
				sli = new SynchronizedLimitedInt(0, _limit);
				_sourcesEventCount.put(source, sli);
			}
		}
		return sli;
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
		// eventBatch is local and only referenced by a single thread so we
		// don't have to synchronize it
		int eventsToProcess = 0;
		Event[] eventBatch = new Event[0];

		while (!_killed) {
			try {
				// for performance reasons we don't want to block for too long
				// synchronize _queue for thread safety
				// copy all of the new queued events to the local batch and
				// clear _queue
				synchronized (_queue) {
					// wait for new requests
					if (!_killed && _queue.isEmpty())
						_queue.wait();

					if (!_killed && !_queue.isEmpty()) {
						eventsToProcess = _queue.size();
						// create new instance of batch array only if necessary
						if (eventsToProcess > eventBatch.length)
							eventBatch = new Event[eventsToProcess];

						// only copy (will not recreate array)
						_queue.toArray(eventBatch);
						_queue.clear();

						// notify queue clean-up
						_queue.notifyAll();
					}
				}

				// process all events in the local batch until it is empty
				for (int i = 0; !_killed && i < eventsToProcess; i++) {
					// catch all exceptions, so that one buggy listener does
					// not harm the others
					final Event event = eventBatch[i];
					try {
						// remove from override id
						final Object overrideId = eventBatch[i]._overrideId;
						if (overrideId != null) {
							// logger
							// .debug(">>>>>>>>>>>before synchronized (_overrideMap) ");
							synchronized (_overrideMap) {
								_overrideMap.remove(overrideId);
							}
							// logger
							// .debug("<<<<<<<<<<<after synchronized (_overrideMap) ");
						}
						// logger.debug(">>>>>>>>>>>before event.dispatch() for "
						// + event._ev.toString());
						event.dispatch();
						// logger.debug("<<<<<<<<<<<after event.dispatch() for "
						// + event._ev.toString());
					} catch (Throwable th) {
						th.printStackTrace();
					}

					// decrement counter
					getSyncCounter(event).decrement();

					eventBatch[i] = null; // allow to be gc'ed
					Thread.yield();
				}

			} catch (Throwable th) {
				th.printStackTrace();
			}
		}
	}

	/**
	 * @see gov.aps.jca.event.AbstractEventDispatcher#dispose()
	 */
	@Override
	public void dispose() {
		_killed = true;
		_dispatcherThread = null;

		// notify _queue
		synchronized (_queue) {
			_queue.notifyAll();
		}

		// destroy all locks
		synchronized (_sourcesEventCount) {
			Iterator<SynchronizedLimitedInt> iter = _sourcesEventCount.values().iterator();
			while (iter.hasNext())
				(iter.next()).destroy();
		}

		// clear _overrideMap
		synchronized (_overrideMap) {
			_overrideMap.clear();
		}
	}

	/**
	 * @see gov.aps.jca.configuration.Configurable#configure(gov.aps.jca.configuration.Configuration)
	 */
	@Override
	public void configure(Configuration conf) throws ConfigurationException {
		int priority = getPriority();
		try {
			priority = conf.getChild("priority").getValueAsInteger();
		} catch (Exception ex) {
			priority = conf.getAttributeAsInteger("priority", priority);
		}
		setPriority(priority);
	}

	/**
	 * @return priority
	 */
	public int getPriority() {
		return _priority;
	}

	/**
	 * @param priority
	 */
	public void setPriority(int priority) {
		if (_killed) {
			throw new IllegalStateException("Dispatcher thread has been killed");
		}
		_priority = priority;
		_dispatcherThread.setPriority(_priority);
	}

	/**
	 * @see gov.aps.jca.event.EventDispatcher#dispatch(gov.aps.jca.event.ContextMessageEvent, java.util.List)
	 */
	@Override
	public void dispatch(ContextMessageEvent ev, @SuppressWarnings("rawtypes") List listeners) {
		if (_killed)
			return;
		queueEvent(new Event(ev, listeners.toArray()) {
			@Override
			public void dispatch() {
				for (int t = 0; t < _listeners.length; ++t) {
					if (_listeners[t] instanceof ContextMessageListener) {
						((ContextMessageListener) _listeners[t]).contextMessage((ContextMessageEvent) _ev);
					}
				}
			}
		});
	}

	/**
	 * @see gov.aps.jca.event.EventDispatcher#dispatch(gov.aps.jca.event.ContextExceptionEvent, java.util.List)
	 */
	@Override
	public void dispatch(ContextExceptionEvent ev, @SuppressWarnings("rawtypes") List listeners) {
		if (_killed)
			return;
		queueEvent(new Event(ev, listeners.toArray()) {
			@Override
			public void dispatch() {
				for (int t = 0; t < _listeners.length; ++t) {
					if (_listeners[t] instanceof ContextExceptionListener) {
						((ContextExceptionListener) _listeners[t]).contextException((ContextExceptionEvent) _ev);
					}
				}
			}
		});
	}

	/**
	 * @see gov.aps.jca.event.EventDispatcher#dispatch(gov.aps.jca.event.ConnectionEvent, java.util.List)
	 */
	@Override
	public void dispatch(ConnectionEvent ev, @SuppressWarnings("rawtypes") List listeners) {
		if (_killed)
			return;
		queueEvent(new Event(ev, listeners.toArray()) {
			@Override
			public void dispatch() {
				for (int t = 0; t < _listeners.length; ++t) {
					if (_listeners[t] instanceof ConnectionListener) {
						((ConnectionListener) _listeners[t]).connectionChanged((ConnectionEvent) _ev);
					}
				}
			}
		});
	}

	/**
	 * @see gov.aps.jca.event.EventDispatcher#dispatch(gov.aps.jca.event.AccessRightsEvent, java.util.List)
	 */
	@Override
	public void dispatch(AccessRightsEvent ev, @SuppressWarnings("rawtypes") List listeners) {
		if (_killed)
			return;
		queueEvent(new Event(ev, listeners.toArray()) {
			@Override
			public void dispatch() {
				for (int t = 0; t < _listeners.length; ++t) {
					if (_listeners[t] instanceof AccessRightsListener) {
						((AccessRightsListener) _listeners[t]).accessRightsChanged((AccessRightsEvent) _ev);
					}
				}
			}
		});
	}

	/**
	 * @see gov.aps.jca.event.EventDispatcher#dispatch(gov.aps.jca.event.MonitorEvent, java.util.List)
	 */
	@Override
	public void dispatch(MonitorEvent ev, @SuppressWarnings("rawtypes") List listeners) {
		if (_killed)
			return;

		// object creation opt. tweak
		if (listeners.size() == 1) {
			dispatch(ev, (MonitorListener) listeners.get(0));
			return;
		}

		// override check, add to override map or override old event
		synchronized (_overrideMap) {
			Event existingEvent = _overrideMap.get(ev.getSource());
			if (existingEvent != null) {
				existingEvent._ev = ev;
			} else {
				final Event event = new Event(ev, listeners.toArray(), ev.getSource()) {
					@Override
					public void dispatch() {
						for (int t = 0; t < _listeners.length; ++t) {
							if (_listeners[t] instanceof MonitorListener) {
								((MonitorListener) _listeners[t]).monitorChanged((MonitorEvent) _ev);
							}
						}
					}
				};
				_overrideMap.put(ev.getSource(), event);
				queueEvent(event);
			}
		}
	}

	/**
	 * @see gov.aps.jca.event.EventDispatcher#dispatch(gov.aps.jca.event.GetEvent, java.util.List)
	 */
	@Override
	public void dispatch(GetEvent ev, @SuppressWarnings("rawtypes") List listeners) {
		if (_killed)
			return;
		queueEvent(new Event(ev, listeners.toArray()) {
			@Override
			public void dispatch() {
				for (int t = 0; t < _listeners.length; ++t) {
					if (_listeners[t] instanceof GetListener) {
						((GetListener) _listeners[t]).getCompleted((GetEvent) _ev);
					}
				}
			}
		});
	}

	/**
	 * @see gov.aps.jca.event.EventDispatcher#dispatch(gov.aps.jca.event.PutEvent, java.util.List)
	 */
	@Override
	public void dispatch(PutEvent ev, @SuppressWarnings("rawtypes") List listeners) {
		if (_killed)
			return;
		queueEvent(new Event(ev, listeners.toArray()) {
			@Override
			public void dispatch() {
				for (int t = 0; t < _listeners.length; ++t) {
					if (_listeners[t] instanceof PutListener) {
						((PutListener) _listeners[t]).putCompleted((PutEvent) _ev);
					}
				}
			}
		});
	}

	/**
	 * @see gov.aps.jca.event.AbstractEventDispatcher#dispatch(gov.aps.jca.event.ContextMessageEvent,
	 *      gov.aps.jca.event.ContextMessageListener)
	 */
	@Override
	public void dispatch(ContextMessageEvent ev, ContextMessageListener cml) {
		if (_killed)
			return;
		queueEvent(new Event(ev, cml) {
			@Override
			public void dispatch() {
				try {
					((ContextMessageListener) _listener).contextMessage((ContextMessageEvent) _ev);
				} catch (Throwable th) {
					th.printStackTrace();
				}
			}
		});

	}

	/**
	 * @see gov.aps.jca.event.AbstractEventDispatcher#dispatch(gov.aps.jca.event.ContextExceptionEvent,
	 *      gov.aps.jca.event.ContextExceptionListener)
	 */
	@Override
	public void dispatch(ContextExceptionEvent ev, ContextExceptionListener cel) {
		if (_killed)
			return;
		queueEvent(new Event(ev, cel) {
			@Override
			public void dispatch() {
				try {
					((ContextExceptionListener) _listener).contextException((ContextExceptionEvent) _ev);
				} catch (Throwable th) {
					th.printStackTrace();
				}
			}
		});
	}

	/**
	 * @see gov.aps.jca.event.AbstractEventDispatcher#dispatch(gov.aps.jca.event.ConnectionEvent,
	 *      gov.aps.jca.event.ConnectionListener)
	 */
	@Override
	public void dispatch(ConnectionEvent ev, ConnectionListener cl) {
		if (_killed)
			return;
		queueEvent(new Event(ev, cl) {
			@Override
			public void dispatch() {
				try {
					((ConnectionListener) _listener).connectionChanged((ConnectionEvent) _ev);
				} catch (Throwable th) {
					th.printStackTrace();
				}
			}
		});
	}

	/**
	 * @see gov.aps.jca.event.AbstractEventDispatcher#dispatch(gov.aps.jca.event.AccessRightsEvent,
	 *      gov.aps.jca.event.AccessRightsListener)
	 */
	@Override
	public void dispatch(AccessRightsEvent ev, AccessRightsListener arl) {
		if (_killed)
			return;
		queueEvent(new Event(ev, arl) {
			@Override
			public void dispatch() {
				try {
					((AccessRightsListener) _listener).accessRightsChanged((AccessRightsEvent) _ev);
				} catch (Throwable th) {
					th.printStackTrace();
				}
			}
		});
	}

	/**
	 * @see gov.aps.jca.event.AbstractEventDispatcher#dispatch(gov.aps.jca.event.MonitorEvent,
	 *      gov.aps.jca.event.MonitorListener)
	 */
	@Override
	public void dispatch(MonitorEvent ev, MonitorListener ml) {
		// logger.debug(">>>>>>>");
		if (_killed)
			return;

		// override check, add to override map or override old event
		Event event = null;
		synchronized (_overrideMap) {
			Event existingEvent = _overrideMap.get(ev.getSource());
			if (existingEvent != null && existingEvent._listener == ml) {
				// logger.debug(">>>>>>>1");
				existingEvent._ev = ev;
			} else {
				// logger
				// .debug(">>>>>>> overriding - will need to be removed from overrideMap");
				event = new Event(ev, ml, ev.getSource()) {
					@Override
					public void dispatch() {
						try {
							((MonitorListener) _listener).monitorChanged((MonitorEvent) _ev);
						} catch (Throwable th) {
							th.printStackTrace();
						}
					}
				};
				_overrideMap.put(ev.getSource(), event);
			}
		}
		if (event != null) {
			queueEvent(event);
		}
		// logger.debug("<<<<<<");
	}

	/**
	 * @see gov.aps.jca.event.AbstractEventDispatcher#dispatch(gov.aps.jca.event.GetEvent,
	 *      gov.aps.jca.event.GetListener)
	 */
	@Override
	public void dispatch(GetEvent ev, GetListener gl) {
		if (_killed)
			return;
		queueEvent(new Event(ev, gl) {
			@Override
			public void dispatch() {
				try {
					((GetListener) _listener).getCompleted((GetEvent) _ev);
				} catch (Throwable th) {
					th.printStackTrace();
				}
			}
		});
	}

	/**
	 * @see gov.aps.jca.event.AbstractEventDispatcher#dispatch(gov.aps.jca.event.PutEvent,
	 *      gov.aps.jca.event.PutListener)
	 */
	@Override
	public void dispatch(PutEvent ev, PutListener pl) {
		if (_killed)
			return;
		queueEvent(new Event(ev, pl) {
			@Override
			public void dispatch() {
				try {
					((PutListener) _listener).putCompleted((PutEvent) _ev);
				} catch (Throwable th) {
					th.printStackTrace();
				}
			}
		});
	}

}