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
package org.apache.pivot.tutorials.webqueries.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;

import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.CSVSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.serialization.Serializer;
import org.apache.pivot.web.Query;
import org.apache.pivot.web.Query.Method;
import org.apache.pivot.web.QueryException;
import org.apache.pivot.web.server.QueryServlet;

public class ExpenseServlet extends QueryServlet {
    private static final long serialVersionUID = 0;

    private List<Expense> expenses = null;
    private HashMap<Integer, Expense> expenseMap = new HashMap<>();

    private static int nextID = 0;

    @SuppressWarnings({ "resource", "unchecked" })
    @Override
    public void init() throws ServletException {
        CSVSerializer expenseSerializer = new CSVSerializer(Expense.class);
        expenseSerializer.setKeys("date", "type", "amount", "description");

        // Load the initial expense data
        InputStream inputStream = ExpenseServlet.class.getResourceAsStream("expenses.csv");

        try {
            expenses = (List<Expense>) expenseSerializer.readObject(inputStream);
        } catch (IOException exception) {
            throw new ServletException(exception);
        } catch (SerializationException exception) {
            throw new ServletException(exception);
        }

        // Index the initial expenses
        for (Expense expense : expenses) {
            int id = nextID++;
            expense.setID(id);
            expenseMap.put(id, expense);
        }
    }

    @Override
    protected Object doGet(Path path) throws QueryException {
        Object value;

        if (path.getLength() == 0) {
            value = expenses;
        } else {
            // Get the ID of the expense to retrieve from the path
            int id = Integer.parseInt(path.get(0));

            // Get the expense data from the map
            synchronized (this) {
                value = expenseMap.get(id);
            }

            if (value == null) {
                throw new QueryException(Query.Status.NOT_FOUND);
            }
        }

        return value;
    }

    @Override
    protected URL doPost(Path path, Object value) throws QueryException {
        if (value == null) {
            throw new QueryException(Query.Status.BAD_REQUEST);
        }

        Expense expense = (Expense) value;

        // Add the expense to the list/map
        int id;
        synchronized (this) {
            id = nextID++;
            expense.setID(id);
            expenses.add(expense);
            expenseMap.put(id, expense);
        }

        // Return the location of the newly-created resource
        URL location = getLocation();
        try {
            location = new URL(location, Integer.toString(id));
        } catch (MalformedURLException exception) {
            throw new QueryException(Query.Status.INTERNAL_SERVER_ERROR);
        }

        return location;
    }

    @Override
    protected boolean doPut(Path path, Object value) throws QueryException {
        if (path.getLength() == 0 || value == null) {
            throw new QueryException(Query.Status.BAD_REQUEST);
        }

        // Get the ID of the expense to retrieve from the path
        int id = Integer.parseInt(path.get(0));

        // Create the new expense and bind the data to it
        Expense expense = (Expense) value;
        expense.setID(id);

        // Update the list/map
        Expense previousExpense;
        synchronized (this) {
            previousExpense = expenseMap.put(id, expense);
            expenses.remove(previousExpense);
            expenses.add(expense);
        }

        return (previousExpense == null);
    }

    @Override
    protected void doDelete(Path path) throws QueryException {
        if (path.getLength() == 0) {
            throw new QueryException(Query.Status.BAD_REQUEST);
        }

        // Get the ID of the expense to retrieve from the path
        int id = Integer.parseInt(path.get(0));

        // Update the list/map
        Expense expense;
        synchronized (this) {
            expense = expenseMap.remove(id);
            expenses.remove(expense);
        }

        if (expense == null) {
            throw new QueryException(Query.Status.NOT_FOUND);
        }
    }

    @Override
    protected Serializer<?> createSerializer(Method method, Path path) throws QueryException {
        return new JSONSerializer(Expense.class);
    }
}
