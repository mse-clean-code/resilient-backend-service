package clc.resilient.backend.service.controllers;

import clc.resilient.backend.service.data.objects.MovieList;
import clc.resilient.backend.service.data.services.MovieListQueryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author Kacper Urbaniec
 * @version 2023-12-23
 */
@RestController
public class ListController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private MovieListQueryService movieListQueryService;

    @RequestMapping({"/tmdb/4/account/{account_id}/lists"})
    public ResponseEntity<Map> accountLists(
        @RequestBody(required = false) String body,
        String account_id,
        HttpMethod method,
        HttpServletRequest request,
        HttpServletResponse response
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
        logger.debug("Custom List Action | {} | {}", method.name(), request.getRequestURI());
        var movieLists = movieListQueryService.getAll();
        int totalResults = movieLists.size();
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.putIfAbsent("page", 1);
        responseBody.putIfAbsent("total_pages", 1);
        responseBody.putIfAbsent("total_results", totalResults);
        responseBody.putIfAbsent("results", movieLists);
        return ResponseEntity.ok(responseBody);
    }

    // Add Movie to List
    @PostMapping("/tmdb/4/list/{list_id}/items")
    public ResponseEntity<Map> addItemToList(
            @PathVariable("list_id") String listId,
            @RequestBody MovieList movieList
    ) {
        // Implement logic to add a movie to the list with ID listId
        var updatedList = movieListQueryService.add(movieList);
        /*
        {
            "success": true,
            "status_code": 1,
            "status_message": "Success.",
            "results": [
                {
                    "media_id": 11,
                    "media_type": "movie",
                    "success": true
                }
            ]
        }
         */
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.putIfAbsent("success", true);
        responseBody.putIfAbsent("status_code", 1);
        responseBody.putIfAbsent("status_message", "Success.");
        responseBody.putIfAbsent("results", movieList.getItems());
        return ResponseEntity.ok(responseBody);
    }
    // Add Movie to List
    @PutMapping("/tmdb/4/list/{list_id}")
    public ResponseEntity<Map> updateItem(
            @PathVariable("list_id") String listId,
            @RequestBody MovieList movieList
    ) {
        // Implement logic to add a movie to the list with ID listId
        var updatedList = movieListQueryService.add(movieList);
        /*
        {
            "success": true,
            "status_code": 1,
            "status_message": "Success.",
            "results": [
                {
                    "media_id": 11,
                    "media_type": "movie",
                    "success": true
                }
            ]
        }
         */
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.putIfAbsent("success", true);
        responseBody.putIfAbsent("status_code", 1);
        responseBody.putIfAbsent("status_message", "Success.");
        responseBody.putIfAbsent("results", movieList.getItems());
        return ResponseEntity.ok(responseBody);
    }
    // Get List Details
    @GetMapping("/tmdb/4/list/{list_id}")
    public ResponseEntity<MovieList> getListDetails(
            @PathVariable("list_id") Long listId
    ) {
        try {
            var MovieList = movieListQueryService.getItem(listId);
            /* Awaited response
            {
                "average_rating": 7.8,
                "backdrop_path": "/1aABIiqBY7yoQESE8qWvR0w9bJZ.jpg",
                "results": [
                    {
                        "adult": false,
                        "backdrop_path": "/1aABIiqBY7yoQESE8qWvR0w9bJZ.jpg",
                        "id": 265712,
                        "title": "Stand by Me Doraemon",
                        "original_language": "ja",
                        "original_title": "STAND BY ME ドラえもん",
                        "overview": "Sewashi and Doraemon find themselves way back in time and meet Nobita. It is up to Doraemon to take care of Nobita or else he will not return to the present.",
                        "poster_path": "/wc7XQbfx6EIQqCuvmBMt3aisb2Y.jpg",
                        "media_type": "movie",
                        "genre_ids": [
                            16,
                            10751,
                            878,
                            14
                        ],
                        "popularity": 67.03,
                        "release_date": "2014-08-08",
                        "video": false,
                        "vote_average": 7.3,
                        "vote_count": 482
                    },
                    {
                        "adult": false,
                        "backdrop_path": "/4qCqAdHcNKeAHcK8tJ8wNJZa9cx.jpg",
                        "id": 11,
                        "title": "Star Wars",
                        "original_language": "en",
                        "original_title": "Star Wars",
                        "overview": "Princess Leia is captured and held hostage by the evil Imperial forces in their effort to take over the galactic Empire. Venturesome Luke Skywalker and dashing captain Han Solo team together with the loveable robot duo R2-D2 and C-3PO to rescue the beautiful princess and restore peace and justice in the Empire.",
                        "poster_path": "/6FfCtAuVAW8XJjZ7eWeLibRLWTw.jpg",
                        "media_type": "movie",
                        "genre_ids": [
                            12,
                            28,
                            878
                        ],
                        "popularity": 106.454,
                        "release_date": "1977-05-25",
                        "video": false,
                        "vote_average": 8.205,
                        "vote_count": 19436
                    }
                ],
                "comments": {
                    "movie:265712": null,
                    "movie:11": null
                },
                "created_by": {
                    "avatar_path": null,
                    "gravatar_hash": "b497b063bdf23ca14db469949f2584c8",
                    "id": "658add5e5aba3266b0bab7e8",
                    "name": "",
                    "username": "tasibalint"
                },
                "description": "a",
                "id": 8284605,
                "iso_3166_1": "US",
                "iso_639_1": "en",
                "item_count": 2,
                "name": "test",
                "object_ids": {},
                "page": 1,
                "poster_path": null,
                "public": true,
                "revenue": 858459165,
                "runtime": 211,
                "sort_by": "original_order.asc",
                "total_pages": 1,
                "total_results": 2
            }*/
            return ResponseEntity.ok(MovieList);
        } catch(NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    // Create List
    @PostMapping("/tmdb/4/list")
    public ResponseEntity<Map> createList(
            @RequestBody MovieList requestBody
    ) {
        // Implement logic to create a new list
        //return {"success":true,"status_code":1,"status_message":"Success.","id":8284604}
        //
        var movieList = movieListQueryService.add(requestBody);
        Map<String, Object> response = new HashMap<>();
        response.putIfAbsent("success", true);
        response.putIfAbsent("status_code", 1);
        response.putIfAbsent("status_message", "Success.");
        response.putIfAbsent("id", movieList.getId());
        return ResponseEntity.ok(response);
    }

    // Delete List
    @DeleteMapping("/tmdb/4/list/{list_id}")
    public ResponseEntity<Map> deleteList(
            @PathVariable("list_id") Long listId
    ) {
        movieListQueryService.deleteItem(listId);
        // Implement logic to delete the list with ID listId
        Map<String, Object> response = new HashMap<>();
        response.putIfAbsent("success", true);
        response.putIfAbsent("status_code", 13);
        response.putIfAbsent("status_message", "The item/record was deleted successfully.");
        return ResponseEntity.ok(response);
    }


    // Remove Movie from List
    @DeleteMapping("/tmdb/4/list/{list_id}/items")
    public ResponseEntity<Map> removeItemFromList(
            @PathVariable("list_id") int listId,
            @RequestBody MovieList requestBody
    ) {
        var removedMovies = movieListQueryService.deleteMovie(requestBody);
        // Implement logic to remove a movie from the list with ID listId
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.putIfAbsent("success", true);
        responseBody.putIfAbsent("status_code", 1);
        responseBody.putIfAbsent("status_message", "Success.");
        responseBody.putIfAbsent("results", removedMovies);
        return ResponseEntity.ok(responseBody);
    }
}
