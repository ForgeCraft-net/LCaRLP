package net.forgecraft.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PlayerData {
    private UUID uuid;
    private String passwordHash;
    private String username;
    //private String email;
    private Date registerDate;
}