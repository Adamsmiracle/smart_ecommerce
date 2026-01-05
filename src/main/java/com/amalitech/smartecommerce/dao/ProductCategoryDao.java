package com.amalitech.smartecommerce.dao;
import java.util.UUID;
import java.util.List;
import com.amalitech.smartecommerce.model.ProductCategory;


public interface ProductCategoryDao {
    boolean delete(UUID id);
    boolean update(ProductCategory category);
    boolean insert(ProductCategory category);
    List<ProductCategory> findAll();
    ProductCategory findById(UUID id);

}