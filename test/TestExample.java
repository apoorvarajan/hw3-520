// package test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import javax.swing.*;
import java.awt.Color;
import java.awt.Component;

import javax.swing.table.TableColumn;
import javax.swing.table.TableCellRenderer;



import java.util.Date;
import java.util.List;
import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

import controller.ExpenseTrackerController;
import model.ExpenseTrackerModel;
import model.Transaction;
import view.ExpenseTrackerView;
import model.Filter.AmountFilter;
import model.Filter.CategoryFilter;


public class TestExample {
  
  private ExpenseTrackerModel model;
  private ExpenseTrackerView view;
  private ExpenseTrackerController controller;

  @Before
  public void setup() {
    model = new ExpenseTrackerModel();
    view = new ExpenseTrackerView();
    controller = new ExpenseTrackerController(model, view);
  }

    public double getTotalCost() {
        double totalCost = 0.0;
        List<Transaction> allTransactions = model.getTransactions(); // Using the model's getTransactions method
        for (Transaction transaction : allTransactions) {
            totalCost += transaction.getAmount();
        }
        return totalCost;
    }


    public void checkTransaction(double amount, String category, Transaction transaction) {
	assertEquals(amount, transaction.getAmount(), 0.01);
        assertEquals(category, transaction.getCategory());
        String transactionDateString = transaction.getTimestamp();
        Date transactionDate = null;
        try {
            transactionDate = Transaction.dateFormatter.parse(transactionDateString);
        }
        catch (ParseException pe) {
            pe.printStackTrace();
            transactionDate = null;
        }
        Date nowDate = new Date();
        assertNotNull(transactionDate);
        assertNotNull(nowDate);
        // They may differ by 60 ms
        assertTrue(nowDate.getTime() - transactionDate.getTime() < 60000);
    }


    @Test
    public void testAddTransaction() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add a transaction
	double amount = 50.0;
	String category = "food";
        assertTrue(controller.addTransaction(amount, category));
    
        // Post-condition: List of transactions contains only
	//                 the added transaction	
        assertEquals(1, model.getTransactions().size());
    
        // Check the contents of the list
	Transaction firstTransaction = model.getTransactions().get(0);
	checkTransaction(amount, category, firstTransaction);
	
	// Check the total amount
        assertEquals(amount, getTotalCost(), 0.01);
    }


    @Test
    public void testRemoveTransaction() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add and remove a transaction
	double amount = 50.0;
	String category = "food";
        Transaction addedTransaction = new Transaction(amount, category);
        model.addTransaction(addedTransaction);
    
        // Pre-condition: List of transactions contains only
	//                the added transaction
        assertEquals(1, model.getTransactions().size());
	Transaction firstTransaction = model.getTransactions().get(0);
	checkTransaction(amount, category, firstTransaction);

	assertEquals(amount, getTotalCost(), 0.01);
	
	// Perform the action: Remove the transaction
        model.removeTransaction(addedTransaction);
    
        // Post-condition: List of transactions is empty
        List<Transaction> transactions = model.getTransactions();
        assertEquals(0, transactions.size());
    
        // Check the total cost after removing the transaction
        double totalCost = getTotalCost();
        assertEquals(0.00, totalCost, 0.01);
    }
    @Test
    public void testAddTransactionWithValidInput() {
        double amount = 50.0;
        String category = "food";

        assertTrue(controller.addTransaction(amount, category));

        assertEquals(1, model.getTransactions().size());
        Transaction addedTransaction = model.getTransactions().get(0);
        checkTransaction(amount, category, addedTransaction);
        assertEquals(amount, getTotalCost(), 0.01);
    }
    @Test
    public void testInvalidInputHandling() {
        double invalidAmount = -10.0;
        String invalidCategory = "";

        assertFalse(controller.addTransaction(invalidAmount, invalidCategory));

        assertEquals(0, model.getTransactions().size());
        assertEquals(0.0, getTotalCost(), 0.01);
    }
    private Color getRowBackgroundColor(JTable table, int rowIndex) {
        TableColumn colorColumn = table.getColumnModel().getColumn(2);
        return (Color) table.getValueAt(rowIndex, colorColumn.getModelIndex());
    }
    @Test
    public void testFilterByAmount() {
        double amount1 = 30.0;
        double amount2 = 50.0;
        double amount3 = 70.0;

        controller.addTransaction(amount1, "food"); 
        controller.addTransaction(amount2, "food");
        controller.addTransaction(amount3, "travel");

        AmountFilter amountFilter = new AmountFilter(amount2);
        controller.setFilter(amountFilter);
        controller.applyFilter();

        JTable transactionTable = view.getTransactionsTable();
        TableCellRenderer renderer = transactionTable.getCellRenderer(1, 1);
        Component component = transactionTable.prepareRenderer(renderer, 1, 1);

        assertEquals(new Color(173, 255, 168), component.getBackground());
    }
    @Test
    public void testFilterByCategory() {
        String category1 = "food";
        String category2 = "travel";
        String category3 = "food";

        controller.addTransaction(30.0, category1);
        controller.addTransaction(50.0, category2);
        controller.addTransaction(70.0, category3);

        CategoryFilter categoryFilter = new CategoryFilter(category2);
        controller.setFilter(categoryFilter);
        controller.applyFilter();

        JTable transactionTable = view.getTransactionsTable();
        TableCellRenderer renderer = transactionTable.getCellRenderer(1, 1);
        Component component = transactionTable.prepareRenderer(renderer, 1, 1);

        assertEquals(new Color(173, 255, 168), component.getBackground());
    }
    @Test
    public void undoDisallowed() {
        List<Transaction> transactions = model.getTransactions();
        assertTrue("The list should be empty", transactions.isEmpty());
    }
    @Test
    public void undoAllowed() {
        String category1 = "food";
        String category2 = "travel";
        String category3 = "food";

        controller.addTransaction(30.0, category1);
        controller.addTransaction(50.0, category2);
        controller.addTransaction(70.0, category3);
        List<Transaction> transactionsbefore = model.getTransactions();
        view.getTransactionsTable().setRowSelectionInterval(0, 2);
        controller.undoTransaction(view.getTransactionsTable().getSelectedRows());
        List<Transaction> transactionsafter = model.getTransactions();
        assertNotEquals("The lists should not be equal", transactionsbefore, transactionsafter);
    }
}
