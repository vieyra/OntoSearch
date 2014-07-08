package org.semanticwb.LinkedData.sparql;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.semanticwb.LinkedData.Resources.Prefix.PrefixDeclaration;


/**
 * @author Samuel Vieyra
 * samuel.vieyra@infotec.com.mx
 */

public class SPARQLQuery {
    
    public static void main1(String[] args) throws IOException {
//        SPARQLQuery sparqlService = new SPARQLQuery("http://lod2.openlinksw.com/sparql");
//        List<String> superClasses = sparqlService.listAllSuperClasses("http://yago-knowledge.org/resource/wordnet_request_107185325", new ArrayList<String>());
        
        
      
        SPARQLQuery sparqlService = new SPARQLQuery("http://lov.okfn.org/endpoint/lov_aggregator");
        ResultSet response = sparqlService.executeSelect(""
               + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
               + "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n" + "\n"
               + "SELECT ?class ?label\n"
               + "WHERE{\n"
               + "  ?class a owl:Class .\n"
               + "  ?class rdfs:label ?label .\n"
               + "  FILTER(REGEX(STR(?label),\"^student$\",\"i\"))\n"
               + "}");
        ResultSetFormatter.out(System.out, response);
        
////        System.out.println(prepareStringLiteral("Baja California "));
//        String[] municipalities = new String[]{
//            "Retrieve","user data","Capture","application","Validate"
//                ,"request"
//            ,"authorization","Use","third party services","Capture","Notify","reason","request rejected","Generate","format","meet service delivery","delivery","Notify","application rejection","Receive","application form","Service location","location","Assignment area"
//            "Rejection","Message","applicant","application date","Approver person"
//                ,"Request","Requester","Counter"
//        };
//        for (int i = 2500; i < municipalities.length; i += 20) {
//            System.out.println("\t\t//Iteration " + i / 20 + "\\");
//            List<String[]> results = sparqlService.searchConceptByExactMatch(municipalities);
//            for (String[] strings : results) {
//                for (String string : strings) {
//                    System.out.print(string + "\t");
//                }
//                System.out.println("");
//             }
//            ResultSetFormatter.out(System.out, results);
        
        
//        }
        
    }

    private String sparqlService = "http://factforge.net/sparql";
    private int offset = 10;
    
    public SPARQLQuery() throws IOException {
//        listInstances();
//        describeInstance("<http://www.w3.org/2002/07/owl#sameAs>");
    }

    public SPARQLQuery(String sparqlService) throws IOException {
        this.sparqlService = sparqlService;
    }

    public SPARQLQuery(String sparqlEPURL, int offset) throws IOException {
        this.sparqlService = sparqlEPURL;
        this.offset = offset;
    }

    
    public ResultSet executeSelect(String sparqlStatement){
        QueryExecution x = QueryExecutionFactory.sparqlService(sparqlService, sparqlStatement);
        return x.execSelect();
    }
    
    public Model executeDescribe(String uriResource){
        
        return null;
    }
    
    public boolean executeAsk(String sparqlStatement){
        QueryExecution x = QueryExecutionFactory.sparqlService(sparqlService, sparqlStatement);
        return x.execAsk();
    }
    
    public ResultSet searchInstanceByExactMatch(String stringLiteral){
        String sparqlStatement = ""
                + PrefixDeclaration.RDFS
                + "\n"
                + "SELECT DISTINCT ?instance ?label ?type\n"
                + "WHERE {\n"
                + "  ?instance a ?type .\n"
                + "  ?instance rdfs:label ?label .\n"
                + "  FILTER("
                    + "REGEX(STR(?label),\"^"+ prepareStringLiteral(stringLiteral) +"$\")"
                + "  ) ."
                + "}";
        QueryExecution x = QueryExecutionFactory.sparqlService(sparqlService, sparqlStatement);
        return x.execSelect();
    }
    
