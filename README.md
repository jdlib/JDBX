# JDBX

JDBX is a library which provides replacements for JDBC Statement and ResultSet classes
to allow you to write compact and effective database code.

It shares this intent with libraries like Apache DBUtils, Spring JDBC template, JDBI, etc which all wrap plain JDBC to make database access easier.
But unlike these libraries it does not only simplify coding but still provides an API which retains 100% of the original JDBC functionality.
    
JDBX requires Java 8+, has no external dependencies and is released under the Apache 2.0 license.

* [Examples](#examples)
* [Download and Installation](#download)
* [Documentation](#docs)
* [License](#license)


## <a name="examples"></a>Examples

The following snippets show two examples of typical JDBC code and the JDBX rewrite:

**Example 1:** 
Perform a SQL select, create a bean object for every result row, return all beans in a list.

*using JDBC:*
        
	public List<City> queryCitiesWithJdbc(Connection con) throws SQLException {
		try (Statement stmt = con.createStatement()) {
			List<City> list = new ArrayList<>();
			ResultSet result = stmt.executeQuery("SELECT * FROM Cities ORDER BY name");
			while (result.next())
				list.add(City.read(result));
			return list;
		}
	}
	
*using JDBX:*

	public List<City> queryCitiesWithJdbx(Connection con) {
		return Jdbx.createQuery(con, "SELECT * FROM Cities ORDER BY name").rows().value(City::read);
	}


**Example 2:**
Perform a parameterized insert, return the auto generated primary key, convert any SQLException to a runtime exception.

*using JDBC:*

	public Integer createUserWithJdbc(Connection con, String firstName, String lastName) {
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


*using JDBX:*

	public Integer createUserWithJdbx(Connection con, String firstName, String lastName) {
		try (PrepStmt pstmt = new PrepStmt(con)) {
			pstmt.init().reportAutoKeys("id").cmd("INSERT INTO Users VALUES (DEFAULT, ?, ?)");
			pstmt.params(firstName, lastName);
			return pstmt.createUpdate().runGetAutoKey(Integer.class).checkCount(1).checkHasValue();
		}
	}
	
	
## <a name="download"></a>Download and Installation 

* Download the [latest Release](https://github.com/jdlib/JDBX/releases/latest)
* and put the JDBX jar file into the classpath.


## <a name="docs"></a>Documentation

* [User Guide](https://github.com/jdlib/JDBX/blob/master/UserGuide.md): explains how to use JDBX.
* [Design](https://github.com/jdlib/JDBX/blob/master/Design.md): Discusses the design of JDBX and the refactorings applied to JDBC. 


## <a name="license"></a>License

JDBX is released under the Apache 2.0 license.
	    
