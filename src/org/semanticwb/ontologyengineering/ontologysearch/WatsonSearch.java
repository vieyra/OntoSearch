package org.semanticwb.ontologyengineering.ontologysearch;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.rpc.ServiceException;

import uk.ac.open.kmi.watson.clientapi.SearchConf;
import uk.ac.open.kmi.watson.clientapi.SemanticContentResult;
import uk.ac.open.kmi.watson.clientapi.SemanticContentSearch;
import uk.ac.open.kmi.watson.clientapi.SemanticContentSearchServiceLocator;

/**
 * @author Samuel Vieyra 
 * samuel.vieyra@infotec.com.mx
 */
public class WatsonSearch {

   private SemanticContentSearch contentSearcher;
   private SearchConf configuration;
   private List<String[]> retrievedTermResourceDescription;
   
   //Map<URIDocument, CacheDocument>
   HashMap<String, String> cacheDocument = new HashMap<String,String>();
   

   /* Default configurations */
   private int confEntities = SearchConf.CLASS;
   private int confScope = SearchConf.LOCAL_NAME + SearchConf.LABEL;
   private int confMatch = SearchConf.EXACT_MATCH;

   
   public static void main(String[] args) {
      //Set the configurations
      SearchConfiguration conf = new SearchConfiguration();

      //Setting entities
      conf.setClasses(true);
      conf.setProperties(false);
      conf.setIndividuals(false);

      //setting scopes
      conf.setLocalName(true);
      conf.setLabel(true);
      conf.setComment(false);
      
      //Match
      conf.setExactMatch(true);
      
      String[] terms = {"student", "teacher", "learning process", "tutor", "activities", "knowledge", "learning", "process", "concepts", "ideas", "goals", "knowledge object", "peers", "support", "members", "resources", "aim", "group", "learning goals", "personal ability", "strategies", "tasks", "auto-evaluation", "co-evaluation", "cognitive skills", "communicative cognitive conflicts", "ethical skills", "knowledge enabler", "opinions", "relationships"};
      
      WatsonSearch ws = new WatsonSearch(conf);
      ws.searchTerms(terms);
      
      
      
   }
   
   public WatsonSearch(SearchConfiguration conf) {
      retrievedTermResourceDescription = new ArrayList<String[]>();
      SemanticContentSearchServiceLocator locator = new SemanticContentSearchServiceLocator();
      configuration = new SearchConf();
      try {
         contentSearcher = locator.getUrnSemanticContentSearch();
      } catch (ServiceException ex) {
         ex.printStackTrace();
         ex.getCause();
      }
      if(conf!=null)
         setConfiguration(conf);
      else 
         setConfiguration();
   }
   
   public WatsonSearch() {
      this(null);
   }
   
   public List<String[]> searchTerms(String[] terms){
      for (String term : terms) {
         try {
            SemanticContentResult[] results = search(new String[]{term});
//            System.out.println("Term " + term + " has " + results.length + " results");
            for (SemanticContentResult result : results) {
               String cacheLocation = "";
               if(cacheDocument.containsKey(result.getURI())){
                  cacheLocation = cacheDocument.get(result.getURI());
               } else {
                  cacheLocation = contentSearcher.getCacheLocation(result.getURI());
                  cacheDocument.put(result.getURI(), cacheLocation);
               }
               String[] newDescription = new String[]{term, "-", result.getURI(), cacheLocation};
                  if (!previouslyRecovered(newDescription)) {
                     retrievedTermResourceDescription.add(newDescription);
                  } 
            }
         } catch (RemoteException ex) {
            System.err.println(ex.getMessage());
            System.err.println(ex.getCause());
         }
      }
      return retrievedTermResourceDescription;
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

   private void setConfiguration(SearchConfiguration conf) {
      confEntities = 0 + (conf.isClasses()?SearchConf.CLASS:0) + (conf.isProperties()?SearchConf.PROPERTY:0) + (conf.isIndividuals()?SearchConf.INDIVIDUAL:0);
      confScope = 0 + (conf.isLocalName()?SearchConf.LOCAL_NAME:0) + (conf.isLabel()?SearchConf.LABEL:0) + (conf.isComment()?SearchConf.COMMENT:0);
      confMatch = 0 + (conf.isExactMatch()?SearchConf.EXACT_MATCH:SearchConf.TOKEN_MATCH);
      configuration.setEntities(confEntities);
      configuration.setScope(confScope);
      configuration.setMatch(confMatch);
   }
   
   private void setConfiguration() {
      configuration.setEntities(confEntities);
      configuration.setScope(confScope);
      configuration.setMatch(confMatch);
   }

   public int getConfEntities() {
      return confEntities;
   }

   public void setConfEntities(int confEntities) {
      this.confEntities = confEntities;
      setConfiguration();
   }

   public int getConfScope() {
      return confScope;
   }

   public void setConfScope(int confScope) {
      this.confScope = confScope;
      setConfiguration();
   }

   public int getConfMatch() {
      return confMatch;
   }

   public void setConfMatch(int confMatch) {
      this.confMatch = confMatch;
      setConfiguration();
   }

   public SemanticContentSearch getContentSearcher() {
      return contentSearcher;
   }

   public SemanticContentResult[] search(String[] params) throws RemoteException {
      return contentSearcher.getSemanticContentByKeywords(params, configuration);
   }

   public SemanticContentResult[] searchClasses(String[] params) throws RemoteException {
      confEntities = SearchConf.CLASS;
      setConfiguration();
      return contentSearcher.getSemanticContentByKeywords(params,configuration);
   }

   public SemanticContentResult[] searchIndividuals(String[] params) throws RemoteException {
      confEntities = SearchConf.INDIVIDUAL;
      setConfiguration();
      return contentSearcher.getSemanticContentByKeywords(params,configuration);
   }

   public String cacheLocation(String uri) throws RemoteException {
      return contentSearcher.getCacheLocation(uri);
   }
   
}
