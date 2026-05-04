package com.example.aquaticboogaloo.repository.specification;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.SingularAttribute;
import org.springframework.data.jpa.domain.Specification;

import java.util.function.Function;


public final class SpecificationUtils {

    public static <T, V> Specification<T> equalIfPresent(
            SingularAttribute<? super T, V> attribute,
            V value
    ) {
        if (value == null) return null;

        return (root, query, cb) -> cb.equal(root.get(attribute), value);
    }

    public static <T, J, V> Specification<T> equalIfPresent(
            SingularAttribute<? super T, J> firstAttribute,
            SingularAttribute<? super J, V> secondAttribute,
            V value
    ) {
        if (value == null) return null;

        return (root, query, cb) -> cb.equal(root.get(firstAttribute).get(secondAttribute), value);
    }

    public static <T> Specification<T> likeIgnoreCaseIfPresent(
            SingularAttribute<? super T, String> attribute,
            String value
    ) {
        return likeIgnoreCaseIfPresent(root -> root.get(attribute), value);
    }

    public static <T, J> Specification<T> likeIgnoreCaseIfPresent(
            SingularAttribute<? super T, J> joinAttribute,
            SingularAttribute<? super J, String> attribute,
            String value
    ) {
        return likeIgnoreCaseIfPresent(root -> root.get(joinAttribute).get(attribute), value);
    }

    public static <T> Specification<T> likeIgnoreCaseIfPresent(
            Function<Root<T>, Expression<String>> expressionProvider,
            String value
    ) {
        if (value == null || value.isBlank()) return null;

        return (root, query, cb) -> cb.like(
                cb.lower(expressionProvider.apply(root)),
                likeLowerCasePattern(value)
        );
    }

    private static String likeLowerCasePattern(String value) {
        return "%" + value.trim().toLowerCase() + "%";
    }
}
