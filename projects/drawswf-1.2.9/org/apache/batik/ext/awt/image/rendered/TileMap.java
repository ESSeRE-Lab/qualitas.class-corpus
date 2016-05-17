/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.batik.ext.awt.image.rendered;

import java.awt.Point;
import java.awt.image.Raster;
import java.util.HashMap;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

public class TileMap implements TileStore {
    private static final boolean DEBUG = false;
    private static final boolean COUNT = false;		

    private static ReferenceQueue queue = new ReferenceQueue();

    private static HashMap items  =new HashMap();
    private HashMap rasters=new HashMap();
 
    static class TileMapLRUMember extends TileLRUMember {
        public Point   pt;
        public SoftReference parent;
        TileMapLRUMember(TileMap parent, Point pt, Raster ras) {
            super(ras);
            this.parent = new SoftReference(parent);
            this.pt     = pt;
        }

        public void setRaster(Raster ras) {
            hRaster = ras;
            synchronized (items) {
                if (wRaster != null)
                    items.remove(wRaster);
                wRaster = new SoftReference(ras, queue);
                items.put(wRaster, this);
            }
        }
    }

    private TileGenerator source = null;
    private LRUCache      cache = null;

    public TileMap(TileGenerator source,
		   LRUCache cache) {
        this.cache    = cache;
        this.source   = source;
    }

    public void setTile(int x, int y, Raster ras) {
        Point pt = new Point(x, y);

        if (ras == null) {
            // Clearing entry...
            Object o = rasters.remove(pt);
            if (o != null) 
                cache.remove((TileMapLRUMember)o);
            return;
        }

        Object o = rasters.get(pt);
        TileMapLRUMember item;
        if (o == null) {
            item = new TileMapLRUMember(this, pt, ras);
            rasters.put(pt, item);
        } else {
            item = (TileMapLRUMember)o;
            item.setRaster(ras);
        }
		
        cache.add(item);
        if (DEBUG) System.out.println("Setting: (" + x + ", " + y + ")");
    }

    // Returns Raster if the tile is _currently_ in the cache.  
    // If it is not currently in the cache it returns null.
    public Raster getTileNoCompute(int x, int y) {
        Point pt = new Point(x, y);
        Object o = rasters.get(pt);
        if (o == null) 
            return null;

        TileMapLRUMember item = (TileMapLRUMember)o;
        Raster ret = item.retrieveRaster();
        if (ret != null)
            cache.add(item);
        return ret;
    }

    public Raster getTile(int x, int y) {
        if (DEBUG) System.out.println("Fetching: (" + (x) + ", " + 
                                      (y) + ")");
        if (COUNT) synchronized (TileMap.class) { requests++; }

        Raster       ras  = null;
        Point pt = new Point(x, y);
        Object o = rasters.get(pt);
        TileMapLRUMember item = null;
        if (o != null) {
            item = (TileMapLRUMember)o;
            ras = item.retrieveRaster();
        }
		
        if (ras == null) {
            if (DEBUG) System.out.println("Generating: ("+(x)+", "+
                                          (y) + ")");
            if (COUNT) synchronized (TileMap.class) { misses++; }
            ras = source.genTile(x, y);

            // In all likelyhood the contents of this tile is junk!
            // So don't cache it (returning is probably fine since it
            // shouldn't come back to haunt us...)
            if (Thread.currentThread().isInterrupted())
                return ras;

            if (item != null)
                item.setRaster(ras);
            else  {
                item = new TileMapLRUMember(this, pt, ras);
                rasters.put(pt, item);
            }
        }

        // Update the item's position in the cache..
        cache.add(item);

        return ras;
    }

    static int requests;
    static int misses;

    static Thread cleanup;

    static {
        cleanup = new Thread() {
                public void run() {
                    while(true) {
                        Reference ref;

                        try {
                            ref = queue.remove();
                        } catch (InterruptedException ie) {
                            continue;
                        }
                        synchronized (items) {
                            Object o = items.remove(ref);
                            if (DEBUG) System.out.println("Cleaning: " + o);
                            if (o == null) continue;
                        
                            TileMapLRUMember item = (TileMapLRUMember)o;
			    TileMap parent = (TileMap)item.parent.get();
			    if (parent != null)
				parent.rasters.remove(item.pt);
                        }
                    }
                }
            };
        cleanup.setDaemon(true);
        cleanup.start();
    }

}
