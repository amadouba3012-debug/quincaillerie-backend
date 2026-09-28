package backend.controller;

import backend.entity.Commande;
import backend.entity.LigneCommande;
import backend.entity.Produit;
import backend.repository.CommandeRepository;
import backend.repository.ProduitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/commandes")
@CrossOrigin(origins = "*")
public class CommandeController {

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private ProduitRepository produitRepository;

    @GetMapping
    public List<Commande> getAllCommandes() {
        return commandeRepository.findAll();
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createCommande(@RequestBody Commande commande) {
        // Déduction des stocks
        for (LigneCommande ligne : commande.getLignes()) {
            Produit p = produitRepository.findById(ligne.getProduit().getId()).orElse(null);
            if (p == null) {
                return ResponseEntity.badRequest().body("Produit introuvable ID: " + ligne.getProduit().getId());
            }
            if (p.getQuantiteStock() < ligne.getQuantite()) {
                return ResponseEntity.badRequest().body("Stock insuffisant pour : " + p.getNom());
            }
            p.setQuantiteStock(p.getQuantiteStock() - ligne.getQuantite());
            produitRepository.save(p);

            // S'assurer que chaque ligne pointe bien vers la commande parente
            ligne.setCommande(commande);
        }

        Commande nouvelleCommande = commandeRepository.save(commande);
        return ResponseEntity.ok(nouvelleCommande);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> modifierCommande(@PathVariable Long id, @RequestBody Commande commandeInfo) {
        try {
            Commande commande = commandeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Commande introuvable"));

            // 1. Mettre à jour les informations de base
            commande.setNomClient(commandeInfo.getNomClient());
            commande.setStatutPaiement(commandeInfo.getStatutPaiement());
            commande.setStatutLivraison(commandeInfo.getStatutLivraison());

            // 2. Restituer le stock des anciennes lignes
            if (commande.getLignes() != null) {
                for (LigneCommande ancienneLigne : commande.getLignes()) {
                    if (ancienneLigne.getProduit() != null) {
                        Produit p = produitRepository.findById(ancienneLigne.getProduit().getId()).orElse(null);
                        if (p != null) {
                            p.setQuantiteStock(p.getQuantiteStock() + ancienneLigne.getQuantite());
                            produitRepository.save(p);
                        }
                    }
                }
                commande.getLignes().clear();
            }

            // 3. Appliquer les nouvelles lignes et réajuster le stock
            BigDecimal total = BigDecimal.ZERO;
            BigDecimal totalBenefice = BigDecimal.ZERO;

            if (commandeInfo.getLignes() != null) {
                for (LigneCommande nouvelleLigne : commandeInfo.getLignes()) {
                    Produit p = produitRepository.findById(nouvelleLigne.getProduit().getId())
                            .orElseThrow(() -> new RuntimeException("Produit introuvable"));

                    if (p.getQuantiteStock() < nouvelleLigne.getQuantite()) {
                        throw new RuntimeException("Stock insuffisant pour : " + p.getNom());
                    }

                    p.setQuantiteStock(p.getQuantiteStock() - nouvelleLigne.getQuantite());
                    produitRepository.save(p);

                    BigDecimal quantiteBD = BigDecimal.valueOf(nouvelleLigne.getQuantite());
                    BigDecimal prixVenteBD = p.getPrixVente();
                    BigDecimal sousTotalBD = prixVenteBD.multiply(quantiteBD);

                    LigneCommande ligne = new LigneCommande();
                    ligne.setCommande(commande);
                    ligne.setProduit(p);
                    ligne.setQuantite(nouvelleLigne.getQuantite());
                    ligne.setPrixUnitaire(prixVenteBD.doubleValue());
                    ligne.setSousTotal(sousTotalBD.doubleValue());

                    total = total.add(sousTotalBD);

                    if (p.getPrixAchat() != null) {
                        BigDecimal beneficeUnitaire = prixVenteBD.subtract(p.getPrixAchat());
                        BigDecimal beneficeLigne = beneficeUnitaire.multiply(quantiteBD);
                        totalBenefice = totalBenefice.add(beneficeLigne);
                    }

                    commande.getLignes().add(ligne);
                }
            }

            commande.setMontantTotal(total.doubleValue());
            commande.setBenefice(totalBenefice.doubleValue());

            Commande commandeSauvegardee = commandeRepository.save(commande);
            return ResponseEntity.ok(commandeSauvegardee);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
