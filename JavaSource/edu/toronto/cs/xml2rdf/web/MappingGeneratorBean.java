/*
 *    Copyright (c) 2013, University of Toronto.
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 */
package edu.toronto.cs.xml2rdf.web;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import sun.awt.image.URLImageSource;

import edu.toronto.cs.xml2rdf.analysis.SchemaGraph;
import edu.toronto.cs.xml2rdf.mapping.Mapping;
import edu.toronto.cs.xml2rdf.mapping.generator.DummyMappingGenerator;
import edu.toronto.cs.xml2rdf.mapping.generator.DummySimilarityMetric;
import edu.toronto.cs.xml2rdf.string.NoWSCaseInsensitiveStringMetric;
import edu.toronto.cs.xml2rdf.web.conf.Configuration;
import edu.toronto.cs.xml2rdf.xml.XMLUtils;

/**
 * Created by JBoss Tools
 */
@ManagedBean(name="mapgen")
@SessionScoped
public class MappingGeneratorBean {
  private String url;
  private String mappingContent;
  private String convertedContent;
  private double ontologyMatchingThreshold;
  private double schemaSimilarityThreshold;
  private int sampleSize;
  String outputFormat;
  String typePrefix;
  private int minimumElementRequiredForLeafPromotion;
  private double ontologyPruningThreshold;
  private double internalLinkingThreshold;
  
  public MappingGeneratorBean() {
  }

  public String getUrl() {
    return url;
  }
  
  public void setUrl(String url) {
    this.url = url;
  }
  
  public String getMappingContent() {
    return mappingContent;
  }
  
  public void setMappingContent(String mappingContent) {
    this.mappingContent = mappingContent;
  }
  
  public String getConvertedContent() {
    return convertedContent;
  }
  
  public void setConvertedContent(String convertedContent) {
    this.convertedContent = convertedContent;
  }
  
  public double getOntologyMatchingThreshold() {
    return ontologyMatchingThreshold;
  }
  
  public void setOntologyMatchingThreshold(double ontologyMatchingThreshold) {
    this.ontologyMatchingThreshold = ontologyMatchingThreshold;
  }
  
  public double getSchemaSimilarityThreshold() {
    return schemaSimilarityThreshold;
  }
  
  public void setSchemaSimilarityThreshold(double schemaSimilarityThreshold) {
    this.schemaSimilarityThreshold = schemaSimilarityThreshold;
  }
  
  public String getOutputFormat() {
    return outputFormat;
  }
  
  public void setOutputFormat(String outputFormat) {
    this.outputFormat = outputFormat;
  }
  
  public String getTypePrefix() {
    return typePrefix;
  }
  
  public void setTypePrefix(String typePrefix) {
    this.typePrefix = typePrefix;
  }
  
  public int getSampleSize() {
    return sampleSize;
  }
  
  public void setSampleSize(int sampleSize) {
    this.sampleSize = sampleSize;
  }
  
  public int getMinimumElementRequiredForLeafPromotion() {
    return minimumElementRequiredForLeafPromotion;
  }
  
  public void setMinimumElementRequiredForLeafPromotion(
      int minimumElementRequiredForLeafPromotion) {
    this.minimumElementRequiredForLeafPromotion = minimumElementRequiredForLeafPromotion;
  }
  
  public double getOntologyPruningThreshold() {
    return ontologyPruningThreshold;
  }
  
  public void setOntologyPruningThreshold(double ontologyPruningThreshold) {
    this.ontologyPruningThreshold = ontologyPruningThreshold;
  }
  
  public double getInternalLinkingThreshold() {
    return internalLinkingThreshold;
  }
  
  public void setInternalLinkingThreshold(double internalLinkingThreshold) {
    this.internalLinkingThreshold = internalLinkingThreshold;
  }
  
  public void mapURL() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
    
