/*******************************************************************************
 * Copyright (c) 2014 OpenLegacy Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     OpenLegacy Inc. - initial API and implementation
 *******************************************************************************/

package org.openlegacy.db.exceptions;

import org.openlegacy.exceptions.EntityNotAccessibleException;

/**
 * @author Ivan Bort
 * 
 */
public class DbActionException extends EntityNotAccessibleException {

	private static final long serialVersionUID = 1L;

	public DbActionException(Exception e) {
		super(e);
	}

	public DbActionException(String s) {
		super(s);
	}

}
