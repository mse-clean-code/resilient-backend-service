package clc.resilient.backend.service.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
