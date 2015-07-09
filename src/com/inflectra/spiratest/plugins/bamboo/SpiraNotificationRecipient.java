package com.inflectra.spiratest.plugins.bamboo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.atlassian.bamboo.deployments.notification.DeploymentResultAwareNotificationRecipient;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.notification.NotificationRecipient;
import com.atlassian.bamboo.notification.NotificationTransport;
import com.atlassian.bamboo.notification.recipients.AbstractNotificationRecipient;
import com.atlassian.bamboo.notification.recipients.UserRecipient;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.cache.ImmutablePlan;
import com.atlassian.bamboo.plugin.descriptor.NotificationRecipientModuleDescriptor;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.atlassian.bamboo.template.TemplateRenderer;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.variable.CustomVariableContext;
import com.atlassian.event.Event;
import com.atlassian.plugin.web.descriptors.WeightedDescriptor;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.lang3.StringUtils;

/**
 * This defines the 'SpiraNotificationRecipient' class, which contains methods to
 * receive information from user to appropriately setup a notification
 * 
 * @author		Bruno Gruber - Inflectra Corporation
 * @version		1.0.0 - July 2015
 *
 */

public class SpiraNotificationRecipient extends AbstractNotificationRecipient implements DeploymentResultAwareNotificationRecipient,
                                                                                           NotificationRecipient.RequiresPlan,
                                                                                           NotificationRecipient.RequiresResultSummary,
                                                                                           NotificationRecipient.RequiresEvent
                                                                                           
