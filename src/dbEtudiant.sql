-------------------------------------------------------------------
----Application etudiant
-------------------------------------------------------------------

CREATE OR REPLACE FUNCTION logiciel.chercher_id_etudiant(_mail VARCHAR)
    RETURNS INTEGER AS
$$
DECLARE
    _id_etudiant INTEGER;
BEGIN
    SELECT e.id_etudiant
    FROM logiciel.etudiants e
    WHERE e.mail = _mail
    INTO _id_etudiant;

    IF (_id_etudiant IS NULL) then
        RAISE 'mail inexistant dans la DB ';
    end if;

    RETURN _id_etudiant;
end;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION logiciel.recuperer_mdp_etudiant(_id_etudiant INTEGER)
    RETURNS VARCHAR AS
$$
DECLARE
    mdp_etudiant VARCHAR;
BEGIN
    SELECT e.pass_word
    FROM logiciel.etudiants e
    WHERE e.id_etudiant = _id_etudiant
    INTO mdp_etudiant;

    RETURN mdp_etudiant;
end;
$$ LANGUAGE plpgsql;


--1
CREATE OR REPLACE VIEW logiciel.afficher_mes_cours AS
SELECT ic.etudiant                                                              as "Etudiant",
       c.code_cours                                                             as "Code cours",
       c.nom                                                                    as "Nom",
       COALESCE(string_agg(p.identifiant_projet, ', '), 'pas encore de projet') as les_projets
FROM logiciel.cours c
         LEFT OUTER JOIN logiciel.projets p on c.id_cours = p.cours
         LEFT OUTER JOIN logiciel.inscriptions_cours ic on c.id_cours = ic.cours
group by c.nom, c.code_cours, ic.etudiant;

--2
CREATE OR REPLACE FUNCTION logiciel.inscrire_etudiant_groupe(_etudiant INTEGER, _num_groupe INTEGER, _identifiant_projet VARCHAR)
    RETURNS BOOLEAN AS
$$
DECLARE
    _num_projet INTEGER;
    _id_groupe  INTEGER;
BEGIN
    SELECT logiciel.chercher_id_projet(_identifiant_projet) INTO _num_projet;
    SELECT logiciel.groupe_existe(_identifiant_projet, _num_groupe) INTO _id_groupe;

    INSERT INTO logiciel.inscriptions_groupes(etudiant, groupe, projet)
    VALUES (_etudiant, _id_groupe, _num_projet);
    RETURN TRUE;
end;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION logiciel.deja_inscrits()
    RETURNS TRIGGER AS
$$
BEGIN
    IF EXISTS(SELECT ig.id_inscription_groupe
              FROM logiciel.inscriptions_groupes ig
              WHERE ig.projet = OLD.projet
                AND ig.etudiant = OLD.etudiant) THEN
        RAISE 'Vous êtes déjà inscrit dans un groupe pour ce projet !';
    end if;
    RETURN NEW;
end;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_etudiant_deja_inscrit_groupe
    BEFORE INSERT
    on logiciel.inscriptions_groupes
    FOR EACH ROW
EXECUTE PROCEDURE logiciel.deja_inscrits();

CREATE OR REPLACE FUNCTION logiciel.incrementer_nb_inscrits()
    RETURNS TRIGGER AS
$$
BEGIN
    UPDATE logiciel.groupes g
    SET nombre_inscrits = g.nombre_inscrits + 1
    WHERE g.id_groupe = NEW.groupe
      AND g.projet = NEW.projet;
    RETURN NEW;
end;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_incrementer_nb_etudiant_dans_groupe
    AFTER INSERT
    on logiciel.inscriptions_groupes
    FOR EACH ROW
EXECUTE PROCEDURE logiciel.incrementer_nb_inscrits();

CREATE OR REPLACE FUNCTION logiciel.check_groupe_complet()
    RETURNS TRIGGER AS
$$
DECLARE
    taille_gr  INTEGER;
    nb_inscrit INTEGER;
BEGIN
    SELECT g.taille_groupe
    FROM logiciel.groupes g
    WHERE g.id_groupe = NEW.groupe
    INTO taille_gr;

    SELECT g.nombre_inscrits
    FROM logiciel.groupes g
    WHERE g.id_groupe = NEW.groupe
    INTO nb_inscrit;

    IF (taille_gr = nb_inscrit) THEN
        UPDATE logiciel.groupes g
        SET complet = TRUE
        WHERE g.num_groupe = NEW.groupe;
    end if;
    RETURN NEW;
end ;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_groupe_complet
    AFTER INSERT
    on logiciel.inscriptions_groupes
    FOR EACH ROW
EXECUTE PROCEDURE logiciel.check_groupe_complet();


CREATE OR REPLACE FUNCTION logiciel.taille_groupe()
    RETURNS TRIGGER AS
$$
DECLARE
    nb_inscrits   INTEGER;
    taille_groupe INTEGER;
BEGIN
    SELECT g.nombre_inscrits
    FROM logiciel.groupes g
    WHERE g.id_groupe = NEW.groupe
    INTO nb_inscrits;

    SELECT g.taille_groupe
    FROM logiciel.groupes g
    WHERE g.id_groupe = NEW.groupe
    INTO taille_groupe;

    IF (nb_inscrits = taille_groupe) THEN
        RAISE 'Groupe déjà complet';
    end if;
    RETURN NEW;
end;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_place_disponible_groupe
    BEFORE INSERT
    on logiciel.inscriptions_groupes
    FOR EACH ROW
EXECUTE PROCEDURE logiciel.taille_groupe();

-----------------------------------------------------------
--3

