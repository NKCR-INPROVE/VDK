<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xalan/java"
  xmlns:rb="cz.incad.xsl.ResourceBundleService"
  version="1.0">
    <xsl:output method="html"/>
    <xsl:include href="xsl/pagination.xsl" />
    <xsl:param name="locale" select="locale" />
    <xsl:param name="pagination" select="1" />
    <xsl:variable name="i18n" select="rb:getBundle('labels', $locale)"/>
    <xsl:template match="/">
        <xsl:variable name="firsthit"><xsl:choose>
            <xsl:when test="/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='start']">
                <xsl:value-of select="/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='start']" />
            </xsl:when>
            <xsl:otherwise>0</xsl:otherwise>
        </xsl:choose></xsl:variable>
        <xsl:variable name="numDocs"><xsl:value-of select="/response/lst[@name='grouped']/lst[@name='code']/int[@name='matches']" /></xsl:variable>
        <xsl:variable name="rows"><xsl:value-of select="count(/response/lst[@name='grouped']/lst[@name='code']/arr/lst)" /></xsl:variable>
            <div class="numDocs">
                <xsl:call-template name="title">
                    <xsl:with-param name="numDocs"><xsl:value-of select="$numDocs" /></xsl:with-param>
                </xsl:call-template>
                <xsl:if test="$pagination='1'">
                <xsl:call-template name="pagination">
                    <xsl:with-param name="rows"><xsl:value-of select="$rows" /></xsl:with-param>
                    <xsl:with-param name="start"><xsl:value-of select="$firsthit" /></xsl:with-param>
                    <xsl:with-param name="numDocs"><xsl:value-of select="$numDocs" /></xsl:with-param>
                </xsl:call-template>
                </xsl:if>
            </div>
            
    </xsl:template>
    <xsl:template name="title">
        <xsl:param name="numDocs"/>
        <xsl:variable name="numDocsStr">
            <xsl:choose>
                <xsl:when test="$numDocs = 1"><xsl:value-of select="rb:getString($i18n,'results.documents.singular')"/></xsl:when>
                <xsl:when test="$numDocs &gt; 1 and $numDocs &lt; 5"><xsl:value-of select="rb:getString($i18n,'results.documents.plural_1')"/></xsl:when>
                <xsl:when test="$numDocs &gt; 4"><xsl:value-of select="rb:getString($i18n,'results.documents.plural_2')"/></xsl:when>
            </xsl:choose>
        </xsl:variable>
        <xsl:value-of select="rb:getString($i18n,'results.documents.hits')"/>: <xsl:value-of select="$numDocs" />&#160;<xsl:value-of select="$numDocsStr" />
        <input type="hidden" id="totalHits"><xsl:attribute name="value" ><xsl:value-of select="$numDocs" /></xsl:attribute></input>
    </xsl:template>
</xsl:stylesheet>
