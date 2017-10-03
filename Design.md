# JDBX Refactorings

* Uses the Builder pattern to reduce the size of `Statement` and `ResultSet` API.
	example:    
	
	- also: Statement API contains methods which only should be called when other methods have been called before
		example: getGeneratedKeys() getUpdateCount(), getResultSet() -> move these methods out of the way

* Fixes the class hierarchy of JDBC `Statement`, `PreparedStatement`, `CallableStatement`: 
	The latter two classes inherit methods from Statement which should not be used (e.g.)
	
* introduces functional interfaces to read the result of a statement execution and
	therefore reduces repeating boilerplate code
	
	PreparedStatement pstmt = ...;
	try (ResultSet rs = pstmt.executeQuery())
	{
		while (rs.next)
		    readRow(rs); 
	} 
	


   	PprepStmt pstmt = ...;
   	pstmt.query().rows().read(this::readRow);
	    
	// also has many abbreviations for standard read patterns    
	PrepStmt pstmt = ...;
	List<String> list = pstmt.query().rows().col().getString();
	
	in particular this reduces nested try-with-resources blocks to close resultsets

* replaces the checked java.sql.SQLException by an unchecked JdbxException 

* introduces Enums for Statement and ResultSet int constants
		
 		
		