    public List<String[]> searchInstanceByExactMatch(String[] stringLiterals){
        String sparqlStatement = ""
                + PrefixDeclaration.RDFS
                + "\n"
                + "SELECT DISTINCT ?instance ?label ?type\n"
                + "WHERE {\n"
                + "  ?instance a ?type .\n"
                + "  ?instance rdfs:label ?label .\n"
//                + "  FILTER langMatches( lang(?label), \"ES\" ) .\n"
                + "  FILTER(REGEX(STR(?label),\"";
        for(int i = 0; i < stringLiterals.length; i++){
            String stringLiteral = stringLiterals[i];
            sparqlStatement+= "^"+ stringLiteral.trim().replaceAll("[ ]+", ".?") +"$"
                    + ( (i < stringLiterals.length-1) ?"|":"");
        }
        sparqlStatement+= "\",\"i\")) .\n"
                + "}";
        System.out.println(sparqlStatement);
        QueryExecution x = QueryExecutionFactory.sparqlService(sparqlService, sparqlStatement);
        ResultSet results = x.execSelect();
        List<String[]> resultsArray = new ArrayList<String[]>();
        while(results.hasNext()){
            QuerySolution qs = results.next();
            String[] array = new String[3];
            array[0] = qs.get("instance").asNode().getURI();
            array[1] = qs.getLiteral("label").getString();
            array[2] = qs.get("type").asNode().getURI();
            resultsArray.add(array);
        }
        return resultsArray;
    }
   
    public List<String[]> searchConceptByExactMatch(String[] stringLiterals){
        String sparqlStatement = ""
                + PrefixDeclaration.RDFS
                + "\n"
                + "SELECT DISTINCT ?concept ?label ?comment\n"
                + "WHERE {\n"
                + "  ?concept rdfs:label ?label .\n"
                + "  FILTER(REGEX(STR(?label),\"";
        for(int i = 0; i < stringLiterals.length; i++){
            String stringLiteral = stringLiterals[i];
            sparqlStatement+= "^"+ stringLiteral.trim().replaceAll("[ ]+", ".?") +"$"
                    + ( (i < stringLiterals.length-1) ?"|":"");
        }
        sparqlStatement+= "\",\"i\")) .\n"
                + "  OPTIONAL{\n" 
                + "    ?concept rdfs:comment ?comment .\n"
                + "  }"
                + "}";
        System.out.println(sparqlStatement);
        QueryExecution x = QueryExecutionFactory.sparqlService(sparqlService, sparqlStatement);
        ResultSet results = x.execSelect();
        List<String[]> resultsArray = new ArrayList<String[]>();
        while(results.hasNext()){
            QuerySolution qs = results.next();
            String[] array = new String[3];
            array[0] = qs.get("instance").asNode().getURI();
            array[1] = qs.getLiteral("label").getString();
            array[2] = qs.get("type").asNode().getURI();
            resultsArray.add(array);
        }
        return resultsArray;
    }
    
    public ResultSet searchInstanceByTypeWithExactMatch(String[] stringLiterals, String typeURI){
        
        String sparqlStatement = ""
                + PrefixDeclaration.RDFS
                + "\n"
                + "SELECT DISTINCT ?instance ?label\n"
                + "WHERE {\n"
                + "  ?instance a <" + typeURI +"> .\n"
                + "  ?instance rdfs:label ?label .\n"
                + "  FILTER langMatches( lang(?label), \"ES\" ) .\n"
                + "  FILTER(REGEX(STR(?label),\"";
        for(int i = 0; i < stringLiterals.length; i++){
            String stringLiteral = stringLiterals[i];
            sparqlStatement+= "^"+ stringLiteral.trim().replaceAll("[ ]+", ".?") +"$"
                    + ( (i < stringLiterals.length-1) ?"|":"");
        }
        sparqlStatement+= "\",\"i\")) .\n"
                + "}";
        System.out.println(sparqlStatement);
        QueryExecution x = QueryExecutionFactory.sparqlService(sparqlService, sparqlStatement);
        return x.execSelect();
    }
    
    
    public ResultSet searchInstance(String stringLiteral){
        String sparqlStatement = ""
                + PrefixDeclaration.RDFS
                + "\n"
                + "SELECT DISTINCT ?instance ?label\n"
                + "WHERE {\n"
                + "  ?instance a ?Concept .\n"
                + "  ?instance rdfs:label ?label .\n"
                + "  FILTER(REGEX(STR(?label),\"^"+ stringLiteral.trim().replaceAll("[ ]+", ".?") +"$\",\"i\"))"
                + "}";
        QueryExecution x = QueryExecutionFactory.sparqlService(sparqlService, sparqlStatement);
        return x.execSelect();
    }
    
