package com.amalitech.smartecommerce.dao;

import java.util.List;
import java.util.UUID;

public interface DAO<T> {
    T findById(UUID id);

    List<T> findAll();

    T create(T t);

    T update(T t);

    T delete(UUID id);

}