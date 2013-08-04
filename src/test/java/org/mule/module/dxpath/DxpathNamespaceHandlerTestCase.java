/***************************************************************************
 * 
 * This file is part of the 'dxpath mule module' project at
 * https://github.com/skjolber/mule-module-dxpath
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
 * 
 ****************************************************************************/

package org.mule.module.dxpath;

import org.junit.Test;
import org.mule.tck.junit4.FunctionalTestCase;

public class DxpathNamespaceHandlerTestCase extends FunctionalTestCase
{
    protected String getConfigResources()
    {
        return "dxpath-namespace-config.xml";
    }

    @Test
    public void testDxpathConfig() throws Exception
    {
    	// empty
    }
}
