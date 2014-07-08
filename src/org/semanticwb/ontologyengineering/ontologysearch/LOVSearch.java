package org.semanticwb.ontologyengineering.ontologysearch;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.semanticwb.LinkedData.sparql.SPARQLQuery;
import org.semanticwb.LinkedData.Resources.Prefix.PrefixDeclaration;

/**
 * @author Samuel Vieyra
 * samuel.vieyra@infotec.com.mx
 */
public class LOVSearch {
   
   private SearchConfiguration configuration;
   
   //Map < Resource_uri, vocabulary_uri>
   private List<String[]> retrievedTermResourceDescription;
   private HashMap<String,String> uriNamespaceVocab;
   

   private static String LOV_AGREGATOR_ENDPOINT_URI = "http://lov.okfn.org/endpoint/lov_aggregator";
   private static String LOV_ENDPOINT_URI = "http://lov.okfn.org/endpoint/lov";
   
   private SPARQLQuery lovAgregatorService;
   private SPARQLQuery lovEndpointService;
   
   public LOVSearch() throws IOException {
      this(new SearchConfiguration());
   }
   
   public LOVSearch(SearchConfiguration configuration) throws IOException {
      this.configuration = configuration;
      lovAgregatorService = new SPARQLQuery(LOV_AGREGATOR_ENDPOINT_URI);
      lovEndpointService = new SPARQLQuery(LOV_ENDPOINT_URI);
   }
   
   public List<String[]> searchEntities(String[] terms){
      uriNamespaceVocab = new HashMap<String, String>();
      retrievedTermResourceDescription = new ArrayList<String[]>();
      for (String term : terms) {
         searchEntity(term);
      }
      return retrievedTermResourceDescription;
   }
   
   public void searchEntity(String term){
      String sparqlStatement = buildSparqlStatement(term);
      ResultSet results = lovAgregatorService.executeSelect(sparqlStatement);
      //Map <NameSpace, VocabUri>
      while(results.hasNext()){
         QuerySolution qs =  results.next();
         Resource resource = qs.getResource("resource");
         String nameSpace = resource.getNameSpace();
         if(uriNamespaceVocab.containsKey(nameSpace)){
            String vocabUri = uriNamespaceVocab.get(nameSpace);
            String[] newDescription = new String[]{term, resource.getURI(), vocabUri,""};
            if (!previouslyRecovered(newDescription)) {
               retrievedTermResourceDescription.add(newDescription);
            }
         } else {
            String vocabUri = getVocabForNameSpace(nameSpace);
            if (!vocabUri.equals("")) {
               uriNamespaceVocab.put(nameSpace, vocabUri);
               String[] newDescription = new String[]{term, resource.getURI(), vocabUri,""};
               if (!previouslyRecovered(newDescription)) {
                  retrievedTermResourceDescription.add(newDescription);
               }
            } else {
               String tmpNS = nameSpace.substring(0, nameSpace.length() - 1);
               tmpNS = tmpNS.substring(0, tmpNS.lastIndexOf("/"));
               vocabUri = getVocabForNameSpace(tmpNS);
               if (!vocabUri.equals("")) {
                  uriNamespaceVocab.put(nameSpace, vocabUri);
                  String[] newDescription = new String[]{term, resource.getURI(), vocabUri,""};
                  if (!previouslyRecovered(newDescription)) {
                     retrievedTermResourceDescription.add(newDescription);
                  }
               }
            }
         }
      }
   }
   
   private boolean previouslyRecovered(String[] resourceDescription){
      for (String[] resource : retrievedTermResourceDescription) {
         if( resource[0] == resourceDescription[0] &&
         resource[1] == resourceDescription[1] &&
         resource[2] == resourceDescription[2]){
            return true;
         }
      }
      return false;
   }
   
