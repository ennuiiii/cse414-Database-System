import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;
import java.lang.*;
import java.util.*;

/**
 * Runs queries against a back-end database.
 * This class is responsible for searching for flights.
 */
public class QuerySearchOnly
{
  // `dbconn.properties` config file
  private String configFilename;

  // DB Connection
  protected Connection conn;

  // Canned queries
  private static final String CHECK_FLIGHT_CAPACITY = "SELECT capacity FROM Flights WHERE fid = ?";
  protected PreparedStatement checkFlightCapacityStatement;
  private static final String CHECK_DIRECT_FLIGHT = "SELECT TOP(?) * FROM Flights "
  + "WHERE origin_city = ? AND dest_city = ? AND day_of_month = ? "
  + "AND canceled <> 1 ORDER BY actual_time, fid ASC";
  protected PreparedStatement checkDirectFlightStatement;
  private static final String CHECK_INDIRECT_FLIGHT = "SELECT TOP(?) F1.fid AS F1_fid, F2.fid AS F2_fid, "
  + "F1.day_of_month AS F1_day_of_month, F2.day_of_month AS F2_day_of_month, "
  + "F1.carrier_id AS F1_carrier_id, F2.carrier_id AS F2_carrier_id, "
  + "F1.flight_num AS F1_flight_num, F2.flight_num AS F2_flight_num, "
  + "F1.origin_city AS F1_origin_city, F2.origin_city AS F2_origin_city, "
  + "F1.dest_city AS F1_dest_city, F2.dest_city AS F2_dest_city, "
  + "F1.actual_time AS F1_actual_time, F2.actual_time AS F2_actual_time, "
  + "F1.capacity AS F1_capacity, F2.capacity AS F2_capacity, "
  + "F1.price AS F1_price, F2.price AS F2_price "
  + "FROM Flights F1, Flights F2 "
  + "WHERE F1.origin_city = ? AND F1.dest_city = F2.origin_city AND F2.dest_city = ? "
  + "AND F1.day_of_month = ? AND F2.day_of_month = F1.day_of_month "
  + "AND F1.canceled <> 1 AND F2.canceled <> 1 "
  + "ORDER BY (F1.actual_time + F2.actual_time), F1.fid, F2.fid ASC";
  protected PreparedStatement checkIndirectFlightStatement;

  private List<Itinarary> curr;

  class Flight
  {
    public int fid;
    public int dayOfMonth;
    public String carrierId;
    public String flightNum;
    public String originCity;
    public String destCity;
    public int time;
    public int capacity;
    public int price;

    public Flight(int f, int d, String c, String fl, String ori, String des, int t, int ca, int p) {
      this.fid = f;
      this.dayOfMonth = d;
      this.carrierId = c;
      this.flightNum = fl;
      this.originCity = ori;
      this.destCity = des;
      this.time = t;
      this.capacity = ca;
      this.price = p;
    }

    @Override
    public String toString()
    {
      return "ID: " + fid + " Day: " + dayOfMonth + " Carrier: " + carrierId +
              " Number: " + flightNum + " Origin: " + originCity + " Dest: " + destCity + " Duration: " + time +
              " Capacity: " + capacity + " Price: " + price;
    }
  }

  class Itinarary implements Comparable<Itinarary>
  {
    public int time;
    Flight itinarary_1;
    Flight itinarary_2;

    public Itinarary(int t, Flight f1) {
      this.time = t;
      this.itinarary_1 = f1;
      this.itinarary_2 = null;
    }

    public Itinarary(int t, Flight f1, Flight f2) {
      this.time = t;
      this.itinarary_1 = f1;
      this.itinarary_2 = f2;
    }

    @Override
    public int compareTo(Itinarary other) {
      return this.time - other.time;
    }

    @Override
    public String toString() {
      StringBuffer sb = new StringBuffer();
      if (this.itinarary_2 == null) {
        sb.append(this.itinarary_1.toString()).append('\n');
      } else {
        sb.append(this.itinarary_1.toString()).append('\n').append(this.itinarary_2.toString()).append('\n');
      }
      return sb.toString();
    }

    public int flightnum() {
      if (itinarary_2 == null) {
        return 1;
      } else {
        return 2;
      }
    }
  }

  public QuerySearchOnly(String configFilename)
  {
    this.configFilename = configFilename;
  }

