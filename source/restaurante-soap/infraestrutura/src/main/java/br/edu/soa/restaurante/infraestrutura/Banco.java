// Finalidade: Preparar conexões e aplicar as migrações do esquema.
package br.edu.soa.restaurante.infraestrutura;

import java.io.PrintWriter;
import java.sql.*;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;

public final class Banco {
    private Banco() {}
    // Recebe os dados externos, prepara o DataSource e aplica as migrações.
    public static DataSource configurar(String url, String usuario, String senha) {
    	 carregarDriver(url);
        if (url == null || usuario == null || senha == null) throw new IllegalStateException("Configure DB_URL, DB_USER e DB_PASSWORD.");
        DataSource ds = new SimplesDataSource(url, usuario, senha);
        Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();
        return ds;
    }
    private record SimplesDataSource(String url, String usuario, String senha) implements DataSource {
        public Connection getConnection() throws SQLException { return DriverManager.getConnection(url,usuario,senha); }
        public Connection getConnection(String u,String s) throws SQLException { return DriverManager.getConnection(url,u,s); }
        public PrintWriter getLogWriter() { return null; }
        public void setLogWriter(PrintWriter p) { }
        public void setLoginTimeout(int seconds) { DriverManager.setLoginTimeout(seconds); }
        public int getLoginTimeout() { return DriverManager.getLoginTimeout(); }
        public Logger getParentLogger() { return Logger.getLogger("restaurante.jdbc"); }
        public <T> T unwrap(Class<T> type) throws SQLException { throw new SQLException("Não é wrapper"); }
        public boolean isWrapperFor(Class<?> type) { return false; }
        // function toString() { [native code] }
        @Override public String toString() { return "DataSource[configuração externa]"; }
    }
    
    private static void carregarDriver(String url) {
        try {
            if (url.startsWith("jdbc:mysql:")) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } else if (url.startsWith("jdbc:h2:")) {
                Class.forName("org.h2.Driver");
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                "Driver JDBC não encontrado.", e
            );
        }
    }
}
