package clc.resilient.backend.service.list;

import clc.resilient.backend.service.data.objects.MovieList;
import clc.resilient.backend.service.data.objects.MovieRelation;
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
    MovieList movieListToEntity(MovieListDTO dto);

    @Mapping(source = "backdrop_path", target = "backdropPath")
    MovieListDTO movieListToDto(MovieList entity);

    List<MovieListDTO> movieListToDto(List<MovieList> entities);

    @Mapping(target = "movieList", ignore = true)
    @Mapping(target = "id", ignore = true)
    MovieRelation mediaItemToEntity(MediaItemDTO dto);

    Set<MovieRelation> mediaItemToEntity(List<MediaItemDTO> dtos);

    @Mapping(target = "success", constant = "true")
    MediaItemDTO mediaItemToDto(MovieRelation entity);

    List<MediaItemDTO> mediaItemToDto(Set<MovieRelation> entities);
}
