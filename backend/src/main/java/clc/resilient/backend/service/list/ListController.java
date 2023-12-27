package clc.resilient.backend.service.list;

import clc.resilient.backend.service.list.objects.ListItem;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final ListData listData = ListData.getInstance();
    // Will be resolved in issue #3
    // TODO: Split to multiple endpoints
    // TODO: Adapt `tmdb-v4.json` if endpoint signature differs from original tmdb api
    //   Needed for swagger / openapi compatibility

    // Catches all list CRUD operations
    @RequestMapping({"/tmdb/4/list/**", "/tmdb/4/{_}"})
    @SuppressWarnings("MVCPathVariableInspection")
    public ResponseEntity<String> listActions(
        @RequestBody(required = false) String body,
        HttpMethod method, HttpServletRequest request,
        HttpServletResponse response
    ) {
        logger.debug("Custom List Action | {} | {}", method.name(), request.getRequestURI());
        throw new NotImplementedException();
    }
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
        var listItems = listData.getItems();
        int totalResults = listItems.size();
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.putIfAbsent("page", 1);
        responseBody.putIfAbsent("total_pages", 1);
        responseBody.putIfAbsent("total_results", totalResults);
        responseBody.putIfAbsent("results", listItems);
        return ResponseEntity.ok(responseBody);
    }

    // Add Movie to List
    @PostMapping("/tmdb/4/list/{list_id}/items")
    public ResponseEntity<ListItem> addItemToList(
            @PathVariable("list_id") String listId,
            @RequestBody ListItem listBody
    ) {
        // Implement logic to add a movie to the list with ID listId
        var updatedList = listData.add(listBody);
        return ResponseEntity.ok(updatedList);
    }

    // Get List Details
    @GetMapping("/tmdb/4/list/{list_id}")
    public ResponseEntity<ListItem> getListDetails(
            @PathVariable("list_id") int listId
    ) {
        try {
            var listItem = listData.getItem(listId);
            /* Awaited response
            {
                "average_rating": 0.0,
                "backdrop_path": null,
                "results": [],
                "comments": {},
                "created_by": {
                    "avatar_path": null,
                    "gravatar_hash": "b497b063bdf23ca14db469949f2584c8",
                    "id": "658add5e5aba3266b0bab7e8",
                    "name": "",
                    "username": "tasibalint"
                },
                "description": "new",
                "id": 8284609,
                "iso_3166_1": "US",
                "iso_639_1": "en",
                "item_count": 0,
                "name": "new list",
                "object_ids": {},
                "page": 1,
                "poster_path": null,
                "public": true,
                "revenue": 0,
                "runtime": 0,
                "sort_by": "original_order.asc",
                "total_pages": 1,
                "total_results": 0
            }*/
            return ResponseEntity.ok(listItem);
        } catch(NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    // Check Item Status
    @GetMapping("/tmdb/4/list/{list_id}/item_status")
    public ResponseEntity<String> getItemStatus(
            @PathVariable("list_id") String listId
    ) {
        // Implement logic to check item status in the list with ID listId
        return ResponseEntity.ok("Item status retrieved");
    }

    // Clear List
    @PostMapping("/tmdb/4/list/{list_id}/clear")
    public ResponseEntity<String> clearList(
            @PathVariable("list_id") String listId
    ) {
        // Implement logic to clear the list with ID listId
        return ResponseEntity.ok("List cleared");
    }

    // Create List
    @PostMapping("/tmdb/4/list")
    public ResponseEntity<Map> createList(
            @RequestBody ListItem requestBody
    ) {
        // Implement logic to create a new list
        //return {"success":true,"status_code":1,"status_message":"Success.","id":8284604}
        //
        var listItem = listData.add(requestBody);
        Map<String, Object> response = new HashMap<>();
        response.putIfAbsent("success", true);
        response.putIfAbsent("status_code", 1);
        response.putIfAbsent("status_message", "Success.");
        response.putIfAbsent("id", listItem.getId());
        return ResponseEntity.ok(response);
    }

    // Delete List
    @DeleteMapping("/tmdb/4/list/{list_id}")
    public ResponseEntity<String> deleteList(
            @PathVariable("list_id") int listId
    ) {
        listData.deleteItem(listId);
        // Implement logic to delete the list with ID listId
        return ResponseEntity.ok("List deleted");
    }


    // Remove Movie from List
    @PostMapping("/tmdb/4/list/{list_id}/remove_item")
    public ResponseEntity<String> removeItemFromList(
            @PathVariable("list_id") String listId,
            @RequestBody ListItem requestBody
    ) {
        // Implement logic to remove a movie from the list with ID listId
        return ResponseEntity.ok("Movie removed from the list");
    }
}
