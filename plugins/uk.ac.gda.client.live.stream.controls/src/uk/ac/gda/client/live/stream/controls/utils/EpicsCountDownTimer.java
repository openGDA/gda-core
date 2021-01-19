package uk.ac.gda.client.live.stream.controls.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.epics.connection.EpicsController;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorListener;

/**
 * An {@link IObservable} provides count down time update to the
 * {@link IObserver}s.
 * 
 * It monitors a EPICS PV {@link #countdownTriggerPvName} to trigger start or
 * stop the count down process with a count down time obtained from another
 * EPICS PV {@link #countdownTimePvName} and a user-specified update interval
 * {@link #updateIntervalInSeconds}. Users also need to specify update text
 * format {@link #timeFormatInSeconds}
 * 
 * @author fy65
 *
 */
public class EpicsCountDownTimer extends ObservableComponent {
	private static final Logger logger = LoggerFactory.getLogger(EpicsCountDownTimer.class);
	protected static final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	// Spring bean properties
	private String countdownTriggerPvName;
	private String countdownTimePvName;
	protected Long updateIntervalInSeconds; // in seconds
	private String timeFormatInSeconds = "%4d";

	protected CountDownTimer countDownTimer;
	private Channel startChannel;
	private Monitor startMonitor;
	protected Channel timeChannel;
	protected long countdownTime;

	protected final ExecutorService executroService = Executors.newSingleThreadExecutor();

	private MonitorListener ml = event -> {

			DBR dbr = event.getDBR();
			if (dbr.isENUM()) {
				short value = ((DBR_Enum) dbr).getEnumValue()[0];
				if (value == 1) {
					startCountdown();
				} else if (value == 0) {
					concelCountdown();
				}
			}
	};

	protected void concelCountdown() {
		if (countdownTime > 100) { //stop call cancel when count down time less than and equal to 100.
			executroService.submit(() -> {
				if (countDownTimer != null) {
					logger.info("Cancel count down timer.");
					countDownTimer.cancel();
				}
			});
		}
	}

	protected void startCountdown() {
		executroService.submit(() -> {
			try {
				double cagetDouble = EPICS_CONTROLLER.cagetDouble(timeChannel);
				countDownTimer = new CountDownTimer((long) cagetDouble * 1000, updateIntervalInSeconds * 1000) {
					long watchedValue = 0;

					@Override
					public void onTick(long millisUntilFinished) {
						if (watchedValue != millisUntilFinished) {
							watchedValue = countdownTime = millisUntilFinished;
							notifyIObservers(EpicsCountDownTimer.this, String.format(timeFormatInSeconds, millisUntilFinished / 1000));
						}
					}

					@Override
					public void onFinish() {
						if (watchedValue != 0) {
							watchedValue = countdownTime = 0;
							notifyIObservers(EpicsCountDownTimer.this, String.format(timeFormatInSeconds, 0));
						}
					}
				};
				logger.info("Start count down timer.");
				countDownTimer.start();
			} catch (TimeoutException | CAException e) {
				logger.error("Failed to get count down time from {}", timeChannel.getName(), e);
			} catch (InterruptedException e) {
				logger.error("interrupted while getting count down time from {}", timeChannel.getName(), e);
				Thread.currentThread().interrupt();
			}
		});
	}

	public void init() {
		if (countdownTriggerPvName == null) {
			throw new IllegalStateException("coundownTriggerPvName is not set.");
		} else {
			try {
				startChannel = EPICS_CONTROLLER.createChannel(countdownTriggerPvName);
				startMonitor = EPICS_CONTROLLER.setMonitor(startChannel, ml);
				logger.info("Connect to {} and add monitor listener", countdownTriggerPvName);
			} catch (CAException | TimeoutException e) {
				logger.error("Failed to connect to {}", countdownTriggerPvName, e);
			} catch (InterruptedException e) {
				logger.error("Failed to add a monitor listener to {}", countdownTriggerPvName, e);
				Thread.currentThread().interrupt();
			}
		}
		if (countdownTimePvName == null) {
			throw new IllegalStateException("countdowmTimePvName is not set.");
		} else {
			try {
				timeChannel = EPICS_CONTROLLER.createChannel(countdownTimePvName);
				logger.info("Connect to {}", countdownTimePvName);
			} catch (CAException | TimeoutException e) {
				logger.error("Failed to connect to {}", countdownTimePvName, e);
			}
		}
		if (updateIntervalInSeconds == null) {
			throw new IllegalStateException("updateIntervalInSeconds is not set.");
		}
		if (timeFormatInSeconds == null) {
			throw new IllegalStateException("timeFormatInSeconds is not set.");
		}
	}

	public void destroy() {
		if (startMonitor != null) {
			startMonitor.removeMonitorListener(ml);
			startMonitor = null;
		}
		if (startChannel != null) {
			try {
				startChannel.destroy();
				startChannel = null;
			} catch (IllegalStateException | CAException e) {
				logger.error("exception on destroy channel {}", startChannel.getName());
			}
		}
		if (timeChannel != null) {
			try {
				timeChannel.destroy();
				timeChannel = null;
			} catch (IllegalStateException | CAException e) {
				logger.error("exception on destroy channel {}", timeChannel.getName());
			}
		}
		if (executroService != null) {
			executroService.shutdown();
			try {
				if (!executroService.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
					executroService.shutdownNow();
				}
			} catch (InterruptedException e) {
				executroService.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}
		if (countDownTimer != null) {
			countDownTimer.dispose();
		}
	}

	public String getCountdownTriggerPvName() {
		return countdownTriggerPvName;
	}

	public void setCountdownTriggerPvName(String countdownTriggerPvName) {
		this.countdownTriggerPvName = countdownTriggerPvName;
	}

	public String getCountdownTimePvName() {
		return countdownTimePvName;
	}

	public void setCountdownTimePvName(String countdownTimePvName) {
		this.countdownTimePvName = countdownTimePvName;
	}

	public long getUpdateIntervalInSeconds() {
		return updateIntervalInSeconds;
	}

	public void setUpdateIntervalInSeconds(long updateIntervalInSeconds) {
		this.updateIntervalInSeconds = updateIntervalInSeconds;
	}

	public String getTimeFormatInSeconds() {
		return timeFormatInSeconds;
	}

	public void setTimeFormatInSeconds(String timeFormatInSeconds) {
		this.timeFormatInSeconds = timeFormatInSeconds;
	}

}