  /** Open a connection to SQL Server in Microsoft Azure.  */
  public void openConnection() throws Exception
  {
    Properties configProps = new Properties();
    configProps.load(new FileInputStream(configFilename));

    String jSQLDriver = configProps.getProperty("flightservice.jdbc_driver");
    String jSQLUrl = configProps.getProperty("flightservice.url");
    String jSQLUser = configProps.getProperty("flightservice.sqlazure_username");
    String jSQLPassword = configProps.getProperty("flightservice.sqlazure_password");

    /* load jdbc drivers */
    Class.forName(jSQLDriver).newInstance();

    /* open connections to the flights database */
    conn = DriverManager.getConnection(jSQLUrl, // database
            jSQLUser, // user
            jSQLPassword); // password

    conn.setAutoCommit(true); //by default automatically commit after each statement
    /* In the full Query class, you will also want to appropriately set the transaction's isolation level:
          conn.setTransactionIsolation(...)
       See Connection class's JavaDoc for details.
    */
  }

  public void closeConnection() throws Exception
  {
    conn.close();
  }

  /**
   * prepare all the SQL statements in this method.
   * "preparing" a statement is almost like compiling it.
   * Note that the parameters (with ?) are still not filled in
   */
  public void prepareStatements() throws Exception
  {
    checkFlightCapacityStatement = conn.prepareStatement(CHECK_FLIGHT_CAPACITY);
    checkDirectFlightStatement = conn.prepareStatement(CHECK_DIRECT_FLIGHT);
    checkIndirectFlightStatement = conn.prepareStatement(CHECK_INDIRECT_FLIGHT);
    /* add here more prepare statements for all the other queries you need */
    /* . . . . . . */
  }



  /**
   * Implement the search function.
   *
   * Searches for flights from the given origin city to the given destination
   * city, on the given day of the month. If {@code directFlight} is true, it only
   * searches for direct flights, otherwise it searches for direct flights
   * and flights with two "hops." Only searches for up to the number of
   * itineraries given by {@code numberOfItineraries}.
   *
   * The results are sorted based on total flight time.
   *
   * @param originCity
   * @param destinationCity
   * @param directFlight if true, then only search for direct flights, otherwise include indirect flights as well
   * @param dayOfMonth
   * @param numberOfItineraries number of itineraries to return
   *
   * @return If no itineraries were found, return "No flights match your selection\n".
   * If an error occurs, then return "Failed to search\n".
   *
   * Otherwise, the sorted itineraries printed in the following format:
   *
   * Itinerary [itinerary number]: [number of flights] flight(s), [total flight time] minutes\n
   * [first flight in itinerary]\n
   * ...
   * [last flight in itinerary]\n
   *
   * Each flight should be printed using the same format as in the {@code Flight} class. Itinerary numbers
   * in each search should always start from 0 and increase by 1.
   *
   * @see Flight#toString()
   */
  public String transaction_search(String originCity, String destinationCity, boolean directFlight, int dayOfMonth,
                                   int numberOfItineraries)
  {
    // Please implement your own (safe) version that uses prepared statements rather than string concatenation.
    // You may use the `Flight` class (defined above).
    // return transaction_search_unsafe(originCity, destinationCity, directFlight, dayOfMonth, numberOfItineraries);
    return transaction_search_safe(originCity, destinationCity, directFlight, dayOfMonth, numberOfItineraries);
  }

  private String transaction_search_safe(String originCity, String destinationCity, boolean directFlight,
                                          int dayOfMonth, int numberOfItineraries) {
    curr = new ArrayList<Itinarary>();
    StringBuffer sb = new StringBuffer();
    try {
      checkDirectFlightStatement.clearParameters();
      checkDirectFlightStatement.setInt(1, numberOfItineraries);
      checkDirectFlightStatement.setString(2, originCity);
      checkDirectFlightStatement.setString(3, destinationCity);
      checkDirectFlightStatement.setInt(4, dayOfMonth);
      ResultSet result = checkDirectFlightStatement.executeQuery();
      if (directFlight) {
        // int count = numberOfItineraries;
        while (result.next()) {
          // sb.append("Itinerary ").append(numberOfItineraries - count).append(": 1 flight(s), ");
          Flight Itinerary = new Flight(result.getInt("fid"), result.getInt("day_of_month"), result.getString("carrier_id"),
                                          result.getString("flight_num"), result.getString("origin_city"), result.getString("dest_city"),
                                          result.getInt("actual_time"), result.getInt("capacity"), result.getInt("price"));
          curr.add(new Itinarary(Itinerary.time, Itinerary));
          // sb.append(Itinerary.time).append(" minutes\n").append(Itinerary.toString()).append('\n');
          // count--;
        }
        set_string_buffer(sb, numberOfItineraries);
      } else {
        checkIndirectFlightStatement.clearParameters();
        checkIndirectFlightStatement.setInt(1, numberOfItineraries);
        checkIndirectFlightStatement.setString(2, originCity);
        checkIndirectFlightStatement.setString(3, destinationCity);
        checkIndirectFlightStatement.setInt(4, dayOfMonth);
        ResultSet results = checkIndirectFlightStatement.executeQuery();
        // List<Itinarary> curr = new ArrayList<Itinarary>();
        while (result.next()) {
          Flight Itinerary = new Flight(result.getInt("fid"), result.getInt("day_of_month"), result.getString("carrier_id"),
                                          result.getString("flight_num"), result.getString("origin_city"), result.getString("dest_city"),
                                          result.getInt("actual_time"), result.getInt("capacity"), result.getInt("price"));
          Itinarary temp = new Itinarary(Itinerary.time, Itinerary);
          curr.add(temp);
        }
        while (results.next() && curr.size() != numberOfItineraries) {
          Flight f1 = new Flight(results.getInt("F1_fid"), results.getInt("F1_day_of_month"), results.getString("F1_carrier_id"),
                                          results.getString("F1_flight_num"), results.getString("F1_origin_city"), results.getString("F1_dest_city"),
                                          results.getInt("F1_actual_time"), results.getInt("F1_capacity"), results.getInt("F1_price"));
          Flight f2 = new Flight(results.getInt("F2_fid"), results.getInt("F2_day_of_month"), results.getString("F2_carrier_id"),
                                          results.getString("F2_flight_num"), results.getString("F2_origin_city"), results.getString("F2_dest_city"),
                                          results.getInt("F2_actual_time"), results.getInt("F2_capacity"), results.getInt("F2_price"));
          Itinarary temp = new Itinarary(f1.time + f2.time, f1, f2);
          curr.add(temp);
        }
        Collections.sort(curr);
        set_string_buffer(sb, numberOfItineraries);
      }
    } catch (SQLException e) {return "Failed to search\n";
    // e.printStackTrace();
    }
    if (sb.toString().length() == 0) {
      return "No flights match your selection\n";
    }
    return sb.toString();
  }

