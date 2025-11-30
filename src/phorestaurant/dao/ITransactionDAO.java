package phorestaurant.dao;

import phorestaurant.model.Transaction;
import java.util.List;

public interface ITransactionDAO {
	boolean saveTransaction(Transaction trans);
	double getTotalRevenue();
	List<Transaction> getAllTransactions();
}
