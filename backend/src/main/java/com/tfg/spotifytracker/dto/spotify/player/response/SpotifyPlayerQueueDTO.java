package com.tfg.spotifytracker.dto.spotify.player.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SpotifyPlayerQueueDTO {
    private SpotifyNowPlayingDTO currentlyPlaying;
    private List<SpotifyNowPlayingDTO> queue;
}