    private static String prepareStringLiteral(String stringLiteral){
        stringLiteral = stringLiteral.trim().replaceAll("[ ]+", ".?");
        Matcher m = Pattern.compile("[A-Z]").matcher(stringLiteral);

        StringBuilder sb = new StringBuilder();
        int last = 0;
        while (m.find()) {
            sb.append(stringLiteral.substring(last, m.start()));
            sb.append("[" + m.group(0) + m.group(0).toLowerCase() + "]");
            last = m.end();
        }
        sb.append(stringLiteral.substring(last));
        return sb.toString();
//        return stringLiteral.replaceAll("[A-Z]", "[" + ("\1").toLowerCase() + "\1]");
    }
    
    public List<String> listAllSuperClasses(String uri, List<String> superClasses) throws IOException{
        String sparqlStatement = ""
                + PrefixDeclaration.RDFS
                + "\n"
                + "SELECT DISTINCT ?Concept\n"
                + "WHERE {\n"
                + "  <"+ uri + "> rdfs:subClassOf ?Concept .\n"
                + "}";
//        System.out.println(sparqlStatement);
        ResultSet sparqlResponse = executeSelect(sparqlStatement);
        System.out.println(sparqlResponse.getRowNumber());
        while(sparqlResponse.hasNext()){
            QuerySolution qs = sparqlResponse.next();
            String superClass = qs.get("Concept").asNode().getURI();
            if(!superClasses.contains(superClass)){
                System.out.println("<" + superClass + ">");
                superClasses.add(superClass);
                listAllSuperClasses(superClass, superClasses);
            }
        }
        return superClasses;
    }
    
    private Model describeInstance(String uri) throws IOException{
        String sparqlQuery =
                "DESCRIBE "+ uri;

        QueryExecution x = QueryExecutionFactory.sparqlService(sparqlService, sparqlQuery);
        return x.execDescribe();
//        results.write(new FileWriter(new File("model.n3")),"N3");
//        ResultSetFormatter.out(System.out, results);
        
//        return new String[]{};
    }
    
}

