package itstep.learning.dal.dao.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dto.shop.Category;
import itstep.learning.models.form.ShopCategoryFormModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class CategoryDao {
    private final Connection connection;
    private final Logger logger;

    @Inject
    public CategoryDao( Connection connection, Logger logger ) {
        this.connection = connection;
        this.logger = logger;
    }

    public List<Category> all() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories c WHERE c.delete_dt IS NULL";
        try( Statement statement = connection.createStatement() ) {
            ResultSet resultSet = statement.executeQuery( sql ) ;
            while( resultSet.next() ) {
                categories.add( new Category( resultSet ) ) ;
            }
        }
        catch( SQLException ex ) {
            logger.log( Level.WARNING, ex.getMessage() + " -- " + sql, ex );
        }
        return categories ;
    }

    public Category add( ShopCategoryFormModel formModel ) {
        Category category = new Category()
                .setId( UUID.randomUUID() )
                .setName( formModel.getName() )
                .setDescription( formModel.getDescription() )
                .setImageUrl( formModel.getSavedFilename() );
        String sql = "INSERT INTO categories (category_id, name, description, image_url) " +
                " VALUES (?, ?, ?, ?)";
        try ( PreparedStatement prep = connection.prepareStatement( sql ) ) {
            prep.setString( 1, category.getId().toString() );
            prep.setString( 2, category.getName() );
            prep.setString( 3, category.getDescription() );
            prep.setString( 4, category.getImageUrl() );
            prep.executeUpdate();
            return category;
        }
        catch( SQLException ex ) {
            logger.log( Level.WARNING, ex.getMessage() + " -- " + sql, ex );
            return null;
        }
    }

    public boolean installTables() {
        String sql =
                "CREATE TABLE IF NOT EXISTS categories (" +
                        "category_id  CHAR(36)      PRIMARY KEY  DEFAULT( UUID() )," +
                        "name         VARCHAR(128)  NOT NULL," +
                        "image_url    VARCHAR(512)  NOT NULL," +
                        "description  TEXT              NULL," +
                        "delete_dt    DATETIME          NULL" +
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
