package com.amalitech.smartecommerce.service;

import com.amalitech.smartecommerce.dao.ProductDao;
import com.amalitech.smartecommerce.dao.ProductDaoImpl;
import com.amalitech.smartecommerce.model.Product;
import com.amalitech.smartecommerce.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class ProductServiceImpl implements ProductService {
    private final ProductDao productDao;

    public ProductServiceImpl() {
        this.productDao = new ProductDaoImpl();
    }

    public ProductServiceImpl(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    public Product getProductById(UUID id) {
        return productDao.findById(id);
    }

    @Override
    public List<Product> getAllProducts() {
        return productDao.findAll();
    }

    @Override
    public List<Product> getProductsByCategoryId(UUID categoryId) {
        return productDao.findByCategoryId(categoryId);
    }

    @Override
    public List<Product> searchProductsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return productDao.findAll();
        }
        return productDao.searchByName(name);
    }

    @Override
    public Product createProduct(Product product) {
        // Validate product
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (product.getCategoryId() == null) {
            throw new IllegalArgumentException("Product categoryId is required");
        }

        // Set id if missing
        if (product.getId() == null) {
            product.setId(UUID.randomUUID());
        }

        Product created = productDao.create(product);

        // Also create a default product_item record for this product
        if (created != null) {
            createProductItem(created.getId(), 0.0, 100);
        }

        return created;
    }

    @Override
    public Product createProductWithPrice(Product product, double price, int stock) {
        // Validate product
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (product.getCategoryId() == null) {
            throw new IllegalArgumentException("Product categoryId is required");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }

        // Set id if missing
        if (product.getId() == null) {
            product.setId(UUID.randomUUID());
        }

        Product created = productDao.create(product);

        // Create product_item record with the specified price and stock
        if (created != null) {
            createProductItem(created.getId(), price, stock);
        }

        return created;
    }

    /**
     * Creates a product_item record for a new product with specified price and stock.
     */
    private void createProductItem(UUID productId, double price, int stock) {
        String sql = "INSERT INTO product_item (id, product_id, qty_in_stock, price, image) VALUES (?, ?, ?, ?, ?)";
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, UUID.randomUUID());
                stmt.setObject(2, productId);
                stmt.setInt(3, stock);
                stmt.setDouble(4, price);
                stmt.setString(5, null);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Warning: Could not create product_item for product " + productId + ": " + e.getMessage());
        }
    }

    @Override
    public Product updateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (product.getId() == null) {
            throw new IllegalArgumentException("Product ID is required for update");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        return productDao.update(product);
    }

    @Override
    public Product deleteProduct(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        return productDao.delete(id);
    }
}
