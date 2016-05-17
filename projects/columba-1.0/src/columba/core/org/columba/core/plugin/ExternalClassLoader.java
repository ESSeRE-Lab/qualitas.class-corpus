//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.core.plugin;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.logging.Logger;

/**
 * Classloader responsible for instanciating plugins which can also be outside.
 * <p>
 * Note, that this classloader tries to find the correct constructor based on
 * the arguments.
 * 
 * @author fdietz
 */
public class ExternalClassLoader extends URLClassLoader {

	private static final Logger LOG = Logger
			.getLogger("org.columba.core.loader");

	/**
	 * Constructor for ExternalClassLoader.
	 * 
	 * @param urls
	 * @param parent
	 */
	public ExternalClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	/**
	 * Constructor for ExternalClassLoader.
	 * 
	 * @param urls
	 */
	public ExternalClassLoader(URL[] urls) {
		super(urls);
	}

	/**
	 * Constructor for ExternalClassLoader.
	 * 
	 * @param urls
	 * @param parent
	 * @param factory
	 */
	public ExternalClassLoader(URL[] urls, ClassLoader parent,
			URLStreamHandlerFactory factory) {
		super(urls, parent, factory);
	}

	public void addURL(URL url) {
		super.addURL(url);
	}

	public Class findClass(String className) throws ClassNotFoundException {
		Class temp = super.findClass(className);

		return temp;
	}

	public Object instanciate(String className) throws Exception {
		Class actClass = findClass(className);

		return actClass.newInstance();
	}

	public Object instanciate(String className, Object[] args) throws Exception {

		Class actClass = findClass(className);

		Constructor constructor = null;

		// we can't just load the first constructor
		// -> go find the correct constructor based
		// -> based on the arguments
		if ((args == null) || (args.length == 0)) {

			return actClass.newInstance();
		}

		constructor = ClassLoaderHelper.findConstructor(args, actClass);

		// couldn't find correct constructor
		if (constructor == null) {
			LOG.severe("Couldn't find constructor for " + className
					+ " with matching argument-list: ");
			for (int i = 0; i < args.length; i++) {
				LOG.severe("argument[" + i + "]=" + args[i]);
			}
			return null;
		}

		return constructor.newInstance(args);
	}

	/* (non-Javadoc)
	 * @see java.net.URLClassLoader#findResource(java.lang.String)
	 */
	public URL findResource(String name) {
		
		URL url = super.findResource(name);
		return url;
	}

}