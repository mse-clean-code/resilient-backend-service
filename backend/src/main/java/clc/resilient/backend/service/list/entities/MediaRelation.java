package clc.resilient.backend.service.list.entities;

import clc.resilient.backend.service.list.validators.MediaRelationExists;
import clc.resilient.backend.service.list.validators.groups.ListServiceValidation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "movie_relation")
@Getter
@Setter
@MediaRelationExists(groups = ListServiceValidation.class)
public class MediaRelation {
    @Id
    @GeneratedValue
    Long id;

    @NotNull
    Long mediaId;
    @NotNull
    @NotEmpty
    String mediaType;

    @ManyToOne
    @JoinColumn(name = "movie_list_id")
    private MovieList movieList;

    @Transient
    private Map<String, Object> apiData = new HashMap<>(0);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MediaRelation that)) return false;
        return Objects.equals(mediaId, that.mediaId) && Objects.equals(mediaType, that.mediaType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mediaId, mediaType);
    }
}
