package clc.resilient.backend.service.configuration;

import clc.resilient.backend.service.list.ListMapper;
import clc.resilient.backend.service.list.dtos.MediaItemDTO;
import clc.resilient.backend.service.list.dtos.MovieListDTO;
import clc.resilient.backend.service.list.services.MovieListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-07
 */
@Component
@Profile("datagen") // Use environment "spring.profiles.active=datagen" to perform data generation
public class DataGenConfiguration {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ListMapper mapper;
    private final MovieListService service;

    public DataGenConfiguration(ListMapper mapper, MovieListService service) {
        this.mapper = mapper;
        this.service = service;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void generate() {
        logger.info("Generating test data...");
        var createList = MovieListDTO.builder()
            .description("Some hits of 2023!")
            .name("My Cool List")
            .visible(false)
            .build();
        var movie = mapper.movieListToEntity(createList);
        movie = service.createList(movie);

        var createItems = List.of(
            MediaItemDTO.builder().mediaId(940721L).mediaType("movie").build(),
            MediaItemDTO.builder().mediaId(346698L).mediaType("movie").build(),
            MediaItemDTO.builder().mediaId(872585L).mediaType("movie").build()
        );
        var items = mapper.mediaItemToEntity(createItems);
        service.addItemsToList(movie.getId(), items);
        logger.info("Generation finished!");
    }
}
