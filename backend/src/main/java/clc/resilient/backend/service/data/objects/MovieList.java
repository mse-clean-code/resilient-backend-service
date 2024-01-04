package clc.resilient.backend.service.data.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "movie_list")
@Getter
@Setter
public class MovieList {
    @Id
    private Long id;
    private String name;
    private String description;
    @JsonProperty("iso_639_1")
    private String iso6391;
    private boolean isPrivate;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "movie_list_id")
    private List<MovieRelation> items;
    @JsonProperty("number_of_items")
    private int numberOfItems;
    private String backdrop_path;

    public int getNumberOfItems() {
        return items.size();
    }
}
