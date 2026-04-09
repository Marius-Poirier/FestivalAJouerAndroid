package com.example.frontend.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// cette table stocke les jeux qui seront presenter dans le festivale avec toute les informations 
// des jeux et de l'editeur 
// correspond a JeuFestivalViewDto
@Entity(tableName = "jeux_festival")
data class JeuFestivalEntity(
    @PrimaryKey
    val id: Int,

    @ColumnInfo(name = "jeu_id")             
    val jeuId: Int,

    @ColumnInfo(name = "reservation_id")     
    val reservationId: Int,

    @ColumnInfo(name = "festival_id")        
    val festivalId: Int,

    @ColumnInfo(name = "dans_liste_demandee") 
    val dansListeDemandee: Boolean,

    @ColumnInfo(name = "dans_liste_obtenue") 
    val dansListeObtenue: Boolean,
    
    // information jeu
    @ColumnInfo(name = "jeux_recu")          
    val jeuxRecu: Boolean,

    @ColumnInfo(name = "jeu_nom")            
    val jeuNom: String?,

    @ColumnInfo(name = "type_jeu_nom")       
    val typeJeuNom: String?,
    
    @ColumnInfo(name = "nb_joueurs_min")     
    val nbJoueursMin: Int?,
    
    @ColumnInfo(name = "nb_joueurs_max")     
    val nbJoueursMax: Int?,
    
    @ColumnInfo(name = "duree_minutes")      
    val dureeMinutes: Int?,
    
    @ColumnInfo(name = "age_min")            
    val ageMin: Int?,

    @ColumnInfo(name = "url_image")          
    val urlImage: String?,

    val prototype: Boolean?,

    // information editeur
    @ColumnInfo(name = "editeur_id")         
    val editeurId: Int,

    @ColumnInfo(name = "editeur_nom")        
    val editeurNom: String?
)
