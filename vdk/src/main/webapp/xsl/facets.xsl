<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:java="http://xml.apache.org/xalan/java"
  xmlns:rb="cz.incad.xsl.ResourceBundleService"
  exclude-result-prefixes="java rb"
  version="1.0">
    <xsl:output method="html"/>
    <xsl:param name="locale" select="locale" />
    <xsl:variable name="i18n" select="rb:getBundle('labels', $locale)"/>
    <xsl:param name="url" select="url" />
    <xsl:param name="q" select="q" />
    <xsl:param name="nav" select="nav" />
    <xsl:param name="numInitial" select="25" />
    <xsl:template match="/">
        <xsl:for-each select="//lst[@name='facet_counts']/lst[@name='facet_fields']/lst" >
            <h3>
                <span><xsl:value-of select="rb:getString($i18n,concat('filter.',./@name))"/></span>
            </h3>
            <div>
            <xsl:if test="./@name='pocet_exemplaru'">
                <div id="pocet_select" style="text-align:center;margin-bottom:16px;">
                    <span class="label">od <xsl:value-of select="./int[position()=1]/@name" /> - do <xsl:value-of select="./int[position()=last()]/@name" /></span>
                    <span class="go" style="float:right;">go</span>
                </div>
                <div id="pocet_range" style="margin:0px 10px;">
                    <xsl:attribute name="data-min"><xsl:value-of select="./int[position()=1]/@name" /></xsl:attribute>
                    <xsl:attribute name="data-max"><xsl:value-of select="./int[position()=last()]/@name" /></xsl:attribute>
                </div>
                <div style="height:3px;border:none;border-top:solid 1px;margin-top:7px;">
                </div>
            </xsl:if>  
            <xsl:if test="./@name='rokvydani'">
                <div id="rokvydani_select" style="text-align:center;margin-bottom:16px;">
                    <span class="label">od <xsl:value-of select="./int[position()=1]/@name" /> - do <xsl:value-of select="./int[position()=last()]/@name" /></span>
                    <span class="go" style="float:right;">go</span>
                </div>
                <div id="rokvydani_range" style="margin:0px 10px;">
                    <xsl:attribute name="data-min"><xsl:value-of select="./int[position()=1]/@name" /></xsl:attribute>
                    <xsl:attribute name="data-max"><xsl:value-of select="./int[position()=last()]/@name" /></xsl:attribute>
                </div>
                <div style="height:3px;border:none;border-top:solid 1px;margin-top:7px;">
                </div>
            </xsl:if>    
            <xsl:choose>
                <xsl:when test="./@name='zdroj'">
                    <xsl:call-template name="zdrojnav">
                        <xsl:with-param name="navName"  select="./@name" />
                        <xsl:with-param name="content"  select="." />
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="./@name='nabidka'">
                    <xsl:call-template name="nabidka">
                        <xsl:with-param name="navName"  select="./@name" />
                        <xsl:with-param name="content"  select="." />
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="./@name='poptavka'">
                    <xsl:call-template name="poptavka">
                        <xsl:with-param name="navName"  select="./@name" />
                        <xsl:with-param name="content"  select="." />
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="./@name='rokvydani'">
                    
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="facet">
                        <xsl:with-param name="navName"  select="./@name" />
                        <xsl:with-param name="content"  select="." />
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>
            </div>
        </xsl:for-each>
        <xsl:call-template name="rokvydani"></xsl:call-template>
    </xsl:template>
    
    
    <xsl:template name="poptavka">
        <xsl:param name="navName" />
        <xsl:param name="content" />
        <ul id="nav_poptavka">
            <li><a>
            <xsl:attribute name="href">javascript:filterDemands();</xsl:attribute>
                :: <xsl:value-of select="rb:getString($i18n,'poptavka.all','all')" />
            </a></li>
            <!--
            <xsl:for-each select="$content/int" >
                <xsl:variable name="bundle_name"><xsl:value-of select="$navName" />.<xsl:value-of select="./@name" /></xsl:variable>
                    <li class="offer">
                        <xsl:attribute name="data-offer" ><xsl:value-of select="./@name" /></xsl:attribute>
                        <xsl:if test="position() &gt; $numInitial">
                            <xsl:attribute name="class">more</xsl:attribute>
                        </xsl:if><a>
                        <xsl:attribute name="href">javascript:addNav('<xsl:value-of select="$navName" />:"<xsl:value-of select="./@name" />"');</xsl:attribute>
                            :: <xsl:value-of select="rb:getString($i18n,$navName,./@name)" />
                        </a>
                        <span style="float:right;">(<xsl:value-of select="." />)</span>
                    </li>
            </xsl:for-each>
            <xsl:if test="count($content/int) &gt; $numInitial"><li><a>
                <xsl:attribute name="href">javascript:toggleNav('<xsl:value-of select="$navName" />');</xsl:attribute>...</a></li>
            </xsl:if>
            -->
        </ul>
    </xsl:template>
    <xsl:template name="nabidka">
        <xsl:param name="navName" />
        <xsl:param name="content" />
        <ul id="nav_nabidka">
            <li><a>
            <xsl:attribute name="href">javascript:filterOffers();</xsl:attribute>
                :: <xsl:value-of select="rb:getString($i18n,'nabidka.all','all')" />
            </a></li>
            
            <xsl:for-each select="$content/int" >
                <xsl:variable name="bundle_name"><xsl:value-of select="$navName" />.<xsl:value-of select="./@name" /></xsl:variable>
                    <li class="offer">
                        <xsl:attribute name="data-offer" ><xsl:value-of select="./@name" /></xsl:attribute>
                        <xsl:if test="position() &gt; $numInitial">
                            <xsl:attribute name="class">more</xsl:attribute>
                        </xsl:if><a>
                        <xsl:attribute name="href">javascript:filterOffer(<xsl:value-of select="./@name" />);</xsl:attribute>
                            :: <xsl:value-of select="rb:getString($i18n,$navName,./@name)" />
                        </a>
                        <span style="float:right;">(<xsl:value-of select="." />)</span>
                    </li>
            </xsl:for-each>
            <xsl:if test="count($content/int) &gt; $numInitial"><li><a>
                <xsl:attribute name="href">javascript:toggleNav('<xsl:value-of select="$navName" />');</xsl:attribute>...</a></li>
            </xsl:if>
            
        </ul>
    </xsl:template>
    <xsl:template name="facet">
        <xsl:param name="navName" />
        <xsl:param name="content" />
        <ul>
            <xsl:attribute name="id">nav_<xsl:value-of select="$navName" /></xsl:attribute>
            <xsl:for-each select="$content/int" >
                <xsl:variable name="bundle_name"><xsl:value-of select="$navName" />.<xsl:value-of select="./@name" /></xsl:variable>
                    <li>
                        <xsl:variable name="fqStr"><xsl:value-of select="$navName" />:"<xsl:value-of select="./@name" />"</xsl:variable>
                        <xsl:if test="position() &gt; $numInitial">
                            <xsl:attribute name="class">more</xsl:attribute>
                        </xsl:if><xsl:choose>
                        <xsl:when test="/response/lst[@name='responseHeader']/lst[@name='params']/arr[@name='fq'][str=$fqStr]">
                            <span class="selected">:: <xsl:value-of select="rb:getString($i18n,$navName,./@name)" />
                            </span>
                        </xsl:when>
                        <xsl:otherwise><a>
                        <xsl:attribute name="href">javascript:addNav('<xsl:value-of select="$navName" />:"<xsl:value-of select="./@name" />"');</xsl:attribute>
                            :: <xsl:value-of select="rb:getString($i18n,$navName,./@name)" />
                        </a></xsl:otherwise>
                        </xsl:choose>
                        
                        <span style="float:right;">(<xsl:value-of select="." />)</span>
                    </li>
            </xsl:for-each>
            <xsl:if test="count($content/int) &gt; $numInitial"><li><a>
                <xsl:attribute name="href">javascript:toggleNav('<xsl:value-of select="$navName" />');</xsl:attribute>...</a></li>
            </xsl:if>
        </ul>
    </xsl:template>
    
    <xsl:template name="zdrojnav">
        <xsl:param name="navName" />
        <xsl:param name="content" />
        <ul>
            <xsl:attribute name="id">nav_<xsl:value-of select="$navName" /></xsl:attribute>
            <xsl:for-each select="$content/int" >
                <xsl:variable name="fqval">{!tag=dt}zdroj:<xsl:value-of select="./@name" /></xsl:variable>
                <xsl:variable name="bundle_name"><xsl:value-of select="$navName" />.<xsl:value-of select="./@name" /></xsl:variable>
                    <li>
                        <xsl:if test="position() &gt; $numInitial">
                            <xsl:attribute name="class">more</xsl:attribute>
                        </xsl:if>
                        <!--
                        <input type="checkbox" >
                            <xsl:attribute name="onclick">zdrojNav('<xsl:value-of select="./@name" />', this)</xsl:attribute>
                            <xsl:if test="/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='fq'][.=$fqval]">
                                <xsl:attribute name="checked">checked</xsl:attribute>
                            </xsl:if>
                            <xsl:if test="/response/lst[@name='responseHeader']/lst[@name='params']/arr[@name='fq'][./str=$fqval]">
                                <xsl:attribute name="checked">checked</xsl:attribute>
                            </xsl:if>
                        </input>
                        -->
                        <input class="chci" type="checkbox" >
                            <xsl:attribute name="onclick">zdrojNav('<xsl:value-of select="./@name" />', this)</xsl:attribute>
                            <xsl:attribute name="id">zdrojNav_<xsl:value-of select="./@name" />_chci</xsl:attribute>
                            <xsl:attribute name="name">zdrojNav_<xsl:value-of select="./@name" /></xsl:attribute>
                        </input>
                        <label>
                            <xsl:attribute name="for">zdrojNav_<xsl:value-of select="./@name" />_chci</xsl:attribute>
                        include</label>
                        <input class="nechci" type="checkbox" >
                            <xsl:attribute name="onclick">zdrojNav('-<xsl:value-of select="./@name" />', this)</xsl:attribute>
                            <xsl:attribute name="id">zdrojNav_<xsl:value-of select="./@name" />_nechci</xsl:attribute>
                            <xsl:attribute name="name">zdrojNav_<xsl:value-of select="./@name" /></xsl:attribute>
                        </input>
                        <label>
                            <xsl:attribute name="for">zdrojNav_<xsl:value-of select="./@name" />_nechci</xsl:attribute>
                        exclude</label>
                        <span>
                        <xsl:value-of select="rb:getString($i18n,$navName,./@name)" />
                        </span>
                        <span style="float:right;">(<xsl:value-of select="." />)</span>
                    </li>
            </xsl:for-each>
            <xsl:if test="count($content/int) &gt; $numInitial"><li><a>
                <xsl:attribute name="href">javascript:toggleNav('<xsl:value-of select="$navName" />');</xsl:attribute>...</a></li>
            </xsl:if>
        </ul>
        
    </xsl:template>
    <xsl:template name="rokvydani">
        <h3>
            <span><xsl:value-of select="rb:getString($i18n,'filter.rokvydani')"/></span>
        </h3>
        <xsl:variable name="od" select="round(/response/lst[@name='stats']/lst[@name='stats_fields']/lst[@name='rokvydani']/double[@name='min'])"/>
        <xsl:variable name="do" select="round(/response/lst[@name='stats']/lst[@name='stats_fields']/lst[@name='rokvydani']/double[@name='max'])"/>
        <div>
            <div id="rokvydani_select" style="text-align:center;margin-bottom:16px;">
                <span class="label">
                    od <xsl:value-of select="$od" /> - 
                    do <xsl:value-of select="$do" />
                </span>
                <span class="go" style="float:right;">go</span>
            </div>
            <div id="rokvydani_range" style="margin:0px 10px;">
                <xsl:attribute name="data-min"><xsl:value-of select="$od" /></xsl:attribute>
                <xsl:attribute name="data-max"><xsl:value-of select="$do" /></xsl:attribute>
            </div>
            <div style="height:3px;border:none;border-top:solid 1px;margin-top:7px;">
            </div>
        </div>
    </xsl:template>
</xsl:stylesheet>