/*
 * Copyright 2005 Ralf Joachim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.castor.cache.hashbelt;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.castor.cache.Cache;
import org.castor.cache.CacheAcquireException;
import org.castor.cache.CacheFactory;

/**
 * @author <a href="mailto:ralf DOT joachim AT syscon DOT eu">Ralf Joachim</a>
 * @version $Revision: 9041 $ $Date: 2011-08-16 11:51:17 +0200 (Di, 16 Aug 2011) $
 * @since 1.0
 */
public final class TestFIFOHashbeltFactory extends TestCase {
    public static Test suite() {
        TestSuite suite = new TestSuite("FIFOHashbeltFactory Tests");

        suite.addTest(new TestFIFOHashbeltFactory("testConstructor"));
        suite.addTest(new TestFIFOHashbeltFactory("testGetCacheType"));
        suite.addTest(new TestFIFOHashbeltFactory("testGetCacheClassName"));
        suite.addTest(new TestFIFOHashbeltFactory("testGetCache"));
        suite.addTest(new TestFIFOHashbeltFactory("testShutdown"));

        return suite;
    }

    public TestFIFOHashbeltFactory(final String name) { super(name); }

    public void testConstructor() {
        CacheFactory<String, String> cf = new FIFOHashbeltFactory<String, String>();
        assertTrue(cf instanceof FIFOHashbeltFactory);
    }

    public void testGetCacheType() {
        CacheFactory<String, String> cf = new FIFOHashbeltFactory<String, String>();
        assertEquals("fifo", cf.getCacheType());
    }

    public void testGetCacheClassName() {
        CacheFactory<String, String> cf = new FIFOHashbeltFactory<String, String>();
        String classname = "org.castor.cache.hashbelt.FIFOHashbelt";
        assertEquals(classname, cf.getCacheClassName());
    }

    public void testGetCache() {
        CacheFactory<String, String> cf = new FIFOHashbeltFactory<String, String>();
        try {
            Cache<String, String> c = cf.getCache(null);
            assertTrue(c instanceof FIFOHashbelt);
        } catch (CacheAcquireException ex) {
            fail("Failed to get instance of FIFOHashbelt from factroy");
        }
    }

    public void testShutdown() {
        CacheFactory<String, String> cf = new FIFOHashbeltFactory<String, String>();
        cf.shutdown();
    }
}
