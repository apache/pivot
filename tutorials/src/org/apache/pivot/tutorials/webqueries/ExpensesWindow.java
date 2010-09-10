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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
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
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.Bindable;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;

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

    private RefreshExpenseListAction refreshExpenseListAction = new RefreshExpenseListAction();
    private AddExpenseAction addExpenseAction = new AddExpenseAction();
    private EditSelectedExpenseAction editSelectedExpenseAction = new EditSelectedExpenseAction();
    private DeleteSelectedExpenseAction deleteSelectedExpenseAction = new DeleteSelectedExpenseAction();

    @WTKX private TableView expenseTableView = null;
    @WTKX private ActivityIndicator activityIndicator = null;
    @WTKX private BoxPane activityIndicatorBoxPane = null;

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
    @SuppressWarnings("unchecked")
    public void initialize(Resources resources) {
        // Load the add/edit sheet
        try {
            WTKXSerializer wtkxSerializer = new WTKXSerializer(new Resources(ExpenseSheet.class.getName()));
            expenseSheet = (ExpenseSheet)wtkxSerializer.readObject(this, "expense_sheet.wtkx");
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }

        // Create the delete confirmation prompt
        ArrayList<String> options = new ArrayList(resources.getString("cancel"),
            resources.getString("ok"));
        deleteConfirmationPrompt = new Prompt(MessageType.QUESTION, resources.getString("confirmDelete"),
            options);

        // Attach event listener(s)
        expenseTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener.Adapter() {
            @Override
            public void selectedRangesChanged(TableView tableView, Sequence<Span> previousSelectedRanges) {
                updateSelectionState();
            }
        });
    }

    @Override
    public void open(Display display, Window owner) {
        super.open(display, owner);

        // Load the expense data
        refreshExpenseList();
    }

    private void refreshExpenseList() {
        Expenses expensesApplication = Expenses.getInstance();
        GetQuery expenseListQuery = new GetQuery(expensesApplication.getHostname(),
            expensesApplication.getPort(), "/pivot-tutorials/expenses",
            expensesApplication.isSecure());

        activityIndicatorBoxPane.setVisible(true);
        activityIndicator.setActive(true);

        expenseListQuery.execute(new TaskAdapter<Object>(new TaskListener<Object>() {
            @Override
            public void taskExecuted(Task<Object> task) {
                activityIndicatorBoxPane.setVisible(false);
                activityIndicator.setActive(false);

                List<?> expenseData = (List<?>)task.getResult();
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

    @SuppressWarnings("unchecked")
    private void addExpense() {
        expenseSheet.clear();
        expenseSheet.open(this, new SheetCloseListener() {
            @Override
            public void sheetClosed(Sheet sheet) {
                if (sheet.getResult()) {
                    // Get the expense data from the sheet
                    final HashMap<String, Object> expense = new HashMap<String, Object>();
                    expenseSheet.store(expense);

                    // POST expense to server and then add to table
                    Expenses expensesApplication = Expenses.getInstance();
                    PostQuery addExpenseQuery = new PostQuery(expensesApplication.getHostname(),
                        expensesApplication.getPort(), "/pivot-tutorials/expenses",
                        expensesApplication.isSecure());
                    addExpenseQuery.setValue(expense);

                    activityIndicatorBoxPane.setVisible(true);
                    activityIndicator.setActive(true);

                    addExpenseQuery.execute(new TaskAdapter<URL>(new TaskListener<URL>() {
                        @Override
                        public void taskExecuted(Task<URL> task) {
                            activityIndicatorBoxPane.setVisible(false);
                            activityIndicator.setActive(false);

                            URL location = task.getResult();
                            String file = location.getFile();
                            int id = Integer.parseInt(file.substring(file.lastIndexOf('/') + 1));
                            expense.put("id", id);

                            List<Object> expenses = (List<Object>)expenseTableView.getTableData();
                            expenses.add(expense);
                        }

                        @Override
                        public void executeFailed(Task<URL> task) {
                            activityIndicatorBoxPane.setVisible(false);
                            activityIndicator.setActive(false);

                            Prompt.prompt(MessageType.ERROR, task.getFault().getMessage(), ExpensesWindow.this);
                        }
                    }));
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void updateSelectedExpense() {
        Object expense = expenseTableView.getSelectedRow();
        final int id = JSON.getInteger(expense, "id");

        expenseSheet.load(expense);
        expenseSheet.open(this, new SheetCloseListener() {
            @Override
            public void sheetClosed(Sheet sheet) {
                if (sheet.getResult()) {
                    // Get the expense data from the sheet
                    final HashMap<String, Object> expense = new HashMap<String, Object>();
                    expenseSheet.store(expense);

                    // PUT expense to server and then update table
                    Expenses expensesApplication = Expenses.getInstance();
                    PutQuery updateExpenseQuery = new PutQuery(expensesApplication.getHostname(),
                        expensesApplication.getPort(), "/pivot-tutorials/expenses/" + JSON.get(expense, "id"),
                        expensesApplication.isSecure());
                    updateExpenseQuery.setValue(expense);

                    activityIndicatorBoxPane.setVisible(true);
                    activityIndicator.setActive(true);

                    updateExpenseQuery.execute(new TaskAdapter<Boolean>(new TaskListener<Boolean>() {
                        @Override
                        public void taskExecuted(Task<Boolean> task) {
                            activityIndicatorBoxPane.setVisible(false);
                            activityIndicator.setActive(false);

                            // Find matching row and update
                            List<Object> expenses = (List<Object>)expenseTableView.getTableData();
                            for (int i = 0, n = expenses.getLength(); i < n; i++) {
                                if (JSON.get(expenses.get(i), "id").equals(id)) {
                                    expenses.update(i, expense);
                                    break;
                                }
                            }
                        }

                        @Override
                        public void executeFailed(Task<Boolean> task) {
                            activityIndicatorBoxPane.setVisible(false);
                            activityIndicator.setActive(false);

                            Prompt.prompt(MessageType.ERROR, task.getFault().getMessage(), ExpensesWindow.this);
                        }
                    }));
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void deleteSelectedExpense() {
        Object expense = expenseTableView.getSelectedRow();
        final int id = JSON.getInteger(expense, "id");

        deleteConfirmationPrompt.open(this, new SheetCloseListener() {
            @Override
            public void sheetClosed(Sheet sheet) {
                if (sheet.getResult()
                    && ((Prompt)sheet).getSelectedOption() == 1) {
                    // DELETE expense from server and then remove from table
                    Expenses expensesApplication = Expenses.getInstance();
                    DeleteQuery deleteExpenseQuery = new DeleteQuery(expensesApplication.getHostname(),
                        expensesApplication.getPort(), "/pivot-tutorials/expenses/" + id,
                        expensesApplication.isSecure());

                    activityIndicatorBoxPane.setVisible(true);
                    activityIndicator.setActive(true);

                    deleteExpenseQuery.execute(new TaskAdapter<Void>(new TaskListener<Void>() {
                        @Override
                        public void taskExecuted(Task<Void> task) {
                            activityIndicatorBoxPane.setVisible(false);
                            activityIndicator.setActive(false);

                            // Find matching row and remove
                            List<Object> expenses = (List<Object>)expenseTableView.getTableData();
                            for (int i = 0, n = expenses.getLength(); i < n; i++) {
                                if (JSON.get(expenses.get(i), "id").equals(id)) {
                                    expenses.remove(i, 1);
                                    updateSelectionState();
                                    break;
                                }
                            }
                        }

                        @Override
                        public void executeFailed(Task<Void> task) {
                            activityIndicatorBoxPane.setVisible(false);
                            activityIndicator.setActive(false);

                            Prompt.prompt(MessageType.ERROR, task.getFault().getMessage(), ExpensesWindow.this);
                        }
                    }));
                }
            }
        });
    }

    private void updateSelectionState() {
        int selectedIndex = expenseTableView.getSelectedIndex();
        editSelectedExpenseAction.setEnabled(selectedIndex != -1);
        deleteSelectedExpenseAction.setEnabled(selectedIndex != -1);
    }
}
