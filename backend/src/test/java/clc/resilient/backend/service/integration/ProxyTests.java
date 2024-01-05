package clc.resilient.backend.service.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProxyTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void search_movie_via_tmdb() throws Exception {
        mvc.perform(get("/tmdb/3/search/movie?query=godzilla&include_adult=false&language=en-US&page=1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void get_image_via_tmdb() throws Exception {
        mvc.perform(get("/image.tmdb/t/p/w342/exNtEY8QUuQh9e23wSQjkPxKIU3.jpg")
            .contentType(MediaType.APPLICATION_OCTET_STREAM))
            .andExpect(status().isOk())
            .andExpect(content().string(not(blankOrNullString())));
    }

}
