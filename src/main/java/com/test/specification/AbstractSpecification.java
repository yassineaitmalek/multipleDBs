package com.test.specification;

import java.util.Optional;
import java.util.function.Function;

import org.springframework.data.jpa.domain.Specification;

public abstract class AbstractSpecification {

  public static String like(String element) {
    return Optional.ofNullable(element).map(e -> "%" + e + "%").orElseGet(() -> "%%");
  }

  public static <T, U> Optional<Specification<U>> transformer(T object, Function<T, Specification<U>> mapper) {
    return Optional.ofNullable(object)
        .map(e -> Optional.ofNullable(mapper.apply(e)))
        .orElseGet(Optional::empty);
  }

  public static <T> Specification<T> distinct() {
    return (root, query, builder) -> {
      query.distinct(true);
      return builder.isNotNull(root);
    };
  }

  public static <T> Specification<T> whereDistinct() {
    return Specification.where(distinct());
  }

  public static <T> Specification<T> unitSpecification() {
    return (root, query, builder) -> builder.isNotNull(root);

  }

}
