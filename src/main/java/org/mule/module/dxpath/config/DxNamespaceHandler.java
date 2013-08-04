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

package org.mule.module.dxpath.config;

import org.mule.config.spring.parsers.collection.ChildMapEntryDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class DxNamespaceHandler extends NamespaceHandlerSupport {
	public void init() {
		registerBeanDefinitionParser("dxpath", new DynamicXPathTransformerDefinitionParser());

		registerBeanDefinitionParser("variable", new ChildMapEntryDefinitionParser("contextProperties", "key", "value"));
	}
}
