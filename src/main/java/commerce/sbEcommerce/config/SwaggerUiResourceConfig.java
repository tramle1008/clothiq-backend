package commerce.sbEcommerce.config;

import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springdoc.webmvc.ui.SwaggerResourceResolver;
import org.springdoc.webmvc.ui.SwaggerWebMvcConfigurer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceChainRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.resource.CachingResourceResolver;

@Configuration
public class SwaggerUiResourceConfig {

    @Bean
    public SwaggerWebMvcConfigurer swaggerWebMvcConfigurer(
            SwaggerUiConfigProperties swaggerUiConfigProperties,
            WebProperties webProperties,
            WebMvcProperties webMvcProperties,
            SwaggerIndexTransformer swaggerIndexTransformer,
            SwaggerResourceResolver swaggerResourceResolver,
            SwaggerWelcomeCommon swaggerWelcomeCommon
    ) {
        return new PatchedSwaggerWebMvcConfigurer(
                swaggerUiConfigProperties,
                webProperties,
                webMvcProperties,
                swaggerIndexTransformer,
                swaggerResourceResolver,
                swaggerWelcomeCommon
        );
    }

    static final class PatchedSwaggerWebMvcConfigurer extends SwaggerWebMvcConfigurer {
        private static final String SWAGGER_UI_PREFIX = "/swagger-ui";
        private static final String WEBJARS_RESOURCE_LOCATION = "classpath:/META-INF/resources/webjars/";

        private final SwaggerUiConfigProperties swaggerUiConfigProperties;
        private final WebProperties webProperties;
        private final WebMvcProperties webMvcProperties;
        private final SwaggerIndexTransformer swaggerIndexTransformer;
        private final SwaggerResourceResolver swaggerResourceResolver;

        PatchedSwaggerWebMvcConfigurer(
                SwaggerUiConfigProperties swaggerUiConfigProperties,
                WebProperties webProperties,
                WebMvcProperties webMvcProperties,
                SwaggerIndexTransformer swaggerIndexTransformer,
                SwaggerResourceResolver swaggerResourceResolver,
                SwaggerWelcomeCommon swaggerWelcomeCommon
        ) {
            super(
                    swaggerUiConfigProperties,
                    webProperties,
                    webMvcProperties,
                    swaggerIndexTransformer,
                    swaggerResourceResolver,
                    swaggerWelcomeCommon
            );
            this.swaggerUiConfigProperties = swaggerUiConfigProperties;
            this.webProperties = webProperties;
            this.webMvcProperties = webMvcProperties;
            this.swaggerIndexTransformer = swaggerIndexTransformer;
            this.swaggerResourceResolver = swaggerResourceResolver;
        }

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            String uiRootPath = super.getUiRootPath();
            String swaggerUiLocation = WEBJARS_RESOURCE_LOCATION + "swagger-ui/" + swaggerUiConfigProperties.getVersion() + "/";

            register(registry, false, swaggerUiLocation, uiRootPath + SWAGGER_UI_PREFIX + "/swagger-initializer.js");
            register(registry, true, swaggerUiLocation, uiRootPath + SWAGGER_UI_PREFIX + "/**");

            if (webProperties.getResources().isAddMappings()) {
                String webjarsBasePath = normalizeWebjarsBasePath(webMvcProperties.getWebjarsPathPattern());
                register(registry, false, WEBJARS_RESOURCE_LOCATION,
                        webjarsBasePath + "/swagger-ui/swagger-initializer.js",
                        webjarsBasePath + "/swagger-ui/" + swaggerUiConfigProperties.getVersion() + "/swagger-initializer.js");
                register(registry, true, WEBJARS_RESOURCE_LOCATION, webjarsBasePath + "/swagger-ui/**");
            }
        }

        private String normalizeWebjarsBasePath(String webjarsPattern) {
            if (webjarsPattern == null || webjarsPattern.isBlank()) {
                return "/webjars";
            }
            if (webjarsPattern.endsWith("/**")) {
                return webjarsPattern.substring(0, webjarsPattern.length() - 3);
            }
            return webjarsPattern.endsWith("/") ? webjarsPattern.substring(0, webjarsPattern.length() - 1) : webjarsPattern;
        }

        private void register(ResourceHandlerRegistry registry, boolean cacheResources, String location, String... patterns) {
            ResourceHandlerRegistration registration = registry.addResourceHandler(patterns);
            registration.addResourceLocations(location);

            ResourceChainRegistration chain;
            if (cacheResources) {
                chain = registration.resourceChain(true, super.getCache());
            } else {
                registration.setUseLastModified(false);
                registration.setCacheControl(CacheControl.noStore());
                chain = registration.resourceChain(false);
                chain.addResolver(new CachingResourceResolver(super.getCache()));
            }

            chain.addResolver(swaggerResourceResolver);
            chain.addTransformer(swaggerIndexTransformer);
        }
    }
}
