/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.tests;

import static java.lang.System.out;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;

import org.apache.pivot.collections.Map;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.sql.ResultList;

public final class ResultListTest {
    /** Hide utility class constructor. */
    private ResultListTest() { }

    public static void main(String[] args) throws Exception {
        // e.g. jdbc:mysql://localhost/test
        String connectionURL = args[0];

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        ResultList resultList = null;

        try {
            connection = DriverManager.getConnection(connectionURL);
            statement = connection.createStatement();

            resultSet = statement.executeQuery("SELECT * FROM result_list_test");
            resultList = new ResultList(resultSet);
            resultList.setFields(new ResultList.Field("i"), new ResultList.Field("f"),
                new ResultList.Field("s"), new ResultList.Field("b"));
            out.println(JSONSerializer.toString(resultList));

            resultSet = statement.executeQuery("SELECT * FROM result_list_test");
            resultList = new ResultList(resultSet);
            resultList.setFields(new ResultList.Field("i", "integer"), new ResultList.Field("f",
                "float"), new ResultList.Field("s", "string"), new ResultList.Field("b", "boolean"));
            out.println(JSONSerializer.toString(resultList));

            resultSet = statement.executeQuery("SELECT * FROM result_list_test");
            resultList = new ResultList(resultSet);
            resultList.setFields(new ResultList.Field("i", "integer", Integer.class),
                new ResultList.Field("f", "float", Float.class), new ResultList.Field("s",
                    "string", String.class), new ResultList.Field("b", "boolean", Boolean.class));
            out.println(JSONSerializer.toString(resultList));

            // Test forward and backward iteration
            resultSet = statement.executeQuery("SELECT * FROM result_list_test");
            resultList = new ResultList(resultSet);
            resultList.setFields(new ResultList.Field("i"), new ResultList.Field("f"),
                new ResultList.Field("s"), new ResultList.Field("b"));

            Iterator<Map<String, Object>> iterator = resultList.iterator();
            while (iterator.hasNext()) {
                out.println(JSONSerializer.toString(iterator.next()));
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }

            if (statement != null) {
                statement.close();
            }

            if (connection != null) {
                connection.close();
            }
        }
    }
}
