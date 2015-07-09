package com.inflectra.spiratest.plugins.bamboo;

import com.atlassian.bamboo.commit.Commit;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.notification.Notification;
import com.atlassian.bamboo.notification.NotificationTransport;
import com.atlassian.bamboo.plan.cache.ImmutablePlan;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.atlassian.bamboo.template.TemplateRenderer;
import com.atlassian.bamboo.variable.CustomVariableContext;
import com.atlassian.event.Event;
import com.google.common.collect.ImmutableList;

import org.apache.commons.httpclient.HttpClient;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.inflectra.spiratest.plugins.bamboo.SpiraImportExport;

/**
 * This defines the 'SpiraNotificationTransport' class, which contains methods to
 * get build information from Bamboo, and setup the notifications to SpiraTeam
 * 
 * @author		Bruno Gruber - Inflectra Corporation
 * @version		1.0.0 - July 2015
 *
 */

public class SpiraNotificationTransport implements NotificationTransport {

	private static final Logger log = Logger.getLogger(SpiraNotificationTransport.class);
    
    private String URL = null;
    private String user = null;
    private String passw = null;
    private String pvers = null;
    private String pnumber = null;
    private String miliDate = Long.toString(System.currentTimeMillis());
   
    private boolean notify = false;
    private HttpClient client;

    @Nullable
    private ImmutablePlan plan;
    @Nullable
    private ResultsSummary resultsSummary;
    @Nullable
    private DeploymentResult deploymentResult;
    private Event event;    
    private TemplateRenderer templateRenderer;
    
    public SpiraNotificationTransport(String URL,String user, String passw, String pvers,
            						  String pnumber, boolean notify,@Nullable ImmutablePlan plan,
            						  @Nullable ResultsSummary resultsSummary,
            						  @Nullable DeploymentResult deploymentResult,
            						  Event event, CustomVariableContext customVariableContext,  
            						  TemplateRenderer templateRenderer)
    {
        this.URL = customVariableContext.substituteString(URL);
        this.user = customVariableContext.substituteString(user);
        this.passw = customVariableContext.substituteString(passw);
        this.pvers = customVariableContext.substituteString(pvers);
        this.pnumber = customVariableContext.substituteString(pnumber); 
        this.notify = notify;
        this.event = event;
        this.plan = plan;
        this.resultsSummary = resultsSummary;
        this.deploymentResult = deploymentResult;
        client = new HttpClient();      
    }

    @Override
    public void sendNotification(Notification notification){
    	
    	List<Integer> incidentIds = new ArrayList<Integer>();
    	List<String> revisions = new ArrayList<String>();
    	SpiraImportExport spiraClient = new SpiraImportExport();
    	
    	spiraClient.setUrl(URL);	
    	spiraClient.setUserName(user);
    	spiraClient.setPassword(passw);
    	
    	Date date = resultsSummary.getBuildDate();  	
    	String name = resultsSummary.getBuildKey() + " #" + resultsSummary.getBuildNumber();
    	
    	String description = ("Information retrieved from Bamboo: " + "<br/>"
    	                     + resultsSummary.getReasonSummary() + "<br/> of " 
    	                     + resultsSummary.getPlanResultKey() + "<br/> at "
    	                     + date + "<br/> with duration of " + 
    	                     resultsSummary.getDuration() + " miliseconds <br/> " +
    	                     resultsSummary.getCustomBuildData() + "<br/>" +
    	                     resultsSummary.getChangesListSummary());
  
    	ImmutableList<Commit> commits = resultsSummary.getCommits();
    	if (commits != null)
    	{
    		for (Commit commit : commits)
    		{
    			String revisionId = commit.getChangeSetId();
    			revisions.add(revisionId);
    		}    		
    	}
     
    	try {
    		spiraClient.testConnection();
    	} catch (Exception e){
    		log.info(":: SpiraTeam Plugin :: Error connecting to Spira Server. No secondary action needed.");
    		e.printStackTrace();
    	}
    	
    	spiraClient.setProjectId(Integer.parseInt(pnumber));
    	
    	if(resultsSummary.isSuccessful())
    	{
    	try {// 2 = Successful state
			spiraClient.recordBuild(pvers,date,2,name,description,revisions,incidentIds);
		} catch (Exception e) {
			e.printStackTrace();
			log.info(":: SpiraTeam Plugin :: Error recording the Build Information (2) on SpiraTeam."+
					 "Action: connection aborted");
		}
    	}

    	if(resultsSummary.isFailed())
    	{
        	try { // 1 = Failed state
    			spiraClient.recordBuild(pvers,date,1,name,description,revisions,incidentIds);
    		} catch (Exception e) {
    			e.printStackTrace();
    			log.info(":: SpiraTeam Plugin :: Error recording the Build Information (1) on SpiraTeam."+
   					     "Action: connection aborted");
    		}
    	}
    
    	if(resultsSummary.isNotBuilt()){
        	try {// 4 = Aborted state
    			spiraClient.recordBuild(pvers,date,4,name,description,revisions,incidentIds);
    		} catch (Exception e) {
    			e.printStackTrace();
    			log.info(":: SpiraTeam Plugin :: Error recording the Build Information (4) on SpiraTeam."+
  					     "Action: connection aborted");
    		}
    	}
        
        if(resultsSummary.isPending()){
            try { // 5 = Unstable state
        			spiraClient.recordBuild(pvers,date,3,name,description,revisions,incidentIds);
        	} catch (Exception e) {
        		e.printStackTrace();
        		log.info(":: SpiraTeam Plugin :: Error recording the Build Information (5) on SpiraTeam."+
      					     "Action: connection aborted");
        		}
        	}
    }
}
