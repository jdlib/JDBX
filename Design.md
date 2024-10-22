# JDBX Design

The refactorings applied to JDBC are 

* Fixes the class hierarchy of JDBC statement classes: `PreparedStatement`, `CallableStatement` derive from `Statement` but 
some base methods inherit should not be used by the derived class. E.g. if you use a `PreparedStatement` then `Statement.executeQuery(String)` should not be used. JDBX statement classes only offer methods which are valid to be called.
	
* Uses the Builder pattern to reduce the size of the crowded APIs of `Statement` and `ResultSet`. E.g. all option related methods are pushed into a class `StmtOptions`.

* JDBC `Statement` has many methods which can only be called if the statement object is in a certain state, e.g. `getGeneratedKeys()`, `getUpdateCount()`, `getResultSet()`.
These methods are moved into special result objects so you can only access them when dealing with the result.     

* Uses functional interfaces to read the result of a statement execution and therefore reduces repeating boilerplate code.
	
* Replaces the checked `java.sql.SQLException` by an unchecked `JdbxException`. 

* Introduces Enums for the `int` constants in `Statement` and `ResultSet`. 