CREATE OR REPLACE FUNCTION logiciel.retirer_etudiant(_etudiant INTEGER, _identifiant_projet VARCHAR)
    RETURNS BOOLEAN AS
$$
DECLARE
    _num_projet INTEGER;
BEGIN
    SELECT logiciel.chercher_id_projet(_identifiant_projet) INTO _num_projet;

    DELETE
    FROM logiciel.inscriptions_groupes ig
    WHERE ig.etudiant = _etudiant
      AND ig.projet = _num_projet;
    RETURN TRUE;
end;
$$ LANGUAGE plpgsql;

--TRIGGER décrémenter nb etudiant
CREATE OR REPLACE FUNCTION logiciel.decrementer_nb_etudiants()
    RETURNS TRIGGER AS
$$
BEGIN
    UPDATE logiciel.groupes g
    SET nombre_inscrits = nombre_inscrits - 1,
        complet         = FALSE
    WHERE g.id_groupe = OLD.groupe;
    RETURN OLD;
end;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_dec_nb_etudiant
    AFTER DELETE
    on logiciel.inscriptions_groupes
    FOR EACH ROW
EXECUTE PROCEDURE logiciel.decrementer_nb_etudiants();

CREATE OR REPLACE FUNCTION logiciel.groupe_deja_valide()
    RETURNS TRIGGER AS
$$
DECLARE
    est_valide BOOLEAN;
BEGIN
    SELECT g.valide
    FROM logiciel.groupes g,
         logiciel.inscriptions_groupes ig
    WHERE OLD.id_inscription_groupe = ig.id_inscription_groupe
      AND ig.groupe = g.id_groupe
    INTO est_valide;

    IF (est_valide = TRUE) THEN
        RAISE 'Le groupe est déjà validé, impossible de quitter le groupe';
    end if;
    RETURN NEW;
end;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_est_groupe_valide
    BEFORE DELETE
    on logiciel.inscriptions_groupes
    FOR EACH ROW
EXECUTE PROCEDURE logiciel.groupe_deja_valide();

CREATE OR REPLACE FUNCTION logiciel.etudiant_est_dans_groupe()
    RETURNS TRIGGER AS
$$
DECLARE
    _id_etudiant INTEGER;
BEGIN
    SELECT ig.etudiant
    FROM logiciel.inscriptions_groupes ig
    WHERE ig.id_inscription_groupe = OLD.id_inscription_groupe
    INTO _id_etudiant;

    IF (_id_etudiant IS NULL) THEN
        RAISE 'Vous n êtes pas inscrit dans ce groupe de ce projet';
    end if;
    RETURN NEW;
end;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_etudiant_est_dans_groupe
    BEFORE DELETE
    on logiciel.inscriptions_groupes
    FOR EACH ROW
EXECUTE PROCEDURE logiciel.etudiant_est_dans_groupe();


-------------------------------------------------------------------------
--4 Visualiser tous les projets des cours inscrits
CREATE OR REPLACE VIEW logiciel.afficher_lesProjets_d_etudiant AS
SELECT ig.etudiant          as id_etudiant,
       p.identifiant_projet as Identifiant_projet,
       p.nom                as Nom,
       p.cours              as Identifiant_cours,
       g.num_groupe         as Num_groupe
FROM logiciel.projets p
         LEFT OUTER JOIN logiciel.groupes g ON p.num_projet = g.projet
         LEFT OUTER JOIN logiciel.inscriptions_groupes ig ON g.id_groupe = ig.groupe
ORDER BY p.identifiant_projet;


-------------------------------------------------------------------------
--5 Visualiser tous les projets où il n'a pas de groupe

CREATE OR REPLACE VIEW logiciel.afficher_projets_pas_encore_de_groupe AS
SELECT DISTINCT ic.etudiant          as "id etudiant",
                p.identifiant_projet as "Identifiant projet",
                p.nom                as "Nom",
                c.code_cours         as "Identifiant cours",
                p.date_debut         as "Date début",
                p.date_fin           as "Date fin"
FROM logiciel.projets p
         LEFT OUTER JOIN logiciel.groupes g ON p.num_projet = g.projet
         LEFT OUTER JOIN logiciel.cours c ON p.cours = c.id_cours
         LEFT OUTER JOIN logiciel.inscriptions_cours ic on c.id_cours = ic.cours
WHERE NOT EXISTS(SELECT ig.groupe
                 FROM logiciel.inscriptions_groupes ig
                 WHERE ig.projet = p.num_projet
                   AND ig.etudiant = ic.etudiant
                 ORDER BY p.cours);


-----------------------------------------------------------------------------
-----6 Visualiser toutes les compositions de groupes incomplets d'un projet

CREATE OR REPLACE VIEW logiciel.afficher_composition_groupes_incomplets AS
SELECT e.id_etudiant,
       g.num_groupe                        as "Num groupe",
       p.identifiant_projet                as "Identifiant projet",
       e.nom                               as "Nom",
       e.prenom                            as "Prénom",
       g.taille_groupe - g.nombre_inscrits as "Nombre de place restantes"

FROM logiciel.projets p,
     logiciel.etudiants e,
     logiciel.groupes g,
     logiciel.inscriptions_groupes ig
WHERE p.num_projet = g.projet
  AND g.valide = FALSE
  AND g.complet = FALSE
  AND e.id_etudiant = ig.etudiant
  AND ig.projet = p.num_projet
group by p.identifiant_projet, g.num_groupe, e.nom, e.prenom, g.taille_groupe, g.nombre_inscrits, e.id_etudiant
ORDER BY g.num_groupe;