  private void set_string_buffer(StringBuffer sb, int numberOfItineraries) {
    for (int i = 0; i < Math.min(numberOfItineraries, curr.size()); i++) {
      sb.append("Itinerary ").append(i).append(": " + curr.get(i).flightnum() + " flight(s), ").append(curr.get(i).time + " minutes\n").append(curr.get(i).toString());
    }
  }
  /**
   * Same as {@code transaction_search} except that it only performs single hop search and
   * do it in an unsafe manner.
   *
   * @param originCity
   * @param destinationCity
   * @param directFlight
   * @param dayOfMonth
   * @param numberOfItineraries
   *
   * @return The search results. Note that this implementation *does not conform* to the format required by
   * {@code transaction_search}.
   */
  private String transaction_search_unsafe(String originCity, String destinationCity, boolean directFlight,
                                          int dayOfMonth, int numberOfItineraries)
  {
    StringBuffer sb = new StringBuffer();

    try
    {
      // one hop itineraries
      String unsafeSearchSQL =
              "SELECT TOP (" + numberOfItineraries + ") day_of_month,carrier_id,flight_num,origin_city,dest_city,actual_time,capacity,price "
                      + "FROM Flights "
                      + "WHERE origin_city = \'" + originCity + "\' AND dest_city = \'" + destinationCity + "\' AND day_of_month =  " + dayOfMonth + " "
                      + "ORDER BY actual_time ASC";

      Statement searchStatement = conn.createStatement();
      ResultSet oneHopResults = searchStatement.executeQuery(unsafeSearchSQL);

      while (oneHopResults.next())
      {
        int result_dayOfMonth = oneHopResults.getInt("day_of_month");
        String result_carrierId = oneHopResults.getString("carrier_id");
        String result_flightNum = oneHopResults.getString("flight_num");
        String result_originCity = oneHopResults.getString("origin_city");
        String result_destCity = oneHopResults.getString("dest_city");
        int result_time = oneHopResults.getInt("actual_time");
        int result_capacity = oneHopResults.getInt("capacity");
        int result_price = oneHopResults.getInt("price");

        sb.append("Day: ").append(result_dayOfMonth)
                .append(" Carrier: ").append(result_carrierId)
                .append(" Number: ").append(result_flightNum)
                .append(" Origin: ").append(result_originCity)
                .append(" Destination: ").append(result_destCity)
                .append(" Duration: ").append(result_time)
                .append(" Capacity: ").append(result_capacity)
                .append(" Price: ").append(result_price)
                .append('\n');
      }
      oneHopResults.close();
    } catch (SQLException e) { e.printStackTrace();    }

    return sb.toString();
  }

  /**
   * Shows an example of using PreparedStatements after setting arguments.
   * You don't need to use this method if you don't want to.
   */
  private int checkFlightCapacity(int fid) throws SQLException
  {
    checkFlightCapacityStatement.clearParameters();
    checkFlightCapacityStatement.setInt(1, fid);
    ResultSet results = checkFlightCapacityStatement.executeQuery();
    results.next();
    int capacity = results.getInt("capacity");
    results.close();

    return capacity;
  }
}
