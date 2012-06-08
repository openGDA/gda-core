/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.util.userOptions;

import java.io.Serializable;

/**
 * @param <S>
 * @param <T>
 */
public class UserOption<S, T> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3868569116255386440L;

	S description;

	T defaultValue;

	public T getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description.toString();
	}

	/**
	 * 
	 */
	public T value;

	/**
	 * @return value
	 */
	public T getValue() {
		return value;
	}

	/**
	 * @param value
	 */
	public void setValue(T value) {
		this.value = value;
	}

	/**
	 * @param description
	 * @param defaultValue
	 */
	public UserOption(S description, T defaultValue) {
		this(description, defaultValue, null);
	}

	/**
	 * @param description
	 * @param defaultValue
	 * @param value
	 */
	public UserOption(S description, T defaultValue, T value) {
		if (description == null || defaultValue == null)
			throw new IllegalArgumentException("title == null || defaultValue == null ");
		this.description = description;
		this.defaultValue = defaultValue;
		this.value = value != null ? value : defaultValue;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean equals(Object o) {
		if (!(o instanceof UserOption))
			return false;
		UserOption other = (UserOption) o;
		if (other.defaultValue.getClass() != defaultValue.getClass())
			return false;
		if (other.description.getClass() != description.getClass())
			return false;
		return description.equals(other.description) && defaultValue.equals(other.defaultValue)
				&& value.equals(other.value);
	}

	@Override
	public String toString() {
		return description.toString() + ":"+value.toString() + "[" + defaultValue.toString() + "]";
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	
}
