package uk.ac.gda.client.live.stream.controls.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

public class EpicsCountDownTimerProgress extends EpicsCountDownTimer {
	private static final Logger logger = LoggerFactory.getLogger(EpicsCountDownTimerProgress.class);

	@Override
	protected void startCountdown() {
		executroService.submit(() -> {
			try {
				double cagetDouble = EPICS_CONTROLLER.cagetDouble(timeChannel);
				countDownTimer = new CountDownTimer((long) cagetDouble * 1000, updateIntervalInSeconds * 1000) {
					long watchedValue = 0;
					long acquireTime = (long) cagetDouble * 1000;

					@Override
					public void onTick(long millisUntilFinished) {
						if (watchedValue != millisUntilFinished) {
							watchedValue = countdownTime = millisUntilFinished;
							notifyIObservers(EpicsCountDownTimerProgress.this, ((acquireTime-millisUntilFinished)*100 /acquireTime));
						}
					}

					@Override
					public void onFinish() {
						if (watchedValue != 0) {
							watchedValue = countdownTime = 0;
							notifyIObservers(EpicsCountDownTimerProgress.this,100);
						}
					}
				};
				logger.info("Start count down timer.");
				countDownTimer.start();
			} catch (TimeoutException | CAException e) {
				logger.error("Failed to get count down time from {}", timeChannel.getName(), e);
			} catch (InterruptedException e) {
				logger.error("Interrupted while get count down time from {}", timeChannel.getName(), e);
				Thread.currentThread().interrupt();			}
		});
	}

}
