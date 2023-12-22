package clc.resilient.backend.service.proxy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

/**
 * @author Kacper Urbaniec
 * @version 2023-12-22
 */
@RestController
public class ProxyController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/test")
    public String test() {
        logger.debug("/test");
        return "test";
    }



    // @RequestMapping(value = "{_:^(?!swagger-ui/index\\.html$|tmdb-v[34]\\.json$|test$).*}")
    // @RequestMapping(value = "{_:^(?!swagger-ui/index\\.html|dashboard).*$}**")

    // https://stackoverflow.com/a/22411379/12347616

    // @RequestMapping(value = "{_:^(?!swagger-ui|tmdb-v[34].json|4).*}/**")

    @RequestMapping({"/tmdb/3/**", "/tmdb/4/**"})
    public ResponseEntity mirrorRest(@RequestBody(required = false) String body,
                                     HttpMethod method, HttpServletRequest request, HttpServletResponse response)
        throws URISyntaxException {

        logger.debug("mirror | {} | {}", method.name(), request.getRequestURI());

        String requestUrl = request
            .getRequestURI()
            .replaceAll("^/tmdb", "");

        URI uri = new URI("https", null, "api.themoviedb.org", 443, null, null, null);
        uri = UriComponentsBuilder.fromUri(uri)
            .path(requestUrl)
            .query(request.getQueryString())
            .build(true).toUri();

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.set(headerName, request.getHeader(headerName));
        }

        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        try {
            return restTemplate.exchange(uri, method, httpEntity, String.class);
        } catch(HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                .headers(e.getResponseHeaders())
                .body(e.getResponseBodyAsString());
        }
    }

    @RequestMapping(value = "/3/search/movie")
    public ResponseEntity searchMovie(@RequestBody(required = false) String body,
                                     HttpMethod method, HttpServletRequest request, HttpServletResponse response)
        throws URISyntaxException {

        logger.debug("search Movie | {} | {}", method.name(), request.getRequestURI());

        return mirrorRest(body, method, request, response);
    }
}