/*
 * ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 33033 column 93): {E211} Base URI is null, but there are relative URIs to resolve.: <http://dbpedia.org/property/../tv/wayside/waysideEn.xml&xvar>
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 1 column 7): {W104} Unqualified typed nodes are not allowed. Type treated as a relative URI.
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 1 column 7): {W136} Relative URIs are not permitted in RDF: specifically <html>
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 2 column 7): {W104} Unqualified property elements are not allowed. Treated as a relative URI.
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 2 column 7): {W136} Relative URIs are not permitted in RDF: specifically <head>
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 3 column 8): {W104} Unqualified typed nodes are not allowed. Type treated as a relative URI.
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 3 column 8): {W136} Relative URIs are not permitted in RDF: specifically <title>
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 3 column 43): {E202} Expecting XML start or end element(s). String data "The page is temporarily unavailable" not allowed. Maybe there should be an rdf:parseType='Literal' for embedding mixed XML content in RDF. Maybe a striping error.
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 4 column 8): {E201} Multiple children of property element
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 4 column 8): {W104} Unqualified typed nodes are not allowed. Type treated as a relative URI.
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 4 column 8): {W136} Relative URIs are not permitted in RDF: specifically <style>
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 5 column 58): {E202} Expecting XML start or end element(s). String data "
body { font-family: Tahoma, Verdana, Arial, sans-serif; }" not allowed. Maybe there should be an rdf:parseType='Literal' for embedding mixed XML content in RDF. Maybe a striping error.
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 8 column 36): {W104} Unqualified property elements are not allowed. Treated as a relative URI.
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 8 column 36): {W136} Relative URIs are not permitted in RDF: specifically <body>
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 8 column 36): {W136} Relative URIs are not permitted in RDF: specifically <bgcolor>
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 8 column 36): {W102} Unqualified property attributes are not allowed. Property treated as a relative URI.
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 8 column 36): {W136} Relative URIs are not permitted in RDF: specifically <text>
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 8 column 36): {W102} Unqualified property attributes are not allowed. Property treated as a relative URI.
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 9 column 1): {E201} The attributes on this property element, are not permitted with any content; expecting end element tag.
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 9 column 35): {E201} XML element <table> inside an empty property element, whose attributes prohibit any content.
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 10 column 1): {E201} The attributes on this property element, are not permitted with any content; expecting end element tag.
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 10 column 5): {E201} XML element <tr> inside an empty property element, whose attributes prohibit any content.
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 11 column 1): {E201} The attributes on this property element, are not permitted with any content; expecting end element tag.
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 11 column 36): {E201} XML element <td> inside an empty property element, whose attributes prohibit any content.
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 12 column 68): {E201} The attributes on this property element, are not permitted with any content; expecting end element tag.
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 12 column 73): {E201} XML element <br> inside an empty property element, whose attributes prohibit any content.
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 13 column 93): {E202} Expecting XML start or end element(s). String data "
Please try again later. Also you could report problem to infrastructure at ontotext dot com." not allowed. Maybe a striping error.
Exception in thread "main" java.lang.NullPointerException
	at com.hp.hpl.jena.rdf.arp.impl.XMLHandler.endElement(XMLHandler.java:149)
	at org.apache.xerces.parsers.AbstractSAXParser.endElement(Unknown Source)
	at org.apache.xerces.impl.XMLNamespaceBinder.handleEndElement(Unknown Source)
	at org.apache.xerces.impl.XMLNamespaceBinder.endElement(Unknown Source)
	at org.apache.xerces.impl.XMLDocumentFragmentScannerImpl.scanEndElement(Unknown Source)
	at org.apache.xerces.impl.XMLDocumentFragmentScannerImpl$FragmentContentDispatcher.dispatch(Unknown Source)
	at org.apache.xerces.impl.XMLDocumentFragmentScannerImpl.scanDocument(Unknown Source)
	at org.apache.xerces.parsers.DTDConfiguration.parse(Unknown Source)
	at org.apache.xerces.parsers.DTDConfiguration.parse(Unknown Source)
	at org.apache.xerces.parsers.XMLParser.parse(Unknown Source)
	at org.apache.xerces.parsers.AbstractSAXParser.parse(Unknown Source)
	at com.hp.hpl.jena.rdf.arp.impl.RDFXMLParser.parse(RDFXMLParser.java:107)
	at com.hp.hpl.jena.rdf.arp.JenaReader.read(JenaReader.java:158)
	at com.hp.hpl.jena.rdf.arp.JenaReader.read(JenaReader.java:145)
	at com.hp.hpl.jena.rdf.arp.JenaReader.read(JenaReader.java:215)
	at com.hp.hpl.jena.rdf.model.impl.ModelCom.read(ModelCom.java:191)
	at com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP.execModel(QueryEngineHTTP.java:135)
	at com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP.execDescribe(QueryEngineHTTP.java:128)
	at com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP.execDescribe(QueryEngineHTTP.java:126)
	at org.semanticwb.EndPointDataDump.RDFDataDump.describeInstance(RDFDataDump.java:80)
	at org.semanticwb.EndPointDataDump.RDFDataDump.listInstances(RDFDataDump.java:65)
	at org.semanticwb.EndPointDataDump.RDFDataDump.<init>(RDFDataDump.java:28)
	at org.semanticwb.EndPointDataDump.RDFDataDump.main(RDFDataDump.java:21)
Java Result: 1
 */

