import { ref } from "vue";
import { ProfileService } from "../application/ProfileService";
import { SpotifyProfileApiClient } from "../infrastructure/SpotifyProfileApiClient";
import { resolveSpotifyErrorMessage } from "@/shared/services/spotifyApiErrors";

const defaultService = new ProfileService(new SpotifyProfileApiClient());

export function useProfile(profileService = defaultService) {
  const profile = ref(null);
  const loading = ref(false);
  const error = ref("");

  async function loadProfile() {
    loading.value = true;
    error.value = "";

    try {
      profile.value = await profileService.getProfile();
    } catch (requestError) {
      profile.value = null;
      error.value = resolveSpotifyErrorMessage(
        requestError,
        "No se pudo cargar el perfil de Spotify."
      );
    } finally {
      loading.value = false;
    }
  }

  return { profile, loading, error, loadProfile };
}
