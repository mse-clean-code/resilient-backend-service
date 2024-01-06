package clc.resilient.backend.service.list.repositories;

import clc.resilient.backend.service.list.entities.MovieList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieListRepository extends JpaRepository<MovieList, Long> {
}
