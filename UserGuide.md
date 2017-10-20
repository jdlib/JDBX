JDBX User Guide

1. [Intro](#intro)
2. [Statements](#stmts)
   * [Create and close statements](#stmts-create)
   * [StaticStmt](#stmts-static)
   * [PrepStmt](#stmts-prep)
   * [CallStmt](#stmts-call)
3. [Running SQL queries](#queries)
   * [QueryResult class](#queries-queryresult)
   * [Read a single result row](#queries-singlerow)
   * [Read all result rows](#queries-allrows)
   * [Skip rows](#queries-skipping)
   * [QueryCursor class](#queries-querycursorclass)
4. [Running DML or DDL updates](#updates)
   * [Run the update](#updates-run)
   * [Update class](#updates-updateclass)
   * [Read returned columns values](#updates-readcols)
   * [Return large update counts](#updates-large)
5. [Execute arbitrary SQL commands](#execute)
6. [Run batches](#batches)
7. [Exceptons](#exceptions)
8. [Run a single command](#single-cmd)


## <a name="stmts"></a>1. Intro

JDBX offers an alternative way to execute SQL or DDL commands and read query or update results.
For that it wraps JDBC statement and result-set classes with an own API.
Still the starting point of all its operations is a `java.sql.Connection` or `javax.sql.DataSource` object. 
 

## <a name="stmts"></a>2. Statements

JDBX provides three alternative statement classes to replace the corresponding JDBC classes:

JDBC|JDBX|Used to 
----|----|-------
`java.sql.Statement`|`org.jdbx.StaticStmt`|execute static, non-parameterized SQL commands
`java.sql.PreparedStatement`|`org.jdbx.PrepStmt`|execute precompiled, parameterized SQL commands
`java.sql.CallableStatement`|`org.jdbx.CallStmt`|call stored procedures

JDBX - as JDBC - differentiates between

1. Running SQL queries, returning results
2. Running SQL UPDATE, DELETE, INSERT commands or DDL commands, returning update counts and automatically generated keys
3. Running SQL commands which can return multiple results
4. Running SQL commands in a batch
5. Calling stored procedures

`StaticStmt` and `PrepStmt` can run SQL or DDL commands (1-4), `CallStmt` can call stored procedures (5).


### <a name="stmts-create"></a>Create and close statements

In order to create a JDBX statement you need a `java.sql.Connection` or `javax.sql.DataSource`:

     Connection con   = ...
     DataSource ds    = ...
     StaticStmt stmt  = new StaticStmt(con); // or new StaticStmt(ds) 
     PrepStmt   pstmt = new PrepStmt(con);   // or new PrepStmt(ds)
     CallStmt   cstmt = new CallStmt(con);   // or new CallStmt(ds)
     
     
Statement objects should be actively **closed** once they are no longer used. Since all statement classes implement `java.io.AutoCloseable` 
the typical pattern is to create and use a statement object within a Java try-with-resources block:

     Connection con = ...
     try (StaticStmt stmt = new StaticStmt(con)) {
          ... // use the statement
     }   

Statements created from a `DataSource` will use a connection obtained from the `DataSource`. When the statement is closed the 
connection will also be closed automatically.

To **configure** a statement object use the builder returned by its `options()` method:      

    stmt.options()
        .setResultType(ResultType.SCROLL_SENSITIVE)
        .setResultConcurrency(Concurrency.READ_ONLY);
        .setQueryTimeout(20)
	    .setFetchSize(5000);
	int seconds = stmt.options().getQueryTimeout();


### <a name="stmts-static"></a>StaticStmt

`org.jdbx.StaticStmt` can execute static (non-parameterized) SQL commands. Example:

    StaticStmt stmt = ...
    int count = stmt.update("INSERT INTO Users VALUES (DEFAULT, 'John', 'Doe')");
    
### <a name="stmts-prep"></a>PrepStmt

`org.jdbx.PrepStmt` can execute precompiled, parameterized SQL commands. After it is initialized it can
be executed multiple times using different parameter values. 
Contrary to `java.sql.PreparedStatement` you can also re-initialize the command. Example:

    PrepStmt pstmt = ...
    pstmt.init("INSERT INTO Users VALUES (DEFAULT, ?, ?)");
    pstmt.params("John", "Doe").update();
    pstmt.params("Mary", "Jane").update();
    pstmt.init("UPDATE Users SET name = ? WHERE id = ?");
    ...
     
    
Like in `StaticStmt` you can use the builder returned by the `options()` method to configure the `PrepStmt`.
      
    pstmt.options()
        .setResultType(ResultType.SCROLL_INSENSITIVE)
        .setResultHoldability(Holdability.HOLD_OVER_COMMIT);
     

### <a name="stmts-call"></a>CallStmt

`org.jdbx.CallStmt` can call stored procedures. After it is initialized it can
be executed multiple times using different parameter values. Example:

    CallStmt cstmt = ...
    cstmt.init("{call getUserName(?, ?)");
    cstmt.param(1).setInt(12045);
    cstmt.param(2).out(Types.VARCHAR);
	cstmt.execute();
	String userName = cstmt.param(2).getString();


## <a name="queries"></a>3. Run SQL queries

In JDBC executing a query returns a `java.sql.ResultSet`. Given the `ResultSet` you can loop over its rows and extract 
values from the rows.

JDBX uses fluent APIs and functional programming to avoid most of the boilerplate code needed in JDBC.

**Example 1:** Read all rows from a query result and convert each row into a data object

    Connection con = ...
    String sql = "SELECT * FROM Cities ORDER BY name";
     
    // JDBC:             
    try (Statement stmt = con.createStatement()) {                        
        ResultSet result = stmt.executeQuery(sql);         
        List<City> cities = new ArrayList<>();
        while (result.next()) {
            City city = City.read(result); 
            cities.add(city);
        }
        return cities;
    }
	    
    // JDBX:
    try (StaticStmt stmt = new StaticStmt(con)) {
        return stmt.query(sql).rows().read(City::read);
    }
     
**Example 2:** Extract a single value from a result set which contains 0 or 1 rows

    Connection con = ...
    String sql = "SELECT name FROM Cities WHERE code = ?";
    
    // JDBC:             
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
        pstmt.setString(1, "MUC");                        
        ResultSet result = pstmt.executeQuery();
        String name = null;         
        if (result.next())
            name = result.getString(1);
        return name;
    } 
	    
    // JDBX:
    try (PrepStmt pstmt = new PrepStmt(con)) {
        return pstmt.init(sql).params("MUC").query().row().col().getString();
    }


### <a name="queries-queryresult">QueryResult class

In order to execute a SQL query you need a `StaticStmt` or `PrepStmt`. 

`StaticStmt.query(String)` and `PrepStmt.query()` return a `org.jdbx.QueryResult` object
which provides a fluent API to extract values from the result:

     QueryResult qr = stmt.query(sql);
     QueryResult qr = pstmt.init(sql).params("a", "b").query();
     
If you have obtained a `java.sql.ResultSet` from somewhere else you can also turn it into a `QueryResult` for easy value extraction:

    java.sql.ResultSet resultSet = ...
    List<String> names = QueryResult.of(resultSet).rows().col("name").getString();
     
In the following variable `qr` represents a `QueryResult` object obtained from a `StaticStmt` or `PrepStmt`.     
But because of its fluent API you rarely will need to store a `QueryResult` in a local variable but rather chain
method calls until you receive the result of the query.      

Also note that the actual JDBC query is usually not run until you invoke the terminal method of the fluent call chain.


### <a name="queries-singlerow">Read a single result row

Call `QueryResult.row()` to retrieve a builder to read values from the first result row:     
     
    qr.row()...     
    qr.row().col()...              // returns a builder to retrieve a value of the first column
    qr.row().col().getString();    // returns the value of the first column as String
    qr.row().col(3)...             // returns a builder to retrieve a value of the third column
    qr.row().col(3).getInteger();  // returns the value of the third column as Integer
    qr.row().col("sort")...        // returns a builder to retrieve a value of the "sort" column  
    qr.row().col("sort").getInt(); // returns the value of "sort" column as int
    qr.row().cols();               // returns the value of all columns, as Object[]
    qr.row().cols(1,3,7);          // returns the value of columns 1,3,7, as Object[] 
    qr.row().map();                // returns a Map<String,Object> mapping column name to value

If the result is empty, all the examples above will return a null value (or a default value for primitive terminals like `getInt()`).
If you want rule out this case use `QueryResult.row().required()`:

     // will throw an exception if the result contains no rows
     qr.row().required().col().getString()
     
You may also want detect the case when the result contains more than one row, using `QueryResult.row().unique()`:
     
     // will throw an exception if the result contains more than one row
     qr.row().unique().col().getString()


### <a name="queries-allrows"></a>Read all result rows

Call `QueryResult.rows()` to retrieve a builder to read values from all rows and return as `java.util.List`:

    qr.rows()...
    qr.rows().col()...                  // return values of first column
    qr.rows().col().getString();        // return values of first column as List<String>
    qr.rows().col(3)...                 // return values of column by index
    qr.rows().col(3).getDouble();       // return values of third column, as List<Double>
    qr.rows().col("sort")...;           // return values of column by name 
    qr.rows().col("sort").getInteger(); // return values of "sort" column, as List<Integer>
    qr.rows().cols();                   // return values of all columns, as List<Object[]>
    qr.rows().cols(1,3,7);              // return values of columns 1,3,7, as List<Object[]> 
    qr.rows().map();                    // return a List<Map<String,Object>>
     
You may also limit the number of processed rows if this is not done within the SQL query itself:

    qr.rows(5)...
    

### <a name="queries-skipping"></a>Skip rows

Call `Queryresult.skip(int)` if you want to skip a number of rows before you extract values 
by calling `QueryResult.row()`, `.rows()` or `.rows(int)`:

    qr.skip(3).rows()...   // all rows after the first three rows


### <a name="queries-querycursorclass"></a>QueryCursor class

As shown above the `QueryResult` class makes it easy to extract a column value or an array of column values from a result row
using the various `col()` and `cols()` builder methods.

For more complicated cases JDBX provides the `QueryCursor` class which replaces/wraps `java.sql.ResultSet` and allows
you to navigate thought the result rows and read values from each row.

When positioned on a result row, `QueryCursor` offers similar methods like the builder returned by `QueryResult.row()` to extract values 
from the row:

    QueryCursor qc = ...         // a QueryCursor, positioned on a result row (as explained later)
    qc.col()...                  // first column
    qc.col().getString();        // first column as String
    qc.col(3)...                 // column by index
    qc.col(3).getDouble();       // third column as double
    qc.col("sort")...;           // column by name 
    qc.col("sort").getInteger(); // "sort" column, as Integer
    qc.cols(1,3,7);              // columns 1,3,7, as Object[] 
    qc.map();                    // returns a Map<String,Object>

Given this API it is easy to create a function which obtains a `QueryCursor` and returns a complex
value constructed from row values:

    public class City {
         public static City read(QueryCursor qc) {
             City city = new City();
             city.setCode(qc.col(1).getString());
             city.setName(qc.col(2).getString());
             ...
             return city;  
         }
         
         public void setName(String name) { ...
    }    

Now the builders returned by `QueryResult.row()` and `.rows()` accept such a reader function and invoke it for the first row / all rows
to return a single object / a list of objects:

    City city       = qr.row().read(City::read);    // read a single data object from first result row 	 
    List<City> city = qr.rows().read(City::read);   // read a list of data objects from all rows 


#### Self-managed QueryCursor navigation 

If you want to navigate through a `QueryCursor` yourself you can obtain the cursor by calling
`QueryResult.cursor()`. You should actively close the `QueryCursor` once it is no longer used
therefore it is best wrapped in a try-with-resources block:

     QueryResult qr = ...
     try (QueryCursor qc = qr.cursor()) {
         // loop through result and read its rowss
     }

Given a `QueryCursor` it is easy to run through its rows in a forward only manner:

    while (qc.next()) {
        // read the result row
    }
     
If your cursor is scrollable you can ask for the position and freely move the current row,
by using the service objects returned by `QueryCursor.position()` and `.move()`:

	// configure a scroll sensitive cursor	
	StaticStmt stmt = ....
	stmt.options().setResultType(ResultType.SCROLL_SENSITIVE);
	
	// and run the query
	try (QueryCursor qc = stmt.query(sql).cursor()) {
	    // read position
	    qc.position().isBeforeFirst() 
	    // also: .isAfterLast(), .isLast()  

	    // move current row
	    qc.move().first() 
	    qc.move().absolute(5) 
	    qc.move().relative(2)
	    // also: .relative(), .afterLast(), .beforeFirst(), .first(), .etc.
	}
  
#### Update a QueryCursor row
    
If your cursor is updatable, you can or update or delete the current row, or insert a new row:

	// configure the result to be updatable
	StaticStmt stmt = ....
	stmt.options().setResultConcurrency(Concurrency.CONCUR_UPDATABLE);
	QueryCursor qc = stmt.query(sql).cursor();
	
	// position row
	... 
	
   	qc.col("status").setString("ok"); 
    qc.row().update();
    qc.row().refresh();
    // also: .insert(), .isUpdated(), .delete(), .isDeleted(), etc.
     
     
#### Access the ResultSet  
      
You can still obtain the underlying `java.sql.ResultSet` of a query cursor if you need to:
 
    ResultSet resultSet = qc.resultSet();
    while (resultSet.next())
        ... 
    
## <a name="updates"></a>4. Run DML or DDL updates

JDBX - as JDBC - uses the term *update* for running DML commands (i.e. UPDATE, INSERT, DELETE), DDL commands and all SQL commands which
return nothing. 

Running an update command returns the number of affected records and optionally the values of changed columns, e.g.
most important the auto-generated values of primary key columns.

### <a name="updates-run">Run the update

Updates are executed by either using a `StaticStmt` or a `PrepStmt`.

If you just want to run an update command and are not interested in returned column values then call
`StaticStmt.update(String)` or `PrepStmt.update()`:

   	String sql      = ... 
   	StaticStmt stmt = ...
	stmt.update(sql);
	
	// or: 
   	PrepStmt ptmt   = ...
   	pstmt.update();
   	
Both calls return the update result as an `org.jdbx.UpdateResult` object. The method `UpdateResult.count()` 
gives the update count, i.e. the number of affected records:

	String sql   = "INSERT INTO ...";
	int inserted = stmt.update(sql).count();
	if (inserted != 1)
	    throw new IllegalStateException("insert failed");

Testing the update count can be shortened by calling `UpdateResult.requireCount`: 

	stmt.update(sql).requireCount(1); // throws an exception if the update count is not 1
	

### <a name="updates-updateclass">Update class

If you want to retrieve returned columns values, e.g. auto-generated primary key values, or want to enable large (`long`) update counts, 
then call `StaticStmt.createUpdate(String)` and `PrepStmt.createUpdate()` which returns a `org.jdbx.Update` object.
The `Update` class provides a fluent API to configure and then run the update to return a `UpdateResult`:
    
     Update u = stmt.createUpdate(sql);
     Update u = pstmt.init(sql).params("1", "2").createUpdate();
     
Because of its fluent API you will rarely need to store an `Update` object in a local variable but rather chain
method calls to retrieve the result. 


### <a name="updates-readcols">Read returned columns values

If you are interested to read returned column values, e.g. auto-generated primary key values, you need to

   1. specify the columns which should be returned
   2. invoke an appropriate method on the `Update` object to read the returned column values and
      store them in the `UpdateResult`    

For `StaticStmt` steps 1) and 2) are done by configuring the `Update` object:
  
    StaticStmt stmt = ...
    UpdateResult<Integer> result = stmt.createUpdate("INSERT INTO Users VALUES (DEFAULT, 'John', 'Doe'")
        .returnAutoKeyCols()        // step 1: tell the Update to return the auto-generated key columns
        .runGetCol(Integer.class);  // step 2: run the update, extract the new inserted primary key column as Integer 
    int inserted  = result.count();
    Integer newId = result.value();
    
The convenience method `UpdateResult.requireValue` tests if the column value stored in the result is not null and returns the value.
This allows to write:  
        
    Integer newId = stmt.createUpdate("INSERT INTO ...")
        .returnAutoKeyCols()
        .runGetCol(Integer.class)
        .requireCount(1)  // could throw an Exception
        .requireValue();  // could throw an Exception

For step 1 there are alternative ways to specify the returned columns, for instance by using column indexes or names:

    stmt.createUpdate(sql).returnCols(1, 5, 7)...   
    stmt.createUpdate(sql).returnCols("id", "timestamp")...
    
For step 2 there are alternative ways to retrieve the returned column values. If multiple records are affected, and you want
to retrieve one column value from each row you can call `Update.runGetCols(Class<T>)` to retrieve the values as `java.util.List<T>`:

    List<Integer> newIds = stmt.createUpdate("INSERT INTO Names (name) VALUES ('Peter'), ('Paul')") 
        .returnAutoKeyCols()
        .runGetCols(Integer.class)
        .value();

For the general case `Update` has a `runGetCols` method  which accepts a reader function: The function then receives the result set
containing the returned values and must read and construct a final value which is then stored in the `UpdateResult`. 
   
For `PrepStmt` step 1) must be done during the initialization phase:

    PrepStmt pstmt = ...
    pstmt.init().returnAutoKeyCols().cmd("INSERT INTO Users VALUES (DEFAULT, ?, ?)")
    Integer newId  = pstmt.params("John", "Doe").createUpdate()
        .runGetCol(Integer.class)
        .requireValue();
        
### <a name="updates-large">Return large update counts

Since version 4.2 JDBC has an API for large update counts, represented as `long` values. Since not all
JDBC drivers support this feature JDBX gives optional access to large count values:       

    long updated = stmt.createUpdate("UPDATE MegaTable SET timestamp = NOW()") 
        .returnLargeCount()  // configures the Update to retrieve large counts 
        .run()               // runs the Update and returns the UpdateResult
        .largeCount();       // returns the update count as long   
        

## <a name="execute"></a>5. Execute arbitrary SQL commands

JDBX - as JDBC - uses the term *execute* for running SQL commands with yet unknown result type - update or query - or which can return multiple
update and query results.

You can execute a command by calling `StaticStmt.execute(String sql)` or `PrepStmt.execute()` on a initialized `PrepStmt`.
Both methods return a `org.jdbx.ExecuteResult` which allows you to loop through the individual results, detect if they are
update or query results and evaluate the `UpdateResult` or `QueryResult`.  

	String sql = ...
	StaticStmt stmt = ...
	ExecuteResult result = stmt.execute(sql);
	while (result.next()) {
	    if (result.isQuery())
            // process result.getQueryResult() 
	    else
	        // process result.getUpdateResult()
	}  	

When doing updates the update result may return column values from the update. If you want to obtain these column values,
TODO  

## <a name="batches"></a>6. Run batches

All JDBX statement classes - like their counterparts in JDBC - allow to group SQL commands in batches to improve
roundtrip performance. Instead of directly incorporating batch related methods into the statement interface,
all JDBX statements know a `batch()` method which returns a `org.jdbc.Batch` object to add or clear commands and run the batch.

When the Batch is run it returns a `org.jdbx.BatchResult` which similar to `UpdateResult` allows you to access the update counts:

    StaticStmt stmt = ...
    stmt.batch()
        .add("INSERT INTO BatchDemo (name) VALUES ('A')")
        .add("INSERT INTO BatchDemo (name) VALUES ('B'), ('C'), ('D')")
        .run()  // returns a BatchResult
		.requireSize(2)
		.requireCount(0, 1)
		.requireCount(1, 3);
       
    PStmt pstmt = ... 
    pstmt.init("INSERT INTO BatchDemo (name) VALUES (?)");
    pstmt.params("A").batch().add();
    pstmt.params("B").batch().add()
        .run()  // returns a BatchResult
		.requireSize(2)
		.requireCount(0, 1;
		.requireCount(1, 1);
		

## <a name="exceptions"></a>7. Exceptions

JDBC reports database errors as **checked** exceptions using `java.sql.SQLException` and derived classes.

JDBX has a fundamental stance to favor unchecked exceptions and therefore introduces an own **unchecked** exception class
`org.jdbx.JdbxException` which can be thrown by its operations. Especially any `SQLException` thrown by
the underlying JDBC operations is wrapped into a `JdbxException` and reported as its exception cause.  

For easier exception handling a `JdbxExeption` contains a enum `JdbxExeption.Reason` to classify the exception
context. If based on a `SQLException` it also contains a enum `JdbxExeption.SqlExType` to classify the 
the `SQLException`.

## <a name="single-cmd"></a>8. Run a single command
        
If you only want to run a single SQL query or DML update you can use the convenience methods in class `org.jdbx.Jdbx` 
which handle creation and closing of a statement object. 

    Connection con = ...

    // run a static SELECT: creates a StaticStmt internally and returns the QueryResult
    int cityCount = Jdbx.query(con, "SELECT COUNT(*) FROM Cities").row().col().getInt();

    // run a parameterized INSERT: creates a PrepStmt internally and returns the UpdateResult 
    Jdbx.update(con, "INSERT INTO Status (flag) VALUES (?)", "F").requireCount(1);
 
But if you are running a couple of SQL commands it is more efficient to create a statement and reuse it.   
 

TODO
org.jdbx.demo package
resultiterator
querycursor config: setfetchdirection
multistmt
named params