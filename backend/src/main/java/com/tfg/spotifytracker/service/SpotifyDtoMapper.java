package com.tfg.spotifytracker.service;

import com.tfg.spotifytracker.dto.spotify.common.SpotifyAlbumDTO;
import com.tfg.spotifytracker.dto.spotify.common.SpotifyArtistDTO;
import com.tfg.spotifytracker.dto.spotify.player.response.SpotifyNowPlayingDTO;
import com.tfg.spotifytracker.dto.spotify.playlist.response.SpotifyPlaylistDTO;
import com.tfg.spotifytracker.dto.spotify.common.SpotifyTrackDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SpotifyDtoMapper {

    public SpotifyTrackDTO toTrack(Map<String, Object> track) {
        Map<String, Object> album = asMap(track.get("album"));
        Map<String, Object> externalUrls = asMap(track.get("external_urls"));
        String releaseDate = asString(album.get("release_date"));

        return SpotifyTrackDTO.builder()
            .id(asString(track.get("id")))
            .name(asString(track.get("name")))
            .imageUrl(extractImageUrl(album))
            .artists(extractNamedValues(track.get("artists"), "name"))
            .albumName(asString(album.get("name")))
            .releaseDate(releaseDate)
            .releaseYear(extractYear(releaseDate))
            .durationMs(asNullableInteger(track.get("duration_ms")))
            .explicit(asBoolean(track.get("explicit")))
            .externalUrl(asString(externalUrls.get("spotify")))
            .popularity(asNullableInteger(track.get("popularity")))
            .build();
    }

    public SpotifyArtistDTO toArtist(Map<String, Object> artist) {
        Map<String, Object> followers = asMap(artist.get("followers"));
        Map<String, Object> externalUrls = asMap(artist.get("external_urls"));

        return SpotifyArtistDTO.builder()
            .id(asString(artist.get("id")))
            .name(asString(artist.get("name")))
            .imageUrl(extractImageUrl(artist))
            .followersTotal(asNullableInteger(followers.get("total")))
            .genres(asStringList(artist.get("genres")))
            .externalUrl(asString(externalUrls.get("spotify")))
            .popularity(asNullableInteger(artist.get("popularity")))
            .build();
    }

    public SpotifyAlbumDTO toAlbum(Map<String, Object> album) {
        Map<String, Object> externalUrls = asMap(album.get("external_urls"));

        return SpotifyAlbumDTO.builder()
            .id(asString(album.get("id")))
            .name(asString(album.get("name")))
            .imageUrl(extractImageUrl(album))
            .artists(extractNamedValues(album.get("artists"), "name"))
            .albumType(asString(album.get("album_type")))
            .totalTracks(asNullableInteger(album.get("total_tracks")))
            .releaseDate(asString(album.get("release_date")))
            .externalUrl(asString(externalUrls.get("spotify")))
            .build();
    }

    public SpotifyPlaylistDTO toPlaylist(Map<String, Object> playlist) {
        Map<String, Object> externalUrls = asMap(playlist.get("external_urls"));
        Map<String, Object> owner = asMap(playlist.get("owner"));

        return SpotifyPlaylistDTO.builder()
            .id(asString(playlist.get("id")))
            .name(asString(playlist.get("name")))
            .imageUrl(extractImageUrl(playlist))
            .tracksTotal(extractPlaylistTracksTotal(playlist))
            .publicPlaylist(asBoolean(playlist.get("public")))
            .ownerName(extractPlaylistOwnerName(playlist))
            .ownerId(asString(owner.get("id")))
            .externalUrl(asString(externalUrls.get("spotify")))
            .collaborative(Boolean.TRUE.equals(asBoolean(playlist.get("collaborative"))))
            .hasLikedTracks(false)
            .ownPlaylist(false)
            .build();
    }

    public SpotifyNowPlayingDTO toNowPlayingTrack(Map<String, Object> item, Map<String, Object> context) {
        if (item.isEmpty()) {
            return null;
        }

        Map<String, Object> album = asMap(item.get("album"));
        Map<String, Object> externalUrls = asMap(item.get("external_urls"));

        return SpotifyNowPlayingDTO.builder()
            .id(asString(item.get("id")))
            .name(asString(item.get("name")))
            .imageUrl(extractImageUrl(album))
            .artists(extractNamedValues(item.get("artists"), "name"))
            .albumName(asString(album.get("name")))
            .externalUrl(asString(externalUrls.get("spotify")))
            .isPlaying(asBoolean(context.get("is_playing")))
            .progressMs(asNullableInteger(context.get("progress_ms")))
            .durationMs(asNullableInteger(item.get("duration_ms")))
            .build();
    }

    public List<Map<String, Object>> extractItems(Map<String, Object> response) {
        Object itemsObj = response.get("items");
        if (!(itemsObj instanceof List<?> items)) {
            return List.of();
        }

        List<Map<String, Object>> parsedItems = new ArrayList<>();
        for (Object item : items) {
            parsedItems.add(asMap(item));
        }
        return parsedItems;
    }

    public Map<String, Object> asMap(Object value) {
        if (!(value instanceof Map<?, ?> rawMap)) {
            return Map.of();
        }

        Map<String, Object> map = new HashMap<>();
        rawMap.forEach((key, mapValue) -> {
            if (key != null) {
                map.put(String.valueOf(key), mapValue);
            }
        });
        return map;
    }

    public List<String> extractNamedValues(Object value, String fieldName) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        List<String> values = new ArrayList<>();
        for (Object entry : list) {
            Map<String, Object> map = asMap(entry);
            String fieldValue = asString(map.get(fieldName));
            if (StringUtils.hasText(fieldValue)) {
                values.add(fieldValue);
            }
        }
        return values;
    }

    public List<String> asStringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        List<String> values = new ArrayList<>();
        for (Object entry : list) {
            String textValue = asString(entry);
            if (StringUtils.hasText(textValue)) {
                values.add(textValue);
            }
        }
        return values;
    }

    public String extractImageUrl(Map<String, Object> source) {
        Object imagesObj = source.get("images");
        if (!(imagesObj instanceof List<?> images) || images.isEmpty()) {
            return null;
        }

        Map<String, Object> firstImage = asMap(images.get(0));
        return asString(firstImage.get("url"));
    }

    public Integer extractPlaylistTracksTotal(Map<String, Object> playlist) {
        Map<String, Object> tracks = asMap(playlist.get("tracks"));
        Integer tracksTotal = asNullableInteger(tracks.get("total"));
        if (tracksTotal != null && tracksTotal > 0) {
            return tracksTotal;
        }

        Map<String, Object> items = asMap(playlist.get("items"));
        Integer itemsTotal = asNullableInteger(items.get("total"));
        if (itemsTotal != null && itemsTotal > 0) {
            return itemsTotal;
        }

        Object itemsList = items.get("items");
        if (itemsList instanceof List<?> listItems) {
            return listItems.size();
        }

        return 0;
    }

    public String extractPlaylistOwnerName(Map<String, Object> playlist) {
        Map<String, Object> owner = asMap(playlist.get("owner"));
        String ownerDisplayName = asString(owner.get("display_name"));
        if (StringUtils.hasText(ownerDisplayName)) {
            return ownerDisplayName;
        }

        return asString(owner.get("id"));
    }

    public String asString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    public Integer asNullableInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }

        if (value instanceof String text && text.matches("\\d+")) {
            return Integer.parseInt(text);
        }

        return null;
    }

    public Boolean asBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }

        if (value instanceof String text && StringUtils.hasText(text)) {
            return Boolean.parseBoolean(text);
        }

        return null;
    }

    private Integer extractYear(String releaseDate) {
        if (!StringUtils.hasText(releaseDate)) {
            return null;
        }
        String trimmed = releaseDate.trim();
        if (trimmed.length() < 4) {
            return null;
        }
        String year = trimmed.substring(0, 4);
        return year.matches("\\d{4}") ? Integer.parseInt(year) : null;
    }
}
