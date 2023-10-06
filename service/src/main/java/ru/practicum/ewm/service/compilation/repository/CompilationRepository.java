package ru.practicum.ewm.service.compilation.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.service.compilation.model.Compilation;

import java.util.List;

@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    @Query("select c " +
           "from Compilation c " +
           "where ((:pinned) is null or c.pinned = :pinned)")
    List<Compilation> findAllByPinnedIsNullOrPinned(@Param("pinned") Boolean pinned, Pageable pageable);
}
