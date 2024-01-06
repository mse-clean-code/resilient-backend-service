package clc.resilient.backend.service.data.objects;

import clc.resilient.backend.service.list.validators.ListServiceValidation;
import clc.resilient.backend.service.list.validators.MovieListConstraint;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// TODO: Remove JsonProperty annotations!

@Entity
@Table(name = "movie_list")
@Getter
@Setter
@MovieListConstraint(groups = ListServiceValidation.class)
public class MovieList {
    @Id
    @GeneratedValue
    private Long id;
    @NotNull
    @NotEmpty
    private String name;
    private String description;
    @JsonProperty("iso_639_1")
    private String iso6391;
    private boolean isPrivate;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "movie_list_id")
    private List<MovieRelation> items = new ArrayList<>(0);
    @JsonProperty("number_of_items")
    private int numberOfItems;
    private String backdrop_path;

    public int getNumberOfItems() {
        return items.size();
    }
}
