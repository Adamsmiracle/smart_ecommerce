package com.amalitech.smartecommerce.dao;

import java.util.UUID;
import java.util.List;
import com.amalitech.smartecommerce.model.Product;

public interface ProductDao extends DAO<Product> {
    List<Product> searchByName(String name);
    List<Product> findByCategoryId(UUID categoryId);
}
