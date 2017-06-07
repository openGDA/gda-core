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

package uk.ac.diamond.daq.persistence.jythonshelf;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.reloading.ReloadingStrategy;

/**
 * Implementation of {@link FileConfiguration} that delegates to an
 * underlying {@link FileConfiguration} instance, enforcing mutual exclusion
 * using a lock object.
 */
public class ThreadSafeFileConfiguration implements FileConfiguration {

	private final FileConfiguration config;
	private final Object lock;

	public ThreadSafeFileConfiguration(FileConfiguration config, Object lock) {
		this.config = config;
		this.lock = lock;
	}

	@Override
	public void load() throws ConfigurationException {
		synchronized (lock) {
			config.load();
		}
	}

	@Override
	public void load(String fileName) throws ConfigurationException {
		synchronized (lock) {
			config.load(fileName);
		}
	}

	@Override
	public void load(File file) throws ConfigurationException {
		synchronized (lock) {
			config.load(file);
		}
	}

	@Override
	public void load(URL url) throws ConfigurationException {
		synchronized (lock) {
			config.load(url);
		}
	}

	@Override
	public void load(InputStream in) throws ConfigurationException {
		synchronized (lock) {
			config.load(in);
		}
	}

	@Override
	public void load(InputStream in, String encoding) throws ConfigurationException {
		synchronized (lock) {
			config.load(in, encoding);
		}
	}

	@Override
	public Configuration subset(String prefix) {
		synchronized (lock) {
			return config.subset(prefix);
		}
	}

	@Override
	public void load(Reader in) throws ConfigurationException {
		synchronized (lock) {
			config.load(in);
		}
	}

	@Override
	public void save() throws ConfigurationException {
		synchronized (lock) {
			config.save();
		}
	}

	@Override
	public void save(String fileName) throws ConfigurationException {
		synchronized (lock) {
			config.save(fileName);
		}
	}

	@Override
	public void save(File file) throws ConfigurationException {
		synchronized (lock) {
			config.save(file);
		}
	}

	@Override
	public boolean isEmpty() {
		synchronized (lock) {
			return config.isEmpty();
		}
	}

	@Override
	public void save(URL url) throws ConfigurationException {
		synchronized (lock) {
			config.save(url);
		}
	}

	@Override
	public boolean containsKey(String key) {
		synchronized (lock) {
			return config.containsKey(key);
		}
	}

	@Override
	public void save(OutputStream out) throws ConfigurationException {
		synchronized (lock) {
			config.save(out);
		}
	}

	@Override
	public void addProperty(String key, Object value) {
		synchronized (lock) {
			config.addProperty(key, value);
		}
	}

	@Override
	public void save(OutputStream out, String encoding) throws ConfigurationException {
		synchronized (lock) {
			config.save(out, encoding);
		}
	}

	@Override
	public void save(Writer out) throws ConfigurationException {
		synchronized (lock) {
			config.save(out);
		}
	}

	@Override
	public void setProperty(String key, Object value) {
		synchronized (lock) {
			config.setProperty(key, value);
		}
	}

	@Override
	public String getFileName() {
		synchronized (lock) {
			return config.getFileName();
		}
	}

	@Override
	public void setFileName(String fileName) {
		synchronized (lock) {
			config.setFileName(fileName);
		}
	}

	@Override
	public void clearProperty(String key) {
		synchronized (lock) {
			config.clearProperty(key);
		}
	}

	@Override
	public String getBasePath() {
		synchronized (lock) {
			return config.getBasePath();
		}
	}

	@Override
	public void clear() {
		synchronized (lock) {
			config.clear();
		}
	}

	@Override
	public Object getProperty(String key) {
		synchronized (lock) {
			return config.getProperty(key);
		}
	}

	@Override
	public void setBasePath(String basePath) {
		synchronized (lock) {
			config.setBasePath(basePath);
		}
	}

	@Override
	public Iterator<String> getKeys(String prefix) {
		synchronized (lock) {
			return config.getKeys(prefix);
		}
	}

	@Override
	public File getFile() {
		synchronized (lock) {
			return config.getFile();
		}
	}

	@Override
	public Iterator<String> getKeys() {
		synchronized (lock) {
			return config.getKeys();
		}
	}

	@Override
	public void setFile(File file) {
		synchronized (lock) {
			config.setFile(file);
		}
	}

	@Override
	public URL getURL() {
		synchronized (lock) {
			return config.getURL();
		}
	}

	@Override
	public void setURL(URL url) {
		synchronized (lock) {
			config.setURL(url);
		}
	}

	@Override
	public void setAutoSave(boolean autoSave) {
		synchronized (lock) {
			config.setAutoSave(autoSave);
		}
	}

	@Override
	public Properties getProperties(String key) {
		synchronized (lock) {
			return config.getProperties(key);
		}
	}

	@Override
	public boolean isAutoSave() {
		synchronized (lock) {
			return config.isAutoSave();
		}
	}

