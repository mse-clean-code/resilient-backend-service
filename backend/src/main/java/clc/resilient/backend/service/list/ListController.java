package clc.resilient.backend.service.list;

import clc.resilient.backend.service.list.dtos.MediaItemsDTO;
import clc.resilient.backend.service.list.dtos.MovieListDTO;
import clc.resilient.backend.service.list.dtos.PaginatedResponseDTO;
import clc.resilient.backend.service.list.dtos.ResponseDTO;
import clc.resilient.backend.service.list.services.MovieListService;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
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
import java.util.function.Supplier;

/**
 * @author Kacper Urbaniec
 * @version 2023-12-23
 */

@Validated
@RestController
public class ListController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final MovieListService movieListService;
    private final ListMapper mapper;

    public ListController(MovieListService movieListService, ListMapper mapper) {
        this.movieListService = movieListService;
        this.mapper = mapper;
    }

    //region movie list

    @Retry(name = ListResilience.LIST_RETRY, fallbackMethod = "retryFallbackCompletion")
    @CircuitBreaker(name = ListResilience.LIST_CIRCUIT_BREAKER, fallbackMethod = "circuitBreakerFallbackCompletion")
    @TimeLimiter(name = ListResilience.LIST_TIME_LIMITER, fallbackMethod = "timeLimiterFallback")
    @RequestMapping({"/tmdb/4/account/{account_id}/lists"})
    public CompletionStage<ResponseEntity<?>> accountLists(
        @SuppressWarnings("unused") @PathVariable String account_id
    ) {
        return catchValidationAndNotFoundExAsync(() -> {
            logger.debug("accountLists()");
            var lists = movieListService.getAllWithoutItems();
            var listDtos = mapper.movieListToDto(lists);
            var response = PaginatedResponseDTO.builder()
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
    public ResponseEntity<?> list(
        @PathVariable("list_id") @NotNull Long listId
    ) {
        return catchValidationAndNotFoundEx(() -> {
            logger.debug("list({})", listId);
            var list = movieListService.getWithItems(listId);
            var listDto = mapper.movieListToDto(list);
            return ResponseEntity.ok(listDto);
        });
    }

    @Retry(name = ListResilience.LIST_RETRY, fallbackMethod = "retryFallbackCompletion")
    @CircuitBreaker(name = ListResilience.LIST_CIRCUIT_BREAKER, fallbackMethod = "circuitBreakerFallbackCompletion")
    @TimeLimiter(name = ListResilience.LIST_TIME_LIMITER, fallbackMethod = "timeLimiterFallback")
    @PostMapping("/tmdb/4/list")
    public CompletionStage<ResponseEntity<?>> createList(
        @RequestBody @NotNull MovieListDTO createListDto
    ) {
        logger.debug("createList({})", createListDto);
        return catchValidationAndNotFoundExAsync(() -> {
            var list = mapper.movieListToEntity(createListDto);
            list = movieListService.createList(list);
            var listDto = mapper.movieListToDto(list);
            var response = ResponseDTO.builder()
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
    public ResponseEntity<?> updateList(
        @PathVariable("list_id") @NotNull Long listId,
        @RequestBody @NotNull MovieListDTO updateListDto
    ) {
        return catchValidationAndNotFoundEx(() -> {
            logger.debug("updateList({}, {})", listId, updateListDto);
            updateListDto.setId(listId);
            var list = mapper.movieListToEntity(updateListDto);
            list = movieListService.updateList(list);
            var listDto = mapper.movieListToDto(list);
            var response = ResponseDTO.builder()
                .success(true)
                .statusMessage("The item/record was updated successfully.")
                .id(listDto.getId())
                .movieListDTO(listDto)
                .build();
            return ResponseEntity.ok(response);
        });
    }

    @Retry(name = ListResilience.LIST_RETRY, fallbackMethod = "retryFallback")
    @CircuitBreaker(name = ListResilience.LIST_CIRCUIT_BREAKER, fallbackMethod = "circuitBreakerFallback")
    @DeleteMapping("/tmdb/4/{list_id}")
    public ResponseEntity<?> deleteList(
        @PathVariable("list_id") @NotNull Long listId
    ) {
        return catchValidationAndNotFoundEx(() -> {
            logger.debug("deleteList({})", listId);
            movieListService.deleteList(listId);
            var response = ResponseDTO.builder()
                .success(true)
                .statusMessage("The item/record was deleted successfully.")
                .id(listId)
                .build();
            return ResponseEntity.ok(response);
        });
    }

    //endregion

    //region movie list item

    @Retry(name = ListResilience.LIST_RETRY, fallbackMethod = "retryFallback")
    @CircuitBreaker(name = ListResilience.LIST_CIRCUIT_BREAKER, fallbackMethod = "circuitBreakerFallback")
    @PostMapping("/tmdb/4/list/{list_id}/items")
    public ResponseEntity<?> addItemsToList(
        @PathVariable("list_id") @NotNull Long listId,
        @RequestBody @NotNull MediaItemsDTO itemsDto
    ) {
        return catchValidationAndNotFoundEx(() -> {
            logger.debug("addItemsToList({}, {})", listId, itemsDto);
            var items = mapper.mediaItemToEntity(itemsDto.getItems());
            var list = movieListService.addItemsToList(listId, items);
            var listDto = mapper.movieListToDto(list);
            var response = ResponseDTO.builder()
                .success(true)
                .statusMessage("The item/record was updated successfully.")
                .id(listDto.getId())
                .movieListDTO(listDto)
                .build();
            return ResponseEntity.ok(response);
        });
    }

    @Retry(name = ListResilience.LIST_RETRY, fallbackMethod = "retryFallback")
    @CircuitBreaker(name = ListResilience.LIST_CIRCUIT_BREAKER, fallbackMethod = "circuitBreakerFallback")
    @DeleteMapping("/tmdb/4/list/{list_id}/items")
    public ResponseEntity<?> removeItemsFromList(
        @PathVariable("list_id") @NotNull Long listId,
        @RequestBody @NotNull MediaItemsDTO itemsDto
    ) {
        return catchValidationAndNotFoundEx(() -> {
            logger.debug("removeItemsFromList({}, {})", listId, itemsDto);
            var items = mapper.mediaItemToEntity(itemsDto.getItems());
            var list = movieListService.removeItemsFromList(listId, items);
            var listDto = mapper.movieListToDto(list);
            var response = ResponseDTO.builder()
                .success(true)
                .statusMessage("The item/record was updated successfully.")
                .id(listDto.getId())
                .movieListDTO(listDto)
                .build();
            return ResponseEntity.ok(response);
        });
    }

    //endregion

    //================================================================================
    // Error Handling which does not trigger Resilience4j Fallbacks in some cases
    //================================================================================

    ResponseEntity<?> catchValidationAndNotFoundEx(
        Supplier<ResponseEntity<?>> supplier
    ) {
        try {
            return supplier.get();
        } catch (ConstraintViolationException | EntityNotFoundException ex) {
            logger.debug("catchValidationAndNotFound", ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    CompletableFuture<ResponseEntity<?>> catchValidationAndNotFoundExAsync(
        Supplier<ResponseEntity<?>> supplier
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (ConstraintViolationException | EntityNotFoundException ex) {
                logger.debug("catchValidationAndNotFound", ex);
                return ResponseEntity.badRequest().body(ex.getMessage());
            }
        });
    }

    //================================================================================
    // Resilience4j Fallbacks
    //================================================================================

    //region fallbacks

    /**
     * Function that is executed when all retries attempts have exhausted.
     */
    @SuppressWarnings("unused")
    public ResponseEntity<String> retryFallback(Exception ex) {
        logger.warn("retryFallback {}", ex.getMessage());
        return new ResponseEntity<>("all retries have exhausted", HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Function that is executed when circuit breaker is open.
     */
    @SuppressWarnings("unused")
    public ResponseEntity<String> circuitBreakerFallback(CallNotPermittedException ex) {
        // Note: Specific exception type is important! Else retry fallback will be always executed
        // https://resilience4j.readme.io/docs/getting-started-3#fallback-methods
        logger.warn("circuitBreakerFallback {}", ex.getMessage());
        return new ResponseEntity<>("service is unavailable", HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Function that is executed when Request timed out / time limiter is triggered
     */
    @SuppressWarnings("unused")
    public CompletionStage<ResponseEntity<String>> timeLimiterFallback(TimeoutException ex) {
        logger.warn("timeLimiterFallback {}", ex.getMessage());
        return CompletableFuture.completedFuture(new ResponseEntity<>("Request timed out", HttpStatus.REQUEST_TIMEOUT));
    }

    /**
     * Function that is executed when all retries attempts have exhausted.
     */
    @SuppressWarnings("unused")
    public CompletionStage<ResponseEntity<String>> retryFallbackCompletion(Exception ex) {
        logger.warn("retryFallback {}", ex.getMessage());
        return CompletableFuture.completedFuture(new ResponseEntity<>("all retries have exhausted", HttpStatus.SERVICE_UNAVAILABLE));
    }

    /**
     * Function that is executed when circuit breaker is open.
     */
    @SuppressWarnings("unused")
    public CompletionStage<ResponseEntity<String>> circuitBreakerFallbackCompletion(CallNotPermittedException ex) {
        // Note: Specific exception type is important! Else retry fallback will be always executed
        // https://resilience4j.readme.io/docs/getting-started-3#fallback-methods
        logger.warn("circuitBreakerFallback {}", ex.getMessage());
        return CompletableFuture.completedFuture(new ResponseEntity<>("service is unavailable", HttpStatus.SERVICE_UNAVAILABLE));
    }

    //endregion
}
