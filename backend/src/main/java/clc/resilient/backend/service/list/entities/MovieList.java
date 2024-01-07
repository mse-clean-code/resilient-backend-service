package clc.resilient.backend.service.list.entities;

import clc.resilient.backend.service.list.validators.groups.CreateListValidation;
import clc.resilient.backend.service.list.validators.groups.UpdateListValidation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.groups.Default;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "movie_list")
@Getter
@Setter
public class MovieList {
    @Id
    @GeneratedValue
    @Null(groups = CreateListValidation.class)
    @NotNull(groups = UpdateListValidation.class)
    private Long id;

    @NotNull(groups = {CreateListValidation.class})
    @NotEmpty(groups = {CreateListValidation.class})
    private String name;
    private String description;
    private String iso6391;
    private Boolean visible;
    private String backdropPath;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "movie_list_id")
    private Set<MediaRelation> items = new HashSet<>(0);
    private int numberOfItems;

    @Override
    public String toString() {
        return "MovieList{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", iso6391='" + iso6391 + '\'' +
            ", visible=" + visible +
            ", backdropPath='" + backdropPath + '\'' +
            ", numberOfItems=" + numberOfItems +
            '}';
    }
}
