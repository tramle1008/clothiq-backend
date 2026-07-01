package commerce.sbEcommerce.config;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SwaggerUiInitializerController {

    @GetMapping(
            value = {"/swagger-ui/swagger-initializer.js", "/webjars/swagger-ui/swagger-initializer.js"},
            produces = "application/javascript"
    )
    public ResponseEntity<String> swaggerInitializer(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String script = """
                window.onload = function () {
                  window.ui = SwaggerUIBundle({
                    configUrl: '%s/v3/api-docs/swagger-config',
                    dom_id: '#swagger-ui',
                    deepLinking: true,
                    presets: [
                      SwaggerUIBundle.presets.apis,
                      SwaggerUIStandalonePreset
                    ],
                    plugins: [
                      SwaggerUIBundle.plugins.DownloadUrl
                    ],
                    layout: 'StandaloneLayout'
                  });
                };
                """.formatted(contextPath);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.valueOf("application/javascript"))
                .body(script);
    }
}
