/*

   Derby - Class org.apache.derby.impl.sql.execute.CursorActivation

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

package org.apache.derby.impl.sql.execute;

/**
 *
 * In the family of activation support classes,
 * this one provides an activation with a cursor name.
 *
 */
public abstract class CursorActivation 
	extends BaseActivation
{
	/**
	 * remember the cursor name
	 */
	public void	setCursorName(String cursorName) 
	{
		if (!isClosed())
			super.setCursorName(cursorName);
	}

	/**
	 * @see org.apache.derby.iapi.sql.Activation#isCursorActivation
	 */
	public boolean isCursorActivation()
	{
		return true;
	}
}
