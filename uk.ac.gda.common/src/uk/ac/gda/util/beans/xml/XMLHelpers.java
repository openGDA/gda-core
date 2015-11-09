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

package uk.ac.gda.util.beans.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.XMLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import uk.ac.gda.util.beans.BeansFactory;

public class XMLHelpers {


	/**
	 * Will get a bean of the appropriate type from any an exafs bean file. One of ScanParameters, XanesScanParameters,
	 * SampleParameters, DetectorParameters, OutputParameters is returned.
	 *
	 * @param beanFile
	 * @return the bean
	 * @throws Exception
	 */
	public static XMLRichBean getBean(final File beanFile) throws Exception {
		for (int i = 0; i < BeansFactory.getClasses().length; i++) {
			if (BeansFactory.isBean(beanFile, BeansFactory.getClasses()[i])) {
				return (XMLRichBean) XMLHelpers.readBean(beanFile, BeansFactory.getClasses()[i]);
			}
		}
		return null;
	}

	/**
	 * Save a bean to a file.
	 *
	 * @param templatePath
	 * @param editingBean
	 * @throws Exception
	 */
	public static void saveBean(File templatePath, Object editingBean) throws Exception {
		URL mapping = null;
		final Field[] fa = editingBean.getClass().getFields();
		for (int i = 0; i < fa.length; i++) {
			if (fa[i].getName().equalsIgnoreCase("mappingurl")) {
				mapping = (URL) fa[i].get(null);
				break;
			}
		}
		writeToXML(mapping, editingBean, templatePath);
	}

	/**
	 * Retrieves the bean given the file name, or the bean itself.
	 * <p>
	 * For use in command-line tools where the user can supply filenames or beans interchangeably in commands.
	 *
	 * @param dir
	 * @param beanOrFile
	 * @return ExafsBeansFactory.getBean(new File(beanFile));
	 * @throws Exception
	 */
	public static XMLRichBean getBeanObject(final String dir, final Object beanOrFile) throws Exception {

		for (int i = 0; i < BeansFactory.getClasses().length; i++) {
			if (BeansFactory.getClasses()[i].isInstance(beanOrFile))
				return (XMLRichBean) beanOrFile;
		}

		String path;
		if(dir != null){
			path = dir + beanOrFile;
		} else {
			path = beanOrFile.toString();
		}
		if (!path.endsWith(".xml"))
			path = path + ".xml";
		return getBean(new File(path));

	}


	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(XMLHelpers.class);

	private static URLResolver urlResolver;

	private static class UrlClassLoaderPair {
		private URL mappingURL;
		private ClassLoader cl;
		public UrlClassLoaderPair(final URL mappingURL, final ClassLoader cl) {
			this.mappingURL = mappingURL;
			this.cl = cl;
		}

