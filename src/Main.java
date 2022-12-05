import java.sql.*;
import java.util.Scanner;

public class Main {
    static Scanner scanner = new Scanner(System.in);
    private static String url = "jdbc:postgresql://localhost:5432/logiciel";
    private static int idEtudiant;

    public static void main(String[] args) {

        int choix;
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver PostgreSQL manquant !");
            System.exit(1);
        }

        System.out.println("-------------------------------------------------------");
        System.out.println("--------------MENU APPLICATION ETUDIANT----------------");
        System.out.println("-------------------------------------------------------");

        //se connecter TODO
//        System.out.print("Entrez votre mail: ");
//        String mail = scanner.nextLine();
//        System.out.print("Entrez votre mot de passe: ");
//        String password = scanner.nextLine();
//        Connection conn = connexionDatabase();
//        try {
//            PreparedStatement ps = conn.prepareStatement("SELECT logiciel.chercher_id_etudiant(?)");
//            //   PreparedStatement ps = conn.prepareStatement("SELECT logiciel.chercher_id_etudiant(?)");
//            ps.setString(1, mail);
//            ResultSet rs = ps.executeQuery();
//            rs.next();
//            idEtudiant = rs.getInt(1);
//            System.out.println(idEtudiant);
//
//            //verifier mot de passe
//            ps = conn.prepareStatement("SELECT logiciel.verifier_mdp_etudiant(?)");
//            //   PreparedStatement ps = conn.prepareStatement("SELECT logiciel.chercher_id_etudiant(?)");
//            ps.setInt(1, idEtudiant);
//            rs = ps.executeQuery();
//            rs.next();
//            if (rs.getBoolean(1))
//                System.out.println("--------> Connecté !  <---------");
//            else
//                System.out.println("---------> mot de passe erroné <---------");
//        } catch (SQLException se) {
//            System.out.println(se.getMessage());
//            System.exit(1);
//        }
        do {
            System.out.println("1- Afficher mes cours");
            System.out.println("2- S'inscrire dans un groupe");
            System.out.println("3- Se désinscrire d'un groupe");
            System.out.println("4- Afficher mes projets");
            System.out.println("5- Afficher les projets où je ne suis pas inscris");
            System.out.println("6- Afficher les groupes incomplets d'un projet");
            System.out.println();
            System.out.print("Entrez votre choix: ");
            choix = scanner.nextInt();

            switch (choix) {
                case 1:
                    afficherMesCours();
                    break;
                case 2:
                    sInscrireGroupe();
                    break;
                case 3:
                    desinscrireGroupe();
                    break;
                case 4:
                    afficherMesProjets();
                    break;
                case 5:
                    afficherProjetPasEncoreGroupe();
                    break;
                case 6:
                    compositionGroupesIncomplets();
            }
        } while (choix >= 1 && choix <= 6);
    }

    private static Connection connexionDatabase() {
        //  String url = "jdbc:postgresql://localhost:5432/logiciel";
        // String url="jdbc:postgresql://172.24.2.6:5432/dbchehrazadouazzani“  <-- A MODIFIER
        Connection conn = null;

        try {
            //conn=DriverManager.getConnection(url,”dbchehrazadouazzani”,”SQINPAG0B”);
            conn = DriverManager.getConnection(url, "postgres", "shera");
        } catch (SQLException e) {
            System.out.println("Impossible de joindre le server !");
            System.exit(1);
        }
        return conn;
    }

    private static void afficherMesCours() {
        System.out.println("--------------------Mes cours---------------------------");
        Connection conn = connexionDatabase();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM logiciel.afficher_mes_cours");
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            for (int i = 2; i <= resultSetMetaData.getColumnCount(); i++) {
                System.out.print(resultSetMetaData.getColumnName(i) + "          ");
            }
            System.out.println();
            System.out.println("----------------------------------------------------------");
            while (rs.next()) {
                //TODO
              //  if(rs.getInt(1) == idEtudiant) {
                    System.out.println(rs.getString(2) + "          \t" + rs.getString(3)
                            + "             \t" + rs.getString(4));
            //    }
            }
            System.out.println("----------------------------------------------------------");

        } catch (SQLException se) {
            System.out.println(se.getMessage());
            System.exit(1);
        }
    }
    private static void sInscrireGroupe() {
    //
        System.out.println("----------------S'inscrire dans un groupe----------------------");
        Connection conn = connexionDatabase();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT logiciel.inscrire_etudiant_groupe(?,?,?)");
            int num_groupe, num_projet;
            System.out.print("Entrez le numéro du projet: ");
            num_projet = scanner.nextInt();
            ps.setInt(3, num_projet);
            System.out.print("Entrez le numéro du groupe: ");
            num_groupe = scanner.nextInt();
            ps.setInt(2, num_groupe);

            // A CHANGER TODO METTRE L ID DE L ETUDIANT!!!!!!!!!!!!!!!!!!!!!
            //TODO  logiciel.taille_groupe() à faire la requête, cmt trouver le cours d'un étudiant ?
            ps.setInt(1, 3);
            //TODO -------------------------------------------
            ResultSet rs = ps.executeQuery();
            rs.next();
            if (rs.getBoolean(1))
                System.out.println("--------> INSCRIT !  <---------");
            else
                System.out.println("---------> Problème lors de l'inscription <---------");
            System.out.println();
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            System.exit(1);
        }
    }
    private static void desinscrireGroupe() {
        System.out.println("----------------Se désinscrire d'un groupe----------------------");
        Connection conn = connexionDatabase();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT logiciel.retirer_etudiant(?,?,?)");
            int num_groupe, num_projet;
            System.out.print("Entrez le numéro du projet: ");
            num_projet = scanner.nextInt();
            ps.setInt(3, num_projet);
            System.out.print("Entrez le numéro du groupe: ");
            num_groupe = scanner.nextInt();
            ps.setInt(2, num_groupe);

            // A CHANGER TODO METTRE L ID DE L ETUDIANT!!!!!!!!!!!!!!!!!!!!!
            //TODO
            ps.setInt(1, 1);
            //TODO -------------------------------------------
            ResultSet rs = ps.executeQuery();
            rs.next();
            if (rs.getBoolean(1))
                System.out.println("--------> DESINSCRIT !  <---------");
            else
                System.out.println("---------> Problème lors de la désinscription <---------");
            System.out.println();
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            System.exit(1);
        }
    }
    private static void afficherMesProjets() {
        System.out.println("----------------------------Visualiser mes projets--------------------------------------");
        Connection conn = connexionDatabase();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM logiciel.afficher_lesProjets_d_etudiant");
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            for (int i = 2; i <= resultSetMetaData.getColumnCount(); i++) {
                System.out.print(resultSetMetaData.getColumnName(i) + "          \t");
            }
            System.out.println();
            System.out.println("--------------------------------------------------------------------------------------");
            while (rs.next()) {
                if(rs.getInt(1) == 1) {
                    System.out.println(rs.getString(2) + "\t" + rs.getString(3)
                            + "             \t" + rs.getString(4)+ "             \t" + rs.getInt(5));
                }
            }
            System.out.println("-------------------------------------------------------------------------------------");

        } catch (SQLException se) {
            System.out.println(se.getMessage());
            System.exit(1);
        }
    }
    private static void afficherProjetPasEncoreGroupe() {
        System.out.println("----------------------------Les projets où je n'ai pas encore de groupe--------------------------------------");
        Connection conn = connexionDatabase();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM logiciel.afficher_projets_pas_encore_de_groupe");
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            for (int i = 2; i <= resultSetMetaData.getColumnCount(); i++) {
                System.out.print(resultSetMetaData.getColumnName(i) + "          \t");
            }
            System.out.println();
            System.out.println("-----------------------------------------------------------------------------------------------");
            while (rs.next()) {
                if(rs.getInt(1) == 1) {
                    System.out.println(rs.getString(2) + "\t" + rs.getString(3)
                            + "             \t" + rs.getInt(4)+ "             \t" + rs.getDate(5)
                            + "             \t" + rs.getDate(6));
                }
            }
            System.out.println("-------------------------------------------------------------------------------------------------");

        } catch (SQLException se) {
            System.out.println(se.getMessage());
            System.exit(1);
        }
    }
    private static void compositionGroupesIncomplets() {
        System.out.println("Entrez identifiant du projet: ");
        int idProjet = scanner.nextInt();
        System.out.println();
        System.out.println("----------------------------Les projets où je n'ai pas encore de groupe--------------------------------------");
        Connection conn = connexionDatabase();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM logiciel.afficher_composition_groupes_incomplets");
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            for (int i = 2; i <= resultSetMetaData.getColumnCount(); i++) {
                System.out.print(resultSetMetaData.getColumnName(i) + "          \t");
            }
            System.out.println();
            System.out.println("-----------------------------------------------------------------------------------------------");
            while (rs.next()) {
                //TODO ID projet
                if(rs.getInt(1) == idProjet) {
                    System.out.println(rs.getInt(2) + "\t\t\t\t" + rs.getString(3)
                            + "\t\t\t\t" + rs.getString(4)+ "\t\t\t\t" + rs.getInt(5));
                }
            }
            System.out.println("-------------------------------------------------------------------------------------------------");

        } catch (SQLException se) {
            System.out.println(se.getMessage());
            System.exit(1);
        }
    }
}
