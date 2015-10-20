<?xml version="1.0" encoding="UTF-8"?><xsl:stylesheet xmlns:marc="http://www.loc.gov/MARC21/slim" 
                xmlns:oai="http://www.openarchives.org/OAI/2.0/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>
    <xsl:param name="id" select="id" />
    <xsl:template match="/">
        <xsl:copy-of select="//oai:record[./oai:header/oai:identifier=$id]" /> 
    </xsl:template>
</xsl:stylesheet>
