package be.ugent.equatic.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "user_roles")
public class Role implements Serializable {

    @Id
    @SequenceGenerator(name = "user_roles_seq_gen", sequenceName = "user_roles_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_roles_seq_gen")
    private long id;

    @Enumerated(EnumType.STRING)
    private Authority role;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Role() {
    }

    public Role(Authority role, User user) {
        this.role = role;
        this.user = user;
    }

    public Authority getRole() {
        return role;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return role.name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Role role1 = (Role) o;

        return role == role1.role;
    }

    @Override
    public int hashCode() {
        return role.hashCode();
    }
}

