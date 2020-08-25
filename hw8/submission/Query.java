import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;
import java.lang.*;
import java.util.*;

// write-read conflict
// read-write conflict
// write-write conflict
/*
Atomic : change of the database should be none or all
Consistent : when multiple applications, the database should be all consistent
Isolated : when multiple transactions, their effect is as if each of then running alone
Durable : the effect should be persist after the transaction finished
*/
/*
BEGIN TRANSACTION
	[SQL statements]
COMMIT or
ROLLBACK
*/
/*
pessimistic - locking scheduler
optimistic - multiversion concurrency control
LOCKING SCHEDULER: lock-based implementation of transactions
2PL: acquiring the locks before unlocking
Strict 2PL:
All locks are held until commit/abort
All unlocks are done together with commit/abort
ensure conflict serializability and recoverbility
*/
/*
Dead lock:
T1 : R(A), W(B)
T2 : R(B), W(A)
T1: holds the lock on A, waits for B
T2: holds the lock on B, waits for A
*/
/*
sahred lock: for read
exclusive lock: for write
*/

public class Query extends QuerySearchOnly {

	// Logged In User
	private String username; // customer username is unique

	// transactions
	private static final String BEGIN_TRANSACTION_SQL = "SET TRANSACTION ISOLATION LEVEL SERIALIZABLE; BEGIN TRANSACTION;";
	protected PreparedStatement beginTransactionStatement;

	private static final String COMMIT_SQL = "COMMIT TRANSACTION";
	protected PreparedStatement commitTransactionStatement;

	private static final String ROLLBACK_SQL = "ROLLBACK TRANSACTION";
	protected PreparedStatement rollbackTransactionStatement;

	private static final String CREATE_USER = "INSERT INTO USERS VALUES (?, ?, ?)";
	protected PreparedStatement createUserStatement;

	private static final String CHECK_VALID_USER = "SELECT * FROM USERS WHERE username = ?";
	protected PreparedStatement checkValidUserStatement;

	private static final String CHECK_PASSWORD = "SELECT * FROM USERS WHERE username = ? AND password = ?";
	protected PreparedStatement checkPasswordStatement;

	private static final String SEARCH_RESERVATION = "SELECT * FROM RESERVATION WHERE user_id = ?";
	protected PreparedStatement searchReservationStatement;

	private static final String CHECK_VALID_BOOK_DAY = "SELECT * FROM RESERVATION WHERE user_id = ? AND day = ?";
	protected PreparedStatement checkValidDayStatement;

	private static final String CHECK_CURRENT_RESERVE = "SELECT COUNT(*) AS count FROM RESERVATION WHERE fid_1 = ? OR fid_2 = ?";
	protected PreparedStatement checkCapacityStatement;

	private static final String UPDATE_RESERVATION = "INSERT INTO RESERVATION VALUES (?,?,?,?,0,?,?)";
	protected PreparedStatement updateRervationStatement;

	private static final String GET_RESERVATION_ID = "SELECT COUNT(*) AS count FROM RESERVATION";
	protected PreparedStatement getReservationIdStatement;

	private static final String CHECK_RESERVATIONS = "SELECT * FROM RESERVATION WHERE user_id = ? AND rid = ?";
	protected PreparedStatement checkReservationStatement;

	private static final String CHECK_BALANCE = "SELECT balance FROM USERS WHERE username = ?";
	protected PreparedStatement checkBalanceStatement;

	private static final String UPDATE_BALANCE  = "UPDATE Users SET balance = ? WHERE username = ?";
	protected PreparedStatement updateBalanceStatement;

	private static final String UPDATE_PAY = "UPDATE RESERVATION SET paid = 1 WHERE rid = ?";
	protected PreparedStatement updatePayStatement;

	private static final String GET_FLIGHT = "SELECT * FROM Flights WHERE fid = ?";
	protected PreparedStatement getFlightStatement;

	private static final String GET_RESERVATION_COUNT = "SELECT COUNT(*) as count FROM RESERVATION WHERE user_id = ?";
	protected PreparedStatement getResCountStatement;

