import java.sql.*;
import java.util.Scanner;

public class AppEtudiant {
    static Scanner scanner = new Scanner(System.in);
    private int idEtudiant;
    private static Connection conn;
    private PreparedStatement ps1;
    private PreparedStatement ps2;
    private PreparedStatement ps3;
    private PreparedStatement ps4;
    private PreparedStatement ps5;
    private PreparedStatement ps6;
    private PreparedStatement ps7;
    private PreparedStatement ps8;
    private PreparedStatement ps9;

    /**
     * connect to postgresql and prepare statements and connect the student
     */
    public AppEtudiant() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver PostgreSQL manquant !");
            System.exit(1);
        }

        try {
          //  String url = "jdbc:postgresql://localhost:5432/logiciel";
            String url = "jdbc:postgresql://172.24.2.6:5432/dbchehrazadouazzani";
          //  conn = DriverManager.getConnection(url, "postgres", "shera");
              conn = DriverManager.getConnection(url, "mariammiclauri", "123");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("Impossible de joindre le server !");
            System.exit(1);
        }
        preparedStatement();
        seConnecter();
    }

    public void menu() {
        int choix;

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
        } while (choix >= 0 && choix <= 6);
    }

    /**
     * prepare all the statements
     */
    public void preparedStatement() {
        try {
            ps1 = conn.prepareStatement("SELECT logiciel.chercher_id_etudiant(?)");
            ps2 = conn.prepareStatement("SELECT logiciel.recuperer_mdp_etudiant(?)");
            ps3 = conn.prepareStatement("SELECT * FROM logiciel.afficher_mes_cours");
            ps4 = conn.prepareStatement("SELECT logiciel.inscrire_etudiant_groupe(?,?,?)");
            ps5 = conn.prepareStatement("SELECT logiciel.retirer_etudiant(?,?)");
            ps6 = conn.prepareStatement("SELECT * FROM logiciel.afficher_lesProjets_d_etudiant");
            ps7 = conn.prepareStatement("SELECT * FROM logiciel.afficher_projets_pas_encore_de_groupe");
            ps8 = conn.prepareStatement("SELECT * FROM logiciel.afficher_composition_groupes_incomplets");
            ps9 = conn.prepareStatement("SELECT logiciel.chercher_id_projet(?)");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * connect the student
     * ps1 = SELECT logiciel.chercher_id_etudiant(?)
     * ps2 = SELECT logiciel.recuperer_mdp_etudiant(?)
     */
    private void seConnecter() {
        System.out.println("-------------------------------------------------------");
        System.out.println("--------------MENU APPLICATION ETUDIANT----------------");
        System.out.println("-------------------------------------------------------");
        System.out.println("------------------SE CONNECTER-------------------------");
        System.out.println("-------------------------------------------------------");

        boolean isConnecter = false;
        String mail, password;
        ResultSet rs;

        while (!isConnecter) {
            System.out.print("Entrez votre mail: ");
            mail = scanner.nextLine();
            System.out.print("Entrez votre mot de passe: ");
            password = scanner.nextLine();

            try {
                ps1.setString(1, mail);
                rs = ps1.executeQuery();
                rs.next();
                idEtudiant = rs.getInt(1);

                ps2.setInt(1, idEtudiant);
                rs = ps2.executeQuery();
                if (rs.next()) {
                    if (BCrypt.checkpw(password, rs.getString(1))) {
                        System.out.println("--------> Connecté !  <---------");
                        isConnecter = true;
                    } else {
                        System.out.println("---------> mot de passe erroné <---------");
                    }
                } else {
                    System.out.println("Non trouvé");
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * display the courses of the student
     * ps3 = SELECT * FROM logiciel.afficher_mes_cours
     */
    public void afficherMesCours() {
        System.out.println("--------------------Mes cours---------------------------");
        try {
            ResultSet rs = ps3.executeQuery();
            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            for (int i = 2; i <= resultSetMetaData.getColumnCount(); i++) {
                System.out.print(resultSetMetaData.getColumnName(i) + "          ");
            }
            System.out.println();
            System.out.println("----------------------------------------------------------");
            while (rs.next()) {
                if (rs.getInt(1) == idEtudiant) {
                    System.out.println(rs.getString(2) + "          \t" + rs.getString(3)
                            + "             \t" + rs.getString(4));
                }
            }
            System.out.println("----------------------------------------------------------");

        } catch (SQLException se) {
            System.out.println(se.getMessage());
        }
    }

    /**
     * subscribe the student to a group
     * ps4 = SELECT logiciel.inscrire_etudiant_groupe(?,?,?)
     */
    public void sInscrireGroupe() {
        System.out.println("----------------S'inscrire dans un groupe----------------------");
        try {
            ps4.setInt(1, idEtudiant);
            System.out.print("Entrez l'identifiant du projet: ");
            String valeur = scanner.nextLine();
            ps4.setString(3, valeur);
            System.out.print("Entrez le numéro de groupe du projet: ");
            int numGroupe = Integer.parseInt(scanner.nextLine());
            ps4.setInt(2, numGroupe);

            ResultSet rs = ps4.executeQuery();
            rs.next();
            if (rs.getBoolean(1)) {
                System.out.println("--------> INSCRIT !  <---------");
            } else {
                System.out.println("---------> Problème lors de l'inscription <---------");
            }
            System.out.println();
        } catch (SQLException se) {
            System.out.println(se.getMessage());
        }
    }

    /**
     * unsubscribe the student to a group
     * ps5 = logiciel.retirer_etudiant(?,?)
     */
    public void desinscrireGroupe() {
        System.out.println("----------------Se désinscrire d'un groupe----------------------");
        try {
            ps5.setInt(1, idEtudiant);
            System.out.print("Entrez l'identifiant du projet: ");
            String valeur = scanner.nextLine();
            ps5.setString(2, valeur);

            ResultSet rs = ps5.executeQuery();
            rs.next();
            if (rs.getBoolean(1)) {
                System.out.println("--------> DESINSCRIT !  <---------");
            } else {
                System.out.println("---------> Problème lors de la désinscription <---------");
            }
            System.out.println();
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            System.out.println("---------> Problème lors de la désinscription <---------");
        }
    }

    /**
     * display all the projects of the student connected
     * ps6 = SELECT * FROM logiciel.afficher_lesProjets_d_etudiant
     */
    public void afficherMesProjets() {
        System.out.println("----------------------------Visualiser mes projets--------------------------------------");
        try {
            ResultSet rs = ps6.executeQuery();
            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            for (int i = 2; i <= resultSetMetaData.getColumnCount(); i++) {
                System.out.print(resultSetMetaData.getColumnName(i) + "          \t");
            }
            System.out.println();
            System.out.println("--------------------------------------------------------------------------------------");
            while (rs.next()) {
                if (rs.getInt(1) == idEtudiant) {
                    System.out.println(rs.getString(2) + "\t" + rs.getString(3)
                            + "             \t" + rs.getString(4) + "             \t" + rs.getInt(5));
                }
            }
            System.out.println("-------------------------------------------------------------------------------------");

        } catch (SQLException se) {
            System.out.println(se.getMessage());
        }
    }

    /**
     * display the projects of the student where he is not subscribed in any of the groups
     * ps7 = SELECT * FROM logiciel.afficher_projets_pas_encore_de_groupe
     */
    public void afficherProjetPasEncoreGroupe() {
        System.out.println("----------------------------Mes projets où je n'ai pas encore de groupe--------------------------------------");
        try {
            ResultSet rs = ps7.executeQuery();
            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            for (int i = 2; i <= resultSetMetaData.getColumnCount(); i++) {
                System.out.print(resultSetMetaData.getColumnName(i) + "\t\t\t\t");
            }
            System.out.println();
            System.out.println("-----------------------------------------------------------------------------------------------");
            while (rs.next()) {
                if (rs.getInt(1) == idEtudiant) {
                    System.out.println(rs.getString(2) + "\t\t\t\t" + rs.getString(3)
                            + "\t\t\t\t" + rs.getString(4) + "\t\t\t\t" + rs.getDate(5)
                            + "\t\t\t\t" + rs.getDate(6));
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("-------------------------------------------------------------------------------------------------");
    }

    /**
     * display all the groups not full yet of one specific project
     * ps8 = SELECT * FROM logiciel.afficher_composition_groupes_incomplets
     * ps9 = SELECT logiciel.chercher_id_projet(?)
     */
    public void compositionGroupesIncomplets() {
        System.out.print("Entrez identifiant du projet: ");
        String valeur = scanner.nextLine();
        try {
            ps9.setString(1, valeur);
            ResultSet rs1 = ps9.executeQuery();
            rs1.next();
            int idProjet = rs1.getInt(1);
            System.out.println();
            System.out.println("----------------------------Les projets où je n'ai pas encore de groupe--------------------------------------");

            ResultSet rs = ps8.executeQuery();
            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            for (int i = 2; i <= resultSetMetaData.getColumnCount(); i++) {
                System.out.print(resultSetMetaData.getColumnName(i) + "\t\t\t");
            }
            System.out.println();
            System.out.println("-----------------------------------------------------------------------------------------------");
            while (rs.next()) {
                if (rs.getInt(1) == idProjet) {
                    System.out.println("\t\t" + rs.getInt(2) + "\t\t\t\t" + rs.getString(3) + "\t\t\t\t\t"
                            + rs.getString(4) + "\t\t\t\t" + rs.getString(5));
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("-------------------------------------------------------------------------------------------------");
    }
}
