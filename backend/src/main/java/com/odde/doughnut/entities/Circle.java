package com.odde.doughnut.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Entity
@Table(name = "circle")
public class Circle {
    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Size(min = 1, max = 255)
    @Getter
    @Setter
    private String name;

    @Getter
    @Column(name = "invitation_code")
    @JsonIgnore
    private final String invitationCode = generateRandomInvitationCode(15);

    @JoinTable(name = "circle_user", joinColumns = {
            @JoinColumn(name = "circle_id", referencedColumnName = "id", nullable = false)}, inverseJoinColumns = {
            @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    })
    @ManyToMany
    @JsonIgnore
    @Getter
    private final List<User> members = new ArrayList<>();

    @OneToOne(mappedBy = "circle", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    @JsonIgnore
    @Getter @Setter private Ownership ownership = new Ownership();

    public Circle() {
        ownership.setCircle(this);
    }

    private static String generateRandomInvitationCode(int targetStringLength) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        return  random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

}
