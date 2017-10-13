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
			pstmt.init().returnCols("id").cmd("INSERT INTO Users VALUES (DEFAULT, ?, ?)");
			pstmt.params(firstName, lastName);
			return pstmt.createUpdate().runGetCol(Integer.class).requireCount(1).requireValue();
		}
	}

	
	public void stmtsCreateConfigureClose()
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
	        .setResultType(QResultType.SCROLL_SENSITIVE)
	        .setResultConcurrency(QResultConcurrency.READ_ONLY)
	        .setQueryTimeout(20)
	        .setFetchSize(5000);
	    int seconds = stmt.options().getQueryTimeout();
	}


	public void stmtsStaticStmt()
	{
		long count = stmt.update("INSERT INTO Users VALUES (DEFAULT, 'John', 'Doe')").count();
	}


	public void stmtsPrepStmt()
	{
		pstmt.init("INSERT INTO Users VALUES (DEFAULT, ?, ?)");
		pstmt.params("John", "Doe").update();
		pstmt.params("Mary", "Jane").update();
		pstmt.init("UPDATE Users SET name = ? WHERE id = ?");
		
		pstmt.options().setResultType(QResultType.SCROLL_INSENSITIVE).setResultHoldability(QResultHoldability.HOLD_OVER_COMMIT);
		pstmt.init("SELECT * FROM Cities WHERE name LIKE ?");
	}
	
	
	public void stmtsCallStmt()
	{
		cstmt.init("{call getUserName(?, ?)");
		cstmt.param(1).setInt(12045);
		cstmt.param(2).out(Types.VARCHAR);
		cstmt.execute();
		String userName = cstmt.param(2).getString();
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
	
	
	public void queryResultClass() throws Exception
	{
		qr = stmt.query(sql);
		qr = pstmt.init(sql).params("a", "b").query();
	}
	
	
	public void querySingleRow() throws Exception
	{
		qr.row().col();                // returns a builder to retrieve a value of the first column
		qr.row().col().getString();    // returns the value of the first column as String
		qr.row().col(3);               // returns a builder to retrieve a value of the third column
		qr.row().col(3).getInteger();  // returns the value of the third column as Integer
		qr.row().col("sort");          // returns a builder to retrieve a value of the "sort" column  
		qr.row().col("sort").getInt(); // returns the value of "sort" column as int
		qr.row().cols();               // returns the value of all columns, as Object[]
		qr.row().cols(1,3,7);          // returns the value of columns 1,3,7, as Object[] 
		qr.row().map();                // returns a Map<String,Object> mapping column name to value
		qr.row().read(City::read);    // returns the value returned by the reader function
		
		qr.row().required().col().getString();
		qr.row().unique().col().getString();
	}
	
	
	public void queryAllRows() throws Exception
	{
		qr.rows();
		qr.rows().col();                    // return values of first column
		qr.rows().col().getString();        // return values of first column as List<String>
		qr.rows().col(3);                   // return values of column by index
		qr.rows().col(3).getDouble();       // return values of third column, as List<Double>
		qr.rows().col("sort");              // return values of column by name 
		qr.rows().col("sort").getInteger(); // return values of "sort" column, as List<Integer>
		qr.rows().cols();                   // return values of all columns, as List<Object[]>
		qr.rows().cols(1,3,7);              // return values of columns 1,3,7, as List<Object[]> 
		qr.rows().map();                    // return a List<Map<String,Object>>
		
		qr.rows(5);
		qr.skip(3).rows();
	}
	
	
	public void queryCursor() throws Exception
	{
        qc.col();                    // first column
        qc.col().getString();        // first column as String
        qc.col(3);                   // column by index
        qc.col(3).getDouble();       // third column as double
        qc.col("sort");              // column by name 
        qc.col("sort").getInteger(); // "sort" column, as Integer
        qc.cols(1,3,7);              // columns 1,3,7, as Object[] 
        qc.map();                    // returns a Map<String,Object>

		qr.row().read(City::read);
		qr.rows().read(City::read);
}

	
    public static class City1 
    {
        public static City1 read(QueryCursor qc) {
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
	
	
	public void queryCursorNav() throws Exception
	{
		while (qc.next()) {
		    // read the result row
		}
		
		stmt.options().setResultType(QResultType.SCROLL_SENSITIVE).setResultConcurrency(QResultConcurrency.CONCUR_UPDATABLE);

		// qr is obtained from stmt
		qc = stmt.query("sql").cursor();
		qc.position().isBeforeFirst(); 
		// also: .isAfterLast(), .isLast()  

		qc.move().first() ;
		qc.move().absolute(5); 
		qc.move().relative(2);
		// also: .relative(), .afterLast(), .beforeFirst(), .first(), .etc.
		 
		qc.row().update();
		qc.row().refresh();
		// also: .insert(), .isUpdated(), .delete(), .isDeleted(), etc.		
	}

	
	public void queryCursorObtain() throws Exception
	{
		stmt.options().setResultConcurrency(QResultConcurrency.CONCUR_UPDATABLE);

	   	qc.col("status").setString("ok"); 
	    qc.row().update();
	    qc.row().refresh();
	}
	
	
	public void update() throws Exception
	{
		stmt.update(sql);
		pstmt.update();
	}
	
	
	public void updateRun() throws Exception
	{
		int count;
		
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
            .returnAutoKeyCols()        // step 1: tell the Update to return auto-generated key value
            .runGetCol(Integer.class);  // step 2: run the update, extract the new inserted primary key value as Integer 
        int inserted  = result.count();
        Integer newId = result.value(); 

        newId = stmt.createUpdate("INSERT INTO ...")
            .returnAutoKeyCols()
            .runGetCol(Integer.class)
            .requireCount(1)  // could throw an Exception
            .requireValue();  // could throw an Exception
        
        stmt.createUpdate(sql).returnCols(1, 5, 7);   
        stmt.createUpdate(sql).returnCols("id", "timestamp");

        newId = pstmt.init().returnAutoKeyCols().cmd("INSERT INTO Users VALUES (DEFAULT, ?, ?)")
        	.params("John", "Doe")
        	.createUpdate()
            .runGetCol(Integer.class)
            .requireValue();
	}


	public void updateAutoGen() throws Exception
	{
	    long updated = stmt.createUpdate("UPDATE MegaTable SET timestamp = NOW()") 
	        .returnLargeCount()  // configures the Update 
	        .run()               // runs the Update and returns the UpdateResult
	        .largeCount();       // returns the count as long
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
	
	
	private String sql;
	private StaticStmt stmt;
	private PrepStmt pstmt;
	private CallStmt cstmt;
	private Connection con;
	private DataSource ds;
	private QueryResult qr;
	private QueryCursor qc;
	private Update u;
	private boolean jdbc;
}
