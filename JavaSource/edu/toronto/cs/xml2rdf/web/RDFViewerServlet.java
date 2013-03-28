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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.toronto.cs.xml2rdf.jena.JenaUtils;
import edu.toronto.cs.xml2rdf.web.conf.Configuration;

/**
 * Servlet implementation class RDFViewerServlet
 */
public class RDFViewerServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RDFViewerServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    private String urlify(String url) {
      if (url.startsWith("http:")) {
        if (!url.contains("freebase") && !url.contains("opencyc")) {
          return "<a href=\"/xcurator/rdfview/" + url.replace("#", "%23") + "\">" + url + "</a>";
        } else {
          return "<a href=\"" + url + "\" target=\"blank\">" + url + "</a>";
        }
      }
      return url;
    }
    
  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/html; charset=UTF-8");

    String resourceURL = request.getParameter("url");
    if (resourceURL == null || resourceURL.length() == 0) {
      return;
    }
    
    String fromString = request.getParameter("from");
    int from = 0;
    if (fromString != null) {
      from = Math.max(from, Integer.valueOf(fromString));
    }
    
    int size = 50;
    String sizeString = request.getParameter("size");
    if (sizeString != null) {
      size = Math.min(size, Integer.valueOf(sizeString));
    }
    
    int to = from + size;
    
    String format = request.getParameter("format");
    if (format == null) {
      format = "N-TRIPLE";
    }

    ServletOutputStream out = response.getOutputStream();
    
    out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" +
      "<html xmlns=\"http://www.w3.org/1999/xhtml\">" + 
        "<head>" + 
      "<link  href=\"http://fonts.googleapis.com/css?family=Lato:100,100italic,300,300italic,400,400italic,700,700italic,900,900italic\" rel=\"stylesheet\" type=\"text/css\" />" +
      "<link  href=\"http://fonts.googleapis.com/css?family=Neuton:regular,italic\" rel=\"stylesheet\" type=\"text/css\" />" +
        "<link href=\"/xcurator/style/xcurator.css\" rel=\"stylesheet\" type=\"text/css\">" +
        "</head><body>");
    
    out.println("<span style=\"height: 5em;\" class=\"header\">xCurator Triple Viewer</span>");

    out.println("<table>");
    
    Model model = JenaUtils.getTDBModel(Configuration.getTDBPath());
    Resource resource = model.getResource(resourceURL);
    Property property = model.getProperty(resourceURL);
    out.println("<tr><td height=\"100px\" colspan=2> URI: " + resource.getURI() + "<a href=\"/xcurator/rdfdata/" + from + "/" + size + "/" + resourceURL.replace("#", "%23") + "\" > View Pure RDF Data </a>" + "</td></tr>");
    out.println("</table>");
    out.println("<table>");
    int current = 0;
    
    if (resource != null) {
      ExtendedIterator<Statement> iter = model.listStatements(resource, (Property) null, (RDFNode) null);
      
      for (; current < from && iter.hasNext(); current++) {
        iter.next();
      }
      
      if (current < to && iter.hasNext()) {
        out.println("<tr><td><span class=\"subheader\">As Subject:</span></td></tr>");
        out.println("<tr><td>Predicate</td><td></td><td>Object</td></tr>");
      }
      
      for (; current < to && iter.hasNext(); current++) {
        Statement stmt = iter.next();
        out.println("<tr><td>" + urlify(stmt.getPredicate().getURI()) + "</td><td>&rarr;</td>" 
            + "<td>" + urlify(stmt.getObject().toString()) + "</td></tr>");
      }
    }
    
    
    if (resource != null) {
      ExtendedIterator<Statement> iter = model.listStatements(null, null, resource);
      
      for (; current < from && iter.hasNext(); current++) {
        iter.next();
      }
      
      if (current < to && iter.hasNext()) {
        out.println("<tr><td><span class=\"subheader\">As Object:</span></td></tr>");
        out.println("<tr><td>Subject</td><td></td><td>Predicate</td></tr>");
      }
      
      for (; current < to && iter.hasNext(); current++) {
        Statement stmt = iter.next();
        out.println("<tr><td>" + urlify(stmt.getSubject().getURI()) + "</td><td>&rarr;</td>" 
            + "<td>" + urlify(stmt.getPredicate().getURI()) + "</td></tr>");
      }
    }

    
    
    if (resource != null) {
      ExtendedIterator<Statement> iter = model.listStatements(null, property, (RDFNode) null);

      for (; current < from && iter.hasNext(); current++) {
        iter.next();
      }
      
      if (current < to && iter.hasNext()) {
        out.println("<tr><td><span class=\"subheader\">As Predicate:</span></td></tr>");
        out.println("<tr><td>Subject</td><td></td><td>Object</td></tr>");
      }
      
      for (; current < to && iter.hasNext(); current++) {
        Statement stmt = iter.next();
        out.println("<tr><td>" + urlify(stmt.getSubject().getURI()) + "</td><td>&rarr;</td>"  
            + "<td>" + urlify(stmt.getObject().toString()) + "</td></tr>");
      }
    }
    model.close();
    
    out.println("<tr><td>");
    out.println("</td></tr>");
    out.println("<tr><td>");
    out.println(getPrevURL(resourceURL, from, size));
    out.println("</td><td></td><td>");
    if (current >= to) {
      out.println(getNextURL(resourceURL, from, size));
    }
    out.println("</td></tr>");
    out.println("</table>");
    out.println("</bdoy></html>");

  }

  private String getPrevURL(String resourceURL, int from, int size) {
    if (from == 0) {
      return "";
    }
    return "<a href=\"/xcurator/rdfview/" + Math.max(0, from - size) + "/" + size + "/" + resourceURL.replace("#", "%23") +"\">Previous</a>";
  }

  private String getNextURL(String resourceURL, int from, int size) {
    return "<a href=\"/xcurator/rdfview/" + (from + size) + "/" + size + "/" + resourceURL.replace("#", "%23") +"\">Next</a>";
  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

}
