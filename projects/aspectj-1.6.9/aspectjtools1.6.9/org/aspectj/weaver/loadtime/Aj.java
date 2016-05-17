/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package org.aspectj.weaver.loadtime;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.aspectj.bridge.context.CompilationAndWeavingContext;
import org.aspectj.weaver.Dump;
import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;
import org.aspectj.weaver.tools.WeavingAdaptor;

/**
 * Adapter between the generic class pre processor interface and the AspectJ weaver Load time weaving consistency relies on
 * Bcel.setRepository
 * 
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class Aj implements ClassPreProcessor {

	private IWeavingContext weavingContext;

	/**
	 * References are added to this queue when their associated classloader is removed, and once on here that indicates that we
	 * should tidy up the adaptor map and remove the adaptor (weaver) from the map we are maintaining from adaptorkey > adaptor
	 * (weaver)
	 */
	private static ReferenceQueue adaptorQueue = new ReferenceQueue();

	private static Trace trace = TraceFactory.getTraceFactory().getTrace(Aj.class);

	public Aj() {
		this(null);
	}

	public Aj(IWeavingContext context) {
		if (trace.isTraceEnabled())
			trace.enter("<init>", this, new Object[] { context, getClass().getClassLoader() });
		this.weavingContext = context;
		if (trace.isTraceEnabled())
			trace.exit("<init>");
	}

	/**
	 * Initialization
	 */
	public void initialize() {

	}

	private final static String deleLoader = "sun.reflect.DelegatingClassLoader";

	/**
	 * Weave
	 * 
	 * @param className
	 * @param bytes
	 * @param loader
	 * @return weaved bytes
	 */
	public byte[] preProcess(String className, byte[] bytes, ClassLoader loader) {
		// TODO AV needs to doc that
		if (loader == null || className == null || loader.getClass().getName().equals(deleLoader)) {
			// skip boot loader, null classes (hibernate), or those from a reflection loader
			return bytes;
		}

		if (trace.isTraceEnabled())
			trace.enter("preProcess", this, new Object[] { className, bytes, loader });
		if (trace.isTraceEnabled())
			trace.event("preProcess", this, new Object[] { loader.getParent(), Thread.currentThread().getContextClassLoader() });

		try {
			synchronized (loader) {
				WeavingAdaptor weavingAdaptor = WeaverContainer.getWeaver(loader, weavingContext);
				if (weavingAdaptor == null) {
					if (trace.isTraceEnabled())
						trace.exit("preProcess");
					return bytes;
				}
				byte[] newBytes = weavingAdaptor.weaveClass(className, bytes, false);
				Dump.dumpOnExit(weavingAdaptor.getMessageHolder(), true);
				if (trace.isTraceEnabled())
					trace.exit("preProcess", newBytes);
				return newBytes;
			}

			/* Don't like to do this but JVMTI swallows all exceptions */
		} catch (Throwable th) {
			trace.error(className, th);
			Dump.dumpWithException(th);
			// FIXME AV wondering if we should have the option to fail (throw runtime exception) here
			// would make sense at least in test f.e. see TestHelper.handleMessage()
			if (trace.isTraceEnabled())
				trace.exit("preProcess", th);
			return bytes;
		} finally {
			CompilationAndWeavingContext.resetForThread();
		}
	}

	/**
	 * An AdaptorKey is a WeakReference wrapping a classloader reference that will enqueue to a specified queue when the classloader
	 * is GC'd. Since the AdaptorKey is used as a key into a hashmap we need to give it a non-varying hashcode/equals
	 * implementation, and we need that hashcode not to vary even when the internal referent has been GC'd. The hashcode is
	 * calculated on creation of the AdaptorKey based on the loader instance that it is wrapping. This means even when the referent
	 * is gone we can still use the AdaptorKey and it will 'point' to the same place as it always did.
	 */
	private static class AdaptorKey extends WeakReference {

		private int hashcode = -1;

		public AdaptorKey(ClassLoader loader) {
			super(loader, adaptorQueue);
			hashcode = loader.hashCode() * 37;
		}

		public ClassLoader getClassLoader() {
			ClassLoader instance = (ClassLoader) get();
			// Assert instance!=null - shouldn't be asked for after a GC of the referent has occurred !
			return instance;
		}

		public boolean equals(Object obj) {
			if (!(obj instanceof AdaptorKey))
				return false;
			AdaptorKey other = (AdaptorKey) obj;
			return other.hashcode == hashcode;
		}

		public int hashCode() {
			return hashcode;
		}

	}

	/**
	 * The reference queue is only processed when a request is made for a weaver adaptor. This means there can be one or two stale
	 * weavers left around. If the user knows they have finished all their weaving, they might wish to call removeStaleAdaptors
	 * which will process anything left on the reference queue containing adaptorKeys for garbage collected classloaders.
	 * 
	 * @param displayProgress produce System.err info on the tidying up process
	 * @return number of stale weavers removed
	 */
	public static int removeStaleAdaptors(boolean displayProgress) {
		int removed = 0;
		synchronized (WeaverContainer.weavingAdaptors) {
			if (displayProgress) {
				System.err.println("Weaver adaptors before queue processing:");
				Map m = WeaverContainer.weavingAdaptors;
				Set keys = m.keySet();
				for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
					Object object = iterator.next();
					System.err.println(object + " = " + WeaverContainer.weavingAdaptors.get(object));
				}
			}
			Object o = adaptorQueue.poll();
			while (o != null) {
				if (displayProgress)
					System.err.println("Processing referencequeue entry " + o);
				AdaptorKey wo = (AdaptorKey) o;
				boolean didit = WeaverContainer.weavingAdaptors.remove(wo) != null;
				if (didit) {
					removed++;
				} else {
					throw new RuntimeException("Eh?? key=" + wo);
				}
				if (displayProgress)
					System.err.println("Removed? " + didit);
				o = adaptorQueue.poll();
			}
			if (displayProgress) {
				System.err.println("Weaver adaptors after queue processing:");
				Map m = WeaverContainer.weavingAdaptors;
				Set keys = m.keySet();
				for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
					Object object = iterator.next();
					System.err.println(object + " = " + WeaverContainer.weavingAdaptors.get(object));
				}
			}
		}
		return removed;
	}

	/**
	 * @return the number of entries still in the weavingAdaptors map
	 */
	public static int getActiveAdaptorCount() {
		return WeaverContainer.weavingAdaptors.size();
	}

	/**
	 * Process the reference queue that contains stale AdaptorKeys - the keys are put on the queue when their classloader referent
	 * is garbage collected and so the associated adaptor (weaver) should be removed from the map
	 */
	public static void checkQ() {
		synchronized (adaptorQueue) {
			Object o = adaptorQueue.poll();
			while (o != null) {
				AdaptorKey wo = (AdaptorKey) o;
				// boolean removed =
				WeaverContainer.weavingAdaptors.remove(wo);
				// DBG System.err.println("Evicting key " + wo + " = " + didit);
				o = adaptorQueue.poll();
			}
		}
	}

	static {
		// pr271840 - touch the types early and outside the locks
		new ExplicitlyInitializedClassLoaderWeavingAdaptor(new ClassLoaderWeavingAdaptor());
	}

	/**
	 * Cache of weaver There is one weaver per classloader
	 */
	static class WeaverContainer {

		final static Map weavingAdaptors = Collections.synchronizedMap(new HashMap());

		static WeavingAdaptor getWeaver(ClassLoader loader, IWeavingContext weavingContext) {
			ExplicitlyInitializedClassLoaderWeavingAdaptor adaptor = null;
			AdaptorKey adaptorKey = new AdaptorKey(loader);

			String loaderClassName = loader.getClass().getName();

			synchronized (weavingAdaptors) {
				checkQ();
				adaptor = (ExplicitlyInitializedClassLoaderWeavingAdaptor) weavingAdaptors.get(adaptorKey);
				if (adaptor == null) {
					// create it and put it back in the weavingAdaptors map but avoid any kind of instantiation
					// within the synchronized block
					ClassLoaderWeavingAdaptor weavingAdaptor = new ClassLoaderWeavingAdaptor();
					adaptor = new ExplicitlyInitializedClassLoaderWeavingAdaptor(weavingAdaptor);
					weavingAdaptors.put(adaptorKey, adaptor);
				}
			}
			// perform the initialization
			return adaptor.getWeavingAdaptor(loader, weavingContext);

		}
	}

	static class ExplicitlyInitializedClassLoaderWeavingAdaptor {
		private final ClassLoaderWeavingAdaptor weavingAdaptor;
		private boolean isInitialized;

		public ExplicitlyInitializedClassLoaderWeavingAdaptor(ClassLoaderWeavingAdaptor weavingAdaptor) {
			this.weavingAdaptor = weavingAdaptor;
			this.isInitialized = false;
		}

		private void initialize(ClassLoader loader, IWeavingContext weavingContext) {
			if (!isInitialized) {
				isInitialized = true;
				weavingAdaptor.initialize(loader, weavingContext);
			}
		}

		public ClassLoaderWeavingAdaptor getWeavingAdaptor(ClassLoader loader, IWeavingContext weavingContext) {
			initialize(loader, weavingContext);
			return weavingAdaptor;
		}
	}

	/**
	 * Returns a namespace based on the contest of the aspects available
	 */
	public String getNamespace(ClassLoader loader) {
		ClassLoaderWeavingAdaptor weavingAdaptor = (ClassLoaderWeavingAdaptor) WeaverContainer.getWeaver(loader, weavingContext);
		return weavingAdaptor.getNamespace();
	}

	/**
	 * Check to see if any classes have been generated for a particular classes loader. Calls
	 * ClassLoaderWeavingAdaptor.generatedClassesExist()
	 * 
	 * @param loader the class cloder
	 * @return true if classes have been generated.
	 */
	public boolean generatedClassesExist(ClassLoader loader) {
		return ((ClassLoaderWeavingAdaptor) WeaverContainer.getWeaver(loader, weavingContext)).generatedClassesExistFor(null);
	}

	public void flushGeneratedClasses(ClassLoader loader) {
		((ClassLoaderWeavingAdaptor) WeaverContainer.getWeaver(loader, weavingContext)).flushGeneratedClasses();
	}

}