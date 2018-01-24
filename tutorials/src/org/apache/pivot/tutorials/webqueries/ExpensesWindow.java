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
package org.apache.pivot.tutorials.webqueries;

import java.io.IOException;
import java.net.URL;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.json.JSON;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.web.DeleteQuery;
import org.apache.pivot.web.GetQuery;
import org.apache.pivot.web.PostQuery;
import org.apache.pivot.web.PutQuery;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.Window;

/**
 * Main expense management window.
 */
public class ExpensesWindow extends Window implements Bindable {

    private class RefreshExpenseListAction extends Action {
        @Override
        public void perform(Component source) {
            refreshExpenseList();
        }
    }

    private class AddExpenseAction extends Action {
        @Override
        public void perform(Component source) {
            addExpense();
        }
    }

    private class EditSelectedExpenseAction extends Action {
        public EditSelectedExpenseAction() {
            super(false);
        }

        @Override
        public void perform(Component source) {
            updateSelectedExpense();
        }
    }

    private class DeleteSelectedExpenseAction extends Action {
        public DeleteSelectedExpenseAction() {
            super(false);
        }

        @Override
        public void perform(Component source) {
            deleteSelectedExpense();
        }
    }

    private Expenses expensesApplication = null;

    private RefreshExpenseListAction refreshExpenseListAction = new RefreshExpenseListAction();
    private AddExpenseAction addExpenseAction = new AddExpenseAction();
    private EditSelectedExpenseAction editSelectedExpenseAction = new EditSelectedExpenseAction();
    private DeleteSelectedExpenseAction deleteSelectedExpenseAction = new DeleteSelectedExpenseAction();

    private TableView expenseTableView = null;
    private ActivityIndicator activityIndicator = null;
    private BoxPane activityIndicatorBoxPane = null;

    private ExpenseSheet expenseSheet = null;
    private Prompt deleteConfirmationPrompt = null;

    public static final String REFRESH_EXPENSE_LIST_ACTION_ID = "refresh_expense_list";
    public static final String ADD_EXPENSE_ACTION_ID = "add_expense";
    public static final String EDIT_SELECTED_EXPENSE_ACTION_ID = "edit_selected_expense";
    public static final String DELETE_SELECTED_EXPENSE_ACTION_ID = "delete_selected_expense";

    public ExpensesWindow() {
        // Add actions to global action dictionary
        Action.getNamedActions().put(REFRESH_EXPENSE_LIST_ACTION_ID, refreshExpenseListAction);
        Action.getNamedActions().put(ADD_EXPENSE_ACTION_ID, addExpenseAction);
        Action.getNamedActions().put(EDIT_SELECTED_EXPENSE_ACTION_ID, editSelectedExpenseAction);
        Action.getNamedActions().put(DELETE_SELECTED_EXPENSE_ACTION_ID, deleteSelectedExpenseAction);
    }

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        expenseTableView = (TableView) namespace.get("expenseTableView");
        activityIndicator = (ActivityIndicator) namespace.get("activityIndicator");
        activityIndicatorBoxPane = (BoxPane) namespace.get("activityIndicatorBoxPane");

        // Load the add/edit sheet
        try {
            BXMLSerializer bxmlSerializer = new BXMLSerializer();
            expenseSheet = (ExpenseSheet) bxmlSerializer.readObject(ExpenseSheet.class,
                "expense_sheet.bxml", true);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }

        // Create the delete confirmation prompt
        ArrayList<String> options = new ArrayList<>((String) resources.get("ok"),
            (String) resources.get("cancel"));
        deleteConfirmationPrompt = new Prompt(MessageType.QUESTION,
            (String) resources.get("confirmDelete"), options);

