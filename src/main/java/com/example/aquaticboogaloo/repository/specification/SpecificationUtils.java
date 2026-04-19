package com.example.aquaticboogaloo.repository.specification;

import jakarta.persistence.metamodel.SingularAttribute;
import org.springframework.data.jpa.domain.Specification;


public final class SpecificationUtils {

    public static <T, V> Specification<T> equalIfPresent(
            SingularAttribute<? super T, V> attribute,
            V value
    ) {
        return (root, query, cb) ->
                value == null ? null : cb.equal(root.get(attribute), value);
    }

    public static <T, V> Specification<T> likeIgnoreCaseIfPresent(
            SingularAttribute<? super T, String> attribute,
            String value
    ) {
        return (root, query, cb) ->
                value == null ? null : cb.like(
                        cb.lower(root.get(attribute)),
                        likeLowerCasePattern(value)
                );
    }

    private static String likeLowerCasePattern(String value) {
        return "%" + value.trim().toLowerCase() + "%";
    }
}
