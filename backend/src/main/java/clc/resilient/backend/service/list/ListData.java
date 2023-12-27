package clc.resilient.backend.service.list;

import clc.resilient.backend.service.list.objects.ListItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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
            unknownItem.setId(items.size());
            unknownItem.setItems(new ArrayList<>());
            items.add(unknownItem);
        }
        for(int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            if(item.getId().equals(unknownItem.getId())) {
                logger.debug("Updated item " + item.getId());
                items.set(i, unknownItem);
                unknownItem = items.get(i);
            }
        }
        return unknownItem;
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
