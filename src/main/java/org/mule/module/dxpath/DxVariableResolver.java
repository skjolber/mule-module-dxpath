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

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;

public class DxVariableResolver implements XPathVariableResolver {

    private Map<String, Object> variables = new HashMap<String, Object>();

    public void newVariable(String name, Object value) {
    	variables.put(name, value);
    }

    public Object resolveVariable(QName variableName) {
        Object object = variables.get(variableName.getLocalPart());
        if(object == null) {
        	throw new IllegalArgumentException("Cannot resolve variable " + variableName.getLocalPart());
        }
        return object;
    }

    public void clear() {
    	variables.clear();
    }
}