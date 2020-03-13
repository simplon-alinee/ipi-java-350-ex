package com.ipiecoles.java.java350.service;

import com.ipiecoles.java.java350.exception.EmployeException;
import com.ipiecoles.java.java350.model.Employe;
import com.ipiecoles.java.java350.model.Entreprise;
import com.ipiecoles.java.java350.model.NiveauEtude;
import com.ipiecoles.java.java350.model.Poste;
import com.ipiecoles.java.java350.repository.EmployeRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityExistsException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeServiceTest {

    @InjectMocks
    EmployeService employeService;

    @Mock
    EmployeRepository employeRepository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this.getClass());
    }

    @Test
    public void testEmbaucheEmployeTechnicienPleinTempsBts() throws EmployeException {
        //Given
        String nom = "Doe";
        String prenom = "John";
        Poste poste = Poste.TECHNICIEN;
        NiveauEtude niveauEtude = NiveauEtude.BTS_IUT;
        Double tempsPartiel = 1.0;
        when(employeRepository.findLastMatricule()).thenReturn("00345");
        when(employeRepository.findByMatricule("T00346")).thenReturn(null);

        //When
        employeService.embaucheEmploye(nom, prenom, poste, niveauEtude, tempsPartiel);

        //Then
        ArgumentCaptor<Employe> employeArgumentCaptor = ArgumentCaptor.forClass(Employe.class);
        verify(employeRepository, times(1)).save(employeArgumentCaptor.capture());
        Assertions.assertEquals(nom, employeArgumentCaptor.getValue().getNom());
        Assertions.assertEquals(prenom, employeArgumentCaptor.getValue().getPrenom());
        Assertions.assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), employeArgumentCaptor.getValue().getDateEmbauche().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        Assertions.assertEquals("T00346", employeArgumentCaptor.getValue().getMatricule());
        Assertions.assertEquals(tempsPartiel, employeArgumentCaptor.getValue().getTempsPartiel());

        //1521.22 * 1.2 * 1.0
        Assertions.assertEquals(1825.46, employeArgumentCaptor.getValue().getSalaire().doubleValue());
    }

    @Test
    public void testEmbaucheEmployeManagerMiTempsMaster() throws EmployeException {
        //Given
        String nom = "Doe";
        String prenom = "John";
        Poste poste = Poste.MANAGER;
        NiveauEtude niveauEtude = NiveauEtude.MASTER;
        Double tempsPartiel = 0.5;
        when(employeRepository.findLastMatricule()).thenReturn("00345");
        when(employeRepository.findByMatricule("M00346")).thenReturn(null);

        //When
        employeService.embaucheEmploye(nom, prenom, poste, niveauEtude, tempsPartiel);

        //Then
        ArgumentCaptor<Employe> employeArgumentCaptor = ArgumentCaptor.forClass(Employe.class);
        verify(employeRepository, times(1)).save(employeArgumentCaptor.capture());
        Assertions.assertEquals(nom, employeArgumentCaptor.getValue().getNom());
        Assertions.assertEquals(prenom, employeArgumentCaptor.getValue().getPrenom());
        Assertions.assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), employeArgumentCaptor.getValue().getDateEmbauche().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        Assertions.assertEquals("M00346", employeArgumentCaptor.getValue().getMatricule());
        Assertions.assertEquals(tempsPartiel, employeArgumentCaptor.getValue().getTempsPartiel());

        //1521.22 * 1.4 * 0.5
        Assertions.assertEquals(1064.85, employeArgumentCaptor.getValue().getSalaire().doubleValue());
    }

    @Test
    public void testEmbaucheEmployeManagerMiTempsMasterNoLastMatricule() throws EmployeException {
        //Given
        String nom = "Doe";
        String prenom = "John";
        Poste poste = Poste.MANAGER;
        NiveauEtude niveauEtude = NiveauEtude.MASTER;
        Double tempsPartiel = 0.5;
        when(employeRepository.findLastMatricule()).thenReturn(null);
        when(employeRepository.findByMatricule("M00001")).thenReturn(null);

        //When
        employeService.embaucheEmploye(nom, prenom, poste, niveauEtude, tempsPartiel);

        //Then
        ArgumentCaptor<Employe> employeArgumentCaptor = ArgumentCaptor.forClass(Employe.class);
        verify(employeRepository, times(1)).save(employeArgumentCaptor.capture());
        Assertions.assertEquals("M00001", employeArgumentCaptor.getValue().getMatricule());
    }

    @Test
    public void testEmbaucheEmployeManagerMiTempsMasterExistingEmploye() {
        //Given
        String nom = "Doe";
        String prenom = "John";
        Poste poste = Poste.MANAGER;
        NiveauEtude niveauEtude = NiveauEtude.MASTER;
        Double tempsPartiel = 0.5;
        when(employeRepository.findLastMatricule()).thenReturn(null);
        when(employeRepository.findByMatricule("M00001")).thenReturn(new Employe());

        //When/Then
        EntityExistsException e = Assertions.assertThrows(EntityExistsException.class, () -> employeService.embaucheEmploye(nom, prenom, poste, niveauEtude, tempsPartiel));
        Assertions.assertEquals("L'employé de matricule M00001 existe déjà en BDD", e.getMessage());
    }

    @Test
    public void testEmbaucheEmployeManagerMiTempsMaster99999() {
        //Given
        String nom = "Doe";
        String prenom = "John";
        Poste poste = Poste.MANAGER;
        NiveauEtude niveauEtude = NiveauEtude.MASTER;
        Double tempsPartiel = 0.5;
        when(employeRepository.findLastMatricule()).thenReturn("99999");

        //When/Then
        EmployeException e = Assertions.assertThrows(EmployeException.class, () -> employeService.embaucheEmploye(nom, prenom, poste, niveauEtude, tempsPartiel));
        Assertions.assertEquals("Limite des 100000 matricules atteinte !", e.getMessage());
    }

    //TODO
    @Test
    public void testCalculPerformanceCommercialInferieur20() throws EmployeException {
        //Given
        String matricule = "C00001";
        Long caTraite = 500L;
        Long objectifCa = 1000L;
        Employe e = new Employe();
        e.setNom("Doe")
                .setPrenom("John")
                .setMatricule(matricule)
                .setDateEmbauche(LocalDate.now())
                .setSalaire(Entreprise.SALAIRE_BASE)
                .setPerformance(1)
                .setTempsPartiel(1D);
        when(employeRepository.findByMatricule("C00001")).thenReturn(e);
        when(employeRepository.avgPerformanceWhereMatriculeStartsWith("C")).thenReturn(1D);
        Integer newPerf = employeService.calculPerformanceCommercial(matricule, caTraite, objectifCa);
        //When
        e.setPerformance(newPerf);

        //Then
        Assertions.assertEquals(1, e.getPerformance().intValue());
    }

    @Test
    public void testCalculPerformanceCommercialInferieur5à20() throws EmployeException {
        //Given
        String matricule = "C00001";
        Long caTraite = 900L;
        Long objectifCa = 1000L;
        Employe e = new Employe();
        e.setNom("Doe")
                .setPrenom("John")
                .setMatricule(matricule)
                .setDateEmbauche(LocalDate.now())
                .setSalaire(Entreprise.SALAIRE_BASE)
                .setPerformance(1)
                .setTempsPartiel(1D);
        when(employeRepository.findByMatricule("C00001")).thenReturn(e);
        when(employeRepository.avgPerformanceWhereMatriculeStartsWith("C")).thenReturn(1D);
        Integer newPerf = employeService.calculPerformanceCommercial(matricule, caTraite, objectifCa);
        //When
        e.setPerformance(newPerf);

        //Then
        Assertions.assertEquals(1, e.getPerformance().intValue());
    }

    @Test
    public void testCalculPerformanceCommercialEntreMoins5Plus5() throws EmployeException {
        //Given
        String matricule = "C00001";
        Long caTraite = 1100L;
        Long objectifCa = 1100L;
        Employe e = new Employe();
        e.setNom("Doe")
                .setPrenom("John")
                .setMatricule(matricule)
                .setDateEmbauche(LocalDate.now())
                .setSalaire(Entreprise.SALAIRE_BASE)
                .setPerformance(1)
                .setTempsPartiel(1D);
        when(employeRepository.findByMatricule("C00001")).thenReturn(e);
        when(employeRepository.avgPerformanceWhereMatriculeStartsWith("C")).thenReturn(1D);
        Integer newPerf = employeService.calculPerformanceCommercial(matricule, caTraite, objectifCa);
        //When
        e.setPerformance(newPerf);

        //Then
        Assertions.assertEquals(1, e.getPerformance().intValue());
    }

    @Test
    public void testCalculPerformanceCommercialSuperieur5à20() throws EmployeException {
        //Given
        String matricule = "C00001";
        Long caTraite = 1100L;
        Long objectifCa = 1000L;
        Employe e = new Employe();
        e.setNom("Doe")
                .setPrenom("John")
                .setMatricule(matricule)
                .setDateEmbauche(LocalDate.now())
                .setSalaire(Entreprise.SALAIRE_BASE)
                .setPerformance(1)
                .setTempsPartiel(1D);
        when(employeRepository.findByMatricule("C00001")).thenReturn(e);
        when(employeRepository.avgPerformanceWhereMatriculeStartsWith("C")).thenReturn(1D);
        Integer newPerf = employeService.calculPerformanceCommercial(matricule, caTraite, objectifCa);
        //When
        e.setPerformance(newPerf);

        //Then
        Assertions.assertEquals(3, e.getPerformance().intValue());
    }

    @Test
    public void testCalculPerformanceCommercialSuperieur20() throws EmployeException {
        //Given
        String matricule = "C00001";
        Long caTraite = 1300L;
        Long objectifCa = 1000L;
        Employe e = new Employe();
        e.setNom("Doe")
                .setPrenom("John")
                .setMatricule(matricule)
                .setDateEmbauche(LocalDate.now())
                .setSalaire(Entreprise.SALAIRE_BASE)
                .setPerformance(1)
                .setTempsPartiel(1D);
        when(employeRepository.findByMatricule("C00001")).thenReturn(e);
        when(employeRepository.avgPerformanceWhereMatriculeStartsWith("C")).thenReturn(1D);
        Integer newPerf = employeService.calculPerformanceCommercial(matricule, caTraite, objectifCa);
        //When
        e.setPerformance(newPerf);

        //Then
        Assertions.assertEquals(6, e.getPerformance().intValue());
    }

    @Test
    public void testCalculPerformanceCommercialAvecMatriculeNull() throws EmployeException {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
            employeService.calculPerformanceCommercial(null, 100L, 100L);
        }).isInstanceOf(EmployeException.class).hasMessage("Le matricule ne peut être null et doit commencer par un C !");
    }

    @Test
    public void testCalculPerformanceCommercialFauxMatricule() throws EmployeException {
        String matricule = "C12345";
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
            employeService.calculPerformanceCommercial("C12345", 100L, 100L);
        }).isInstanceOf(EmployeException.class).hasMessage("Le matricule " + matricule + " n'existe pas !");
    }

    @Test
    public void testCalculPerformanceCommercialNegatifCaTraite() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
            employeService.calculPerformanceCommercial("C12345", -100L, 100L);
        }).isInstanceOf(EmployeException.class).hasMessage("Le chiffre d'affaire traité ne peut être négatif ou null !");
    }

    @Test
    public void testCalculPerformanceCommercialNegatifObjectifCa() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
            employeService.calculPerformanceCommercial("C12345", 100L, -100L);
        }).isInstanceOf(EmployeException.class).hasMessage("L'objectif de chiffre d'affaire ne peut être négatif ou null !");
    }

}