<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:wxsl1="http://www.w3schools.com/w3style.xsl"
 xmlns:xsd="http://www.w3.org/2001/XMLSchema"
>


<!-- TODO - preserve &#181; etc - may need to do special Xalan trick or XSLT 2.0 character map?? &#181;m &#xB5;m -->
<!-- *********** TODO - fix "identity transform version of objectfactory ******* -->
<!-- *********** TODO -  fix any other complextypes that contain complextypes - not aware of any ******* -->


<!--
 xmlns:blah="http://www.w3.org/1999/XSL/Transform"
 xmlns:wxsl="http://www.w3schools.com/w3style.xsl"
-->  
  <xsl:output method="xml" encoding="utf-8" indent="yes" />
  <!--xsl:namespace-alias stylesheet-prefix="blah" result-prefix="xsl"/ -->
  <xsl:namespace-alias stylesheet-prefix="wxsl1" result-prefix="xsl"/>

 <!-- Ignore annotations -->
  <xsl:template match="xsd:annotation">
  </xsl:template>


<!--****WORKING VERSION*** - DOES REORDERING OF ELEMENTS TO FIT SCHEMA
     Pass one to the called ComplexType template, so does surround it with element tag
     (with name of the type). ObjectFactory name tag just gets copied as special-case.
     Deals with GenericOE as well, since it contains an anonymous ComplexType.
  -->
   <xsl:template match="xsd:element[@name='ObjectFactory']">
    <wxsl1:template match="{@name}" name="{@name}" >
      <wxsl1:copy>
        <xsl:for-each select="./xsd:complexType/xsd:sequence/xsd:element">
         
          <wxsl1:for-each select="{@name}">

            <xsl:choose>
          
                <xsl:when test="@name='name'" >
                  <wxsl1:element name="{@name}"><wxsl1:value-of select="."/></wxsl1:element>
                </xsl:when> 

                <xsl:when test="@name='GenericOE'">
                  <wxsl1:copy>
                    <xsl:for-each select="./xsd:complexType/xsd:sequence/xsd:element">
                      <wxsl1:for-each select="{@name}">
             
                        <xsl:if test="@name='name'" >
                          <wxsl1:element name="{@name}"><wxsl1:value-of select="."/></wxsl1:element>
                        </xsl:if> 
            
                        <xsl:if test="@name!='name'" >
                          <wxsl1:call-template name="{@type}" >
                            <wxsl1:with-param name="doElement" select="1"/>
                          </wxsl1:call-template>
                        </xsl:if>
             
                      </wxsl1:for-each>
                    </xsl:for-each>
                  </wxsl1:copy>
                </xsl:when>                   
              <xsl:otherwise>
 				    <wxsl1:call-template name="{@type}">
				      <wxsl1:with-param name="doElement" select="1"/>
				    </wxsl1:call-template>
				  </xsl:otherwise>  
		    
 				    
            </xsl:choose>
				    
          </wxsl1:for-each>
       
        </xsl:for-each>
   
     </wxsl1:copy>
    </wxsl1:template>
   </xsl:template>
 
 <!-- FOR DEBUG - VERSION - DONT REARRANGE TOPLEVEL ELEMENTS???   -->
 <!--
  <xsl:template match="xsd:element[@name='ObjectFactory']">
    <wxsl1:template match="{@name}" name="{@name}" >
      <wxsl1:copy>
     
       <    <xsl:for-each select="./xsd:complexType/xsd:sequence/xsd:element"> >
     <     <xsl:value-of select="@name"/> >
 
       <        <wxsl1:element name="{@name}"><wxsl1:value-of select="."/></wxsl1:element> >
   <             <xsl:apply-templates select="."/>   
       >
       
          <wxsl1:for-each select="./*">
          <xsl:for-each select="./xsd:complexType/xsd:sequence/xsd:element">
    
            <wxsl1:choose>

              <wxsl1:when test="@name='name'" >
                <wxsl1:element name="{@name}"><wxsl1:value-of select="."/></wxsl1:element>
              </wxsl1:when> 
      <-
              <wxsl1:when test="@name='GenericOE'">
                <wxsl1:copy>
                  foo
                </wxsl1:copy>
              </wxsl1:when>                   
            >
              <wxsl1:when test="@name='{@name}'">
               <wxsl1:call-template name="{@name}">
				      <wxsl1:with-param name="doElement" select="1"/>
				    </wxsl1:call-template>
				  </wxsl1:when>  
				    
            </wxsl1:choose>

          </xsl:for-each>       
          </wxsl1:for-each>       
       
      <  </xsl:for-each> >
 
          
