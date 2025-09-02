package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сгенерированный jwt")
public record JwtResponse(
    @Schema(description = "jwt", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI5N2UwZjJlNC0wMzM5LTRlODQtOGQ1OS0zOGM1YjY0NjMzMDIiLCJyb2xlIjoiVVNFUiIsImlhdCI6MTc1NjgyMTUyNSwiZXhwIjoxNzU2ODI1MTI1fQ.ImL_47MNFoHI8lUhpWdlhfWJmRlZgu-I7PbudmVcaBrdQOp5GnaCqxkggEO7BRk6ic_Eq3YbOhlNH-YNatLvow")
    String jwt
) {}
