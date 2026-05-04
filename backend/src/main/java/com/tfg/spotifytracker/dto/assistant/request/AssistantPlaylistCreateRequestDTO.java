package com.tfg.spotifytracker.dto.assistant.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssistantPlaylistCreateRequestDTO {
    @NotBlank(message = "El mensaje no puede estar vacio")
    private String message;

    @Min(value = 5, message = "trackLimit debe ser mayor o igual a 5")
    @Max(value = 50, message = "trackLimit debe ser menor o igual a 50")
    private Integer trackLimit;

    private Boolean publicPlaylist;
}
