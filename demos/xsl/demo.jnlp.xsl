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

<!-- Translates a demo XML document into a JNLP demo file -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:import href="project.xsl"/>

    <!-- Output method -->
    <xsl:output method="xml" encoding="UTF-8" indent="no"/>

    <!-- Parameters (overrideable) -->
    <xsl:param name="version"/>
    <xsl:param name="root"/>

    <!-- <document> delegates to <application> -->
    <xsl:template match="document">
        <xsl:apply-templates select="//application"/>
    </xsl:template>

    <!-- <root> gets resolved to the 'root' XSL parameter -->
    <xsl:template match="root">
        <xsl:value-of select="$root"/>
    </xsl:template>

    <!-- <application> translates to JNLP XML -->
    <xsl:template match="application">
        <xsl:text disable-output-escaping="yes">
            <![CDATA[
            <%@ page language="java" contentType="application/x-java-jnlp-file" pageEncoding="UTF-8" %>
            <%
                // response.setHeader("Cache-Control", "no-cache");
                // response.setHeader("Pragma", "no-cache");
                // response.setDateHeader("Expires", 0);

                String requestURL = request.getRequestURL().toString();
                int lastSlash = requestURL.lastIndexOf('/');
                String codebase = "";
                String href = "";
                if (requestURL != null) {
                    if (lastSlash < 0) {
                        lastSlash = 0;
                    }

                    codebase = requestURL.substring(0, lastSlash + 1);
                    if ((lastSlash + 1) < requestURL.length()) {
                        href = requestURL.substring(lastSlash + 1);
                    }

                }
            %>

            <jnlp spec="1.7+" codebase="<%= codebase %>" href="<%= href %>">
            ]]>
        </xsl:text>

        <information>
            <title>Pivot <xsl:value-of select="/document/properties/title"/> Demo</title>
            <description>
                <xsl:value-of select="normalize-space(/document/properties/description)"/>
            </description>
            <vendor><xsl:value-of select="$project/vendor"/></vendor>
            <homepage href="{$project/@href}"/>
        </information>

        <xsl:if test="boolean(libraries/@signed)">
            <security>
                <all-permissions/>
            </security>
        </xsl:if>

        <resources>
            <property name="jnlp.packEnabled" value="true"/>
            <xsl:if test="boolean(libraries/@signed)">
                <property name="sun.awt.noerasebackground" value="true"/>
                <property name="sun.awt.erasebackgroundonresize=true" value="true"/>
            </xsl:if>

    <xsl:choose>
        <xsl:when test="/document/properties/java_memory_options_huge">
            <java version="1.7+" href="http://java.sun.com/products/autodl/j2se"
                initial-heap-size="256M" max-heap-size="1024M"
            />
        </xsl:when>
        <xsl:otherwise>
            <java version="1.7+" href="http://java.sun.com/products/autodl/j2se"/>
        </xsl:otherwise>
    </xsl:choose>

            <xsl:variable name="signed" select="libraries/@signed"/>
            <xsl:for-each select="libraries/library">
                <xsl:element name="jar">
                    <xsl:attribute name="href">
                        <xsl:value-of select="'lib/pivot-'"/>
                        <xsl:value-of select="."/>
                        <xsl:value-of select="'-'"/>
                        <xsl:value-of select="$version"/>
                        <xsl:if test="$signed">
                            <xsl:value-of select="'.signed'"/>
                        </xsl:if>
                        <xsl:value-of select="'.jar'"/>
                    </xsl:attribute>
                    <xsl:if test=".='wtk'">
                        <xsl:attribute name="main">true</xsl:attribute>
                    </xsl:if>
                </xsl:element>
            </xsl:for-each>

            <xsl:choose>
                <xsl:when test='$signed'>
                    <jar href="lib/svgSalamander-tiny.signed.jar"/>
                </xsl:when>
                <xsl:otherwise>
                    <jar href="lib/svgSalamander-tiny.jar"/>
                </xsl:otherwise>
            </xsl:choose>
        </resources>

        <application-desc main-class="org.apache.pivot.wtk.DesktopApplicationContext">
            <argument><xsl:value-of select="@class"/></argument>
            <argument>--center=true</argument>
            <xsl:for-each select="startup-properties/*">
            <argument>--<xsl:value-of select="name(.)"/>=<xsl:apply-templates/></argument>
            </xsl:for-each>
        </application-desc>

        <update check="background"/>

        <xsl:text disable-output-escaping="yes"><![CDATA[</jnlp>]]></xsl:text>
    </xsl:template>
</xsl:stylesheet>
