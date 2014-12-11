package org.openlegacy.as400.menus;

import org.openlegacy.annotations.screen.*;
import org.openlegacy.terminal.actions.TerminalActions;
import org.openlegacy.terminal.actions.TerminalAction.AdditionalKey;
import org.openlegacy.modules.menu.Menu.MenuEntity;  
import org.openlegacy.modules.menu.Menu.MenuSelectionField;  

@ScreenEntity(screenType=MenuEntity.class)
@ScreenIdentifiers(identifiers = { 
				@Identifier(row = 1, column = 32, value = "IBM i Access Tasks"), 
				@Identifier(row = 5, column = 4, value = "User Tasks"), 
				@Identifier(row = 12, column = 4, value = "Administrator Tasks") 
				})
@ScreenActions(actions = { 
				@Action(action = TerminalActions.F3.class, displayName = "Exit", alias = "exit"), 
				@Action(action = TerminalActions.F4.class, displayName = "Prompt", alias = "prompt"), 
				@Action(action = TerminalActions.F9.class, displayName = "Retrieve", alias = "retrieve"), 
				@Action(action = TerminalActions.F12.class, displayName = "Cancel", alias = "cancel"), 
				@Action(action = TerminalActions.F1.class ,additionalKey= AdditionalKey.SHIFT, displayName = "Information Assistant", alias = "informationAssistant"), 
				@Action(action = TerminalActions.F4.class ,additionalKey= AdditionalKey.SHIFT, displayName = "System Main menu", alias = "systemMainMenu") 
				})
@ScreenNavigation(accessedFrom = OfficeTasks.class 
					, assignedFields = { 
					@AssignedField(field = "menuSelection", value = "2")
					 }						
					)
public class IbmIAccessTasks {
    
	
	@ScreenField(row = 2, column = 72, endColumn= 79 ,labelColumn= 62 ,displayName = "System", sampleValue = "S1051C6E")
    private String system;
	
	@ScreenField(row = 21, column = 7, endColumn= 79 ,editable= true ,fieldType=MenuSelectionField.class ,displayName = "Menu Selection")
    private String menuSelection;

    


 
}
