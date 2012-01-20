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

<!-- Translates a demo index XML document into an HTML demo index -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:import href="project.xsl"/>

    <!-- Output method -->
    <xsl:output method="html" encoding="UTF-8" indent="no"
        doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
        doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>

    <!-- <document> gets translated to an HTML container -->
    <xsl:template match="document">
        <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                <title>
                  <xsl:value-of select="$project/title"/>
                </title>

                <link rel="stylesheet" href="demo.css" type="text/css"/>
                <link rel="stylesheet" href="demo_print.css" type="text/css" media="print"/>
                <link rel="icon" href="favicon.png" type="image/png" />
                <link rel="shortcut icon" href="favicon.png" type="image/png" />

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

    <!-- <document-item> gets translated to a demo summary with links to the demo -->
    <xsl:template match="document-item">
        <xsl:variable name="id" select="@id"/>
        <xsl:variable name="document" select="document(concat('../www/', $id, '.xml'))/document"/>
    <tr>
        <td class="indexLeft">
        <h3><xsl:value-of select="normalize-space($document/properties/title)"/></h3>
        <p>
            <xsl:value-of select="normalize-space($document/properties/description)"/>
        </p>
        <p>
            <a href="{$id}.html">Applet</a>

            <!-- JNLP translation must ignore the head, so if one exists, we skip JNLP link -->
            <xsl:if test="not($document/head)">
                <xsl:text> | </xsl:text>
                <a href="{$id}.jnlp">Web Start</a>
            </xsl:if>
        </p>
        </td>
        <td class="indexRight">
        <!-- Include a screenshot if one exists -->
        <xsl:if test="$project/demo-screenshots/screenshot[@id=$id]">
            <xsl:variable name="src" select="$project/demo-screenshots/screenshot[@id=$id]/@src"/>
            <p><img src="{$src}"/></p>
        </xsl:if>
        </td>
    </tr>
    </xsl:template>

</xsl:stylesheet>
