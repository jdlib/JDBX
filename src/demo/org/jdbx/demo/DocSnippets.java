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
	public void stmtsCreatingClosing()
	{
		new StaticStmt(con); 
		new PrepStmt(con);
		new CallStmt(con);
		new StaticStmt(ds); 
		new PrepStmt(ds);
		new CallStmt(ds);
		
		try (StaticStmt stmt = new StaticStmt(con)) {
		} 
	}


	public void stmtsStaticStmt()
	{
		long count = stmt.update("INSERT INTO Users VALUES (DEFAULT, 'John', 'Doe')").count();
		stmt.init().resultType(ResultType.SCROLL_SENSITIVE).resultConcurrency(ResultConcurrency.READ_ONLY);
	}


	public void stmtsPrepStmt()
	{
		pstmt.init("INSERT INTO Users VALUES (DEFAULT, ?, ?)");
		pstmt.params("John", "Doe").update();
		pstmt.params("Mary", "Jane").update();
		pstmt.init("UPDATE Users SET name = ? WHERE id = ?");
		pstmt.init()
	        .resultType(ResultType.SCROLL_INSENSITIVE)
	        .resultHoldability(ResultHoldability.HOLD_OVER_COMMIT)
	        .cmd("SELECT * FROM Cities WHERE name LIKE ?");
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
	
	
	public void queryClass() throws Exception
	{
		q = stmt.query(sql);
		q = pstmt.init(sql).params("a", "b").query();
	}
	
	
	public void querySingleRow() throws Exception
	{
		q.row().col();                // returns a builder to retrieve a value of the first column
		q.row().col().getString();    // returns the value of the first column as String
		q.row().col(3);               // returns a builder to retrieve a value of the third column
		q.row().col(3).getInteger();  // returns the value of the third column as Integer
		q.row().col("sort");          // returns a builder to retrieve a value of the "sort" column  
		q.row().col("sort").getInt(); // returns the value of "sort" column as int
		q.row().cols();               // returns the value of all columns, as Object[]
		q.row().cols(1,3,7);          // returns the value of columns 1,3,7, as Object[] 
		q.row().map();                // returns a Map<String,Object> mapping column name to value
		q.row().read(City::read);    // returns the value returned by the reader function
		
		q.row().required().col().getString();
		q.row().unique().col().getString();
	}
	
	
	public void queryAllRows() throws Exception
	{
		q.rows();
		q.rows().col();                    // return values of first column
		q.rows().col().getString();        // return values of first column as List<String>
		q.rows().col(3);                   // return values of column by index
		q.rows().col(3).getDouble();       // return values of third column, as List<Double>
		q.rows().col("sort");              // return values of column by name 
		q.rows().col("sort").getInteger(); // return values of "sort" column, as List<Integer>
		q.rows().cols();                   // return values of all columns, as List<Object[]>
		q.rows().cols(1,3,7);              // return values of columns 1,3,7, as List<Object[]> 
		q.rows().map();                    // return a List<Map<String,Object>>
		
		q.rows(5);
		q.skip(3).rows();
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

		q.row().read(City::read);
		q.rows().read(City::read);
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
		
		stmt.init().resultType(ResultType.SCROLL_SENSITIVE).resultConcurrency(ResultConcurrency.CONCUR_UPDATABLE);

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
		stmt.init().resultConcurrency(ResultConcurrency.CONCUR_UPDATABLE);

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
	
	
	public void updateAutoGen() throws Exception
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
	
	
	private String sql;
	private StaticStmt stmt;
	private PrepStmt pstmt;
	private CallStmt cstmt;
	private Connection con;
	private DataSource ds;
	private Query q;
	private QueryCursor qc;
	private Update u;
	private boolean jdbc;
}
