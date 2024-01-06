package clc.resilient.backend.service.list;

import clc.resilient.backend.service.data.objects.MovieList;
import clc.resilient.backend.service.list.dtos.MovieListDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-05
 */
@Mapper(componentModel = "spring")
public interface ListMapper {
    @Mapping(source = "backdropPath", target = "backdrop_path")
    @Mapping(source = "private", target = "private")
    MovieList movieListToEntity(MovieListDTO dto);

    @Mapping(source = "backdrop_path", target = "backdropPath")
    @Mapping(source = "private", target = "private")
    MovieListDTO movieListToDto(MovieList entity);
}