{
    private static final Logger log = Logger.getLogger(SpiraNotificationRecipient.class);
   
    private static String SPIRA_URL = "spiraURL";
    private static String SPIRA_USER = "spiraUser";
    private static String SPIRA_PASSW = "spiraPassw";
    private static String SPIRA_PVERS = "spiraPVersion";
    private static String SPIRA_PNUMBER = "spiraPNumber";
    private static String SPIRA_DATE = "spiraDate";//included to avoid BatchUpdateException of Java (having always new info)
    
    private String URL = null;
    private String user = null;
    private String passw = null;
    private String pvers = null;
    private String pnumber = null;
    private String miliDate = Long.toString(System.currentTimeMillis());
    private boolean notify = false;
    
    private TemplateRenderer templateRenderer;
    private ImmutablePlan plan;
    private ResultsSummary resultsSummary;
    private DeploymentResult deploymentResult;
    private Event event;
    private CustomVariableContext customVariableContext;
    
    @Override
    public void populate(@NotNull Map<String, String[]> params)
    {
    	for (String next : params.keySet())
        {
            System.out.println("next = " + next);
        }
        if (params.containsKey(SPIRA_URL))
        {
            int i = params.get(SPIRA_URL).length - 1;
            this.URL = params.get(SPIRA_URL)[i];
        }
        if (params.containsKey(SPIRA_USER))
        {
            int i = params.get(SPIRA_USER).length - 1;
            this.user = params.get(SPIRA_USER)[i];
        }
        if (params.containsKey(SPIRA_PASSW)) {
            int i = params.get(SPIRA_PASSW).length - 1;
            this.passw = params.get(SPIRA_PASSW)[i];
        }
        if (params.containsKey(SPIRA_PVERS)) {
            int i = params.get(SPIRA_PVERS).length - 1;
            this.pvers = params.get(SPIRA_PVERS)[i];
        }
        if (params.containsKey(SPIRA_PNUMBER)) {
            int i = params.get(SPIRA_PNUMBER).length - 1;
            this.pnumber = (params.get(SPIRA_PNUMBER)[i]);
        }
    }
     
    @Override
    public void init(@Nullable String configurationData)
    {
    	if (StringUtils.isNotBlank(configurationData))
        {
            String delimiter = "\\|";

            String[] configValues = configurationData.split(delimiter);

             if (configValues.length > 0) {
            	URL = configValues[0];
            }if (configValues.length > 1) {
            	user = configValues[1];
            }if (configValues.length > 2) {
            	passw = configValues[2];
            }if (configValues.length > 3) {
            	pvers = configValues[3];
            }if (configValues.length > 4) {
            	pnumber = configValues[4];
            }
        }  	
    }

    @NotNull
    @Override
    public String getRecipientConfig()
    {
        String delimiter = "|";

        StringBuilder recipientConfig = new StringBuilder();
        if (StringUtils.isNotBlank(URL)) {
            recipientConfig.append(URL);
        }
        if (StringUtils.isNotBlank(user)) {
            recipientConfig.append(delimiter);
            recipientConfig.append(user);
        }
        if (StringUtils.isNotBlank(passw)) {
            recipientConfig.append(delimiter);
            recipientConfig.append(passw);
        }
        if (StringUtils.isNotBlank(pvers)) {
            recipientConfig.append(delimiter);
            recipientConfig.append(pvers);
        }
        if (StringUtils.isNotBlank(pnumber)) {
            recipientConfig.append(delimiter);
            recipientConfig.append(pnumber);
        }
        if (StringUtils.isNotBlank(miliDate)) {
            recipientConfig.append(delimiter);
            recipientConfig.append(miliDate);
        }
        
        return recipientConfig.toString();
    }

    @NotNull
    @Override
    public String getEditHtml()
    {
        String editTemplateLocation = ((NotificationRecipientModuleDescriptor)getModuleDescriptor()).getEditTemplate();
        return templateRenderer.render(editTemplateLocation, populateContext());
    }

    private Map<String, Object> populateContext()
    {
    	Map<String, Object> context = Maps.newHashMap();
    	
         if (URL != null)
        {
            context.put(SPIRA_URL, URL);
        }if (user != null)
        {
            context.put(SPIRA_USER, user);
        }if (passw != null) {
            context.put(SPIRA_PASSW, passw);
        }if (pvers != null) {
            context.put(SPIRA_PVERS, pvers);
        }if (pnumber != null) {
            context.put(SPIRA_PNUMBER, pnumber);
        }
        if (miliDate != null) {
            context.put(SPIRA_DATE, miliDate);
        }
        return context;
    }

    @NotNull
    @Override
    public String getViewHtml()
    {
    	String editTemplateLocation = ((NotificationRecipientModuleDescriptor)getModuleDescriptor()).getViewTemplate();
        return templateRenderer.render(editTemplateLocation, populateContext());
    }

    @NotNull
    @Override
    public List<NotificationTransport> getTransports()
    { 
    	List<NotificationTransport> list = Lists.newArrayList();
        list.add(new com.inflectra.spiratest.plugins.bamboo.SpiraNotificationTransport(URL, user, passw,
        										            pvers,pnumber,notify, plan, resultsSummary, 
        										            deploymentResult, event, customVariableContext, 
        										            templateRenderer));
        return list;
    }

    @Override
    public void setEvent(@Nullable final Event event)
    {
        this.event = event;
    }

    public void setPlan(@Nullable final Plan plan)
    {
        this.plan = plan;
    }

    @Override
    public void setPlan(@Nullable final ImmutablePlan plan)
    {
        this.plan = plan;
    }

    @Override
    public void setDeploymentResult(@Nullable final DeploymentResult deploymentResult)
    {
        this.deploymentResult = deploymentResult;
    }

    @Override
    public void setResultsSummary(@Nullable final ResultsSummary resultsSummary)
    {
        this.resultsSummary = resultsSummary;
    }
    
    public void setTemplateRenderer(TemplateRenderer templateRenderer)
    {
        this.templateRenderer = templateRenderer;
    }

    public void setCustomVariableContext(CustomVariableContext customVariableContext) 
    { 
    	this.customVariableContext = customVariableContext; 
    }

	@Override
	public ErrorCollection validate(Map<String, String[]> params) {
		
		boolean badConnection = false;
		boolean badRelease = false;
		boolean badFormat = false;
		    
		ErrorCollection errors = super.validate(params);
		
		 if (params.containsKey(SPIRA_URL) ){
            int i = params.get(SPIRA_URL).length - 1;
            this.URL = params.get(SPIRA_URL)[i];
        }if (params.containsKey(SPIRA_USER)) {
            int i = params.get(SPIRA_USER).length - 1;
            this.user = params.get(SPIRA_USER)[i];
        }if (params.containsKey(SPIRA_PASSW)) {
            int i = params.get(SPIRA_PASSW).length - 1;
            this.passw = params.get(SPIRA_PASSW)[i];
        }if (params.containsKey(SPIRA_PVERS)) {
            int i = params.get(SPIRA_PVERS).length - 1;
            this.pvers = params.get(SPIRA_PVERS)[i];
        }if (params.containsKey(SPIRA_PNUMBER)) {
            int i = params.get(SPIRA_PNUMBER).length - 1;
            this.pnumber = (params.get(SPIRA_PNUMBER)[i]);
        }
        
        SpiraImportExport spiraClient = new SpiraImportExport();
    	spiraClient.setUrl(URL);	
    	spiraClient.setUserName(user);
    	spiraClient.setPassword(passw);
    	
    	try {
			spiraClient.testConnection();   
		} catch (Exception e) {
			badConnection = true;
			log.info(":: SpiraTeam Plugin :: Error connecting to Spira Server."
					+ "Action: Display error message to user.");
		}
    
    	try{
    	spiraClient.setProjectId(Integer.parseInt(pnumber));
    	}catch(NumberFormatException e3){
        	badFormat = true;
    	}
    	
    	try {
			if(spiraClient.verifyRelease (pvers)==null){
				badRelease = true;
				log.info(":: SpiraTeam Plugin :: Release version of SpiraTeam project does not exist."
						+ "Action: Display error message to user.");
			}
			
		} catch (Exception e) {
			badRelease = true;
			log.info(":: SpiraTeam Plugin :: SpiraTeam Project does not exist."
					+ "Action: Display error message to user.");
		}
    	
    	 if(badFormat){
    		errors.addErrorMessage("Error. Please use a numeric value for Project ID."+
    							   "All the fields are required.");
    		return errors; 
    	}if(badConnection){
    		errors.addErrorMessage("Unable to connect to the Spira server." +
    				 			   "Please verify the information and try again.");
    		return errors;
		}if(badRelease){
			errors.addErrorMessage("Unable to acess this project/release." +
					 			   "Please verify the information and try again.");
			return errors;	
		}	
	return errors;
	}
	
	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return super.compareTo(arg0);
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return super.getDescription();
	}

	@Override
	public String getKey() {
		// TODO Auto-generated method stub
		return super.getKey();
	}

	@Override
	public WeightedDescriptor getModuleDescriptor() {
		// TODO Auto-generated method stub
		return super.getModuleDescriptor();
	}

	@Override
	protected String getParam(String arg0, Map<String, String[]> arg1) {
		// TODO Auto-generated method stub
		return super.getParam(arg0, arg1);
	}

	@Override
	protected List<NotificationTransport> getTransports(Set<UserRecipient> arg0) {
		// TODO Auto-generated method stub
		return super.getTransports(arg0);
	}

	@Override
	public void init(WeightedDescriptor moduleDescriptor) {
		// TODO Auto-generated method stub
		super.init(moduleDescriptor);
	}
	
}
