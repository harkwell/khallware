// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.dstore;

import com.khallware.api.Datastore;
import com.khallware.api.DatastoreException;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.db.MysqlDatabaseType;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.Dao;
import java.sql.SQLException;

public class Operator
{
	@FunctionalInterface
	public interface Operation<T>
	{
		public void handle(Dao<T, Integer> dao) throws Exception;
	}

	public static <T> void perform(Operation<T> operation, Class<T> clazz)
			throws DatastoreException
	{
		ConnectionSource cs = null;
		Dao<T, Integer> dao = null;
		try {
			cs = new DataSourceConnectionSource(
				Datastore.getDataSource(),
				new MysqlDatabaseType());
			dao = DaoManager.<Dao<T,Integer>,T>createDao(cs, clazz);
			operation.handle(dao);
			cs.close();
		}
		catch (Exception e) {
			throw new DatastoreException(e);
		}
		finally {
			if (cs != null && cs.isOpen()) {
				try {
					cs.close();
				}
				catch (SQLException se) {
					throw new DatastoreException(se);
				}
			}
		}
	}
}
