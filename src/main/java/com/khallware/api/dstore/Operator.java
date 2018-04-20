// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.dstore;

import com.khallware.api.Datastore;
import com.khallware.api.DatastoreException;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.db.SqliteDatabaseType;
import com.j256.ormlite.db.MysqlDatabaseType;
import com.j256.ormlite.db.DatabaseType;
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

	private static DatabaseType databaseType = null;

	public static <T> void perform(Operation<T> operation, Class<T> clazz)
			throws DatastoreException
	{
		ConnectionSource cs = null;
		Dao<T, Integer> dao = null;
		try {
			cs = new DataSourceConnectionSource(
				Datastore.getDataSource(),
				Operator.getDatabaseType());
			dao = DaoManager.<Dao<T,Integer>,T>createDao(cs, clazz);
			operation.handle(dao);
			cs.close();
		}
		catch (Exception e) {
			throw new DatastoreException(e);
		}
		finally {
			if (cs != null) {
				try {
					cs.close();
				}
				catch (Exception se) {
					throw new DatastoreException(se);
				}
			}
		}
	}

	private static DatabaseType getDatabaseType()
	{
		if (databaseType != null) {
			return(databaseType);
		}
		switch (Datastore.DS().getProperty(Datastore.PROP_DBDRIVER,"")){
		case "org.sqlite.JDBC":
			Operator.databaseType = new SqliteDatabaseType();
			break;
		default:
			Operator.databaseType = new MysqlDatabaseType();
			break;
		}
		return(databaseType);
	}
}
