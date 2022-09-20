package org.dawnsci.common.live;

import static java.util.stream.Collectors.toList;

import gda.configuration.properties.LocalProperties;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.dawnsci.datavis.api.ILiveFileListener;
import org.dawnsci.datavis.api.ILiveFileService;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.core.IPropertyFilter.FilterAction;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.ui.CommandConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLiveFileService implements ILiveFileService {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractLiveFileService.class);
	
	private static final String EXTERNAL_FILE_TOPIC = "org.dawnsci.file.topic";
	private static final String GDA_SCAN_TOPIC = "gda.messages.scan";
	private static final String PROCESSING_TOPIC = "gda.messages.processing";
	private static final String SCAN_STATUS_STARTED = "STARTED";
	private static final String SCAN_STATUS_UPDATED = "UPDATED";
	private static final String SCAN_STATUS_FINISHED = "FINISHED";
	private static final String SCAN_STATUS_ABORTED = "ABORTED";
	private static final String MESSAGE_STATUS_FIELD = "status";
	private static final String MESSAGE_FILEPATH_FIELD = "filePath";
	private static final String MESSAGE_INITIAL_FILEPATH_FIELD = "initialFilePath";
	private static final String SWMR_STATUS = "swmrStatus";
	private static final String SWMR_ACTIVE = "ACTIVE";
	private static final String SWMR_ENABLED = "ENABLED";
	
	protected Set<ILiveFileListener> listeners = new HashSet<>();

	private ISubscriber<EventListener> scanSubscriber;
	private ISubscriber<EventListener> procSubscriber;
	private ISubscriber<EventListener> fileSubscriber;
	
	private IBeanListener<Map<String, Object>> scanListener;
	private IBeanListener<Map<String, Object>> operationListener;
	private IBeanListener<Map<String, Object>> fileListener;

	private AtomicReference<Runnable> atomicRunnable = new AtomicReference<>();
	private ExecutorService executor = Executors.newSingleThreadExecutor();

	private static final long MIN_REFRESH_TIME = LocalProperties.getAsInt("gda.live.file.min.refresh", 2000);
	
	private boolean attached = false;
	
	protected String[] initialFiles;
	
	private IEventService eventService;

	public void setEventService(IEventService service) {
		this.eventService = service;
	}
	
	protected void attach() throws Exception {

		final String suri = CommandConstants.getScanningBrokerUri();
		if (suri==null) return; // Nothing to start, standard DAWN.

		final URI uri = new URI(suri);

		scanSubscriber = eventService.createSubscriber(uri, GDA_SCAN_TOPIC);


		// We don't care about the scan request, removing it means that
		// all the points models and detector models to not have to resolve in
		// order to get the event.
		scanSubscriber.addProperty("scanRequest", FilterAction.DELETE); 
		scanSubscriber.addProperty("position", FilterAction.DELETE);

		if (scanListener == null) scanListener = new ScanListener();
		
		scanSubscriber.addListener(scanListener);
		
		procSubscriber = eventService.createSubscriber(uri, PROCESSING_TOPIC);

		if (operationListener == null) operationListener = new ScanListener();
		
		procSubscriber.addListener(operationListener);

		fileSubscriber = eventService.createSubscriber(uri, EXTERNAL_FILE_TOPIC);
		
		if (fileListener == null) fileListener = new FileBeanListener();
		
		fileSubscriber.addListener(fileListener);
		
		attached = true;
		
		Runnable r = () -> {
				List<String> allRunningFiles = getAllRunningFiles();
				
				handleFileLoad(allRunningFiles.toArray(new String[allRunningFiles.size()]), null, true);
				
				if (initialFiles != null) {
					List<String> notRunning = Arrays.stream(initialFiles).filter(f -> !allRunningFiles.contains(f)).collect(Collectors.toList());
					handleFileLoad(notRunning.toArray(new String[notRunning.size()]), null, false);
				}
			};

		Executors.newSingleThreadExecutor().execute(r);
	}
	
	private void detach() {
		if (scanSubscriber != null && scanListener != null) {
			scanSubscriber.removeListener(scanListener);
			try {
				scanSubscriber.disconnect();
			} catch (EventException e) {
				logger.error("Could not disconnect subscriber to scan topic", e);
			}
			scanSubscriber = null;
		}
		
		if (procSubscriber != null && operationListener != null) {
			procSubscriber.removeListener(operationListener);
			try {
				procSubscriber.disconnect();
			} catch (EventException e) {
				logger.error("Could not disconnect subscriber to processing topic", e);
			}
			procSubscriber = null;
		}
		
		if (fileSubscriber != null && fileListener != null) {
			fileSubscriber.removeListener(fileListener);
			try {
				fileSubscriber.disconnect();
			} catch (EventException e) {
				logger.error("Could not disconnect subscriber to file topic", e);
			}
			
			fileSubscriber = null;
		}
		
		attached = false;
	}
	
	
	public void addLiveFileListener(ILiveFileListener l) {
		listeners.add(l);
		if (!attached) {
			try {
				attach();
			} catch (Exception e) {
				logger.debug("Could not add attach live listener", e);
			}
			
		}
	}

	public void removeLiveFileListener(ILiveFileListener l) {
		listeners.remove(l);
		if (listeners.isEmpty()) {
			detach();
		}
	}
	
	
	public void runUpdate(Runnable runnable, boolean queue) {

		// not queued updates are allowed to be dropped
		// Atomic runnable used as a length 1 queue
		if (!queue) {
			Runnable current = atomicRunnable.getAndSet(runnable);

			// if current is a runnable, the internal runnable hasn't been taken,
			// no need to submit in this case, stops the queue being flooded.
			if (current != null) {
				return;
			}

			executor.submit(new Runnable() {

				@Override
				public void run() {
					Runnable run = atomicRunnable.getAndSet(null);
					if (run == null)
						return;
					run.run();

					try {
						// Wait so to not swamp the UI with updates
						Thread.sleep(MIN_REFRESH_TIME);
					} catch (InterruptedException e) {
						// do nothing;
					}
				}
			});
		} else {
			// queued are added to the executor queue
			// and not dropped (reloads etc)
			executor.submit(runnable);
		}
	}
	
	protected List<String> getAllRunningFiles(){
		List<String> fileNames = new ArrayList<>();
		fileNames.addAll(getRunningFiles(EventConstants.SUBMISSION_QUEUE,
				bean -> ((ScanBean) bean).getFilePath()));
		return fileNames;
	}
	
	private List<String> getRunningFiles(String submitQueueName, Function<StatusBean, String> fileNameMapper) {
		final List<StatusBean> beans = getRunningBeans(submitQueueName);
		return beans.stream()
				.filter(b -> b.getStatus().isActive())
				.map(fileNameMapper)
				.filter(Objects::nonNull)
				.collect(toList());
	}
	
	private List<StatusBean> getRunningBeans(String submitQueueName) {
		final String suri = CommandConstants.getScanningBrokerUri();
		if (suri != null) { // will be null for standard DAWN
			try (IJobQueue<StatusBean> queueConnection = eventService.createJobQueueProxy(new URI(suri), submitQueueName)) {
				return queueConnection.getRunningAndCompleted();
			} catch (Exception e) {
				// there may be no processing queue present, so we just log this as a warning
				logger.warn("Could not get running files for submission queue {}", submitQueueName);
			}
		}
		return Collections.emptyList();
	}
	
	public static String getDataServerHost() {
		String name = System.getProperty("org.eclipse.dawnsci.data.server.host");
		if (name==null) name = System.getProperty("GDA/gda.dataserver.host");
		if (name==null) name = System.getProperty("gda.dataserver.host");
		return name;
	}

	public static int getDataServerPort() {
		int port = Integer.getInteger("org.eclipse.dawnsci.data.server.port", -1);
		if (port<=0) port = Integer.getInteger("GDA/gda.dataserver.port", -1);
		if (port<=0) port = Integer.getInteger("gda.dataserver.port", -1);
		return port;
	}
	
	protected abstract void handleFileLoad(String[] file, String parent, boolean live);
	
	private class ScanListener implements IBeanListener<Map<String, Object>> {
		
		@Override
		public void beanChangePerformed(BeanEvent<Map<String, Object>> evt) {
			
			Map<String, Object> msg = evt.getBean();
			
			boolean doesStatusFieldExist = msg.containsKey(MESSAGE_STATUS_FIELD);

			if (!doesStatusFieldExist) {
				logger.error("Missing {} field in message", MESSAGE_STATUS_FIELD);
				return;
			}
			
			boolean swmrActive = msg.containsKey(SWMR_STATUS) && SWMR_ACTIVE.equals(msg.get(SWMR_STATUS));
			boolean swmrEnabled = msg.containsKey(SWMR_STATUS) && SWMR_ENABLED.equals(msg.get(SWMR_STATUS));
			
			if (!(swmrActive || swmrEnabled)) {
				//do nothing if swmr not active or enabled
				return;
			}
			
			if (msg.get(MESSAGE_FILEPATH_FIELD) == null || msg.get(MESSAGE_FILEPATH_FIELD).toString().isEmpty()) {
				//do nothing if filename null or empty
				return;
			}

			Object status = msg.get(MESSAGE_STATUS_FIELD);
			if (status.equals(SCAN_STATUS_UPDATED)) {
				for (ILiveFileListener l : listeners) {
					l.refreshRequest();
				}
			}
			
			final String filePath = (String) msg.get(MESSAGE_FILEPATH_FIELD);
			// Scan started
			if (status.equals(SCAN_STATUS_STARTED)) {
				String parent = msg.containsKey(MESSAGE_INITIAL_FILEPATH_FIELD) ? msg.get(MESSAGE_INITIAL_FILEPATH_FIELD).toString() : null;
				handleFileLoad(new String[] {filePath}, parent, true);
			}

			if (status.equals(SCAN_STATUS_FINISHED) || status.equals(SCAN_STATUS_ABORTED)) {
				for (ILiveFileListener l : listeners) {
					l.localReload(filePath, false);
				}
			}
		}
	}
	
	private class FileBeanListener implements IBeanListener<Map<String, Object>> {
		
		@Override
		public void beanChangePerformed(BeanEvent<Map<String, Object>> evt) {
			
			Map<String, Object> msg = evt.getBean();
			boolean doesFilepathFieldExist = msg.containsKey(MESSAGE_FILEPATH_FIELD);

			if (!doesFilepathFieldExist) {
				logger.error("Missing {} field in message", MESSAGE_FILEPATH_FIELD);
				return;
			}

			String filePath = (String) msg.get(MESSAGE_FILEPATH_FIELD);
			handleFileLoad(new String[] {filePath}, null, false);

		}

	}
	
}