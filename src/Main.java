import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args){
        int choix;
        Scanner scanner = new Scanner(System.in);
        System.out.println("-------------------------------------------------------");
        System.out.println("--------------MENU APPLICATION ETUDIANT----------------");
        System.out.println("-------------------------------------------------------");
        AppEtudiant app = new AppEtudiant();

        do {
            System.out.println("1- Afficher mes cours");
            System.out.println("2- S'inscrire dans un groupe");
            System.out.println("3- Se désinscrire d'un groupe");
            System.out.println("4- Afficher mes projets");
            System.out.println("5- Afficher les projets où je ne suis pas inscris");
            System.out.println("6- Afficher les groupes incomplets d'un projet");
            System.out.println();
            System.out.print("Entrez votre choix: ");
            choix = Integer.parseInt(scanner.nextLine());

            switch (choix) {
                case 1:
                    app.afficherMesCours();
                    break;
                case 2:
                    app.sInscrireGroupe();
                    break;
                case 3:
                    app.desinscrireGroupe();
                    break;
                case 4:
                    app.afficherMesProjets();
                    break;
                case 5:
                    app.afficherProjetPasEncoreGroupe();
                    break;
                case 6:
                    app.compositionGroupesIncomplets();
            }
        } while (choix >= 0 && choix <= 6);
    }
}