    String[] blacklist = {
        "http://rdf.freebase.com/rdf/m.04mp1fp",
        "http://rdf.freebase.com/rdf/location.dated_location",
        "http://rdf.freebase.com/rdf/location.statistical_region",
        "http://rdf.freebase.com/rdf/location.administrative_division",
        "http://rdf.freebase.com/rdf/music.artist",
        "http://rdf.freebase.com/rdf/base.whoami.answer",
        "http://rdf.freebase.com/rdf/base.legislation.vote_value",
        "http://rdf.freebase.com/rdf/m.04lqt84",
        "http://rdf.freebase.com/rdf/base.umltools.design_pattern",
        "http://rdf.freebase.com/rdf/base.braziliangovt.brazilian_governmental_vote_type",
        "http://sw.opencyc.org/concept/Mx4rJ3ZbguI8QdeGDNhCi9LL3Q",
        "http://rdf.freebase.com/rdf/base.whoami.answer",
        "http://sw.opencyc.org/concept/Mx4rvUCoPtoTQdaZVdw2OtjsAg",
        "http://sw.opencyc.org/concept/Mx4rveI9NpwpEbGdrcN5Y29ycA",
        "http://rdf.freebase.com/rdf/m.04mp1fp",
        "http://sw.opencyc.org/concept/Mx4rIGTaIPAIQdaffLzGWDo0Zw",
        "http://rdf.freebase.com/rdf/m.04lqt84",
        "http://sw.opencyc.org/concept/Mx4rvVj8VZwpEbGdrcN5Y29ycA",
        "http://rdf.freebase.com/rdf/military.military_combatant",
        "http://rdf.freebase.com/rdf/book.book_subject",
        "http://rdf.freebase.com/rdf/sports.sports_team_location",
        "http://rdf.freebase.com/rdf/user.tsegaran.random.taxonomy_subject",
        "http://rdf.freebase.com/rdf/food.beer_country_region",
        "http://rdf.freebase.com/rdf/user.skud.flags.flag_having_thing",
        "http://rdf.freebase.com/rdf/m.04l1354",
        "http://rdf.freebase.com/rdf/olympics.olympic_participating_country",
        "http://rdf.freebase.com/rdf/organization.organization_member",
        "http://rdf.freebase.com/rdf/biology.breed_origin",
        "http://rdf.freebase.com/rdf/user.robert.military.military_power",
        "http://rdf.freebase.com/rdf/base.ontologies.ontology_instance",
        "http://rdf.freebase.com/rdf/government.governmental_jurisdiction"
    };

    
    Mapping mapping = new Mapping(new StringReader(mappingContent), new HashSet<String>(Arrays.asList(blacklist)));
    Document dataDoc = XMLUtils.parse((InputStream) new URL(url).getContent(), 5);
    
    ByteArrayOutputStream bao = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(bao);

    mapping.generateRDFSchema(Configuration.getTDBPath(), 
        dataDoc, typePrefix, ps, outputFormat,
        new NoWSCaseInsensitiveStringMetric(), ontologyMatchingThreshold);

    mapping.generateRDFs(
        Configuration.getTDBPath(),
        dataDoc, typePrefix, ps, outputFormat,
        new NoWSCaseInsensitiveStringMetric(), ontologyMatchingThreshold);
    
    convertedContent = new String(bao.toByteArray());
  }
  
  public void generateMapping() throws MalformedURLException, IOException, SAXException, ParserConfigurationException {
    if (url == null || url.length() == 0) {
      return;
    }
    
    InputStream is = (InputStream) new URL(url).getContent();
    Document rootDoc = XMLUtils.parse(is, -1);
    
    Document doc = new DummyMappingGenerator(ontologyMatchingThreshold, 
        new NoWSCaseInsensitiveStringMetric(), 
        schemaSimilarityThreshold, new   DummySimilarityMetric(),
        minimumElementRequiredForLeafPromotion, ontologyPruningThreshold, 
        sampleSize, sampleSize, .25, 2, internalLinkingThreshold).generateMapping(rootDoc.getDocumentElement(), typePrefix);
    
    OutputFormat format = new OutputFormat(doc);
        format.setLineWidth(65);
        format.setIndenting(true);
        format.setIndent(2);
        
        StringWriter stringWriter = new StringWriter();
    XMLSerializer serializer = new XMLSerializer (
        stringWriter, format);
    serializer.asDOMSerializer();
    serializer.serialize(doc);
    
    mappingContent = stringWriter.getBuffer().toString();
    stringWriter.close();
  }
  
  final static String linkedCTSampleURI = "/clinicaltrials/mapping/linkedct.500.xml";
  public void fillInClinicalTrialSampleData() {
    BufferedReader reader = new BufferedReader(new InputStreamReader(MappingGeneratorBean.class.getResourceAsStream(linkedCTSampleURI)));
    String data = "";
    String line = null;
    try {
      while ((line = reader.readLine()) != null) {
        data += line + "\n";
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    ontologyMatchingThreshold = 1;
    schemaSimilarityThreshold = 0.95;
    minimumElementRequiredForLeafPromotion = 4;
    ontologyPruningThreshold = 0.75;
    sampleSize = 1000;
    internalLinkingThreshold = .8;
    
    typePrefix = "http://www.linkedct.org/0.1#";
    url = Configuration.getDomain() + "/xcurator/data/ct.xml";
    
    mappingContent = data;
  }
  
  public String getInstanceURI() {
    return typePrefix == null ? "" : "/xcurator/rdfview/" + typePrefix.replace("#", "%23") + "instanceBag"; 
  }
  
  public String getClassURI() {
    return typePrefix == null ? "" : "/xcurator/rdfview/" + typePrefix.replace("#", "%23") + "classBag"; 
  }

  
  public String getForcedJS() {
    
    
    StringWriter jsWriter = new StringWriter();
    SchemaGraph graph = null;
    try {
      graph = new SchemaGraph(new StringReader(mappingContent), typePrefix);
      graph.generateProtoVis(new PrintWriter(jsWriter));
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
    return jsWriter.getBuffer().toString() + ";";

  }
}
