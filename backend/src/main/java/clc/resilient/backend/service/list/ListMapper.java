package clc.resilient.backend.service.list;

import clc.resilient.backend.service.list.entities.MediaRelation;
import clc.resilient.backend.service.list.entities.MovieList;
import clc.resilient.backend.service.list.dtos.MediaItemDTO;
import clc.resilient.backend.service.list.dtos.MovieListDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-05
 */
@Mapper(componentModel = "spring")
public interface ListMapper {
    @Mapping(source = "backdropPath", target = "backdrop_path")
    @Mapping(source = "results", target = "items")
    MovieList movieListToEntity(MovieListDTO dto);

    @Mapping(source = "backdrop_path", target = "backdropPath")
    @Mapping(source = "items", target = "results")
    MovieListDTO movieListToDto(MovieList entity);

    List<MovieListDTO> movieListToDto(List<MovieList> entities);

    @Mapping(target = "movieList", ignore = true)
    @Mapping(target = "id", ignore = true)
    MediaRelation mediaItemToEntity(MediaItemDTO dto);

    Set<MediaRelation> mediaItemToEntity(List<MediaItemDTO> dtos);

    @Mapping(target = "success", constant = "true")
    MediaItemDTO mediaItemToDto(MediaRelation entity);

    List<MediaItemDTO> mediaItemToDto(Set<MediaRelation> entities);
}
