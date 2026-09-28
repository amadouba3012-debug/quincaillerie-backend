package backend.controller;

import backend.entity.Produit;
import backend.entity.Vente;
import backend.repository.ProduitRepository;
import backend.repository.VenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ventes")
@CrossOrigin(origins = "*")
public class VenteController {

    @Autowired
    private VenteRepository venteRepository;

    @Autowired
    private ProduitRepository produitRepository;

    @GetMapping
    public List<Vente> getAllVentes() {
        return venteRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> enregistrerVente(@RequestBody Vente vente) {
        if (vente.getProduit() == null || vente.getProduit().getId() == null) {
            return ResponseEntity.badRequest().body("Produit non spécifié");
        }

        Produit produit = produitRepository.findById(vente.getProduit().getId()).orElse(null);
        if (produit == null) {
            return ResponseEntity.badRequest().body("Produit introuvable");
        }

        // Vérification du stock
        if (produit.getQuantiteStock() < vente.getQuantite()) {
            return ResponseEntity.badRequest().body("Stock insuffisant ! Disponible : " + produit.getQuantiteStock());
        }

        // Décrémenter le stock
        produit.setQuantiteStock(produit.getQuantiteStock() - vente.getQuantite());
        produitRepository.save(produit);

        // Finaliser la vente
        vente.setDateVente(LocalDateTime.now());
        Vente nouvelleVente = venteRepository.save(vente);

        return ResponseEntity.ok(nouvelleVente);
    }
}
