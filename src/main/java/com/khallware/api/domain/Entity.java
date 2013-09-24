// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.domain;

import java.util.UUID;
import java.util.Date;

/**
 * The Entity.  An Entity is the most generic container that persists in the db.
 *
 * @author khall
 */
public interface Entity
{
	public int getUser();
	public void setUser(int user);
	public void preSave();
	public UUID getUUID();
	public Date getModified();
}
