package com.amalitech.smartecommerce.dao;

import java.util.UUID;
import java.util.List;
import com.amalitech.smartecommerce.model.Product;

public interface ProductDao {
    boolean delete(UUID id);
    Product update(Product product);
    Product insert(Product product);
    List<Product> searchByName(String name);
    List<Product> findByCategoryId(UUID categoryId);
    List<Product> findAll();
    Product findById(UUID id);
}