        // Attach event listeners
        expenseTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener() {
                @Override
                public void selectedRowChanged(TableView tableView, Object previousSelectedRow) {
                    int selectedIndex = expenseTableView.getSelectedIndex();
                    editSelectedExpenseAction.setEnabled(selectedIndex != -1);
                    deleteSelectedExpenseAction.setEnabled(selectedIndex != -1);
                }
            });
    }

    public Expenses getExpensesApplication() {
        return expensesApplication;
    }

    public void setExpensesApplication(Expenses expensesApplication) {
        this.expensesApplication = expensesApplication;
    }

    @Override
    public void open(Display display, Window owner) {
        super.open(display, owner);

        // Load the expense data
        refreshExpenseList();
    }

    private void refreshExpenseList() {
        Expenses expensesApplicationLocal = getExpensesApplication();
        GetQuery expenseListQuery = new GetQuery(expensesApplicationLocal.getHostname(),
            expensesApplicationLocal.getPort(), "/pivot-tutorials/expenses",
            expensesApplicationLocal.isSecure());

        activityIndicatorBoxPane.setVisible(true);
        activityIndicator.setActive(true);

        expenseListQuery.execute(new TaskAdapter<>(new TaskListener<Object>() {
            @Override
            public void taskExecuted(Task<Object> task) {
                activityIndicatorBoxPane.setVisible(false);
                activityIndicator.setActive(false);

                List<?> expenseData = (List<?>) task.getResult();
                expenseTableView.setTableData(expenseData);
            }

            @Override
            public void executeFailed(Task<Object> task) {
                activityIndicatorBoxPane.setVisible(false);
                activityIndicator.setActive(false);

                Prompt.prompt(MessageType.ERROR, task.getFault().getMessage(), ExpensesWindow.this);
            }
        }));
    }

    private void addExpense() {
        expenseSheet.clear();
        expenseSheet.open(this, new SheetCloseListener() {
            @Override
            public void sheetClosed(Sheet sheet) {
                if (sheet.getResult()) {
                    // Get the expense data from the sheet
                    final HashMap<String, Object> expense = new HashMap<>();
                    expenseSheet.store(expense);

                    // POST expense to server and then add to table
                    Expenses expensesApplicationLocal = getExpensesApplication();
                    PostQuery addExpenseQuery = new PostQuery(
                        expensesApplicationLocal.getHostname(), expensesApplicationLocal.getPort(),
                        "/pivot-tutorials/expenses", expensesApplicationLocal.isSecure());
                    addExpenseQuery.setValue(expense);

                    activityIndicatorBoxPane.setVisible(true);
                    activityIndicator.setActive(true);

                    addExpenseQuery.execute(new TaskAdapter<>(new TaskListener<URL>() {
                        @Override
                        public void taskExecuted(Task<URL> task) {
                            activityIndicatorBoxPane.setVisible(false);
                            activityIndicator.setActive(false);

                            URL location = task.getResult();
                            String file = location.getFile();
                            int id = Integer.parseInt(file.substring(file.lastIndexOf('/') + 1));
                            expense.put("id", id);

                            @SuppressWarnings("unchecked")
                            List<Object> expenses = (List<Object>) expenseTableView.getTableData();
                            expenses.add(expense);
                        }

                        @Override
                        public void executeFailed(Task<URL> task) {
                            activityIndicatorBoxPane.setVisible(false);
                            activityIndicator.setActive(false);

                            Prompt.prompt(MessageType.ERROR, task.getFault().getMessage(),
                                ExpensesWindow.this);
                        }
                    }));
                }
            }
        });
    }

    private void updateSelectedExpense() {
        Object expense = expenseTableView.getSelectedRow();
        final int id = JSON.getInt(expense, "id");

        expenseSheet.load(expense);
        expenseSheet.open(this, new SheetCloseListener() {
            @Override
            public void sheetClosed(Sheet sheet) {
                if (sheet.getResult()) {
                    // Get the expense data from the sheet
                    final HashMap<String, Object> expenseLocal = new HashMap<>();
                    expenseSheet.store(expenseLocal);

                    // PUT expense to server and then update table
                    Expenses expensesApplicationLocal = getExpensesApplication();
                    PutQuery updateExpenseQuery = new PutQuery(
                        expensesApplicationLocal.getHostname(), expensesApplicationLocal.getPort(),
                        "/pivot-tutorials/expenses/" + JSON.get(expenseLocal, "id"),
                        expensesApplicationLocal.isSecure());
                    updateExpenseQuery.setValue(expenseLocal);

                    activityIndicatorBoxPane.setVisible(true);
                    activityIndicator.setActive(true);

                    updateExpenseQuery.execute(new TaskAdapter<>(new TaskListener<Boolean>() {
                        @Override
                        public void taskExecuted(Task<Boolean> task) {
                            activityIndicatorBoxPane.setVisible(false);
                            activityIndicator.setActive(false);

                            // Find matching row and update
                            @SuppressWarnings("unchecked")
                            List<Object> expenses = (List<Object>) expenseTableView.getTableData();
                            for (int i = 0, n = expenses.getLength(); i < n; i++) {
                                if (JSON.get(expenses.get(i), "id").equals(id)) {
                                    expenses.update(i, expenseLocal);
                                    break;
                                }
                            }
                        }

                        @Override
                        public void executeFailed(Task<Boolean> task) {
                            activityIndicatorBoxPane.setVisible(false);
                            activityIndicator.setActive(false);

                            Prompt.prompt(MessageType.ERROR, task.getFault().getMessage(),
                                ExpensesWindow.this);
                        }
                    }));
                }
            }
        });
    }

    private void deleteSelectedExpense() {
        Object expense = expenseTableView.getSelectedRow();
        final int id = JSON.getInt(expense, "id");

        deleteConfirmationPrompt.open(this, new SheetCloseListener() {
            @Override
            public void sheetClosed(Sheet sheet) {
                if (sheet.getResult() && ((Prompt) sheet).getSelectedOptionIndex() == 1) {
                    // DELETE expense from server and then remove from table
                    Expenses expensesApplicationLocal = getExpensesApplication();
                    DeleteQuery deleteExpenseQuery = new DeleteQuery(
                        expensesApplicationLocal.getHostname(), expensesApplicationLocal.getPort(),
                        "/pivot-tutorials/expenses/" + id, expensesApplicationLocal.isSecure());

                    activityIndicatorBoxPane.setVisible(true);
                    activityIndicator.setActive(true);

                    deleteExpenseQuery.execute(new TaskAdapter<>(new TaskListener<Void>() {
                        @Override
                        public void taskExecuted(Task<Void> task) {
                            activityIndicatorBoxPane.setVisible(false);
                            activityIndicator.setActive(false);

                            // Find matching row and remove
                            @SuppressWarnings("unchecked")
                            List<Object> expenses = (List<Object>) expenseTableView.getTableData();
                            for (int i = 0, n = expenses.getLength(); i < n; i++) {
                                if (JSON.get(expenses.get(i), "id").equals(id)) {
                                    expenses.remove(i, 1);
                                    break;
                                }
                            }
                        }

                        @Override
                        public void executeFailed(Task<Void> task) {
                            activityIndicatorBoxPane.setVisible(false);
                            activityIndicator.setActive(false);

                            Prompt.prompt(MessageType.ERROR, task.getFault().getMessage(),
                                ExpensesWindow.this);
                        }
                    }));
                }
            }
        });
    }

}
