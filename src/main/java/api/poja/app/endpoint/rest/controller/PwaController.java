package api.poja.app.endpoint.rest.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
public class PwaController {

  private static final MediaType MANIFEST = MediaType.parseMediaType("application/manifest+json");

  @GetMapping("/manifest.webmanifest")
  public ResponseEntity<Resource> manifest() {
    return ResponseEntity.ok()
        .contentType(MANIFEST)
        .cacheControl(CacheControl.noCache())
        .body(new ClassPathResource("static/manifest.webmanifest"));
  }

  @GetMapping("/sw.js")
  public ResponseEntity<Resource> serviceWorker() {
    return ResponseEntity.ok()
        .contentType(MediaType.valueOf("text/javascript"))
        .cacheControl(CacheControl.noCache())
        .body(new ClassPathResource("static/sw.js"));
  }
}
