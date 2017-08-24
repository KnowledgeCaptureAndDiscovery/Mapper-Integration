package edu.isi.wings.opmm.Validations;



import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import edu.isi.wings.opmm.Validations.Queries;

import javax.xml.bind.DatatypeConverter;

//import org.apache.commons.lang3.time.DateUtils;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import edu.isi.wings.opmm.Validations.*;

public class Scenario3 {

	public static String validateRepo(OntModel m,String templateName,String newTemplateName,String timegiven)
	{
		newTemplateName=newTemplateName.toUpperCase();
		templateName=templateName.toUpperCase();
		 String result = "##########REPORT##########\n";    
		 StringBuilder ans=new StringBuilder("");
		    int n=0;
		    System.out.println("VALIDATION TESTS FOR SCENARIO-1 (NUMBER OF EXECUTIONS, EXPANDED TEMPLATES AND ABSTRACT TEMPLATES)");
		    //TEST 1: DO WE HAVE ANY SIMILAR NAMED TEMPLATES?
		      result+="#TEST"+(++n)+":DO WE HAVE ANY SIMILAR NAMED TEMPLATES?\n";
		      HashSet<String> hs1names=new HashSet<String>();
		      HashSet<String> hs2times=new HashSet<String>();
		      
		      hs1names=Utils.queryresult112(edu.isi.wings.opmm.Validations.Queries.DO_WE_HAVE_ANY_SIMILAR_NAMED_TEMPLATES, m, "t");
		      System.out.println();
		      Date latesttobelinked=null;
		      String templateTobeLinked=null;
		      String ansfinal=null;
		      for(String x:hs1names)
		    	  System.out.println(x);
		      boolean secondpart=true;
		      if(hs1names.size()==0)
		    	  return "repository does not have a match";
		      
		      for(String x:hs1names)
		      {
		    	  System.out.println("x "+x);
		    	  System.out.println("given template "+newTemplateName);
		    	  if(x.equals(newTemplateName))
		    		  secondpart=false;
		      }
		      
		      if(secondpart==true)
		      {
		      for(String x:hs1names){
		    	  System.out.println("template name "+templateName);
		    	 System.out.println("xsubstring "+x.substring(0, x.lastIndexOf("_")));
		    		
		    	  if(!x.equals(newTemplateName) && templateName.equals(x.substring(0, x.lastIndexOf("_"))))
		    	  {
		    		  System.out.println("given newTemplate "+newTemplateName+" current x is: "+x);
		    		  System.out.println("Template names are same "+x);
		    		  //since the template names match now find the timing of each of these types of templates
		    		  Literal timefromquery=Utils.queryresult113(Queries.TIME_CHECK(x), m, "time");
		    		  System.out.println("time from query "+timefromquery.getString());
		    		  try{
		    			  Calendar c1 = DatatypeConverter.parseDateTime(timegiven);
		    			    Date d1 = c1.getTime();
		    			  Calendar c2 = DatatypeConverter.parseDateTime(timefromquery.getString());
		    			    Date d2 = c2.getTime();  
		    			    System.out.println("parsed date GIVEN "+d1);    
		    		  System.out.println("parsed date QUERY "+d2);
		    		  if(d1.after(d2))
		    		  {
		    			  System.out.println("THE GIVEN TIME IS AFTER QUERY TIME");
		    			  if(latesttobelinked==null){
		    				  System.out.println("im here");
		    				  latesttobelinked=d2;
		    				  templateTobeLinked=x;
		    			  }
		    			  else if(latesttobelinked!=null && d2.after(latesttobelinked))
		    			  {
		    				  latesttobelinked=d2;
		    				  templateTobeLinked=x;
		    			  }
		    		  }

		    		  }catch(Exception e)
		    		  {
		    			  System.out.println("error");
		    		  }
		    		  

		    		  System.out.println("time given "+timegiven);
		    	  }

		      }
		      }

		      if(latesttobelinked!=null && templateTobeLinked!=null)
		      {
		    	  System.out.println("We are returning the ans "+ansfinal);
		    	  ansfinal=templateTobeLinked;
		      }
		      
		      
		      result+="\n\n";   
		    
		 return ansfinal;   
		    
	}
}