	@Override
	public ReloadingStrategy getReloadingStrategy() {
		synchronized (lock) {
			return config.getReloadingStrategy();
		}
	}

	@Override
	public void setReloadingStrategy(ReloadingStrategy strategy) {
		synchronized (lock) {
			config.setReloadingStrategy(strategy);
		}
	}

	@Override
	public void reload() {
		synchronized (lock) {
			config.reload();
		}
	}

	@Override
	public String getEncoding() {
		synchronized (lock) {
			return config.getEncoding();
		}
	}

	@Override
	public boolean getBoolean(String key) {
		synchronized (lock) {
			return config.getBoolean(key);
		}
	}

	@Override
	public void setEncoding(String encoding) {
		synchronized (lock) {
			config.setEncoding(encoding);
		}
	}

	@Override
	public boolean getBoolean(String key, boolean defaultValue) {
		synchronized (lock) {
			return config.getBoolean(key, defaultValue);
		}
	}

	@Override
	public Boolean getBoolean(String key, Boolean defaultValue) {
		synchronized (lock) {
			return config.getBoolean(key, defaultValue);
		}
	}

	@Override
	public byte getByte(String key) {
		synchronized (lock) {
			return config.getByte(key);
		}
	}

	@Override
	public byte getByte(String key, byte defaultValue) {
		synchronized (lock) {
			return config.getByte(key, defaultValue);
		}
	}

	@Override
	public Byte getByte(String key, Byte defaultValue) {
		synchronized (lock) {
			return config.getByte(key, defaultValue);
		}
	}

	@Override
	public double getDouble(String key) {
		synchronized (lock) {
			return config.getDouble(key);
		}
	}

	@Override
	public double getDouble(String key, double defaultValue) {
		synchronized (lock) {
			return config.getDouble(key, defaultValue);
		}
	}

	@Override
	public Double getDouble(String key, Double defaultValue) {
		synchronized (lock) {
			return config.getDouble(key, defaultValue);
		}
	}

	@Override
	public float getFloat(String key) {
		synchronized (lock) {
			return config.getFloat(key);
		}
	}

	@Override
	public float getFloat(String key, float defaultValue) {
		synchronized (lock) {
			return config.getFloat(key, defaultValue);
		}
	}

	@Override
	public Float getFloat(String key, Float defaultValue) {
		synchronized (lock) {
			return config.getFloat(key, defaultValue);
		}
	}

	@Override
	public int getInt(String key) {
		synchronized (lock) {
			return config.getInt(key);
		}
	}

	@Override
	public int getInt(String key, int defaultValue) {
		synchronized (lock) {
			return config.getInt(key, defaultValue);
		}
	}

	@Override
	public Integer getInteger(String key, Integer defaultValue) {
		synchronized (lock) {
			return config.getInteger(key, defaultValue);
		}
	}

	@Override
	public long getLong(String key) {
		synchronized (lock) {
			return config.getLong(key);
		}
	}

	@Override
	public long getLong(String key, long defaultValue) {
		synchronized (lock) {
			return config.getLong(key, defaultValue);
		}
	}

	@Override
	public Long getLong(String key, Long defaultValue) {
		synchronized (lock) {
			return config.getLong(key, defaultValue);
		}
	}

	@Override
	public short getShort(String key) {
		synchronized (lock) {
			return config.getShort(key);
		}
	}

	@Override
	public short getShort(String key, short defaultValue) {
		synchronized (lock) {
			return config.getShort(key, defaultValue);
		}
	}

	@Override
	public Short getShort(String key, Short defaultValue) {
		synchronized (lock) {
			return config.getShort(key, defaultValue);
		}
	}

	@Override
	public BigDecimal getBigDecimal(String key) {
		synchronized (lock) {
			return config.getBigDecimal(key);
		}
	}

	@Override
	public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
		synchronized (lock) {
			return config.getBigDecimal(key, defaultValue);
		}
	}

	@Override
	public BigInteger getBigInteger(String key) {
		synchronized (lock) {
			return config.getBigInteger(key);
		}
	}

	@Override
	public BigInteger getBigInteger(String key, BigInteger defaultValue) {
		synchronized (lock) {
			return config.getBigInteger(key, defaultValue);
		}
	}

	@Override
	public String getString(String key) {
		synchronized (lock) {
			return config.getString(key);
		}
	}

	@Override
	public String getString(String key, String defaultValue) {
		synchronized (lock) {
			return config.getString(key, defaultValue);
		}
	}

	@Override
	public String[] getStringArray(String key) {
		synchronized (lock) {
			return config.getStringArray(key);
		}
	}

	@Override
	public List<Object> getList(String key) {
		synchronized (lock) {
			return config.getList(key);
		}
	}

	@Override
	public List<Object> getList(String key, @SuppressWarnings("rawtypes") List defaultValue) {
		synchronized (lock) {
			return config.getList(key, defaultValue);
		}
	}

}
