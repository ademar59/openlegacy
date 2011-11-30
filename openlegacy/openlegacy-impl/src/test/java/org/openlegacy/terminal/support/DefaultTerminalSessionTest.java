package org.openlegacy.terminal.support;

import apps.inventory.screens.MainMenu;
import apps.inventory.screens.SignOn;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlegacy.AbstractTest;
import org.openlegacy.exceptions.SessionEndedException;
import org.openlegacy.terminal.actions.TerminalActions;
import org.openlegacy.terminal.exceptions.TerminalActionException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import junit.framework.Assert;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class DefaultTerminalSessionTest extends AbstractTest {

	@Test
	public void testConnection() {
		Assert.assertFalse(terminalSession.isConnected());

		terminalSession.getSnapshot();

		Assert.assertTrue(terminalSession.isConnected());

		terminalSession.disconnect();

		Assert.assertFalse(terminalSession.isConnected());
	}

	@Test
	public void testCursor() {
		SignOn signOn = terminalSession.getEntity(SignOn.class);
		Assert.assertEquals("user", signOn.getFocusField());

		signOn.setUser("someuser");
		signOn.setPassword("somepwd");
		signOn.setFocusField("user");
		try {
			terminalSession.doAction(TerminalActions.ENTER(), signOn, MainMenu.class);
		} catch (SessionEndedException e) {
			// ok
		}
	}

	@Test(expected = TerminalActionException.class)
	public void testCursorException() {
		SignOn signOn = terminalSession.getEntity(SignOn.class);

		signOn.setUser("someuser");
		signOn.setPassword("somepwd");
		// cursor is expected to be at "user"
		signOn.setFocusField("password");
		try {
			terminalSession.doAction(TerminalActions.ENTER(), signOn, MainMenu.class);
		} catch (SessionEndedException e) {
			// ok
		}
	}

	@Test(expected = TerminalActionException.class)
	public void testNotAllFieldsSent() {
		SignOn signOn = terminalSession.getEntity(SignOn.class);

		signOn.setUser("someuser");
		try {
			terminalSession.doAction(TerminalActions.ENTER(), signOn, MainMenu.class);
		} catch (SessionEndedException e) {
			// ok
		}
	}

	@Test(expected = TerminalActionException.class)
	public void testCursorIncorrect() {
		SignOn signOn = terminalSession.getEntity(SignOn.class);
		signOn.setFocusField("no_such_field");
		terminalSession.doAction(TerminalActions.ENTER(), signOn, MainMenu.class);
	}
}
