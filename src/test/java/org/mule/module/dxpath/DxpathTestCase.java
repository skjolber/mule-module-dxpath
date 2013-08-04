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

import org.junit.Assert;
import org.junit.Test;
import org.mule.api.MuleEvent;
import org.mule.construct.Flow;
import org.mule.tck.junit4.FunctionalTestCase;

public class DxpathTestCase extends FunctionalTestCase {
	protected String getConfigResources() {
		return "dxpath-functional-test-config.xml";
	}

	@Test
	public void testNoNamespace() throws Exception {
		runFlowAndExpect("noNamespace", Boolean.TRUE);
	}

	@Test
	public void testString() throws Exception {
		runFlowAndExpect("noNamespace", Boolean.TRUE);
	}

	@Test
	public void testDefaultNamespace() throws Exception {
		runFlowAndExpect("defaultNamespace", Boolean.TRUE);
	}

	@Test
	public void testSingleNamespace() throws Exception {
		runFlowAndExpect("singleNamespace", Boolean.TRUE);
	}

	@Test
	public void testMultipleNamespace() throws Exception {
		runFlowAndExpect("multipleNamespace", Boolean.TRUE);
	}

	@Test
	public void testResultTypeString() throws Exception {
		runFlowAndExpect("resultTypeString", "Magnus");
	}

	@Test
	public void testResultTypeDefault() throws Exception {
		runFlowAndExpect("resultTypeString", "Magnus");
	}

	@Test
	public void testResultTypeNode() throws Exception {
		runFlowAndExpect("resultTypeNode", Boolean.TRUE);
	}


	@Test
	public void testNullPayload() throws Exception {
		runFlowAndExpect("nullPayload", Boolean.TRUE);
	}

	/**
	 * Run the flow specified by name and assert equality on the expected output
	 * 
	 * @param flowName
	 *            The name of the flow to run
	 * @param expect
	 *            The expected output
	 */
	protected <T> void runFlowAndExpect(String flowName, T expect) throws Exception {
		Flow flow = (Flow) getFlowConstruct(flowName);
		MuleEvent event = getTestEvent(null);
		MuleEvent responseEvent = flow.process(event);
		Assert.assertEquals(expect, responseEvent.getMessage().getPayload());
	}

}