	private static final String CLEAR = "DELETE FROM Reservation;"
	+" DELETE FROM Users;"
	+" INSERT INTO Reservation VALUES (0,null,null,null,null,null,null)";
	protected PreparedStatement clearStatement;
	public Query(String configFilename) {
		super(configFilename);
	}


	/**
	 * Clear the data in any custom tables created. Do not drop any tables and do not
	 * clear the flights table. You should clear any tables you use to store reservations
	 * and reset the next reservation ID to be 1.
	 */
	public void clearTables ()
	{
		// your code here
		try {
			clearStatement.executeUpdate();
		} catch (SQLException e) {e.printStackTrace();}
	}


	/**
	 * prepare all the SQL statements in this method.
	 * "preparing" a statement is almost like compiling it.
	 * Note that the parameters (with ?) are still not filled in
	 */
	@Override
	public void prepareStatements() throws Exception
	{
		super.prepareStatements();
		beginTransactionStatement = conn.prepareStatement(BEGIN_TRANSACTION_SQL);
		commitTransactionStatement = conn.prepareStatement(COMMIT_SQL);
		rollbackTransactionStatement = conn.prepareStatement(ROLLBACK_SQL);
		createUserStatement = conn.prepareStatement(CREATE_USER);
		checkValidUserStatement = conn.prepareStatement(CHECK_VALID_USER);
		checkPasswordStatement = conn.prepareStatement(CHECK_PASSWORD);
		searchReservationStatement = conn.prepareStatement(SEARCH_RESERVATION);
		checkValidDayStatement = conn.prepareStatement(CHECK_VALID_BOOK_DAY);
		checkCapacityStatement = conn.prepareStatement(CHECK_CURRENT_RESERVE);
		updateRervationStatement = conn.prepareStatement(UPDATE_RESERVATION);
		getReservationIdStatement = conn.prepareStatement(GET_RESERVATION_ID);
		checkReservationStatement = conn.prepareStatement(CHECK_RESERVATIONS);
		checkBalanceStatement = conn.prepareStatement(CHECK_BALANCE);
		updatePayStatement = conn.prepareStatement(UPDATE_PAY);
		updateBalanceStatement = conn.prepareStatement(UPDATE_BALANCE);
		getFlightStatement = conn.prepareStatement(GET_FLIGHT);
		clearStatement = conn.prepareStatement(CLEAR);
		getResCountStatement = conn.prepareStatement(GET_RESERVATION_COUNT);

		/* add here more prepare statements for all the other queries you need */
		/* . . . . . . */
	}


	/**
	 * Takes a user's username and password and attempts to log the user in.
	 *
	 * @return If someone has already logged in, then return "User already logged in\n"
	 * For all other errors, return "Login failed\n".
	 *
	 * Otherwise, return "Logged in as [username]\n".
	 */
	public String transaction_login(String username, String password)
	{
		if (this.username != null) {
			return "User already logged in\n";
		} else {
			try {
				checkPasswordStatement.clearParameters();
				checkPasswordStatement.setString(1, username);
				checkPasswordStatement.setString(2, password);
				ResultSet result = checkPasswordStatement.executeQuery();
				if (result.next()) {
					// password match username
					this.username = username;
					return "Logged in as " + username + "\n";
				} else {
					// wrong password or wrong username
					return "Login failed\n";
				}
			} catch(SQLException e) {
				// e.printStackTrace();
				return "Login failed\n";
			}
		}
	}