<
        <wxsl1:for-each select="/ObjectFactory/*">


				    <wxsl1:call-template name="//.">
				      <wxsl1:with-param name="doElement" select="1"/>
				    </wxsl1:call-template>
 
				    <xsl:apply-templates select="."/>
     
        </wxsl1:for-each>
>
     </wxsl1:copy>
    </wxsl1:template>
  </xsl:template>
 -->
 
  <!-- match all elements with same name in current contex - to elements with maxOccurs greater than 1 -->
  <!-- N.B.  special cases for various elements with nested complex types. its hacky, but gets the job done.
  hopefully can work out a way of making this generic, so dont have to hard code. ie detect whether an element  is a complextype during the first pass thru the schema. -->
  <xsl:template match="xsd:element">
  
    <wxsl1:for-each select="{@name}">
      <wxsl1:element name="{@name}">
      
        <xsl:choose>
          
          <xsl:when test="@name='Dimension'" >
            <wxsl1:call-template name="{@name}">
              <wxsl1:with-param name="doElement" select="0"/>
            </wxsl1:call-template>
          </xsl:when>
          <xsl:when test="@name='IdentityFunction'" >
            <wxsl1:call-template name="{@name}">
              <wxsl1:with-param name="doElement" select="0"/>
            </wxsl1:call-template>
          </xsl:when>
          <xsl:when test="@name='LinearFunction'" >
            <wxsl1:call-template name="{@name}">
              <wxsl1:with-param name="doElement" select="0"/>
            </wxsl1:call-template>
          </xsl:when>
          <xsl:when test="@name='InterpolationFunction'" >
            <wxsl1:call-template name="{@name}">
              <wxsl1:with-param name="doElement" select="0"/>
            </wxsl1:call-template>
          </xsl:when>
          
          <xsl:when test="@name='mutualPhaseGapPowerMap'" >
            <wxsl1:call-template name="DOFRouteChecker">
              <wxsl1:with-param name="doElement" select="0"/>
            </wxsl1:call-template>
          </xsl:when>
          <xsl:when test="@name='opposingPhaseGapPowerMap'" >
            <wxsl1:call-template name="DOFRouteChecker">
              <wxsl1:with-param name="doElement" select="0"/>
            </wxsl1:call-template>
          </xsl:when>
          <xsl:otherwise>
            <wxsl1:value-of select="."/>
          </xsl:otherwise>  
          
        </xsl:choose>
       
      </wxsl1:element>
    </wxsl1:for-each>
    
  </xsl:template>

  <!-- match type extension - recursively call base type template then process inside.
     Pass zero to call template, so doesnt surround the instance
      with an element tag (with the name of the type).
   -->
  <xsl:template match="xsd:extension">
    <wxsl1:call-template name="{@base}">
      <wxsl1:with-param name="doElement" select="0"/>
    </wxsl1:call-template>
    <xsl:apply-templates/>
  </xsl:template>  

  <!-- match sequence - ignore and process inside -->
  <xsl:template match="xsd:sequence">
    <xsl:apply-templates/>
  </xsl:template>
  
  <!-- match complexContent - ignore and process inside -->
  <xsl:template match=" xsd:complexContent">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- match simpleTypes - ignore and process inside -->
  <xsl:template match="xsd:simpleType">
    <xsl:apply-templates/>
  </xsl:template>
  
  <!-- match anonymous complexTypes - ignore and process inside -->
  <!-- TODO - not sure if predicate is correct, but has desired effect! -->
  <xsl:template match="xsd:complexType[count(.)]">
    <xsl:apply-templates/>
  </xsl:template>
  
  <!-- FIXME - ignore timeType as we dont want it at the moment! -->
  <xsl:template match="xsd:complexType[@name='timeType']" />
 
 
   <!-- match named complexTypes - generate a named template and process inside -->
  <xsl:template match="xsd:complexType">
    <wxsl1:template match="{@name}" name="{@name}" >
    <wxsl1:param name="doElement" select="1" />

      <wxsl1:if test="$doElement = 1" >
        <wxsl1:element name="{@name}">
          <xsl:apply-templates/>
        </wxsl1:element>
      </wxsl1:if>  

      <wxsl1:if test="$doElement = 0">
          <xsl:apply-templates/>
      </wxsl1:if>  

    </wxsl1:template>
  </xsl:template>


  <!-- match document root - create stylesheet template and process inside -->
  <xsl:template match="/">
    <wxsl1:stylesheet version="1.0">
      <wxsl1:output method="xml" encoding="utf-8" indent="yes" />

      <xsl:apply-templates/>
      
    </wxsl1:stylesheet>
  </xsl:template>

</xsl:stylesheet>