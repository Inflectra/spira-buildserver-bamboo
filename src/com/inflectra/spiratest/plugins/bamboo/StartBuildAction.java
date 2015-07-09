package com.inflectra.spiratest.plugins.bamboo;

import com.atlassian.bamboo.build.CustomPreBuildAction;
import com.atlassian.bamboo.notification.*;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.ww2.actions.build.admin.create.BuildConfiguration;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * User: Tishenko
 * Datetime: 28.01.13 14:52
 */
public class StartBuildAction implements CustomPreBuildAction {
	private static final Logger log = Logger.getLogger(StartBuildAction.class);
	BuildContext buildContext;
	private PlanManager planManager;
	private TransactionTemplate transactionTemplate;
	
	public void init(@NotNull final BuildContext buildContext) {
		InfoManager info = new InfoManager();
        this.buildContext = buildContext;
        info.storeRevision( buildContext.getBuildChanges().getRepositoryChanges().toString());
    }
	
	@NotNull
	public BuildContext call() throws InterruptedException, Exception {
		return this.buildContext;
	}
	
	@Override
	public ErrorCollection validate(BuildConfiguration arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}