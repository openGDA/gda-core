package gda.util;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.callable;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.python.core.PyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.factory.FindableBase;
import gda.jython.Jython;
import gda.jython.logging.PythonException;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * RemoteCommandRunner that prevents command being run multiple times concurrently
 * <br>
 * Multiple commands can also be configured such that only one of them may be
 * running at any one time.
 */
@ServiceInterface(RemoteCommandRunner.class)
public class SingleCommandRunner extends FindableBase implements RemoteCommandRunner, InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(SingleCommandRunner.class);

	/** Lock to ensure only one command can be run at a time */
	private ReentrantLock lock = new ReentrantLock();
	/** The running state of this command (and any others that share state) */
	private AtomicBoolean running = new AtomicBoolean();
	/** The command to run. Command will be formatted with arguments to runCommand method */
	private String commandFormat;
	/** The Jython instance to run the commands */
	private Jython commandRunner;

	@Override
	public void runCommand(Serializable... arguments) {
		checkState();
		String command = String.format(commandFormat, (Object[])arguments);
		run(command);
	}

	/** Ensure that the required fields are set before continuing */
	private void checkState() {
		if (commandFormat == null || commandRunner == null || lock == null) {
			throw new IllegalStateException("RemoteTaskRunner is not configured correctly");
		}
	}

	private void run(String command) {
		try {
			if (lock.tryLock() && running.compareAndSet(false, true)) {
				Async.call(callable(() -> commandRunner.exec(command)))
						.onSuccess(this::success)
						.onFailure(this::failure);
			} else {
				throw new IllegalStateException("Command already running");
			}
		} finally {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

	private void failure(Throwable error) {
		try {
			if (error instanceof PyException) {
				logger.error("Command failed", PythonException.from((PyException)error));
			} else {
				logger.error("Command failed", error);
			}
		} finally {
			complete();
		}
	}

	// Needs to implement Consumer<Object>
	private void success(@SuppressWarnings("unused") Object result) {
		logger.debug("command complete");
		complete();
	}

	private void complete() {
		lock.lock();
		try {
			running.set(false);
		} finally {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

	public void setSharedState(AtomicBoolean state) {
		try {
			if (lock.tryLock() && !running.get()) {
				running = requireNonNull(state, "Shared state cannot be null");
			} else {
				throw new IllegalStateException("Can't update shared state while command is running");
			}
		} finally {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

	public void setCommand(String command) {
		commandFormat = requireNonNull(command, "Command cannot be null");
	}

	public void setRunner(Jython runner) {
		commandRunner = requireNonNull(runner, "Command runner cannot be null");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		requireNonNull(lock, "Lock must be set");
		requireNonNull(commandRunner, "Command Runner must be set");
		requireNonNull(commandFormat, "Command must be set");
	}
}
