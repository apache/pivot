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

import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.Bindable;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;

public class ExpensesWindow extends Window implements Bindable {
    private class RefreshExpenseListAction extends Action {
        @Override
        public void perform() {
            refreshExpenseList();
        }
    }

    private class AddExpenseAction extends Action {
        @Override
        public void perform() {
            addExpense();
        }
    }

    private class EditSelectedExpenseAction extends Action {
        public EditSelectedExpenseAction() {
            super(false);
        }

        @Override
        public void perform() {
            updateSelectedExpense();
        }
    }

    private class DeleteSelectedExpenseAction extends Action {
        public DeleteSelectedExpenseAction() {
            super(false);
        }

        @Override
        public void perform() {
            deleteSelectedExpense();
        }
    }

    private RefreshExpenseListAction refreshExpenseListAction = new RefreshExpenseListAction();
    private AddExpenseAction addExpenseAction = new AddExpenseAction();
    private EditSelectedExpenseAction editSelectedExpenseAction = new EditSelectedExpenseAction();
    private DeleteSelectedExpenseAction deleteSelectedExpenseAction = new DeleteSelectedExpenseAction();

    @WTKX private TableView expenseTableView = null;

    private ExpenseSheet expenseSheet = null;

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
    public void initialize(Resources resource) {
        // Load the add/edit sheet
        try {
            Resources resources = new Resources(ExpenseSheet.class.getName());
            WTKXSerializer wtkxSerializer = new WTKXSerializer(resources);
            expenseSheet = (ExpenseSheet)wtkxSerializer.readObject(this, "expense_sheet.wtkx");
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }

        // Attach event listener(s)
        expenseTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener.Adapter() {
            @Override
            public void selectedRangesChanged(TableView tableView, Sequence<Span> previousSelectedRanges) {
                int selectedIndex = expenseTableView.getSelectedIndex();
                editSelectedExpenseAction.setEnabled(selectedIndex != -1);
                deleteSelectedExpenseAction.setEnabled(selectedIndex != -1);
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
        // TODO Show activity indicator and load expenses
    }

    @SuppressWarnings("unchecked")
    private void addExpense() {
        expenseSheet.clear();
        expenseSheet.open(ExpensesWindow.this, new SheetCloseListener() {
            @Override
            public void sheetClosed(Sheet sheet) {
                if (sheet.getResult()) {
                    // Add result to table
                    HashMap<String, Object> expense = new HashMap<String, Object>();
                    expenseSheet.store(expense);

                    // TODO POST expense to server and then add to table

                    List<Object> expenses = (List<Object>)expenseTableView.getTableData();
                    expenses.add(expense);
                }
            }
        });
    }

    private void updateSelectedExpense() {
        expenseSheet.load(expenseTableView.getSelectedRow());
        expenseSheet.open(ExpensesWindow.this, new SheetCloseListener() {
            @Override
            public void sheetClosed(Sheet sheet) {
                if (sheet.getResult()) {
                    HashMap<String, Object> expense = new HashMap<String, Object>();
                    expenseSheet.store(expense);

                    // TODO PUT expense to server and then update table
                }
            }
        });
    }

    private void deleteSelectedExpense() {
        // TODO DELETE expense from server and then remove from table
    }
}