		private boolean equalsHelper(Object o1, Object o2) {
			if (o1 == o2)
				return true;
			if (o1 == null || o2 == null)
				return false;
			return o1.equals(o2);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof UrlClassLoaderPair))
				return false;
			UrlClassLoaderPair other = (UrlClassLoaderPair)obj;
			if (!equalsHelper(mappingURL, other.mappingURL))
				return false;
			if (!equalsHelper(cl, other.cl))
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((mappingURL == null) ? 0 : mappingURL.hashCode());
			result = prime * result + ((cl == null) ? 0 : cl.hashCode());
			return result;
		}
	}

	private static Map<UrlClassLoaderPair, XMLContext> xmlContextCache = new HashMap<UrlClassLoaderPair, XMLContext>();

	private static XMLContext createXMLContext(final URL mappingURL, final ClassLoader cl) throws MappingException, IOException {
		XMLContext context;
		if (mappingURL == null || cl == null)
			throw new NullPointerException();
		UrlClassLoaderPair urlClassLoaderPair = new UrlClassLoaderPair(mappingURL, cl);
		if (xmlContextCache.containsKey(urlClassLoaderPair)) {
			context = xmlContextCache.get(urlClassLoaderPair);
		} else {
			// Load Mapping. NOTE May need to provide class loader here if class
			// which are instantiated are on a different class loader to the
			// one which loaded the mapping class.
			Mapping mapping = new Mapping(cl);
			mapping.loadMapping(mappingURL);
			// initialise and configure XMLContext
			context = new XMLContext();
			context.addMapping(mapping);
			context.setProperty("org.exolab.castor.indent", "true");
			// store in the cache
			xmlContextCache.put(urlClassLoaderPair, context);
		}
		return context;
	}

	/**
	 *
	 * @param mappingURL
	 * @param cl
	 * @param schemaUrl
	 * @param filename
	 * @return Object
	 * @throws Exception
	 */
	public static Object createFromXML(URL mappingURL, Class<? extends Object> cl, URL schemaUrl, String filename) throws Exception {
        return XMLHelpers.createFromXML(mappingURL,cl,schemaUrl,filename,true);
	}

	public static Object createFromXML(URL mappingURL, Class<? extends Object> cl, URL schemaUrl, String filename, String encoding) throws Exception {
        return XMLHelpers.createFromXML(mappingURL,cl,schemaUrl,filename,true, encoding);
	}

	/**
	 * Creates an object from an inputsource. To create an InputSource from a String use the form:
	 * String xml = "<?xml version="1.0" encoding="UTF-8"?><expt-table-bean><beamsize_horz>beamsize_horz</beamsize_horz></expt-table-bean>"
	 *  new InputSource( new StringReader(xml)).
	 *  If you use the form new InputSource(xml) you will get the error . org.exolab.castor.xml.MarshalException: no protocol:
	 *
	 * @param mappingURL
	 * @param cl
	 * @param schemaUrl
	 * @param source
	 * @return Object
	 * @throws Exception
	 */
	public static Object createFromXML(URL mappingURL, Class<? extends Object> cl, URL schemaUrl, InputSource source) throws Exception {
        return XMLHelpers.createFromXMLInternal(mappingURL,cl,schemaUrl,source,true);
	}

	/**
	 * Unmarshals an object from an XML document.
	 *
	 * @param mappingURL the URL of the Castor mapping file
	 * @param cl class of the object being deserialized
	 * @param schemaUrl URL of the XML schema
	 * @param source the source XML document
	 * @param validate whether the XML document should be validated against the schema
	 *
	 * @return the unmarshalled object
	 *
	 * @throws Exception
	 */
	public static Object createFromXML(URL mappingURL, Class<? extends Object> cl, URL schemaUrl, InputSource source, boolean validate) throws Exception {
        return XMLHelpers.createFromXMLInternal(mappingURL,cl,schemaUrl,source,validate);
	}

	/**
	 *
	 * @param mappingURL
	 * @param cl
	 * @param schemaUrl
	 * @param file
	 * @return Object
	 * @throws Exception
	 */
	public static Object createFromXML(URL mappingURL, Class<? extends Object> cl, URL schemaUrl, File file) throws Exception {
	    FileReader reader = null;
		try {
			reader = new FileReader(file);
	        return XMLHelpers.createFromXMLInternal(mappingURL,cl,schemaUrl,new InputSource(reader),true);
		}
		finally {
			if (reader!=null) reader.close();
		}
	}

	public static Object createFromXML(URL mappingURL, Class<? extends Object> cl, URL schemaUrl, File file, String encoding) throws Exception {
		InputStreamReader reader = new InputStreamReader(new FileInputStream(file), encoding);
		try {
			return XMLHelpers.createFromXMLInternal(mappingURL,cl,schemaUrl,new InputSource(reader),true);
		} finally {
			reader.close();
		}
	}

	/**
	 * @param mappingURL
	 * @param cl
	 * @param schemaUrl
	 * @param filename
	 * @param validate
	 * @return Object
	 * @throws Exception
	 * @throws XMLHelpersException
	 */
	public static Object createFromXML(URL mappingURL, Class<? extends Object> cl, URL schemaUrl, String filename, final boolean validate) throws Exception {
		return createFromXML(mappingURL, cl, schemaUrl, filename, validate, null);
	}

	public static Object createFromXML(URL mappingURL, Class<? extends Object> cl, URL schemaUrl, String filename, final boolean validate, String encoding) throws Exception {
		InputSource source;
		// GDA-3377 This fails on windows if filename is similar to
		// c:/data/file because c: is considered the scheme
		URI uri = new URI(filename);
		if (uri.getScheme() != null && (uri.getScheme().equals("http") || uri.getScheme().equals("file")))
			source = new InputSource(uri.toURL().openStream());
		else {
			if (encoding == null)
				source = new InputSource(new FileReader(filename));
			else
				source = new InputSource(new InputStreamReader(new FileInputStream(filename), encoding));
		}
		return XMLHelpers.createFromXMLInternal(mappingURL, cl, schemaUrl, source, validate);
	}


	/**
	 * Sets up the bean from the XML. Gives an exception with a useful message if
	 * the XML was invalid. Can be used for validating text editors with xml.
	 *
	 * Beans properties will be nullified before transferring the data from the XML
	 * to the bean.
	 *
	 * The bean being set by this method must have a clear() method. This nullifys the
	 * beans properties.
	 *
	 * @param existingBean
	 * @param mappingUrl
	 * @param schemaUrl
	 * @param xml
	 * @throws Exception
	 */
	public static void setFromXML(final Object existingBean, URL mappingUrl, URL schemaUrl, String xml) throws Exception {

		if (urlResolver!=null) {
			mappingUrl = urlResolver.resolve(mappingUrl);
			schemaUrl  = urlResolver.resolve(schemaUrl);
		}

		InputSource source=null;
		if (schemaUrl != null) {
			XMLObjectConfigFileValidator validator = new XMLObjectConfigFileValidator();
			source = validator.validateSource(schemaUrl.toString(), xml.toCharArray(), true);

			if (source == null) {
				throw new XMLHelpersXMLValidationError();
			}
		}

		try {
			final Method clear = existingBean.getClass().getMethod("clear");
			clear.invoke(existingBean);
		} catch (Throwable ignored) {
			// then don't clear
		}

		Unmarshaller unmarshaller = createXMLContext(mappingUrl, existingBean.getClass().getClassLoader()).createUnmarshaller();
		unmarshaller.setClass(existingBean.getClass());
		unmarshaller.setObject(existingBean);
		unmarshaller.unmarshal(source);
	}

	private static Object createFromXMLInternal(URL mappingUrl, Class<? extends Object> cl, URL schemaUrl, InputSource source, final boolean validate) throws Exception {
		if (urlResolver!=null) {
			mappingUrl = urlResolver.resolve(mappingUrl);
			schemaUrl  = urlResolver.resolve(schemaUrl);
		}
		if (validate) {
			if (schemaUrl != null) {
				XMLObjectConfigFileValidator validator = new XMLObjectConfigFileValidator();
				source = validator.validateSource(schemaUrl.toString(), source, true);
				if (source == null)
					throw new XMLHelpersXMLValidationError();
			}
		}
		Object obj = null;
		if (mappingUrl != null) {
			Unmarshaller unmarshaller = createXMLContext(mappingUrl, cl.getClassLoader()).createUnmarshaller();
			unmarshaller.setClass(cl);
			obj = unmarshaller.unmarshal(source);
		} else
			obj = Unmarshaller.unmarshal(cl, source);
		if (!obj.getClass().equals(cl))
			throw new XMLHelpersException("Class created is incorrect = " + obj.getClass().getName());
		return obj;
	}

	/**
	 * Writes the object to a file.
	 * @param mappingURL
	 * @param object
	 * @param file
	 * @throws Exception
	 * @throws XMLHelpersException
	 */
	public static void writeToXML(URL mappingURL, Object object, File file) throws Exception {
		final Writer writer = new FileWriter(file);
		XMLHelpers.writeToXMLInternal(mappingURL, object, writer);
	}

	public static void writeToXML(URL mappingURL, Object object, File file, String encoding) throws Exception {
		final Writer writer = new OutputStreamWriter(new FileOutputStream(file), encoding);
		XMLHelpers.writeToXMLInternal(mappingURL, object, writer);
	}

	/**
	 * Writes the object to a file.
	 * @param mappingURL
	 * @param object
	 * @param filename
	 * @throws Exception
	 * @throws XMLHelpersException
	 */
	public static void writeToXML(URL mappingURL, Object object, String filename) throws Exception {
		writeToXML(mappingURL, object, filename, null);
	}

	public static void writeToXML(URL mappingURL, Object object, String filename, String encoding) throws Exception {
		if (filename.startsWith("file:")) filename = filename.substring(5);
		Writer writer = null;
		if (encoding == null)
			writer = new FileWriter(filename);
		else
			writer = new OutputStreamWriter(new FileOutputStream(filename), encoding);
		XMLHelpers.writeToXMLInternal(mappingURL, object, writer);
	}
	/**
	 * Writes the object to a string
	 * @param mappingURL
	 * @param object
	 * @param fileData
	 * @throws Exception
	 * @throws XMLHelpersException
	 */
	public static void writeToXML(URL mappingURL, Object object, Writer fileData) throws Exception {
		XMLHelpers.writeToXMLInternal(mappingURL, object, fileData);
	}

	private static void writeToXMLInternal(URL mappingURL, Object object, Writer writer) throws Exception {
		try {
			if (urlResolver!=null)
				mappingURL = urlResolver.resolve(mappingURL);
			if (mappingURL != null) {
				XMLContext context    = createXMLContext(mappingURL, object.getClass().getClassLoader());
				Marshaller marshaller = context.createMarshaller();
				marshaller.setWriter(writer);
				marshaller.marshal(object);
			} else
				Marshaller.marshal(object, writer);
		} finally {
			if (writer!=null) writer.flush();
			if (writer!=null) writer.close();
		}
	}

	/**
	 * Read a bean from a file and the class to read it into.
	 *
	 * The static fields mappingurl(case insensitive) and schemaurl(case insensitive) should
	 * exist in the beanClass.
	 *
	 * @param beanFile
	 * @param beanClass
	 * @return the bean
	 * @throws Exception
	 */
	public static Object readBean(File beanFile, Class<?> beanClass) throws Exception {
		URL mapping = null;
		URL schema  = null;
		final Field [] fa = beanClass.getFields();
		for (int i = 0; i < fa.length; i++) {
			if (fa[i].getName().equalsIgnoreCase("mappingurl")) {
				mapping = (URL)fa[i].get(null);
			} else if (fa[i].getName().equalsIgnoreCase("schemaurl")) {
				schema = (URL)fa[i].get(null);
			}
		}
		return createFromXML(mapping, beanClass, schema, beanFile);
	}

	/**
	 * @return Returns the urlResolver.
	 */
	public static URLResolver getUrlResolver() {
		return urlResolver;
	}

	/**
	 * @param urlResolver The urlResolver to set.
	 */
	public static void setUrlResolver(URLResolver urlResolver) {
		XMLHelpers.urlResolver = urlResolver;
	}

}
