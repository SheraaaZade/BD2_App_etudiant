import java.sql.*;
import java.util.Scanner;

public class appEtudiant {
    static Scanner scanner = new Scanner(System.in);
    //  private static String url = "jdbc:postgresql://localhost:5432/logiciel";
    private static String url = "jdbc:postgresql://172.24.2.6:5432/dbchehrazadouazzani";
    private static int idEtudiant;
    private static Connection conn;

    public appEtudiant() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver PostgreSQL manquant !");
            System.exit(1);
        }

        try {
            conn = DriverManager.getConnection(url, "chehrazadouazzani", "SQINPAG0B");
            // conn = DriverManager.getConnection(url, "postgres", "shera");
        } catch (SQLException e) {
            System.out.println("Impossible de joindre le server !");
            System.exit(1);
        }
        try {
            preparedStatement();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private static PreparedStatement ps1;
    private static PreparedStatement ps2;
    private static PreparedStatement ps3;
    private static PreparedStatement ps4;
    private static PreparedStatement ps5;
    private static PreparedStatement ps6;
    private static PreparedStatement ps7;

    public void preparedStatement() throws SQLException {
        ps1 = conn.prepareStatement("SELECT logiciel.chercher_id_etudiant(?)");
        ps2 = conn.prepareStatement("SELECT logiciel.recuperer_mdp_etudiant(?)");
        ps3 = conn.prepareStatement("SELECT * FROM logiciel.afficher_mes_cours");
        ps4 = conn.prepareStatement("SELECT logiciel.inscrire_etudiant_groupe(?,?,?)");
        ps5 = conn.prepareStatement("SELECT logiciel.retirer_etudiant(?,?)");
        ps6 = conn.prepareStatement("SELECT * FROM logiciel.afficher_projets_pas_encore_de_groupe");
        ps7 = conn.prepareStatement("SELECT * FROM logiciel.afficher_composition_groupes_incomplets");
    }

    public static void main(String[] args) throws SQLException {
        appEtudiant app = new appEtudiant();
        int choix;
        System.out.println("-------------------------------------------------------");
        System.out.println("--------------MENU APPLICATION ETUDIANT----------------");
        System.out.println("-------------------------------------------------------");

        System.out.print("Entrez votre mail: ");
        String mail = scanner.nextLine();
        System.out.print("Entrez votre mot de passe: ");
        //     String password = new String(console.readPassword("Entrez votre mot de passe: "));

        String password = scanner.nextLine();
//        String gensel = BCrypt.gensalt();
//        String cryptePassword = BCrypt.hashpw(password, gensel);

        ps1.setString(1, mail);
        ResultSet rs = ps1.executeQuery();
        rs.next();
        idEtudiant = rs.getInt(1);

        ps2.setInt(1, idEtudiant);
        rs = ps2.executeQuery();
        if (rs.next()) {
            if (BCrypt.checkpw(password, rs.getString(1))) {
                System.out.println("--------> Connecté !  <---------");
            } else {
                System.out.println("---------> mot de passe erroné <---------");
                System.exit(0);
            }

        } else {
            System.out.println("Non trouvé");
            System.exit(0);
        }

        do {
            System.out.println("1- Afficher mes cours");
            System.out.println("2- S'inscrire dans un groupe");
            System.out.println("3- Se désinscrire d'un groupe");
            System.out.println("4- Afficher mes projets");
            System.out.println("5- Afficher les projets où je ne suis pas inscris");
            System.out.println("6- Afficher les groupes incomplets d'un projet");
            System.out.println();
            System.out.print("Entrez votre choix: ");
            choix = Integer.parseInt( scanner.nextLine());

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

    private static void afficherMesCours() {
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
            System.exit(1);
        }
    }

    private static void sInscrireGroupe() {
        System.out.println("----------------S'inscrire dans un groupe----------------------");
        try {
            ps4.setInt(1, idEtudiant);
            System.out.print("Entrez l'identifiant du projet: ");
            String valeur = scanner.nextLine();
            valeur = scanner.nextLine();
            ps4.setString(3, valeur);
            System.out.print("Entrez le numéro de groupe du projet: ");
            int numGroupe = scanner.nextInt();
            ps4.setInt(2, numGroupe);


            ResultSet rs = ps4.executeQuery();
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
        try {
            ps5.setInt(1, idEtudiant);
            System.out.print("Entrez l'identifiant du projet: ");
            String valeur = scanner.nextLine();
            valeur = scanner.nextLine();
            ps5.setString(2, valeur);

            ResultSet rs = ps5.executeQuery();
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
                if (rs.getInt(1) == idEtudiant) {
                    System.out.println(rs.getString(2) + "\t" + rs.getString(3)
                            + "             \t" + rs.getString(4) + "             \t" + rs.getInt(5));
                }
            }
            System.out.println("-------------------------------------------------------------------------------------");

        } catch (SQLException se) {
            System.out.println(se.getMessage());
            System.exit(1);
        }
    }

    private static void afficherProjetPasEncoreGroupe() throws SQLException {
        System.out.println("----------------------------Les projets où je n'ai pas encore de groupe--------------------------------------");
//TODO la requête est fausse, normalement M.Damas pas inscrit au cours d'APOO
        ResultSet rs = ps6.executeQuery();
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
        System.out.println("-------------------------------------------------------------------------------------------------");

    }

    private static void compositionGroupesIncomplets() throws SQLException {
        System.out.print("Entrez identifiant du projet: ");
        String valeur = scanner.nextLine();
        System.out.println();
        System.out.println("----------------------------Les projets où je n'ai pas encore de groupe--------------------------------------");

        ResultSet rs = ps7.executeQuery();
        ResultSetMetaData resultSetMetaData = rs.getMetaData();
        for (int i = 2; i <= resultSetMetaData.getColumnCount(); i++) {
            System.out.print(resultSetMetaData.getColumnName(i) + "\t\t\t");
        }
        System.out.println();
        System.out.println("-----------------------------------------------------------------------------------------------");
        while (rs.next()) {
            if (rs.getInt(1) == idEtudiant) {
                System.out.println("\t\t"+rs.getInt(2)+"\t\t\t\t"+rs.getString(3) + "\t\t\t\t\t"
                        + rs.getString(4) + "\t\t\t\t" + rs.getString(5)
                        + "\t\t\t\t" + rs.getInt(6));
            }
        }
        System.out.println("-------------------------------------------------------------------------------------------------");

    }
}
