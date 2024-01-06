package clc.resilient.backend.service.list.service;

import clc.resilient.backend.service.list.entities.MediaRelation;
import clc.resilient.backend.service.list.entities.MovieList;
import clc.resilient.backend.service.list.repositories.MovieListRepository;
import clc.resilient.backend.service.list.validators.groups.CreateListValidation;
import clc.resilient.backend.service.common.SimpleHttpServletRequest;
import clc.resilient.backend.service.common.TmdbClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.function.BiConsumer;

@Service
@Validated
public class MovieListQueryService {
    private final MovieListRepository movieListRepository;

    private final TmdbClient tmdbClient;

    private final ObjectMapper objectMapper;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public MovieListQueryService(MovieListRepository movieListRepository, TmdbClient tmdbClient, ObjectMapper objectMapper) {
        this.movieListRepository = movieListRepository;
        this.tmdbClient = tmdbClient;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<MovieList> getAllWithoutItems() {
        return movieListRepository.findAll();
    }

    @Transactional(readOnly = true)
    public MovieList getWithItems(@NotNull Long id) {
        var list = movieListRepository.findById(id).orElseThrow();
        fetchTmdbItems(list);
        return list;
    }

    @Transactional
    // Validate with specific group
    // https://reflectoring.io/bean-validation-with-spring-boot/
    // Required so hibernate does not call the @MovieListConstraint!
    @Validated({CreateListValidation.class})
    public MovieList createList(@Valid MovieList addList) {
        return movieListRepository.save(addList);
    }

    @Transactional
    // @Validated({UpdateListValidation.class})
    // public MovieList updateList(@Valid MovieList updateList) {
    public MovieList updateList(MovieList updateList) {
        // TODO: Handle not found
        var updateReference = movieListRepository.getReferenceById(updateList.getId());
        if (updateList.getName() != null && !updateList.getName().isEmpty())
            updateReference.setName(updateList.getName());
        if (updateList.getDescription() != null)
            updateReference.setDescription(updateList.getDescription());
        updateReference.setVisible(updateList.isVisible());
        if (updateList.getBackdrop_path() != null)
            updateReference.setBackdrop_path(updateList.getBackdrop_path());

        return updateList;
    }

    @Transactional
    public void deleteList(@NotNull Long id) {
        movieListRepository.deleteById(id);
    }


    @Transactional
    public MovieList addItemsToList(@NotNull Long id, Set<MediaRelation> mediaItems) {
        // TODO: Validate added items if really exist
        return modifyItemsInList(id, mediaItems, Set::addAll);
    }

    @Transactional
    public MovieList removeItemsFromList(@NotNull Long id, Set<MediaRelation> mediaItems) {
        return modifyItemsInList(id, mediaItems, Set::removeAll);
    }

    private MovieList modifyItemsInList(
        @NotNull Long id, Set<MediaRelation> mediaItems,
        BiConsumer<Set<MediaRelation>, Set<MediaRelation>> operation
    ) {
        // TODO: Exception handling
        var list = movieListRepository.findById(id).orElseThrow();
        operation.accept(list.getItems(), mediaItems);
        list.setNumberOfItems(list.getItems().size());
        fetchTmdbItems(list);
        return list;
    }

    public Set<MediaRelation> deleteMovie(MovieList toDeleteMovie) {
        Optional<MovieList> optionalItem = movieListRepository.findById(toDeleteMovie.getId());
        if (optionalItem.isPresent()) {
            MovieList item = optionalItem.get();
            Set<MediaRelation> toDeleteMovies = toDeleteMovie.getItems();
            Set<MediaRelation> movies = item.getItems();
            movies.removeAll(toDeleteMovies);
            item.setItems(movies);
            movieListRepository.saveAndFlush(item);
            return toDeleteMovie.getItems();
        } else {
            // Handle scenario where the item with the provided ID is not found.
            // You might want to throw an exception or handle it differently.
            return null;
        }
    }

    private void fetchTmdbItems(@NotNull MovieList list) {
        for (MediaRelation mediaItem : list.getItems()) {
            fetchTmdbItem(mediaItem);
        }
    }

    private void fetchTmdbItem(@NotNull MediaRelation item) {
        var id = item.getMediaId();
        var mediaType = item.getMediaType();

        String requestUrl;
        if (mediaType.equals("movie")) requestUrl = "/3/movie/" + id;
        else requestUrl = "/3/tv/" + id;

        var response = tmdbClient
            .fetchTmdbApi(HttpMethod.GET, requestUrl, new SimpleHttpServletRequest(), null);
        var json = response.getBody();

        try {
            @SuppressWarnings("unchecked")
            var data = (Map<String, Object>) objectMapper.readValue(json, Map.class);
            item.setApiData(data);
        } catch (JsonProcessingException ex) {
            logger.warn("fetchTmdbItem", ex);
        }
    }
}