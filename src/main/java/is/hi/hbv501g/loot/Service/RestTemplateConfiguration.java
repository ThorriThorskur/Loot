package is.hi.hbv501g.loot.Service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

    private final RestTemplateBuilder builder;

    public RestTemplateConfiguration(RestTemplateBuilder builder) {
        this.builder = builder;
    }

    @Bean
    public RestTemplate restTemplate() {
        return builder
                .defaultHeader(HttpHeaders.USER_AGENT, "Loot/1.0 stt27@hi.is")
                .build();
    }
}

