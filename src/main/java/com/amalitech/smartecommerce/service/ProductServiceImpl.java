package com.amalitech.smartecommerce.service;

import com.amalitech.smartecommerce.cache.InventoryCache;
import com.amalitech.smartecommerce.cache.ProductCache;
import com.amalitech.smartecommerce.dao.ProductDao;
import com.amalitech.smartecommerce.dao.ProductDaoImpl;
import com.amalitech.smartecommerce.dao.ProductItemDao;
import com.amalitech.smartecommerce.model.Product;
import com.amalitech.smartecommerce.model.ProductItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductServiceImpl implements ProductService {
    private final ProductDao productDao;
    private final ProductItemDao productItemDao;
    private final ProductCache productCache = ProductCache.getInstance();

    public ProductServiceImpl() {
        this.productDao = new ProductDaoImpl();
        this.productItemDao = new ProductItemDao();
    }

    public ProductServiceImpl(ProductItemDao productItemDao) {
        this.productItemDao = productItemDao;
        this.productDao = new ProductDaoImpl();
    }

    public ProductServiceImpl(ProductDao productDao, ProductItemDao productItemDao) {
        this.productDao = productDao;
        this.productItemDao = productItemDao;
    }

    @Override
    public Product getProductById(UUID id) {
        // Try cache first
        Product p = productCache.getById(id);
        if (p != null) return p;

        p = productDao.findById(id);
        if (p != null) {
            productCache.put(p);
        }
        return p;
    }

    @Override
    public List<Product> getAllProducts() {
        if (productCache.getSize() > 0) {
            return productCache.getAll();
        }

        List<Product> products = productDao.findAll();
        if (products != null) {
            productCache.loadAll(products);
            return products;
        }
        return new ArrayList<>();
    }

    @Override
    public List<Product> getProductsByCategoryId(UUID categoryId) {
        List<Product> byCat = productCache.getByCategory(categoryId);
        if (byCat != null && !byCat.isEmpty()) return byCat;

        List<Product> fromDb = productDao.findByCategoryId(categoryId);
        if (fromDb != null && !fromDb.isEmpty()) {
            for (Product p : fromDb) productCache.put(p);
        }
        return fromDb != null ? fromDb : new ArrayList<>();
    }


    @Override
    public List<Product> searchProductsByName(String name) {

        List<Product> results = productCache.searchByName(name);
        if (results != null && !results.isEmpty()) return results;

        List<Product> fromDb = productDao.searchByName(name);
        if (fromDb != null && !fromDb.isEmpty()) {
            for (Product p : fromDb) productCache.put(p);
        }
        return fromDb != null ? fromDb : new ArrayList<>();
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

        if (product.getId() == null) {
            product.setId(UUID.randomUUID());
        }

        Product created = productDao.create(product);

        // Create product_item record with the specified price and stock
        if (created != null) {
            createProductItem(created.getId(), price, stock);
            productCache.put(created);
        }

        return created;
    }

    /**
     * Creates a product_item record for a new product with specified price and stock.
     * Uses the ProductItemDao.create(...) method so DB details are centralized.
     */
    private void createProductItem(UUID productId, double price, int stock) {
        ProductItem item = new ProductItem(null, productId, stock, price, null);
        ProductItem created = productItemDao.create(item);
        if (created == null) {
            System.err.println("Warning: Could not create product_item for product " + productId);
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
        Product updated = productDao.update(product);
        if (updated != null) {
            productCache.update(updated);
        }
        return updated;
    }

    @Override
    public Product deleteProduct(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        Product deleted = productDao.delete(id);
        if (deleted != null) {
            productCache.remove(id);
        }
        return deleted;
    }


    public ProductItem updateProductStock(ProductItem productItem) throws Exception {
        if (productItem == null) {
            return null;
        }

        ProductItem updated = productItemDao.updateProductQuantity(productItem);
        if (updated == null) {
            try {
                ProductItem created = productItemDao.create(productItem);
                if (created != null) {
                    // Update cache and return
                    try {
                        com.amalitech.smartecommerce.cache.InventoryCache invCache = com.amalitech.smartecommerce.cache.InventoryCache.getInstance();
                        invCache.add(created);
                    } catch (Exception e) {
                        // non-fatal
                    }
                    return created;
                }
            } catch (Exception e) {
                System.err.println("Error creating product_item after failed update: " + e.getMessage());
            }

            throw new Exception("Failed to persist product_item for product: " + productItem.getProductId());
        }

        // Update InventoryCache if present
        try {
            InventoryCache invCache = InventoryCache.getInstance();
            if (updated != null) {
                invCache.update(updated);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return updated;
    }

//    @Override
//    public ProductItem ensureProductItemExists(UUID productId, double defaultPrice, int defaultStock) {
//        if (productId == null) return null;
//        com.amalitech.smartecommerce.cache.InventoryCache invCache = com.amalitech.smartecommerce.cache.InventoryCache.getInstance();
//        // Check cache first
//        ProductItem existing = invCache.getByProductId(productId);
//        if (existing != null) return existing;
//
//        // Fallback to DB
//        existing = productItemDao.findByProductId(productId);
//        if (existing != null) {
//            invCache.add(existing);
//            return existing;
//        }
//
//        ProductItem newItem = new ProductItem(null, productId, defaultStock, defaultPrice, null);
//        ProductItem created = productItemDao.create(newItem);
//        if (created != null) invCache.add(created);
//        return created;
//    }

    @Override
    public ProductItem updateProductPriceAndStock(UUID productId, double price, int stock) {
        if (productId == null) return null;
        ProductItem updated = productItemDao.updatePriceAndStock(productId, price, stock);
        // Update InventoryCache if present
        try {
            InventoryCache invCache = InventoryCache.getInstance();
            if (updated != null) {
                invCache.update(updated);
            }
        } catch (Exception e) {
            // non-fatal
        }
        return updated;
    }

    @Override
    public ProductItem getProductItemByProductId(UUID productId) {
        if (productId == null) {
            return null;
        }

        try {
            InventoryCache invCache = InventoryCache.getInstance();
            ProductItem cached = invCache.getByProductId(productId);
            if (cached != null) return cached;

            ProductItem fromDb = productItemDao.findByProductId(productId);
            if (fromDb != null) invCache.add(fromDb);
            return fromDb;
        } catch (Exception e) {
            System.err.println("Error fetching ProductItem for product " + productId + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<ProductItem> getAllProductItems() {
        try {
            return productItemDao.findAll();
        } catch (Exception e) {
            System.err.println("Error fetching all ProductItems: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Product getProductByProductItemId(UUID productItemId) {
        if (productItemId == null) return null;
        try {
            // Try inventory cache first
            InventoryCache invCache = InventoryCache.getInstance();
            try {
                ProductItem cachedItem = invCache.getById(productItemId);
                if (cachedItem != null) {
                    return getProductById(cachedItem.getProductId());
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            ProductItem pi = productItemDao.findById(productItemId);
            if (pi == null) return null;
            return getProductById(pi.getProductId());
        } catch (Exception e) {
            System.err.println("Error resolving product by product_item id " + productItemId + ": " + e.getMessage());
            return null;
        }
    }


    @Override
    public void clearCache() {
        productCache.clear();
    }
}
