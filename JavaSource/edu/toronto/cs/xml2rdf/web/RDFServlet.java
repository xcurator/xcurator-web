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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.toronto.cs.xml2rdf.jena.JenaUtils;
import edu.toronto.cs.xml2rdf.web.conf.Configuration;

/**
 * Servlet implementation class RDFServlet
 */
public class RDFServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
       
    public RDFServlet() {
        super();
    }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/plain; charset=UTF-8");

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

    int current = 0;
    
    String format = request.getParameter("format");
    if (format == null) {
      format = "N-TRIPLE";
    }
    
    Model model = JenaUtils.getTDBModel(Configuration.getTDBPath());
    Resource resource = model.getResource(resourceURL);
    Property property = model.getProperty(resourceURL);

    if (resource != null) {
      Model outputModel = ModelFactory.createDefaultModel();
      
      ExtendedIterator<Statement> iter = model.listStatements(resource, (Property) null, (RDFNode) null);
      for (; current < from && iter.hasNext(); current++) {
        iter.next();
      }
      
      for (; current < to && iter.hasNext(); current++) {
        Statement stmt = iter.next();
        outputModel.add(stmt);
      }

      iter = model.listStatements(null, null, resource);
      
      for (; current < from && iter.hasNext(); current++) {
        iter.next();
      }
      
      for (; current < to && iter.hasNext(); current++) {
        Statement stmt = iter.next();
        outputModel.add(stmt);
      }

      iter = model.listStatements(null, property, (RDFNode) null);

      for (; current < from && iter.hasNext(); current++) {
        iter.next();
      }
      
      for (; current < to && iter.hasNext(); current++) {
        Statement stmt = iter.next();
        outputModel.add(stmt);
      }
      
      outputModel.write(response.getOutputStream(), format);
      outputModel.close();
    }
    model.close();
  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);  
  }

}
