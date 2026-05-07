import { ref } from "vue";
import { FollowService } from "../application/FollowService";
import { SpotifyFollowApiClient } from "../infrastructure/SpotifyFollowApiClient";
import { resolveSpotifyErrorMessage } from "@/shared/services/spotifyApiErrors";

const defaultService = new FollowService(new SpotifyFollowApiClient());

export function useFollowedArtists(followService = defaultService) {
  const artistsPage = ref({ items: [], limit: 20, nextCursor: "", hasNext: false });
  const loading = ref(false);
  const error = ref("");
  const mutating = ref(false);

  async function loadArtists(limit = 20, after = "") {
    loading.value = true;
    error.value = "";

    try {
      const response = await followService.getArtists(limit, after);
      artistsPage.value = after
        ? {
            ...response,
            items: [...(artistsPage.value.items || []), ...(response.items || [])],
          }
        : response;
    } catch (requestError) {
      artistsPage.value = { items: [], limit, nextCursor: "", hasNext: false };
      error.value = resolveSpotifyErrorMessage(requestError, "No se pudieron cargar tus artistas seguidos.");
    } finally {
      loading.value = false;
    }
  }

  async function toggleFollowArtist(artistId, followed) {
    if (!artistId || mutating.value) {
      return null;
    }

    mutating.value = true;
    error.value = "";

    try {
      if (followed) {
        return await followService.unfollowArtist(artistId);
      }
      return await followService.followArtist(artistId);
    } catch (requestError) {
      error.value = resolveSpotifyErrorMessage(requestError, "No se pudo actualizar el seguimiento del artista.");
      return null;
    } finally {
      mutating.value = false;
    }
  }

  return {
    artistsPage,
    loading,
    error,
    mutating,
    loadArtists,
    toggleFollowArtist,
  };
}
