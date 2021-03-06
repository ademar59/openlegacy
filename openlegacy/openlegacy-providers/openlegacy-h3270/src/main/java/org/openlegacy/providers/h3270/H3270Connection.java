package org.openlegacy.providers.h3270;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.h3270.host.S3270;
import org.h3270.host.S3270Screen;
import org.openlegacy.terminal.TerminalConnection;
import org.openlegacy.terminal.TerminalField;
import org.openlegacy.terminal.TerminalPosition;
import org.openlegacy.terminal.TerminalSendAction;
import org.openlegacy.terminal.TerminalSnapshot;

import java.text.MessageFormat;
import java.util.List;

public class H3270Connection implements TerminalConnection {

	private final static Log logger = LogFactory.getLog(H3270Connection.class);

	private S3270 s3270Session;

	// adding sequence support to tn5250j which doesn't support it
	private int sequence = 1;

	private boolean rightToLeftState = false;

	private boolean convertToLogical;

	private Boolean connected;

	public H3270Connection(S3270 s3270Session, boolean convertToLogical) {
		this.s3270Session = s3270Session;
		this.convertToLogical = convertToLogical;
	}

	public TerminalSnapshot getSnapshot() {
		connected = null;
		s3270Session.updateScreen();
		return new H3270TerminalSnapshot((S3270Screen)s3270Session.getScreen(), sequence, rightToLeftState, convertToLogical);
	}

	public TerminalSnapshot fetchSnapshot() {
		return getSnapshot();
	}

	public void doAction(TerminalSendAction terminalSendAction) {
		TerminalSnapshot snapshot = getSnapshot();
		List<TerminalField> modifiedFields = terminalSendAction.getFields();
		TerminalPosition cursor = terminalSendAction.getCursorPosition();
		for (TerminalField modifiedField : modifiedFields) {
			H3270TerminalField field = (H3270TerminalField)snapshot.getField(modifiedField.getPosition());
			if (field != null) {
				field.setValue(modifiedField.getValue());
			} else {
				logger.warn(MessageFormat.format("Field in position {0} not found for sending modified content:{1}",
						modifiedField.getPosition(), modifiedField.getValue()));
			}
		}
		s3270Session.submitScreen();
		if (cursor != null) {
			// s3270 is 0 based
			s3270Session.setCursor(cursor.getRow() - 1, cursor.getColumn() - 1);
		}
		s3270Session.doKey(terminalSendAction.getCommand().toString());
		sequence += 2;
	}

	public Object getDelegate() {
		return s3270Session;
	}

	public String getSessionId() {
		// TODO better session id handling
		return String.valueOf(s3270Session.hashCode());
	}

	public boolean isConnected() {
		if (connected == null) {
			connected = s3270Session.isConnected();
		}
		return connected;
	}

	public void disconnect() {
		s3270Session.disconnect();
	}

	public Integer getSequence() {
		return sequence;
	}

	public void flip() {
		rightToLeftState = !rightToLeftState;
	}

	public boolean isRightToLeftState() {
		return rightToLeftState;
	}
}
