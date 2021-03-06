// WARNING: DO NOT EDIT THIS FILE.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.openlegacy.as400.menus;

import java.util.*;
import org.openlegacy.terminal.ScreenEntity;
import org.openlegacy.terminal.Color;
import org.openlegacy.terminal.definitions.TerminalActionDefinition;

privileged @SuppressWarnings("unused") aspect InteractiveDataDefinitionUtility_Aspect {

    declare parents: InteractiveDataDefinitionUtility implements ScreenEntity;
    private String InteractiveDataDefinitionUtility.focusField;
    private List<TerminalActionDefinition> InteractiveDataDefinitionUtility.actions = new ArrayList<TerminalActionDefinition>();
    
	

	

    

    public String InteractiveDataDefinitionUtility.getSystem(){
    	return this.system;
    }
    

    public String InteractiveDataDefinitionUtility.getMenuSelection(){
    	return this.menuSelection;
    }
    
    public void InteractiveDataDefinitionUtility.setMenuSelection(String menuSelection){
    	this.menuSelection = menuSelection;
    }


    public String InteractiveDataDefinitionUtility.getFocusField(){
    	return focusField;
    }
    public void InteractiveDataDefinitionUtility.setFocusField(String focusField){
    	this.focusField = focusField;
    }
    
    public List<TerminalActionDefinition> InteractiveDataDefinitionUtility.getActions(){
    	return actions;
    }
    
}
