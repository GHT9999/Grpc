package ma.projet.grpc.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.hibernate.annotations.GenericGenerator;

@Entity
public class Compte {
    @Id
    @GeneratedValue(generator = "uuid") // Use UUID generator
    @GenericGenerator(name = "uuid", strategy = "uuid2") // Specify UUID strategy
    private String id;

    private float solde;
    private String dateCreation;
    private String type;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getSolde() {
        return solde;
    }

    public void setSolde(float solde) {
        this.solde = solde;
    }

    public String getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type=type;
}
}