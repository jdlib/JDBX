package org.jdbx.demo;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.jdbx.*;


/**
 * DocSnippets contains all code snippets from UserGuide.md
 * to make sure that they compile.
 */
@SuppressWarnings({"unused","resource"})
public class DocSnippets
{
	public List<City> readmeEx1Jdbc() throws SQLException
	{
		List<City> list = new ArrayList<>();
	    try (Statement stmt = con.createStatement()) {
	        ResultSet result = stmt.executeQuery("SELECT * FROM Cities ORDER BY name");
	        while (result.next())
	            list.add(City.read(result));
	    }
	    return list;
	}


	public List<City> readmeEx2Jdbc() throws SQLException
	{
		return Jdbx.query(con, "SELECT * FROM Cities ORDER BY name").rows().read(City::read);
	}


	public Integer readmeEx2Jdbc(Connection con, String firstName, String lastName)
	{
	    try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO Users VALUES (DEFAULT, ?, ?)",
	        new String[] { "id" })) {
	        pstmt.setString(1, firstName);
	        pstmt.setString(2, lastName);
	        if (pstmt.executeUpdate() != 1)
	            throw new IllegalStateException("insert failed");
	        Integer id = null;
	        ResultSet result = pstmt.getGeneratedKeys();
	        if (result.next())
	            id = result.getObject(1, Integer.class);
	        if (id == null)
	            throw new IllegalStateException("id not returned");
	        return id;
	    }
	    catch (SQLException e) {
	        throw new IllegalStateException("sql error", e);
	    }
	}


	public Integer readmeEx2Jdbx(Connection con, String firstName, String lastName)
	{
		try (PrepStmt pstmt = new PrepStmt(con)) {
			return pstmt.init().returnCols("id").sql("INSERT INTO Users VALUES (DEFAULT, ?, ?)")
				.params(firstName, lastName)
				.createUpdate().runGetCol(Integer.class).requireCount(1).requireValue();
		}
	}


	public void stmtsCreateClose()
	{
		// createing
		new StaticStmt(con);
		new PrepStmt(con);
		new CallStmt(con);
		new StaticStmt(ds);
		new PrepStmt(ds);
		new CallStmt(ds);

		// close
		try (StaticStmt stmt = new StaticStmt(con)) {
		}

		// configure
	    stmt.options()
	        .setQueryTimeoutSeconds(20)
	        .setFetchRows(5000)
	        .setResultType(ResultType.SCROLL_SENSITIVE)
	        .setResultConcurrency(Concurrency.READ_ONLY);
	    int seconds = stmt.options().getQueryTimeoutSeconds();
	}


	public void stmtsInit()
	{
		pstmt.init("INSERT INTO Users VALUES (DEFAULT, ?, ?)");
		cstmt.init("{call getUserName(?, ?)}");

		pstmt.init().returnCols("id").sql("INSERT INTO Users VALUES (DEFAULT, ?, ?)");
		pstmt.init().namedParams().sql("UPDATE Users SET name = :name");
	}


	public void stmtsOptions()
	{
		stmt.options().setQueryTimeoutSeconds(20).setFetchRows(5000);
		int timeoutSecs = stmt.options().getQueryTimeoutSeconds();
	}


	public void stmtsParams()
	{
		pstmt.param(1).setString("John");
		pstmt.param(2).setString("Doe");
		pstmt.param(2).setInt(42);
	    pstmt.param(1, "John").param(2, "Doe");

		pstmt.params("John", "Doe");

	    pstmt.init().namedParams().sql("INSERT INTO Users VALUES (DEFAULT, :lastname, :firstname, :lastname + ', ' + :firstname)");
	    pstmt.param("lastname").setString("John");
	    pstmt.param("firstname").setString("Doe");

		cstmt.init("{call GetUserName(?,?,?)}")
			.registerOutParam(2).as(java.sql.Types.VARCHAR)
			.registerOutParam(3).as(java.sql.Types.VARCHAR);
		cstmt.param(1).setLong(831L);
		cstmt.execute(); // explained in next chapters
		String lastName  = cstmt.param(2).getString();
		String firstName = cstmt.param(3).getString();

	    pstmt.clearParams();
	    cstmt.clearParams();
	}


	public List<City> queryRunningEx1() throws Exception
	{
		String sql = "SELECT * FROM Cities ORDER BY name";

		if (jdbc)
		{
			// JDBC:
			Statement stmt = con.createStatement();
			ResultSet result = stmt.executeQuery(sql);
			List<City> cities = new ArrayList<>();
			while (result.next()) {
			    City city = City.read(result);
			    cities.add(city);
			}
			return cities;
		}
		else {
			// JDBX
			try (StaticStmt stmt = new StaticStmt(con)) {
				return stmt.query(sql).rows().read(City::read);
			}
		}
	}


	public String queryRunningEx2() throws Exception
	{
		String sql = "SELECT name FROM Cities WHERE code = ?";

		if (jdbc)
		{
			// JDBC:
			try (PreparedStatement pstmt = con.prepareStatement(sql)) {
				pstmt.setString(1, "MUC");
				ResultSet result = pstmt.executeQuery();
				String name = null;
				if (result.next())
				    name = result.getString(1);
				return name;
			}
		}
		else
		{
			// JDBX
			try (PrepStmt pstmt = new PrepStmt(con)) {
				return pstmt.init(sql).params("MUC").query().row().col().getString();
			}
		}
	}


	public void queryClass() throws Exception
	{
		q = stmt.query(sql);
		q = pstmt.init(sql).params("a", "b").query();
	}


	public void querySingleRow() throws Exception
	{
		q.row().col();                 // returns a builder to retrieve a value of the first column
		q.row().col().getString();     // returns the value of the first column as String
		q.row().col(3);                // returns a builder to retrieve a value of the third column
		q.row().col(3).getInteger();   // returns the value of the third column as Integer
		q.row().col("sort");           // returns a builder to retrieve a value of the "sort" column
		q.row().col("sort").getInt();  // returns the value of "sort" column as int
		q.row().cols();                // returns a builder to retrieve the value of all columns
		q.row().cols().toList();       // returns the value of all columns, as List<Object>
		q.row().cols(1,3,7);           // returns a builder to retrieve the value of columns 1, 3, 7
		q.row().cols(1,3,7).toArray(); // returns the value of columns 1,3,7, as Object[]
		q.row().cols("a", "b", "c");    // returns a builder to retrieve the value of columns "a", "b", "c"
		q.row().cols("a", "b", "c").toMap();  // returns a Map<String,Object> mapping column name to value
		q.row().read(City::read);      // returns the value returned by the reader function

		q.row().required().col().getString();
		q.row().unique().col().getString();
	}


	public void queryAllRows() throws Exception
	{
		q.rows();
		q.rows().col();                    // return values of first column
		q.rows().col().getString();        // return values of first column as List<String>
		q.rows().col(3);                   // return values of column by column number
		q.rows().col(3).getDouble();       // return values of third column, as List<Double>
		q.rows().col("sort");              // return values of column by name
		q.rows().col("sort").getInteger(); // return values of "sort" column, as List<Integer>
		q.rows().cols();                   // returns a builder to retrieve the value of all columns
	    q.rows().cols().toList();          // returns the value of all columns, as List<List<Object>>
	    q.rows().cols(1,3,7);              // returns a builder to retrieve the value of columns 1, 3, 7
	    q.rows().cols(1,3,7).toArray();    // returns the value of columns 1,3,7, as List<Object[]>
	    q.rows().cols("a", "b", "c");      // returns a builder to retrieve the value of columns "a", "b", "c"
	    q.rows().cols("a", "b", "c").toMap();  // returns a List<Map<String,Object>>, each mapping column name to value
		q.rows().read(City::read);         // returns a List of values returned by the reader function

		q.rows().max(5);
		q.skip(3).rows();
	}


	public void queryresult() throws Exception
	{
        qr.col();                    // first column
        qr.col().getString();        // first column as String
        qr.col(3);                   // column by number
        qr.col(3).getDouble();       // third column as double
        qr.col("sort");              // column by name
        qr.col("sort").getInteger(); // "sort" column, as Integer
        qr.cols();                   // a builder to access all columns
        qr.cols().toMap();           // returns a Map<String,Object>
        qr.cols(1,3,7).toList();     // columns 1,3,7, as List<Object>
        qr.cols(1,3,7).toArray();    // columns 1,3,7, as Object[]

		q.row().read(City::read);
		q.rows().read(City::read);
}


    public static class City1
    {
        public static City1 read(QueryResult qc) {
            City1 city = new City1();
            city.setCode(qc.col(1).getString());
            city.setName(qc.col(2).getString());
            return city;
        }

        public void setCode(String value)
        {
        }

        public void setName(String value)
        {
        }
    }


	public void queryResultNav() throws Exception
	{
		while (qr.nextRow()) {
		    // read the result row
		}

		stmt.options().setResultType(ResultType.SCROLL_SENSITIVE).setResultConcurrency(Concurrency.UPDATABLE);

		// qr is obtained from stmt
		qr = stmt.query("sql").result();
		qr.position().isBeforeFirst();
		// also: .isAfterLast(), .isLast()

		qr.move().first() ;
		qr.move().absolute(5);
		qr.move().relative(2);
		// also: .relative(), .afterLast(), .beforeFirst(), .first(), .etc.

		qr.row().update();
		qr.row().refresh();
		// also: .insert(), .isUpdated(), .delete(), .isDeleted(), etc.
	}


	public void queryResultObtain() throws Exception
	{
		stmt.options().setResultConcurrency(Concurrency.UPDATABLE);

	   	qr.col("status").setString("ok");
	    qr.row().update();
	    qr.row().refresh();
	}


	public void update() throws Exception
	{
		stmt.update(sql);
		pstmt.update();
	}


	public void updateRun() throws Exception
	{
		long count;

   		count = stmt.update(null).count();
   	   	count = pstmt.update().count();

   	   	stmt.update(null).requireCount(1);
	}


	public void updateClass() throws Exception
	{
		u = stmt.createUpdate(sql);
		u = pstmt.init(sql).createUpdate();
	}


	public void updateReturnCols() throws Exception
	{
        UpdateResult<Integer> result = stmt.createUpdate("INSERT INTO Users VALUES (DEFAULT, 'John', 'Doe'")
            .returnAutoKeyCols()        // step 1: tell the Update to return auto-generated key columns
            .runGetCol(Integer.class);  // step 2: run the update, extract the new inserted primary key value as Integer
        long inserted = result.count();
        Integer newId = result.value();

        newId = stmt.createUpdate("INSERT INTO ...")
            .returnAutoKeyCols()
            .runGetCol(Integer.class)
            .requireCount(1)  // could throw an Exception
            .requireValue();  // could throw an Exception

        stmt.createUpdate(sql).returnCols(1, 5, 7);
        stmt.createUpdate(sql).returnCols("id", "timestamp");

        newId = pstmt.init().returnAutoKeyCols().sql("INSERT INTO Users VALUES (DEFAULT, ?, ?)")
        	.params("John", "Doe")
        	.createUpdate()
            .runGetCol(Integer.class)
            .requireValue();
	}


	public void updateAutoGen() throws Exception
	{
	    long updated = stmt.createUpdate("UPDATE MegaTable SET timestamp = NOW()")
	        .enableLargeCount()  // configures the Update
	        .run()               // runs the Update and returns the UpdateResult
	        .count();            // returns update count
	}


	public void execute() throws Exception
	{
		ExecuteResult result = stmt.execute(sql);
		while (result.next()) {
			if (result.isQueryResult())
				result.getQueryResult();
			else
				result.getUpdateResult();
		}
	}


	public void batches() throws Exception
	{
        stmt.batch()
            .add("INSERT INTO BatchDemo (name) VALUES ('A')")
            .add("INSERT INTO BatchDemo (name) VALUES ('B'), ('C'), ('D')")
            .run()  // returns a BatchResult
    		.requireSize(2)
    		.requireCount(0, 1)
    		.requireCount(1, 3);

        pstmt.init("INSERT INTO BatchDemo (name) VALUES (?)");
        pstmt.params("A").batch().add();
        pstmt.params("B").batch().add()
            .run()  // returns a BatchResult
    		.requireSize(2)
    		.requireCount(0, 1)
    		.requireCount(1, 1);
	}


	public void runningSingleCommands() throws Exception
	{
		int cityCount = Jdbx.query(con, "SELECT COUNT(*) FROM Cities").row().col().getInt();

		Jdbx.update(con, "INSERT INTO Status (flag) VALUES (?)", "F").requireCount(1);
	}


	public void multiStmts() throws Exception
	{
		try (MultiStmt mstmt = new MultiStmt(con))
		{
			StaticStmt s1 = mstmt.newStaticStmt();
			PrepStmt s2   = mstmt.newPrepStmt();
		}
	}


	private String sql;
	private StaticStmt stmt;
	private PrepStmt pstmt;
	private CallStmt cstmt;
	private Connection con;
	private DataSource ds;
	private Query q;
	private QueryResult qr;
	private Update u;
	private boolean jdbc;
}
