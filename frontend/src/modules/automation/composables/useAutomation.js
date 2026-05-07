import { ref } from "vue";
import { AutomationService } from "../application/AutomationService";
import { SpotifyAutomationApiClient } from "../infrastructure/SpotifyAutomationApiClient";
import { resolveSpotifyErrorMessage } from "@/shared/services/spotifyApiErrors";

const defaultService = new AutomationService(new SpotifyAutomationApiClient());

export function useAutomation(automationService = defaultService) {
  const loading = ref(false);
  const error = ref("");
  const result = ref(null);

  async function createTopTracksPlaylist(payload) {
    loading.value = true;
    error.value = "";

    try {
      result.value = await automationService.createTopTracksPlaylist(payload);
      return result.value;
    } catch (requestError) {
      result.value = null;
      error.value = resolveSpotifyErrorMessage(
        requestError,
        "No se pudo crear la playlist automatica."
      );
      return null;
    } finally {
      loading.value = false;
    }
  }

  return {
    loading,
    error,
    result,
    createTopTracksPlaylist,
  };
}
