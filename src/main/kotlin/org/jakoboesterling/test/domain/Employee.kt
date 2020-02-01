package org.jakoboesterling.test.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.io.Serializable
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy

/**
 * The Employee entity.
 */
@ApiModel(description = "The Employee entity.")
@Entity
@Table(name = "employee")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class Employee(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    var id: Long? = null,

    /**
     * The firstname attribute.
     */
    @ApiModelProperty(value = "The firstname attribute.")
    @Column(name = "first_name")
    var firstName: String? = null,

    @Column(name = "last_name")
    var lastName: String? = null,

    @Column(name = "email")
    var email: String? = null,

    @Column(name = "phone_number")
    var phoneNumber: String? = null,

    @Column(name = "hire_date")
    var hireDate: Instant? = null,

    @Column(name = "salary")
    var salary: Long? = null,

    @Column(name = "commission_pct")
    var commissionPct: Long? = null,

    @OneToMany(mappedBy = "employee")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    var jobs: MutableSet<Job> = mutableSetOf(),

    @ManyToOne @JsonIgnoreProperties("employees")
    var manager: Employee? = null,

    /**
     * Another side of the same relationship
     */
    @ApiModelProperty(value = "Another side of the same relationship")
    @ManyToOne @JsonIgnoreProperties("employees")
    var department: Department? = null

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
) : Serializable {

    fun addJob(job: Job): Employee {
        this.jobs.add(job)
        job.employee = this
        return this
    }

    fun removeJob(job: Job): Employee {
        this.jobs.remove(job)
        job.employee = null
        return this
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Employee) return false
        if (other.id == null || id == null) return false

        return id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "Employee{" +
        "id=$id" +
        ", firstName='$firstName'" +
        ", lastName='$lastName'" +
        ", email='$email'" +
        ", phoneNumber='$phoneNumber'" +
        ", hireDate='$hireDate'" +
        ", salary=$salary" +
        ", commissionPct=$commissionPct" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
