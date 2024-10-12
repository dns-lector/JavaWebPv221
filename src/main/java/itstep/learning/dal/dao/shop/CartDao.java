package itstep.learning.dal.dao.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class CartDao {
    private final Connection connection;
    private final Logger logger;

    @Inject
    public CartDao( Connection connection, Logger logger ) {
        this.connection = connection;
        this.logger = logger;
    }

    public boolean installTables() {
        String sql =
                "CREATE TABLE IF NOT EXISTS carts (" +
                        "cart_id     CHAR(36)  PRIMARY KEY  DEFAULT( UUID() )," +
                        "user_id     CHAR(36)  NOT NULL," +
                        "open_dt     DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "close_dt    DATETIME      NULL," +
                        "is_canceled TINYINT   NOT NULL DEFAULT 0" +
                        ") ENGINE = InnoDB, DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci";

        try( Statement stmt = connection.createStatement() ) {
            stmt.executeUpdate( sql );
        }
        catch( SQLException ex ) {
            logger.log( Level.WARNING, ex.getMessage() + " -- " + sql, ex );
            return false;
        }

        sql =
                "CREATE TABLE IF NOT EXISTS cart_items (" +
                        "cart_id     CHAR(36)      NOT NULL," +
                        "product_id  CHAR(36)      NOT NULL," +
                        "cnt         INT UNSIGNED  NOT NULL DEFAULT 1," +
                        "PRIMARY KEY(cart_id, product_id)" +
                        ") ENGINE = InnoDB, DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci";

        try( Statement stmt = connection.createStatement() ) {
            stmt.executeUpdate( sql );
            return true;
        }
        catch( SQLException ex ) {
            logger.log( Level.WARNING, ex.getMessage() + " -- " + sql, ex );
            return false;
        }
    }
}
/*
[carts]         [cart_items]
|cart_id        |cart_id
|user_id        |product_id
|close_dt       |cnt
|open_dt
|is_canceled

 */