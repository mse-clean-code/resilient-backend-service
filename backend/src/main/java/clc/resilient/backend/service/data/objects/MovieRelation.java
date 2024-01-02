package clc.resilient.backend.service.data.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "movie_relation")
@Getter
@Setter
public class MovieRelation {
    @Id
    Long media_id;
    String media_type;

    @ManyToOne
    @JoinColumn(name = "movie_list_id")
    @JsonIgnore
    private MovieList movieList;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true; // If it's the same instance, return true
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false; // If the object is null or of a different class, return false
        }
        // Cast the object to the appropriate type
        MovieRelation otherMovie = (MovieRelation) obj;
        // Compare the movie_id attribute
        return this.getMedia_id().equals(otherMovie.getMedia_id());
    }
}
