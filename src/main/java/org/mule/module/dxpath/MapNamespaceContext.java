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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

public final class MapNamespaceContext implements NamespaceContext {
	
	private Map<String, String> namespaces = new HashMap<String, String>();

	public MapNamespaceContext(final Map<String, String> ns) {
		this.namespaces = ns;
	}

	public void addNamespace(final String prefix, final String namespaceURI) {
		this.namespaces.put(prefix, namespaceURI);
	}

	public void addNamespaces(final Map<String, String> ns) {
		this.namespaces.putAll(ns);
	}

	public String getNamespaceURI(String prefix) {
		return (String) namespaces.get(prefix);
	}

	public String getPrefix(String namespaceURI) {
		for(Map.Entry<String, String> e : namespaces.entrySet()) {
			if (e.getValue().equals(namespaceURI)) {
				return (String) e.getKey();
			}
		}
		return null;
	}

	public Iterator<?> getPrefixes(String namespaceURI) {
		String prefix = getPrefix(namespaceURI);
		if (prefix == null) {
			return Collections.emptyList().iterator();
		} else {
			return Arrays.asList(prefix).iterator();
		}
	}

	
}
