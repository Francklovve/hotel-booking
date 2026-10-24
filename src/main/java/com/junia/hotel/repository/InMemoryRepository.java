package com.junia.hotel.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/** Stockage par identifiant. LinkedHashMap conserve aussi l'ordre d'ajout des objets. */
public class InMemoryRepository<T, ID> implements Repository<T, ID> {

    private final Map<ID, T> items = new LinkedHashMap<>();
    private final Function<T, ID> idExtractor;

    public InMemoryRepository(Function<T, ID> idExtractor) {
        this.idExtractor = Objects.requireNonNull(idExtractor);
    }

    @Override
    public void save(T item) {
        items.put(idExtractor.apply(item), item);
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public int count() {
        return items.size();
    }
}
