package org.jakoboesterling.test.domain

import java.io.Serializable
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.jakoboesterling.test.domain.enumeration.Language

/**
 * A JobHistory.
 */
@Entity
@Table(name = "job_history")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class JobHistory(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    var id: Long? = null,

    @Column(name = "start_date")
    var startDate: Instant? = null,

    @Column(name = "end_date")
    var endDate: Instant? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "language")
    var language: Language? = null,

    @OneToOne @JoinColumn(unique = true)
    var job: Job? = null,

    @OneToOne @JoinColumn(unique = true)
    var department: Department? = null,

    @OneToOne @JoinColumn(unique = true)
    var employee: Employee? = null

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
) : Serializable {
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JobHistory) return false
        if (other.id == null || id == null) return false

        return id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "JobHistory{" +
        "id=$id" +
        ", startDate='$startDate'" +
        ", endDate='$endDate'" +
        ", language='$language'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
