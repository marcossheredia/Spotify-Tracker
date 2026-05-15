package com.tfg.spotifytracker.service;


import com.tfg.spotifytracker.dto.spotify.common.SpotifyAlbumDTO;
import com.tfg.spotifytracker.dto.spotify.common.SpotifyArtistDTO;
import com.tfg.spotifytracker.dto.spotify.playlist.response.SpotifyPlaylistDTO;
import com.tfg.spotifytracker.dto.spotify.search.response.SpotifySearchResultDTO;
import com.tfg.spotifytracker.dto.spotify.common.SpotifyTrackDTO;
import com.tfg.spotifytracker.exception.SpotifyApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class SpotifySearchService {

    private final SpotifyApiClient spotifyApiClient;
    private final SpotifyDtoMapper mapper;

    public SpotifySearchResultDTO search(String accessToken, String query, String types, int limit, int offset) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        int safeOffset = Math.max(0, offset);
        String safeTypes = resolveTypes(types);
        String safeQuery = query == null ? "" : query.trim();

        if (!StringUtils.hasText(safeQuery) || safeQuery.length() < 3) {
            return SpotifySearchResultDTO.builder()
                .query(query)
                .tracks(List.of())
                .artists(List.of())
                .albums(List.of())
                .playlists(List.of())
                .build();
        }

        Map<String, Object> response = spotifyApiClient.getMap(
            accessToken,
            buildSearchUri(safeQuery, safeTypes, safeLimit, safeOffset, true, true)
        );

        return SpotifySearchResultDTO.builder()
            .query(query)
            .tracks(extractTracks(response))
            .artists(extractArtists(response))
            .albums(extractAlbums(response))
            .playlists(extractPlaylists(response))
            .build();
    }

    private String resolveTypes(String rawTypes) {
        if (rawTypes == null || rawTypes.isBlank()) {
            return "track,artist,album,playlist";
        }
        return rawTypes;
    }

    private String buildSearchUri(String query,
                                  String types,
                                  Integer limit,
                                  int offset,
                                  boolean includeMarket,
                                  boolean includeLimit) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/search")
            .queryParam("q", query)
            .queryParam("type", types)
            .queryParam("offset", offset);

        if (includeLimit && limit != null) {
            builder.queryParam("limit", limit);
        }

        if (includeMarket) {
            builder.queryParam("market", "from_token");
        }

        return builder.build().encode().toUriString();
    }

    private List<SpotifyTrackDTO> extractTracks(Map<String, Object> response) {
        Map<String, Object> tracksPage = mapper.asMap(response.get("tracks"));
        List<SpotifyTrackDTO> tracks = new ArrayList<>();
        for (Map<String, Object> item : mapper.extractItems(tracksPage)) {
            tracks.add(mapper.toTrack(item));
        }
        return tracks;
    }

    private List<SpotifyArtistDTO> extractArtists(Map<String, Object> response) {
        Map<String, Object> artistsPage = mapper.asMap(response.get("artists"));
        List<SpotifyArtistDTO> artists = new ArrayList<>();
        for (Map<String, Object> item : mapper.extractItems(artistsPage)) {
            artists.add(mapper.toArtist(item));
        }
        return artists;
    }

    private List<SpotifyAlbumDTO> extractAlbums(Map<String, Object> response) {
        Map<String, Object> albumsPage = mapper.asMap(response.get("albums"));
        List<SpotifyAlbumDTO> albums = new ArrayList<>();
        for (Map<String, Object> item : mapper.extractItems(albumsPage)) {
            albums.add(mapper.toAlbum(item));
        }
        return albums;
    }

    private List<SpotifyPlaylistDTO> extractPlaylists(Map<String, Object> response) {
        Map<String, Object> playlistsPage = mapper.asMap(response.get("playlists"));
        List<SpotifyPlaylistDTO> playlists = new ArrayList<>();
        for (Map<String, Object> item : mapper.extractItems(playlistsPage)) {
            playlists.add(mapper.toPlaylist(item));
        }
        return playlists;
    }
}
