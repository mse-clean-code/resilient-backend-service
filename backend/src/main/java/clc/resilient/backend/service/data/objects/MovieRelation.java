package clc.resilient.backend.service.data.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class MovieRelation {
    @Id
    Long mediaId;
    String mediaType;

    @ManyToOne
    @JoinColumn(name = "movie_list_id")
    @JsonIgnore
    private MovieList movieList;

    @Transient
    private Map<String, Object> apiData = new HashMap<>(0);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovieRelation that)) return false;
        return Objects.equals(mediaId, that.mediaId) && Objects.equals(mediaType, that.mediaType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mediaId, mediaType);
    }
}
