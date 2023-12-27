package clc.resilient.backend.service.movie.objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovieBody {
    int movie_id;
    String media_type;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true; // If it's the same instance, return true
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false; // If the object is null or of a different class, return false
        }
        // Cast the object to the appropriate type
        MovieBody otherMovie = (MovieBody) obj;
        // Compare the movie_id attribute
        return this.getMovie_id() == otherMovie.getMovie_id();
    }
}
