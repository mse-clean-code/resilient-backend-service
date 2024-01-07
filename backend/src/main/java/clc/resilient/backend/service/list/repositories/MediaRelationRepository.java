package clc.resilient.backend.service.list.repositories;

import clc.resilient.backend.service.list.entities.MediaRelation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaRelationRepository extends JpaRepository<MediaRelation, Long> {
}
