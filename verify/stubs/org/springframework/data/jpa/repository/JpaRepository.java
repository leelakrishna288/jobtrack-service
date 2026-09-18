package org.springframework.data.jpa.repository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface JpaRepository<T, ID> {
  T save(T entity);
  T saveAndFlush(T entity);
  Optional<T> findById(ID id);
  Page<T> findAll(Pageable pageable);
}
