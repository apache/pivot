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
    <xsl:param name="release"/>

    <!-- Output method -->
    <xsl:output method="html" encoding="UTF-8" indent="no"
        doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
        doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>

    <xsl:template match="document">
        <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                <title>Pivot Demos</title>
                <link rel="stylesheet" href="demo.css" type="text/css"/>
                <link rel="icon" href="favicon.png" type="image/png" />
                <link rel="shortcut icon" href="favicon.png" type="image/png" />
                <style>
                    body {
                        font-family:Verdana;
                        font-size:11px;
                    }

                    p.caption {
                        font-style:italic;
                        padding-top:0px;
                    }

                    p.command {
                        font-family:"Consolas", "Monaco", "Bitstream Vera Sans Mono", "Courier New";
                        background-color:#E7E5DC;
                        padding-top:12px;
                        padding-left:24px;
                        padding-bottom:12px;
                        padding-right:24px;
                    }

                    pre.snippet {
                        padding:6px;
                        border:#E7E5DC solid 1px;
                        font-family:"Consolas", "Monaco", "Bitstream Vera Sans Mono", "Courier New";
                        font-size:1em;
                    }

                    tt {
                        font-family:"Consolas", "Monaco", "Bitstream Vera Sans Mono", "Courier New";
                    }

                    table {
                        font-size:11px;
                    }

                    img {
                        vertical-align: middle;
                    }
                </style>
                <script xmlns="" type="text/javascript" src="http://java.com/js/deployJava.js"></script>
                <xsl:apply-templates select="head"/>
            </head>

            <body>
                <xsl:apply-templates select="body"/>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="head">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="body">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="demo">
        <xsl:if test="position()&gt;1">
            <hr/>
        </xsl:if>

        <xsl:variable name="id" select="@id"/>
        <xsl:variable name="demo" select="document(concat('../www/', $id, '.xml'))/document"/>

        <h3><a href="{$id}.html"><xsl:value-of select="$demo/properties/title"/></a></h3>
        <p><a href="{$id}.jnlp">Web start</a></p>
        <p><xsl:value-of select="$demo/properties/description"/></p>
        <xsl:if test="screenshot[@kind='large']">
            <xsl:variable name="src" select="screenshot[@kind='large']"/>
            <p><img src="{$src}"/></p>
        </xsl:if>
    </xsl:template>

    <xsl:template match="*|@*">
        <xsl:copy>
            <xsl:apply-templates select="@*|*|text()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
