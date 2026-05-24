package com.tfg.spotifytracker.dto.spotify.search.response;

import com.tfg.spotifytracker.dto.spotify.common.SpotifyAlbumDTO;
import com.tfg.spotifytracker.dto.spotify.common.SpotifyArtistDTO;
import com.tfg.spotifytracker.dto.spotify.common.SpotifyTrackDTO;
import com.tfg.spotifytracker.dto.spotify.playlist.response.SpotifyPlaylistDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
/**
 * Clase funcional: SpotifySearchResultDTO.
 * Representa datos que viajan entre capas o por la API.
 * Se conecta con: controladores y servicios que leen o devuelven estos datos.
 */
public class SpotifySearchResultDTO {
    private String query;
    private List<SpotifyTrackDTO> tracks;
    private List<SpotifyArtistDTO> artists;
    private List<SpotifyAlbumDTO> albums;
    private List<SpotifyPlaylistDTO> playlists;
}

