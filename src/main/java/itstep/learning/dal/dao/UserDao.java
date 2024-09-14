package itstep.learning.dal.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dto.User;
import itstep.learning.models.form.UserSignupFormModel;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class UserDao {
    private final Connection connection;
    private final Logger logger;

    @Inject
    public UserDao( Connection connection, Logger logger ) {
        this.connection = connection;
        this.logger = logger;
    }

    public User getUserById( UUID id ) {
        return null;
    }

    public User signup( UserSignupFormModel user ) {
        return null;
    }

    public boolean installTables() {
        String sql =
            "CREATE TABLE IF NOT EXISTS users (" +
                "id        CHAR(36)     PRIMARY KEY  DEFAULT( UUID() )," +
                "name      VARCHAR(128) NOT NULL," +
                "email     VARCHAR(128) NOT NULL," +
                "avatar    VARCHAR(128)     NULL," +
                "birthdate DATETIME         NULL," +
                "signup_dt DATETIME     NOT NULL   DEFAULT CURRENT_TIMESTAMP," +
                "delete_dt DATETIME         NULL" +
            ") ENGINE = InnoDB, DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci";

        try( Statement stmt = connection.createStatement() ) {
            stmt.executeUpdate( sql );
        }
        catch( SQLException ex ) {
            logger.log( Level.WARNING, ex.getMessage() + " -- " + sql, ex );
            return false;
        }
        sql =
            "CREATE TABLE IF NOT EXISTS users_security (" +
                "id        CHAR(36)     PRIMARY KEY  DEFAULT( UUID() )," +
                "user_id   CHAR(36)     NOT NULL," +
                "login     VARCHAR(64)  NOT NULL," +
                "salt      CHAR(32)     NOT NULL," +
                "dk        CHAR(32)     NOT NULL," +
                "role_id   CHAR(36)         NULL" +
            ") ENGINE = InnoDB, DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci";
        try( Statement stmt = connection.createStatement() ) {
            stmt.executeUpdate( sql );
        }
        catch( SQLException ex ) {
            logger.log( Level.WARNING, ex.getMessage() + " -- " + sql, ex );
            return false;
        }
        return true;
    }
}
