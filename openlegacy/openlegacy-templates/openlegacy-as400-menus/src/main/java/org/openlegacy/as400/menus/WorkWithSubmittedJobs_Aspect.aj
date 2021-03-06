// WARNING: DO NOT EDIT THIS FILE.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.openlegacy.as400.menus;

import java.util.*;
import org.openlegacy.terminal.ScreenEntity;
import org.openlegacy.terminal.Color;
import org.openlegacy.terminal.definitions.TerminalActionDefinition;

privileged @SuppressWarnings("unused") aspect WorkWithSubmittedJobs_Aspect {

    declare parents: WorkWithSubmittedJobs implements ScreenEntity;
    private String WorkWithSubmittedJobs.focusField;
    private List<TerminalActionDefinition> WorkWithSubmittedJobs.actions = new ArrayList<TerminalActionDefinition>();
    
	

	

    private List<TerminalActionDefinition> WorkWithSubmittedJobs.WorkWithSubmittedJobsRecordsActions = new ArrayList<TerminalActionDefinition>();
    

    public String WorkWithSubmittedJobs.getSubmittedFrom(){
    	return this.submittedFrom;
    }
    

    public List<WorkWithSubmittedJobsRecord> WorkWithSubmittedJobs.getWorkWithSubmittedJobsRecords(){
    	return this.WorkWithSubmittedJobsRecords;
    }
    

    public List<TerminalActionDefinition> WorkWithSubmittedJobs.getWorkWithSubmittedJobsRecordsActions(){
    	return this.WorkWithSubmittedJobsRecordsActions;
    }

    public String WorkWithSubmittedJobs.getFocusField(){
    	return focusField;
    }
    public void WorkWithSubmittedJobs.setFocusField(String focusField){
    	this.focusField = focusField;
    }
    
    public List<TerminalActionDefinition> WorkWithSubmittedJobs.getActions(){
    	return actions;
    }
    
}
