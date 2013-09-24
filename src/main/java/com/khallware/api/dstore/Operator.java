// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.dstore;

import com.khallware.api.Datastore;
import com.khallware.api.DatastoreException;
import com.j256.ormlite.db.MysqlDatabaseType;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.Dao;
import java.sql.SQLException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class Operator
{
	public static String username = Datastore.DEF_DBUSER;
	public static String password = Datastore.DEF_DBPASS;
	public static String url = Datastore.DEF_DBURL;

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
			cs = new JdbcConnectionSource(url, username,
				password, new MysqlDatabaseType());
			dao = DaoManager.<Dao<T,Integer>, T>
				createDao(cs, clazz);
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
					throw new DatastoreException(
						se);
				}
			}
		}
	}
}
