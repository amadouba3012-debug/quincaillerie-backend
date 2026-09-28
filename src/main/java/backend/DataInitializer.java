package backend;

import backend.entity.Category;
import backend.entity.Produit;
import backend.repository.CategoryRepository;
import backend.repository.ProduitRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProduitRepository produitRepository;

    public DataInitializer(CategoryRepository categoryRepository, ProduitRepository produitRepository) {
        this.categoryRepository = categoryRepository;
        this.produitRepository = produitRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Insérer des catégories seulement si la table est vide
        if (categoryRepository.count() == 0) {
            Category outillage = new Category(null, "Outillage", "Marteaux, tournevis, clés...");
            Category plomberie = new Category(null, "Plomberie", "Tuyaux, raccords, robinets...");
            Category electricite = new Category(null, "Électricité", "Câbles, prises, disjoncteurs...");

            categoryRepository.save(outillage);
            categoryRepository.save(plomberie);
            categoryRepository.save(electricite);

            // Ajouter des produits associés
            Produit marteau = new Produit();
            marteau.setNom("Marteau de charpentier");
            marteau.setCodeBarre("100001");
            marteau.setDescription("Marteau avec manche en acier");
            marteau.setPrixAchat(new BigDecimal("2500"));
            marteau.setPrixVente(new BigDecimal("4000"));
            marteau.setQuantiteStock(15);
            marteau.setSeuilAlerte(3);
            marteau.setCategorie(outillage);

            Produit tournevis = new Produit();
            tournevis.setNom("Jeu de tournevis");
            tournevis.setCodeBarre("100002");
            tournevis.setDescription("Lot de 6 tournevis isolés");
            tournevis.setPrixAchat(new BigDecimal("3500"));
            tournevis.setPrixVente(new BigDecimal("5500"));
            tournevis.setQuantiteStock(8);
            tournevis.setSeuilAlerte(2);
            tournevis.setCategorie(outillage);

            produitRepository.save(marteau);
            produitRepository.save(tournevis);

            System.out.println("=== DONNÉES INITIALES CHARGÉES AVEC SUCCÈS ===");
        }
    }
}
