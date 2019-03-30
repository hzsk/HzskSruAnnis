<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:ed="http://clarin.eu/fcs/endpoint-description"
  xmlns:cmd="http://www.clarin.eu/cmd/"
  version="2.0" default-mode="xml">

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <xsl:template match="cmd:CMD">
    <xsl:apply-templates select="cmd:Components"/>
  </xsl:template>

  <xsl:template match="cmd:ResourceProxy">
    <xsl:if test="cmd:ResourceType = 'LandingPage'">
      <ed:LandingPageURI>
        <xsl:apply-templates select="cmd:ResourceRef"/>
      </ed:LandingPageURI>
    </xsl:if>
  </xsl:template>

  <xsl:template match="cmd:Components">
    <ed:Resources>
      <xsl:apply-templates/>
    </ed:Resources>
  </xsl:template>

  <xsl:template match="cmd:TextCorpusProfile|cmd:SpokenCorpusProfile">
    <ed:Resource>
      <xsl:attribute name="pid">
        <xsl:value-of select="cmd:GeneralInfo/cmd:PID"/>
      </xsl:attribute>
      <xsl:apply-templates/>
    </ed:Resource>
  </xsl:template>

  <xsl:template match="cmd:Title">
    <ed:Title>
      <xsl:attribute name="xml:lang">
        <xsl:value-of select="@xml:lang"/>
      </xsl:attribute>
      <xsl:apply-templates/>
    </ed:Title>
  </xsl:template>

  <xsl:template match="cmd:Description">
    <ed:Description>
      <xsl:attribute name="xml:lang">
        <xsl:value-of select="@xml:lang"/>
      </xsl:attribute>
      <xsl:apply-templates/>
    </ed:Description>
  </xsl:template>

  <xsl:template match="cmd:SubjectLanguages">
    <ed:Languages>
      <xsl:apply-templates/>
    </ed:Languages>
  </xsl:template>

  <xsl:template match="cmd:Language">
    <ed:Language>
      <xsl:apply-templates select="cmd:ISO639/cmd:iso-639-3-code"/>
    </ed:Language>
  </xsl:template>

  <xsl:template match="cmd:AnnotationTypes">
    <!-- hard-coced, to fix after copy/paste-->
    <ed:AvailableDataViews ref="hits_dataview adv_dataview"/>
    <ed:AvailableLayers>
      <xsl:attribute name="ref">
        <xsl:apply-templates/>
      </xsl:attribute>
    </ed:AvailableLayers>
  </xsl:template>

  <xsl:template match="cmd:AnnotationType">
    <xsl:choose>
      <xsl:when test="cmd:AnnotationLevelType='Part-of-speech'">
        layer_pos
      </xsl:when>
      <xsl:when test="cmd:AnnotationLevelType='Lemma'">
        layer_lemma
      </xsl:when>
      <xsl:when test="cmd:AnnotationLevelType='Morphology'">
        layer_msd
      </xsl:when>
      <xsl:otherwise>
        layer_unk_<xsl:value-of select="cmd:AnnotationLevelType"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ignore for now -->
  <xsl:template match="cmd:Name|cmd:ResourceClass|cmd:PublicationDate|cmd:LifeCycleStatus|cmd:LegalOwner|cmd:Version|cmd:Contact|cmd:License|cmd:Project|cmd:Creators|cmd:Contributors|cmd:CorpusContext|cmd:Coverage|cmd:Content|cmd:Size|cmd:Documentation|cmd:BibliographicCitations|cmd:PID|cmd:Availability|cmd:Multilinguality|cmd:SegmentationUnits|cmd:Keyword|cmd:Validation|cmd:TextCorpusInfo">
    <!-- emptiness -->
  </xsl:template>

</xsl:stylesheet>
