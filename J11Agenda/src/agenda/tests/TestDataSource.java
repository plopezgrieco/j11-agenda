package agenda.tests;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import agenda.config.Config;

public class TestDataSource {
	
	public static void main(String[] args) throws SQLException {
		DataSource ds = Config.getDataSource();
		Connection cx = ds.getConnection();
		//operamos
		cx.close();
		System.out.println("todo ok");
	}
}
