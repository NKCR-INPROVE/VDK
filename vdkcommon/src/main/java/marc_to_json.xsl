<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="plain"/>
    
    <xsl:template match="/">
        {"listRecords:[
        <xsl:for-each select="oai:ListRecords/oai:record">
            "identifier": <xsl:value-of select="./oai:header/oai:identifier/text()" />
        </xsl:for-each>
        ]
        }
    </xsl:template>

</xsl:stylesheet>
