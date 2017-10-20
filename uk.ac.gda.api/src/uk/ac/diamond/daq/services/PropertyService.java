package uk.ac.diamond.daq.services;

/**
 * A service for accessing properties.
 *
 * @Author James Mudd
 */
public interface PropertyService {

	/**
	 * Get the value of a property as a {@code String}. Equivalent to {@code getAsString(property, null)}
	 *
	 * @param property
	 *            The property to lookup
	 * @return The property value or null if not set
	 * @see #getAsString(String)
	 * @see #getAsString(String, String)
	 */
	public default String get(String property) {
		return getAsString(property);
	}

	/**
	 * Get the value of a property as a {@code String}. Equivalent to {@code getAsString(property, null)}
	 *
	 * @param property
	 *            The property to lookup
	 * @return The property value or null if not set
	 * @see #getAsString(String, String)
	 */
	public default String getAsString(String property) {
		return getAsString(property, null);
	}

	/**
	 * Get the value of a property as a {@code String} with a default.
	 *
	 * @param property
	 *            The property to lookup
	 * @param defaultValue
	 *            The value to return if the property is not set
	 * @return The property value or the defaultValue if not set
	 * @see #getAsString(String)
	 */
	public String getAsString(String property, String defaultValue);

	/**
	 * Get the value of a property as an {@code int} with a default. An attempt to convert the property value to an
	 * {@code int} will be made, if this fails the default will be returned.
	 *
	 * @param property
	 *            The property to lookup
	 * @param defaultValue
	 *            The value to return if the property is not set
	 * @return The property value or the defaultValue if not set
	 */
	public int getAsInt(String property, int defaultValue);

	/**
	 * Get the value of a property as an {@code double} with a default. An attempt to convert the property value to a
	 * {@code double} will be made, if this fails the default will be returned.
	 *
	 * @param property
	 *            The property to lookup
	 * @param defaultValue
	 *            The value to return if the property is not set
	 * @return The property value or the defaultValue if not set
	 */
	public double getAsDouble(String property, double defaultValue);

	/**
	 * Get the value of a property as a {@code boolean} with a default. An attempt to convert the property value to a
	 * {@code boolean} will be made, if this fails the default will be returned.
	 *
	 * @param property
	 *            The property to lookup
	 * @param defaultValue
	 *            The value to return if the property is not set
	 * @return The property value or the defaultValue if not set
	 */
	public boolean getAsBoolean(String property, boolean defaultValue);

	/**
	 * Check if a property is set.
	 *
	 * @param property
	 *            The property to lookup
	 * @return {@code true} if the property is set, {@code false} otherwise
	 */
	public boolean isSet(String property);

	/**
	 * Sets a property to the provided value. It will only be set for the life of the JVM, it will not be persisted to
	 * the property files.
	 * <p>
	 * This method is provided to bridge support for existing code, new code should not set
	 * properties programmatically at runtime and support for this feature will be removed in the future.
	 *
	 * @deprecated This is marked as deprecated to discourage new uses of this method.
	 * @param property
	 *            The property to set
	 * @param value
	 *            The value to set the property to
	 */
	@Deprecated
	public void set(String property, String value);
}
