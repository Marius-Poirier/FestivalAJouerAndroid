package com.example.frontend.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey
    val id: Int,

    @ColumnInfo(name = "editeur_id")           
    val editeurId: Int,
    
    @ColumnInfo(name = "festival_id")          
    val festivalId: Int,

    // Statut stocké en String car pas d'enum en room conversion en enume dans converters
    @ColumnInfo(name = "statut_workflow")      
    val statutWorkflow: String?,

    @ColumnInfo(name = "editeur_presente_jeux") 
    val editeurPresenteJeux: Boolean,

    @ColumnInfo(name = "remise_pourcentage")   
    val remisePourcentage: Double?,

    @ColumnInfo(name = "remise_montant")       
    val remiseMontant: Double?,

    @ColumnInfo(name = "prix_total")           
    val prixTotal: Double?,
    
    @ColumnInfo(name = "prix_final")           
    val prixFinal: Double?,

    @ColumnInfo(name = "commentaires_paiement") 
    val commentairesPaiement: String?,

    @ColumnInfo(name = "paiement_relance")     
    val paiementRelance: Boolean,

    @ColumnInfo(name = "date_facture")         
    val dateFacture: String?,
    
    @ColumnInfo(name = "date_paiement")        
    val datePaiement: String?
)
