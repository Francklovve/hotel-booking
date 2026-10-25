package com.junia.hotel.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {

    void save(T item);

    Optional<T> findById(ID id);

    List<T> findAll();

    int count();
}
