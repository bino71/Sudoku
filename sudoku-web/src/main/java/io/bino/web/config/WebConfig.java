package io.bino.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve committed static assets (favicon.svg, etc.) from resources/public/
        // Spring Boot also auto-serves this location, but registering explicitly
        // ensures it takes priority over the SPA catch-all below.
        registry.addResourceHandler("/favicon.svg", "/favicon.ico")
                .addResourceLocations("classpath:/public/", "classpath:/static/");

        // Angular SPA lives under /app — all /app/** paths fall back to index.html
        registry.addResourceHandler("/app", "/app/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        // Strip leading "app/" prefix added by the handler pattern
                        String stripped = resourcePath.startsWith("app/")
                                ? resourcePath.substring(4) : resourcePath;
                        Resource requested = location.createRelative(stripped);
                        return (requested.exists() && requested.isReadable())
                                ? requested
                                : new ClassPathResource("/static/index.html");
                    }
                });
    }
}
