package com.junia.hotel.repository;

import java.util.List;
import java.util.Optional;

/**
 * Storage abstraction. Services depend on this interface, not on a concrete storage
 * (Dependency Inversion): an in-memory map today, a file or a database tomorrow.
 *
 * @param <T>  stored type
 * @param <ID> identifier type
 */
public interface Repository<T, ID> {

    void save(T item);

    Optional<T> findById(ID id);

    List<T> findAll();

    int count();
}
