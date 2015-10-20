<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xalan/java"
exclude-result-prefixes="java"
version="1.0">
<xsl:output method="text" omit-xml-declaration="yes"  encoding="UTF-8" indent="no" />
<xsl:template match="/"><xsl:for-each select="//doc" >
    <xsl:value-of select="./arr[@name='export']/str" /></xsl:for-each></xsl:template></xsl:stylesheet>