package clc.resilient.backend.service.list.dtos;

import lombok.Data;

import java.util.List;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-06
 */
@Data
public class MediaItemsDTO {
    private List<MediaItemDTO> items;
}
