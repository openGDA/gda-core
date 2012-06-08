/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.factory.corba.util;

import gda.configuration.properties.LocalProperties;
import gda.factory.FactoryException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.jacorb.naming.Name;
import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CosNaming.Binding;
import org.omg.CosNaming.BindingHolder;
import org.omg.CosNaming.BindingIteratorHolder;
import org.omg.CosNaming.BindingListHolder;
import org.omg.CosNaming.BindingType;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.AlreadyBound;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * A Network implementation for CORBA
 */
public class NetService {
	private static final Logger logger = LoggerFactory.getLogger(NetService.class);

	/** the number of retries on a calling method */
	public final static int RETRY = 5;

	private ORB orb = null;

	private POA poa = null;

	private NamingContextExt namingContext = null;

	public static final String OBJECT_DELIMITER = "/";

	private static NetService instance = null;

	/**
	 * Return a singleton instance of the initialised netservice. This method is synchronized so that other threads
	 * cannot intervene after the instance==null check and create a second instance;
	 * 
	 * @return the netservice singleton.
	 * @throws FactoryException
	 */
	public static synchronized NetService getInstance() throws FactoryException {
		if (instance == null) {
			instance = new NetService();
			instance.init();
		}
		return instance;
	}

	private NetService() {
	}

	private void init() throws FactoryException {
		String property;
		Properties props = System.getProperties();

		if ((property = LocalProperties.get("gda.ORBClass")) != null)
			props.put("org.omg.CORBA.ORBClass", property);

		if ((property = LocalProperties.get("gda.ORBSingletonClass")) != null)
			props.put("org.omg.CORBA.ORBSingletonClass", property);

		System.setProperties(props);
		try {
			String[] args = new String[] {};
			orb = ORB.init(args, props);
			poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			poa.the_POAManager().activate();

			if ((namingContext = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"))) == null) {
				throw new FactoryException("`NameService' initial reference not found");
			}

		} catch (AdapterInactive ex) {
			throw new FactoryException("NetService: init " + ex.toString());
		} catch (org.omg.CORBA.ORBPackage.InvalidName ex) {
			throw new FactoryException("NetService: init " + ex.toString());
		} catch (SystemException ex) {
			logger.debug(ex.getStackTrace().toString());
			throw new FactoryException("NetService:init " + ex.toString());
		}
	}

	/**
	 * @return the ORB
	 */
	public ORB getOrb() {
		return orb;
	}

	/**
	 * @return the POA
	 */
	public POA getPOA() {
		return poa;
	}

	/**
	 * Get the naming context.
	 * 
	 * @return the naming context
	 */
	public NamingContextExt getNamingContextExt() {
		return namingContext;
	}

	/**
	 * Get an object that is registered with the naming service.
	 * 
	 * @param name
	 *            the name of the object to retrieve
	 * @param type
	 *            the class of the object
	 * @return the object from the name service.
	 * @throws FactoryException
	 */
	public org.omg.CORBA.Object retrieve(String name, String type) throws FactoryException {
		org.omg.CORBA.Object object = null;
		Vector<String> names = getComponents(name, OBJECT_DELIMITER);
		String kind = type.replace('.', '%');

		NameComponent[] bindingName = toBindingName(names, kind);
		try {
			object = namingContext.resolve(bindingName);
			return object;
		} catch (NotFound ex) {
			throw new FactoryException("Naming Service can't find object " + name);
		} catch (CannotProceed ex) {
			throw new FactoryException("NetService:retrieve " + ex.toString());
		} catch (InvalidName ex) {
			throw new FactoryException("NetService:retrieve " + ex.toString());
		}
	}

	/**
	 * Put an object on the name service
	 * 
	 * @param name
	 *            the name of the object
	 * @param object
	 *            the object to place on the name service
	 * @throws FactoryException
	 */
	public void bind(String name, org.omg.CORBA.Object object) throws FactoryException {
		try {
			logger.debug("NetService: binding name " + name);
			Vector<String> names = getComponents(name, OBJECT_DELIMITER);
			NameComponent[] bindingName;
			Vector<String> v = new Vector<String>();

			// bind the path using size-1 as we're using the last component
			// as the object name not a context.
			for (int i = 0; i < names.size() - 1; i++) {
				v.addElement(names.elementAt(i));
				bindingName = toBindingName(v);
				try {
					namingContext.bind_new_context(bindingName);
				} catch (AlreadyBound ex) {
					// don't care!
				}
			}

			// finally, bind the object itself ...
			v.addElement(names.lastElement());
			bindingName = toBindingName(v);
			namingContext.rebind(bindingName, object);
		} catch (NotFound ex) {
			throw new FactoryException("Naming Service can't find object " + name);
		} catch (CannotProceed ex) {
			throw new FactoryException("NetService:bind " + ex.toString());
		} catch (InvalidName ex) {
			throw new FactoryException("NetService:bind " + ex.toString());
		}
	}

	/**
	 * Put an object on the name service
	 * 
	 * @param name
	 *            the name of the object
	 * @param type
	 *            the class of the object
	 * @param object
	 *            the object to place on the name service
	 * @throws FactoryException
	 */
	public void bind(String name, String type, org.omg.CORBA.Object object) throws FactoryException {
		try {
			String kind = type.replace('.', '%');
			logger.debug("NetService: binding name " + name + " of kind " + kind);
			Vector<String> names = getComponents(name, OBJECT_DELIMITER);
			NameComponent[] bindingName;
			Vector<String> v = new Vector<String>();

			// bind the path using size-1 as we're using the last component
			// as the object name not a context.
			for (int i = 0; i < names.size() - 1; i++) {
				v.addElement(names.elementAt(i));
				bindingName = toBindingName(v);
				try {
					namingContext.bind_new_context(bindingName);
				} catch (AlreadyBound ex) {
					// don't care!
				}
			}

			// finally, bind the object itself ...
			v.addElement(names.lastElement());
			bindingName = toBindingName(v, kind);
			namingContext.rebind(bindingName, object);
		} catch (NotFound ex) {
			throw new FactoryException("Naming Service can't find object " + name);
		} catch (CannotProceed ex) {
			throw new FactoryException("NetService:bind " + ex.toString());
		} catch (InvalidName ex) {
			throw new FactoryException("NetService:bind " + ex.toString());
		}
	}

	/**
	 * Remove the object from the name service
	 * 
	 * @param name
	 *            the name of the object to remove
	 * @param type
	 *            the class of the object
	 * @throws FactoryException
	 */
	public void unbind(String name, String type) throws FactoryException {
		String kind = type.replace('.', '%');
		logger.debug("Unbinding name " + name);
		Vector<String> names = getComponents(name, OBJECT_DELIMITER);
		NameComponent[] bindingName = toBindingName(names, kind);
		
		// This method no longer unbinds all parent contexts above the object
		// being removed. This has the undesired effect of removing all sibling
		// objects
		try {
			namingContext.unbind(bindingName);
		} catch (NotFound ex) {
			throw new FactoryException("Naming Service can't find object " + name);
		} catch (CannotProceed ex) {
			throw new FactoryException("NetService:unbind " + ex.toString());
		} catch (InvalidName ex) {
			throw new FactoryException("NetService:unbind " + ex.toString());
		}
	}

	/**
	 * List all objects registered on the name service
	 */
	public void listAll() {
		list(namingContext, "  ");
	}

	/**
	 * Get the names of all Findable objects on the name service under the specified context.
	 * 
	 * @param contextPath context at which to start
	 * 
	 * @return a list of all findable names on the name service
	 */
	public List<String> listAllFindables(String contextPath) throws FactoryException {
		if (!StringUtils.hasText(contextPath)) {
			return listAllFindables();
		}
		NamingContextExt context;
		try {
			context = NamingContextExtHelper.narrow(namingContext.resolve_str(contextPath));
			return listAllFindables(context, contextPath);
		} catch (NotFound e) {
			logger.warn(String.format("Context path %s not found. Nothing placed on CORBA yet or the configuration needs to be checked.", StringUtils.quote(contextPath)));
			return Collections.emptyList();
		} catch (Exception e) {
			throw new FactoryException("Unable to list findables under " + StringUtils.quote(contextPath), e);
		}
	}
	
	/**
	 * Get the names of all Findable objects on the name service
	 * 
	 * @return a list of all findable names on the name service
	 */
	public List<String> listAllFindables() throws FactoryException {
		return listAllFindables(namingContext, "");
	}

	private List<String> listAllFindables(NamingContextExt context, String contextPath) throws FactoryException {
		ArrayList<String> names = new ArrayList<String>();
		listToArrayFindables(context, contextPath, names, false);
		return names;
	}
	
	/**
	 * used by listAllFindables to fill a vector of all the object names in the nameserver. This function recursively
	 * calls itself as it moves through the object heirarchy.
	 * 
	 * @param n
	 *            NamingContextExt
	 * @param context
	 *            String
	 * @param names
	 *            a list of names
	 */
	private void listToArrayFindables(NamingContextExt n, String context, List<String> names, boolean recurse) throws FactoryException {
		try {
			BindingListHolder blsoh = new BindingListHolder(new Binding[0]);
			BindingIteratorHolder bioh = new BindingIteratorHolder();

			n.list(0, blsoh, bioh);

			BindingHolder bh = new BindingHolder();

			if (bioh.value == null) {
				return;
			}

			while (bioh.value.next_one(bh)) {
				String fullPath = (StringUtils.hasText(context) ? context + OBJECT_DELIMITER : "") + bh.value.binding_name[0].id;
				Name name = new Name(bh.value.binding_name);
				if (bh.value.binding_type.value() == BindingType._nobject) {
					names.add(fullPath);
				}
				if (recurse && bh.value.binding_type.value() == BindingType._ncontext) {
					NamingContextExt newContext = NamingContextExtHelper.narrow(n.resolve(name.components()));
					listToArrayFindables(newContext, fullPath, names, recurse);
				}
			}
		} catch (Exception e) {
			throw new FactoryException("Unable to list findables under " + StringUtils.quote(context), e);
		}
	}

	private void list(NamingContextExt n, String indent) {
		try {
			BindingListHolder blsoh = new BindingListHolder(new Binding[0]);
			BindingIteratorHolder bioh = new BindingIteratorHolder();

			n.list(0, blsoh, bioh);

			BindingHolder bh = new BindingHolder();

			if (bioh.value == null)
				return;

			while (bioh.value.next_one(bh)) {
				Name name = new Name(bh.value.binding_name);
				logger.info(indent + name);
				if (bh.value.binding_type.value() == BindingType._ncontext) {
					String _indent = indent + "\t";
					list(NamingContextExtHelper.narrow(n.resolve(name.components())), _indent);
				}
			}
		} catch (Exception e) {
			logger.debug(e.getStackTrace().toString());
		}
	}

	private NameComponent[] toBindingName(Vector<String> names) {
		NameComponent[] bindingName = new NameComponent[names.size()];

		for (int i = 0; i < names.size(); i++) {
			bindingName[i] = new NameComponent();
			bindingName[i].id = names.elementAt(i);
			bindingName[i].kind = "";
		}
		return bindingName;
	}

	private NameComponent[] toBindingName(Vector<String> names, String kind) {
		NameComponent[] bindingName = new NameComponent[names.size()];

		int size = names.size();
		for (int i = 0; i < size; i++) {
			bindingName[i] = new NameComponent();
			bindingName[i].id = names.elementAt(i);
			if (i == (size - 1))
				bindingName[i].kind = kind;
			else
				bindingName[i].kind = "";
		}
		return bindingName;
	}

	/**
	 * Hand control to the orb an wait for incoming Corba calls.
	 */
	public void serverRun() {
		if (orb != null)
			orb.run();
	}

	/**
	 * Get the components of the name
	 * 
	 * @param name
	 *            the full name
	 * @param delim
	 *            the delimiter between individual component names
	 * @return a vector of component names.
	 */
	private static Vector<String> getComponents(String name, String delim) {
		Vector<String> v = new Vector<String>();
		int startIndex = 0;
		int endIndex = 0;
		int offset = delim.length();

		if (!delim.equals("")) {
			while ((endIndex = name.indexOf(delim, startIndex)) != -1) {
				v.addElement(name.substring(startIndex, endIndex));
				startIndex = endIndex + offset;
			}
		}
		v.addElement(name.substring(startIndex));
		return v;
	}

	/**
	 * Reconnect to the name service in the event of failure
	 * 
	 * @param name
	 *            the name of the object to reconnect
	 * @return the reconnected object
	 */
	public org.omg.CORBA.Object reconnect(String name) {
		org.omg.CORBA.Object obj = null;
		try {
			logger.debug(name + " needs reconnection now");
			obj = retrieve(name, getType(name));
		} catch (FactoryException ex) {
			logger.error("NetService: Factory Exception: " + ex.toString());
		}
		return obj;
	}

	/**
	 * Get the type of object specified by the name
	 * 
	 * @param name
	 *            the name of the object to get the type
	 * @return the type of object.
	 */
	public String getType(String name) {
		Vector<String> names = getComponents(name, OBJECT_DELIMITER);
		Enumeration<String> e = names.elements();
		String type = getTypeKind(namingContext, e);
		return type.replace('%', '.');
	}

	private String getTypeKind(NamingContextExt n, Enumeration<String> e) {
		String typeKind = "";
		try {
			BindingListHolder blsoh = new BindingListHolder(new Binding[0]);
			BindingIteratorHolder bioh = new BindingIteratorHolder();

			n.list(0, blsoh, bioh);

			BindingHolder bh = new BindingHolder();

			if (bioh.value != null) {
				String lookupName = e.nextElement();

				while (bioh.value.next_one(bh)) {
					Name name = new Name(bh.value.binding_name);

					if (name.baseNameComponent().id.equals(lookupName)) {
						if (bh.value.binding_type.value() == BindingType._ncontext) {
							typeKind = getTypeKind(NamingContextExtHelper.narrow(n.resolve(name.components())), e);
						} else
							typeKind = name.kind();
						break;
					}
				}
			}
		} catch (Exception ex) {
			logger.debug(ex.getStackTrace().toString());
		}
		return typeKind;
	}
}
