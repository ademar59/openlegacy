// WARNING: DO NOT EDIT THIS FILE.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.openlegacy.terminal.samples.model;

import java.util.*;
import org.openlegacy.terminal.ScreenEntity;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;

privileged @SuppressWarnings("unused") aspect WorkWithItemMaster_Aspect {
    
    declare @type: WorkWithItemMaster : @Component;
	declare @type: WorkWithItemMaster : @Scope("prototype");
    

    declare parents: WorkWithItemMaster implements ScreenEntity;
    private String WorkWithItemMaster.focusField;
    
	

	

    

    public String WorkWithItemMaster.getPositionTo(){
    	return this.positionTo;
    }
    
    public void WorkWithItemMaster.setPositionTo(String positionTo){
    	this.positionTo = positionTo;
    }



    public List<WorkWithItemMasterRecord> WorkWithItemMaster.getWorkWithItemMasterRecords(){
    	return this.workWithItemMasterRecords;
    }
    




    public String WorkWithItemMaster.getFocusField(){
    	return focusField;
    }
    public void WorkWithItemMaster.setFocusField(String focusField){
    	this.focusField = focusField;
    }
    
}