	/**
	 * Implement the create user function.
	 *
	 * @param username new user's username. User names are unique the system.
	 * @param password new user's password.
	 * @param initAmount initial amount to deposit into the user's account, should be >= 0 (failure otherwise).
	 *
	 * @return either "Created user {@code username}\n" or "Failed to create user\n" if failed.
	 */
	public String transaction_createCustomer (String username, String password, int initAmount)
	{
		if (initAmount < 0) {
			return "Failed to create user\n";
		} else {
			try {
				beginTransaction();
				checkValidUserStatement.clearParameters();
				checkValidUserStatement.setString(1, username);
				ResultSet result = checkValidUserStatement.executeQuery();
				if (result.next()) {
					// Username already exist
					rollbackTransaction();
					return "Failed to create user\n";
				} else {
					try {
						createUserStatement.clearParameters();
						createUserStatement.setString(1, username);
						createUserStatement.setString(2, password);
						createUserStatement.setInt(3, initAmount);
						createUserStatement.executeUpdate();
						commitTransaction();
						return "Created user " + username + "\n";
					} catch (SQLException e) {
						// deadlock, abort and try again
						rollbackTransaction();
						// e.printStackTrace();
						return transaction_createCustomer(username, password, initAmount);
					}
				}
			} catch(SQLException e) {
				// e.printStackTrace();
				return "Failed to create user\n";
			}
		}
	}

