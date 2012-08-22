/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.scan;

/**
 * InterruptedException to be thrown by the Scan mechanism.
 * <p>
 * This enables user-friendly messages to be printed out, while containing a useful stack trace to report where the
 * original InterruptedException was thrown from.
 * <p>
 * Extends InterruptedException so error handling inside Jython scripts that run scans can look for this type of
 * exception and react accordingly.
 */
public class ScanInterruptedException extends InterruptedException {

	private StackTraceElement[] stackTrace;

	public ScanInterruptedException() {
		super();
		stackTrace = Thread.getAllStackTraces().get(Thread.currentThread());
	}

	public ScanInterruptedException(String s) {
		super(s);
		stackTrace = Thread.getAllStackTraces().get(Thread.currentThread());
	}

	public ScanInterruptedException(String s, StackTraceElement[] stackTrace) {
		super(s);
		this.stackTrace = stackTrace;
	}

	@Override
	public StackTraceElement[] getStackTrace() {
		return stackTrace;
	}

}
