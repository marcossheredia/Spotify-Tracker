package com.tfg.spotifytracker.service;

import com.tfg.spotifytracker.dto.spotify.response.SpotifyCompatibilityStatusDTO;
import com.tfg.spotifytracker.exception.SpotifyApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpotifyCompatibilityService {

    private final SpotifyApiClient spotifyApiClient;
    private final SpotifyDtoMapper mapper;
    private final SpotifyLibraryCompatibilityService spotifyLibraryCompatibilityService;

    public SpotifyCompatibilityStatusDTO getStatus(String accessToken) {
        List<String> warnings = new ArrayList<>();

        String spotifyUserId = null;
        String product = "unknown";
        boolean canReadProfile = false;
        boolean canReadTopItems = false;
        boolean canReadLibrary = false;
        boolean canModifyLibrary = false;
        boolean canCreatePlaylist = false;
        boolean canControlPlayback = false;

        try {
            Map<String, Object> me = spotifyApiClient.getMap(accessToken, "/me");
            spotifyUserId = mapper.asString(me.get("id"));
            String productValue = mapper.asString(me.get("product"));
            if (StringUtils.hasText(productValue)) {
                product = productValue;
            }
            canReadProfile = true;
        } catch (SpotifyApiException ex) {
            warnings.add(buildWarning("profile", ex));
        }

        try {
            spotifyApiClient.getMap(accessToken, "/me/top/tracks?limit=1");
            canReadTopItems = true;
        } catch (SpotifyApiException ex) {
            warnings.add(buildWarning("top_items", ex));
        }

        try {
            Map<String, Object> tracks = spotifyApiClient.getMap(accessToken, "/me/tracks?limit=1");
            canReadLibrary = true;
            String trackId = null;
            Object itemsObj = tracks.get("items");
            if (itemsObj instanceof List<?> items && !items.isEmpty()) {
                Map<String, Object> firstItem = mapper.asMap(items.get(0));
                Map<String, Object> track = mapper.asMap(firstItem.get("track"));
                trackId = mapper.asString(track.get("id"));
            }

            if (StringUtils.hasText(trackId)) {
                try {
                    spotifyLibraryCompatibilityService.containsTracks(accessToken, List.of(trackId));
                    canModifyLibrary = true;
                } catch (SpotifyApiException ex) {
                    warnings.add(buildWarning("library_modify", ex));
                }
            } else {
                warnings.add("library_modify: No hay tracks guardados para validar permisos de modificacion.");
            }
        } catch (SpotifyApiException ex) {
            warnings.add(buildWarning("library_read", ex));
        }

        try {
            spotifyApiClient.getMap(accessToken, "/me/playlists?limit=1");
            canCreatePlaylist = true;
        } catch (SpotifyApiException ex) {
            warnings.add(buildWarning("playlists", ex));
        }

        try {
            spotifyApiClient.getMap(accessToken, "/me/player");
            canControlPlayback = true;
        } catch (SpotifyApiException ex) {
            warnings.add(buildWarning("player", ex));
        }

        return SpotifyCompatibilityStatusDTO.builder()
            .spotifyUserId(spotifyUserId)
            .product(product)
            .canReadProfile(canReadProfile)
            .canReadTopItems(canReadTopItems)
            .canReadLibrary(canReadLibrary)
            .canModifyLibrary(canModifyLibrary)
            .canCreatePlaylist(canCreatePlaylist)
            .canControlPlayback(canControlPlayback)
            .warnings(warnings)
            .build();
    }

    private String buildWarning(String area, SpotifyApiException ex) {
        Integer status = ex.getStatusCode();
        String statusText = status != null ? String.valueOf(status) : "unknown";
        String category = ex.getSpotifyErrorCategory();
        String categoryText = StringUtils.hasText(category) ? " (" + category + ")" : "";

        return area + ": Spotify devolvio status " + statusText + categoryText + ".";
    }
}

