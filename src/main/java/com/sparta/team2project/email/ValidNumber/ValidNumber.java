package com.sparta.team2project.email.ValidNumber;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class ValidNumber {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "valid_number_id")
    private Long id;

    @Column
    private int validNumber;

    @Column
    private String email;

    @Column
    private long time;

    public ValidNumber(int validNumber, String email, long time) {
        this.validNumber = validNumber;
        this.email = email;
        this.time = time;
    }

}
