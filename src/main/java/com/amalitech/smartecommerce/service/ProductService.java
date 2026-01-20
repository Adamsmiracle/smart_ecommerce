package com.amalitech.smartecommerce.service;

import java.util.List;
import java.util.UUID;
import com.amalitech.smartecommerce.model.Product;
import com.amalitech.smartecommerce.model.ProductItem;


public interface ProductService {
    Product getProductById(UUID id);

    List<Product> getAllProducts();

    List<Product> getProductsByCategoryId(UUID categoryId);

    List<Product> searchProductsByName(String name);

    Product createProductWithPrice(Product product, double price, int stock);

    Product updateProduct(Product product);

    Product deleteProduct(UUID id);

    ProductItem updateProductStock(ProductItem productItem) throws Exception;

    ProductItem getProductItemByProductId(UUID productId);


    /**
     * Update price and stock for a product (delegates to ProductItemDao.updatePriceAndStock).
     */
    ProductItem updateProductPriceAndStock(UUID productId, double price, int stock);

    List<ProductItem> getAllProductItems();



    void clearCache();

    /**
     * Resolve a Product by a product_item id (returns the Product that the product_item points to).
     */
    Product getProductByProductItemId(java.util.UUID productItemId);
}
