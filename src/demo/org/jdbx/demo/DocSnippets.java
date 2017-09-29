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
		int count = stmt.update("INSERT INTO Users VALUES (DEFAULT, 'John', 'Doe')");
		stmt.init().resultType(ResultType.SCROLL_SENSITIVE).resultConcurrency(ResultConcurrency.READ_ONLY);
	}


	public void stmtsPrepStmt()
	{
		pstmt.init("INSERT INTO Users VALUES (DEFAULT, ?, ?)");
		pstmt.params("John", "Doe").update();
		pstmt.params("Mary", "Jane").update();
		pstmt.init("UPDATE Users SET name = ? WHERE id = ?");
		pstmt.init().resultType(ResultType.SCROLL_INSENSITIVE);
	}
	
	
	public void stmtsCallStmt()
	{
		cstmt.init("{call getUserName(?, ?)");
		cstmt.param(1).setInt(12045);
		cstmt.param(2).out(Types.VARCHAR);
		cstmt.execute();
		String userName = cstmt.param(2).getString();
	}
	
	
	public void queryRunningEx1() throws Exception
	{
		String sql = "SELECT * FROM Cities ORDER BY name";
		 
		// JDBC:
		{
			Statement stmt = con.createStatement();                        
			ResultSet result = stmt.executeQuery(sql);         
			List<City> cities = new ArrayList<>();
			while (result.next()) {
			    City city = City.read(result); 
			    cities.add(city);
			}
		}
		    
		// JDBX
		StaticStmt stmt = new StaticStmt(con);
		List<City> cities = stmt.createQuery(sql).rows().value(City::read);
	}
	
	
	public void queryRunningEx2() throws Exception
	{
		String sql = "SELECT name FROM Cities WHERE code = ?";

		// JDBC:    
		{
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, "MUC");                        
			ResultSet result = pstmt.executeQuery();
			String name = null;         
			if (result.next())
			    name = result.getString(1);
		}
		    
		// JDBX
		PrepStmt pstmt = new PrepStmt(con);
		String name = pstmt.init(sql).params("MUC").createQuery().row().col().getString();
	}
	
	
	public void queryClass() throws Exception
	{
		String sql = null;
		Query q = stmt.createQuery(sql);
		q = pstmt.createQuery();
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
		q.row().value(City::read);    // returns the value returned by the reader function
		
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
		q.rows().value(City::read);        // returns List<City>
//		q.read(consumer);
//		q.rows().read(...callback...);     // invokes the callback for every result row
		
		q.rows(5);
		q.skip(3).rows();
		
		ResultSet resultSet = q.resultSet();
		List<String> names  = Query.of(resultSet).rows().col("name").getString();
	}
	
	
	private StaticStmt stmt;
	private PrepStmt pstmt;
	private CallStmt cstmt;
	private Connection con;
	private DataSource ds;
	private Query q;
}
