package org.jakoboesterling.test.domain

import io.swagger.annotations.ApiModel
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy

/**
 * not an ignored comment
 */
@ApiModel(description = "not an ignored comment")
@Entity
@Table(name = "location")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class Location(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    var id: Long? = null,

    @Column(name = "street_address")
    var streetAddress: String? = null,

    @Column(name = "postal_code")
    var postalCode: String? = null,

    @Column(name = "city")
    var city: String? = null,

    @Column(name = "state_province")
    var stateProvince: String? = null,

    @OneToOne @JoinColumn(unique = true)
    var country: Country? = null

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
) : Serializable {
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Location) return false
        if (other.id == null || id == null) return false

        return id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "Location{" +
        "id=$id" +
        ", streetAddress='$streetAddress'" +
        ", postalCode='$postalCode'" +
        ", city='$city'" +
        ", stateProvince='$stateProvince'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
