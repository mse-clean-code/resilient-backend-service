package clc.resilient.backend.service.data.objects;

import jakarta.persistence.*;
import lombok.Data;
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
    private String iso_639_1;
    private boolean isPrivate;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "movie_list_id")
    private List<MovieRelation> items;
    private int number_of_items;
    private String backdrop_path;
    public int getNumber_of_items() {
        return items.size();
    }
}
