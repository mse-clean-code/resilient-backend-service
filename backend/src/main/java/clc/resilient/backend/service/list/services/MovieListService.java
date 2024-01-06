package clc.resilient.backend.service.list.services;

import clc.resilient.backend.service.list.entities.MediaRelation;
import clc.resilient.backend.service.list.entities.MovieList;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-06
 */
public interface MovieListService {
    List<MovieList> getAllWithoutItems();

    MovieList getWithItems(@NotNull Long id);

    MovieList createList(@Valid MovieList addList);

    MovieList updateList(MovieList updateList);

    void deleteList(@NotNull Long id);

    MovieList addItemsToList(@NotNull Long id, @NotNull Set<MediaRelation> mediaItems);

    MovieList removeItemsFromList(@NotNull Long id, @NotNull Set<MediaRelation> mediaItems);
}
