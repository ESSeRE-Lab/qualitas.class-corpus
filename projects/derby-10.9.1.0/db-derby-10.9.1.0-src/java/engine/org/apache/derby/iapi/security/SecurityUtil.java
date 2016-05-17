/*

   Derby - Class org.apache.derby.iapi.security.SecurityUtil

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package org.apache.derby.iapi.security;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.io.IOException;

import java.security.PrivilegedAction;
import java.security.AccessController;
import java.security.AccessControlException;
import java.security.AccessControlContext;
import java.security.Permission;
import javax.security.auth.Subject;

import org.apache.derby.authentication.SystemPrincipal;
import org.apache.derby.iapi.util.IdUtil;
import org.apache.derby.iapi.error.StandardException;


/**
 * This class provides helper functions for security-related features.
 */
public class SecurityUtil {

    /**
     * Creates a (read-only) Subject representing a given user
     * as a System user within Derby.
     *
     * @param user the user name
     * @return a Subject representing the user by its exact and normalized name
     *
     * @see <a href="http://wiki.apache.org/db-derby/UserIdentifiers">User Names & Authorization Identifiers in Derby</a>
     */
    static public Subject createSystemPrincipalSubject(String user) {
        final Set principals = new HashSet();
        // add the authenticated user
        if (user != null) {
            // The Java security runtime checks whether a Subject falls
            // under a Principal policy by looking for a literal match
            // of the Principal name as exactly found in a policy file
            // clause with any of the Subject's listed Principal names.
            //
            // To support Authorization Identifier as Principal names
            // we add two Principals here: one with the given name and
            // another one with the normalized name.  This way, a
            // permission will be granted if the authenticated user name
            // matches a Principal clause in the policy file with either
            // the exact same name or the normalized name.
            //
            // An alternative approach of normalizing all names within
            // SystemPrincipal has issues; see comments there.
            principals.add(new SystemPrincipal(user));
            principals.add(new SystemPrincipal(getAuthorizationId(user)));
        }
        final boolean readOnly = true;
        final Set credentials = new HashSet();
        return new Subject(readOnly, principals, credentials, credentials);
    }

    /**
     * Returns the Authorization Identifier for a principal name.
     *
     * @param name the name of the principal
     * @return the authorization identifier for this principal
     */
    static private String getAuthorizationId(String name) {
        // RuntimeException messages not localized
        if (name == null) {
            throw new NullPointerException("name can't be null");
        }
        if (name.length() == 0) {
            throw new IllegalArgumentException("name can't be empty");
        }
        try {
            return IdUtil.getUserAuthorizationId(name);
        } catch (StandardException se) {
            throw new IllegalArgumentException(se.getMessage());
		}
    }

    /**
     * Checks that a Subject has a Permission under the SecurityManager.
     * To perform this check the following policy grant is required
     * <ul>
     * <li> to run the encapsulated test:
     *      permission javax.security.auth.AuthPermission "doAsPrivileged";
     * </ul>
     * or an AccessControlException will be raised detailing the cause.
     * <p>
     *
     * @param subject the subject representing the SystemPrincipal(s)
     * @param perm the permission to be checked
     * @throws AccessControlException if permissions are missing
     */
    static public void checkSubjectHasPermission(final Subject subject,
                                                 final Permission perm) {
        // the checks
        final PrivilegedAction runCheck
            = new PrivilegedAction() {
                    public Object run() {
                        AccessController.checkPermission(perm);
                        return null;
                    }
                };
        final PrivilegedAction runCheckAsPrivilegedUser
            = new PrivilegedAction() {
                    public Object run() {
                        // run check only using the the subject
                        // (by using null as the AccessControlContext)
                        final AccessControlContext acc = null;
                        Subject.doAsPrivileged(subject, runCheck, acc);
                        return null;
                    }
                };

        // run check as privileged action for narrow codebase permissions
        AccessController.doPrivileged(runCheckAsPrivilegedUser);
    }

    /**
     * Checks that a User has a Permission under the SecurityManager.
     * To perform this check the following policy grant is required
     * <ul>
     * <li> to run the encapsulated test:
     *      permission javax.security.auth.AuthPermission "doAsPrivileged";
     * </ul>
     * or an AccessControlException will be raised detailing the cause.
     * <p>
     *
     * @param user the user to be check for having the permission
     * @param perm the permission to be checked
     * @throws AccessControlException if permissions are missing
     */
    static public void checkUserHasPermission(String user,
                                              Permission perm) {
        // approve action if not running under a security manager
        if (System.getSecurityManager() == null) {
            return;
        }

        // check the subject for having the permission
        final Subject subject = createSystemPrincipalSubject(user);
        checkSubjectHasPermission(subject, perm);
    }
}
