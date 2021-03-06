// WARNING: DO NOT EDIT THIS FILE.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.openlegacy.as400.menus;

import java.util.*;
import org.openlegacy.terminal.ScreenEntity;
import org.openlegacy.terminal.Color;
import org.openlegacy.terminal.definitions.TerminalActionDefinition;

privileged @SuppressWarnings("unused") aspect IbmIMainMenu_Aspect {

    declare parents: IbmIMainMenu implements ScreenEntity;
    private String IbmIMainMenu.focusField;
    private List<TerminalActionDefinition> IbmIMainMenu.actions = new ArrayList<TerminalActionDefinition>();
    
	

	

    

    public String IbmIMainMenu.getSystem(){
    	return this.system;
    }
    

    public String IbmIMainMenu.getMenuSelection(){
    	return this.menuSelection;
    }
    
    public void IbmIMainMenu.setMenuSelection(String menuSelection){
    	this.menuSelection = menuSelection;
    }


    public String IbmIMainMenu.getFocusField(){
    	return focusField;
    }
    public void IbmIMainMenu.setFocusField(String focusField){
    	this.focusField = focusField;
    }
    
    public List<TerminalActionDefinition> IbmIMainMenu.getActions(){
    	return actions;
    }
    
}
