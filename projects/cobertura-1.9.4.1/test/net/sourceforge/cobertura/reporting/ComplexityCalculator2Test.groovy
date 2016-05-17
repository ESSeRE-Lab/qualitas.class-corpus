/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (C) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 * Copyright (C) 2008 John Lewis
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Ant" and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package net.sourceforge.cobertura.reporting

import junit.framework.TestCase
import net.sourceforge.cobertura.test.util.TestUtil
import net.sourceforge.cobertura.util.FileFinder
import net.sourceforge.cobertura.coveragedata.SourceFileData
import net.sourceforge.cobertura.reporting.ComplexityCalculator

public class ComplexityCalculator2Test extends TestCase {
	

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	void setUp() throws Exception {
	}
	
	public void testSearchJarsForSourceInJar() {
		TestUtil.withTempDir { tempDir ->
		
			def zipFile = TestUtil.createSourceArchive(tempDir)
			
			//create a ComplexityCalculator that will use the archive
    		def fileFinder = new FileFinder();
    		fileFinder.addSourceDirectory(zipFile.parentFile.absolutePath);
    		def complexity = new ComplexityCalculator( fileFinder)
    	
        	double ccn1 = complexity.getCCNForSourceFile( new SourceFileData(TestUtil.SIMPLE_SOURCE_PATHNAME));
        	assertTrue( ccn1==1.0);
		}
    }
	
	public void testAnnotatedSource() {
		/*
		 * Test for bug #2818738.
		 */
		TestUtil.withTempDir { tempDir ->
			def filename = "TBSException.java"
			def sourceFile = new File(tempDir, filename)
			sourceFile.write('''
public class TBSException extends Exception {
   public TBSException (ErrorHandler handler, Exception wrap) {
		super(wrap);
        @SuppressWarnings("unchecked")
        final Iterator<Exception> iter = handler.getExceptions().iterator();  // LINE 27
		for (; iter.hasNext();) 
        {
			Exception exception = iter.next();
			this.errors.add(exception.getMessage());
		}
	}
}
''')
			//create a ComplexityCalculator that will use the archive
			def fileFinder = new FileFinder();
			fileFinder.addSourceDirectory(tempDir.absolutePath);
			def complexity = new ComplexityCalculator( fileFinder)
			
			double ccn1 = complexity.getCCNForSourceFile( new SourceFileData(filename));
			assertNotNull(ccn1)
			assertEquals( 2.0, ccn1 as Double, 0.01);
		}
	}

	/**
	 * This test highlights an issue with Javancss.
	 * 
	 * http://jira.codehaus.org/browse/JAVANCSS-37
	 * 
	 */
	public void testGenericsProblem() {
		TestUtil.withTempDir { tempDir ->
			def filename = "UserAudit.java"
			def sourceFile = new File(tempDir, filename)
			sourceFile.write('''
import java.util.ArrayList;
import java.util.List;


public class UserAudit extends UserAuditParent {
	void postCopyOnDestination(String str) throws InstantiationException, IllegalAccessException {
		List<AllowedMMProduct> listToReset = new ArrayList<AllowedMMProduct>();
		
		List<AllowedMMProductAudit> auditProducts;
		auditProducts = this.<AllowedMMProduct,AllowedMMProductAudit>copyListFromParent(AllowedMMProductAudit.class, getMmAuthorisedProducts_());
	}
	
	List<AllowedMMProduct> getMmAuthorisedProducts_() {
		return null;
	}
}
''')
			//create a ComplexityCalculator that will use the archive
			def fileFinder = new FileFinder();
			fileFinder.addSourceDirectory(tempDir.absolutePath);
			def complexity = new ComplexityCalculator( fileFinder)
			
			double ccn1 = complexity.getCCNForSourceFile( new SourceFileData(filename));
			assertNotNull(ccn1)
			assertEquals( "Javancss issue has been fixed: http://jira.codehaus.org/browse/JAVANCSS-37.   Now fix this test.", 0.0/*should be 2.0?*/, ccn1 as Double, 0.01);
		}
	}

}
