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

<!-- Translates a tutorial index XML document into an HTML tutorial index -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:import href="project.xsl"/>

    <!-- <document> gets translated to an HTML container -->
    <xsl:template match="document">
        <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                <title>
                  <xsl:value-of select="$project/title"/>
                </title>
                <link rel="stylesheet" href="tutorial.css" type="text/css"/>
                <link rel="stylesheet" href="tutorial_print.css" type="text/css" media="print"/>
                <style>
                    ul {
                        list-style-type: none;
                        padding-left: 20px;
                    }
                </style>
                <xsl:apply-templates select="head"/>
            </head>

            <body>
                <h2>
                  <xsl:value-of select="$project/title"/>
                </h2>
                <xsl:apply-templates select="body"/>
            </body>
        </html>
    </xsl:template>

    <!-- <item-group> gets translated to a properly indented list -->
    <xsl:template match="item-group">
        <h3><xsl:value-of select="@name"/></h3>
        <ul style="padding-left: 0px;">
            <xsl:apply-templates/>
        </ul>

        <xsl:if test="following::item-group">
            <hr/>
        </xsl:if>
    </xsl:template>

    <!-- <document-item> gets translated to a list item with a hyperlink -->
    <xsl:template match="document-item">
        <xsl:variable name="document" select="document(concat('../www/', @id, '.xml'))/document"/>
        <li>
            <a href="{@id}.html">
                <xsl:value-of select="$document/properties/title"/>
            </a>
            <xsl:if test="*">
                <xsl:element name="ul">
                    <xsl:apply-templates/>
                </xsl:element>
            </xsl:if>
        </li>
    </xsl:template>
</xsl:stylesheet>
