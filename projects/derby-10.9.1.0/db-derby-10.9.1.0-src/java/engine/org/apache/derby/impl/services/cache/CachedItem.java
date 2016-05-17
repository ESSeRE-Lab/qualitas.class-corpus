/*

   Derby - Class org.apache.derby.impl.services.cache.CachedItem

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package org.apache.derby.impl.services.cache;

import org.apache.derby.iapi.services.cache.Cacheable;
import org.apache.derby.iapi.services.cache.CacheableFactory;
import org.apache.derby.iapi.services.cache.CacheManager;
import org.apache.derby.iapi.error.StandardException;

import org.apache.derby.iapi.services.sanity.SanityManager;
import org.apache.derby.iapi.services.context.ContextService;

/**
	A generic class to represent the cache related infomation of a cached object (Cacheable).
	<P><PRE>
	The relationship between isValid and settingIdentity can be explain by the
	following life cycle of a cached item.

	Stage		   1	2	3
	----------------------
	isValid	        F	T	T	
	settingIdentity	X	T	F

	In Stage 1, the CachedItem is created but it is invalid and has an entry
	that is just a holder object with no identity.

	In Stage 2, the identity has been set and the item is being created or
	being faulted into the cache.

	In Stage 3, the item found in the CachedItem entry
	</PRE> <P>
	Remove is set if this item is being removed out of existance, not just
	being evicted from the cache.  When the last referece to it release it from
	the cache, it will be removed.
	<BR>
	RecentlyUsed is set whenever this item is accessed (via a keep() call).
	It is reset by the clockHand as it sweeps around the cache looking for
	victims to evict.

	<P>MT - must be MT-safe and work with cache manager.  Every method that
	access (set or get) instance variables is synchronized on the cached item
	object.  The following method waits so it should not be called by the cache
	manager inside a sync block: clean(), waitFor(), create(), remove().
	(RESOLVE: need to move these from the cache manager to here)

	@see org.apache.derby.impl.services.cache
	@see Cacheable
*/
final class CachedItem {
	/*
	** Fields
	*/

	private boolean valid_ = false;
	private boolean removeRequested_ = false;
	private boolean settingIdentity_ = false;
	private boolean removeOk_ = false;
	private boolean recentlyUsed_ = false;

	/**
		The current keep count on the entry.

		<BR> MT - single thread required : synchronization provided by cache manager.

	*/
	private int	keepCount;

	/**
		The Cacheable object being represented.

		<BR> Mutable - content dynamic
	*/
	private Cacheable	entry;
		
	/**
		Create a CachedItem in the not valid state.
	*/
	public CachedItem() {
	}

	/**
		Keep the cached object after a search.

	*/
	public void keepAfterSearch() {
		keepCount++;
		setUsed(true);
	}

	public void keepForCreate() {
		if (SanityManager.DEBUG) {
			SanityManager.ASSERT(!isKept());
			SanityManager.ASSERT(!isValid());
		}
		keepCount = 1;
		settingIdentity_ = true;
	}

    public void unkeepForCreate( )
    {
        settingIdentityComplete();
        unkeep();
    }

	public void keepForClean() {
		if (SanityManager.DEBUG) {
			SanityManager.ASSERT(isValid());
		}
		keepCount++;
	}



	/**
		Unkeep the cached object.

		<P>MT - not synchronized, only modified single threaded by the cache manager

		@return if the object is still kept after this call. 
	*/
	public synchronized boolean unkeep() {
		boolean unkept = --keepCount == 0;

		if (SanityManager.DEBUG) {
			SanityManager.ASSERT(keepCount >= 0);
		}
		return (unkept && removeRequested_);
	}

	/**
		Is the cached object kept?

		<P>MT - not synchronized, only accessed single threaded by the cache manager
	*/
	public final boolean isKept() {

		return keepCount != 0;
	}

	/**
		Clean the cached object

		<P>MT -		<BR>
		The wait will not release the lock on the cache manager, so the
		cache manager should not waitfor clean inside a sync block or
		the whole cache will freeze

		@param forRemove if true, get rid of the backend persistent store object
		@exception StandardException error thrown while writing cacheable
		object to disk
	*/
	public void clean(boolean forRemove) throws StandardException
	{
		entry.clean(forRemove);
	}

	/**
		Set the state of the to-be removed flag.
	*/
	public synchronized void setRemoveState() {
		removeRequested_ = true;
	}

	/**
		Does the cached object have a valid identity.
	*/
	public final synchronized boolean isValid() {
		return valid_;
	}

	/**
		Set the valid state of the cached object.
	*/
	public synchronized void setValidState(boolean flag) {
		valid_ = flag;
		removeRequested_ = false;
		removeOk_ = false;
		recentlyUsed_ = flag;
	}

	/**
		Get the cached object.
	*/
	public Cacheable getEntry() {
		return entry;
	}

	/**
		Make entry (the Cacheable) take on a new identity.
	*/
	public Cacheable takeOnIdentity(CacheManager cm, CacheableFactory holderFactory,
		Object key, boolean forCreate, Object createParameter)
		throws StandardException {

		// tell the object it needs to create itself
		Cacheable oldEntry = entry;
		if (oldEntry == null)
			oldEntry = holderFactory.newCacheable(cm);

		if (forCreate) {
			entry = oldEntry.createIdentity(key, createParameter);
		} else {
			entry = oldEntry.setIdentity(key);
		}

		if (entry != null) {
			// item was found or created
			if (SanityManager.DEBUG) {
				SanityManager.ASSERT(entry.getIdentity().equals(key));
			}

			return entry;
		}

		entry = oldEntry;
		return null;
	}

	public synchronized void settingIdentityComplete() {
		// notify all waiters that this item has finished setting its identity,
		// successfully or not.
		settingIdentity_ = false;
		notifyAll();
	}

	/**
		Allow use of the cacheable entry. 
	*/

	public synchronized Cacheable use() throws StandardException {

		while (settingIdentity_) {
			try {
				if (SanityManager.DEBUG) {
					SanityManager.DEBUG("CacheTrace", 
                        "trying to use a cached item that is taking on an identity");
				}

				wait();

			} catch (InterruptedException ie) {
				throw StandardException.interrupt(ie);
			}
		}

		// see if the setting of this identity failed ...
		if (!valid_) {
			return null;
		}

		if (SanityManager.DEBUG)
        {
            if (SanityManager.DEBUG_ON("CacheTrace"))
                SanityManager.DEBUG(
                    "CacheTrace", "item keep count is " + keepCount);
		}


		return entry;
	}

	/**
	*/
	public void remove(boolean removeNow) throws StandardException {

		if (!removeNow) {

			synchronized (this) {
				while (!removeOk_) {
					try {
						wait();
					} catch (InterruptedException ie) {
						throw StandardException.interrupt(ie);
					}
				}
			}
		}
		
		clean(true);
	}

	public synchronized void notifyRemover() {

		if (SanityManager.DEBUG) {
			SanityManager.ASSERT(removeRequested_);
			SanityManager.ASSERT(isKept());
		}

		removeOk_ = true;
		notifyAll();
	}

	/**
		The clock hand has swept past this entry.
	*/
	public synchronized void setUsed(boolean flag)
	{
		recentlyUsed_ = flag;
	}

	/**
		Has the cached object been referenced (kept) since the last sweep of
		the clock hand?
	*/
	public synchronized boolean recentlyUsed() {
		return recentlyUsed_;
	}
}

	
	
