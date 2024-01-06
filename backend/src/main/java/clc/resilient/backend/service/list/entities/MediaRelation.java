package clc.resilient.backend.service.list.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// TODO: Rename to MediaRelation?

@Entity
@Table(name = "movie_relation")
@Getter
@Setter
public class MediaRelation {
    @Id
    @GeneratedValue
    Long id;

    Long mediaId;
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
