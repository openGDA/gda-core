/***********************************************************************************************************************
 * Copyright (C)2000 - Just van den Broecke - See license below *
 **********************************************************************************************************************/

// package nl.justobjects.toolkit.sys;
package gda.util;

/**
 * Alarm listener callback interface.
 * <h3>Purpose and Responsibilities</h3>
 * Implementations of this interface can be registered at an {@link Alarm} to receive a callback with a specified
 * (optional) argument at a specified time. Author Just van den Broecke
 */
public interface AlarmListener {

	/**
	 * Alarm callback.
	 * 
	 * @param anAlarm
	 *            the alarm performing the callback
	 */
	void alarm(Alarm anAlarm);
}

/*
 * Copyright (C)2000 Just A. van den Broecke <just@justobjects.nl> This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU
 * General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place -
 * Suite 330, Boston, MA 02111-1307, USA.
 */
