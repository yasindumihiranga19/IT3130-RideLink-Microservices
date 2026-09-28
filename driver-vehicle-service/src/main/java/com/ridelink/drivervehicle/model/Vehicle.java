package com.ridelink.drivervehicle.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "driver_id", nullable = false, unique = true)
    @JsonIgnore
    private Driver driver;

    @Column(name = "vehicle_number", nullable = false, unique = true)
    @NotBlank
    private String vehicleNumber;

    @Column(nullable = false)
    @NotBlank
    private String type;

    @NotBlank
    private String model;

    private String color;

    @Column(nullable = false)
    @Min(1)
    private int capacity;
}