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

<!-- Translates a demo XML document into an HTML demo page -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:import href="project.xsl"/>

    <!-- Output method -->
    <xsl:output method="html" encoding="UTF-8" indent="no"
        doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
        doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>

    <!-- Parameters (overrideable) -->
    <xsl:param name="version"/>
    <xsl:param name="root"/>

    <!-- <document> gets translated into an HTML container -->
    <xsl:template match="document">
        <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                <title>Pivot <xsl:value-of select="properties/title"/> Demo</title>

                <link rel="stylesheet" href="demo.css" type="text/css"/>
                <link rel="stylesheet" href="demo_print.css" type="text/css" media="print"/>

                <script xmlns="" type="text/javascript" src="http://java.com/js/deployJava.js"/>
                <xsl:if test="boolean(properties/full-screen)">
                    <style type="text/css">
                        * {
                            padding: 0px;
                            margin: 0px;
                        }

                        html, body {
                            height: 100%;
                            overflow: hidden;
                        }
                    </style>
                </xsl:if>
                <xsl:apply-templates select="head"/>
            </head>

            <body>
                <xsl:apply-templates select="body"/>
            </body>
        </html>
    </xsl:template>

    <!-- <root> gets resolved to the 'root' XSL parameter -->
    <xsl:template match="root">
        <xsl:value-of select="$root"/>
    </xsl:template>

    <!-- <application> gets translated to a JavaScript block that launches the applet -->
    <xsl:template match="application">
        <script type="text/javascript">
            <!-- Base attributes -->
            var attributes = {
                code:"org.apache.pivot.wtk.BrowserApplicationContext$HostApplet",
                width:"<xsl:value-of select="@width"/>",
                height:"<xsl:value-of select="@height"/>"
            };

            <!-- Additional attributes -->
            <xsl:for-each select="attributes/*">
                attributes.<xsl:value-of select="name(.)"/> = '<xsl:value-of select="."/>';
            </xsl:for-each>

            <!-- Archive attribute -->
            var libraries = [];
            <xsl:variable name="signed" select="libraries/@signed"/>
            <xsl:for-each select="libraries/library">
                <xsl:text><![CDATA[libraries.push("]]></xsl:text>
                <xsl:value-of select="'lib/pivot-'"/>
                <xsl:value-of select="."/>
                <xsl:value-of select="'-'"/>
                <xsl:value-of select="$version"/>
                <xsl:if test="$signed">
                    <xsl:value-of select="'.signed'"/>
                </xsl:if>
                <xsl:value-of select="'.jar'"/>
                <xsl:text><![CDATA[");
                ]]></xsl:text>
            </xsl:for-each>

            <xsl:choose>
                <xsl:when test='$signed'>
                    libraries.push("lib/svgSalamander-tiny.signed.jar");
                </xsl:when>
                <xsl:otherwise>
                    libraries.push("lib/svgSalamander-tiny.jar");
                </xsl:otherwise>
            </xsl:choose>

            attributes.archive = libraries.join(",");

            <!-- Base parameters -->
            var parameters = {
                codebase_lookup:false,
                application_class_name:'<xsl:value-of select="@class"/>'
            };

            <!-- Java arguments -->
    <xsl:choose>
        <xsl:when test="/document/properties/java_memory_options_huge">
            var javaArguments = ["-Dsun.awt.noerasebackground=true",
                "-Dsun.awt.erasebackgroundonresize=true",
                "-Xms256M -Xmx1024M"
                ];
        </xsl:when>
        <xsl:otherwise>
            var javaArguments = ["-Dsun.awt.noerasebackground=true",
                "-Dsun.awt.erasebackgroundonresize=true"];
        </xsl:otherwise>
    </xsl:choose>

            <xsl:if test="java-arguments">
                <xsl:for-each select="java-arguments/*">
                    javaArguments.push("-D<xsl:value-of select="name(.)"/>=<xsl:apply-templates/>");
                </xsl:for-each>
            </xsl:if>

            parameters.java_arguments = javaArguments.join(" ");

            <!-- Startup properties -->
            <xsl:if test="startup-properties">
                var startupProperties = [];
                <xsl:for-each select="startup-properties/*">
                    startupProperties.push("<xsl:value-of select="name(.)"/>=<xsl:apply-templates/>");
                </xsl:for-each>
                parameters.startup_properties = startupProperties.join("&amp;");
            </xsl:if>

            <!-- System properties -->
            <xsl:if test="system-properties">
                var systemProperties = [];
                <xsl:for-each select="system-properties/*">
                    systemProperties.push("<xsl:value-of select="name(.)"/>=<xsl:apply-templates/>");
                </xsl:for-each>
                parameters.system_properties = systemProperties.join("&amp;");
            </xsl:if>

            deployJava.runApplet(attributes, parameters, "1.7");
        </script>
    </xsl:template>
</xsl:stylesheet>
