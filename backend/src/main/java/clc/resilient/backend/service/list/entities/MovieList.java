package clc.resilient.backend.service.list.entities;

import clc.resilient.backend.service.list.validators.MovieListConstraint;
import clc.resilient.backend.service.list.validators.groups.CreateListValidation;
import clc.resilient.backend.service.list.validators.groups.ListServiceValidation;
import clc.resilient.backend.service.list.validators.groups.UpdateListValidation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

// TODO: Remove JsonProperty annotations!

@Entity
@Table(name = "movie_list")
@Getter
@Setter
@MovieListConstraint(groups = ListServiceValidation.class)
public class MovieList {
    @Id
    @GeneratedValue
    @Null(groups = CreateListValidation.class)
    @NotNull(groups = UpdateListValidation.class)
    private Long id;

    @NotNull
    @NotEmpty
    private String name;
    private String description;
    private String iso6391;
    private boolean visible;
    private String backdrop_path;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "movie_list_id")
    private Set<MediaRelation> items = new HashSet<>(0);
    private int numberOfItems;
}