	/**
	 * Implements the book itinerary function.
	 *
	 * @param itineraryId ID of the itinerary to book. This must be one that is returned by search in the current session.
	 *
	 * @return If the user is not logged in, then return "Cannot book reservations, not logged in\n".
	 * If try to book an itinerary with invalid ID, then return "No such itinerary {@code itineraryId}\n".
	 * If the user already has a reservation on the same day as the one that they are trying to book now, then return
	 * "You cannot book two flights in the same day\n".
	 * For all other errors, return "Booking failed\n".
	 *
	 * And if booking succeeded, return "Booked flight(s), reservation ID: [reservationId]\n" where
	 * reservationId is a unique number in the reservation system that starts from 1 and increments by 1 each time a
	 * successful reservation is made by any user in the system.
	 */
	public String transaction_book(int itineraryId)
	{
		if (this.username == null) {
			// not log in
			return "Cannot book reservations, not logged in\n";
		} else if (itineraryId < 0 || itineraryId > curr.size()){
			// illegal input
			return "No such itinerary " + itineraryId + "\n";
		} else {
			Itinarary target = curr.get(itineraryId);
			int day = target.itinarary_1.dayOfMonth;
			try {
				beginTransaction();
				try{
					checkValidDayStatement.clearParameters();
					checkValidDayStatement.setString(1, this.username);
					checkValidDayStatement.setInt(2, day);
					ResultSet res1 = checkValidDayStatement.executeQuery();
					if (res1.next()) {
						// have reservation on the same day
						rollbackTransaction();
						return "You cannot book two flights in the same day\n";
					}
					res1.close();
				} catch (SQLException deadlock) {
					return "Booking failed\n";
				}
				// don't have reservation on the same day, continue
				// check first flight capacity, check the second flight capacity if exist
				Flight f1 = target.itinarary_1;
				Flight f2 = target.itinarary_2;
				Boolean canbook = false;
				// check first flight capacity
				int fid1 = f1.fid;
				int fid2 = -1;
				int cap1 = f1.capacity;
				int currentRes1 = 0;
				try {
					checkCapacityStatement.clearParameters();
					checkCapacityStatement.setInt(1, fid1);
					checkCapacityStatement.setInt(2, fid1);
					ResultSet res2 = checkCapacityStatement.executeQuery();
					if (res2.next()) {
						currentRes1 = res2.getInt("count");
					}
					res2.close();
				} catch (SQLException deadlock) {
					return "Booking failed\n";
				}
				if (f2 != null) {
					fid2 = f2.fid;
					int cap2 = f2.capacity;
					int currentRes2 = 0;
					try {
						checkCapacityStatement.clearParameters();
						checkCapacityStatement.setInt(1, fid2);
						checkCapacityStatement.setInt(2, fid2);
						ResultSet res3 = checkCapacityStatement.executeQuery();
						if (res3.next()) {
							currentRes2 = res3.getInt("count");
						}
						res3.close();
					} catch (SQLException deadlock) {
						return "Booking failed\n";
					}
					if (cap1 - currentRes1 > 0 && cap2 - currentRes2 > 0) {
						canbook = true;
					}
				} else if (cap1 - currentRes1 > 0) {
					canbook = true;
				}
				if (canbook) {
					// both flights have spaces
					// proceed to make reservation
					ResultSet res4 = getReservationIdStatement.executeQuery();
					int id = 0;
					if (res4.next()) {
						id = res4.getInt("count");
					}
					res4.close();
					updateRervationStatement.clearParameters();
					updateRervationStatement.setInt(1, id);
					updateRervationStatement.setString(2, this.username);
					updateRervationStatement.setInt(3, fid1);
					updateRervationStatement.setInt(4, fid2);
					if (f2 != null) {
						updateRervationStatement.setInt(5, f1.price + f2.price);
					} else {
						updateRervationStatement.setInt(5, f1.price);
					}
					updateRervationStatement.setInt(6, day);
					try {
						updateRervationStatement.executeUpdate();
					} catch (SQLException e) {
						rollbackTransaction();
						return "Booking failed\n";
					}
					commitTransaction();
					return "Booked flight(s), reservation ID: " + id + "\n";
				} else {
					// at least 1 of the two flights is full
					rollbackTransaction();
					return "Booking failed\n";
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return "Booking failed\n";
			}
		}
	}

	/**
	 * Implements the pay function.
	 *
	 * @param reservationId the reservation to pay for.
	 *
	 * @return If no user has logged in, then return "Cannot pay, not logged in\n"
	 * If the reservation is not found / not under the logged in user's name, then return
	 * "Cannot find unpaid reservation [reservationId] under user: [username]\n"
	 * If the user does not have enough money in their account, then return
	 * "User has only [balance] in account but itinerary costs [cost]\n"
	 * For all other errors, return "Failed to pay for reservation [reservationId]\n"
	 *
	 * If successful, return "Paid reservation: [reservationId] remaining balance: [balance]\n"
	 * where [balance] is the remaining balance in the user's account.
	 */
	public String transaction_pay (int reservationId)
	{
		if (this.username == null) {
			return "Cannot pay, not logged in\n";
		}
		try {
			beginTransaction();
			checkReservationStatement.clearParameters();
			checkReservationStatement.setString(1, this.username);
			checkReservationStatement.setInt(2, reservationId);
			ResultSet res1 = checkReservationStatement.executeQuery();
			if (res1.next()) {
				// there is a reservation of id [reservationId] under this user
				int price = res1.getInt("price");
				int paid = res1.getInt("paid");
				if (paid == 1) {
					// already paid
					commitTransaction();
					return "Cannot find unpaid reservation " + reservationId + " under user: " + this.username + "\n";
				}
				int balance = 0;
				// check user's balance
				checkBalanceStatement.clearParameters();
				checkBalanceStatement.setString(1, this.username);
				ResultSet res2 = checkBalanceStatement.executeQuery();
				if (res2.next()) {
					balance = res2.getInt("balance");
				}
				res2.close();
				if (balance >= price) {
					// can pay, update the user table and reservation table
					// update user's balance
					try {
						updateBalanceStatement.clearParameters();
						updateBalanceStatement.setInt(1, balance - price);
						updateBalanceStatement.setString(2, this.username);
						updateBalanceStatement.executeUpdate();
						updatePayStatement.clearParameters();
						updatePayStatement.setInt(1, reservationId);
						updatePayStatement.executeUpdate();
						commitTransaction();
						return "Paid reservation: " + reservationId + " remaining balance: " + (balance - price) + "\n";
					} catch (SQLException e) {
						// e.printStackTrace();
						return "Failed to pay for reservation " + reservationId + "\n";
					}
				} else {
					// cannot pay
					rollbackTransaction();
					return "User has only " + balance + " in account but itinerary costs " + price + "\n";
				}
			} else {
				rollbackTransaction();
				return "Cannot find unpaid reservation " + reservationId + " under user: " + this.username + "\n";
			}
		} catch (SQLException e) {
			// e.printStackTrace();
			return "Failed to pay for reservation " + reservationId + "\n";
		}
		// return "Failed to pay for reservation " + reservationId + "\n";
	}

	/**
	 * Implements the reservations function.
	 *
	 * @return If no user has logged in, then return "Cannot view reservations, not logged in\n"
	 * If the user has no reservations, then return "No reservations found\n"
	 * For all other errors, return "Failed to retrieve reservations\n"
	 *
	 * Otherwise return the reservations in the following format:
	 *
	 * Reservation [reservation ID] paid: [true or false]:\n"
	 * [flight 1 under the reservation]
	 * [flight 2 under the reservation]
	 * Reservation [reservation ID] paid: [true or false]:\n"
	 * [flight 1 under the reservation]
	 * [flight 2 under the reservation]
	 * ...
	 *
	 * Each flight should be printed using the same format as in the {@code Flight} class.
	 *
	 * @see Flight#toString()
	 */
	public String transaction_reservations()
	{
		StringBuffer sb = new StringBuffer();
		if (this.username == null) {
			return "Cannot view reservations, not logged in\n";
		} else {
			try {
				getResCountStatement.clearParameters();
				getResCountStatement.setString(1, this.username);
				ResultSet re = getResCountStatement.executeQuery();
				re.next();
				int count = re.getInt("count");
				if (count == 0) {
					return "No reservations found\n";
				}
				searchReservationStatement.clearParameters();
				searchReservationStatement.setString(1, this.username);
				ResultSet result = searchReservationStatement.executeQuery();
				while (result.next()) {
					// reservation is not empty
					int id = result.getInt("rid");
					int p  = result.getInt("paid");
					Boolean paid = (p == 1);
					sb.append("Reservation " + id + " paid: " + paid + ":\n");
					int fid1 = result.getInt("fid_1");
					int fid2 = result.getInt("fid_2");
					Flight f1 = null;
					Flight f2 = null;
					getFlightStatement.clearParameters();
					getFlightStatement.setInt(1, fid1);
					ResultSet res = getFlightStatement.executeQuery();
					if (res.next()) {
						f1 = new Flight(res.getInt("fid"), res.getInt("day_of_month"), res.getString("carrier_id"),
	                                res.getString("flight_num"), res.getString("origin_city"), res.getString("dest_city"),
	                                res.getInt("actual_time"), res.getInt("capacity"), res.getInt("price"));
					}
					res.close();
					sb.append(f1.toString()).append("\n");
					if (fid2 != -1) {
						getFlightStatement.clearParameters();
						getFlightStatement.setInt(1, fid2);
						ResultSet res1 = getFlightStatement.executeQuery();
						if (res1.next()) {
							f2 = new Flight(res1.getInt("fid"), res1.getInt("day_of_month"), res1.getString("carrier_id"),
		                                res1.getString("flight_num"), res1.getString("origin_city"), res1.getString("dest_city"),
		                                res1.getInt("actual_time"), res1.getInt("capacity"), res1.getInt("price"));
						}
						res1.close();
						sb.append(f2.toString()).append("\n");
					}
				}
				return sb.toString();
			} catch (SQLException e) {
				e.printStackTrace();
				return "Failed to retrieve reservations\n";}
		}

	}

	/**
	 * Implements the cancel operation.
	 *
	 * @param reservationId the reservation ID to cancel
	 *
	 * @return If no user has logged in, then return "Cannot cancel reservations, not logged in\n"
	 * For all other errors, return "Failed to cancel reservation [reservationId]"
	 *
	 * If successful, return "Canceled reservation [reservationId]"
	 *
	 * Even though a reservation has been canceled, its ID should not be reused by the system.
	 */
	public String transaction_cancel(int reservationId)
	{
		// only implement this if you are interested in earning extra credit for the HW!
		return "Failed to cancel reservation " + reservationId;
	}


	/* some utility functions below */

	public void beginTransaction() throws SQLException
	{
		conn.setAutoCommit(false);
		beginTransactionStatement.executeUpdate();
	}

	public void commitTransaction() throws SQLException
	{
		commitTransactionStatement.executeUpdate();
		conn.setAutoCommit(true);
	}

	public void rollbackTransaction() throws SQLException
	{
		rollbackTransactionStatement.executeUpdate();
		conn.setAutoCommit(true);
	}
}
