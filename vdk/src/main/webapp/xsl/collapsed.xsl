<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xalan/java"
  xmlns:rb="cz.incad.xsl.ResourceBundleService"
  exclude-result-prefixes="java rb"
  version="1.0">
    <xsl:output method="html"/>
    <xsl:param name="locale" select="locale" />
    <xsl:param name="q" select="q" />
    <xsl:variable name="i18n" select="rb:getBundle('labels', $locale)"/>
    <xsl:template match="/">
            <xsl:for-each select="//doc" >
                <xsl:call-template name="hit">
                </xsl:call-template>
            </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="hit">
        <xsl:variable name="zdroj" select="./str[@name='zdroj']" />
        <li class="u_res">
            <xsl:attribute name="id">res_<xsl:value-of select="str[@name='id']" /></xsl:attribute>
        <div class="ex">
            <xsl:attribute name="data-ex">{"exemplare":[
                <xsl:for-each select="./arr[@name='ex']/str">
                    <xsl:value-of  select="." /><xsl:if test="position()!=last()">,</xsl:if>
                </xsl:for-each>]}
            </xsl:attribute>
            <xsl:choose>
            <xsl:when test="contains($zdroj, 'MZK')">
                <xsl:attribute name="data-icon">img/icons/zdroj/mzk.gif</xsl:attribute>
            </xsl:when>
            <xsl:when test="contains($zdroj, 'VKOL')">
                <xsl:attribute name="data-icon">img/icons/zdroj/vkol.gif</xsl:attribute>
            </xsl:when>
            <xsl:when test="contains($zdroj, 'NKC')">
                <xsl:attribute name="data-icon">img/icons/zdroj/nkp.gif</xsl:attribute>
            </xsl:when>
            </xsl:choose>
            <xsl:attribute name="data-zdroj"><xsl:value-of select="$zdroj" /></xsl:attribute>  
        </div>  
        </li>  
    </xsl:template>
    
</xsl:stylesheet>
