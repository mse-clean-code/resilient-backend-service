package clc.resilient.backend.service.list;

import clc.resilient.backend.service.list.objects.ListItem;
import clc.resilient.backend.service.movie.objects.MovieBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ListData {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static ListData instance = null;

    private ListData() {
    }

    public static ListData getInstance() {
        if(instance == null) {
            instance = new ListData();
        }
        return instance;
    }

    private List<ListItem> items = new ArrayList<>();

    public List<ListItem> getItems() {
        return items;
    }

    ListItem getItem(int id) throws NoSuchElementException{
        for(var item : items) {
            if(item.getId().equals(id)) {
                return item;
            }
        }
        throw new NoSuchElementException();
    }

    ListItem add(ListItem unknownItem) {
        if(unknownItem.getId() == null) {
            logger.debug("Added item");
            //list id must be bigger then 0 in order to return on ui to my-list page
            unknownItem.setId(items.size()+1);
            unknownItem.setItems(new ArrayList<>());
            items.add(unknownItem);
        }
        for(int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            if(item.getId().equals(unknownItem.getId())) {
                logger.debug("Updated item " + item.getId());
                //Add movies as into the set
                if(unknownItem.getItems() != null) {
                    var pastItems = item.getItems();
                    for(MovieBody movie : unknownItem.getItems()) {
                        if(!pastItems.contains(movie)) {
                            pastItems.add(movie);
                        }
                    }
                    item.setItems(pastItems);
                }

                //set changeable attributes

                if(unknownItem.getName() != null) {
                    item.setName(unknownItem.getName());
                }
                if(unknownItem.getDescription() != null) {
                    item.setDescription(unknownItem.getDescription());
                }
                item.setPrivate(unknownItem.isPrivate());
                if(unknownItem.getBackdrop_path() != null) {
                    item.setBackdrop_path(unknownItem.getBackdrop_path());
                }
                items.set(i, item);
                unknownItem = items.get(i);
            }
        }
        return unknownItem;
    }

    List<MovieBody> deleteMovie(ListItem toDeleteMovie) {
        for(int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            if (item.getId().equals(toDeleteMovie.getId())) {
                var toDeleteMovies = toDeleteMovie.getItems();
                var movies = item.getItems();
                movies.removeAll(toDeleteMovies);
                item.setItems(movies);
                items.set(i, item);
            }
        }
        return toDeleteMovie.getItems();
    }

    void deleteItem(Integer id) {
        int i;
        for(i = 0; i < items.size(); i++) {
            var item = items.get(i);
            if(item.getId().equals(id)) {
                break;
            }
        }
        if(i < items.size()) {
            logger.debug("item removed " + items.get(i).getId());
            items.remove(i);
        }
    }
}
