package com.openlegacy.enterprise.ide.eclipse.editors.actions.screen;

import com.openlegacy.enterprise.ide.eclipse.editors.actions.AbstractAction;
import com.openlegacy.enterprise.ide.eclipse.editors.actions.ActionType;
import com.openlegacy.enterprise.ide.eclipse.editors.models.screen.ScreenNavigationModel;

import org.openlegacy.annotations.screen.ScreenNavigation;

import java.util.UUID;

/**
 * @author Ivan Bort
 * 
 */
public class ScreenNavigationAction extends AbstractAction {

	/**
	 * @param uuid
	 * @param namedObject
	 * @param actionType
	 * @param kind
	 * @param key
	 * @param value
	 */
	public ScreenNavigationAction(UUID uuid, ScreenNavigationModel namedObject, ActionType actionType, int kind, String key,
			Object value) {
		super(uuid, namedObject, actionType, kind, key, value);
	}

	@Override
	public Class<?> getAnnotationClass() {
		return ScreenNavigation.class;
	}

}
