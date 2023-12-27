package clc.resilient.backend.service.list.objects;

import clc.resilient.backend.service.movie.objects.MovieBody;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;


@Getter
@Setter
public class ListItem {

    private Integer id;
    private String name;
    private String description;
    private String iso_639_1;
    private boolean isPrivate;
    private List<MovieBody> items;
    private int number_of_items;
    private String backdrop_path;

    public int getNumber_of_items() {
        return items.size();
    }
}
