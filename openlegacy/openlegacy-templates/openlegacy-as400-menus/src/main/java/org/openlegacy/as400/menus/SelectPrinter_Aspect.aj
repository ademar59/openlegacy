// WARNING: DO NOT EDIT THIS FILE.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.openlegacy.as400.menus;

import java.util.*;
import org.openlegacy.terminal.ScreenEntity;
import org.openlegacy.terminal.Color;
import org.openlegacy.terminal.definitions.TerminalActionDefinition;

privileged @SuppressWarnings("unused") aspect SelectPrinter_Aspect {

    declare parents: SelectPrinter implements ScreenEntity;
    private String SelectPrinter.focusField;
    private List<TerminalActionDefinition> SelectPrinter.actions = new ArrayList<TerminalActionDefinition>();
    
	

    private List<TerminalActionDefinition> SelectPrinter.selectPrinterRecordsActions = new ArrayList<TerminalActionDefinition>();
    

    public List<SelectPrinterRecord> SelectPrinter.getSelectPrinterRecords(){
    	return this.selectPrinterRecords;
    }
    

    public List<TerminalActionDefinition> SelectPrinter.getSelectPrinterRecordsActions(){
    	return this.selectPrinterRecordsActions;
    }

    public String SelectPrinter.getFocusField(){
    	return focusField;
    }
    public void SelectPrinter.setFocusField(String focusField){
    	this.focusField = focusField;
    }
    
    public List<TerminalActionDefinition> SelectPrinter.getActions(){
    	return actions;
    }
    
}