<?xml version="1.0" encoding="UTF-8"?>
<!--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to you under the Apache License,
Version 2.0 (the "License"); you may not use this file except in
compliance with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="common.xsl"/>

    <!--
    Output method. NOTE This must be text because JSP tags are not valid XML, so setting the
    output method to XML caused the XSLT process to escape the JSP tags, thus breaking the JSP
    -->
    <xsl:output method="text"/>

    <xsl:template match="demo">
        &lt;?xml version="1.0" encoding="UTF-8" ?&gt;

        &lt;%@ page language="java" contentType="application/x-java-jnlp-file" pageEncoding="UTF-8" %&gt;
        &lt;%@ include file="jnlp_common.jsp" %&gt;

        &lt;jnlp spec="1.6+" codebase="&lt;%= codebase %&gt;" href="&lt;%= href %&gt;"&gt;
            &lt;information&gt;
                &lt;title&gt;Pivot <xsl:value-of select="//document/properties/title"/> Demo&lt;/title&gt;
                &lt;description&gt;<xsl:value-of select="//document/properties/description"/>&lt;/description&gt;
                &lt;vendor&gt;Apache Pivot&lt;/vendor&gt;
                &lt;homepage href="http://pivot.apache.org/"/&gt;
                &lt;icon kind="shortcut" href="logo.png"/&gt;
                &lt;offline-allowed/&gt;
                &lt;shortcut online="false"&gt;
                    &lt;desktop/&gt;
                &lt;/shortcut&gt;
            &lt;/information&gt;

            &lt;resources&gt;
                &lt;property name="jnlp.packEnabled" value="true"/&gt;
                &lt;property name="sun.awt.noerasebackground" value="true"/&gt;
                &lt;property name="sun.awt.erasebackgroundonresize=true" value="true"/&gt;

                &lt;java version="1.6+" href="http://java.sun.com/products/autodl/j2se"/&gt;

                <xsl:for-each select="libraries/library">
                    <xsl:choose>
                        <xsl:when test=".='core'">
                            &lt;jar href="<xsl:value-of select="$jar-core"/>"/&gt;
                        </xsl:when>
                        <xsl:when test=".='wtk'">
                            &lt;jar href="<xsl:value-of select="$jar-wtk"/>" main="true"/&gt;
                            &lt;jar href="<xsl:value-of select="$jar-wtk-terra"/>"/&gt;
                        </xsl:when>
                        <xsl:when test=".='web'">
                            &lt;jar href="<xsl:value-of select="$jar-web"/>"/&gt;
                        </xsl:when>
                        <xsl:when test=".='demos'">
                            &lt;jar href="<xsl:value-of select="$jar-demos"/>"/&gt;
                        </xsl:when>
                        <xsl:when test=".='tutorials'">
                            &lt;jar href="<xsl:value-of select="$jar-tutorials"/>"/&gt;
                        </xsl:when>
                        <xsl:when test=".='tools'">
                            &lt;jar href="<xsl:value-of select="$jar-tools"/>"/&gt;
                        </xsl:when>
                    </xsl:choose>
                </xsl:for-each>
            &lt;/resources&gt;

            &lt;application-desc main-class="org.apache.pivot.wtk.DesktopApplicationContext"&gt;
                &lt;argument&gt;<xsl:value-of select="@class"/>&lt;/argument&gt;
                &lt;argument&gt;--width=<xsl:value-of select="@width"/>&lt;/argument&gt;
                &lt;argument&gt;--height=<xsl:value-of select="@height"/>&lt;/argument&gt;
                &lt;argument&gt;--center=true&lt;/argument&gt;
                <xsl:for-each select="startup-properties/*">
                &lt;argument&gt;--<xsl:value-of select="name(.)"/>=<xsl:value-of select="."/>&lt;/argument&gt;
                </xsl:for-each>
            &lt;/application-desc&gt;

            &lt;update check="background"/&gt;
        &lt;/jnlp&gt;
    </xsl:template>

    <xsl:template match="*|@*">
        <xsl:apply-templates select="*"/>
    </xsl:template>
</xsl:stylesheet>
