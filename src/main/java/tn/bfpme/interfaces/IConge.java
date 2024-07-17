package tn.bfpme.interfaces;

import tn.bfpme.models.Conge;
import tn.bfpme.models.Statut;

import java.util.List;

public interface IConge<C> {
    List<Conge> afficher();
    void Add (C c);
    void updateConge(Conge conge);
    void updateStatutConge(int id, Statut statut);

    void updateSoldeAnnuel(int id, int solde);

    void updateSoldeAnnuel(int id, double solde);

    void updateSoldeMaladie(int id, int solde);

    void updateSoldeMaladie(int id, double solde);

    void updateSoldeExceptionnel(int id, int solde);

    void updateSoldeExceptionnel(int id, double solde);

    void updateSoldeMaternité(int id, int solde);


    void updateSoldeMaternité(int id, double solde);

    void deleteConge(Conge conge);
    void deleteCongeByID(int id);

    List<Conge> TriparStatut();

    List<Conge> TriparType();

    List<Conge> TriparDateD();

    List<Conge> TriparDateF();

    List<Conge> TriparDesc();

    List<Conge> Rechreche(String recherche);
}
