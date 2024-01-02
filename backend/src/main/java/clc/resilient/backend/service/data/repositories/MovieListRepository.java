package clc.resilient.backend.service.data.repositories;

import clc.resilient.backend.service.data.objects.MovieList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieListRepository extends JpaRepository<MovieList, Long> {

}
