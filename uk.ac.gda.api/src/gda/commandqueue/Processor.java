/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.commandqueue;

import gda.observable.IObservable;
/**
 * Processor handles the processing of a collection of items, one item at a time.
 * The processing can be started, stopped and paused
 * The processor can be skip the processing of the current item,
 * 
 * ProcessListeners can register for updates on changes to the state of the 
 * processor.
 */
public interface Processor extends IObservable{
	enum STATE{UNKNOWN, WAITING_START, WAITING_QUEUE, PROCESSING_ITEMS}
	public void stop(long timeout_ms) throws Exception;
	public void pause(long timeout_ms) throws Exception;
	public void skip(long timeout_ms) throws Exception;
	public void start(long timeout_ms) throws Exception;
	public void stopAfterCurrent() throws Exception;
	public STATE getState();
	public ProcessorCurrentItem getCurrentItem()  throws Exception;
}
