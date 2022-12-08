import java.sql.*;
import java.util.Scanner;

public class AppEtudiant {
    static Scanner scanner = new Scanner(System.in);
    private int idEtudiant;
    private static Connection conn;
    private PreparedStatement psDemo;
    private PreparedStatement ps1;
    private PreparedStatement ps2;
    private PreparedStatement ps3;
    private PreparedStatement ps4;
    private PreparedStatement ps5;
    private PreparedStatement ps6;
    private PreparedStatement ps7;
    private PreparedStatement ps8;
    private PreparedStatement ps9;

    /*
    LISTE DES QUESTIONS
    1- afficher msg personnaliser pour chaque erreur?
        si ajouter un etudiant dans un groupe mais que le groupe est complet et que l'étudiant
         est déjà inscrit dans ce groupe quel msg afficher?

         ERREUR: la valeur d'une clé dupliquée rompt la contrainte unique « inscriptions_groupes_etudiant_projet_key »
         Détail : La clé « (etudiant, projet)=(1, 3) » existe déjà.
         Où  : instruction SQL « INSERT INTO logiciel.inscriptions_groupes(etudiant, groupe, projet)
            VALUES (_etudiant, _id_groupe, _num_projet) »
        fonction PL/pgsql logiciel.inscrire_etudiant_groupe(integer,integer,character varying),

     2-
     */





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
            String url = "jdbc:postgresql://localhost:5432/logiciel";
            //String url = "jdbc:postgresql://172.24.2.6:5432/dbchehrazadouazzani";
            conn = DriverManager.getConnection(url, "postgres", "shera");
            //    conn = DriverManager.getConnection(url, "chehrazadouazzani", "SQINPAG0B");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("Impossible de joindre le server !");
            System.exit(1);
        }
        preparedStatement();
        //requeteDemo();
        seConnecter();
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
        System.out.println("------------------SE CONNECTER-------------------------");

        boolean isConnecter = false;
        String mail, password;
        ResultSet rs;

        while (!isConnecter) {
            System.out.print("Entrez votre mail: ");
            mail = scanner.nextLine();
            System.out.print("Entrez votre mot de passe: ");
            //     String password = new String(console.readPassword("Entrez votre mot de passe: "));
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
        //TODO - se désinscrire d'un groupe (app etudiant) -> complet reste à true, nombre_inscrit ne décremente pas,
        // inscription groupe ne supprime pas le tuple && un etudiant qui n'est pas inscrit ne peut pas se désinscrire

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
        //TODO la requête est fausse, normalement M.Damas pas inscrit au cours d'APOO

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
            ps8.setString(1, valeur);
            ResultSet rs1 = ps9.executeQuery();
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
                            + rs.getString(4) + "\t\t\t\t" + rs.getString(5)
                            + "\t\t\t\t" + rs.getInt(6));
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("-------------------------------------------------------------------------------------------------");
    }

    /**
     * execute all the statements of the demo after creating a student
     */
    public void requeteDemo2() {
        try {

            psDemo = conn.prepareStatement("SELECT logiciel.inscrire_etudiant_cours('cd@student.vinci.be', 'BINV2040')");
            psDemo.executeQuery();
            psDemo = conn.prepareStatement("SELECT logiciel.inscrire_etudiant_cours('sf@student.vinci.be', 'BINV2040')");
            psDemo.executeQuery();
            psDemo = conn.prepareStatement("SELECT logiciel.inserer_projets('projSQL', 'projet SQL', '2023-09-10', '2023-12-15', 'BINV2040')");
            psDemo.executeQuery();
            psDemo = conn.prepareStatement("SELECT logiciel.inserer_projets('dsd', 'DSD', '2023-09-30', '2023-12-01', 'BINV1020')");
            psDemo.executeQuery();
            psDemo = conn.prepareStatement("SELECT logiciel.creer_groupes('projSQL', 1, 2)");
            psDemo.executeQuery();
            psDemo = conn.prepareStatement("SELECT logiciel.inscrire_etudiant_groupe(1, 1, 'projSQL')");
            psDemo.executeQuery();
            psDemo = conn.prepareStatement("SELECT logiciel.inscrire_etudiant_groupe(2, 1, 'projSQL')");
            psDemo.executeQuery();
            //-----------------
            psDemo = conn.prepareStatement("SELECT logiciel.inscrire_etudiant_cours('cd@student.vinci.be','BINV0000')");
            psDemo.executeQuery();
            psDemo = conn.prepareStatement("SELECT logiciel.inscrire_etudiant_cours('sf@student.vinci.be','BINV0000')");
            psDemo.executeQuery();
            psDemo = conn.prepareStatement("SELECT logiciel.inserer_projets('a','a','2020-01-01','2021-01-01','BINV0000')");
            psDemo.executeQuery();

//            psDemo = conn.prepareStatement("SELECT logiciel.inserer_cours('BINV2140', 'SD2', 2, 3)");
//            psDemo.executeQuery();
//            psDemo = conn.prepareStatement("SELECT logiciel.inscrire_etudiant_cours('ic@student.vinci.be', 'BINV2140')");
//            psDemo.executeQuery();
////
//        -- SELECT logiciel.inscrire_etudiant_cours('cd@student.vinci.be', 'BINV2040');
//        -- SELECT logiciel.inscrire_etudiant_cours('sf@student.vinci.be', 'BINV2040');
//        --SELECT logiciel.inserer_etudiant('Cambron', 'Isabelle', 'ic@student.vinci.be','$2a$10$POaQZNkxmAhVNJG2TWvUF.vqN3tV3L2WiS2TTE7DZgMh9OY6DvNcG');
//
//        ne doit pas fonctionner le suivant:
//        --SELECT logiciel.inscrire_etudiant_cours('ic@student.vinci.be', 'BINV2040');
            //       --------------
//        --SELECT logiciel.inscrire_etudiant_cours('sf@student.vinci.be', 'BINV2140');
//        --SELECT logiciel.inscrire_etudiant_cours('cd@student.vinci.be', 'BINV2140');
//        --
//        --select logiciel.inserer_projets('projSD', 'projet SD2', '2023-03-01', '2023-04-01', 'BINV2140');
//        -- --ne doit pas fonctionner le suivant:
//        -- --SELECT logiciel.creer_groupes('projSD', 2, 2);
//        --
            psDemo = conn.prepareStatement("SELECT logiciel.creer_groupes('a', 2, 1)");
            psDemo.executeQuery();
//                --SELECT logiciel.creer_groupes('projSD', 3, 1);
//                SELECT logiciel.creer_groupes('projSD', 3, 1);
//        --
//                -- --ne doit pas fonctionner le suivant:
//        -- --SELECT logiciel.creer_groupes('projSD', 3, 1);
//        --
//                --SELECT logiciel.creer_groupes('projSD', 1, 2);
//
//        --ne doit pas fonctionner le suivant:
//        --SELECT logiciel.creer_groupes('Javascript', 2, 2);
//
//
//        -- SELECT logiciel.inscrire_etudiant_groupe(1, 1, 1);
//        -- SELECT logiciel.valider_un_groupe(1);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
