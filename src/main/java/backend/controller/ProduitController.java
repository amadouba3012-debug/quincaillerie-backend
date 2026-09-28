package backend.controller;

import backend.entity.Produit;
import backend.repository.ProduitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produits")
@CrossOrigin(origins = "*")
public class ProduitController {

    @Autowired
    private ProduitRepository produitRepository;

    @GetMapping
    public List<Produit> getAllProduits() {
        return produitRepository.findByActifTrue();
    }

    @GetMapping("/corbeille")
    public List<Produit> getProduitsArchives() {
        return produitRepository.findByActifFalse();
    }

    @PostMapping
    public Produit createProduit(@RequestBody Produit produit) {
        if (produit.getActif() == null) {
            produit.setActif(true);
        }
        return produitRepository.save(produit);
    }

    @GetMapping("/category/{categoryId}")
    public List<Produit> getProduitsByCategory(@PathVariable Long categoryId) {
        return produitRepository.findByCategorieIdAndActifTrue(categoryId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archiverProduit(@PathVariable Long id) {
        return produitRepository.findById(id).map(produit -> {
            produit.setActif(false);
            produitRepository.save(produit);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/restaurer")
    public ResponseEntity<Produit> restaurerProduit(@PathVariable Long id) {
        return produitRepository.findById(id).map(produit -> {
            produit.setActif(true);
            return ResponseEntity.ok(produitRepository.save(produit));
        }).orElse(ResponseEntity.notFound().build());
    }
}