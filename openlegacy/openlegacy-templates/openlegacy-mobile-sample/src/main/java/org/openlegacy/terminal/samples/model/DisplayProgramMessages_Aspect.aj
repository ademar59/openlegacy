// WARNING: DO NOT EDIT THIS FILE.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.openlegacy.terminal.samples.model;

import java.util.*;
import org.openlegacy.terminal.ScreenEntity;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;

privileged @SuppressWarnings("unused") aspect DisplayProgramMessages_Aspect {
    
    declare @type: DisplayProgramMessages : @Component;
	declare @type: DisplayProgramMessages : @Scope("prototype");
    

    declare parents: DisplayProgramMessages implements ScreenEntity;
    private String DisplayProgramMessages.focusField;
    
	

    

    public String DisplayProgramMessages.getMessage(){
    	return this.message;
    }
    
    public void DisplayProgramMessages.setMessage(String Message){
    	this.message = Message;
    }




    public String DisplayProgramMessages.getFocusField(){
    	return focusField;
    }
    public void DisplayProgramMessages.setFocusField(String focusField){
    	this.focusField = focusField;
    }
    
}
