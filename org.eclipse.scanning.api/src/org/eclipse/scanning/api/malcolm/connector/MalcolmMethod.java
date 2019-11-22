package org.eclipse.scanning.api.malcolm.connector;

import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;

/**
 * An enumeration of the malcolm methods that can be set in a {@link MalcolmMessage}.
 *
 * @author Matthew Dickie
 */
public enum MalcolmMethod {

	ABORT,
	CONFIGURE,
	DISABLE,
	PAUSE,
	RESET,
	RESUME,
	RUN,
	VALIDATE;

	/**
	 * Returns the name of this message in lower case, as expected by the malcolm device
	 * over a communication channel.
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return name().toLowerCase();
	}

	/**
	 * Returns the {@link MalcolmMethod} enum constant object for the given name, or
	 * throws {@link IllegalArgumentException} if no such method exists
	 * @param methodName
	 * @return the enum constants from this enum for the given method name
	 * @throws IllegalArgumentException if no such method exists
	 */
	public static MalcolmMethod fromString(String methodName) {
		return MalcolmMethod.valueOf(methodName.toUpperCase());
	}

}
