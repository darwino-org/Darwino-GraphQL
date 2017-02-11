/*!COPYRIGHT HEADER! 
 *
 * (c) Copyright Darwino Inc. 2014-2016.
 *
 * Licensed under The MIT License (https://opensource.org/licenses/MIT)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
 * and associated documentation files (the "Software"), to deal in the Software without restriction, 
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial 
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.darwino.graphql.example;

import com.darwino.commons.Platform;
import com.darwino.commons.security.acl.User;
import com.darwino.commons.security.acl.impl.UserImpl;
import com.darwino.commons.util.PathUtil;
import com.darwino.jdbc.connector.JdbcConnector;
import com.darwino.jdbc.connector.JdbcDirectConnector;
import com.darwino.jsonstore.LocalJsonDBServer;
import com.darwino.jsonstore.ServerACL;
import com.darwino.jsonstore.Session;
import com.darwino.jsonstore.local.DatabaseACLFactory;
import com.darwino.jsonstore.sql.impl.full.LocalFullJsonDBServerImpl;
import com.darwino.jsonstore.sql.impl.full.SqlContext;
import com.darwino.jsonstore.sql.impl.full.context.SqlJdbcContext;
import com.darwino.sql.drivers.DBDriver;
import com.darwino.sql.drivers.postgresql.PostgreSQLDriver;

/**
 * Base class for DB access.
 * 
 * @author Philippe Riand
 */
public class DbSample {
	
	// RDBMS parameters 
	public static final String RDBMS_SERVER		= "jdbc:postgresql://localhost:5432";
	public static final String RDBMS_DATABASE	= "workshop";
	public static final String RDBMS_SCHEMA		= null;
	public static final String RDBMS_USER		= "postgres";
	public static final String RDBMS_PASSWORD	= "postgres";

	// JSON Store parameters
	public static final String DATABASE_NAME	= "workshop";

	public static final String STORE_PEOPLE		= "peopl";
	public static final String STORE_USSTATES	= "states";
	
	private static LocalJsonDBServer server;
	private static Session defaultSession;
	static {
		try {
			// Use POSTGRESQL as the database
			DBDriver dbDriver = new PostgreSQLDriver(PostgreSQLDriver.CURRENT_VERSION);
			
			// Create a direct JDBC connector (no connection pool)
			// Other connector can be used
			//	- Embedded Connection Pool
			//	- JEE/JNDI datasource
			//  - Cloud connector
			JdbcConnector connector = new JdbcDirectConnector(
				JdbcConnector.TRANSACTION_READ_COMMITTED,
				dbDriver.getDriverClass(),
				PathUtil.concat(RDBMS_SERVER,RDBMS_DATABASE),
				RDBMS_USER,
				RDBMS_PASSWORD,
				null	// JDBC properties
			);
			
			// Create a SQL context to the database
			SqlContext dbContext = SqlJdbcContext.create(dbDriver,connector,RDBMS_SCHEMA);
			
			// Map the server to this context
			ServerACL acl = null;
			DatabaseACLFactory dbAcl = null;
			server = new LocalFullJsonDBServerImpl(dbContext, acl, dbAcl);
			
			// Create a DB session
			User user = new UserImpl("joe@darwino.org", "Joe Darwino");
			defaultSession = server.createSession(user, null);
		} catch(Exception e) {
			IllegalStateException ne = new IllegalStateException("Error while initializing database");
			ne.initCause(e);
			throw ne;
		}
	}
	
	public static LocalJsonDBServer getServer() {
		return server;
	}
	
	public static Session getSession() {
		return defaultSession;
	}
	
	public static void log(String msg, Object...p) {
		Platform.log(msg,p);
	}
	
}
