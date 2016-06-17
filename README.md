# JDBX

JDBX is a small library which wraps the Java JDBC Statement and ResultSet classes. It allows to write less code while at the same time still provides 100% of the original functionality. 
It has no external dependencies and is published under the Apache 2.0 license.

## Example Code

Shows two examples of typical JDBC code and the JDBX rewrites:

**Example 1:** 
Perform a SQL select, create a Bean from every result row, return all beans in a list.

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
		try (ParamStmt pstmt = new ParamStmt(con)) {
			pstmt.init().reportAutoKeys("id").cmd("INSERT INTO Users VALUES (DEFAULT, ?, ?)");
			pstmt.params(firstName, lastName);
			return pstmt.createUpdate().runGetAutoKey(Integer.class).checkCount(1).checkHasValue();
		}
	}
	
## Similar Libraries

There are many libraries like Apache DBUtils, Spring JDBC template, JDBI, etc which wrap plain JDBC to make database access easier. 
JDBX falls into this category - and it also does not only simplify code but also retains 100% of JDBC functionality.

## Download

* [Latest Release](https://github.com/jdlib/JDBX/releases/latest)


## Documentation

* [User Guide](https://github.com/jdlib/JDBX/blob/master/UserGuide.md): This guide contains examples on how to use JDBX.
* [Design](https://github.com/jdlib/JDBX/blob/master/Design.md): Discusses the design of JDBX and the refactorings applied to JDBC. 

## License

JDBX is released under the Apache 2.0 license.
	    
