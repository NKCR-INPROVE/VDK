<xsl:stylesheet xmlns:marc="http://www.loc.gov/MARC21/slim" 
                xmlns:oai="http://www.openarchives.org/OAI/2.0/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		 xmlns:exts="java://cz.incad.xml.XSLFunctions" 
                version="1.1" exclude-result-prefixes="marc oai java">
    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>
    <xsl:param name="file" select="file" />
    <xsl:variable name="xslfunctions" select="exts:new()" />
    <xsl:variable name="zdroj"><xsl:choose>
      <xsl:when test="starts-with(//oai:ListRecords/oai:record/oai:header/oai:setSpec, 'MZK')">MZK</xsl:when>
      <xsl:otherwise><xsl:value-of select="//oai:ListRecords/oai:record/oai:header/oai:setSpec" /></xsl:otherwise>
    </xsl:choose></xsl:variable>
    <xsl:template match="/">
        <xsl:variable name="request" select="//oai:OAI-PMH/oai:request" />
        <add>
            <xsl:for-each select="//marc:record" >
                <xsl:variable name="identifier" select="../../oai:header/oai:identifier" />
                <xsl:variable name="title" select="concat(marc:datafield[@tag=245]/marc:subfield[@code='a'],marc:datafield[@tag=245]/marc:subfield[@code='b'])"/>
                <xsl:variable name="autor" select="marc:datafield[@tag=700]/marc:subfield[@code='a']"/>
                <xsl:variable name="mistovydani" select="marc:datafield[@tag=260]/marc:subfield[@code='a']"/>
                <xsl:variable name="datumvydani" select="marc:datafield[@tag=260]/marc:subfield[@code='c']"/>
                <xsl:variable name="md5" select="exts:generateNormalizedMD5($xslfunctions, concat($title, $autor, $mistovydani, $datumvydani))"/>
                <doc>
                
		    <field column="id" xpath="/OAI-PMH/ListRecords/record/header/identifier" />
		    <field column="code" xpath="/OAI-PMH/ListRecords/record/aa" />
		    <field column="code_type" xpath="/OAI-PMH/ListRecords/record/md5" />
		    <field column="zdroj" xpath="/OAI-PMH/ListRecords/record/knihovna" />
		    <field column="title" xpath="/OAI-PMH/ListRecords/record/metadata/record/datafield[@tag='245']/subfield[@code='a']" />
		    <field column="ccnb" xpath="/OAI-PMH/ListRecords/record/metadata/record/datafield[@tag='015']/subfield[@code='a']" />
		    <field column="isbn" xpath="/OAI-PMH/ListRecords/record/metadata/record/datafield[@tag='020']/subfield[@code='a']" />
		    <field column="issn" xpath="/OAI-PMH/ListRecords/record/metadata/record/datafield[@tag='022']/subfield[@code='a']" />
		    <field column="xml" xpath="/OAI-PMH/ListRecords/record" flatten="true" />
                    
                    <field name="id">
                        <xsl:value-of select="../../oai:header/oai:identifier"/>
                    </field>
                    <field name="title">
                        <xsl:value-of select="../../oai:header/oai:identifier"/>
                    </field>
		    
                </doc>
            </xsl:for-each>
        </add>
    </xsl:template>
    
    