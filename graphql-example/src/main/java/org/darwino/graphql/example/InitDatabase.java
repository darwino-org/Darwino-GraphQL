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

import com.darwino.commons.json.JsonException;
import com.darwino.commons.util.StringUtil;
import com.darwino.jsonstore.impl.DatabaseFactoryImpl;
import com.darwino.jsonstore.meta._Database;
import com.darwino.jsonstore.meta._Store;

/**
 * Initialize the JSON database.
 * 
 * @author Philippe Riand
 */
public class InitDatabase extends DbSample {
	
	public void testDeploy() throws Exception {
		deploy();
	}

	public void deploy() throws Exception {
		getServer().deployDatabase(DATABASE_NAME, new AppDatabaseDef(), null, true, null);
		log("Database {0} successfully deployed!",DATABASE_NAME);
	}

	/**
	 * Definition of a simple database.
	 */
	public class AppDatabaseDef extends DatabaseFactoryImpl {

		public static final int DATABASE_VERSION	= 1;
		
		@Override
		public int getDatabaseVersion(String databaseName) throws JsonException {
			if(!StringUtil.equalsIgnoreCase(databaseName, DATABASE_NAME)) {
				return -1;
			}
			return DATABASE_VERSION;
		}
		
		@Override
		public _Database loadDatabase(String databaseName) throws JsonException {
			if(!StringUtil.equalsIgnoreCase(databaseName, DATABASE_NAME)) {
				return null;
			}
			_Database db = new _Database(DATABASE_NAME, "GraphQL", DATABASE_VERSION);
			{
				_Store _def = db.addStore(STORE_PEOPLE);
			}
			{
				_Store _def = db.addStore(STORE_USSTATES);
			}

			return db;
		}
	}
}
