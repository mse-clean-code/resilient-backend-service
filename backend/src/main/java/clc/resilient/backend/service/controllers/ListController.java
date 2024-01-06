package clc.resilient.backend.service.controllers;

import clc.resilient.backend.service.controllers.messages.ResponseMessage;
import clc.resilient.backend.service.controllers.messages.ResponseOfMovieListsPaginated;
import clc.resilient.backend.service.data.services.MovieListQueryService;
import clc.resilient.backend.service.list.ListMapper;
import clc.resilient.backend.service.list.dtos.MediaItemsDTO;
import clc.resilient.backend.service.list.dtos.MovieListDTO;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;

/**
 * @author Kacper Urbaniec
 * @version 2023-12-23
 */

// TODO: Add interface
@Validated
@RestController
public class ListController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final MovieListQueryService movieListQueryService;
    private final ListMapper mapper;

    public ListController(MovieListQueryService movieListQueryService, ListMapper mapper) {
        this.movieListQueryService = movieListQueryService;
        this.mapper = mapper;
    }

    //region movie list

    @Retry(name = ListResilience.LIST_RETRY, fallbackMethod = "retryFallbackCompletion")
    @CircuitBreaker(name = ListResilience.LIST_CIRCUIT_BREAKER, fallbackMethod = "circuitBreakerFallbackCompletion")
    @TimeLimiter(name = ListResilience.LIST_TIME_LIMITER, fallbackMethod = "timeLimiterFallback")
    @RequestMapping({"/tmdb/4/account/{account_id}/lists"})
    public CompletionStage<ResponseEntity<ResponseOfMovieListsPaginated>> accountLists(
        @SuppressWarnings("unused") @PathVariable String account_id
    ) {
        /*
        {
            "page": 1,
            "results": [
                {
                    "account_object_id": "658add5e5aba3266b0bab7e8",
                    "adult": 0,
                    "average_rating": 0.0,
                    "backdrop_path": null,
                    "created_at": "2023-12-27 10:58:05 UTC",
                    "description": "test",
                    "featured": 0,
                    "id": 8284698,
                    "iso_3166_1": "US",
                    "iso_639_1": "en",
                    "name": "test16",
                    "number_of_items": 0,
                    "poster_path": null,
                    "public": 1,
                    "revenue": 0,
                    "runtime": "0",
                    "sort_by": 1,
                    "updated_at": "2023-12-27 10:58:05 UTC"
                },
                {
                    "account_object_id": "658add5e5aba3266b0bab7e8",
                    "adult": 0,
                    "average_rating": 0.0,
                    "backdrop_path": null,
                    "created_at": "2023-12-27 10:58:01 UTC",
                    "description": "test",
                    "featured": 0,
                    "id": 8284696,
                    "iso_3166_1": "US",
                    "iso_639_1": "en",
                    "name": "test15",
                    "number_of_items": 0,
                    "poster_path": null,
                    "public": 1,
                    "revenue": 0,
                    "runtime": "0",
                    "sort_by": 1,
                    "updated_at": "2023-12-27 10:58:01 UTC"
                },
            ],
            "total_pages": 1,
            "total_results": 19
        }
         */
        logger.debug("accountLists()");
        return CompletableFuture.supplyAsync(() -> {
            var lists = movieListQueryService.getAllWithoutItems();
            var listDtos = mapper.movieListToDto(lists);
            var response = ResponseOfMovieListsPaginated.builder()
                .page(1)
                .totalPages(1)
                .totalResults(listDtos.size())
                .results(listDtos)
                .build();
            return ResponseEntity.ok(response);
        });
    }

    @Retry(name = ListResilience.LIST_RETRY, fallbackMethod = "retryFallback")
    @CircuitBreaker(name = ListResilience.LIST_CIRCUIT_BREAKER, fallbackMethod = "circuitBreakerFallback")
    @GetMapping("/tmdb/4/list/{list_id}")
    public ResponseEntity<MovieListDTO> list(
        @PathVariable("list_id") @NotNull Long listId
    ) {
        logger.debug("list({})", listId);
        var list = movieListQueryService.getWithItems(listId);
        var listDto = mapper.movieListToDto(list);
        return ResponseEntity.ok(listDto);
    }

    @Retry(name = ListResilience.LIST_RETRY, fallbackMethod = "retryFallbackCompletion")
    @CircuitBreaker(name = ListResilience.LIST_CIRCUIT_BREAKER, fallbackMethod = "circuitBreakerFallbackCompletion")
    @TimeLimiter(name = ListResilience.LIST_TIME_LIMITER, fallbackMethod = "timeLimiterFallback")
    @PostMapping("/tmdb/4/list")
    public CompletionStage<ResponseEntity<ResponseMessage>> createList(
        @RequestBody @NotNull MovieListDTO createListDto
    ) {
        logger.debug("createList({})", createListDto);
        return CompletableFuture.supplyAsync(() -> {
            var list = mapper.movieListToEntity(createListDto);
            list = movieListQueryService.createList(list);
            var listDto = mapper.movieListToDto(list);
            var response = ResponseMessage.builder()
                .success(true)
                .statusMessage("The item/record was created successfully.")
                .id(listDto.getId())
                .movieListDTO(listDto)
                .build();
            return ResponseEntity.ok(response);
        });
    }

    @Retry(name = ListResilience.LIST_RETRY, fallbackMethod = "retryFallback")
    @CircuitBreaker(name = ListResilience.LIST_CIRCUIT_BREAKER, fallbackMethod = "circuitBreakerFallback")
    @PutMapping("/tmdb/4/list/{list_id}")
    public ResponseEntity<ResponseMessage> updateList(
        @PathVariable("list_id") @NotNull String listId,
        @RequestBody @NotNull MovieListDTO updateListDto
    ) {
        logger.debug("updateList({}, {})", listId, updateListDto);
        var list = mapper.movieListToEntity(updateListDto);
        list = movieListQueryService.updateList(list);
        var listDto = mapper.movieListToDto(list);
        var response = ResponseMessage.builder()
            .success(true)
            .statusMessage("The item/record was updated successfully.")
            .id(listDto.getId())
            .movieListDTO(listDto)
            .build();
        return ResponseEntity.ok(response);
    }

    @Retry(name = ListResilience.LIST_RETRY, fallbackMethod = "retryFallback")
    @CircuitBreaker(name = ListResilience.LIST_CIRCUIT_BREAKER, fallbackMethod = "circuitBreakerFallback")
    @DeleteMapping("/tmdb/4/{list_id}")
    public ResponseEntity<ResponseMessage> deleteList(
        @PathVariable("list_id") @NotNull Long listId
    ) {
        logger.debug("deleteList({})", listId);
        movieListQueryService.deleteList(listId);
        var response = ResponseMessage.builder()
            .success(true)
            .statusMessage("The item/record was deleted successfully.")
            .id(listId)
            .build();
        return ResponseEntity.ok(response);
    }

    //endregion

    //region movie list item

    @Retry(name = ListResilience.LIST_RETRY, fallbackMethod = "retryFallback")
    @CircuitBreaker(name = ListResilience.LIST_CIRCUIT_BREAKER, fallbackMethod = "circuitBreakerFallback")
    @PostMapping("/tmdb/4/list/{list_id}/items")
    public ResponseEntity<ResponseMessage> addItemsToList(
        @PathVariable("list_id") @NotNull Long listId,
        @RequestBody @NotNull MediaItemsDTO itemsDto
    ) {
        logger.debug("addItemsToList({}, {})", listId, itemsDto);
        var items = mapper.mediaItemToEntity(itemsDto.getItems());
        var list = movieListQueryService.addItemsToList(listId, items);
        var listDto = mapper.movieListToDto(list);
        var response = ResponseMessage.builder()
            .success(true)
            .statusMessage("The item/record was updated successfully.")
            .id(listDto.getId())
            .movieListDTO(listDto)
            .build();
        return ResponseEntity.ok(response);
    }

    @Retry(name = ListResilience.LIST_RETRY, fallbackMethod = "retryFallback")
    @CircuitBreaker(name = ListResilience.LIST_CIRCUIT_BREAKER, fallbackMethod = "circuitBreakerFallback")
    @DeleteMapping("/tmdb/4/list/{list_id}/items")
    public ResponseEntity<ResponseMessage> removeItemsFromList(
        @PathVariable("list_id") @NotNull Long listId,
        @RequestBody @NotNull MediaItemsDTO itemsDto
    ) {
        logger.debug("removeItemsFromList({}, {})", listId, itemsDto);
        var items = mapper.mediaItemToEntity(itemsDto.getItems());
        var list = movieListQueryService.removeItemsFromList(listId, items);
        var listDto = mapper.movieListToDto(list);
        var response = ResponseMessage.builder()
            .success(true)
            .statusMessage("The item/record was updated successfully.")
            .id(listDto.getId())
            .movieListDTO(listDto)
            .build();
        return ResponseEntity.ok(response);
    }

    //endregion

    /**
     * Function that is executed when all retries attempts have exhausted.
     */
    @SuppressWarnings("unused")
    public ResponseEntity<String> retryFallback(Exception ex) {
        logger.debug("retryFallback({})", ex.getMessage());
        return new ResponseEntity<>("all retries have exhausted", HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Function that is executed when circuit breaker is open.
     */
    @SuppressWarnings("unused")
    public ResponseEntity<String> circuitBreakerFallback(CallNotPermittedException ex) {
        // Note: Specific exception type is important! Else retry fallback will be always executed
        // https://resilience4j.readme.io/docs/getting-started-3#fallback-methods
        logger.debug("circuitBreakerFallback({})", ex.getMessage());
        return new ResponseEntity<>("service is unavailable", HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Function that is executed when Request timed out / time limiter is triggered
     */
    @SuppressWarnings("unused")
    public CompletionStage<ResponseEntity<String>> timeLimiterFallback(TimeoutException ex) {
        logger.debug("timeLimiterFallback({})", ex.getMessage());
        return CompletableFuture.completedFuture(new ResponseEntity<>("Request timed out", HttpStatus.REQUEST_TIMEOUT));
    }

    /**
     * Function that is executed when all retries attempts have exhausted.
     */
    @SuppressWarnings("unused")
    public CompletionStage<ResponseEntity<String>> retryFallbackCompletion(Exception ex) {
        logger.warn("retryFallback", ex);
        return CompletableFuture.completedFuture(new ResponseEntity<>("all retries have exhausted", HttpStatus.SERVICE_UNAVAILABLE));
    }

    /**
     * Function that is executed when circuit breaker is open.
     */
    @SuppressWarnings("unused")
    public CompletionStage<ResponseEntity<String>> circuitBreakerFallbackCompletion(CallNotPermittedException ex) {
        // Note: Specific exception type is important! Else retry fallback will be always executed
        // https://resilience4j.readme.io/docs/getting-started-3#fallback-methods
        logger.debug("circuitBreakerFallback({})", ex.getMessage());
        return CompletableFuture.completedFuture(new ResponseEntity<>("service is unavailable", HttpStatus.SERVICE_UNAVAILABLE));
    }
}
