package com.gomes.photographer_manager.domain.photographer;

import com.gomes.photographer_manager.domain.photographer.response.PhotographerPortfolioResponse;
import com.gomes.photographer_manager.domain.photographer.response.PublicPhotographerDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/photographers")
@Tag(name = "Fotógrafos Públicos")
public class PublicPhotographerController {

    private final PhotographerService photographerService;

    public PublicPhotographerController(PhotographerService photographerService) {
        this.photographerService = photographerService;
    }

    @GetMapping("/public")
    @Operation(summary = "Lista fotógrafos publicamente (sem autenticação)")
    public ResponseEntity<List<PublicPhotographerDTO>> listPublic() {
        return ResponseEntity.ok(photographerService.listPublic());
    }

    @GetMapping("/{photographerId}/portfolio")
    @Operation(summary = "Retorna portfólio público de um fotógrafo")
    public ResponseEntity<PhotographerPortfolioResponse> getPortfolio(@PathVariable String photographerId) {
        return ResponseEntity.ok(photographerService.getPortfolio(photographerId));
    }
}
