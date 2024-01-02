package clc.resilient.backend.service.data.repositories;

import clc.resilient.backend.service.data.objects.MovieRelation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRelationRepository  extends JpaRepository<MovieRelation, Long> {
}
