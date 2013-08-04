mule-module-dxpath
=============

Introduction
------------
This module adds an XPath transformer which resolves variables at runtime, effectively making it a dynamic XPath transformer. It should work with Mule ESB 3.2.1 or later.

XPath variables
---------------
Variables in XPath expressions are prefixed by `$`, for example the variable _playerName_ in

    /b:team/b:player[b:name = $playerName]/b:goals/text()

An XPath expression can contain multiple variables.

Usage
----------
The `dxpath` XML element syntax is best illustrated using an example:

    <dx:dxpath expression="/b:team/b:player[b:name = $playerName]/b:goals/text()" >
      <dx:variable key="playerName" value="#[header:invocation:playerName]"/>
    </dx:dxpath>

The namespace prefixes are resolved from the flow namespace manager, i.e.

    <mule-xml:namespace-manager>
	  <mule-xml:namespace prefix="b" uri="http://urn.indoor.bandy/v1.0"/>
	  <!-- more namespaces -->
	</mule-xml:namespace-manager>
 
Additional XPath variables can be added using more `variable` child elements: 

	<dx:dxpath expression="/b:team[name = $teamName]/b:player[b:name = $playerName]/b:goals/text()">
	  <dx:variable key="playerName" value="#[header:invocation:playerName]"/>
	  <dx:variable key="teamName" value="#[header:invocation:teamName]"/>
	  <!-- unlimited number of variables -->
	</dx:dxpath>

An optional `resultType` attribute can be added with the standard types of `STRING`, `BOOLEAN`, `NODESET`, `NODE` and `NUMBER` like so:

	<dx:dxpath expression="/b:team/b:player/b:name/text() = $playerName" resultType="BOOLEAN">
	  <dx:variable key="playerName" value="#[header:invocation:playerName]"/>
	</dx:dxpath>

Default `resultType` is `STRING`. 

The transformer is expecting as input a w3c DOM node, so consider adding for example

	<mule-xml:xml-to-dom-transformer returnClass="org.w3c.dom.Document"/>

to your flow. A null payload is treated as an empty document.

Adding module to project
-------------------
To use this module in your project, build the module using Maven and add the mule-module-dxpath-*.jar to your project, or include the module in your project via your local maven artifact repository. When done, modify your mule flows:

#### Add the namespace
	xmlns:dx="http://www.mulesoft.org/schema/mule/dxpath"

#### and append the schemaLocation attribute with
	http://www.mulesoft.org/schema/mule/dxpath http://www.mulesoft.org/schema/mule/dxpath/3.2/mule-dxpath.xsd

and you are ready to go :-)

License 
---------
This module is released under the [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) license. 

Author
---------
[Thomas Rørvik Skjølberg](http://www.linkedin.com/in/skjolberg) is a senior software developer from Oslo, Norway, focusing on middleware and mobile applications. While doing the occational freelance project, his day job is at  [Greenbird Integration Technologies](http://www.greenbird.com). 

Feel free to [get in touch](mailto:thomas.skjolberg@gmail.com) for feeback on this module, or if you require professional assistance.

