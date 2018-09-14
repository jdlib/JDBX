# JDBX Refactorings

* Fixes the class hierarchy of JDBC `Statement`.
   `PreparedStatement`, `CallableStatement` derive from `Statement` and inherit methods
    which should not be used by the derived class (e.g. `Statement.executeQuery(String)`)
	
* Avoids crowding of `Statement` and `ResultSet` API. E.g. all option related methods are pushed into a class `StmtOptions` to reduce
    the size of the `Stmt` API. 
 
* Uses the Builder pattern to reduce API size and allow for shallow learning curve. 

* JDBC Statement has many methods which can only be called in a certain state (e.g. `getGeneratedKeys()`, `getUpdateCount()`, `getResultSet()`.
   We move these methos into special result objects so you can only access them in the rihgt situation.     

* Uses functional interfaces to read the result of a statement execution and therefore reduces repeating boilerplate code
	
* Replaces the checked java.sql.SQLException by an unchecked JdbxException. 

* Introduces Enums for Statement and ResultSet `int` constants