/*
 * 		///Iteration 2339\\\
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 1 column 7): {W104} Unqualified typed nodes are not allowed. Type treated as a relative URI.
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 1 column 7): {W136} Relative URIs are not permitted in RDF: specifically <html>
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 2 column 7): {W104} Unqualified property elements are not allowed. Treated as a relative URI.
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 2 column 7): {W136} Relative URIs are not permitted in RDF: specifically <head>
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 3 column 8): {W104} Unqualified typed nodes are not allowed. Type treated as a relative URI.
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 3 column 8): {W136} Relative URIs are not permitted in RDF: specifically <title>
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 3 column 43): {E202} Expecting XML start or end element(s). String data "The page is temporarily unavailable" not allowed. Maybe there should be an rdf:parseType='Literal' for embedding mixed XML content in RDF. Maybe a striping error.
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 4 column 8): {E201} Multiple children of property element
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 4 column 8): {W104} Unqualified typed nodes are not allowed. Type treated as a relative URI.
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 4 column 8): {W136} Relative URIs are not permitted in RDF: specifically <style>
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 5 column 58): {E202} Expecting XML start or end element(s). String data "
body { font-family: Tahoma, Verdana, Arial, sans-serif; }" not allowed. Maybe there should be an rdf:parseType='Literal' for embedding mixed XML content in RDF. Maybe a striping error.
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 8 column 36): {W104} Unqualified property elements are not allowed. Treated as a relative URI.
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 8 column 36): {W136} Relative URIs are not permitted in RDF: specifically <body>
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 8 column 36): {W136} Relative URIs are not permitted in RDF: specifically <bgcolor>
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 8 column 36): {W102} Unqualified property attributes are not allowed. Property treated as a relative URI.
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 8 column 36): {W136} Relative URIs are not permitted in RDF: specifically <text>
 WARN [main] (RDFDefaultErrorHandler.java:36) - (line 8 column 36): {W102} Unqualified property attributes are not allowed. Property treated as a relative URI.
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 9 column 1): {E201} The attributes on this property element, are not permitted with any content; expecting end element tag.
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 9 column 35): {E201} XML element <table> inside an empty property element, whose attributes prohibit any content.
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 10 column 1): {E201} The attributes on this property element, are not permitted with any content; expecting end element tag.
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 10 column 5): {E201} XML element <tr> inside an empty property element, whose attributes prohibit any content.
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 11 column 1): {E201} The attributes on this property element, are not permitted with any content; expecting end element tag.
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 11 column 36): {E201} XML element <td> inside an empty property element, whose attributes prohibit any content.
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 12 column 68): {E201} The attributes on this property element, are not permitted with any content; expecting end element tag.
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 12 column 73): {E201} XML element <br> inside an empty property element, whose attributes prohibit any content.
ERROR [main] (RDFDefaultErrorHandler.java:40) - (line 13 column 93): {E202} Expecting XML start or end element(s). String data "
Please try again later. Also you could report problem to infrastructure at ontotext dot com." not allowed. Maybe a striping error.
Exception in thread "main" java.lang.NullPointerException
	at com.hp.hpl.jena.rdf.arp.impl.XMLHandler.endElement(XMLHandler.java:149)
	at org.apache.xerces.parsers.AbstractSAXParser.endElement(Unknown Source)
	at org.apache.xerces.impl.XMLNamespaceBinder.handleEndElement(Unknown Source)
	at org.apache.xerces.impl.XMLNamespaceBinder.endElement(Unknown Source)
	at org.apache.xerces.impl.XMLDocumentFragmentScannerImpl.scanEndElement(Unknown Source)
	at org.apache.xerces.impl.XMLDocumentFragmentScannerImpl$FragmentContentDispatcher.dispatch(Unknown Source)
	at org.apache.xerces.impl.XMLDocumentFragmentScannerImpl.scanDocument(Unknown Source)
	at org.apache.xerces.parsers.DTDConfiguration.parse(Unknown Source)
	at org.apache.xerces.parsers.DTDConfiguration.parse(Unknown Source)
	at org.apache.xerces.parsers.XMLParser.parse(Unknown Source)
	at org.apache.xerces.parsers.AbstractSAXParser.parse(Unknown Source)
	at com.hp.hpl.jena.rdf.arp.impl.RDFXMLParser.parse(RDFXMLParser.java:107)
	at com.hp.hpl.jena.rdf.arp.JenaReader.read(JenaReader.java:158)
	at com.hp.hpl.jena.rdf.arp.JenaReader.read(JenaReader.java:145)
	at com.hp.hpl.jena.rdf.arp.JenaReader.read(JenaReader.java:215)
	at com.hp.hpl.jena.rdf.model.impl.ModelCom.read(ModelCom.java:191)
	at com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP.execModel(QueryEngineHTTP.java:135)
	at com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP.execDescribe(QueryEngineHTTP.java:128)
	at com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP.execDescribe(QueryEngineHTTP.java:126)
	at org.semanticwb.EndPointDataDump.RDFDataDump.describeInstance(RDFDataDump.java:80)
	at org.semanticwb.EndPointDataDump.RDFDataDump.listInstances(RDFDataDump.java:65)
	at org.semanticwb.EndPointDataDump.RDFDataDump.<init>(RDFDataDump.java:28)
	at org.semanticwb.EndPointDataDump.RDFDataDump.main(RDFDataDump.java:21)
Java Result: 1

 */