   private String getVocabForNameSpace(String nameSpace){
      String sparqlStatement = ""
              + "PREFIX voaf:<http://purl.org/vocommons/voaf#>\n"
              + "\n"
              + "SELECT ?vocabURI \n"
              + "WHERE{\n"
              + "	?vocabURI a voaf:Vocabulary.\n"
              + "	FILTER(REGEX(STR(?vocabURI),\""+ nameSpace.substring(0, nameSpace.length() - 1).replace(".", "\\\\.") +"\",\"i\"))\n"
              + "}";
      
      ResultSet results = lovEndpointService.executeSelect(sparqlStatement);
      Resource resource = null;
      if(results.hasNext()){
         resource = results.next().getResource("vocabURI");
      }
      return (resource != null)? resource.getURI() : "";
   }
   
   private String buildSparqlStatement(String term){
      String sparqlStatement = ""
               + PrefixDeclaration.RDFS + "\n"
               + PrefixDeclaration.OWL + "\n" + "\n"
               + "SELECT ?resource \n"
               + "WHERE{\n";
      
      // is it needed use union of entities?
      boolean entityUnion = (configuration.isClasses() && configuration.isProperties()) ||
              (configuration.isClasses() && configuration.isIndividuals()) ||
              (configuration.isIndividuals()&& configuration.isProperties()) ;
      
      // ?resource a [owl:Class | owl:ObjectProperty | @instance]
      if(configuration.isClasses())
         sparqlStatement += "  " 
                 + (entityUnion ? "{ " : "")
                 + "?resource a owl:Class ."
                 + (entityUnion ? " }" : "")
                 + "\n"
                 + (configuration.isProperties()|| configuration.isIndividuals()? "  UNION \n":"\n");
      
      if(configuration.isProperties())
         sparqlStatement += "  " 
                 + (entityUnion ? "{ " : "")
                 + "?resource a owl:ObjectProperty ."
                 + (entityUnion ? " }" : "")
                 + "\n"
                 + (configuration.isIndividuals()? "  UNION \n":"\n");
      
      if(configuration.isIndividuals())
         sparqlStatement += "  " 
                 + (entityUnion ? "{ " : "")
                 + "?resource a ?class .\n"
                 + "  ?class a owl:Class."
                 + (entityUnion?" }":"")
                 + "\n";
      
      // filters
      // is it needed use union of entities?
      boolean filterUnion = (configuration.isLocalName()&& configuration.isLabel()) ||
              (configuration.isLocalName()&& configuration.isComment()) ||
              (configuration.isLabel()&& configuration.isComment()) ;
      
      if(configuration.isLocalName())
         sparqlStatement += "" 
                 + (filterUnion ? "  { " : "")
                 + "  FILTER(REGEX(STR(?resource),\"\\\\.+[/#]"+ term.replaceAll("[ -]", ".?") +"$\",\"i\"))"
                 + (filterUnion?" . }":"")
                 + "\n"
                 + (configuration.isLabel() || configuration.isComment()? "  UNION \n":"\n");
      
      if(configuration.isLabel())
         sparqlStatement += "" 
                 + (filterUnion ? "  { " : "")
                 + "  ?resource rdfs:label ?label . \n"
                 + "  FILTER(REGEX(STR(?label),\""
                 + (configuration.isExactMatch()?"^":"") + term.replaceAll("[ -]", ".?")
                 + (configuration.isExactMatch()?"$":"") + "\",\"i\"))"
                 + (filterUnion?" . }":"") + "\n"
                 + (configuration.isComment()? "  UNION \n":"\n");
      
      if(configuration.isComment())
         sparqlStatement += "" 
                 + (filterUnion ? "  { " : "")
                 + "  ?resource rdfs:comment ?comment . \n"
                 + "  FILTER(REGEX(STR(?comment),\""
                 + (configuration.isExactMatch()?"^":"") + term.replaceAll("[ -]", ".?")
                 + (configuration.isExactMatch()?"$":"")+"\",\"i\"))"
                 + (filterUnion?" . }":"") + "\n"
                 + (configuration.isComment()? "  UNION \n":"\n");
      
      return sparqlStatement +"\n}";
   }

   public SearchConfiguration getConfiguration() {
      return configuration;
   }

   public List<String[]> getRetrievedTermResourceDescription() {
      return retrievedTermResourceDescription;
   }
   
}
